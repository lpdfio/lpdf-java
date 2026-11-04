package io.lpdf;

import java.util.List;
import java.util.Map;

/** A container of a layout: a column, row, grid, list, li, frame, link, table, thead, tr or td. */
public final class ContainerNode extends Node {
    ContainerNode(String type, Map<String, Object> attrs, List<?> nodes) {
        super(type, attrs, nodes);
    }
}
