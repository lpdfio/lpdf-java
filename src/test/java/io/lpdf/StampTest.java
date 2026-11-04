package io.lpdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.DeflaterOutputStream;
import org.junit.jupiter.api.Test;

/** Stamps: PNG images drawn over the pages of an existing PDF, through the engine's {@code stamp} request. */
class StampTest {
    private final PdfEngine engine = L.engine();

    @Test
    void a_stamp_returns_the_input_with_an_update_after_it_that_draws_the_image() {
        var input = pdf(2);

        var output = engine.stamp(input, List.of(new StampAttr().image(png(60, 30)).page("last")
                .x("72pt").y("72pt").width("144pt").height("36pt").justify("end")));

        assertTrue(output.length > input.length);
        assertArrayEquals(input, Arrays.copyOf(output, input.length));
        assertTrue(update(input, output).contains("/LpdfStamp0 Do"));
    }

    @Test
    void an_image_given_to_several_stamps_is_embedded_once() {
        var input = pdf(3);
        var seal = png(40, 40);

        var output = engine.stamp(input, List.of(
                new StampAttr().image(seal).page("each").anchor("top-right").x("-36pt").y("36pt").width("40pt").height("40pt"),
                new StampAttr().image(seal).page("1").anchor("center").width("40pt").height("40pt").maxGrow(2),
                new StampAttr().image(png(20, 10)).page("2-3").x("1in").y("1in").width("1in").height("1in")));

        assertEquals(2, Pattern.compile("/Subtype\\s*/Image").matcher(update(input, output)).results().count());
    }

    @Test
    void without_a_license_key_each_stamped_page_gets_the_attribution_line_linked_to_lpdf_io() {
        var input = pdf(1);

        var output = engine.stamp(input, List.of(
                new StampAttr().image(png(10, 10)).page("1").anchor("center").width("50pt").height("50pt")));

        assertTrue(update(input, output).contains("(https://lpdf.io)"));
    }

    @Test
    void a_page_the_pdf_does_not_have_and_a_box_with_no_position_are_engine_errors() {
        var input = pdf(2);

        var past = assertThrows(EngineException.class, () -> engine.stamp(input, List.of(
                new StampAttr().image(png(10, 10)).page("3").x("0pt").y("0pt").width("10pt").height("10pt"))));
        var nowhere = assertThrows(EngineException.class, () -> engine.stamp(input, List.of(
                new StampAttr().image(png(10, 10)).page("1").width("10pt").height("10pt"))));
        var jpeg = assertThrows(EngineException.class, () -> engine.stamp(input, List.of(
                new StampAttr().image(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0}).page("1")
                        .x("0pt").y("0pt").width("10pt").height("10pt"))));

        assertEquals("stamp 1: the PDF has no page 3: it has 2 pages", past.getMessage());
        assertEquals("stamp 1: has no position: give it x and y, or an anchor", nowhere.getMessage());
        assertTrue(jpeg.getMessage().contains("the image is a JPEG, and a stamp takes a PNG"), jpeg.getMessage());
    }

    @Test
    void a_stamp_without_its_image_page_or_size_is_refused_before_the_engine() {
        var input = pdf(1);

        var error = assertThrows(IllegalArgumentException.class,
                () -> engine.stamp(input, List.of(new StampAttr().page("1").x("0pt").y("0pt").width("10pt").height("10pt"))));

        assertEquals("<stamp> needs image: set it with .image(…)", error.getMessage());
    }

    /** A PDF of {@code pages} pages, rendered by the engine. */
    private byte[] pdf(int pages) {
        var sections = IntStream.rangeClosed(1, pages)
                .mapToObj(i -> "<section><layout><text>Page " + i + "</text></layout></section>")
                .collect(Collectors.joining());
        return engine.render("<lpdf version=\"1\"><document>" + sections + "</document></lpdf>");
    }

    /** The text of what a stamp appended to {@code input}. */
    private static String update(byte[] input, byte[] output) {
        return new String(output, input.length, output.length - input.length, StandardCharsets.ISO_8859_1);
    }

    /** An RGB PNG of {@code width} by {@code height} pixels, all red. */
    static byte[] png(int width, int height) {
        var header = ByteBuffer.allocate(13).putInt(width).putInt(height).put(new byte[] {8, 2, 0, 0, 0}).array();
        var raw = new ByteArrayOutputStream();
        for (var y = 0; y < height; y++) {
            raw.write(0);
            for (var x = 0; x < width; x++) raw.writeBytes(new byte[] {(byte) 200, 40, 40});
        }
        var packed = new ByteArrayOutputStream();
        try (var zlib = new DeflaterOutputStream(packed)) {
            zlib.write(raw.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        var png = new ByteArrayOutputStream();
        png.writeBytes(HexFormat.of().parseHex("89504e470d0a1a0a"));
        png.writeBytes(chunk("IHDR", header));
        png.writeBytes(chunk("IDAT", packed.toByteArray()));
        png.writeBytes(chunk("IEND", new byte[0]));
        return png.toByteArray();
    }

    /** A PNG chunk; the decoder doesn't check the CRC, which is left 0. */
    private static byte[] chunk(String tag, byte[] data) {
        return ByteBuffer.allocate(12 + data.length).putInt(data.length).put(tag.getBytes(StandardCharsets.US_ASCII))
                .put(data).putInt(0).array();
    }
}
