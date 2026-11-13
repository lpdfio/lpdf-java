// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** One color of a gradient, at an offset along it. */
public final class GradientStop extends Attributes {
    /** Attributes with none set. */
    public GradientStop() {
        super("stop", "offset", "color");
    }

    /**
     * Where the color is reached along the gradient, from 0% (the start) to 100% (the end). Stops go in increasing
     * order.
     */
    public GradientStop offset(String offset) {
        set("offset", offset);
        return this;
    }

    /**
     * The color at the stop: hex, cmyk(), rgb(), hsl(), a color token, or a spot color or a tint of it. The stops of a
     * gradient are all CMYK, all RGB, or all tints of one spot color.
     */
    public GradientStop color(String color) {
        set("color", color);
        return this;
    }
}
