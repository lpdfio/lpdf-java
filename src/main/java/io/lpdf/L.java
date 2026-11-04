package io.lpdf;

import java.util.List;

/**
 * Builds and renders Lpdf documents: one builder for each element, its attributes first and its children second.
 *
 * <pre>{@code
 * import io.lpdf.*;
 * import java.util.List;
 * import static io.lpdf.L.NoAttr;
 *
 * var doc = L.document(new DocumentAttr().size("a4"), List.of(
 *     L.section(NoAttr, List.of(
 *         L.layout(new LayoutAttr().margin("40pt"), List.of(
 *             L.text(new TextAttr().font("Helvetica-Bold"), List.of("Hello"))
 *         ))
 *     ))
 * ));
 * byte[] pdf = L.engine().setLicenseKey(key).render(doc);
 * }</pre>
 *
 * A builder whose attributes are optional takes {@link #NoAttr} in their place. A builder checks what it is given when
 * it is called, so a missing attribute or a wrong child throws on the line that wrote it.
 */
public final class L {
    private L() {
    }

    /**
     * No attributes: give it to any builder whose attributes are optional. Import it with
     * <code>import static io.lpdf.L.NoAttr;</code> and write it bare, as the other SDKs do.
     */
    public static final NoAttributes NoAttr = NoAttributes.INSTANCE;

    // ── Engine ────────────────────────────────────────────────────────────────

    /** A new engine, with a timeout of 5 seconds and a memory cap of 1024 MB. */
    public static PdfEngine engine() {
        return new PdfEngine();
    }

    /** A new engine with {@code options}. */
    public static PdfEngine engine(EngineOptions options) {
        return new PdfEngine(options);
    }

    // ── XML ───────────────────────────────────────────────────────────────────

    /** The XML of a document tree, without rendering it. */
    public static String toXml(PdfDocument document) {
        java.util.Objects.requireNonNull(document, "document");
        return new WasmRunner(EngineOptions.DEFAULT_TIMEOUT, EngineOptions.DEFAULT_MAX_MEMORY_MB).kitToXml(Json.write(document));
    }

    // ── Document and section ──────────────────────────────────────────────────

    /** Builds the <code>document</code>, the root: its attributes, tokens, assets, meta, output and attachments, and its sections. */
    public static PdfDocument document(DocumentAttr attrs, List<SectionNode> nodes) {
        return new PdfDocument(Attributes.of(attrs), KitNode.children("document", nodes));
    }

    /** Builds the <code>document</code> with no attributes. */
    public static PdfDocument document(NoAttributes attrs, List<SectionNode> nodes) {
        return document((DocumentAttr) null, nodes);
    }

    /**
     * Builds a <code>section</code>, a run of pages. Its content is a bookmark, then layers, then at most one layout or
     * markdown, then layers: the layers before the layout paint under it, and the layers after it paint over it.
     */
    public static SectionNode section(SectionAttr attrs, List<? extends SectionContent> nodes) {
        return new SectionNode(Attributes.of(attrs), KitNode.children("section", nodes));
    }

    /** Builds a <code>section</code> with no attributes. */
    public static SectionNode section(NoAttributes attrs, List<? extends SectionContent> nodes) {
        return section((SectionAttr) null, nodes);
    }

    /** Builds the <code>layout</code> of a section: the nodes that flow on its pages, inside its margin. */
    public static SectionLayout layout(LayoutAttr attrs, List<? extends Node> nodes) {
        return new SectionLayout(Attributes.of(attrs), KitNode.children("layout", nodes));
    }

    /** Builds the <code>layout</code> of a section with no attributes. */
    public static SectionLayout layout(NoAttributes attrs, List<? extends Node> nodes) {
        return layout((LayoutAttr) null, nodes);
    }

    /**
     * Builds a <code>markdown</code>, which takes the place of a layout in a section and holds only Markdown: the
     * {@link LMarkdown#styles} that say how each part looks, which are optional, and one {@link LMarkdown#content} with
     * the text. What Markdown can't say, such as the page, a header, a footer or a cover, stays in the section around
     * it: layers paint a header and a footer in the margin it leaves.
     */
    public static SectionMarkdown markdown(MarkdownAttr attrs, List<? extends MarkdownPart> nodes) {
        return new SectionMarkdown(Attributes.of(attrs), KitNode.children("markdown", nodes));
    }

    /** Builds a <code>markdown</code> with no attributes. */
    public static SectionMarkdown markdown(NoAttributes attrs, List<? extends MarkdownPart> nodes) {
        return markdown((MarkdownAttr) null, nodes);
    }

    /** The assets of a document, as given: a factory for symmetry with the other builders. */
    public static DocumentAssets assets(DocumentAssets attrs) {
        return attrs;
    }

    /** The tokens of a document, as given: a factory for symmetry with the other builders. */
    public static DocumentTokens tokens(DocumentTokens attrs) {
        return attrs;
    }

    // ── Layout containers ─────────────────────────────────────────────────────

    /** Builds a <code>column</code>: its children from top to bottom. */
    public static ContainerNode column(ColumnAttr attrs, List<? extends Node> nodes) {
        return container("column", attrs, nodes);
    }

    /** Builds a <code>column</code> with no attributes. */
    public static ContainerNode column(NoAttributes attrs, List<? extends Node> nodes) {
        return container("column", null, nodes);
    }

    /** Builds a <code>row</code>: its children side by side, wrapping. */
    public static ContainerNode row(RowAttr attrs, List<? extends Node> nodes) {
        return container("row", attrs, nodes);
    }

    /** Builds a <code>row</code> with no attributes. */
    public static ContainerNode row(NoAttributes attrs, List<? extends Node> nodes) {
        return container("row", null, nodes);
    }

    /** Builds a <code>grid</code>: its children in columns of equal width. */
    public static ContainerNode grid(GridAttr attrs, List<? extends Node> nodes) {
        return container("grid", attrs, nodes);
    }

    /** Builds a <code>grid</code> with no attributes. */
    public static ContainerNode grid(NoAttributes attrs, List<? extends Node> nodes) {
        return container("grid", null, nodes);
    }

    /** Builds a <code>list</code> of items, each an {@link #li}: bulleted, or numbered with <code>type("number")</code> in the <code>format</code> given. */
    public static ContainerNode list(ListAttr attrs, List<? extends Node> nodes) {
        return container("list", attrs, nodes);
    }

    /** Builds a <code>list</code> with no attributes. */
    public static ContainerNode list(NoAttributes attrs, List<? extends Node> nodes) {
        return container("list", null, nodes);
    }

    /** Builds an <code>li</code>, an item of a {@link #list}. Its words go in a {@link #text}; a list inside it is a list under the item. */
    public static ContainerNode li(LiAttr attrs, List<? extends Node> nodes) {
        return container("li", attrs, nodes);
    }

    /** Builds a <code>li</code> with no attributes. */
    public static ContainerNode li(NoAttributes attrs, List<? extends Node> nodes) {
        return container("li", null, nodes);
    }

    /** Builds a <code>frame</code>: a box of its own size around its children. */
    public static ContainerNode frame(FrameAttr attrs, List<? extends Node> nodes) {
        return container("frame", attrs, nodes);
    }

    /** Builds a <code>frame</code> with no attributes. */
    public static ContainerNode frame(NoAttributes attrs, List<? extends Node> nodes) {
        return container("frame", null, nodes);
    }

    /** Builds a <code>link</code> around its children, to its <code>href</code>. */
    public static ContainerNode link(LinkAttr attrs, List<? extends Node> nodes) {
        return container("link", attrs, nodes);
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    /** Builds a <code>table</code>, whose <code>cols</code> set the widths of its columns. */
    public static ContainerNode table(TableAttr attrs, List<? extends Node> nodes) {
        return container("table", attrs, nodes);
    }

    /** Builds a <code>thead</code>: the header row of a table, which repeats at the top of every page. */
    public static ContainerNode thead(TheadAttr attrs, List<? extends Node> nodes) {
        return container("thead", attrs, nodes);
    }

    /** Builds a <code>thead</code> with no attributes. */
    public static ContainerNode thead(NoAttributes attrs, List<? extends Node> nodes) {
        return container("thead", null, nodes);
    }

    /** Builds a <code>tr</code>, a row of a table. */
    public static ContainerNode tr(TrAttr attrs, List<? extends Node> nodes) {
        return container("tr", attrs, nodes);
    }

    /** Builds a <code>tr</code> with no attributes. */
    public static ContainerNode tr(NoAttributes attrs, List<? extends Node> nodes) {
        return container("tr", null, nodes);
    }

    /** Builds a <code>td</code>, a cell of a row. */
    public static ContainerNode td(TdAttr attrs, List<? extends Node> nodes) {
        return container("td", attrs, nodes);
    }

    /** Builds a <code>td</code> with no attributes. */
    public static ContainerNode td(NoAttributes attrs, List<? extends Node> nodes) {
        return container("td", null, nodes);
    }

    // ── Layout leaves ─────────────────────────────────────────────────────────

    /**
     * Builds a <code>text</code>, a paragraph. Its content is a list of strings, {@link #span}s, {@link #codeSpan}s and
     * {@link #fn}s, mixed: <code>List.of("Total due: ", L.span(…, List.of("$800.00")))</code>.
     *
     * @throws IllegalArgumentException when an item of {@code nodes} is anything else
     */
    public static TextNode text(TextAttr attrs, List<?> nodes) {
        return new TextNode(Attributes.of(attrs), KitNode.content("text", nodes));
    }

    /** Builds a <code>text</code> with no attributes. */
    public static TextNode text(NoAttributes attrs, List<?> nodes) {
        return text((TextAttr) null, nodes);
    }

    /**
     * Builds a <code>code</code>: code in a box that fills the width and grows with its lines. Its strings are kept as
     * they are written: every space, and a new line wherever there is a <code>\n</code>. A line longer than the box
     * wraps. The items are strings and {@link #span}s, which color parts of the code.
     *
     * @throws IllegalArgumentException when an item of {@code nodes} is not a string or an inline node
     */
    public static CodeNode code(CodeAttr attrs, List<?> nodes) {
        return new CodeNode(Attributes.of(attrs), KitNode.content("code", nodes));
    }

    /** Builds a <code>code</code> with no attributes. */
    public static CodeNode code(NoAttributes attrs, List<?> nodes) {
        return code((CodeAttr) null, nodes);
    }

    /** Builds a <code>span</code>, a run of a text in a style of its own. */
    public static SpanNode span(SpanAttr attrs, List<String> nodes) {
        return new SpanNode(Attributes.of(attrs), KitNode.children("span", nodes));
    }

    /** Builds a <code>span</code> with no attributes. */
    public static SpanNode span(NoAttributes attrs, List<String> nodes) {
        return span((SpanAttr) null, nodes);
    }

    /** Builds a <code>code-span</code>: a run of code in a text, in the code font on a tint. */
    public static CodeSpanNode codeSpan(CodeSpanAttr attrs, List<String> nodes) {
        return new CodeSpanNode(Attributes.of(attrs), KitNode.children("code-span", nodes));
    }

    /** Builds a <code>code-span</code> with no attributes. */
    public static CodeSpanNode codeSpan(NoAttributes attrs, List<String> nodes) {
        return codeSpan((CodeSpanAttr) null, nodes);
    }

    /**
     * Builds an <code>fn</code>, a footnote, for the content of a text. The engine numbers it and puts a small raised
     * number at its place in the text; its words go at the bottom of the page where that number lands. It has no
     * attributes. Its words are strings and spans.
     *
     * @throws IllegalArgumentException when an item of {@code nodes} is not a string or an inline node
     */
    public static FnNode fn(NoAttributes attrs, List<?> nodes) {
        return new FnNode(java.util.Map.of(), KitNode.content("fn", nodes));
    }

    /** Builds a <code>divider</code>, a line across its container. */
    public static DividerNode divider(DividerAttr attrs) {
        return new DividerNode(Attributes.of(attrs));
    }

    /** Builds a <code>divider</code> with no attributes. */
    public static DividerNode divider(NoAttributes attrs) {
        return divider((DividerAttr) null);
    }

    /** Builds an <code>img</code>, an image of the assets by its <code>name</code>. */
    public static ImgNode img(ImgAttr attrs) {
        return new ImgNode(required(attrs, "img"));
    }

    /** Builds a <code>shape</code>: a rect, circle or ellipse that takes its box in the layout, as an image does. */
    public static ShapeNode shape(ShapeAttr attrs) {
        return new ShapeNode(required(attrs, "shape"));
    }

    /** Builds a <code>barcode</code>: a QR code or a barcode of its <code>data</code>. */
    public static BarcodeNode barcode(BarcodeAttr attrs) {
        return new BarcodeNode(required(attrs, "barcode"));
    }

    /** Builds a <code>region</code>: a block pinned to an edge or a corner of the page, over or under the flow. */
    public static RegionNode region(RegionAttr attrs, List<? extends Node> nodes) {
        return new RegionNode(required(attrs, "region"), KitNode.children("region", nodes));
    }

    /** Builds a <code>field</code>, a form field the reader fills in. */
    public static FieldNode field(FieldAttr attrs) {
        return new FieldNode(required(attrs, "field"));
    }

    /**
     * Builds a <code>bookmark</code>: an entry of the PDF's outline and a target of <code>href="#id"</code> links. It
     * marks the node that follows it; first in a section's content, it marks the section's first page.
     */
    public static BookmarkNode bookmark(BookmarkAttr attrs) {
        return new BookmarkNode(required(attrs, "bookmark"));
    }

    // ── Layers ────────────────────────────────────────────────────────────────

    /** Builds a <code>layer</code>, which goes in a section and holds what it draws at fixed places. */
    public static LayerNode layer(LayerAttr attrs, List<? extends CanvasNode> nodes) {
        return new LayerNode(Attributes.of(attrs), KitNode.children("layer", nodes));
    }

    /** Builds a <code>layer</code> with no attributes. */
    public static LayerNode layer(NoAttributes attrs, List<? extends CanvasNode> nodes) {
        return layer((LayerAttr) null, nodes);
    }

    /** Builds a <code>rect</code> in a layer. */
    public static RectNode rect(RectAttr attrs) {
        return new RectNode(required(attrs, "rect"));
    }

    /** Builds a <code>line</code> in a layer. */
    public static LineNode line(LineAttr attrs) {
        return new LineNode(required(attrs, "line"));
    }

    /** Builds an <code>ellipse</code> in a layer. */
    public static EllipseNode ellipse(EllipseAttr attrs) {
        return new EllipseNode(required(attrs, "ellipse"));
    }

    /** Builds a <code>circle</code> in a layer. */
    public static CircleNode circle(CircleAttr attrs) {
        return new CircleNode(required(attrs, "circle"));
    }

    /** Builds a <code>path</code> in a layer, from the SVG path in its <code>d</code>. */
    public static PathNode path(PathAttr attrs) {
        return new PathNode(required(attrs, "path"));
    }

    /**
     * Builds a <code>text-at</code>, the text of the layout put at a fixed place. Its content is strings,
     * {@link #span}s and {@link #codeSpan}s, mixed.
     *
     * @throws IllegalArgumentException when an item of {@code nodes} is not a string or an inline node
     */
    public static TextAtNode textAt(TextAtAttr attrs, List<?> nodes) {
        return new TextAtNode(required(attrs, "text-at"), KitNode.content("text-at", nodes));
    }

    /** Builds an <code>img-at</code>, an image at a fixed place. It needs <code>width</code> or <code>height</code>. */
    public static ImgAtNode imgAt(ImgAtAttr attrs) {
        return new ImgAtNode(required(attrs, "img-at"));
    }

    /** Builds a <code>barcode-at</code>, a barcode at a fixed place. */
    public static BarcodeAtNode barcodeAt(BarcodeAtAttr attrs) {
        return new BarcodeAtNode(required(attrs, "barcode-at"));
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private static ContainerNode container(String type, Attributes attrs, List<? extends Node> nodes) {
        return new ContainerNode(type, Attributes.of(attrs), KitNode.children(type, nodes));
    }

    /** The attributes of an element that needs them. */
    private static java.util.Map<String, Object> required(Attributes attrs, String element) {
        java.util.Objects.requireNonNull(attrs, () -> "<" + element + "> needs its attributes");
        return attrs.values();
    }
}
