package io.lpdf;

/**
 * What a section holds: a bookmark, then layers, then at most one layout or markdown, then layers. The layers before
 * the layout paint under it, and the layers after it over it.
 */
public sealed interface SectionContent permits BookmarkNode, LayerNode, SectionLayout, SectionMarkdown {
    /** The element's name, as the schema writes it. */
    String type();
}
