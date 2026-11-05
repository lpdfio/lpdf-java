package io.lpdf;

import java.util.Map;

/** An <code>img</code>: an image of the assets, in the layout. */
public final class ImgNode extends Node {
    ImgNode(Map<String, Object> attrs) {
        super("img", attrs, null);
    }
}
