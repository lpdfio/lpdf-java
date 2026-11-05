package io.lpdf;

/**
 * Thrown when the engine refuses a document or a stamp, saying what to change, a render runs past its timeout, its
 * memory cap or the JVM's heap, or the engine crashes.
 */
public final class EngineException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** The message of a render that crashes the engine, the same in every SDK of Lpdf. */
    static final String CRASHED =
            "The engine crashed during the render. This is a bug in Lpdf, or the render ran out of memory before its cap "
            + "stopped it: please report it with the document at https://github.com/lpdfio/lpdf/issues.";

    /** An engine error with the message the engine gave. */
    public EngineException(String message) {
        super(message);
    }

    EngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
