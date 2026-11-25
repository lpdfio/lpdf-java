// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * The values of the <code>type</code> attribute of a chart: the kind of chart. bar draws a bar for each value, line a
 * line through the values of each series, area a line filled down to zero, or stacked, and sparkline a small line with
 * no axis, labels or legend.
 */
public final class ChartType {
    private ChartType() {
    }

    public static final String BAR = "bar";
    public static final String LINE = "line";
    public static final String AREA = "area";
    public static final String SPARKLINE = "sparkline";
}
