package io.lpdf;

import java.util.List;
import java.util.Map;

/** A <code>code</code>: code in a box that fills the width, its strings kept as they are written. */
public final class CodeNode extends Node {
    CodeNode(Map<String, Object> attrs, List<?> nodes) {
        super("code", attrs, nodes);
    }
}
