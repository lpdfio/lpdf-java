package io.lpdf;

import java.util.Map;

/** A rule of a markdown's styles: the Markdown it is named after, such as <code>h1</code>, and the attributes it sets. */
public final class MarkdownRule extends KitNode {
    MarkdownRule(String type, Map<String, Object> attrs) {
        super(type, attrs, null);
    }
}
