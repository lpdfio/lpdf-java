// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** Attributes of the <code>circle</code> element in a layer. */
public final class CircleAttr extends Attributes {
    /** Attributes with none set. */
    public CircleAttr() {
        super("circle", "r");
    }

    public CircleAttr r(String r) {
        set("r", r);
        return this;
    }

    /**
     * Where the circle's box goes, the square of side 2r around it: its left edge, or with anchor, an offset from that
     * point of the page, where the box's matching point goes. The centre is r further in.
     */
    public CircleAttr x(String x) {
        set("x", x);
        return this;
    }

    /**
     * Where the circle's box goes, the square of side 2r around it: its top edge, or with anchor, an offset from that
     * point of the page, where the box's matching point goes. The centre is r further in.
     */
    public CircleAttr y(String y) {
        set("y", y);
        return this;
    }

    public CircleAttr anchor(String anchor) {
        set("anchor", anchor);
        return this;
    }

    public CircleAttr fill(String fill) {
        set("fill", fill);
        return this;
    }

    public CircleAttr stroke(String stroke) {
        set("stroke", stroke);
        return this;
    }

    public CircleAttr strokeWidth(String strokeWidth) {
        set("stroke-width", strokeWidth);
        return this;
    }

    public CircleAttr strokeDash(String strokeDash) {
        set("stroke-dash", strokeDash);
        return this;
    }

    /**
     * A hard-edged shadow: a copy of the shape, moved right and down by the first two lengths (negative is left and up)
     * and filled with the color, drawn under the shape. An opacity from 0 to 1 after the color fades the shadow alone:
     * "2pt 3pt #000000 0.2". There is no blur. A shape with no fill shows the shadow only outside it.
     */
    public CircleAttr shadow(String shadow) {
        set("shadow", shadow);
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
    public CircleAttr ocg(String ocg) {
        set("ocg", ocg);
        return this;
    }
}
