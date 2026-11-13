package io.lpdf;

import java.util.List;

/**
 * The whole document, and the page settings every section starts from: size, orientation, background and font. It holds
 * an optional meta element, an optional output element, optional attachments and at least one section. Its tokens,
 * assets, meta, output and attachments are set here too, as the XML writes them in elements of their own.
 */
public final class DocumentAttr extends Attributes {
    /** Attributes with none set. */
    public DocumentAttr() {
        super("document");
    }

    public DocumentAttr font(String font) {
        set("font", font);
        return this;
    }

    public DocumentAttr size(String size) {
        set("size", size);
        return this;
    }

    public DocumentAttr orientation(String orientation) {
        set("orientation", orientation);
        return this;
    }

    /**
     * How far the page reaches past its trim edge on every side, for a background or an image that runs off the page
     * when it is cut: such as 3mm, which most printers ask for. The PDF page grows by it: its TrimBox is the page's
     * size and its BleedBox the grown page, and the page background fills the bleed. Layout and positions keep to the
     * trimmed page, so a document lays out the same with or without it. A section's own bleed overrides it.
     */
    public DocumentAttr bleed(String bleed) {
        set("bleed", bleed);
        return this;
    }

    public DocumentAttr background(String background) {
        set("background", background);
        return this;
    }

    public DocumentAttr debug(boolean debug) {
        set("debug", debug);
        return this;
    }

    /**
     * Where the numbers of the footnotes start over: on every page, in every section (the default), or never, so that
     * they run on through the document. A section's own footnote-counter overrides it.
     */
    public DocumentAttr footnoteCounter(String footnoteCounter) {
        set("footnote-counter", footnoteCounter);
        return this;
    }

    /**
     * The design tokens: the colors, spot colors, gradients, rulings, aliases and the rows of sizes the document names.
     */
    public DocumentAttr tokens(DocumentTokens tokens) {
        set("tokens", tokens);
        return this;
    }

    /** The fonts, images and color profiles the document declares. */
    public DocumentAttr assets(DocumentAssets assets) {
        set("assets", assets);
        return this;
    }

    /** The title, author and the rest of the PDF's metadata, and the language of the document. */
    public DocumentAttr meta(DocumentMeta meta) {
        set("meta", meta);
        return this;
    }

    /** How the PDF file is written: its encryption, what a reader may do, and the standards it meets. */
    public DocumentAttr output(DocumentOutput output) {
        set("output", output);
        return this;
    }

    /**
     * The files the PDF carries inside it, as the attachments element of the XML: a reader lists them in its
     * attachments panel. Each file's bytes are read from its src through the engine's srcFallback, or loaded with
     * loadFile under its ref or name.
     */
    public DocumentAttr attachments(List<FileAttr> attachments) {
        set("attachments", attachments == null ? null : List.copyOf(attachments));
        return this;
    }
}
