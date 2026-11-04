package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>layer</code>: what it draws at fixed places, in a section, under its layout or over it. */
public final class LayerNode extends CanvasNode implements SectionContent {
    LayerNode(Map<String, Object> attrs, List<?> nodes) {
        super("layer", attrs, nodes);
    }
}
