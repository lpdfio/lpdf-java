package io.lpdf;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A Factur-X / ZUGFeRD invoice PDF made from the invoice's XML, the EN 16931 Cross Industry Invoice that accounting
 * software writes. The engine reads the XML through a map and checks it; this class hands the values to the caller's
 * {@link InvoiceFormat} functions.
 *
 * <pre>{@code
 * byte[] pdf = LCrossIndustryInvoice.generatePdf(L.engine(), template, new GeneratePdfOptions()
 *     .invoiceXml(Files.readAllBytes(Path.of("invoice.xml")))
 *     .createdOn(Instant.now().toString()));
 * }</pre>
 */
public final class LCrossIndustryInvoice {
    /**
     * The runner of {@link #extractInvoiceData(byte[], ExtractInvoiceDataOptions)} and {@link #defaultInvoiceMap()},
     * which take no engine: the default time and memory limits. {@code generatePdf} reads through its engine instead.
     */
    private static final WasmRunner READER = WasmRunner.withoutEngine(WasmRunner.ENGINE);

    private LCrossIndustryInvoice() {
    }

    /**
     * The invoice's PDF, a Factur-X / ZUGFeRD file: {@code template}, an XML string, filled with the values of the
     * invoice, which it binds under the map's prefix ({@code invoice:seller.name}) beside the options' data. The file is
     * PDF/A-3, 3b unless the template sets 3a or 3u, and carries the XML as {@code factur-x.xml}, or
     * {@code xrechnung.xml} for XRECHNUNG. The template says only how the invoice looks: it declares no invoice file.
     *
     * @param engine   the engine, which carries the fonts, the license key and the limits: its timeout and memory cap
     *                 hold for the read of the invoice as for the render
     * @param template how the invoice looks, as lpdf XML
     * @param options  the invoice's XML, and the map, the format functions, the data and the creation date
     * @throws EngineException          when the engine refuses the map or the invoice, as
     *                                  {@link #extractInvoiceData(byte[], ExtractInvoiceDataOptions)} says, or a
     *                                  template path under the prefix that the map lacks, a PDF/A level other than 3a,
     *                                  3b or 3u, {@code encrypt}, {@code pdfx}, or a template that declares the invoice
     *                                  file; or the read or the render runs past the engine's timeout or memory cap
     * @throws IllegalArgumentException when the invoice's XML or the creation date is missing
     * @throws NullPointerException     when a format function returns null, naming its type and the entry
     */
    public static byte[] generatePdf(PdfEngine engine, String template, GeneratePdfOptions options) {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(template, "template");
        check(options);
        var invoice = new LinkedHashMap<String, Object>();
        invoice.put("xml", Base64.getEncoder().encodeToString(options.invoiceXml()));
        invoice.put("map", options.invoiceMap());
        invoice.put("data", formatted(engine.extractInvoiceData(options.invoiceXml(), options.invoiceMap()), options.format()));
        return engine.renderInvoice(template, invoice, options.data(), options.createdOn());
    }

    /**
     * The invoice's PDF, a Factur-X / ZUGFeRD file, from a document built in code: the file is PDF/A-3, 3b unless the
     * document sets 3a or 3u, and carries the XML as {@code factur-x.xml}, or {@code xrechnung.xml} for XRECHNUNG. The
     * document has no bindings, so only the invoice's XML and the creation date of the options apply.
     *
     * @param engine   the engine, which carries the fonts, the license key and the limits
     * @param document the document, built with {@link L}
     * @param options  the invoice's XML and the creation date
     * @throws EngineException          when the engine refuses the invoice, a PDF/A level other than 3a, 3b or 3u,
     *                                  {@code encrypt}, {@code pdfx}, or a document that declares the invoice file
     * @throws IllegalArgumentException when the invoice's XML or the creation date is missing
     */
    public static byte[] generatePdf(PdfEngine engine, PdfDocument document, GeneratePdfOptions options) {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(document, "document");
        check(options);
        var invoice = new LinkedHashMap<String, Object>();
        invoice.put("xml", Base64.getEncoder().encodeToString(options.invoiceXml()));
        return engine.renderInvoice(document, invoice, options.createdOn());
    }

    /** The values of an invoice read through the default map: {@link #extractInvoiceData(byte[], ExtractInvoiceDataOptions)}. */
    public static Map<String, Object> extractInvoiceData(byte[] invoiceXml) {
        return extractInvoiceData(invoiceXml, new ExtractInvoiceDataOptions());
    }

    /**
     * The values of an invoice as plain data: a value is a field, a group a map, a list a list, all strings, as the map
     * names them. A value the invoice lacks is absent, and a list with no items is empty. For code that needs the
     * values itself: a document built in code, choosing a template, naming the file. The read runs under the default
     * timeout and memory cap of {@link EngineOptions}.
     *
     * @param invoiceXml the invoice's XML, an EN 16931 Cross Industry Invoice
     * @param options    the map, and the format functions
     * @throws EngineException      when the map is not one or doesn't fit the schema, the invoice is not a Cross
     *                              Industry Invoice or is MINIMUM or BASIC WL, or lacks a value the map marks
     *                              required; or the read runs past the default timeout or memory cap
     * @throws NullPointerException when a format function returns null, naming its type and the entry
     */
    public static Map<String, Object> extractInvoiceData(byte[] invoiceXml, ExtractInvoiceDataOptions options) {
        Objects.requireNonNull(invoiceXml, "invoiceXml");
        Objects.requireNonNull(options, "options");
        return formatted(READER.extractInvoiceData(invoiceXml, options.invoiceMap()), options.format());
    }

    /** {@link #extractInvoiceData(byte[])} of XML given as text. */
    public static Map<String, Object> extractInvoiceData(String invoiceXml) {
        return extractInvoiceData(invoiceXml, new ExtractInvoiceDataOptions());
    }

    /** {@link #extractInvoiceData(byte[], ExtractInvoiceDataOptions)} of XML given as text. */
    public static Map<String, Object> extractInvoiceData(String invoiceXml, ExtractInvoiceDataOptions options) {
        Objects.requireNonNull(invoiceXml, "invoiceXml");
        return extractInvoiceData(invoiceXml.getBytes(StandardCharsets.UTF_8), options);
    }

    /**
     * Lpdf's default invoice map, as XML text: the values EN 16931 numbers but the parties' identifiers BT-29,
     * BT-46, BT-60 and BT-71, what a line may hold several of, and an attached file's content. The start of a map of
     * your own.
     */
    public static String defaultInvoiceMap() {
        return READER.defaultInvoiceMap();
    }

    private static void check(GeneratePdfOptions options) {
        Objects.requireNonNull(options, "options");
        if (options.invoiceXml() == null) throw new IllegalArgumentException("generatePdf needs invoiceXml, the invoice's XML");
        if (options.createdOn() == null) throw new IllegalArgumentException("generatePdf needs createdOn, which PDF/A requires");
    }

    /**
     * The values of the engine's {@code extract_invoice_data} answer, with the format function of each typed one
     * applied.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> formatted(Map<String, Object> answer, InvoiceFormat format) {
        var data = (Map<String, Object>) answer.get("data");
        for (var entry : (List<Object>) answer.get("typed")) {
            var typed = (Map<String, Object>) entry;
            var type = (String) typed.get("type");
            var fn = format == null ? null : format.of(type);
            if (fn == null) continue;
            var at = (List<Object>) typed.get("at");
            Object holder = data;
            for (var step : at.subList(0, at.size() - 1)) holder = step(holder, step);
            var last = at.get(at.size() - 1);
            var name = (String) typed.get("name");
            // Refused here, where the function and its entry are known.
            var written = Objects.requireNonNull(fn.apply((String) step(holder, last), name), () -> "format." + type
                    + " returned null for '" + name + "': return the text to bind, or the value as it came.");
            if (last instanceof Number index) ((List<Object>) holder).set(index.intValue(), written);
            else ((Map<String, Object>) holder).put((String) last, written);
        }
        return data;
    }

    /** What is at {@code step} of {@code holder}: a key of a map or an index of a list. */
    @SuppressWarnings("unchecked")
    private static Object step(Object holder, Object step) {
        return step instanceof Number index ? ((List<Object>) holder).get(index.intValue()) : ((Map<String, Object>) holder).get(step);
    }
}
