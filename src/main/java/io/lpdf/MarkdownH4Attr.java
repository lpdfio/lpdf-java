// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** A heading of level 4, written #### in Markdown. */
public final class MarkdownH4Attr extends Attributes {
    /** Attributes with none set. */
    public MarkdownH4Attr() {
        super("h4");
    }

    public MarkdownH4Attr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownH4Attr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public MarkdownH4Attr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public MarkdownH4Attr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownH4Attr align(String align) {
        set("align", align);
        return this;
    }

    public MarkdownH4Attr lineHeight(String lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownH4Attr lineHeight(double lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownH4Attr padding(String padding) {
        set("padding", padding);
        return this;
    }

    public MarkdownH4Attr background(String background) {
        set("background", background);
        return this;
    }

    public MarkdownH4Attr border(String border) {
        set("border", border);
        return this;
    }

    public MarkdownH4Attr radius(String radius) {
        set("radius", radius);
        return this;
    }
}
