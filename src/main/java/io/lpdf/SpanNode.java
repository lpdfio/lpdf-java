package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>span</code>: a run of a text in a style of its own. */
public final class SpanNode extends Node {
    SpanNode(Map<String, Object> attrs, List<?> nodes) {
        super("span", attrs, nodes);
    }
}
