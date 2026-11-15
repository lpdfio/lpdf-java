package io.lpdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/** Merges: existing PDFs drawn into one PDF made for printing, through the engine's {@code merge} request. */
class MergeTest {
    /** A page object: {@code /Type /Page}, and not {@code /Pages} or another name that starts with it. */
    private static final Pattern PAGE = Pattern.compile("/Type\\s*/Page(?![s\\w])");

    private final PdfEngine engine = L.engine();

    @Test
    void a_merge_returns_one_pdf_with_the_pages_of_the_sources_in_order() {
        var output = engine.merge(List.of(
                new MergeSource().name("a.pdf").pdf(pdf(2)),
                new MergeSource().name("b.pdf").pdf(pdf(1))));

        assertTrue(latin1(output).startsWith("%PDF-"));
        assertEquals(3, pageCount(output));
    }

    @Test
    void double_sided_adds_a_blank_side_after_a_document_with_an_odd_number_of_pages_and_none_after_the_last() {
        var odd = pdf(1);
        var even = pdf(2);
        var sources = List.of(
                new MergeSource().name("odd.pdf").pdf(odd),
                new MergeSource().name("even.pdf").pdf(even),
                new MergeSource().name("odd-again.pdf").pdf(odd));

        assertEquals(4, pageCount(engine.merge(sources)));
        assertEquals(4, pageCount(engine.merge(sources, new MergeOptions())));
        assertEquals(5, pageCount(engine.merge(sources, new MergeOptions().doubleSided(true))));
    }

    @Test
    void a_file_that_is_not_a_pdf_is_an_engine_error_that_names_it() {
        var sources = List.of(
                new MergeSource().name("a.pdf").pdf(pdf(1)),
                new MergeSource().name("notes.txt").pdf("hello".getBytes(StandardCharsets.UTF_8)));

        var error = assertThrows(EngineException.class, () -> engine.merge(sources));

        assertEquals("notes.txt: the file is not a PDF: it doesn't start with %PDF-", error.getMessage());
    }

    @Test
    void an_empty_list_is_an_engine_error() {
        var error = assertThrows(EngineException.class, () -> engine.merge(List.of()));

        assertEquals("no PDFs were given: give merge at least one", error.getMessage());
    }

    @Test
    void a_file_whose_owner_restricts_printing_or_copying_is_refused_by_name() {
        var limited = restricted();

        var error = assertThrows(EngineException.class,
                () -> engine.merge(List.of(new MergeSource().name("limited.pdf").pdf(limited))));

        assertEquals(
                "limited.pdf: the owner restricts printing or copying: set the allow-restricted option to merge it anyway",
                error.getMessage());
    }

    @Test
    void allow_restricted_merges_a_file_whose_owner_restricts_it_without_its_encryption() {
        var limited = restricted();

        var output = engine.merge(List.of(new MergeSource().name("limited.pdf").pdf(limited)),
                new MergeOptions().allowRestricted(true));

        assertEquals(1, pageCount(output));
        assertFalse(latin1(output).contains("/Encrypt"));
    }

    @Test
    void a_source_without_its_name_or_pdf_is_refused_before_the_engine() {
        var nameless = List.of(new MergeSource().pdf(pdf(1)));
        var empty = List.of(new MergeSource().name("a.pdf"));

        var withoutName = assertThrows(IllegalArgumentException.class, () -> engine.merge(nameless));
        var withoutPdf = assertThrows(IllegalArgumentException.class, () -> engine.merge(empty));

        assertEquals("<merge-source> needs name: set it with .name(…)", withoutName.getMessage());
        assertEquals("<merge-source> needs pdf: set it with .pdf(…)", withoutPdf.getMessage());
    }

    /** A PDF of {@code pages} pages, rendered by the engine. */
    private byte[] pdf(int pages) {
        return render(pages, "", new RenderOptions());
    }

    /** A PDF of one page that AES-256 encrypts with an owner password and no user password, and lets only print. */
    private byte[] restricted() {
        return render(1, "<output encrypt=\"aes-256\" allow=\"print\"/>", new RenderOptions().ownerPassword("owner"));
    }

    private byte[] render(int pages, String output, RenderOptions options) {
        var sections = IntStream.rangeClosed(1, pages)
                .mapToObj(i -> "<section><layout><text>Page " + i + "</text></layout></section>")
                .collect(Collectors.joining());
        return engine.render("<lpdf version=\"1\"><document>" + output + sections + "</document></lpdf>", options);
    }

    /** How many pages a merged PDF has: its page objects, which the merge writes uncompressed. */
    private static long pageCount(byte[] output) {
        return PAGE.matcher(latin1(output)).results().count();
    }

    private static String latin1(byte[] pdf) {
        return new String(pdf, StandardCharsets.ISO_8859_1);
    }
}
