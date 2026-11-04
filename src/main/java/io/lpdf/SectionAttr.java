package io.lpdf;

/**
 * A run of pages with its own settings, which override the document's. Each section paginates on its own, and its pages
 * count on from the section before unless it sets page-start. A bookmark as its first child marks its first page. It
 * holds layers and at most one layout or one markdown, painted in the order they are written: layers before the layout
 * paint under it, and layers after it paint over it. A section with neither is one page. Its title is accepted but not
 * used: the outline takes its entries from bookmarks.
 */
public final class SectionAttr extends Attributes {
    /** Attributes with none set. */
    public SectionAttr() {
        super("section");
    }

    public SectionAttr size(String size) {
        set("size", size);
        return this;
    }

    public SectionAttr orientation(String orientation) {
        set("orientation", orientation);
        return this;
    }

    /**
     * How far this section's pages reach past their trim edge on every side. Overrides the document's bleed; 0mm takes
     * it away.
     */
    public SectionAttr bleed(String bleed) {
        set("bleed", bleed);
        return this;
    }

    public SectionAttr background(String background) {
        set("background", background);
        return this;
    }

    /**
     * Draws the lines of a ruling of the tokens on every page of the section, under its layers and its layout, and over
     * its background: the name of a ruling in rulings. A name the tokens don't declare is an error that lists the ones
     * they do. Without it the section has no lines.
     */
    public SectionAttr ruling(String ruling) {
        set("ruling", ruling);
        return this;
    }

    public SectionAttr title(String title) {
        set("title", title);
        return this;
    }

    public SectionAttr debug(boolean debug) {
        set("debug", debug);
        return this;
    }

    /**
     * toc makes the section the document's table of contents. It is laid out after the other sections, with the data
     * $toc: one entry per bookmark outside it, with index, level, label, page (the number its page prints, as a text),
     * anchor and indent. Repeat an element over it with data-source="$toc". One section per document may be a table of
     * contents.
     */
    public SectionAttr type(String type) {
        set("type", type);
        return this;
    }

    /**
     * Where the numbers of the footnotes in this section start over: on every page, in this section (the default), or
     * never, so that they run on from the earlier sections that number across the document. Overrides the document's.
     */
    public SectionAttr footnoteCounter(String footnoteCounter) {
        set("footnote-counter", footnoteCounter);
        return this;
    }

    /** In a table of contents, the left padding an entry gets for each level below the first. Defaults to 12pt. */
    public SectionAttr tocIndentStep(String tocIndentStep) {
        set("toc-indent-step", tocIndentStep);
        return this;
    }

    /**
     * The number of the section's first page, which starts a new run of pages: page-start="1" restarts the count, and a
     * number above 1 continues one from outside the file, such as volume 2 from page 321. Without it the pages count on
     * from the section before, and the first section counts from 1. {page} prints the page's number and {pages} how
     * many pages the run has, up to the next section that sets page-start. The table of contents and the viewer's page
     * box show the same numbers.
     */
    public SectionAttr pageStart(int pageStart) {
        set("page-start", pageStart);
        return this;
    }

    /**
     * How the section's page numbers are written: decimal (1, 2, 3), lower-alpha (a, b, c), upper-alpha (A, B, C),
     * lower-roman (i, ii, iii) or upper-roman (I, II, III). Past z, letters repeat as a PDF viewer counts pages: aa,
     * bb, cc, where a list's go aa, ab. A number past 3999 is written in decimal. Without it a section keeps the format
     * of the section before; the first is decimal. {pages} is written in it too. Changing the format doesn't restart
     * the count: page-start does.
     */
    public SectionAttr pageFormat(String pageFormat) {
        set("page-format", pageFormat);
        return this;
    }

    /**
     * Text before the section's page numbers: page-prefix="A-" numbers them A-1, A-2. {page} prints it and {pages} does
     * not. Without it a section keeps the prefix of the section before; page-prefix="" takes it away.
     */
    public SectionAttr pagePrefix(String pagePrefix) {
        set("page-prefix", pagePrefix);
        return this;
    }

    /**
     * Sets the section's flow in this many columns, for a layout and a markdown alike. Each column fills like a small
     * page, left to right, and what is left after the last one goes on to the next page. Headers, footers, footnotes
     * and layers keep the full width of the page. Missing or 1 is one column, as without it.
     */
    public SectionAttr columns(int columns) {
        set("columns", columns);
        return this;
    }

    /**
     * The space between two columns, as gap takes it: a space token or a length. Defaults to l. Ignored when columns is
     * missing or 1.
     */
    public SectionAttr columnGap(String columnGap) {
        set("column-gap", columnGap);
        return this;
    }

    /**
     * A line in the middle of the gap between two columns, as border takes it: a thickness and a color, such as 0.5pt
     * #ccc. It runs from the top of the columns to the foot of the tallest on the page, only between columns that hold
     * something, and skips an inset that covers its gap. Ignored when columns is missing or 1.
     */
    public SectionAttr columnRule(String columnRule) {
        set("column-rule", columnRule);
        return this;
    }

    /**
     * How the columns of the last page fill. auto, the default, fills them in order as on every other page, so the last
     * can be short or empty; balance levels them as evenly as the content allows. CSS's default is balance. Ignored
     * when columns is missing or 1.
     */
    public SectionAttr columnFill(String columnFill) {
        set("column-fill", columnFill);
        return this;
    }
}
