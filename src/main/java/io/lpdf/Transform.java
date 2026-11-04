package io.lpdf;

import java.util.Locale;

/**
 * An affine transform, the matrix <code>[a, b, c, d, e, f]</code>, for a layer's <code>transform</code>:
 * <code>new LayerAttr().transform(new Transform(1, 0, 0, 1, 20, 0).toString())</code>.
 */
public record Transform(double a, double b, double c, double d, double e, double f) {
    /** The <code>matrix(a,b,c,d,e,f)</code> form, which a layer's <code>transform</code> takes. */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "matrix(%s,%s,%s,%s,%s,%s)", n(a), n(b), n(c), n(d), n(e), n(f));
    }

    /** A number as the shortest text that reads back as it: 1 rather than 1.0. */
    private static String n(double value) {
        return value == Math.rint(value) && Math.abs(value) < 1e15 ? Long.toString((long) value) : Double.toString(value);
    }
}
