// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * Code, or any text whose spaces and lines matter, in a box. The text is kept as it is written: every space, and a new
 * line wherever one is written, except the line break after the start tag and the indent of the line the end tag stands
 * on. A line longer than the box wraps at a space, and a word with none breaks at the last character that fits, so
 * nothing is cut off. A tab fills to the next multiple of tab-size columns. The box fills the width and grows with its
 * lines, in Courier on a light tint with padding m unless font, background and padding say otherwise. It holds text and
 * span children, which color or style parts of it. It splits across pages between lines, and each part is drawn in its
 * own box. A code element is a column around one text, so the text takes its font, size and color from font, font-size
 * and color.
 */
public final class CodeAttr extends Attributes {
    /** Attributes with none set. */
    public CodeAttr() {
        super("code");
    }

    public CodeAttr font(String font) {
        set("font", font);
        return this;
    }

    public CodeAttr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    /**
     * The color of the text inside the box. A text that sets no color of its own takes the nearest box's, and the text
     * token when no box sets one. A divider or a barcode in the box keeps its own color.
     */
    public CodeAttr color(String color) {
        set("color", color);
        return this;
    }

    /**
     * Space between the edge of the box and its content, written like CSS: one value for all sides, two for top-bottom
     * and left-right, three for top, left-right and bottom, four for top, right, bottom and left. Each value is a
     * length or a name from the space row of the tokens, and they can be mixed: 0pt 0pt 0pt m.
     */
    public CodeAttr padding(String padding) {
        set("padding", padding);
        return this;
    }

    /**
     * Height of the box. Leave it out to size to the content. A length, or a name from the height row of the tokens,
     * fixes it; fill takes what is left after its siblings (shared equally if several use fill); full takes all the
     * height available. Any of these stops the box splitting across pages.
     */
    public CodeAttr height(String height) {
        set("height", height);
        return this;
    }

    public CodeAttr background(String background) {
        set("background", background);
        return this;
    }

    public CodeAttr border(String border) {
        set("border", border);
        return this;
    }

    public CodeAttr radius(String radius) {
        set("radius", radius);
        return this;
    }

    /**
     * How opaque the box and everything in it is, from 0 to 1. The box is faded as one, so children that overlap do not
     * show through each other, and a box inside a faded box is faded further: 0.5 inside 0.5 is drawn at 0.25. A form
     * field in the box is not faded. Default 1.
     */
    public CodeAttr opacity(double opacity) {
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
    public CodeAttr shadow(String shadow) {
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
    public CodeAttr paginate(String paginate) {
        set("paginate", paginate);
        return this;
    }

    public CodeAttr debug(boolean debug) {
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
    public CodeAttr ocg(String ocg) {
        set("ocg", ocg);
        return this;
    }

    /**
     * Width of the element. A length fixes it. fill, in a row, takes the width its line leaves after the other
     * children, shared equally if several on the line use fill, and never moves to the next line; in a column it takes
     * the full width.
     */
    public CodeAttr width(String width) {
        set("width", width);
        return this;
    }

    /**
     * How many columns a tab fills to: it moves the next character to the next multiple of this many characters from
     * the start of the line. Default 4.
     */
    public CodeAttr tabSize(int tabSize) {
        set("tab-size", tabSize);
        return this;
    }

    /**
     * The distance between lines, as a factor of the font size: a number above 0, such as 1.5, or a name from the
     * line-height row of the tokens. Default 1.2, which is the token m.
     */
    public CodeAttr lineHeight(String lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    /**
     * The distance between lines, as a factor of the font size: a number above 0, such as 1.5, or a name from the
     * line-height row of the tokens. Default 1.2, which is the token m.
     */
    public CodeAttr lineHeight(double lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    /**
     * The fewest lines of the code that may stand alone at the top of the next page when it splits. Default 2. If fewer
     * would be left, lines move from the first page to the next; 1 turns the protection off.
     */
    public CodeAttr widows(int widows) {
        set("widows", widows);
        return this;
    }

    /**
     * The fewest lines of the code that may stand alone at the bottom of a page when it splits. Default 2. If fewer
     * would fit, the whole code moves to the next page; 1 turns the protection off.
     */
    public CodeAttr orphans(int orphans) {
        set("orphans", orphans);
        return this;
    }
}
