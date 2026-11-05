// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** Attributes of the <code>span</code> element. */
public final class SpanAttr extends Attributes {
    /** Attributes with none set. */
    public SpanAttr() {
        super("span");
    }

    public SpanAttr font(String font) {
        set("font", font);
        return this;
    }

    public SpanAttr bold(boolean bold) {
        set("bold", bold);
        return this;
    }

    public SpanAttr color(String color) {
        set("color", color);
        return this;
    }

    /**
     * Makes the span a link: a URL, or #id to jump to the bookmark with that id. In an entry of a table of contents, #
     * alone jumps to that entry's bookmark.
     */
    public SpanAttr href(String href) {
        set("href", href);
        return this;
    }

    /**
     * Makes the span a cross-reference to the bookmark with this id: a link to it whose words say where it is. In the
     * span's words, {bookmark-page} prints the page the bookmark is on, as the footer of that page prints it, and
     * {bookmark-label} the bookmark's label, so "{bookmark-label}, on page {bookmark-page}" shows "clause 4, on page
     * 12". An id that no bookmark has, another {…} token in the words, and href beside it are errors. It works in a
     * text, a footnote, a region, a text-at and a code. The document is laid out again until each reference shows the
     * page its bookmark lands on, and a reference keeps room for the widest words it has shown, so a number that got
     * narrower can leave a small gap after it.
     */
    public SpanAttr bookmarkRef(String bookmarkRef) {
        set("bookmark-ref", bookmarkRef);
        return this;
    }

    public SpanAttr underline(boolean underline) {
        set("underline", underline);
        return this;
    }

    public SpanAttr strike(boolean strike) {
        set("strike", strike);
        return this;
    }
}
