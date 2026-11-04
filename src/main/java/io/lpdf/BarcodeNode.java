package io.lpdf;

import java.util.Map;

/** A <code>barcode</code>: a QR code or a barcode, in the layout. */
public final class BarcodeNode extends Node {
    BarcodeNode(Map<String, Object> attrs) {
        super("barcode", attrs, null);
    }
}
