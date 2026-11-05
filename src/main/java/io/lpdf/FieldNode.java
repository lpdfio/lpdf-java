package io.lpdf;

import java.util.Map;

/** A <code>field</code>: a form field the reader fills in. */
public final class FieldNode extends Node {
    FieldNode(Map<String, Object> attrs) {
        super("field", attrs, null);
    }
}
