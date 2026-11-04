package io.lpdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * The fixtures and snapshots of core: in the lpdf checkout this repository sits in, or copied into <code>test/</code>
 * by CI. The snapshot files are shared by every SDK: the same engine makes the same bytes.
 */
final class Snapshots {
    static final Path ROOT = root();
    static final Path FIXTURES = ROOT.resolve("test/fixtures");
    static final Path SNAPSHOTS = ROOT.resolve("test/snapshots");

    private Snapshots() {
    }

    /** Whether the fixtures are there: they are not in a bare clone of this repository. */
    static boolean present() {
        return Files.isDirectory(FIXTURES) && Files.exists(FIXTURES.resolve("bench_xs.xml"));
    }

    static String fixture(String name) {
        try {
            return Files.readString(FIXTURES.resolve(name + ".xml"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String snapshot(String name) {
        try {
            return Files.readString(SNAPSHOTS.resolve(name + ".pdf.sha256")).trim();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Asserts that {@code pdf} is a PDF whose SHA-256 is the snapshot of {@code name}. */
    static void assertMatches(String name, byte[] pdf) {
        assertArrayEquals("%PDF-".getBytes(StandardCharsets.US_ASCII), Arrays.copyOf(pdf, 5), "a PDF starts with %PDF-");
        assertEquals(snapshot(name), sha256(pdf), name);
    }

    /** The PDF of a fixture rendered as the snapshot tests render it: with the key test-key and no encryption. */
    static byte[] render(String name, EngineOptions options) {
        return L.engine(options).setLicenseKey("test-key").render(fixture(name), new RenderOptions().skipEncryption(true));
    }

    /** The lpdf checkout above this repository, else this repository, whose test folder CI fills. */
    private static Path root() {
        var start = Path.of("").toAbsolutePath();
        for (var dir = start; dir != null; dir = dir.getParent()) {
            if (Files.exists(dir.resolve("Cargo.toml")) && Files.isDirectory(dir.resolve("test/snapshots"))) return dir;
        }
        for (var dir = start; dir != null; dir = dir.getParent()) {
            if (Files.exists(dir.resolve("pom.xml"))) return dir;
        }
        return start;
    }
}
