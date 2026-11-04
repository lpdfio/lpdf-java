// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** Attributes of the <code>rect</code> element in a layer. */
public final class RectAttr extends Attributes {
    /** Attributes with none set. */
    public RectAttr() {
        super("rect", "width", "height");
    }

    public RectAttr width(String width) {
        set("width", width);
        return this;
    }

    public RectAttr height(String height) {
        set("height", height);
        return this;
    }

    public RectAttr x(String x) {
        set("x", x);
        return this;
    }

    public RectAttr y(String y) {
        set("y", y);
        return this;
    }

    public RectAttr anchor(String anchor) {
        set("anchor", anchor);
        return this;
    }

    public RectAttr radius(String radius) {
        set("radius", radius);
        return this;
    }

    public RectAttr fill(String fill) {
        set("fill", fill);
        return this;
    }

    public RectAttr stroke(String stroke) {
        set("stroke", stroke);
        return this;
    }

    public RectAttr strokeWidth(String strokeWidth) {
        set("stroke-width", strokeWidth);
        return this;
    }

    public RectAttr strokeDash(String strokeDash) {
        set("stroke-dash", strokeDash);
        return this;
    }

    /**
     * A hard-edged shadow: a copy of the shape, moved right and down by the first two lengths (negative is left and up)
     * and filled with the color, drawn under the shape. An opacity from 0 to 1 after the color fades the shadow alone:
     * "2pt 3pt #000000 0.2". There is no blur. A shape with no fill shows the shadow only outside it.
     */
    public RectAttr shadow(String shadow) {
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
    public RectAttr ocg(String ocg) {
        set("ocg", ocg);
        return this;
    }
}
