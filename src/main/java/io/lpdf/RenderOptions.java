package io.lpdf;

/**
 * The options of one render: <code>engine.render(xml, new RenderOptions().data(Map.of("total", "$800")))</code>.
 */
public final class RenderOptions {
    private String createdOn;
    private Object data;
    private String dataJson;
    private String userPassword;
    private String ownerPassword;
    private boolean skipEncryption;

    /** No options: a render with no date, no data and no passwords. */
    public RenderOptions() {
    }

    /**
     * The creation time, ISO 8601, such as <code>2024-06-01T12:00:00</code>, written as the PDF's
     * <code>/CreationDate</code>. Left out, the PDF has no date, so a render gives the same bytes every time. A
     * document whose output asks for <code>pdfa</code>, <code>pdfua</code> or <code>pdfx</code> needs it, and writes
     * it as both dates of the file.
     */
    public RenderOptions createdOn(String createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    /** The creation time, or null. */
    public String createdOn() {
        return createdOn;
    }

    /**
     * The data the <code>data-*</code> attributes of an XML template bind to: maps, lists, arrays, records, strings,
     * numbers, booleans and null, written as JSON. Data that is JSON text already goes in {@link #dataJson(String)};
     * the one set last is used. Left out, a template renders with the content it holds. A document built with
     * {@link L} has no data.
     */
    public RenderOptions data(Object data) {
        this.data = data;
        this.dataJson = null;
        return this;
    }

    /** The data, or null. */
    public Object data() {
        return data;
    }

    /**
     * The data as JSON text, such as a file's or a request body's, which goes to the engine as it is: Java has no
     * JSON reader of its own, and this needs none. In place of {@link #data(Object)}: the one set last is used.
     *
     * @throws IllegalArgumentException when {@code json} is not JSON, saying where
     */
    public RenderOptions dataJson(String json) {
        if (json != null) Json.check(json, "The data");
        this.dataJson = json;
        this.data = null;
        return this;
    }

    /** The data as JSON text, or null. */
    public String dataJson() {
        return dataJson;
    }

    /**
     * The password a reader asks for before showing the PDF, for a document that asks for encryption in its
     * <code>output</code>. Left out, or <code>""</code>, anyone can open the PDF, and only the output's
     * <code>allow</code> restricts it.
     */
    public RenderOptions userPassword(String userPassword) {
        this.userPassword = userPassword;
        return this;
    }

    /** The user password, or null. */
    public String userPassword() {
        return userPassword;
    }

    /**
     * The password that opens the PDF with every permission. A document that asks for encryption in its
     * <code>output</code> renders only with it, or with {@link #skipEncryption(boolean)}; a document that doesn't ask
     * for encryption refuses it, so a file never goes out unencrypted while you believe it is locked.
     */
    public RenderOptions ownerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
        return this;
    }

    /** The owner password, or null. */
    public String ownerPassword() {
        return ownerPassword;
    }

    /**
     * Renders a document that asks for encryption unencrypted, for a render that can't have the passwords, such as a
     * preview. Not with the passwords.
     */
    public RenderOptions skipEncryption(boolean skipEncryption) {
        this.skipEncryption = skipEncryption;
        return this;
    }

    /** Whether the render skips the encryption. */
    public boolean skipEncryption() {
        return skipEncryption;
    }
}
