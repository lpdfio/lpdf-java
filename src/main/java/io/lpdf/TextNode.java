package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>text</code>: a paragraph of strings, spans, code spans and footnotes. */
public final class TextNode extends Node {
    TextNode(Map<String, Object> attrs, List<?> nodes) {
        super("text", attrs, nodes);
    }
}
