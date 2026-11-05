// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** Emphasis, written *text* in Markdown. Italic in a built-in font; an embedded font names its italic face in font. */
public final class MarkdownEmAttr extends Attributes {
    /** Attributes with none set. */
    public MarkdownEmAttr() {
        super("em");
    }

    public MarkdownEmAttr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownEmAttr color(String color) {
        set("color", color);
        return this;
    }
}
