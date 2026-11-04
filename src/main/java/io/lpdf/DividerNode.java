package io.lpdf;

import java.util.Map;

/** A <code>divider</code>: a line across its container. */
public final class DividerNode extends Node {
    DividerNode(Map<String, Object> attrs) {
        super("divider", attrs, null);
    }
}
