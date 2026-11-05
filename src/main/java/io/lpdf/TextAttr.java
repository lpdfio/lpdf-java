// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A block of wrapping text. Use span children to style parts of it, and fn children for footnotes. Splits across pages
 * between lines.
 */
public final class TextAttr extends Attributes {
    /** Attributes with none set. */
    public TextAttr() {
        super("text");
    }

    public TextAttr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    public TextAttr font(String font) {
        set("font", font);
        return this;
    }

    /**
     * Use the bold face of the font. It applies to the 14 built-in fonts: Helvetica becomes Helvetica-Bold, Times-Roman
     * becomes Times-Bold, Courier becomes Courier-Bold, and the oblique and italic faces their bold forms. A custom
     * font has no bold face to pick, so name one in font.
     */
    public TextAttr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public TextAttr color(String color) {
        set("color", color);
        return this;
    }

    public TextAttr align(String align) {
        set("align", align);
        return this;
    }

    /**
     * The distance between lines, as a factor of the font size: a number above 0, such as 1.5, or a name from the
     * line-height row of the tokens. Default 1.2, which is the token m.
     */
    public TextAttr lineHeight(String lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    /**
     * The distance between lines, as a factor of the font size: a number above 0, such as 1.5, or a name from the
     * line-height row of the tokens. Default 1.2, which is the token m.
     */
    public TextAttr lineHeight(double lineHeight) {
        set("line-height", lineHeight);
        return this;
    }

    /**
     * The most lines drawn. Only whole lines are drawn, and the rest of the text is dropped. A text in the layout has
     * no cap by default and wraps at the width it has; the lines it drops take no room, and a text that splits across
     * pages keeps the cap across them. A text-at is one line by default, and above 1 needs a width to wrap within.
     */
    public TextAttr maxLines(int maxLines) {
        set("max-lines", maxLines);
        return this;
    }

    /**
     * End the last line drawn in an ellipsis when max-lines drops text, shortened to fit the width. Off by default: the
     * text stops at the end of a line. A text-at also ends a line in one when it is cut off at its width. A text in the
     * layout without max-lines is never cut, so there it has no effect.
     */
    public TextAttr ellipsis(boolean ellipsis) {
        set("ellipsis", ellipsis);
        return this;
    }

    /**
     * Width of the element. A length fixes it. fill, in a row, takes the width its line leaves after the other
     * children, shared equally if several on the line use fill, and never moves to the next line; in a column it takes
     * the full width.
     */
    public TextAttr width(String width) {
        set("width", width);
        return this;
    }

    /**
     * Makes the text a heading, h1 to h6, which a tagged document, one with pdfua or a pdfa of level a, tags as one: a
     * screen reader lists the headings and moves between them. A text without role is a paragraph. A PDF/UA document's
     * headings start at h1 and go down one level at a time. It changes nothing a page shows: set the size and the
     * weight as on any text. A document that is not tagged ignores it.
     */
    public TextAttr role(String role) {
        set("role", role);
        return this;
    }

    /**
     * The fewest lines of the text that may stand alone at the top of the next page when it splits. Default 2. If fewer
     * would be left, lines move from the first page to the next; 1 turns the protection off.
     */
    public TextAttr widows(int widows) {
        set("widows", widows);
        return this;
    }

    /**
     * The fewest lines of the text that may stand alone at the bottom of a page when it splits. Default 2. If fewer
     * would fit, the whole text moves to the next page; 1 turns the protection off. When no split can keep both widows
     * and orphans, even on an empty page, the text splits where the lines fit.
     */
    public TextAttr orphans(int orphans) {
        set("orphans", orphans);
        return this;
    }

    /**
     * Records the words of this text under a name as the pages are filled, so a text with capture-ref in a header or a
     * footer can show them. Put it on a heading in the flow. The page that holds the text counts it, the words above it
     * on that page included, and a text that splits across pages is recorded on the page where it starts. A text cannot
     * have both capture and capture-ref.
     */
    public TextAttr capture(String capture) {
        set("capture", capture);
        return this;
    }

    /**
     * Shows what was captured under this name, as of the page the text is drawn on, in place of words of its own. Put
     * it in a region. It takes the lines it may use, one unless max-lines says more, whatever it shows, so a header or
     * a footer does not grow with its value; a longer value is cut, and ends in dots with ellipsis. On a page where
     * nothing was captured it shows the latest value of an earlier page of the section, and capture-fallback before
     * any.
     */
    public TextAttr captureRef(String captureRef) {
        set("capture-ref", captureRef);
        return this;
    }

    /**
     * Which of the texts captured under the name on this page the text shows: the first or the last. Default last. Only
     * for a text with capture-ref.
     */
    public TextAttr captureMode(String captureMode) {
        set("capture-mode", captureMode);
        return this;
    }

    /**
     * What the text shows until something has been captured in the section. Default: nothing. Only for a text with
     * capture-ref.
     */
    public TextAttr captureFallback(String captureFallback) {
        set("capture-fallback", captureFallback);
        return this;
    }

    public TextAttr paginate(String paginate) {
        set("paginate", paginate);
        return this;
    }

    public TextAttr debug(boolean debug) {
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
    public TextAttr ocg(String ocg) {
        set("ocg", ocg);
        return this;
    }
}
