// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A category of a chart: a bar for each series, or a stack of them, with its label beside the axis. It holds a value
 * for each series of the chart, after the data is bound. data-source repeats it for each item of a list. A chart holds
 * 10,000 categories at most, counted after the data is bound.
 */
public final class ChartCategoryAttr extends Attributes {
    /** Attributes with none set. */
    public ChartCategoryAttr() {
        super("category");
    }

    /** The category's label, under its bars, or beside them with direction="horizontal". */
    public ChartCategoryAttr label(String label) {
        set("label", label);
        return this;
    }

    /** The color of the category's bar, in a chart of one series, where the series' color is the default. */
    public ChartCategoryAttr color(String color) {
        set("color", color);
        return this;
    }
}
