package io.lpdf;

import java.util.List;
import java.util.Map;

/**
 * A node of a layout: a container such as a column or a table, a text, an image and the rest. {@link L} builds them,
 * and a container takes them as its children.
 */
public abstract class Node extends KitNode {
    Node(String type, Map<String, Object> attrs, List<?> nodes) {
        super(type, attrs, nodes);
    }
}
