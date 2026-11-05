package io.lpdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import run.endive.runtime.Instance;
import run.endive.runtime.WasmInterruptedException;
import run.endive.wabt.Wat2Wasm;
import run.endive.wasm.Parser;

/**
 * The timeout: past it, a timer interrupts the thread the render runs on. A render that runs long is a small
 * WebAssembly module of the test's own, run in place of the engine: the real engine renders the largest fixture in well
 * under a second.
 */
class TimeoutTest {
    static final String SPIN_FOREVER = "(module (func (export \"_start\") (loop $spin (br $spin))))";

    /** Spins on the monotonic clock for two seconds, then writes an answer whose PDF is the bytes "%PDF-". */
    static final String SPIN_TWO_SECONDS = """
            (module
              (import "wasi_snapshot_preview1" "clock_time_get" (func $clock (param i32 i64 i32) (result i32)))
              (import "wasi_snapshot_preview1" "fd_write" (func $write (param i32 i32 i32 i32) (result i32)))
              (memory (export "memory") 1)
              (data (i32.const 32) "{\\"pdf\\": \\"JVBERi0=\\"}")
              (func (export "_start")
                (local $until i64)
                (drop (call $clock (i32.const 1) (i64.const 1) (i32.const 0)))
                (local.set $until (i64.add (i64.load (i32.const 0)) (i64.const 2000000000)))
                (loop $spin
                  (drop (call $clock (i32.const 1) (i64.const 1) (i32.const 0)))
                  (br_if $spin (i64.lt_u (i64.load (i32.const 0)) (local.get $until))))
                (i32.store (i32.const 16) (i32.const 32))
                (i32.store (i32.const 20) (i32.const 19))
                (drop (call $write (i32.const 1) (i32.const 16) (i32.const 1) (i32.const 24)))))
            """;

    static final String MINIMAL =
            "<lpdf version=\"1\"><document><section><layout><text>x</text></layout></section></document></lpdf>";

    @Test
    void a_render_past_its_timeout_throws_naming_the_limit() {
        var engine = standIn(SPIN_TWO_SECONDS, new EngineOptions().timeout(1));

        var started = System.nanoTime();
        var error = assertThrows(EngineException.class, () -> engine.render(MINIMAL));
        var seconds = (System.nanoTime() - started) / 1e9;

        assertEquals("The render timed out after 1 second.", error.getMessage());
        assertTrue(seconds >= 1 && seconds < 1.5, "stopped after " + seconds + " s");
        assertFalse(Thread.currentThread().isInterrupted(), "the timeout's interrupt is left set on the thread");
    }

    @Test
    void a_render_with_a_timeout_of_0_runs_with_no_limit() {
        var engine = standIn(SPIN_TWO_SECONDS, new EngineOptions().timeout(0));

        var started = System.nanoTime();
        var pdf = engine.render(MINIMAL);
        var seconds = (System.nanoTime() - started) / 1e9;

        assertArrayEquals("%PDF-".getBytes(StandardCharsets.US_ASCII), pdf);
        assertTrue(seconds >= 2, "rendered in " + seconds + " s");
    }

    @Test
    void a_render_stops_after_5_seconds_when_the_timeout_is_left_out() {
        var engine = standIn(SPIN_FOREVER, new EngineOptions());

        var started = System.nanoTime();
        var error = assertThrows(EngineException.class, () -> engine.render(MINIMAL));
        var seconds = (System.nanoTime() - started) / 1e9;

        assertEquals("The render timed out after 5 seconds.", error.getMessage());
        assertTrue(seconds >= 5 && seconds < 5.5, "stopped after " + seconds + " s");
    }

    @Test
    void the_timeout_is_5_seconds_when_left_out() {
        assertEquals(5, new EngineOptions().timeout());
    }

    @Test
    void a_negative_timeout_is_refused() {
        assertThrows(IllegalArgumentException.class, () -> new EngineOptions().timeout(-1));
    }

    /** An interrupt of the app's own stops a render too, and stays set on the thread for the app to see. */
    @Test
    void an_interrupt_of_the_app_s_own_stops_the_render_and_stays_set() throws InterruptedException {
        var engine = standIn(SPIN_FOREVER, new EngineOptions().timeout(0));
        var error = new AtomicReference<Throwable>();
        var interrupted = new AtomicReference<Boolean>();

        var render = new Thread(() -> {
            try {
                engine.render(MINIMAL);
            } catch (Throwable e) {
                error.set(e);
            }
            interrupted.set(Thread.currentThread().isInterrupted());
        });
        render.start();
        Thread.sleep(200);
        render.interrupt();
        render.join(5000);

        assertFalse(render.isAlive(), "the render did not stop");
        assertEquals("The render was interrupted.", error.get().getMessage());
        assertTrue(interrupted.get(), "the app's interrupt was cleared");
    }

    /** A render stopped at its deadline stops nothing else, and leaves the engine rendering right. */
    @Test
    void a_render_past_its_timeout_leaves_the_others_and_the_next_render_right() throws Exception {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var spinning = standIn(SPIN_FOREVER, new EngineOptions().timeout(1));
        var barrier = new CyclicBarrier(2);
        var error = new AtomicReference<Throwable>();
        var hash = new AtomicReference<String>();
        var stopped = new AtomicLong();
        var rendered = new AtomicLong();

        var timeOut = new Thread(() -> {
            await(barrier);
            error.set(assertThrows(EngineException.class, () -> spinning.render(MINIMAL)));
            stopped.set(System.nanoTime());
        });
        var renderMeanwhile = new Thread(() -> {
            await(barrier);
            hash.set(Snapshots.sha256(Snapshots.render("bench_m", new EngineOptions().timeout(1))));
            rendered.set(System.nanoTime());
        });
        timeOut.start();
        renderMeanwhile.start();
        timeOut.join();
        renderMeanwhile.join();

        assertEquals("The render timed out after 1 second.", error.get().getMessage());
        assertEquals(Snapshots.snapshot("bench_m"), hash.get());
        assertTrue(rendered.get() < stopped.get(), "the render meanwhile ended after the one that timed out");
        Snapshots.assertMatches("bench_m", Snapshots.render("bench_m", new EngineOptions().timeout(1)));
    }

    /**
     * The engine's own compiled classes, which the jar ships, stopped at the deadline: the stand-ins above run on
     * Endive's interpreter, which notices an interrupt by code of its own. The same engine then renders as usual.
     */
    @Test
    void the_engine_stops_at_its_deadline_and_its_next_render_is_right() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var engine = L.engine(new EngineOptions().timeout(1)).setLicenseKey("test-key");
        var xml = Snapshots.benchMaxCopied(16);

        var started = System.nanoTime();
        var error = assertThrows(EngineException.class, () -> engine.render(xml));
        var seconds = (System.nanoTime() - started) / 1e9;

        assertEquals("The render timed out after 1 second.", error.getMessage());
        assertInstanceOf(WasmInterruptedException.class, error.getCause());
        // The clock also counts the 16 MB request the SDK writes before the deadline starts. Uninterrupted, the render
        // takes several seconds.
        assertTrue(seconds >= 1 && seconds < 2, "stopped after " + seconds + " s");
        assertFalse(Thread.currentThread().isInterrupted(), "the timeout's interrupt is left set on the thread");
        Snapshots.assertMatches("bench_m",
                engine.render(Snapshots.fixture("bench_m"), new RenderOptions().skipEncryption(true)));
    }

    @Test
    void a_fixture_renders_with_a_timeout_of_0() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        Snapshots.assertMatches("bench_l", Snapshots.render("bench_l", new EngineOptions().timeout(0)));
    }

    /** An engine that runs the module of {@code wat} in place of the engine's own, on Endive's interpreter. */
    static PdfEngine standIn(String wat, EngineOptions options) {
        var module = Parser.parse(Wat2Wasm.parse(wat));
        return new PdfEngine(options, new WasmRunner(() -> Instance.builder(module), options.timeout(), options.maxMemoryMb()));
    }

    private static void await(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
