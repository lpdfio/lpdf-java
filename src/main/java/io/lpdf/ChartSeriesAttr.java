// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A series of a chart: one value in each category, in the order of the series. Left out, the chart has one series.
 * data-source repeats it for each item of a list, so the data can set how many series there are. A chart holds 100
 * series at most, counted after the data is bound.
 */
public final class ChartSeriesAttr extends Attributes {
    /** Attributes with none set. */
    public ChartSeriesAttr() {
        super("series");
    }

    /** The series' entry in the legend. */
    public ChartSeriesAttr label(String label) {
        set("label", label);
        return this;
    }

    /**
     * The color of the series' bars and its swatch in the legend. Without it, the series takes the next color of a
     * fixed palette of eight, which starts over past the eighth.
     */
    public ChartSeriesAttr color(String color) {
        set("color", color);
        return this;
    }
}
