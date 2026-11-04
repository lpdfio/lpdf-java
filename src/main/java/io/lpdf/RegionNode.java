package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>region</code>: a block pinned to an edge or a corner of the page, over or under the flow. */
public final class RegionNode extends Node {
    RegionNode(Map<String, Object> attrs, List<?> nodes) {
        super("region", attrs, nodes);
    }
}
