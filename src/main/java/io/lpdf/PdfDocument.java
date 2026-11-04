package io.lpdf;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The root of a document tree, which {@link L#document} builds and {@link PdfEngine#render(PdfDocument)} renders. */
public final class PdfDocument extends KitNode {
    PdfDocument(Map<String, Object> attrs, List<SectionNode> nodes) {
        super("document", attrs, nodes);
    }

    @Override
    Object tree() {
        var tree = new LinkedHashMap<String, Object>();
        tree.put("version", 1);
        tree.put("type", type());
        tree.put("attrs", attrs());
        tree.put("nodes", nodes());
        return tree;
    }
}
