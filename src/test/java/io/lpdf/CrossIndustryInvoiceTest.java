package io.lpdf;

import static io.lpdf.L.NoAttr;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * {@link LCrossIndustryInvoice}: a Factur-X / ZUGFeRD invoice PDF made from the invoice's XML. The template, the
 * invoices, the font and the snapshot hash are core's, its test/fixtures, test/assets and test/snapshots, which CI copies
 * in from the core tag.
 */
class CrossIndustryInvoiceTest {
    private static final String CREATED_ON = "2026-11-18T10:00:00Z";
    private static final String NOTE = "Payable within 30 days, by bank transfer.";
    private static final Path FACTURX = Snapshots.ROOT.resolve("test/assets/files/facturx");
    private static final String RECHNUNG_MAP = "<cross-industry-invoice-map prefix=\"rechnung\">"
            + "<value name=\"nummer\" path=\"rsm:ExchangedDocument/ram:ID\"/></cross-industry-invoice-map>";
    private static final String RECHNUNG_TEMPLATE = "<lpdf version=\"1\"><assets><font name=\"body\" src=\"../assets/fonts/SourceSans3-Regular.otf\"/>"
            + "</assets><document size=\"a4\" font=\"body\"><section><layout margin=\"48pt\">"
            + "<text data-value=\"rechnung:nummer\">0000</text></layout></section></document></lpdf>";

    private static byte[] bytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] invoice(String name) {
        return bytes(FACTURX.resolve(name));
    }

    private static String template() {
        return Snapshots.fixture("showcase-cross-industry-invoice");
    }

    /** An unlicensed engine with the template's font loaded under its name. */
    private static PdfEngine engine() {
        return engine(new EngineOptions());
    }

    /** An unlicensed engine with {@code options} and the template's font loaded under its name. */
    private static PdfEngine engine(EngineOptions options) {
        return L.engine(options).loadFont("body", bytes(Snapshots.ROOT.resolve("test/assets/fonts/SourceSans3-Regular.otf")));
    }

    /** en16931.xml with its first line repeated {@code count} times in place of its lines. */
    private static byte[] invoiceWithLines(int count) {
        var xml = new String(invoice("en16931.xml"), StandardCharsets.UTF_8);
        var open = "<ram:IncludedSupplyChainTradeLineItem>";
        var close = "</ram:IncludedSupplyChainTradeLineItem>";
        var start = xml.indexOf(open);
        var line = xml.substring(start, xml.indexOf(close) + close.length());
        var end = xml.lastIndexOf(close) + close.length();
        return (xml.substring(0, start) + line.repeat(count) + xml.substring(end)).getBytes(StandardCharsets.UTF_8);
    }

    private static final Map<String, Map<String, String>> CODE_NAMES = Map.of(
            "typeCode", Map.of("380", "Invoice", "381", "Credit note"),
            "lines.unit", Map.of("H87", "pcs", "KGM", "kg"));

    /**
     * How values are written on the page: dates and amounts through Java's own formatters, and a code as the word for it
     * under its entry's name, or as it came when there is none.
     */
    private static final InvoiceFormat FORMAT = new InvoiceFormat()
            .date((value, entryName) -> LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)))
            .amount((value, entryName) -> String.format(Locale.UK, "%,.2f", new BigDecimal(value)))
            .code((value, entryName) -> CODE_NAMES.getOrDefault(entryName, Map.of()).getOrDefault(value, value));

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Object value) {
        return (List<Object>) value;
    }

    /**
     * The page of core's {@code showcase-cross-industry-invoice} fixture built in code, element for element, from the
     * values of {@code extractInvoiceData}, so that it renders to the bytes of the template filled by
     * {@code generatePdf}.
     */
    private static PdfDocument documentInCode(Map<String, Object> invoiceData) {
        var muted = new TextAttr().fontSize("9pt").color("#5b6475");
        var mutedRight = new TextAttr().fontSize("9pt").color("#5b6475").align("right");
        var right = new TextAttr().align("right");
        var title = new TextAttr().fontSize("16pt").color("#1763cf");
        var total = new TextAttr().fontSize("14pt");
        var seller = map(invoiceData.get("seller"));
        var buyer = map(invoiceData.get("buyer"));
        var totals = map(invoiceData.get("totals"));

        var dates = new ArrayList<Node>(List.of(L.text(muted, List.of("Issued · Due")), L.text(NoAttr, List.of(invoiceData.get("issued")))));
        for (var term : list(invoiceData.get("paymentTerms"))) {
            dates.add(L.row(NoAttr, List.of(L.text(NoAttr, List.of(map(term).get("due"))))));
        }
        var lines = new ArrayList<Node>(List.of(L.thead(NoAttr, List.of(
                L.td(NoAttr, List.of(L.text(muted, List.of("Item")))),
                L.td(NoAttr, List.of(L.text(mutedRight, List.of("Qty")))),
                L.td(NoAttr, List.of(L.text(mutedRight, List.of("Unit")))),
                L.td(NoAttr, List.of(L.text(mutedRight, List.of("Price")))),
                L.td(NoAttr, List.of(L.text(mutedRight, List.of("VAT %")))),
                L.td(NoAttr, List.of(L.text(mutedRight, List.of("Amount"))))))));
        for (var item : list(invoiceData.get("lines"))) {
            var line = map(item);
            lines.add(L.tr(NoAttr, List.of(
                    L.td(NoAttr, List.of(L.text(NoAttr, List.of(line.get("name"))))),
                    L.td(NoAttr, List.of(L.text(right, List.of(line.get("quantity"))))),
                    L.td(NoAttr, List.of(L.text(right, List.of(line.get("unit"))))),
                    L.td(NoAttr, List.of(L.text(right, List.of(line.get("unitPrice"))))),
                    L.td(NoAttr, List.of(L.text(right, List.of(line.get("vatRate"))))),
                    L.td(NoAttr, List.of(L.text(right, List.of(line.get("net"))))))));
        }
        var taxes = new ArrayList<Node>(List.of(L.thead(NoAttr, List.of(
                L.td(NoAttr, List.of(L.text(muted, List.of("VAT rate (%)")))),
                L.td(NoAttr, List.of(L.text(mutedRight, List.of("Basis")))),
                L.td(NoAttr, List.of(L.text(mutedRight, List.of("VAT"))))))));
        for (var item : list(invoiceData.get("taxes"))) {
            var tax = map(item);
            taxes.add(L.tr(NoAttr, List.of(
                    L.td(NoAttr, List.of(L.text(NoAttr, List.of(tax.get("rate"))))),
                    L.td(NoAttr, List.of(L.text(right, List.of(tax.get("basis"))))),
                    L.td(NoAttr, List.of(L.text(right, List.of(tax.get("amount"))))))));
        }
        var sums = new ArrayList<Node>(List.of(L.row(new RowAttr().gap("xs"), List.of(
                L.text(NoAttr, List.of("VAT")), L.text(NoAttr, List.of(totals.get("vat"))), L.text(NoAttr, List.of(totals.get("currency")))))));
        if (totals.containsKey("vatInTaxCurrency")) {
            sums.add(L.row(new RowAttr().gap("xs"), List.of(L.text(muted, List.of("VAT in")),
                    L.text(muted, List.of(totals.get("taxCurrency"))), L.text(muted, List.of(totals.get("vatInTaxCurrency"))))));
        }
        sums.add(L.row(new RowAttr().gap("xs"), List.of(
                L.text(total, List.of("Total")), L.text(total, List.of(totals.get("gross"))), L.text(total, List.of(totals.get("currency"))))));

        return L.document(new DocumentAttr().size("a4").font("body")
                        .assets(new DocumentAssets().fonts(List.of(new FontAttr().name("body").src("../assets/fonts/SourceSans3-Regular.otf"))))
                        .meta(new DocumentMeta().title("Invoice").author("Northwind Paper SAS").lang("en")),
                List.of(L.section(NoAttr, List.of(L.layout(new LayoutAttr().margin("48pt"), List.of(
                        L.column(new ColumnAttr().gap("l"), List.of(
                                L.row(new RowAttr().align("end"), List.of(
                                        L.column(new ColumnAttr().width("fill").gap("xs"), List.of(
                                                L.text(new TextAttr().fontSize("20pt"), List.of(seller.get("name"))),
                                                L.text(muted, List.of(seller.get("street"))),
                                                L.row(new RowAttr().gap("xs"), List.of(L.text(muted, List.of(seller.get("postcode"))), L.text(muted, List.of(seller.get("city"))))),
                                                L.row(new RowAttr().gap("xs"), List.of(L.text(muted, List.of("VAT")), L.text(muted, List.of(seller.get("vatId"))))))),
                                        L.row(new RowAttr().gap("xs"), List.of(
                                                L.text(title, List.of(invoiceData.get("typeCode"))), L.text(title, List.of(invoiceData.get("number"))))))),
                                L.divider(new DividerAttr().color("#dde3ed").thickness("0.5pt")),
                                L.row(new RowAttr().gap("l"), List.of(
                                        L.column(new ColumnAttr().width("fill").gap("xs"), List.of(
                                                L.text(muted, List.of("Billed to")),
                                                L.text(NoAttr, List.of(buyer.get("name"))),
                                                L.text(NoAttr, List.of(buyer.get("street"))),
                                                L.row(new RowAttr().gap("xs"), List.of(L.text(NoAttr, List.of(buyer.get("postcode"))), L.text(NoAttr, List.of(buyer.get("city"))))))),
                                        L.column(new ColumnAttr().width("fill").gap("xs"), dates))),
                                L.table(new TableAttr().cols("4fr 1fr 1fr 1fr 1fr 1fr"), lines),
                                L.divider(new DividerAttr().color("#dde3ed").thickness("0.5pt")),
                                L.table(new TableAttr().cols("1fr 1fr 1fr"), taxes),
                                L.column(new ColumnAttr().gap("xs").align("end"), sums),
                                L.text(muted, List.of(NOTE))))))))));
    }

    @Test
    void fills_the_template_with_the_invoice_and_gives_the_pdf_core_gives() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var pdf = LCrossIndustryInvoice.generatePdf(engine(), template(), new GeneratePdfOptions()
                .invoiceXml(invoice("en16931.xml")).data(Map.of("note", NOTE)).createdOn(CREATED_ON));
        assertEquals(Snapshots.snapshot("showcase-cross-industry-invoice"), Snapshots.sha256(pdf));
        var text = new String(pdf, StandardCharsets.ISO_8859_1);
        assertTrue(text.contains("<pdfaid:part>3</pdfaid:part>"));
        assertTrue(text.contains("<fx:DocumentFileName>factur-x.xml</fx:DocumentFileName>"));
    }

    @Test
    void gives_the_same_page_from_the_template_and_from_the_document_built_in_code() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var invoiceXml = invoice("en16931.xml");
        var engine = engine();
        var fromTemplate = LCrossIndustryInvoice.generatePdf(engine, template(), new GeneratePdfOptions()
                .invoiceXml(invoiceXml).format(FORMAT).data(Map.of("note", NOTE)).createdOn(CREATED_ON));
        var invoiceData = LCrossIndustryInvoice.extractInvoiceData(invoiceXml, new ExtractInvoiceDataOptions().format(FORMAT));
        var fromCode = LCrossIndustryInvoice.generatePdf(engine, documentInCode(invoiceData), new GeneratePdfOptions()
                .invoiceXml(invoiceXml).createdOn(CREATED_ON));
        assertArrayEquals(fromTemplate, fromCode);
    }

    @Test
    void extracts_the_values_core_reads_and_formats_them_by_type_and_entry_name() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var expected = Json.readObject(new String(bytes(FACTURX.resolve("case-vat-rates.json")), StandardCharsets.UTF_8));
        assertEquals(expected, LCrossIndustryInvoice.extractInvoiceData(invoice("case-vat-rates.xml")));
        var formatted = LCrossIndustryInvoice.extractInvoiceData(invoice("en16931.xml"), new ExtractInvoiceDataOptions().format(FORMAT));
        assertEquals("31 October 2026", formatted.get("issued"));
        assertEquals("Invoice", formatted.get("typeCode"));
        assertEquals("pcs", map(list(formatted.get("lines")).get(0)).get("unit"));
        assertEquals("69.60", map(formatted.get("totals")).get("gross"));
        assertEquals("30 November 2026", map(list(formatted.get("paymentTerms")).get(0)).get("due"));
    }

    @Test
    void reads_through_a_map_given_under_its_own_names() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var xml = new String(invoice("en16931.xml"), StandardCharsets.UTF_8);
        assertEquals(Map.of("nummer", "2026-0412"), LCrossIndustryInvoice.extractInvoiceData(xml, new ExtractInvoiceDataOptions().invoiceMap(RECHNUNG_MAP)));
    }

    @Test
    void reads_the_xml_given_as_text_as_it_reads_its_bytes() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var xml = new String(invoice("en16931.xml"), StandardCharsets.UTF_8);
        assertEquals(LCrossIndustryInvoice.extractInvoiceData(invoice("en16931.xml")), LCrossIndustryInvoice.extractInvoiceData(xml));
    }

    @Test
    void fills_a_template_through_a_map_given_under_its_own_names() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var options = new GeneratePdfOptions().invoiceXml(invoice("en16931.xml")).invoiceMap(RECHNUNG_MAP).createdOn(CREATED_ON);
        var engine = engine();
        var bound = LCrossIndustryInvoice.generatePdf(engine, RECHNUNG_TEMPLATE, options);
        var written = RECHNUNG_TEMPLATE.replace("<text data-value=\"rechnung:nummer\">0000</text>", "<text>2026-0412</text>");
        assertArrayEquals(LCrossIndustryInvoice.generatePdf(engine, written, options), bound);
    }

    @Test
    void refuses_a_template_name_that_the_map_given_lacks() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var typo = RECHNUNG_TEMPLATE.replace("rechnung:nummer", "rechnung:nummr");
        var error = assertThrows(EngineException.class, () -> LCrossIndustryInvoice.generatePdf(engine(), typo,
                new GeneratePdfOptions().invoiceXml(invoice("en16931.xml")).invoiceMap(RECHNUNG_MAP).createdOn(CREATED_ON)));
        assertEquals("data-value=\"rechnung:nummr\": 'nummr' is not in the map: the invoice map has nummer", error.getMessage());
    }

    @Test
    void reads_the_invoice_of_a_template_under_the_memory_cap_of_its_engine() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        // 1.2 MB of XML: more than the cap of 1 MB on its own, and small enough to read well within the default timeout.
        var invoiceXml = invoiceWithLines(1_000);
        assertTrue(invoiceXml.length > 1_048_576);
        assertEquals(1_000, list(LCrossIndustryInvoice.extractInvoiceData(invoiceXml).get("lines")).size());
        var formatted = new AtomicInteger();
        var format = new InvoiceFormat().amount((value, entryName) -> {
            formatted.incrementAndGet();
            return value;
        });
        var error = assertThrows(EngineException.class, () -> LCrossIndustryInvoice.generatePdf(
                engine(new EngineOptions().maxMemoryMb(1)), template(), new GeneratePdfOptions()
                        .invoiceXml(invoiceXml).format(format).data(Map.of("note", NOTE)).createdOn(CREATED_ON)));
        assertTrue(error.getMessage().startsWith("the render needs more memory than its cap of 1 MB"), error.getMessage());
        // The read itself was refused: read under the default limits, it would have handed the amounts to the format
        // function before the render failed.
        assertEquals(0, formatted.get());
    }

    @Test
    void refuses_a_format_function_that_returns_null_naming_its_type_and_entry() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var format = new InvoiceFormat().code((value, entryName) -> entryName.equals("lines.unit") ? null : value);
        var error = assertThrows(NullPointerException.class, () -> LCrossIndustryInvoice.extractInvoiceData(
                invoice("en16931.xml"), new ExtractInvoiceDataOptions().format(format)));
        assertEquals("format.code returned null for 'lines.unit': return the text to bind, or the value as it came.",
                error.getMessage());
    }

    @Test
    void gives_the_default_map_as_xml_text() {
        var invoiceMap = LCrossIndustryInvoice.defaultInvoiceMap();
        assertTrue(invoiceMap.startsWith("<?xml"));
        assertTrue(invoiceMap.contains("<cross-industry-invoice-map prefix=\"invoice\">"));
    }

    @Test
    void refuses_what_the_engine_refuses_with_its_error() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var error = assertThrows(EngineException.class, () -> LCrossIndustryInvoice.extractInvoiceData(invoice("minimum.xml")));
        assertTrue(error.getMessage().startsWith("the invoice's profile is MINIMUM"), error.getMessage());
        var typo = template().replace("invoice:seller.name", "invoice:sellr.name");
        error = assertThrows(EngineException.class, () -> LCrossIndustryInvoice.generatePdf(engine(), typo,
                new GeneratePdfOptions().invoiceXml(invoice("en16931.xml")).createdOn(CREATED_ON)));
        assertTrue(error.getMessage().startsWith("data-value=\"invoice:sellr.name\": 'sellr' is not in the map"), error.getMessage());
        assertThrows(IllegalArgumentException.class, () -> LCrossIndustryInvoice.generatePdf(engine(), template(),
                new GeneratePdfOptions().invoiceXml(invoice("en16931.xml"))));
    }
}
