// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** A paragraph, and the words of a list item, a quote and a footnote. */
public final class MarkdownPAttr extends Attributes {
    /** Attributes with none set. */
    public MarkdownPAttr() {
        super("p");
    }

    public MarkdownPAttr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownPAttr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public MarkdownPAttr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public MarkdownPAttr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownPAttr align(String align) {
        set("align", align);
        return this;
    }

    public MarkdownPAttr lineHeight(String lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownPAttr lineHeight(double lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownPAttr padding(String padding) {
        set("padding", padding);
        return this;
    }

    public MarkdownPAttr background(String background) {
        set("background", background);
        return this;
    }

    public MarkdownPAttr border(String border) {
        set("border", border);
        return this;
    }

    public MarkdownPAttr radius(String radius) {
        set("radius", radius);
        return this;
    }
}
