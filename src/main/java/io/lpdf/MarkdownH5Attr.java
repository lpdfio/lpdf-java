// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** A heading of level 5, written ##### in Markdown. */
public final class MarkdownH5Attr extends Attributes {
    /** Attributes with none set. */
    public MarkdownH5Attr() {
        super("h5");
    }

    public MarkdownH5Attr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownH5Attr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public MarkdownH5Attr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public MarkdownH5Attr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownH5Attr align(String align) {
        set("align", align);
        return this;
    }

    public MarkdownH5Attr lineHeight(String lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownH5Attr lineHeight(double lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownH5Attr padding(String padding) {
        set("padding", padding);
        return this;
    }

    public MarkdownH5Attr background(String background) {
        set("background", background);
        return this;
    }

    public MarkdownH5Attr border(String border) {
        set("border", border);
        return this;
    }

    public MarkdownH5Attr radius(String radius) {
        set("radius", radius);
        return this;
    }
}
