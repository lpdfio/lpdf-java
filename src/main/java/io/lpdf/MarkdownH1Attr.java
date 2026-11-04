// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** A heading of level 1, written # in Markdown. */
public final class MarkdownH1Attr extends Attributes {
    /** Attributes with none set. */
    public MarkdownH1Attr() {
        super("h1");
    }

    public MarkdownH1Attr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownH1Attr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public MarkdownH1Attr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public MarkdownH1Attr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownH1Attr align(String align) {
        set("align", align);
        return this;
    }

    public MarkdownH1Attr lineHeight(String lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownH1Attr lineHeight(double lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownH1Attr padding(String padding) {
        set("padding", padding);
        return this;
    }

    public MarkdownH1Attr background(String background) {
        set("background", background);
        return this;
    }

    public MarkdownH1Attr border(String border) {
        set("border", border);
        return this;
    }

    public MarkdownH1Attr radius(String radius) {
        set("radius", radius);
        return this;
    }
}
