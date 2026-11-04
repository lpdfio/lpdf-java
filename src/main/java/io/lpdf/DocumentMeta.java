package io.lpdf;

/**
 * The PDF's metadata: its title, author, subject, keywords and creator, and the language the document is written in.
 */
public final class DocumentMeta extends Attributes {
    /** Attributes with none set. */
    public DocumentMeta() {
        super("meta");
    }

    public DocumentMeta title(String title) {
        set("title", title);
        return this;
    }

    public DocumentMeta author(String author) {
        set("author", author);
        return this;
    }

    public DocumentMeta subject(String subject) {
        set("subject", subject);
        return this;
    }

    public DocumentMeta keywords(String keywords) {
        set("keywords", keywords);
        return this;
    }

    public DocumentMeta creator(String creator) {
        set("creator", creator);
        return this;
    }

    /**
     * The language the document is written in, as a language tag such as en, en-US or de-CH, which a screen reader
     * reads it in. A PDF/UA document needs it, and a PDF/A document of level a takes it only as a language tag.
     */
    public DocumentMeta lang(String lang) {
        set("lang", lang);
        return this;
    }
}
