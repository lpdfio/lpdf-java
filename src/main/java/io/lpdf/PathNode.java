package io.lpdf;

import java.util.Map;

/** A <code>path</code> in a layer, from an SVG path. */
public final class PathNode extends CanvasNode {
    PathNode(Map<String, Object> attrs) {
        super("path", attrs, null);
    }
}
