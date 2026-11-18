package io.lpdf;

/**
 * The options of {@link LCrossIndustryInvoice#generatePdf(PdfEngine, String, GeneratePdfOptions)}: the invoice's XML
 * and the creation date are required.
 *
 * <pre>{@code
 * new GeneratePdfOptions().invoiceXml(Files.readAllBytes(path)).createdOn(Instant.now().toString())
 * }</pre>
 */
public final class GeneratePdfOptions {
    private byte[] invoiceXml;
    private String invoiceMap;
    private InvoiceFormat format;
    private Object data;
    private String createdOn;

    public GeneratePdfOptions() {
    }

    /** The invoice: its XML, which the values are read from and which the PDF carries byte for byte. */
    public GeneratePdfOptions invoiceXml(byte[] invoiceXml) {
        this.invoiceXml = invoiceXml;
        return this;
    }

    public byte[] invoiceXml() {
        return invoiceXml;
    }

    /**
     * The map, an XML document: without it, Lpdf's default map ({@link LCrossIndustryInvoice#defaultInvoiceMap()}). A
     * map given takes over whole. A document built in code has no bindings and needs none.
     */
    public GeneratePdfOptions invoiceMap(String invoiceMap) {
        this.invoiceMap = invoiceMap;
        return this;
    }

    public String invoiceMap() {
        return invoiceMap;
    }

    /** How the values of each type are written. */
    public GeneratePdfOptions format(InvoiceFormat format) {
        this.format = format;
        return this;
    }

    public InvoiceFormat format() {
        return format;
    }

    /**
     * The values the XML lacks, which a template binds without a prefix, as {@code note}: maps, lists, records,
     * strings, numbers, booleans and null.
     */
    public GeneratePdfOptions data(Object data) {
        this.data = data;
        return this;
    }

    public Object data() {
        return data;
    }

    /** The ISO 8601 creation date of the file, which PDF/A requires. */
    public GeneratePdfOptions createdOn(String createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public String createdOn() {
        return createdOn;
    }
}
