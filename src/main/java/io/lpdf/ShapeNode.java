package io.lpdf;

import java.util.Map;

/** A <code>shape</code>: a rect, circle or ellipse that takes its box in the layout, as an image does. */
public final class ShapeNode extends Node {
    ShapeNode(Map<String, Object> attrs) {
        super("shape", attrs, null);
    }
}
