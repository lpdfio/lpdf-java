package io.lpdf;

import java.util.List;
import java.util.Map;

/** The <code>markdown-styles</code> of a markdown: the rules that say how its parts look. */
public final class MarkdownStylesNode extends KitNode implements MarkdownPart {
    MarkdownStylesNode(Map<String, Object> attrs, List<?> nodes) {
        super("markdown-styles", attrs, nodes);
    }
}
