// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A table. Its columns are sized by their content: a column takes the width its cells need on one line, the room that
 * is left is shared in proportion, and a name that does not fit breaks at the edge of its cell. stripe is the color of
 * every other row, or none; head-border is the line under the header row and row-border the line under each of the
 * other rows, as thickness and color like border, or none. The default styles draw a line in line-muted under the
 * header and under each row, with no stripe.
 */
public final class MarkdownTableAttr extends Attributes {
    /** Attributes with none set. */
    public MarkdownTableAttr() {
        super("table");
    }

    public MarkdownTableAttr border(String border) {
        set("border", border);
        return this;
    }

    public MarkdownTableAttr stripe(String stripe) {
        set("stripe", stripe);
        return this;
    }

    public MarkdownTableAttr headBorder(String headBorder) {
        set("head-border", headBorder);
        return this;
    }

    public MarkdownTableAttr rowBorder(String rowBorder) {
        set("row-border", rowBorder);
        return this;
    }

    public MarkdownTableAttr gap(String gap) {
        set("gap", gap);
        return this;
    }

    public MarkdownTableAttr padding(String padding) {
        set("padding", padding);
        return this;
    }

    public MarkdownTableAttr background(String background) {
        set("background", background);
        return this;
    }
}
