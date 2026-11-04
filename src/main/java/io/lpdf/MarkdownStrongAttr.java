// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** Strong text, written **text** in Markdown. Bold in a built-in font; an embedded font names its bold face in font. */
public final class MarkdownStrongAttr extends Attributes {
    /** Attributes with none set. */
    public MarkdownStrongAttr() {
        super("strong");
    }

    public MarkdownStrongAttr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownStrongAttr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownStrongAttr bold(boolean bold) {
        set("bold", bold);
        return this;
    }
}
