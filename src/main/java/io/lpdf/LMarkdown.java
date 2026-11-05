package io.lpdf;

import java.util.List;

/**
 * Builds the parts of a markdown: its styles, its content and the rules of its styles. The markdown itself is
 * {@link L#markdown}.
 *
 * <pre>{@code
 * L.markdown(new MarkdownAttr().margin("54pt").gap("m"), List.of(
 *     LMarkdown.styles(NoAttr, List.of(LMarkdown.h1(new MarkdownH1Attr().color("muted")))),
 *     LMarkdown.content(NoAttr, List.of(body))
 * ))
 * }</pre>
 */
public final class LMarkdown {
    private LMarkdown() {
    }

    /**
     * Builds the rules that say how the parts of a markdown look. A rule is named after the Markdown it styles and takes
     * the attributes of the element it becomes. A rule here overrides the same rule of the file that <code>src</code>
     * names, which overrides the defaults.
     */
    public static MarkdownStylesNode styles(MarkdownStylesAttr attrs, List<MarkdownRule> nodes) {
        return new MarkdownStylesNode(Attributes.of(attrs), KitNode.children("markdown-styles", nodes));
    }

    /** Builds the styles of a markdown with no attributes. */
    public static MarkdownStylesNode styles(NoAttributes attrs, List<MarkdownRule> nodes) {
        return styles((MarkdownStylesAttr) null, nodes);
    }

    /**
     * Builds the Markdown, as strings that make one text. The common indent of its lines is removed, so that indenting
     * the code doesn't make a code block of it. It has no attributes.
     */
    public static MarkdownContentNode content(NoAttributes attrs, List<String> nodes) {
        return new MarkdownContentNode(KitNode.children("markdown-content", nodes));
    }

    /** Builds the rule of a heading of level 1. */
    public static MarkdownRule h1(MarkdownH1Attr attrs) {
        return new MarkdownRule("h1", Attributes.of(attrs));
    }

    /** Builds the rule of a heading of level 1, with no attributes. */
    public static MarkdownRule h1(NoAttributes attrs) {
        return h1((MarkdownH1Attr) null);
    }

    /** Builds the rule of a heading of level 2. */
    public static MarkdownRule h2(MarkdownH2Attr attrs) {
        return new MarkdownRule("h2", Attributes.of(attrs));
    }

    /** Builds the rule of a heading of level 2, with no attributes. */
    public static MarkdownRule h2(NoAttributes attrs) {
        return h2((MarkdownH2Attr) null);
    }

    /** Builds the rule of a heading of level 3. */
    public static MarkdownRule h3(MarkdownH3Attr attrs) {
        return new MarkdownRule("h3", Attributes.of(attrs));
    }

    /** Builds the rule of a heading of level 3, with no attributes. */
    public static MarkdownRule h3(NoAttributes attrs) {
        return h3((MarkdownH3Attr) null);
    }

    /** Builds the rule of a heading of level 4. */
    public static MarkdownRule h4(MarkdownH4Attr attrs) {
        return new MarkdownRule("h4", Attributes.of(attrs));
    }

    /** Builds the rule of a heading of level 4, with no attributes. */
    public static MarkdownRule h4(NoAttributes attrs) {
        return h4((MarkdownH4Attr) null);
    }

    /** Builds the rule of a heading of level 5. */
    public static MarkdownRule h5(MarkdownH5Attr attrs) {
        return new MarkdownRule("h5", Attributes.of(attrs));
    }

    /** Builds the rule of a heading of level 5, with no attributes. */
    public static MarkdownRule h5(NoAttributes attrs) {
        return h5((MarkdownH5Attr) null);
    }

    /** Builds the rule of a heading of level 6. */
    public static MarkdownRule h6(MarkdownH6Attr attrs) {
        return new MarkdownRule("h6", Attributes.of(attrs));
    }

    /** Builds the rule of a heading of level 6, with no attributes. */
    public static MarkdownRule h6(NoAttributes attrs) {
        return h6((MarkdownH6Attr) null);
    }

    /** Builds the rule of a paragraph, and of the words of a list item, a quote and a footnote. */
    public static MarkdownRule p(MarkdownPAttr attrs) {
        return new MarkdownRule("p", Attributes.of(attrs));
    }

    /** Builds the rule of a paragraph, and of the words of a list item, a quote and a footnote, with no attributes. */
    public static MarkdownRule p(NoAttributes attrs) {
        return p((MarkdownPAttr) null);
    }

    /** Builds the rule of code: the box of a block, and the run of inline code. */
    public static MarkdownRule code(MarkdownCodeAttr attrs) {
        return new MarkdownRule("code", Attributes.of(attrs));
    }

    /** Builds the rule of code: the box of a block, and the run of inline code, with no attributes. */
    public static MarkdownRule code(NoAttributes attrs) {
        return code((MarkdownCodeAttr) null);
    }

    /** Builds the rule of a block quote. */
    public static MarkdownRule quote(MarkdownQuoteAttr attrs) {
        return new MarkdownRule("quote", Attributes.of(attrs));
    }

    /** Builds the rule of a block quote, with no attributes. */
    public static MarkdownRule quote(NoAttributes attrs) {
        return quote((MarkdownQuoteAttr) null);
    }

    /** Builds the rule of an item of a bullet or a numbered list. */
    public static MarkdownRule li(MarkdownLiAttr attrs) {
        return new MarkdownRule("li", Attributes.of(attrs));
    }

    /** Builds the rule of an item of a bullet or a numbered list, with no attributes. */
    public static MarkdownRule li(NoAttributes attrs) {
        return li((MarkdownLiAttr) null);
    }

    /** Builds the rule of a link. */
    public static MarkdownRule a(MarkdownAAttr attrs) {
        return new MarkdownRule("a", Attributes.of(attrs));
    }

    /** Builds the rule of a link, with no attributes. */
    public static MarkdownRule a(NoAttributes attrs) {
        return a((MarkdownAAttr) null);
    }

    /** Builds the rule of strong text. */
    public static MarkdownRule strong(MarkdownStrongAttr attrs) {
        return new MarkdownRule("strong", Attributes.of(attrs));
    }

    /** Builds the rule of strong text, with no attributes. */
    public static MarkdownRule strong(NoAttributes attrs) {
        return strong((MarkdownStrongAttr) null);
    }

    /** Builds the rule of emphasis. */
    public static MarkdownRule em(MarkdownEmAttr attrs) {
        return new MarkdownRule("em", Attributes.of(attrs));
    }

    /** Builds the rule of emphasis, with no attributes. */
    public static MarkdownRule em(NoAttributes attrs) {
        return em((MarkdownEmAttr) null);
    }

    /** Builds the rule of struck-through text. */
    public static MarkdownRule del(MarkdownDelAttr attrs) {
        return new MarkdownRule("del", Attributes.of(attrs));
    }

    /** Builds the rule of struck-through text, with no attributes. */
    public static MarkdownRule del(NoAttributes attrs) {
        return del((MarkdownDelAttr) null);
    }

    /** Builds the rule of a table. */
    public static MarkdownRule table(MarkdownTableAttr attrs) {
        return new MarkdownRule("table", Attributes.of(attrs));
    }

    /** Builds the rule of a table, with no attributes. */
    public static MarkdownRule table(NoAttributes attrs) {
        return table((MarkdownTableAttr) null);
    }

    /** Builds the rule of a cell of a table's header row. */
    public static MarkdownRule th(MarkdownThAttr attrs) {
        return new MarkdownRule("th", Attributes.of(attrs));
    }

    /** Builds the rule of a cell of a table's header row, with no attributes. */
    public static MarkdownRule th(NoAttributes attrs) {
        return th((MarkdownThAttr) null);
    }

    /** Builds the rule of a cell of a table's other rows. */
    public static MarkdownRule td(MarkdownTdAttr attrs) {
        return new MarkdownRule("td", Attributes.of(attrs));
    }

    /** Builds the rule of a cell of a table's other rows, with no attributes. */
    public static MarkdownRule td(NoAttributes attrs) {
        return td((MarkdownTdAttr) null);
    }
}
