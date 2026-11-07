// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A thematic break, written --- in Markdown: a divider. color is the color of the line and thickness its thickness, a
 * length or a name from the border row of the tokens.
 */
public final class MarkdownHrAttr extends Attributes {
    /** Attributes with none set. */
    public MarkdownHrAttr() {
        super("hr");
    }

    public MarkdownHrAttr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownHrAttr thickness(String thickness) {
        set("thickness", thickness);
        return this;
    }
}
