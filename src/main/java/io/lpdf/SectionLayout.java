package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>layout</code> in a section: the nodes that flow on its pages, inside its margin. */
public final class SectionLayout extends KitNode implements SectionContent {
    SectionLayout(Map<String, Object> attrs, List<?> nodes) {
        super("layout", attrs, nodes);
    }
}
