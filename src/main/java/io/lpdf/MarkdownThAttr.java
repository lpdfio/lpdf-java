// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** A cell of a table's header row. */
public final class MarkdownThAttr extends Attributes {
    /** Attributes with none set. */
    public MarkdownThAttr() {
        super("th");
    }

    public MarkdownThAttr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownThAttr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public MarkdownThAttr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public MarkdownThAttr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownThAttr align(String align) {
        set("align", align);
        return this;
    }

    public MarkdownThAttr padding(String padding) {
        set("padding", padding);
        return this;
    }

    public MarkdownThAttr background(String background) {
        set("background", background);
        return this;
    }

    public MarkdownThAttr border(String border) {
        set("border", border);
        return this;
    }
}
