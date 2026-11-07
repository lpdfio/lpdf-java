package io.lpdf;

import static io.lpdf.L.NoAttr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** The builders write the schema's names, and what they build renders like the XML it converts to. */
class SchemaTest {
    private static PdfDocument inLayout(Node... nodes) {
        return L.document(NoAttr, List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of(nodes))))));
    }

    // ── Attribute types ───────────────────────────────────────────────────────

    @Test
    void a_typed_attribute_keeps_its_type_in_the_tree() {
        var grid = L.grid(new GridAttr().cols(3).opacity(0.5).debug(true), List.of());

        assertEquals(Map.of("cols", 3, "opacity", 0.5, "debug", true), grid.attrs());
        assertEquals("m", L.text(new TextAttr().lineHeight("m"), List.of()).attrs().get("line-height"));
        assertEquals(1.5, L.text(new TextAttr().lineHeight(1.5), List.of()).attrs().get("line-height"));
    }

    @Test
    void a_typed_value_renders_as_its_text_does_in_xml() {
        var built = inLayout(L.grid(new GridAttr().cols(2).opacity(0.5), List.of(
                L.text(new TextAttr().bold(true).lineHeight(1.5), List.of("a")),
                L.text(new TextAttr().lineHeight("l"), List.of("b")))));
        var xml = "<lpdf version=\"1\"><document><section><layout><grid cols=\"2\" opacity=\"0.5\">"
                + "<text bold=\"true\" line-height=\"1.5\">a</text><text line-height=\"l\">b</text></grid></layout></section>"
                + "</document></lpdf>";

        assertEquals(render(xml), render(built));
    }

    /** Java refuses a wrong type when it compiles; a node built around the classes reaches the engine, which refuses it. */
    @Test
    void the_engine_refuses_a_value_of_the_wrong_type_and_an_attribute_the_element_does_not_have() {
        var type = assertThrows(EngineException.class, () -> render(inLayout(new TextNode(Map.of("bold", "true"), List.of()))));
        var name = assertThrows(EngineException.class, () -> render(inLayout(new TextNode(Map.of("colr", "red"), List.of()))));

        assertTrue(type.getMessage().contains("text: bold must be true or false, not \"true\""), type.getMessage());
        assertTrue(name.getMessage().contains("text: unknown attribute 'colr' — expected font-size, font, bold, color,"),
                name.getMessage());
    }

    /** JSON has no NaN or infinity, so such a number goes as its text, which the engine refuses by name. */
    @ParameterizedTest
    @CsvSource({"NaN, NaN", "Infinity, Infinity"})
    void the_engine_refuses_a_number_that_is_not_finite(double opacity, String text) {
        var error = assertThrows(EngineException.class,
                () -> render(inLayout(L.column(new ColumnAttr().opacity(opacity), List.of()))));

        assertTrue(error.getMessage().contains("column: opacity must be a number from 0 to 1, not \"" + text + "\""),
                error.getMessage());
    }

    @Test
    void a_line_height_is_written_as_a_number_or_a_name() {
        assertTrue(L.text(new TextAttr().lineHeight(1.5), List.of()).toString().contains("\"line-height\":1.5"));
        assertTrue(L.text(new TextAttr().lineHeight("m"), List.of()).toString().contains("\"line-height\":\"m\""));
        assertTrue(L.text(new TextAttr().lineHeight(Double.NaN), List.of()).toString().contains("\"line-height\":\"NaN\""));
    }

    // ── Attribute names ───────────────────────────────────────────────────────

    @Test
    void the_attributes_are_written_under_the_schema_names() {
        assertEquals(Map.of("align", "right", "bold", true),
                L.text(new TextAttr().align("right").bold(true), List.of("x")).attrs());
        assertEquals("https://lpdf.io", L.link(new LinkAttr().href("https://lpdf.io"), List.of()).attrs().get("href"));
        assertEquals("https://lpdf.io", L.span(new SpanAttr().href("https://lpdf.io"), List.of("x")).attrs().get("href"));
        assertEquals(Map.of("type", "text", "name", "email", "max-len", 40, "action-url", "https://lpdf.io"),
                L.field(new FieldAttr().type(FieldType.TEXT).name("email").maxLen(40).actionUrl("https://lpdf.io")).attrs());
        var region = L.region(new RegionAttr().pin(Pin.TOP), List.of(L.text(NoAttr, List.of("header"))));
        assertEquals("region", region.type());
        assertEquals("top", region.attrs().get("pin"));
        assertEquals(Map.of("font", "Times-Roman", "debug", true),
                L.document(new DocumentAttr().font("Times-Roman").debug(true), List.of()).attrs());
    }

    @Test
    void a_required_attribute_is_checked_when_the_builder_is_called() {
        var link = assertThrows(IllegalArgumentException.class, () -> L.link(new LinkAttr(), List.of()));
        var field = assertThrows(IllegalArgumentException.class, () -> L.field(new FieldAttr().name("email")));
        var rect = assertThrows(IllegalArgumentException.class, () -> L.rect(new RectAttr().height("10pt")));
        var font = assertThrows(IllegalArgumentException.class, () -> L.document(new DocumentAttr()
                .assets(new DocumentAssets().fonts(List.of(new FontAttr().core(BuiltinFont.TIMES_BOLD)))), List.of()));

        assertEquals("<link> needs href: set it with .href(…)", link.getMessage());
        assertEquals("<field> needs type: set it with .type(…)", field.getMessage());
        assertEquals("<rect> needs width: set it with .width(…)", rect.getMessage());
        assertEquals("<font> needs name: set it with .name(…)", font.getMessage());
        assertEquals("rect", L.rect(new RectAttr().width("10pt").height("10pt")).type());
    }

    @Test
    void the_constants_are_the_values_of_the_schema_enumerations() {
        assertEquals(List.of("text", "checkbox", "dropdown", "radio", "button"),
                List.of(FieldType.TEXT, FieldType.CHECKBOX, FieldType.DROPDOWN, FieldType.RADIO, FieldType.BUTTON));
        assertEquals(List.of("top", "bottom", "left", "right"), List.of(Pin.TOP, Pin.BOTTOM, Pin.LEFT, Pin.RIGHT));
        assertEquals(List.of("portrait", "landscape"), List.of(Orientation.PORTRAIT, Orientation.LANDSCAPE));
        assertEquals(List.of("each", "first", "last", "odd", "even"),
                List.of(PageScope.EACH, PageScope.FIRST, PageScope.LAST, PageScope.ODD, PageScope.EVEN));
        assertEquals(List.of("source", "data", "alternative", "supplement", "unspecified"),
                List.of(FileRelationship.SOURCE, FileRelationship.DATA, FileRelationship.ALTERNATIVE,
                        FileRelationship.SUPPLEMENT, FileRelationship.UNSPECIFIED));
        assertEquals("all", Permission.ALL);
        assertEquals("Times-Bold", BuiltinFont.TIMES_BOLD);
    }

    // ── Layers ────────────────────────────────────────────────────────────────

    @Test
    void the_shapes_of_a_layer_are_written_under_their_schema_names() {
        var rect = L.rect(new RectAttr().x("10pt").y("20pt").width("100pt").height("50pt").fill("#ff0000").radius("5pt"));

        assertEquals("rect", rect.type());
        assertEquals(Map.of("width", "100pt", "height", "50pt", "x", "10pt", "y", "20pt", "fill", "#ff0000", "radius", "5pt"),
                rect.attrs());
        assertEquals("line", L.line(new LineAttr().x1("0pt").y1("0pt").x2("9pt").y2("9pt").lineCap("round")).type());
        assertEquals("circle", L.circle(new CircleAttr().r("5pt").x("1pt").y("1pt")).type());
        assertEquals("ellipse", L.ellipse(new EllipseAttr().rx("5pt").ry("3pt")).type());
        assertEquals("img-at", L.imgAt(new ImgAtAttr().name("logo").width("10pt").height("10pt")).type());
        assertEquals("evenodd", L.path(new PathAttr().d("M 0 0 L 9 9").fillRule("evenodd")).attrs().get("fill-rule"));
        var barcode = L.barcodeAt(new BarcodeAtAttr().type("qr").data("x").x("10pt").y("20pt").width("60pt"));
        assertEquals("barcode-at", barcode.type());
        assertEquals("60pt", barcode.attrs().get("width"));
    }

    @Test
    void a_layer_and_its_text_are_written_under_the_schema_names() {
        var textAt = L.textAt(new TextAtAttr().x("10pt").y("20pt").fontSize("12pt"),
                List.of("base ", L.span(new SpanAttr().font("Helvetica-Bold"), List.of("bold"))));
        var layer = L.layer(new LayerAttr().page(PageScope.FIRST).opacity(0.5).transform("rotate(45 100 100)")
                .clip("0pt 0pt 200pt 100pt").font("Courier").fontSize("s").color("danger"), List.of());

        assertEquals("text-at", textAt.type());
        assertEquals("12pt", textAt.attrs().get("font-size"));
        assertInstanceOf(SpanNode.class, textAt.nodes().get(1));
        assertEquals("layer", layer.type());
        assertEquals(Map.of("page", "first", "opacity", 0.5, "transform", "rotate(45 100 100)", "clip", "0pt 0pt 200pt 100pt",
                "font", "Courier", "font-size", "s", "color", "danger"), layer.attrs());
    }

    @Test
    void a_box_has_a_color_an_opacity_and_a_shadow_and_so_do_the_shapes_of_a_layer() {
        assertEquals(Map.of("color", "text-muted", "opacity", 0.5),
                L.column(new ColumnAttr().color("text-muted").opacity(0.5), List.of()).attrs());
        assertEquals("2pt 3pt #000000 0.2", L.column(new ColumnAttr().shadow("2pt 3pt #000000 0.2"), List.of()).attrs().get("shadow"));
        assertEquals("2pt 3pt #000000",
                L.rect(new RectAttr().width("50pt").height("30pt").shadow("2pt 3pt #000000")).attrs().get("shadow"));
        assertEquals("-2pt -3pt text", L.circle(new CircleAttr().r("5pt").shadow("-2pt -3pt text")).attrs().get("shadow"));
        assertEquals("2pt 3pt #000000 0.5",
                L.ellipse(new EllipseAttr().rx("5pt").ry("3pt").shadow("2pt 3pt #000000 0.5")).attrs().get("shadow"));
    }

    @Test
    void a_transform_is_a_string_a_layer_accepts() {
        assertEquals("matrix(1,0,0,1,10,20)", new Transform(1, 0, 0, 1, 10, 20).toString());
    }

    @Test
    void a_shape_is_sized_by_width_and_height_and_placed_by_x_and_y() {
        for (var attrs : List.of(RectAttr.class, ImgAtAttr.class, TextAtAttr.class)) {
            assertTrue(has(attrs, "width") && !has(attrs, "w"), attrs.getSimpleName());
        }
        assertTrue(has(RectAttr.class, "height") && !has(RectAttr.class, "h"));
        assertTrue(has(BarcodeAttr.class, "width") && has(BarcodeAttr.class, "height") && !has(BarcodeAttr.class, "size"));
        for (var attrs : List.of(CircleAttr.class, EllipseAttr.class)) {
            assertTrue(has(attrs, "x") && has(attrs, "y") && !has(attrs, "cx") && !has(attrs, "cy"), attrs.getSimpleName());
        }
    }

    private static boolean has(Class<?> type, String setter) {
        return Arrays.stream(type.getMethods()).anyMatch(method -> method.getName().equals(setter));
    }

    @Test
    void a_layer_shape_is_drawn() {
        var empty = L.document(new DocumentAttr().size("a4"), List.of(L.section(NoAttr, List.of(L.layer(NoAttr, List.of())))));
        var drawn = L.document(new DocumentAttr().size("a4"), List.of(L.section(NoAttr, List.of(L.layer(NoAttr, List.of(
                L.rect(new RectAttr().x("50pt").y("50pt").width("200pt").height("100pt").fill("#ff0000"))))))));

        assertNotEquals(render(empty), render(drawn));
    }

    @Test
    void layers_go_directly_in_a_section_before_and_after_its_layout() {
        var section = L.section(NoAttr, List.of(
                L.layer(new LayerAttr().page(PageScope.FIRST), List.of(L.rect(new RectAttr().width("10pt").height("10pt")))),
                L.layout(NoAttr, List.of(L.text(NoAttr, List.of("Body")))),
                L.layer(new LayerAttr().page(PageScope.LAST), List.of(L.rect(new RectAttr().width("20pt").height("20pt"))))));

        assertEquals(List.of("layer", "layout", "layer"), section.nodes().stream().map(node -> ((KitNode) node).type()).toList());
        var xml = L.toXml(L.document(NoAttr, List.of(section)));
        var under = xml.indexOf("<layer page=\"first\">");
        var layout = xml.indexOf("<layout>");
        var over = xml.indexOf("<layer page=\"last\">");
        assertTrue(under >= 0 && under < layout && layout < over, xml);
        assertTrue(!xml.contains("<canvas"), xml);
    }

    @Test
    void a_section_with_two_layouts_is_rejected() {
        var doc = L.document(NoAttr, List.of(L.section(NoAttr, List.of(
                L.layout(NoAttr, List.of(L.text(NoAttr, List.of("a")))),
                L.layout(NoAttr, List.of(L.text(NoAttr, List.of("b"))))))));

        var error = assertThrows(EngineException.class, () -> render(doc));

        assertTrue(error.getMessage().contains("at most one <layout>"), error.getMessage());
    }

    @Test
    void a_text_at_a_circle_and_the_margin_of_the_layout_are_written_as_their_xml() {
        Function<String, PdfDocument> circleAt = x -> L.document(new DocumentAttr().size("a4"), List.of(L.section(NoAttr,
                List.of(L.layer(NoAttr, List.of(L.circle(new CircleAttr().x(x).y("20pt").r("10pt").fill("#ff0000"))))))));
        var textAt = L.toXml(L.document(NoAttr, List.of(L.section(NoAttr, List.of(L.layer(NoAttr, List.of(
                L.textAt(new TextAtAttr().x("10pt").y("20pt").maxLines(2).width("80pt"), List.of("Page {page} of {pages}")))))))));
        var circle = L.toXml(circleAt.apply("20pt"));
        var margin = L.toXml(L.document(NoAttr, List.of(L.section(NoAttr, List.of(
                L.layout(new LayoutAttr().margin("48pt"), List.of(L.text(NoAttr, List.of("Body")))))))));

        assertTrue(textAt.contains("<text-at ") && textAt.contains("max-lines=\"2\"") && textAt.contains(">Page {page} of {pages}</text-at>"),
                textAt);
        assertTrue(circle.contains("<circle ") && circle.contains("x=\"20pt\"") && !circle.contains("cx="), circle);
        assertNotEquals(render(circleAt.apply("20pt")), render(circleAt.apply("120pt")));
        assertTrue(margin.contains("<layout margin=\"48pt\">"), margin);
    }

    @Test
    void a_text_that_captures_and_one_that_shows_a_capture_are_written_under_the_schema_names() {
        assertEquals("chapter", L.text(new TextAttr().capture("chapter"), List.of("x")).attrs().get("capture"));
        assertEquals(Map.of("capture-ref", "chapter", "capture-mode", "first", "capture-fallback", "None"),
                L.text(new TextAttr().captureRef("chapter").captureMode("first").captureFallback("None"), List.of()).attrs());
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Test
    void a_built_document_renders_the_same_as_its_xml() {
        var built = L.document(new DocumentAttr().size("a4"), List.of(L.section(NoAttr, List.of(
                L.layout(NoAttr, List.of(L.column(new ColumnAttr().gap("12pt"), List.of(
                        L.text(new TextAttr().align("right").bold(true), List.of("Title")),
                        L.text(NoAttr, List.of("Body ", L.span(new SpanAttr().bold(true), List.of("bold")), " text")),
                        L.link(new LinkAttr().href("https://lpdf.io"), List.of(L.text(NoAttr, List.of("link")))))))),
                L.layer(new LayerAttr().page(PageScope.EACH), List.of(
                        L.rect(new RectAttr().x("40pt").y("40pt").width("100pt").height("60pt").fill("#ff0000").radius("6pt")),
                        L.circle(new CircleAttr().x("260pt").y("260pt").r("40pt").fill("#00ff00")),
                        L.textAt(new TextAtAttr().x("40pt").y("120pt").fontSize("10pt"),
                                List.of("Layer ", L.span(new SpanAttr().color("#0000ff"), List.of("text"))))))))));

        assertEquals(render(built), render(L.toXml(built)));
    }

    @Test
    void an_alias_is_the_slot_it_names_and_a_name_that_is_neither_is_rejected() {
        Function<String, PdfDocument> build = size -> L.document(
                new DocumentAttr().tokens(new DocumentTokens().aliases(Map.of("caption", "xs", "heading", "xl"))),
                List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of(L.text(new TextAttr().fontSize(size), List.of("Sized"))))))));

        var pdf = render(build.apply("caption"));

        assertEquals(pdf, render(L.toXml(build.apply("caption"))));
        assertEquals(pdf, render(build.apply("xs")));
        assertNotEquals(pdf, render(build.apply("heading")));
        var error = assertThrows(EngineException.class, () -> render(build.apply("medum")));
        assertTrue(error.getMessage().contains("invalid text size: 'medum': expected a slot from xs to xxl, an alias (caption"),
                error.getMessage());
    }

    @Test
    void a_height_is_the_slot_it_names_and_one_that_is_not_a_slot_a_length_fill_or_full_is_rejected() {
        Function<String, PdfDocument> build = height -> build(height, null);

        var pdf = render(build.apply("m"));

        assertEquals(pdf, render(L.toXml(build.apply("m"))));
        assertEquals(pdf, render(build.apply("32pt")));
        assertNotEquals(pdf, render(build.apply("xl")));
        var row = Map.of("xs", "10pt", "s", "20pt", "m", "50pt", "l", "60pt", "xl", "70pt", "xxl", "80pt");
        assertEquals(render(build.apply("50pt")), render(build("m", row)));
        var error = assertThrows(EngineException.class, () -> render(build.apply("medum")));
        assertTrue(error.getMessage().contains(
                "invalid height value: 'medum' — expected full, fill, a slot from xs to xxl or a length in pt, mm or in"), error.getMessage());
    }

    private static PdfDocument build(String height, Map<String, String> row) {
        var attrs = row == null ? new DocumentAttr() : new DocumentAttr().tokens(new DocumentTokens().height(row));
        return L.document(attrs, List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of(
                L.column(new ColumnAttr().background("#cccccc").height(height), List.of())))))));
    }

    @Test
    void a_shape_has_the_attributes_of_its_type_and_each_type_is_drawn_as_its_xml_is() {
        var rect = L.shape(new ShapeAttr().type("rect").width("14pt").height("m").radius("2pt").fill("primary")
                .stroke("#000000").strokeWidth("1pt").strokeDash("3 2").opacity(0.5));
        Function<List<Node>, PdfDocument> build = shapes -> {
            var row = new ArrayList<Node>(shapes);
            row.add(L.text(NoAttr, List.of("After")));
            return inLayout(L.row(new RowAttr().align("center"), row));
        };

        assertEquals("shape", rect.type());
        assertEquals(Map.of("type", "rect", "width", "14pt", "height", "m", "radius", "2pt", "fill", "primary",
                "stroke", "#000000", "stroke-width", "1pt", "stroke-dash", "3 2", "opacity", 0.5), rect.attrs());
        var without = render(build.apply(List.of()));
        for (var shape : List.of(
                L.shape(new ShapeAttr().type("rect").width("60pt").height("12pt").fill("#00aa00")),
                L.shape(new ShapeAttr().type("circle").r("8pt").fill("#00aa00")),
                L.shape(new ShapeAttr().type("ellipse").width("40pt").height("20pt").fill("#00aa00")))) {
            var document = build.apply(List.of(shape));
            assertNotEquals(without, render(document));
            assertEquals(render(document), render(L.toXml(document)));
        }
        var error = assertThrows(EngineException.class, () -> render(inLayout(L.shape(new ShapeAttr().type("circle")))));
        assertTrue(error.getMessage().contains("a circle needs 'r'"), error.getMessage());
    }

    // ── Tables ────────────────────────────────────────────────────────────────

    @Test
    void a_kept_table_row_moves_on_with_the_row_after_it_as_its_xml_does() {
        // The table starts 30pt above the foot of the page, so rows A and B fit and C does not.
        Function<TrAttr, PdfDocument> build = keep -> inLayout(
                L.frame(new FrameAttr().height("739pt"), List.of()),
                L.table(new TableAttr().cols("1fr"), List.of(
                        L.tr(NoAttr, List.of(L.td(NoAttr, List.of(L.text(NoAttr, List.of("A")))))),
                        L.tr(keep, List.of(L.td(NoAttr, List.of(L.text(NoAttr, List.of("B")))))),
                        L.tr(NoAttr, List.of(L.td(NoAttr, List.of(L.text(NoAttr, List.of("C")))))))));
        var kept = build.apply(new TrAttr().keep("with-next"));

        assertEquals("with-next", L.tr(new TrAttr().keep("with-next"), List.of()).attrs().get("keep"));
        assertNotEquals(render(build.apply(new TrAttr())), render(kept));
        assertEquals(render(kept), render(L.toXml(kept)));
    }

    @Test
    void a_cell_that_spans_columns_or_rows_moves_the_cells_after_it_as_its_xml_does() {
        Function<TdAttr, PdfDocument> columns = first -> inLayout(L.table(new TableAttr().cols("1fr 1fr 1fr"), List.of(
                L.tr(NoAttr, List.of(L.td(first, List.of(L.text(NoAttr, List.of("A")))), L.td(NoAttr, List.of(L.text(NoAttr, List.of("B")))))))));
        Function<TdAttr, PdfDocument> rows = first -> inLayout(L.table(new TableAttr().cols("1fr 1fr"), List.of(
                L.tr(NoAttr, List.of(L.td(first, List.of(L.text(NoAttr, List.of("A")))), L.td(NoAttr, List.of(L.text(NoAttr, List.of("B")))))),
                L.tr(NoAttr, List.of(L.td(NoAttr, List.of(L.text(NoAttr, List.of("C")))))))));

        var colspan = columns.apply(new TdAttr().colspan(2));
        var rowspan = rows.apply(new TdAttr().rowspan(2));

        assertEquals(2, L.td(new TdAttr().colspan(2), List.of()).attrs().get("colspan"));
        assertNotEquals(render(columns.apply(new TdAttr())), render(colspan));
        assertEquals(render(colspan), render(L.toXml(colspan)));
        assertNotEquals(render(rows.apply(new TdAttr())), render(rowspan));
        assertEquals(render(rowspan), render(L.toXml(rowspan)));
        var tooWide = assertThrows(EngineException.class, () -> render(columns.apply(new TdAttr().colspan(3))));
        var tooLong = assertThrows(EngineException.class, () -> render(rows.apply(new TdAttr().rowspan(3))));
        assertTrue(tooWide.getMessage().contains("span 4 columns, but the table has 3"), tooWide.getMessage());
        assertTrue(tooLong.getMessage().contains("a rowspan that runs 1 row past the last row of the table"), tooLong.getMessage());
    }

    record Entry(String date, String amount) {
    }

    record Entries(List<Entry> entries) {
    }

    @Test
    void a_bulk_table_renders_from_data_as_the_table_without_bulk_and_one_built_with_l_which_has_no_data_is_rejected() {
        Function<String, String> xml = bulk -> "<lpdf version=\"1\"><document size=\"a4\"><section><layout margin=\"36pt\">"
                + "<table cols=\"1fr 2fr\"" + bulk + "><thead><td><text bold=\"true\">Date</text></td><td><text bold=\"true\">Amount"
                + "</text></td></thead><tr data-source=\"entries\"><td><text data-value=\"date\">-</text></td><td><text "
                + "data-value=\"amount\">-</text></td></tr></table><region pin=\"bottom\"><text align=\"right\">Page {page} of "
                + "{pages}</text></region></layout></section></document></lpdf>";
        var data = new Entries(IntStream.range(0, 2000)
                .mapToObj(i -> new Entry(String.format("2026-01-%02d", i % 28 + 1), "$" + i + ".00")).toList());
        var engine = L.engine().setLicenseKey("test-key");

        var pdf = normalised(engine.render(xml.apply(" bulk=\"true\""), new RenderOptions().data(data)));

        assertEquals(pdf, normalised(engine.render(xml.apply(""), new RenderOptions().data(data))));
        var pages = Pattern.compile("/Type /Pages[^>]*/Count (\\d+)").matcher(pdf).results().findFirst().orElseThrow().group(1);
        assertTrue(Integer.parseInt(pages) > 20, "the rows run over " + pages + " pages");
        var built = inLayout(L.table(new TableAttr().cols("1fr").bulk(true), List.of(
                L.tr(NoAttr, List.of(L.td(NoAttr, List.of(L.text(NoAttr, List.of("A")))))))));
        assertTrue(L.toXml(built).contains("<table bulk=\"true\" cols=\"1fr\">"));
        var error = assertThrows(EngineException.class, () -> render(built));
        assertTrue(error.getMessage().contains("a bulk table repeats one tr with data-source: it has none"), error.getMessage());
    }

    // ── Sections ──────────────────────────────────────────────────────────────

    @Test
    void a_section_in_columns_and_an_inset_render_as_their_xml_and_an_inset_past_the_columns_is_rejected() {
        Function<Integer, PdfDocument> build = col -> L.document(NoAttr, List.of(L.section(
                new SectionAttr().columns(3).columnGap("m").columnRule("0.5pt #cccccc").columnFill(ColumnFill.BALANCE),
                List.of(L.layout(NoAttr, List.of(
                        L.region(new RegionAttr().pin(Pin.INSET).col(col).colspan(2).top("50pt").height("100pt"),
                                List.of(L.text(NoAttr, List.of("Inset")))),
                        L.text(NoAttr, List.of("One")),
                        L.text(new TextAttr().paginate("break-before"), List.of("Two"))))))));

        var xml = L.toXml(build.apply(2));

        assertTrue(xml.contains("<section column-fill=\"balance\" column-gap=\"m\" column-rule=\"0.5pt #cccccc\" columns=\"3\">"), xml);
        assertTrue(xml.contains("<region col=\"2\" colspan=\"2\" height=\"100pt\" pin=\"inset\" top=\"50pt\">"), xml);
        assertEquals(render(build.apply(2)), render(xml));
        var error = assertThrows(EngineException.class, () -> render(build.apply(3)));
        assertTrue(error.getMessage().contains("col 3 and colspan 2 reach column 4, past the section's 3 columns"), error.getMessage());
    }

    @Test
    void a_section_s_page_numbering_renders_as_its_xml_does_and_labels_the_pages() {
        var footer = L.region(new RegionAttr().pin(Pin.BOTTOM), List.of(L.text(NoAttr, List.of("Page {page} of {pages}"))));
        var document = L.document(NoAttr, List.of(
                L.section(new SectionAttr().pageFormat("lower-roman"), List.of(L.layout(NoAttr, List.of(L.text(NoAttr, List.of("Front")), footer)))),
                L.section(new SectionAttr().pageStart(1).pageFormat("decimal").pagePrefix("A-"),
                        List.of(L.layout(NoAttr, List.of(L.text(NoAttr, List.of("Body")), footer))))));

        var xml = L.toXml(document);
        var pdf = render(document);

        assertTrue(xml.contains("<section page-format=\"lower-roman\">"), xml);
        assertTrue(xml.contains("<section page-format=\"decimal\" page-prefix=\"A-\" page-start=\"1\">"), xml);
        assertEquals(pdf, render(xml));
        assertTrue(pdf.replaceAll("\\s+", " ").contains("/PageLabels << /Nums [0 << /S /r >> 1 << /S /D /P (A-) >>] >>"));
        var zero = L.document(NoAttr, List.of(L.section(new SectionAttr().pageStart(0), List.of(L.layout(NoAttr, List.of())))));
        var error = assertThrows(EngineException.class, () -> render(zero));
        assertTrue(error.getMessage().contains("page-start must be a whole number from 1 to 2147483647, not 0"), error.getMessage());
    }

    // ── Footnotes, code and lists ─────────────────────────────────────────────

    @Test
    void a_footnote_is_an_fn_in_a_text_and_its_counter_an_attribute_of_the_document_and_a_section() {
        var text = L.text(NoAttr, List.of("Total",
                L.fn(NoAttr, List.of("See ", L.span(new SpanAttr().bold(true), List.of("this")), ".")), " here"));
        var document = L.document(new DocumentAttr().footnoteCounter(FootnoteCounter.DOCUMENT), List.of(
                L.section(new SectionAttr().footnoteCounter(FootnoteCounter.PAGE), List.of(L.layout(NoAttr, List.of(text))))));

        var xml = L.toXml(document);

        assertTrue(Pattern.compile("<document [^>]*footnote-counter=\"document\"").matcher(xml).find(), xml);
        assertTrue(Pattern.compile("<section [^>]*footnote-counter=\"page\"").matcher(xml).find(), xml);
        assertTrue(xml.contains("<text>Total<fn>See <span bold=\"true\">this</span>.</fn> here</text>"), xml);
        assertEquals("fn", L.fn(NoAttr, List.of()).type());
        assertEquals(Map.of(), L.fn(NoAttr, List.of()).attrs());
    }

    @Test
    void a_footnote_is_drawn_as_its_xml_is_and_one_in_a_table_is_rejected() {
        Function<List<Object>, PdfDocument> build = notes -> {
            var content = new ArrayList<Object>();
            content.add("Total");
            content.addAll(notes);
            content.add(" here");
            return inLayout(L.text(NoAttr, content));
        };
        Function<String, PdfDocument> counted = counter -> L.document(new DocumentAttr().footnoteCounter(counter), List.of(
                L.section(NoAttr, List.of(L.layout(NoAttr, List.of(L.text(NoAttr, List.of("One", L.fn(NoAttr, List.of("A.")))))))),
                L.section(NoAttr, List.of(L.layout(NoAttr, List.of(L.text(NoAttr, List.of("Two", L.fn(NoAttr, List.of("B."))))))))));
        var noted = build.apply(List.of(L.fn(NoAttr, List.of("The note."))));

        assertNotEquals(render(build.apply(List.of())), render(noted));
        assertEquals(render(noted), render(L.toXml(noted)));
        assertNotEquals(render(counted.apply(FootnoteCounter.DOCUMENT)), render(counted.apply(FootnoteCounter.SECTION)));
        var table = inLayout(L.table(new TableAttr().cols("1fr"), List.of(L.tr(NoAttr, List.of(L.td(NoAttr,
                List.of(L.text(NoAttr, List.of("x", L.fn(NoAttr, List.of("y")))))))))));
        var inTable = assertThrows(EngineException.class, () -> render(table));
        var bad = assertThrows(EngineException.class, () -> render(counted.apply("book")));
        assertTrue(inTable.getMessage().contains("<fn> is not supported in a table"), inTable.getMessage());
        assertTrue(bad.getMessage().contains("invalid footnote-counter 'book'"), bad.getMessage());
    }

    @Test
    void a_code_and_a_code_span_are_written_as_their_elements_and_render_as_their_xml() {
        var document = inLayout(
                L.code(new CodeAttr().tabSize(2), List.of("\nfn a() {\n\tb(", L.span(new SpanAttr().color("#c0392b"), List.of("x")), ");\n}\n")),
                L.text(NoAttr, List.of("Run ", L.codeSpan(new CodeSpanAttr().background("#ffff00"), List.of("cargo test")), " now")));

        var xml = L.toXml(document);

        assertTrue(xml.contains("<code tab-size=\"2\">\nfn a() {\n\tb(<span color=\"#c0392b\">x</span>);\n}\n</code>"), xml);
        assertTrue(xml.contains("Run <code-span background=\"#ffff00\">cargo test</code-span> now"), xml);
        assertEquals("code", L.code(NoAttr, List.of()).type());
        assertEquals("code-span", L.codeSpan(NoAttr, List.of()).type());
        assertEquals(render(document), render(xml));
    }

    @Test
    void a_code_keeps_its_spaces_goes_on_past_a_page_and_a_bad_tab_size_is_rejected() {
        Function<String, PdfDocument> build = code -> inLayout(L.code(NoAttr, List.of(code)));
        var lines = IntStream.rangeClosed(1, 150).mapToObj(i -> "line " + i).collect(Collectors.joining("\n"));

        assertNotEquals(render(build.apply("a    b")), render(build.apply("a b")));
        assertTrue(Pattern.compile("/Type\\s*/Page[^s]").matcher(render(build.apply(lines))).results().count() >= 2,
                "the code splits across pages");
        var error = assertThrows(EngineException.class, () -> render(inLayout(L.code(new CodeAttr().tabSize(0), List.of("x")))));
        assertTrue(error.getMessage().contains("tab-size must be a whole number from 1 to 16, not 0"), error.getMessage());
    }

    @Test
    void a_list_is_written_with_its_items_and_renders_as_its_xml() {
        var document = inLayout(L.list(new ListAttr().type("number").format("lower-alpha").suffix(")"), List.of(
                L.li(NoAttr, List.of(L.text(NoAttr, List.of("One")))),
                L.li(NoAttr, List.of(L.text(NoAttr, List.of("Two")),
                        L.list(new ListAttr().bullet("dash"), List.of(L.li(NoAttr, List.of(L.text(NoAttr, List.of("Nested")))))))))));

        var xml = L.toXml(document);

        assertTrue(Pattern.compile("<list format=\"lower-alpha\" suffix=\"\\)\" type=\"number\">\\s*<li>\\s*<text>One</text>\\s*</li>")
                .matcher(xml).find(), xml);
        assertTrue(Pattern.compile("<list bullet=\"dash\">\\s*<li>\\s*<text>Nested</text>").matcher(xml).find(), xml);
        assertEquals("list", L.list(NoAttr, List.of()).type());
        assertEquals("li", L.li(NoAttr, List.of()).type());
        assertEquals(render(document), render(xml));
    }

    @Test
    void a_number_list_writes_its_numbers_and_a_bullet_list_refuses_what_numbers_them() {
        Function<ListAttr, PdfDocument> build = attrs -> inLayout(L.list(attrs, List.of(
                L.li(NoAttr, List.of(L.text(NoAttr, List.of("A")))), L.li(NoAttr, List.of(L.text(NoAttr, List.of("B")))))));

        var bullets = render(build.apply(new ListAttr()));
        var numbers = render(build.apply(new ListAttr().type("number")));
        var roman = render(build.apply(new ListAttr().type("number").format("lower-roman").start(3)));

        assertNotEquals(bullets, numbers);
        assertNotEquals(numbers, roman);
        var error = assertThrows(EngineException.class, () -> render(build.apply(new ListAttr().format("lower-alpha"))));
        assertTrue(error.getMessage().contains("numbers the items of a list"), error.getMessage());
    }

    // ── Tokens, fonts and bookmarks ───────────────────────────────────────────

    @Test
    void a_gradient_fills_a_box_and_a_ruling_is_drawn_once_as_their_xml_are() {
        Function<String, PdfDocument> filled = background -> L.document(new DocumentAttr().tokens(new DocumentTokens().gradients(
                        Map.of("fade", new Gradient().type("linear").from("#ff0000").to("#0000ff").angle(90)))),
                List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of(
                        L.column(new ColumnAttr().background(background).height("60pt"), List.of())))))));
        var ruled = L.document(new DocumentAttr().tokens(new DocumentTokens().rulings(Map.of("notes",
                        new Ruling().edges(false).lines(List.of(new RulingLines().stepY("8mm").color("#c9d6e8")))))),
                List.of(L.section(new SectionAttr().ruling("notes"), List.of(L.layout(NoAttr, List.of(L.text(NoAttr, List.of("On ruled paper"))))))));

        var gradient = render(filled.apply("gradient:fade"));
        var rulings = render(ruled);

        assertEquals(gradient, render(L.toXml(filled.apply("gradient:fade"))));
        assertNotEquals(gradient, render(filled.apply("#336699")));
        assertTrue(gradient.contains("/ShadingType 2"));
        assertEquals(rulings, render(L.toXml(ruled)));
        assertEquals(1, Pattern.compile("/Subtype /Form").matcher(rulings).results().count());
    }

    @Test
    void bold_text_is_the_bold_face_of_the_font() {
        Function<String, String> doc = attrs ->
                "<lpdf version=\"1\"><document><section><layout><text " + attrs + ">Hello</text></layout></section></document></lpdf>";

        assertEquals(render(doc.apply("bold=\"true\"")), render(doc.apply("font=\"Helvetica-Bold\"")));
        assertNotEquals(render(doc.apply("bold=\"true\"")), render(doc.apply("")));
    }

    @Test
    void a_bookmark_goes_in_a_layout_and_directly_in_a_section() {
        var bookmark = L.bookmark(new BookmarkAttr().label("Methods").level(2).id("methods"));
        var section = L.section(new SectionAttr().type(SectionType.TOC).tocIndentStep("14pt"), List.of(
                L.bookmark(new BookmarkAttr().label("Contents")),
                L.layout(NoAttr, List.of(bookmark, L.text(NoAttr, List.of("Methods"))))));

        assertEquals(Map.of("label", "Methods", "level", 2, "id", "methods"), bookmark.attrs());
        assertEquals(Map.of("type", "toc", "toc-indent-step", "14pt"), section.attrs());
        assertEquals("bookmark", assertInstanceOf(BookmarkNode.class, section.nodes().get(0)).type());
    }

    @Test
    void bookmarks_fill_the_outline_and_a_cross_reference_links_to_its_bookmark_as_their_xml_do() {
        var built = L.document(NoAttr, List.of(
                L.section(NoAttr, List.of(L.bookmark(new BookmarkAttr().label("Cover")), L.layout(NoAttr, List.of(L.text(NoAttr, List.of("Cover")))))),
                L.section(NoAttr, List.of(L.layout(NoAttr, List.of(
                        L.link(new LinkAttr().href("#methods"), List.of(L.text(NoAttr, List.of("Go to Methods")))),
                        L.text(NoAttr, List.of("See ", L.span(new SpanAttr().bookmarkRef("prices"), List.of("{bookmark-label} on page {bookmark-page}")))),
                        L.bookmark(new BookmarkAttr().label("Methods")),
                        L.text(NoAttr, List.of("Methods")),
                        L.bookmark(new BookmarkAttr().label("Table 3").id("prices").refOnly(true)),
                        L.text(NoAttr, List.of("Prices"))))))));

        var xml = L.toXml(built);
        var pdf = render(built);

        assertTrue(xml.contains("<span bookmark-ref=\"prices\">"), xml);
        assertTrue(Pattern.compile("<bookmark [^>]*ref-only=\"true\"").matcher(xml).find(), xml);
        for (var part : List.of("/Title (Cover)", "/Title (Methods)", "/D /methods", "/D /prices")) assertTrue(pdf.contains(part), part);
        assertTrue(!pdf.contains("/Title (Table 3)"));
        assertEquals(pdf, render(xml));
        var nowhere = inLayout(L.text(NoAttr, List.of(L.span(new SpanAttr().bookmarkRef("nowhere"), List.of("{bookmark-page}")))));
        var error = assertThrows(EngineException.class, () -> render(nowhere));
        assertTrue(error.getMessage().contains("bookmark-ref 'nowhere' names no bookmark"), error.getMessage());
    }

    // ── Assets ────────────────────────────────────────────────────────────────

    private static final byte[] PIXEL = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    private static PdfDocument withAssets(DocumentAssets assets, Node... nodes) {
        var attrs = assets == null ? new DocumentAttr() : new DocumentAttr().assets(assets);
        return L.document(attrs, List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of(nodes))))));
    }

    @Test
    void assets_declare_fonts_and_images_with_the_schema_names() {
        var assets = new DocumentAssets()
                .fonts(List.of(new FontAttr().name("heading").core(BuiltinFont.TIMES_BOLD)))
                .images(List.of(new ImageAttr().name("logo").ref("company-logo").src("logo.png")));

        var declared = (Map<?, ?>) L.document(new DocumentAttr().assets(assets), List.of()).attrs().get("assets");

        assertEquals(List.of(Map.of("name", "heading", "core", "Times-Bold")), declared.get("fonts"));
        assertEquals(List.of(Map.of("name", "logo", "ref", "company-logo", "src", "logo.png")), declared.get("images"));
        assertSame(assets, L.assets(assets));
    }

    @Test
    void a_font_declared_in_the_assets_is_the_font_the_text_is_set_in() {
        var built = withAssets(new DocumentAssets().fonts(List.of(new FontAttr().name("heading").core(BuiltinFont.TIMES_BOLD))),
                L.text(new TextAttr().font("heading"), List.of("Hello")));

        assertTrue(render(built).contains("/BaseFont /Times-Bold"));
        assertEquals(render(built), render(L.toXml(built)));
    }

    @Test
    void an_image_declared_in_the_assets_renders_as_its_xml_and_one_not_declared_is_an_error_that_names_it() {
        var declared = withAssets(new DocumentAssets().images(List.of(new ImageAttr().name("logo"))),
                L.img(new ImgAttr().name("logo").width("40pt")));
        var ghost = withAssets(null, L.img(new ImgAttr().name("ghost")));
        var engine = L.engine().setLicenseKey("test-key").loadImage("logo", PIXEL).loadImage("ghost", PIXEL);

        assertEquals(normalised(engine.render(declared)), normalised(engine.render(L.toXml(declared))));
        var error = assertThrows(EngineException.class, () -> engine.render(ghost));
        assertTrue(error.getMessage().contains("ghost"), error.getMessage());
    }

    @Test
    void an_image_declared_with_a_src_is_read_through_the_src_fallback() {
        var built = withAssets(new DocumentAssets().images(List.of(new ImageAttr().name("logo").src("logo.png"))),
                L.img(new ImgAttr().name("logo").width("40pt")));

        var pdf = L.engine(new EngineOptions().srcFallback(src -> PIXEL)).setLicenseKey("test-key").render(built);

        assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));
    }

    // ── Markdown ──────────────────────────────────────────────────────────────

    private static final String MARKDOWN_BODY = "# Release notes\n\nSome *emphasis*, **strong** text and `code`.\n\n- one\n- two\n\n> a quote\n";
    private static final String RED_TITLES = "<markdown-styles><h1 color=\"#ff0000\"/></markdown-styles>";

    private static PdfDocument markdownDocument(MarkdownAttr attrs, MarkdownStylesNode styles, String text) {
        var parts = new ArrayList<MarkdownPart>();
        if (styles != null) parts.add(styles);
        parts.add(LMarkdown.content(NoAttr, List.of(text)));
        return L.document(NoAttr, List.of(L.section(NoAttr, List.of(L.markdown(attrs == null ? new MarkdownAttr() : attrs, parts)))));
    }

    private static MarkdownStylesNode redTitles() {
        return LMarkdown.styles(NoAttr, List.of(LMarkdown.h1(new MarkdownH1Attr().color("#ff0000"))));
    }

    @Test
    void a_markdown_its_styles_its_content_and_their_rules_are_built_with_the_schema_names() {
        var markdown = L.markdown(new MarkdownAttr().margin("54pt").gap("m").bookmarks(2), List.of());
        var styles = LMarkdown.styles(new MarkdownStylesAttr().src("styles.xml"),
                List.of(LMarkdown.h1(new MarkdownH1Attr().fontSize("xl").color("muted"))));
        var rule = (MarkdownRule) styles.nodes().get(0);

        assertEquals("markdown", markdown.type());
        assertEquals(Map.of("margin", "54pt", "gap", "m", "bookmarks", 2), markdown.attrs());
        assertEquals("markdown-styles", styles.type());
        assertEquals("h1", rule.type());
        assertEquals(Map.of("font-size", "xl", "color", "muted"), rule.attrs());
        assertEquals("quote", LMarkdown.quote(new MarkdownQuoteAttr().barColor("primary")).type());
        var thematicBreak = LMarkdown.hr(new MarkdownHrAttr().color("muted").thickness("xs"));
        assertEquals("hr", thematicBreak.type());
        assertEquals(Map.of("color", "muted", "thickness", "xs"), thematicBreak.attrs());
        assertEquals(Map.of(), LMarkdown.hr(NoAttr).attrs());
        assertEquals(Map.of(), LMarkdown.em(NoAttr).attrs());
        assertEquals(List.of("# a"), LMarkdown.content(NoAttr, List.of("# a")).nodes());
    }

    @Test
    void a_markdown_is_written_as_xml_with_its_markdown_as_it_is_and_renders_as_its_xml() {
        var document = markdownDocument(new MarkdownAttr().gap("8pt"), redTitles(), "a < b & c ]]> d\n\n    code\n");

        var xml = L.toXml(document);

        assertTrue(xml.contains("<markdown gap=\"8pt\">"), xml);
        assertTrue(Pattern.compile("<markdown-styles>\\s*<h1 color=\"#ff0000\"\\s*/>\\s*</markdown-styles>").matcher(xml).find(), xml);
        assertTrue(xml.contains("<markdown-content><![CDATA[a < b & c ]]]]><![CDATA[> d"), xml);
        assertEquals(render(document), render(xml));
    }

    @Test
    void a_markdown_renders_its_text_and_a_rule_that_sets_a_color_changes_the_page() {
        var plain = render(markdownDocument(null, null, MARKDOWN_BODY));

        assertTrue(plain.startsWith("%PDF-"));
        assertNotEquals(plain, render(markdownDocument(null, redTitles(), MARKDOWN_BODY)));
        assertNotEquals(plain, render(markdownDocument(null, null, "# Another title")));
        assertEquals(render(markdownDocument(null, null, "fine\n\nkept\n")), render(markdownDocument(null, null, "fine\n\n<div>kept</div>\n")));
    }

    @Test
    void a_file_of_rules_is_read_from_the_src_of_its_styles_or_loaded_by_name_and_one_not_there_is_named() {
        var inline = render(markdownDocument(null, redTitles(), MARKDOWN_BODY));
        var fromSrc = markdownDocument(null, LMarkdown.styles(new MarkdownStylesAttr().src("styles.xml"), List.of()), MARKDOWN_BODY);
        var reading = L.engine(new EngineOptions().srcFallback(src -> RED_TITLES.getBytes(StandardCharsets.UTF_8))).setLicenseKey("test-key");
        var loaded = L.engine().setLicenseKey("test-key").loadMarkdownStyles("styles.xml", RED_TITLES);

        assertEquals(inline, normalised(reading.render(fromSrc)));
        assertEquals(inline, normalised(reading.render(L.toXml(fromSrc))));
        assertEquals(inline, normalised(loaded.render(fromSrc)));
        var missing = assertThrows(EngineException.class, () -> render(markdownDocument(null,
                LMarkdown.styles(new MarkdownStylesAttr().src("missing-styles.xml"), List.of()), MARKDOWN_BODY)));
        assertTrue(missing.getMessage().contains("missing-styles.xml"), missing.getMessage());
    }

    @Test
    void the_markdown_comes_from_the_data_of_an_xml_document_or_the_text_written_in_it() {
        var xml = "<lpdf version=\"1\"><document><section><markdown><markdown-content data-value=\"body\"><![CDATA[# From the text]]>"
                + "</markdown-content></markdown></section></document></lpdf>";
        var engine = L.engine().setLicenseKey("test-key");

        var fromData = normalised(engine.render(xml, new RenderOptions().data(Map.of("body", "# From the data"))));

        assertNotEquals(fromData, normalised(engine.render(xml)));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String render(PdfDocument document) {
        return normalised(L.engine().setLicenseKey("test-key").render(document));
    }

    private static String render(String xml) {
        return normalised(L.engine().setLicenseKey("test-key").render(xml));
    }

    /** A PDF as text, with the parts that vary left out. */
    private static String normalised(byte[] pdf) {
        var text = new String(pdf, StandardCharsets.ISO_8859_1);
        text = text.replaceAll("/CreationDate[^\\n]*", "");
        return text.replaceAll("/ID *\\[[^\\]]*\\]", "");
    }
}
