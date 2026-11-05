package io.lpdf;

import java.util.Map;

/** A <code>bookmark</code>: an entry of the outline and a target of links, in a layout or first in a section. */
public final class BookmarkNode extends Node implements SectionContent {
    BookmarkNode(Map<String, Object> attrs) {
        super("bookmark", attrs, null);
    }
}
