package io.lpdf;

import io.lpdf.internal.LpdfModule;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import run.endive.runtime.ImportValues;
import run.endive.runtime.Instance;
import run.endive.runtime.WasmInterruptedException;
import run.endive.wasi.WasiExitException;
import run.endive.wasi.WasiOptions;
import run.endive.wasi.WasiPreview1;
import run.endive.wasm.WasmEngineException;

/**
 * Runs the engine's WASI module on Endive: a fresh instance for each call, the request on its standard input and the
 * answer from its standard output, both in memory. The module is the engine's WASM compiled to JVM bytecode when the
 * SDK was built, which loads once and is shared by every engine and thread.
 */
final class WasmRunner {
    /** The engine's classes, as Endive's Maven plugin compiled them. */
    static final Supplier<Instance.Builder> ENGINE =
            () -> Instance.builder(LpdfModule.load()).withMachineFactory(LpdfModule::create);

    private final Supplier<Instance.Builder> module;
    private final int timeout;
    private final int maxMemoryMb;

    /**
     * @param timeout     the seconds a call may run before it stops with an {@link EngineException}, or 0 for no limit
     * @param maxMemoryMb the megabytes a render may allocate
     */
    WasmRunner(int timeout, int maxMemoryMb) {
        this(ENGINE, timeout, maxMemoryMb);
    }

    /** A runner of {@code module} in place of the engine: for the tests, a module that runs long, or crashes. */
    WasmRunner(Supplier<Instance.Builder> module, int timeout, int maxMemoryMb) {
        this.module = module;
        this.timeout = timeout;
        this.maxMemoryMb = maxMemoryMb;
    }

    // ── Calls ─────────────────────────────────────────────────────────────────

    /** The PDF of a render or a stamp: {@code request} with the clock and the cap added. */
    byte[] pdf(Map<String, Object> request) {
        // See now(): without it the engine cannot check the key's expiry at all.
        request.put("now", now());
        request.put("max_memory_mb", maxMemoryMb);
        var answer = Json.readObject(run(Json.write(request)));
        if (answer.containsKey("error")) throw new EngineException(String.valueOf(answer.get("error")));
        if (!(answer.get("pdf") instanceof String pdf)) throw new IllegalStateException("The engine's answer has no 'pdf'.");
        return Base64.getDecoder().decode(pdf);
    }

    /** The XML of a document tree, {@code json}. */
    String kitToXml(String json) {
        var answer = Json.readObject(run(Json.write(Map.of("method", "kit_to_xml", "input", json))));
        if (answer.containsKey("error")) throw new EngineException(String.valueOf(answer.get("error")));
        if (!(answer.get("xml") instanceof String xml)) throw new IllegalStateException("The engine's answer has no 'xml'.");
        return xml;
    }

    /** What this build of the engine makes of {@code key}. */
    Map<String, Object> checkLicense(String key) {
        return Json.readObject(run(Json.write(Map.of("method", "check_license", "key", key, "now", now()))));
    }

    /**
     * The clock the engine checks a key's expiry against. The engine is WebAssembly and has no clock of its own, so a
     * request without it has its expiry check skipped, and an expired key would render without the attribution line.
     * Sent on every call rather than only when there is a key: the engine ignores it without one.
     */
    private static long now() {
        return Instant.now().getEpochSecond();
    }

    // ── Running ───────────────────────────────────────────────────────────────

    /**
     * The engine's answer to {@code request}, on the caller's thread. Past the timeout, a timer interrupts the thread,
     * which stops the module within about a millisecond, and the next call on any engine runs as usual.
     */
    private String run(String request) {
        if (timeout == 0) {
            try {
                return execute(request);
            } catch (WasmInterruptedException e) {
                throw interrupted(e);
            }
        }
        var deadline = new Deadline(Thread.currentThread());
        var timer = Timer.INSTANCE.schedule(deadline::expire, timeout, TimeUnit.SECONDS);
        try {
            return execute(request);
        } catch (WasmInterruptedException e) {
            if (deadline.finish()) throw new EngineException("The render timed out after " + seconds(timeout) + ".", e);
            throw interrupted(e);
        } finally {
            timer.cancel(false);
            // A deadline that passed just as the module finished interrupted nothing that runs; its flag is cleared.
            deadline.finish();
        }
    }

    private String execute(String request) {
        var stdout = new ByteArrayOutputStream();
        var stderr = new ByteArrayOutputStream();
        var options = WasiOptions.builder()
                .withStdin(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)))
                .withStdout(stdout)
                .withStderr(stderr)
                .build();
        try (var wasi = WasiPreview1.builder().withOptions(options).build()) {
            var instance = module.get()
                    .withImportValues(ImportValues.builder().addFunction(wasi.toHostFunctions()).build())
                    .withStart(false)
                    .build();
            instance.export("_start").apply();
        } catch (WasiExitException e) {
            if (e.exitCode() != 0) {
                throw new EngineException(EngineException.CRASHED, new IllegalStateException(
                        "the engine exited with " + e.exitCode() + ": " + stderr.toString(StandardCharsets.UTF_8)));
            }
        } catch (WasmInterruptedException e) {
            throw e;
        } catch (WasmEngineException | StackOverflowError e) {
            // A trap is a crash: a bug, or memory that ran out before the cap stopped the render. Each call has an
            // instance of its own, so the next one starts on a fresh engine.
            throw new EngineException(EngineException.CRASHED, e);
        }
        return stdout.toString(StandardCharsets.UTF_8);
    }

    /** The error of a call stopped by an interrupt other than its deadline's, such as the app's own, which stays set. */
    private static EngineException interrupted(WasmInterruptedException e) {
        Thread.currentThread().interrupt();
        return new EngineException("The render was interrupted.", e);
    }

    private static String seconds(int count) {
        return count == 1 ? "1 second" : count + " seconds";
    }

    /** The deadline of one call: the timer interrupts the call's thread unless the call has finished. */
    private static final class Deadline {
        private final Thread thread;
        private boolean finished;
        private boolean expired;

        Deadline(Thread thread) {
            this.thread = thread;
        }

        synchronized void expire() {
            if (finished) return;
            expired = true;
            thread.interrupt();
        }

        /** Ends the call, so the timer interrupts nothing after it; true when it did interrupt, whose flag it clears. */
        synchronized boolean finish() {
            if (!finished && expired) Thread.interrupted();
            finished = true;
            return expired;
        }
    }

    /** One daemon thread that interrupts the calls past their deadline, for every engine. */
    private static final class Timer {
        static final ScheduledThreadPoolExecutor INSTANCE = create();

        private static ScheduledThreadPoolExecutor create() {
            var timer = new ScheduledThreadPoolExecutor(1, task -> {
                var thread = new Thread(task, "lpdf-timeout");
                thread.setDaemon(true);
                return thread;
            });
            timer.setRemoveOnCancelPolicy(true);
            return timer;
        }
    }
}
