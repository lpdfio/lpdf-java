package io.lpdf;

import java.util.List;
import java.util.Map;

/** A node of a layer: a rect, line, ellipse, circle, path, text-at, img-at or barcode-at, drawn at a fixed place. */
public abstract class CanvasNode extends KitNode {
    CanvasNode(String type, Map<String, Object> attrs, List<?> nodes) {
        super(type, attrs, nodes);
    }
}
