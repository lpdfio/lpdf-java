package io.lpdf;

/** What a chart holds: its series and its categories. {@link LChart} builds them. */
public sealed interface ChartPart permits ChartSeriesNode, ChartCategoryNode {
    /** The element's name, as the schema writes it. */
    String type();
}
