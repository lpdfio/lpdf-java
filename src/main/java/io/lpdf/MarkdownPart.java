package io.lpdf;

/** What a markdown holds: its styles, which are optional, and its content. {@link LMarkdown} builds them. */
public sealed interface MarkdownPart permits MarkdownStylesNode, MarkdownContentNode {
    /** The element's name, as the schema writes it. */
    String type();
}
