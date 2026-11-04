// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** Attributes of the <code>line</code> element in a layer. */
public final class LineAttr extends Attributes {
    /** Attributes with none set. */
    public LineAttr() {
        super("line", "x1", "y1", "x2", "y2");
    }

    public LineAttr x1(String x1) {
        set("x1", x1);
        return this;
    }

    public LineAttr y1(String y1) {
        set("y1", y1);
        return this;
    }

    public LineAttr x2(String x2) {
        set("x2", x2);
        return this;
    }

    public LineAttr y2(String y2) {
        set("y2", y2);
        return this;
    }

    public LineAttr stroke(String stroke) {
        set("stroke", stroke);
        return this;
    }

    public LineAttr strokeWidth(String strokeWidth) {
        set("stroke-width", strokeWidth);
        return this;
    }

    public LineAttr strokeDash(String strokeDash) {
        set("stroke-dash", strokeDash);
        return this;
    }

    public LineAttr lineCap(String lineCap) {
        set("line-cap", lineCap);
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
    public LineAttr ocg(String ocg) {
        set("ocg", ocg);
        return this;
    }
}
