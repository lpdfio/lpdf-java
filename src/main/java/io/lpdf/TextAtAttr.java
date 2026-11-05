// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * Text at a fixed place on the page. It is the text of the layout, with the same defaults: the document's font or
 * Helvetica, 11pt and the text colour. It is one line unless told otherwise; give width to cut it off at a width or to
 * wrap it with max-lines. x and y place its box: the top-left corner, or with anchor, an offset from that point of the
 * page, where the box's matching point goes. {page} in it prints the page's number and {pages} how many pages its run
 * has, as its section's page-start, page-format and page-prefix number them, and a text that comes from data belongs in
 * the layout, since a longer text loses its end here without a warning.
 */
public final class TextAtAttr extends Attributes {
    /** Attributes with none set. */
    public TextAtAttr() {
        super("text-at");
    }

    public TextAtAttr x(String x) {
        set("x", x);
        return this;
    }

    public TextAtAttr y(String y) {
        set("y", y);
        return this;
    }

    public TextAtAttr anchor(String anchor) {
        set("anchor", anchor);
        return this;
    }

    public TextAtAttr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public TextAtAttr font(String font) {
        set("font", font);
        return this;
    }

    /**
     * Use the bold face of the font. It applies to the 14 built-in fonts: Helvetica becomes Helvetica-Bold, Times-Roman
     * becomes Times-Bold, Courier becomes Courier-Bold, and the oblique and italic faces their bold forms. A custom
     * font has no bold face to pick, so name one in font.
     */
    public TextAtAttr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public TextAtAttr color(String color) {
        set("color", color);
        return this;
    }

    public TextAtAttr align(String align) {
        set("align", align);
        return this;
    }

    /**
     * The distance between lines, as a factor of the font size: a number above 0, such as 1.5, or a name from the
     * line-height row of the tokens. Default 1.2, which is the token m.
     */
    public TextAtAttr lineHeight(String lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    /**
     * The distance between lines, as a factor of the font size: a number above 0, such as 1.5, or a name from the
     * line-height row of the tokens. Default 1.2, which is the token m.
     */
    public TextAtAttr lineHeight(double lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    /**
     * The most lines drawn. Only whole lines are drawn, and the rest of the text is dropped. A text in the layout has
     * no cap by default and wraps at the width it has; the lines it drops take no room, and a text that splits across
     * pages keeps the cap across them. A text-at is one line by default, and above 1 needs a width to wrap within.
     */
    public TextAtAttr maxLines(int maxLines) {
        set("max-lines", maxLines);
        return this;
    }

    /**
     * End the last line drawn in an ellipsis when max-lines drops text, shortened to fit the width. Off by default: the
     * text stops at the end of a line. A text-at also ends a line in one when it is cut off at its width. A text in the
     * layout without max-lines is never cut, so there it has no effect.
     */
    public TextAtAttr ellipsis(boolean ellipsis) {
        set("ellipsis", ellipsis);
        return this;
    }

    /**
     * Width of the box. Without it the text is one line of the width its characters need, and align has no effect; a
     * long text runs on past the edge of the page, which cuts it off, but stays in the PDF. With it, a line past the
     * width is cut off at it, or ends in an ellipsis with ellipsis, and max-lines above 1 wraps the text within it.
     */
    public TextAtAttr width(String width) {
        set("width", width);
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
    public TextAtAttr ocg(String ocg) {
        set("ocg", ocg);
        return this;
    }
}
