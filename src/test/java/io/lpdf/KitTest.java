package io.lpdf;

import static io.lpdf.L.NoAttr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The tree the builders make, under the schema's names, and {@code L.toXml}, which writes it as XML. */
class KitTest {
    // ── The tree ──────────────────────────────────────────────────────────────

    @Test
    void a_document_has_version_1_and_type_document() {
        var tree = (Map<?, ?>) L.document(NoAttr, List.of()).tree();

        assertEquals(1, tree.get("version"));
        assertEquals("document", tree.get("type"));
    }

    @Test
    void meta_and_tokens_are_attributes_of_the_document() {
        var doc = L.document(new DocumentAttr()
                .meta(new DocumentMeta().title("Test"))
                .tokens(new DocumentTokens().colors(Map.of("primary", "#ff0000"))), List.of());

        assertEquals(Map.of("title", "Test"), doc.attrs().get("meta"));
        assertEquals(Map.of("colors", Map.of("primary", "#ff0000")), doc.attrs().get("tokens"));
    }

    @Test
    void the_token_rows_are_under_their_schema_names() {
        var doc = L.document(new DocumentAttr().tokens(new DocumentTokens()
                .lineHeight(Map.of("m", 1.2, "xxl", 2.0))
                .aliases(Map.of("caption", "xs", "sub-title", "l"))
                .height(Map.of("xs", "10pt", "m", "30pt"))
                .textSize(Map.of("s", "9pt"))), List.of());
        var tokens = (Map<?, ?>) doc.attrs().get("tokens");

        assertEquals(Map.of("m", 1.2, "xxl", 2.0), tokens.get("line-height"));
        assertEquals(Map.of("caption", "xs", "sub-title", "l"), tokens.get("aliases"));
        assertEquals(Map.of("xs", "10pt", "m", "30pt"), tokens.get("height"));
        assertEquals(Map.of("s", "9pt"), tokens.get("text-size"));
    }

    @Test
    void gradients_are_a_token_row_of_named_gradients() {
        var doc = L.document(new DocumentAttr().tokens(new DocumentTokens().gradients(Map.of(
                "fade", new Gradient().type("linear").from("primary").to("#0f3e8a").angle(90),
                "sunset", new Gradient().type("radial").stops(List.of(
                        new GradientStop().offset("0%").color("#f97316"),
                        new GradientStop().offset("100%").color("#8b5cf6")))))), List.of());
        var gradients = (Map<?, ?>) ((Map<?, ?>) doc.attrs().get("tokens")).get("gradients");

        assertEquals(Map.of("type", "linear", "from", "primary", "to", "#0f3e8a", "angle", 90.0), gradients.get("fade"));
        assertEquals(List.of(Map.of("offset", "0%", "color", "#f97316"), Map.of("offset", "100%", "color", "#8b5cf6")),
                ((Map<?, ?>) gradients.get("sunset")).get("stops"));
    }

    @Test
    void rulings_are_a_token_row_of_named_rulings_with_their_lines_under_the_schema_names() {
        var doc = L.document(new DocumentAttr().tokens(new DocumentTokens().rulings(Map.of(
                "graph", new Ruling().origin("margin").edges(false).lines(List.of(
                        new RulingLines().step("5mm"),
                        new RulingLines().countX(12).stepY("25mm")))))), List.of());
        var graph = (Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) doc.attrs().get("tokens")).get("rulings")).get("graph");

        assertEquals("margin", graph.get("origin"));
        assertEquals(false, graph.get("edges"));
        assertEquals(List.of(Map.of("step", "5mm"), Map.of("count-x", 12, "step-y", "25mm")), graph.get("lines"));
    }

    @Test
    void the_attributes_are_under_their_schema_names_and_types() {
        assertEquals("graph", L.section(new SectionAttr().ruling("graph"), List.of()).attrs().get("ruling"));
        assertEquals("l", L.text(new TextAttr().lineHeight("l"), List.of("A token")).attrs().get("line-height"));
        assertEquals(1.5, L.text(new TextAttr().lineHeight(1.5), List.of("A factor")).attrs().get("line-height"));
        assertEquals(Map.of("size", "a4", "orientation", "landscape"),
                L.document(new DocumentAttr().size("a4").orientation("landscape"), List.of()).attrs());
        assertEquals(Map.of("margin", "20pt 30pt"), L.layout(new LayoutAttr().margin("20pt 30pt"), List.of()).attrs());
        assertEquals(Map.of("col-width", "120pt", "cols", 3), L.grid(new GridAttr().colWidth("120pt").cols(3), List.of()).attrs());
    }

    @Test
    void a_container_is_a_node_of_its_element() {
        var column = L.column(new ColumnAttr().gap("m").background("surface"), List.of());
        var divider = L.divider(new DividerAttr().color("surface-alt"));

        assertEquals("column", column.type());
        assertEquals(Map.of("gap", "m", "background", "surface"), column.attrs());
        assertEquals("divider", divider.type());
        assertEquals(List.of(), divider.nodes());
        assertTrue(!divider.toString().contains("\"nodes\""), "a divider has no children");
    }

    @Test
    void a_section_holds_its_layout_and_its_layers() {
        var layout = L.layout(NoAttr, List.of(L.column(NoAttr, List.of())));
        var layer = L.layer(NoAttr, List.of(L.rect(new RectAttr().width("10pt").height("10pt"))));
        var section = L.section(new SectionAttr().size("a4"), List.of(layer, layout));

        assertEquals("section", section.type());
        assertEquals("a4", section.attrs().get("size"));
        assertEquals(List.of(layer, layout), section.nodes());
    }

    // ── L.toXml ───────────────────────────────────────────────────────────────

    private static PdfDocument simple() {
        return L.document(NoAttr, List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of())))));
    }

    @Test
    void to_xml_writes_the_declaration_and_the_lpdf_root() {
        var xml = L.toXml(simple());

        assertTrue(xml.startsWith("<?xml version=\"1.0\""), xml);
        assertTrue(xml.contains("<lpdf version=\"1\">"), xml);
    }

    @Test
    void to_xml_writes_the_assets_under_the_schema_names() {
        var xml = L.toXml(L.document(new DocumentAttr().assets(new DocumentAssets()
                .fonts(List.of(
                        new FontAttr().name("heading").core("Helvetica-Bold"),
                        new FontAttr().name("body").ref("body-font").src("/fonts/MyFont.ttf")))
                .images(List.of(new ImageAttr().name("logo").src("logo.png")))),
                List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of()))))));

        assertTrue(xml.contains("<font name=\"heading\" core=\"Helvetica-Bold\"/>"), xml);
        assertTrue(xml.contains("<font name=\"body\" ref=\"body-font\" src=\"/fonts/MyFont.ttf\"/>"), xml);
        assertTrue(xml.contains("<image name=\"logo\" src=\"logo.png\"/>"), xml);
    }

    @Test
    void to_xml_writes_the_tokens() {
        var xml = L.toXml(L.document(new DocumentAttr().tokens(new DocumentTokens()
                .aliases(Map.of("caption", "xs"))
                .height(Map.of("xs", "10pt"))
                .gradients(Map.of("fade", new Gradient().type("linear").from("primary").to("#0f3e8a").angle(90)))
                .rulings(Map.of("graph", new Ruling().origin("margin").lines(List.of(
                        new RulingLines().step("5mm"), new RulingLines().countX(12).stepY("25mm")))))),
                List.of(L.section(new SectionAttr().ruling("graph"), List.of(L.layout(NoAttr, List.of(
                        L.text(new TextAttr().fontSize("caption"), List.of("Small")),
                        L.column(new ColumnAttr().height("xs").background("gradient:fade"), List.of()))))))));

        for (var part : List.of("<alias name=\"caption\" value=\"xs\"/>", "<height xs=\"10pt\"",
                "<gradient name=\"fade\" type=\"linear\" from=\"primary\" to=\"#0f3e8a\" angle=\"90\"/>",
                "<ruling name=\"graph\" origin=\"margin\">", "<lines step=\"5mm\"/>", "count-x=\"12\"", "step-y=\"25mm\"",
                "<section ruling=\"graph\">", "<text font-size=\"caption\">",
                "<column background=\"gradient:fade\" height=\"xs\"/>")) {
            assertTrue(xml.contains(part), part + " in " + xml);
        }
    }

    @Test
    void the_xml_to_xml_writes_renders_the_pdf_of_the_tree() {
        var doc = L.document(NoAttr, List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of(
                L.text(NoAttr, List.of("Hello from toXml"))))))));
        var engine = L.engine().setLicenseKey("test-key");

        var fromXml = engine.render(L.toXml(doc));

        assertEquals("%PDF-", new String(fromXml, 0, 5, StandardCharsets.US_ASCII));
        assertEquals(Snapshots.sha256(engine.render(doc)), Snapshots.sha256(fromXml));
    }
}
