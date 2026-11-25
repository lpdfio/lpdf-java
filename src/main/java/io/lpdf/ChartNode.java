package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>chart</code>: its attributes, then its series and its categories, in the layout. */
public final class ChartNode extends Node {
    ChartNode(Map<String, Object> attrs, List<? extends ChartPart> nodes) {
        super("chart", attrs, nodes);
    }
}
