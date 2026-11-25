package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>category</code> of a chart: a value for each series, and its label. */
public final class ChartCategoryNode extends KitNode implements ChartPart {
    ChartCategoryNode(Map<String, Object> attrs, List<ChartValueNode> nodes) {
        super("category", attrs, nodes);
    }
}
