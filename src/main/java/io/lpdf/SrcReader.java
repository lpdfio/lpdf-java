package io.lpdf;

import java.io.IOException;

/**
 * Reads the bytes of a file a document names by its <code>src</code>: a font, an image, a color profile, a file it
 * carries or a file of Markdown rules. On a server it can read the file system:
 * <code>src -&gt; Files.readAllBytes(Path.of(src))</code>.
 */
@FunctionalInterface
public interface SrcReader {
    /**
     * The bytes of the file {@code src} names.
     *
     * @throws IOException when it can't be read
     */
    byte[] read(String src) throws IOException;
}
