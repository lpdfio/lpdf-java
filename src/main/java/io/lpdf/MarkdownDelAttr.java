// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** Struck-through text, written ~~text~~ in Markdown. */
public final class MarkdownDelAttr extends Attributes {
    /** Attributes with none set. */
    public MarkdownDelAttr() {
        super("del");
    }

    public MarkdownDelAttr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownDelAttr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownDelAttr strike(boolean strike) {
        set("strike", strike);
        return this;
    }
}
