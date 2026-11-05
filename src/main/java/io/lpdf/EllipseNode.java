package io.lpdf;

import java.util.Map;

/** An <code>ellipse</code> in a layer. */
public final class EllipseNode extends CanvasNode {
    EllipseNode(Map<String, Object> attrs) {
        super("ellipse", attrs, null);
    }
}
