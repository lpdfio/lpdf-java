package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>text-at</code>: the text of the layout at a fixed place, its content strings, spans and code spans. */
public final class TextAtNode extends CanvasNode {
    TextAtNode(Map<String, Object> attrs, List<?> nodes) {
        super("text-at", attrs, nodes);
    }
}
