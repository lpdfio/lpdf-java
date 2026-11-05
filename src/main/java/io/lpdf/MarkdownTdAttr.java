// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** A cell of a table's other rows. */
public final class MarkdownTdAttr extends Attributes {
    /** Attributes with none set. */
    public MarkdownTdAttr() {
        super("td");
    }

    public MarkdownTdAttr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownTdAttr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public MarkdownTdAttr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public MarkdownTdAttr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownTdAttr align(String align) {
        set("align", align);
        return this;
    }

    public MarkdownTdAttr padding(String padding) {
        set("padding", padding);
        return this;
    }

    public MarkdownTdAttr background(String background) {
        set("background", background);
        return this;
    }

    public MarkdownTdAttr border(String border) {
        set("border", border);
        return this;
    }
}
