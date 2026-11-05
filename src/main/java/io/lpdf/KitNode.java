package io.lpdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A node of a document tree, the kit: its element, its attributes and its children, as the engine reads them. The
 * builders of {@link L} and {@link LMarkdown} make them.
 */
public abstract class KitNode {
    private final String type;
    private final Map<String, Object> attrs;
    private final List<?> nodes;

    /**
     * @param type  the element's name, as the schema writes it
     * @param attrs its attributes, by the schema's names
     * @param nodes its children, or null for an element that has none
     */
    KitNode(String type, Map<String, Object> attrs, List<?> nodes) {
        this.type = type;
        this.attrs = attrs;
        this.nodes = nodes;
    }

    /** The element's name, as the schema writes it: {@code column}, {@code text}, {@code img}. */
    public String type() {
        return type;
    }

    /** The attributes of the node, by the schema's names: {@code font-size}, not {@code fontSize}. */
    public Map<String, Object> attrs() {
        return attrs;
    }

    /** The children of the node, in order: nodes, and strings in a text. Empty for an element that has none. */
    public List<?> nodes() {
        return nodes == null ? List.of() : nodes;
    }

    /** The node as the engine reads it: {@code type}, {@code attrs}, and {@code nodes} when it has children. */
    Object tree() {
        var tree = new LinkedHashMap<String, Object>();
        tree.put("type", type);
        tree.put("attrs", attrs);
        if (nodes != null) tree.put("nodes", nodes);
        return tree;
    }

    /**
     * The children of an element, copied, or none when {@code nodes} is null.
     *
     * @throws NullPointerException when a child is null, naming its place
     */
    static <T> List<T> children(String element, List<? extends T> nodes) {
        if (nodes == null) return List.of();
        var copy = new ArrayList<T>(nodes.size());
        for (var node : nodes) {
            if (node == null) throw new NullPointerException("child " + (copy.size() + 1) + " of <" + element + "> is null");
            copy.add(node);
        }
        return Collections.unmodifiableList(copy);
    }

    /**
     * The content of a text, a code, a footnote or a text-at, copied: strings, spans, code spans and footnotes. The
     * engine says which of the inline nodes the element takes.
     *
     * @throws IllegalArgumentException when an item is anything else, naming its place and what it is
     */
    static List<Object> content(String element, List<?> nodes) {
        if (nodes == null) return List.of();
        var copy = new ArrayList<Object>(nodes.size());
        for (var item : nodes) {
            if (!(item instanceof String || item instanceof SpanNode || item instanceof CodeSpanNode || item instanceof FnNode)) {
                var what = item == null ? "null"
                        : item instanceof KitNode node ? "a <" + node.type() + ">"
                        : "a " + item.getClass().getName();
                throw new IllegalArgumentException("item " + (copy.size() + 1) + " of <" + element + "> is " + what
                        + ": a " + element + " holds strings and L.span, L.codeSpan and L.fn");
            }
            copy.add(item);
        }
        return Collections.unmodifiableList(copy);
    }

    @Override
    public String toString() {
        return Json.write(this);
    }
}
