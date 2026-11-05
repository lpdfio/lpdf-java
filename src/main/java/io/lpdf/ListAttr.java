// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A list of items, each with a marker: a bullet drawn as a shape, or a number the engine gives. The items go top to
 * bottom with gap between them, each one a li, and a list nests inside an item. The words of an item start indent from
 * the list's left edge, at every level. A long item splits across pages through the line the page ends in, and its
 * marker stays with the first part.
 */
public final class ListAttr extends Attributes {
    /** Attributes with none set. */
    public ListAttr() {
        super("list");
    }

    public ListAttr font(String font) {
        set("font", font);
        return this;
    }

    public ListAttr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    /**
     * The color of the text inside the box. A text that sets no color of its own takes the nearest box's, and the text
     * token when no box sets one. A divider or a barcode in the box keeps its own color.
     */
    public ListAttr color(String color) {
        set("color", color);
        return this;
    }

    public ListAttr gap(String gap) {
        set("gap", gap);
        return this;
    }

    /**
     * Space between the edge of the box and its content, written like CSS: one value for all sides, two for top-bottom
     * and left-right, three for top, left-right and bottom, four for top, right, bottom and left. Each value is a
     * length or a name from the space row of the tokens, and they can be mixed: 0pt 0pt 0pt m.
     */
    public ListAttr padding(String padding) {
        set("padding", padding);
        return this;
    }

    /**
     * Height of the box. Leave it out to size to the content. A length, or a name from the height row of the tokens,
     * fixes it; fill takes what is left after its siblings (shared equally if several use fill); full takes all the
     * height available. Any of these stops the box splitting across pages.
     */
    public ListAttr height(String height) {
        set("height", height);
        return this;
    }

    public ListAttr background(String background) {
        set("background", background);
        return this;
    }

    public ListAttr border(String border) {
        set("border", border);
        return this;
    }

    public ListAttr radius(String radius) {
        set("radius", radius);
        return this;
    }

    /**
     * How opaque the box and everything in it is, from 0 to 1. The box is faded as one, so children that overlap do not
     * show through each other, and a box inside a faded box is faded further: 0.5 inside 0.5 is drawn at 0.25. A form
     * field in the box is not faded. Default 1.
     */
    public ListAttr opacity(double opacity) {
        set("opacity", opacity);
        return this;
    }

    /**
     * A hard-edged shadow: a copy of the box, moved right and down by the first two lengths (negative is left and up)
     * and filled with the color, drawn under the box with the same radius. An opacity from 0 to 1 after the color fades
     * the shadow alone: "2pt 3pt #000000 0.2". There is no blur. The shadow takes no room in the layout, so it can run
     * into the margin or a neighbor, and the siblings after the box are drawn over it. A box with no background shows
     * the shadow only outside it. The box's own opacity fades the box and its shadow together.
     */
    public ListAttr shadow(String shadow) {
        set("shadow", shadow);
        return this;
    }

    /**
     * Where the box falls in the page flow. no: never split the box, and move it whole to the next page when it does
     * not fit. break-before: start it on a new page. break-after: start the next sibling on a new page. keep-next: keep
     * it on one page with the sibling that follows, moving both to the next page if that sibling would not fit and
     * cannot start on this page: a table splits after its header and a row, and a text after a few lines, and then it
     * stays with that first part. break-before, break-after and keep-next take effect on the children of layout; on a
     * box inside another box they are not applied. no applies at any depth.
     */
    public ListAttr paginate(String paginate) {
        set("paginate", paginate);
        return this;
    }

    public ListAttr debug(boolean debug) {
        set("debug", debug);
        return this;
    }

    /**
     * Puts the element, and everything in it, in an optional content group, which the viewer shows or hides. print:
     * printed, and not shown on screen. screen: shown on screen, and not printed. The element keeps its room either
     * way, so nothing moves. An element with a group inside an element with another shows only where both show.
     * Acrobat, Chrome and Firefox switch the groups; Preview and Safari show everything, on screen and in print. Hidden
     * content is still in the file, where it can be selected and copied: it is not a way to keep anything secret. A
     * link in print content is drawn and does nothing, and a form field can't be in print content: use a frame with a
     * border for a box filled in by hand. With pdfa or pdfua on the output, everything shows. With pdfx, the file is
     * the print version: screen content stays in it, off, and a reader can turn it on in a layers panel.
     */
    public ListAttr ocg(String ocg) {
        set("ocg", ocg);
        return this;
    }

    /** bullet marks each item with a shape, number with a number. Default bullet. */
    public ListAttr type(String type) {
        set("type", type);
        return this;
    }

    /**
     * How a number list writes its numbers: decimal (1, 2, 3), lower-alpha (a, b, c, then aa, ab), upper-alpha (A, B,
     * C), lower-roman (i, ii, iii) or upper-roman (I, II, III). A number that a letter or a numeral can't write, below
     * 1 or past 3999 for a numeral, is written in decimal. Default decimal.
     */
    public ListAttr format(String format) {
        set("format", format);
        return this;
    }

    /** The number of a number list's first item, to continue a list that something cut in two. Default 1. */
    public ListAttr start(int start) {
        set("start", start);
        return this;
    }

    /** Text before each number of a number list: prefix="(" and suffix=")" write (a). Default none. */
    public ListAttr prefix(String prefix) {
        set("prefix", prefix);
        return this;
    }

    /** Text after each number of a number list: suffix=")" writes a). Default a full stop, so a number is written 1. */
    public ListAttr suffix(String suffix) {
        set("suffix", suffix);
        return this;
    }

    /**
     * The shape that marks the items of a bullet list. Without it a list uses a disc, a list inside a bullet list a
     * circle, and one inside that a square, then the three again.
     */
    public ListAttr bullet(String bullet) {
        set("bullet", bullet);
        return this;
    }

    /**
     * The width of the marker's column: the words of an item start this far from the list's left edge, and a list
     * inside an item starts there too. The marker is set at the right of its column, so 9. and 10. end together, and a
     * marker wider than the column is not made to fit. The same length at every level. Default 24pt.
     */
    public ListAttr indent(String indent) {
        set("indent", indent);
        return this;
    }

    /**
     * Width of the element. A length fixes it. fill, in a row, takes the width its line leaves after the other
     * children, shared equally if several on the line use fill, and never moves to the next line; in a column it takes
     * the full width.
     */
    public ListAttr width(String width) {
        set("width", width);
        return this;
    }

    /**
     * true lays out the items that the li with data-source repeats one page at a time, straight from the data, for logs
     * and lists of tens of thousands of items and more. The pages are the ones the same list would have without bulk,
     * but where a page would end inside an item, between its texts or inside a text of four lines or more: without bulk
     * the item splits there, and with bulk it moves whole to the next page. A number list numbers the items as it does
     * without bulk. The list has one level, and its items follow rules: an li holds texts only, a text holds plain
     * words with no span, link, bookmark, footnote or reference, and sets no widows or orphans, a text without
     * max-lines holds one line and ends in an ellipsis, and nothing in an item has data-source. The list sits directly
     * in the layout, has no data-source of its own and one li with it, has no height, and neither it nor anything in it
     * has paginate. Its document has no table of contents, no reference to a bookmark, no pdfua and no pdfa of level a,
     * and its section's columns are not balanced. Breaking a rule is an error that names it. Default false.
     */
    public ListAttr bulk(boolean bulk) {
        set("bulk", bulk);
        return this;
    }
}
