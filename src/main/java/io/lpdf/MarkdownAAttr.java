// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** A link, written [text](url) in Markdown. */
public final class MarkdownAAttr extends Attributes {
    /** Attributes with none set. */
    public MarkdownAAttr() {
        super("a");
    }

    public MarkdownAAttr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownAAttr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownAAttr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public MarkdownAAttr underline(boolean underline) {
        set("underline", underline);
        return this;
    }
}
