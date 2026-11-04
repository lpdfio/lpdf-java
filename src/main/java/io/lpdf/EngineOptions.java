package io.lpdf;

/**
 * The options of a {@link PdfEngine}, given when it is made and kept for every render on it:
 * <code>L.engine(new EngineOptions().timeout(10))</code>.
 */
public final class EngineOptions {
    /** The seconds a render may run when the timeout is left out. */
    static final int DEFAULT_TIMEOUT = 5;

    /** The megabytes a render may allocate when the cap is left out. */
    static final int DEFAULT_MAX_MEMORY_MB = 1024;

    /** The most the cap may be: 32-bit WebAssembly addresses 4 GB at most. */
    private static final int MOST_MEMORY_MB = 4096;

    private int timeout = DEFAULT_TIMEOUT;
    private int maxMemoryMb = DEFAULT_MAX_MEMORY_MB;
    private SrcReader srcFallback;

    /** The options left out: a timeout of 5 seconds, a cap of 1024 MB and no {@code srcFallback}. */
    public EngineOptions() {
    }

    /**
     * The seconds a render may run before it stops with an {@link EngineException} that names the limit: 5 when left
     * out, no limit at 0. The render runs on the caller's thread, which the engine interrupts at the deadline. A render
     * stopped at its limit leaves the engine as it was, so the next render on it runs as usual.
     *
     * @throws IllegalArgumentException when {@code seconds} is negative
     */
    public EngineOptions timeout(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("timeout must be a number of seconds, 0 for no limit, not " + seconds);
        }
        timeout = seconds;
        return this;
    }

    /** The seconds a render may run, or 0 for no limit. */
    public int timeout() {
        return timeout;
    }

    /**
     * The megabytes of memory a render may allocate, its data and the fonts, images and files sent with it included:
     * 1024 when left out, from 1 to 4096. A render that needs more stops with an {@link EngineException} that names
     * the cap.
     *
     * @throws IllegalArgumentException when {@code megabytes} is below 1 or above 4096
     */
    public EngineOptions maxMemoryMb(int megabytes) {
        if (megabytes < 1 || megabytes > MOST_MEMORY_MB) {
            throw new IllegalArgumentException(
                    "maxMemoryMb must be a whole number of megabytes from 1 to " + MOST_MEMORY_MB + ", not " + megabytes);
        }
        maxMemoryMb = megabytes;
        return this;
    }

    /** The megabytes a render may allocate. */
    public int maxMemoryMb() {
        return maxMemoryMb;
    }

    /**
     * Reads the <code>src</code> of the fonts, images, color profiles and attached files a document names, and of its
     * files of Markdown rules, at render time: <code>src -&gt; Files.readAllBytes(Path.of(src))</code> on a server.
     * Without it, give their bytes with {@link PdfEngine#loadFont} and its kin.
     *
     * <p>An attached file's bytes go into the PDF as they are, so a reader of the whole file system lets a template
     * embed any file the process can read. Give a template from a source you don't trust a reader of the folder its
     * files are in, or none.
     */
    public EngineOptions srcFallback(SrcReader reader) {
        srcFallback = reader;
        return this;
    }

    /** The reader of the <code>src</code> of a document's files, or null. */
    public SrcReader srcFallback() {
        return srcFallback;
    }
}
