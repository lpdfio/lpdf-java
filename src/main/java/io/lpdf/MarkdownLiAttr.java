// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * An item of a bullet or a numbered list. indent is the width of the marker's column and bullet the shape of a bullet
 * list's marker, as on a list; gap goes between the blocks of an item.
 */
public final class MarkdownLiAttr extends Attributes {
    /** Attributes with none set. */
    public MarkdownLiAttr() {
        super("li");
    }

    public MarkdownLiAttr font(String font) {
        set("font", font);
        return this;
    }

    public MarkdownLiAttr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public MarkdownLiAttr color(String color) {
        set("color", color);
        return this;
    }

    public MarkdownLiAttr gap(String gap) {
        set("gap", gap);
        return this;
    }

    public MarkdownLiAttr padding(String padding) {
        set("padding", padding);
        return this;
    }

    public MarkdownLiAttr background(String background) {
        set("background", background);
        return this;
    }

    public MarkdownLiAttr border(String border) {
        set("border", border);
        return this;
    }

    public MarkdownLiAttr indent(String indent) {
        set("indent", indent);
        return this;
    }

    public MarkdownLiAttr bullet(String bullet) {
        set("bullet", bullet);
        return this;
    }
}
