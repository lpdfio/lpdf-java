package io.lpdf;

import java.util.Map;

/** A <code>barcode-at</code>: a barcode at a fixed place. */
public final class BarcodeAtNode extends CanvasNode {
    BarcodeAtNode(Map<String, Object> attrs) {
        super("barcode-at", attrs, null);
    }
}
