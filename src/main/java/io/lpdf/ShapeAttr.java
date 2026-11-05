// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A rectangle, a circle or an ellipse in the layout, for a status dot, a color swatch or a progress bar. It takes its
 * box in the flow as an image does, with no children, and is drawn centered in that box, which the parent's align
 * places. A rect and an ellipse need a width and a height, and a circle an r. A shape with no fill and no stroke draws
 * nothing, but keeps its box. A stroke is centered on the edge of the box, so half of it lies outside. To put a shape
 * at a place on the page, use a rect, circle or ellipse in a layer.
 */
public final class ShapeAttr extends Attributes {
    /** Attributes with none set. */
    public ShapeAttr() {
        super("shape", "type");
    }

    public ShapeAttr type(String type) {
        set("type", type);
        return this;
    }

    /** Width of a rect or an ellipse, a length or a name from the width row of the tokens. A circle takes r instead. */
    public ShapeAttr width(String width) {
        set("width", width);
        return this;
    }

    /**
     * Height of a rect or an ellipse, a length or a name from the height row of the tokens. A circle takes r instead.
     */
    public ShapeAttr height(String height) {
        set("height", height);
        return this;
    }

    /** Radius of a circle. Its box is the square of side 2r. */
    public ShapeAttr r(String r) {
        set("r", r);
        return this;
    }

    /** Rounding of the corners of a rect. A circle and an ellipse are round already. */
    public ShapeAttr radius(String radius) {
        set("radius", radius);
        return this;
    }

    public ShapeAttr fill(String fill) {
        set("fill", fill);
        return this;
    }

    public ShapeAttr stroke(String stroke) {
        set("stroke", stroke);
        return this;
    }

    public ShapeAttr strokeWidth(String strokeWidth) {
        set("stroke-width", strokeWidth);
        return this;
    }

    public ShapeAttr strokeDash(String strokeDash) {
        set("stroke-dash", strokeDash);
        return this;
    }

    /** How opaque the shape is, from 0 to 1. Default 1. */
    public ShapeAttr opacity(double opacity) {
        set("opacity", opacity);
        return this;
    }

    public ShapeAttr debug(boolean debug) {
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
    public ShapeAttr ocg(String ocg) {
        set("ocg", ocg);
        return this;
    }
}
