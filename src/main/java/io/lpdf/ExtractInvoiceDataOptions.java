package io.lpdf;

/** The options of {@link LCrossIndustryInvoice#extractInvoiceData(byte[], ExtractInvoiceDataOptions)}. */
public final class ExtractInvoiceDataOptions {
    private String invoiceMap;
    private InvoiceFormat format;

    /** The default map, and every value bound as the invoice has it. */
    public ExtractInvoiceDataOptions() {
    }

    /**
     * The map, an XML document: without it, Lpdf's default map ({@link LCrossIndustryInvoice#defaultInvoiceMap()}). A
     * map given takes over whole.
     */
    public ExtractInvoiceDataOptions invoiceMap(String invoiceMap) {
        this.invoiceMap = invoiceMap;
        return this;
    }

    public String invoiceMap() {
        return invoiceMap;
    }

    /** How the values of each type are written. */
    public ExtractInvoiceDataOptions format(InvoiceFormat format) {
        this.format = format;
        return this;
    }

    public InvoiceFormat format() {
        return format;
    }
}
