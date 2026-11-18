package io.lpdf;

import static io.lpdf.L.NoAttr;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        return L.engine().loadFont("body", bytes(Snapshots.ROOT.resolve("test/assets/fonts/SourceSans3-Regular.otf")));
    }

    private static final Map<String, Map<String, String>> CODE_NAMES = Map.of(
            "typeCode", Map.of("380", "Invoice", "381", "Credit note"),
            "lines.unit", Map.of("H87", "pcs", "KGM", "kg"));

    /** How values are written on the page, as the examples write them: Java's own formatters, and the words for the codes. */
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

    /** The page of the template, built in code with the values of {@code extractInvoiceData}, as the second example builds it. */
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
        var pdf = LCrossIndustryInvoice.generatePdf(engine(), template(), new GeneratePdfOptions()
                .invoiceXml(invoice("en16931.xml")).data(Map.of("note", NOTE)).createdOn(CREATED_ON));
        assertEquals(Snapshots.snapshot("showcase-cross-industry-invoice"), Snapshots.sha256(pdf));
        var text = new String(pdf, StandardCharsets.ISO_8859_1);
        assertTrue(text.contains("<pdfaid:part>3</pdfaid:part>"));
        assertTrue(text.contains("<fx:DocumentFileName>factur-x.xml</fx:DocumentFileName>"));
    }

    @Test
    void gives_the_same_page_from_the_template_and_from_the_document_built_in_code() {
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
        var invoiceMap = "<cross-industry-invoice-map prefix=\"rechnung\"><value name=\"nummer\" path=\"rsm:ExchangedDocument/ram:ID\"/></cross-industry-invoice-map>";
        var xml = new String(invoice("en16931.xml"), StandardCharsets.UTF_8);
        assertEquals(Map.of("nummer", "2026-0412"), LCrossIndustryInvoice.extractInvoiceData(xml, new ExtractInvoiceDataOptions().invoiceMap(invoiceMap)));
    }

    @Test
    void gives_the_default_map_as_xml_text() {
        var invoiceMap = LCrossIndustryInvoice.defaultInvoiceMap();
        assertTrue(invoiceMap.startsWith("<?xml"));
        assertTrue(invoiceMap.contains("<cross-industry-invoice-map prefix=\"invoice\">"));
    }

    @Test
    void refuses_what_the_engine_refuses_with_its_error() {
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
