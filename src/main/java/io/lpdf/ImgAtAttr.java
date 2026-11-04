// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * An image at a fixed place on the page. It needs width or height, and the other follows the image's proportions. With
 * both, they are a maximum: the image takes the largest size that fits in them, and its box becomes that size, so an
 * anchor places the image itself. x and y place its box: the top-left corner, or with anchor, an offset from that point
 * of the page, where the box's matching point goes.
 */
public final class ImgAtAttr extends Attributes {
    /** Attributes with none set. */
    public ImgAtAttr() {
        super("img-at", "name");
    }

    public ImgAtAttr name(String name) {
        set("name", name);
        return this;
    }

    public ImgAtAttr x(String x) {
        set("x", x);
        return this;
    }

    public ImgAtAttr y(String y) {
        set("y", y);
        return this;
    }

    public ImgAtAttr anchor(String anchor) {
        set("anchor", anchor);
        return this;
    }

    public ImgAtAttr width(String width) {
        set("width", width);
        return this;
    }

    public ImgAtAttr height(String height) {
        set("height", height);
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
    public ImgAtAttr ocg(String ocg) {
        set("ocg", ocg);
        return this;
    }
}
