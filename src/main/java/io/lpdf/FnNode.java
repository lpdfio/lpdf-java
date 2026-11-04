package io.lpdf;

import java.util.List;
import java.util.Map;

/** An <code>fn</code>: a footnote, numbered by the engine, its words at the bottom of the page. */
public final class FnNode extends Node {
    FnNode(Map<String, Object> attrs, List<?> nodes) {
        super("fn", attrs, nodes);
    }
}
