package io.lpdf;

import java.util.Map;

/** A <code>line</code> in a layer. */
public final class LineNode extends CanvasNode {
    LineNode(Map<String, Object> attrs) {
        super("line", attrs, null);
    }
}
