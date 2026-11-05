package io.lpdf;

import java.util.Map;

/** A <code>rect</code> in a layer. */
public final class RectNode extends CanvasNode {
    RectNode(Map<String, Object> attrs) {
        super("rect", attrs, null);
    }
}
