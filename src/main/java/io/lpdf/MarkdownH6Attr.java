// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** A heading of level 6, written ###### in Markdown. */
public final class MarkdownH6Attr extends Attributes {
    /** Attributes with none set. */
    public MarkdownH6Attr() {
        super("h6");
    }

    public MarkdownH6Attr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownH6Attr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public MarkdownH6Attr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public MarkdownH6Attr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownH6Attr align(String align) {
        set("align", align);
        return this;
    }

    public MarkdownH6Attr lineHeight(String lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownH6Attr lineHeight(double lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownH6Attr padding(String padding) {
        set("padding", padding);
        return this;
    }

    public MarkdownH6Attr background(String background) {
        set("background", background);
        return this;
    }

    public MarkdownH6Attr border(String border) {
        set("border", border);
        return this;
    }

    public MarkdownH6Attr radius(String radius) {
        set("radius", radius);
        return this;
    }
}
