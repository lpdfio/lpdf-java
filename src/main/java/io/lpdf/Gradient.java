// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A named blend of colors. A background or a fill uses it as gradient:name, and the blend spans the box it fills. Give
 * from and to for two colors, or stop children for more.
 */
public final class Gradient extends Attributes {
    /** Attributes with none set. */
    public Gradient() {
        super("gradient", "type");
    }

    /** linear runs along a line across the box. radial runs from the center out to the farthest corner. */
    public Gradient type(String type) {
        set("type", type);
        return this;
    }

    /**
     * The first color: hex, cmyk(), rgb(), hsl(), a color token, or a spot color or a tint of it: CMYK if to is and RGB
     * if it is not, or the same spot color as to. Used with to, when there are no stop children. For a radial gradient
     * it is the center.
     */
    public Gradient from(String from) {
        set("from", from);
        return this;
    }

    /**
     * The last color: hex, cmyk(), rgb(), hsl(), a color token, or a spot color or a tint of it. Used with from, when
     * there are no stop children. For a radial gradient it is the edge.
     */
    public Gradient to(String to) {
        set("to", to);
        return this;
    }

    /**
     * The direction of a linear gradient, in degrees: 0 runs from the top to the bottom, 90 from the left to the right,
     * 180 from the bottom to the top. Default 0. A radial gradient has no direction.
     */
    public Gradient angle(double angle) {
        set("angle", angle);
        return this;
    }

    /**
     * The colors of the gradient, for more than two: each at an offset, in increasing order. Give these, or from and
     * to.
     */
    public Gradient stops(java.util.List<GradientStop> stops) {
        set("stops", stops == null ? null : java.util.List.copyOf(stops));
        return this;
    }
}
