package io.lpdf;

import java.util.Map;

/** A <code>circle</code> in a layer. */
public final class CircleNode extends CanvasNode {
    CircleNode(Map<String, Object> attrs) {
        super("circle", attrs, null);
    }
}
