package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>code-span</code>: a run of code in a text, in the code font on a tint. */
public final class CodeSpanNode extends Node {
    CodeSpanNode(Map<String, Object> attrs, List<?> nodes) {
        super("code-span", attrs, nodes);
    }
}
