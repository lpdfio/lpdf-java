package io.lpdf;

/**
 * One stamp for {@link PdfEngine#stamp}: a PNG, the pages it goes on, and the box it is drawn in on each. The box is
 * placed as a layer element is, and the image is fitted in it without distortion. It needs <code>image</code>,
 * <code>page</code>, <code>width</code> and <code>height</code>.
 */
public final class StampAttr extends Attributes {
    /** A stamp with nothing set. */
    public StampAttr() {
        super("stamp", "image", "page", "width", "height");
    }

    /** The PNG's bytes. An array given to several stamps is embedded in the PDF once. */
    public StampAttr image(byte[] image) {
        set("image", image);
        return this;
    }

    /**
     * The pages: a page scope, such as <code>3</code>, <code>1-2</code>, <code>last</code>, <code>each</code>,
     * <code>odd</code> or <code>1,3,5-last</code>. A page the PDF doesn't have is an error.
     */
    public StampAttr page(String page) {
        set("page", page);
        return this;
    }

    /**
     * Where the box goes across, in <code>pt</code>, <code>mm</code> or <code>in</code>: from the page's left edge as a
     * viewer shows it, or with an anchor, a signed offset from that point of the page. Required without an anchor.
     */
    public StampAttr x(String x) {
        set("x", x);
        return this;
    }

    /**
     * Where the box goes down, in <code>pt</code>, <code>mm</code> or <code>in</code>: from the page's top edge as a
     * viewer shows it, or with an anchor, a signed offset from that point of the page. Required without an anchor.
     */
    public StampAttr y(String y) {
        set("y", y);
        return this;
    }

    /**
     * The point of the page the box is placed from, and the point of the box that goes there: <code>top-left</code>,
     * <code>top-center</code>, <code>top-right</code>, <code>center-left</code>, <code>center</code>,
     * <code>center-right</code>, <code>bottom-left</code>, <code>bottom-center</code> or <code>bottom-right</code>.
     */
    public StampAttr anchor(String anchor) {
        set("anchor", anchor);
        return this;
    }

    /** The width of the box, in <code>pt</code>, <code>mm</code> or <code>in</code>. */
    public StampAttr width(String width) {
        set("width", width);
        return this;
    }

    /** The height of the box, in <code>pt</code>, <code>mm</code> or <code>in</code>. */
    public StampAttr height(String height) {
        set("height", height);
        return this;
    }

    /** Where the image sits across the box, in the room it leaves: <code>start</code>, <code>center</code> (the default) or <code>end</code>. */
    public StampAttr align(String align) {
        set("align", align);
        return this;
    }

    /** Where the image sits down the box, in the room it leaves: <code>start</code>, <code>center</code> (the default) or <code>end</code>. */
    public StampAttr justify(String justify) {
        set("justify", justify);
        return this;
    }

    /**
     * How the image is sized in the box, keeping its proportions: <code>fit</code> (the default) shrinks an image larger
     * than the box and keeps a smaller one at its natural size, from the resolution the PNG records, or a pixel to a
     * point; <code>fill</code> takes the largest size that fits.
     */
    public StampAttr scale(String scale) {
        set("scale", scale);
        return this;
    }

    /** How many times its natural size <code>fit</code> may grow an image, never past the box: 1, the default, or more. */
    public StampAttr maxGrow(double maxGrow) {
        set("max-grow", maxGrow);
        return this;
    }
}
