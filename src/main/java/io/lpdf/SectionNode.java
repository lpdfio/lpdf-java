package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>section</code>: a run of pages with the same size, margin and layers. */
public final class SectionNode extends KitNode {
    SectionNode(Map<String, Object> attrs, List<?> nodes) {
        super("section", attrs, nodes);
    }
}
