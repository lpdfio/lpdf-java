package io.lpdf;

import java.util.Map;

/** An <code>img-at</code>: an image at a fixed place. */
public final class ImgAtNode extends CanvasNode {
    ImgAtNode(Map<String, Object> attrs) {
        super("img-at", attrs, null);
    }
}
