package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>value</code> of a category, as plain text: a number, or nothing for no value. */
public final class ChartValueNode extends KitNode {
    ChartValueNode(List<String> nodes) {
        super("value", Map.of(), nodes);
    }
}
