package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>markdown</code> in a section, where a layout would go: its styles and its content. */
public final class SectionMarkdown extends KitNode implements SectionContent {
    SectionMarkdown(Map<String, Object> attrs, List<?> nodes) {
        super("markdown", attrs, nodes);
    }
}
