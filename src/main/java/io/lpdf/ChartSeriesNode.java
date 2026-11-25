package io.lpdf;

import java.util.Map;

/** A <code>series</code> of a chart: its label and color in the legend. */
public final class ChartSeriesNode extends KitNode implements ChartPart {
    ChartSeriesNode(Map<String, Object> attrs) {
        super("series", attrs, null);
    }
}
