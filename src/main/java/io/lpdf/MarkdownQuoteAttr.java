// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A block quote, written &gt; in Markdown: a bar beside its content. font, font-size, color, line-height and align set
 * the words; padding, background, border and radius the box around the bar and the words; bar-color and bar-width the
 * bar, a strip along the left of the box: bar-width is a length or a name from the space row of the tokens.
 */
public final class MarkdownQuoteAttr extends Attributes {
    /** Attributes with none set. */
    public MarkdownQuoteAttr() {
        super("quote");
    }

    public MarkdownQuoteAttr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownQuoteAttr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public MarkdownQuoteAttr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public MarkdownQuoteAttr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownQuoteAttr align(String align) {
        set("align", align);
        return this;
    }

    public MarkdownQuoteAttr lineHeight(String lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownQuoteAttr lineHeight(double lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    public MarkdownQuoteAttr padding(String padding) {
        set("padding", padding);
        return this;
    }

    public MarkdownQuoteAttr background(String background) {
        set("background", background);
        return this;
    }

    public MarkdownQuoteAttr border(String border) {
        set("border", border);
        return this;
    }

    public MarkdownQuoteAttr radius(String radius) {
        set("radius", radius);
        return this;
    }

    public MarkdownQuoteAttr barColor(String barColor) {
        set("bar-color", barColor);
        return this;
    }

    public MarkdownQuoteAttr barWidth(String barWidth) {
        set("bar-width", barWidth);
        return this;
    }
}
