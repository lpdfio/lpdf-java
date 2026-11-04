// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * Code: the box of a fenced or indented block, and the run of inline code, which takes the font, the color and the
 * background. The language of a fence is ignored, since there is no highlighting by language.
 */
public final class MarkdownCodeAttr extends Attributes {
    /** Attributes with none set. */
    public MarkdownCodeAttr() {
        super("code");
    }

    public MarkdownCodeAttr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownCodeAttr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public MarkdownCodeAttr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownCodeAttr lineHeight(String lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownCodeAttr lineHeight(double lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownCodeAttr padding(String padding) {
        set("padding", padding);
        return this;
    }

    public MarkdownCodeAttr background(String background) {
        set("background", background);
        return this;
    }

    public MarkdownCodeAttr border(String border) {
        set("border", border);
        return this;
    }

    public MarkdownCodeAttr radius(String radius) {
        set("radius", radius);
        return this;
    }

    public MarkdownCodeAttr tabSize(int tabSize) {
        set("tab-size", tabSize);
        return this;
    }
}
