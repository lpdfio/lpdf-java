// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** A heading of level 2, written ## in Markdown. */
public final class MarkdownH2Attr extends Attributes {
    /** Attributes with none set. */
    public MarkdownH2Attr() {
        super("h2");
    }

    public MarkdownH2Attr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownH2Attr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public MarkdownH2Attr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public MarkdownH2Attr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownH2Attr align(String align) {
        set("align", align);
        return this;
    }

    public MarkdownH2Attr lineHeight(String lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownH2Attr lineHeight(double lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownH2Attr padding(String padding) {
        set("padding", padding);
        return this;
    }

    public MarkdownH2Attr background(String background) {
        set("background", background);
        return this;
    }

    public MarkdownH2Attr border(String border) {
        set("border", border);
        return this;
    }

    public MarkdownH2Attr radius(String radius) {
        set("radius", radius);
        return this;
    }
}
