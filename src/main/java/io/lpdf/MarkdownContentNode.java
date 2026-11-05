package io.lpdf;

import java.util.List;
import java.util.Map;

/** The <code>markdown-content</code> of a markdown: the Markdown itself, as strings that make one text. */
public final class MarkdownContentNode extends KitNode implements MarkdownPart {
    MarkdownContentNode(List<?> nodes) {
        super("markdown-content", Map.of(), nodes);
    }
}
