package io.lpdf;

import static io.lpdf.L.NoAttr;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * What the engine does with what a document's output asks for: encryption, PDF/A, PDF/UA, PDF/X, Factur-X, viewer
 * groups, linearization, and the files it carries.
 */
class EngineFeatureTest {
    private static final String MINIMAL = "<lpdf version=\"1\"><document><section><layout></layout></section></document></lpdf>";
    private static final SrcReader FILES = src -> Files.readAllBytes(Path.of(src));

    private final PdfEngine engine = L.engine().setLicenseKey("test-key");

    /** A document whose output has {@code attributes}. */
    private static String encryptedXml(String attributes) {
        return "<lpdf version=\"1\"><document><output " + attributes
                + "/><section><layout><text>Hello</text></layout></section></document></lpdf>";
    }

    private static RenderOptions ownerPassword() {
        return new RenderOptions().ownerPassword("s3cr3t");
    }

    private static String latin1(byte[] pdf) {
        return new String(pdf, StandardCharsets.ISO_8859_1);
    }

    /** A column with a background and no text, so a PDF/A needs no font: the attribution line's face is its one text. */
    private static SectionNode blankSection(SectionAttr attrs) {
        return L.section(attrs, List.of(L.layout(NoAttr, List.of(
                L.column(new ColumnAttr().background("#e8f0fc").height("40pt"), List.of())))));
    }

    // ── Encryption ────────────────────────────────────────────────────────────

    @Test
    void encrypts_a_document_whose_output_asks_for_it_with_the_passwords_of_the_render() {
        var pdf = engine.render(encryptedXml("encrypt=\"aes-256\""), new RenderOptions().userPassword("open").ownerPassword("s3cr3t"));
        var text = latin1(pdf);

        assertTrue(text.startsWith("%PDF-"));
        for (var part : List.of("/Encrypt", "/V 5", "/R 6", "/CFM /AESV3")) assertTrue(text.contains(part), part);
    }

    @Test
    void takes_the_cipher_and_the_permissions_from_the_output() {
        var rc4 = latin1(engine.render(encryptedXml("encrypt=\"rc4-128\""), ownerPassword()));
        var print = latin1(engine.render(encryptedXml("encrypt=\"rc4-128\" allow=\"print\""), ownerPassword()));

        assertTrue(rc4.contains("/V 2") && rc4.contains("/R 3") && !rc4.contains("AESV3"));
        // Printing, at full quality too, and nothing else.
        assertTrue(print.contains("/P -1852"));
    }

    @Test
    void every_aes256_render_gets_a_fresh_key_and_rc4_renders_are_identical() {
        var aes = encryptedXml("encrypt=\"aes-256\"");
        var rc4 = encryptedXml("encrypt=\"rc4-128\"");

        assertFalse(Arrays.equals(engine.render(aes, ownerPassword()), engine.render(aes, ownerPassword())));
        assertArrayEquals(engine.render(rc4, ownerPassword()), engine.render(rc4, ownerPassword()));
    }

    @Test
    void skip_encryption_renders_a_document_that_asks_for_encryption_unencrypted() {
        var text = latin1(engine.render(encryptedXml("encrypt=\"aes-256\""), new RenderOptions().skipEncryption(true)));

        assertFalse(text.contains("/Encrypt"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "a document that asks for encryption without the owner password | true  |        | false | no owner password",
        "passwords for a document that does not ask for encryption      | false | s3cr3t | false | does not ask for encryption",
        "passwords with skipEncryption                                  | true  | s3cr3t | true  | not both",
    })
    void refuses_a_render_and_says_what_to_change(String what, boolean encrypted, String password, boolean skip, String expected) {
        var xml = encrypted ? encryptedXml("encrypt=\"aes-256\"") : MINIMAL;

        var error = assertThrows(EngineException.class,
                () -> engine.render(xml, new RenderOptions().ownerPassword(password).skipEncryption(skip)));

        assertTrue(error.getMessage().contains(expected), what + ": " + error.getMessage());
    }

    @Test
    void encrypts_and_linearizes_a_document_built_with_l_as_its_output_asks() {
        var encrypted = L.document(new DocumentAttr().output(new DocumentOutput()
                        .encrypt(Cipher.AES_256).allow(Permission.PRINT + " " + Permission.FILL_FORMS)),
                List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of(L.text(NoAttr, List.of("Hello PDF"))))))));
        var linearized = L.document(new DocumentAttr().output(new DocumentOutput().encrypt(Cipher.AES_256).linearize(true)),
                List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of(L.text(NoAttr, List.of("Hello PDF"))))))));

        assertTrue(latin1(engine.render(encrypted, ownerPassword())).contains("/CFM /AESV3"));
        assertThrows(EngineException.class, () -> engine.render(encrypted));
        var text = latin1(engine.render(linearized, ownerPassword()));
        assertTrue(text.contains("<< /Linearized 1 ") && text.contains("/CFM /AESV3"));
    }

    // ── PDF/A, Factur-X and PDF/UA ────────────────────────────────────────────

    private static String pdfaXml(String level, String body) {
        return "<lpdf version=\"1\"><document><output pdfa=\"" + level + "\"/><section><layout>" + body
                + "</layout></section></document></lpdf>";
    }

    private static final String BLANK = "<column background=\"#e8f0fc\" height=\"40pt\"/>";

    @Test
    void writes_pdfa_when_the_output_asks_for_it_dated_by_created_on() {
        var text = latin1(engine.render(pdfaXml("2u", BLANK), new RenderOptions().createdOn("2026-10-31T09:30:00Z")));

        for (var part : List.of("<pdfaid:part>2</pdfaid:part>", "<pdfaid:conformance>U</pdfaid:conformance>",
                "/S /GTS_PDFA1", "/ModDate (D:20261031093000Z)")) {
            assertTrue(text.contains(part), part);
        }
    }

    @Test
    void writes_pdfa_level_a_tagged_without_pdfua_when_the_output_asks_for_2a() {
        var text = latin1(engine.render(pdfaXml("2a", BLANK), new RenderOptions().createdOn("2026-10-31")));

        assertTrue(text.contains("<pdfaid:conformance>A</pdfaid:conformance>"));
        assertTrue(text.contains("/StructTreeRoot") && text.contains("/Marked true"));
        assertFalse(text.contains("pdfuaid"));
    }

    @Test
    void takes_pdfa_from_the_output_of_a_document_built_with_l() {
        var document = L.document(new DocumentAttr().output(new DocumentOutput().pdfa("2b")), List.of(blankSection(new SectionAttr())));

        var text = latin1(engine.render(document, new RenderOptions().createdOn("2026-10-31")));

        assertTrue(text.contains("<pdfaid:conformance>B</pdfaid:conformance>"));
    }

    @Test
    void refuses_pdfa_without_created_on_and_text_in_a_built_in_font() {
        var missing = assertThrows(EngineException.class, () -> engine.render(pdfaXml("2b", BLANK)));
        var builtIn = assertThrows(EngineException.class,
                () -> engine.render(pdfaXml("2b", "<text>Hello</text>"), new RenderOptions().createdOn("2026-10-31")));

        assertTrue(missing.getMessage().contains("created_on"), missing.getMessage());
        assertTrue(builtIn.getMessage().contains("\"Hello\" is in Helvetica"), builtIn.getMessage());
    }

    /** The XML of a Factur-X invoice of the EN 16931 profile, as little of it as the engine reads: its BT-24. */
    private static final String CII_INVOICE = "<rsm:CrossIndustryInvoice"
            + " xmlns:rsm=\"urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100\""
            + " xmlns:ram=\"urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100\">"
            + "<rsm:ExchangedDocumentContext><ram:GuidelineSpecifiedDocumentContextParameter><ram:ID>urn:cen.eu:en16931:2017"
            + "</ram:ID></ram:GuidelineSpecifiedDocumentContextParameter></rsm:ExchangedDocumentContext></rsm:CrossIndustryInvoice>";

    private static PdfDocument invoice(DocumentOutput output, FileAttr file) {
        return L.document(new DocumentAttr().output(output).attachments(List.of(file)), List.of(blankSection(new SectionAttr())));
    }

    @Test
    void makes_a_factur_x_invoice_of_a_document_built_with_l_with_pdfa_3b_and_factur_x_xml() {
        engine.loadFile("factur-x.xml", CII_INVOICE.getBytes(StandardCharsets.UTF_8));
        var options = new RenderOptions().createdOn("2026-10-31");

        var text = latin1(engine.render(invoice(new DocumentOutput().pdfa("3b"), new FileAttr().name("factur-x.xml")), options));

        for (var part : List.of("<pdfaid:part>3</pdfaid:part>", "<fx:ConformanceLevel>EN 16931</fx:ConformanceLevel>",
                "/AFRelationship /Alternative", "/Subtype /text#2Fxml")) {
            assertTrue(text.contains(part), part);
        }
        var typed = new FileAttr().name("factur-x.xml").mime("application/xml");
        engine.render(invoice(new DocumentOutput().pdfa("3u"), typed), options);
        var strict = invoice(new DocumentOutput().pdfa("3u").facturxStrict(true), typed);
        var error = assertThrows(EngineException.class, () -> engine.render(strict, options));
        assertTrue(error.getMessage().contains("facturx-strict=\"true\"") && error.getMessage().contains("mime 'application/xml'"),
                error.getMessage());
        assertTrue(L.toXml(strict).contains("facturx-strict=\"true\""));
    }

    @Test
    void takes_pdfua_and_lang_from_a_document_built_with_l_and_tags_the_file() {
        var document = L.document(new DocumentAttr()
                        .meta(new DocumentMeta().title("Report").lang("en-US"))
                        .output(new DocumentOutput().pdfua("1")),
                List.of(blankSection(new SectionAttr())));

        var text = latin1(engine.render(document, new RenderOptions().createdOn("2026-10-31")));

        for (var part : List.of("<pdfuaid:part>1</pdfuaid:part>", "/Lang (en-US)", "/StructTreeRoot")) {
            assertTrue(text.contains(part), part);
        }
    }

    @Test
    void refuses_pdfua_without_the_language_of_the_document() {
        var xml = "<lpdf version=\"1\"><document><meta title=\"Report\"/><output pdfua=\"1\"/><section><layout>"
                + "<column height=\"40pt\"/></layout></section></document></lpdf>";

        var error = assertThrows(EngineException.class, () -> engine.render(xml, new RenderOptions().createdOn("2026-10-31")));

        assertTrue(error.getMessage().contains("add lang to <meta>"), error.getMessage());
    }

    @Test
    void puts_elements_built_with_l_in_print_and_screen_content() {
        var document = L.document(NoAttr, List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of(
                L.text(new TextAttr().ocg("print"), List.of("Printed")),
                L.column(new ColumnAttr().ocg("screen"), List.of(L.text(NoAttr, List.of("On screen"))))))))));

        var text = latin1(engine.render(document));

        for (var part : List.of("/OCProperties", "(Print only)", "(Screen only)")) assertTrue(text.contains(part), part);
    }

    // ── PDF/X ─────────────────────────────────────────────────────────────────

    /**
     * The header of an ICC printer profile in CMYK, with a description and nothing else: enough for the engine to take
     * it as the press profile of a PDF/X document.
     */
    private static byte[] pressProfile(String description) {
        var desc = ByteBuffer.allocate(12 + description.length() + 1 + 12)
                .put("desc".getBytes(StandardCharsets.US_ASCII)).putInt(0).putInt(description.length() + 1)
                .put(description.getBytes(StandardCharsets.US_ASCII)).array();
        var bytes = ByteBuffer.allocate(128 + 16 + desc.length);
        bytes.putInt(0, bytes.capacity());
        bytes.put(12, "prtrCMYKLab ".getBytes(StandardCharsets.US_ASCII));
        bytes.put(36, "acsp".getBytes(StandardCharsets.US_ASCII));
        bytes.putInt(128, 1);
        bytes.put(132, "desc".getBytes(StandardCharsets.US_ASCII));
        bytes.putInt(136, 144);
        bytes.putInt(140, desc.length);
        bytes.put(144, desc);
        return bytes.array();
    }

    /** A titled document with no text of its own that asks for PDF/X-4 with the profile {@code press}. */
    private static final String PDFX_XML = "<lpdf version=\"1\"><assets><profile name=\"press\"/></assets>"
            + "<document bleed=\"3mm\"><meta title=\"Flyer\"/><output pdfx=\"4\" profile=\"press\"/>"
            + "<section><layout><column background=\"#e8f0fc\" height=\"40pt\"/></layout></section></document></lpdf>";

    @Test
    void writes_pdfx_when_the_output_asks_for_it_with_the_profile_from_load_profile() {
        engine.loadProfile("press", pressProfile("Coated press"));

        var text = latin1(engine.render(PDFX_XML, new RenderOptions().createdOn("2026-10-31T09:30:00Z")));

        assertTrue(text.startsWith("%PDF-1.6"));
        for (var part : List.of("/S /GTS_PDFX", "/Info (Coated press)", "/TrimBox", "/DefaultRGB",
                "<pdfxid:GTS_PDFXVersion>PDF/X-4</pdfxid:GTS_PDFXVersion>")) {
            assertTrue(text.contains(part), part);
        }
    }

    @Test
    void takes_pdfx_its_profile_and_bleed_from_a_document_built_with_l() throws IOException {
        var src = Files.createTempFile("press", ".icc");
        Files.write(src, pressProfile("Test press"));
        try {
            var attrs = new DocumentAttr()
                    .assets(new DocumentAssets().profiles(List.of(new ProfileAttr().name("press").src(src.toString()))))
                    .meta(new DocumentMeta().title("Flyer"))
                    .output(new DocumentOutput().pdfx("4").profile("press"))
                    .bleed("3mm");
            var document = L.document(attrs, List.of(blankSection(new SectionAttr().bleed("5mm"))));
            var reading = L.engine(new EngineOptions().srcFallback(FILES)).setLicenseKey("test-key");
            var options = new RenderOptions().createdOn("2026-10-31");

            var pdf = reading.render(document, options);

            assertTrue(latin1(pdf).contains("/S /GTS_PDFX") && latin1(pdf).contains("/BleedBox [-14.173"));
            assertArrayEquals(pdf, reading.render(L.toXml(document), options));
            // A section without a bleed of its own takes the document's 3mm.
            var withDocumentBleed = L.document(attrs, List.of(blankSection(new SectionAttr())));
            assertTrue(latin1(reading.render(withDocumentBleed, options)).contains("/BleedBox [-8.503"));
        } finally {
            Files.delete(src);
        }
    }

    @Test
    void a_profile_with_no_bytes_or_a_src_that_cannot_be_read_is_an_error_that_names_it() {
        var options = new RenderOptions().createdOn("2026-10-31");
        var withSrc = PDFX_XML.replace("<profile name=\"press\"/>", "<profile name=\"press\" src=\"/no/such/press.icc\"/>");
        var reading = L.engine(new EngineOptions().srcFallback(FILES)).setLicenseKey("test-key");

        var noBytes = assertThrows(EngineException.class, () -> engine.render(PDFX_XML, options));
        var noFallback = assertThrows(EngineException.class, () -> engine.render(withSrc, options));
        var unreadable = assertThrows(EngineException.class, () -> reading.render(withSrc, options));

        assertTrue(noBytes.getMessage().contains("loadProfile('press', bytes)"), noBytes.getMessage());
        assertTrue(noFallback.getMessage().contains("profile 'press' of <assets> has the src '/no/such/press.icc'"));
        assertTrue(noFallback.getMessage().contains("loadProfile(\"press\", bytes)"), noFallback.getMessage());
        assertTrue(unreadable.getMessage().contains("cannot read its src '/no/such/press.icc'"), unreadable.getMessage());
    }

    // ── The files a document carries ──────────────────────────────────────────

    private static PdfDocument withAttachments(FileAttr... files) {
        return L.document(new DocumentAttr().attachments(List.of(files)),
                List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of(L.text(NoAttr, List.of("Invoice"))))))));
    }

    private static long embeddedFiles(byte[] pdf) {
        return Pattern.compile("/Type /EmbeddedFile").matcher(latin1(pdf)).results().count();
    }

    @Test
    void embeds_the_files_a_document_carries_from_load_file_or_their_src() throws IOException {
        var terms = Files.createTempFile("terms", ".txt");
        Files.writeString(terms, "Terms of sale");
        try {
            var document = withAttachments(
                    new FileAttr().name("invoice.xml").ref("invoice").mime("text/xml").relationship(FileRelationship.DATA),
                    new FileAttr().name("terms.txt").src(terms.toString()).description("The terms"));
            var reading = L.engine(new EngineOptions().srcFallback(FILES)).setLicenseKey("test-key");
            reading.loadFile("invoice", "<invoice/>".getBytes(StandardCharsets.UTF_8));

            var pdf = reading.render(document);

            assertEquals(2, embeddedFiles(pdf));
            assertTrue(latin1(pdf).contains("/AFRelationship /Data") && latin1(pdf).contains("/Subtype /text#2Fxml"));
            assertArrayEquals(pdf, reading.render(L.toXml(document)));
        } finally {
            Files.delete(terms);
        }
    }

    @Test
    void a_file_src_without_a_src_fallback_or_that_cannot_be_read_is_an_error_that_names_it() {
        var document = withAttachments(new FileAttr().name("data.csv").src("/no/such/data.csv"));
        var reading = L.engine(new EngineOptions().srcFallback(FILES)).setLicenseKey("test-key");

        var noFallback = assertThrows(EngineException.class, () -> engine.render(document));
        var unreadable = assertThrows(EngineException.class, () -> reading.render(document));

        assertTrue(noFallback.getMessage().contains("srcFallback") && noFallback.getMessage().contains("data.csv"),
                noFallback.getMessage());
        assertTrue(unreadable.getMessage().contains("cannot read its src '/no/such/data.csv'"), unreadable.getMessage());
    }

    @Test
    void reads_the_src_of_a_file_as_the_engine_reads_its_xml() throws IOException {
        var folder = Files.createTempDirectory("lpdf-files-");
        var file = folder.resolve("R&D terms.txt");
        Files.writeString(file, "Terms");
        try {
            var document = withAttachments(
                    new FileAttr().name("Terms & Conditions.txt").src(file.toString()).description("Rows > 100"));
            var xml = L.toXml(document)
                    .replace("<attachments>", "<attachments><!-- <file name=\"old.csv\" src=\"/no/such/old.csv\"/> -->");
            var reading = L.engine(new EngineOptions().srcFallback(FILES)).setLicenseKey("test-key");

            assertEquals(1, embeddedFiles(reading.render(xml)));
            assertArrayEquals(reading.render(document), reading.render(xml));
        } finally {
            Files.delete(file);
            Files.delete(folder);
        }
    }

    @Test
    void a_file_with_no_bytes_is_an_error_that_says_how_to_load_it() {
        var error = assertThrows(EngineException.class, () -> engine.render(withAttachments(new FileAttr().name("a.xml"))));

        assertTrue(error.getMessage().contains("loadFile('a.xml', bytes)"), error.getMessage());
    }

    @Test
    void a_loaded_image_renders() {
        // A 1×1 white grayscale PNG.
        engine.loadImage("testimg", HexFormat.of().parseHex("89504e470d0a1a0a0000000d49484452000000010000000108000000003a7e9b55"
                + "0000000a49444154789c6260000000020001e221bc330000000049454e44ae426082"));

        var pdf = engine.render("<lpdf version=\"1\"><assets><image name=\"testimg\"/></assets><document><section><layout>"
                + "<img name=\"testimg\" width=\"10pt\"/></layout></section></document></lpdf>");

        assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));
    }
}
