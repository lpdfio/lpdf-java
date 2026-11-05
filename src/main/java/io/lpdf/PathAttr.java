// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** Attributes of the <code>path</code> element in a layer. */
public final class PathAttr extends Attributes {
    /** Attributes with none set. */
    public PathAttr() {
        super("path", "d");
    }

    public PathAttr d(String d) {
        set("d", d);
        return this;
    }

    public PathAttr fill(String fill) {
        set("fill", fill);
        return this;
    }

    public PathAttr stroke(String stroke) {
        set("stroke", stroke);
        return this;
    }

    public PathAttr fillRule(String fillRule) {
        set("fill-rule", fillRule);
        return this;
    }

    public PathAttr strokeWidth(String strokeWidth) {
        set("stroke-width", strokeWidth);
        return this;
    }

    public PathAttr strokeDash(String strokeDash) {
        set("stroke-dash", strokeDash);
        return this;
    }

    public PathAttr lineCap(String lineCap) {
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
    public PathAttr ocg(String ocg) {
        set("ocg", ocg);
        return this;
    }
}
