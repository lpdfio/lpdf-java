package io.lpdf;

import static io.lpdf.L.NoAttr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * A text's content: strings and inline nodes in one list, which {@code L.text} checks when it is called, so a wrong item
 * throws on the line that wrote it. The children of a container, and the attributes a builder needs, are checked there
 * too.
 */
class TextContentTest {
    @Test
    void text_takes_attributes_first_and_content_second() {
        var node = L.text(new TextAttr().bold(true), List.of("Hello"));

        assertEquals(true, node.attrs().get("bold"));
        assertEquals(List.of("Hello"), node.nodes());
    }

    @Test
    void text_takes_strings_and_inline_nodes_in_one_list() {
        var node = L.text(NoAttr, List.of(
                "Total: ",
                L.span(new SpanAttr().bold(true), List.of("$100")),
                " due ",
                L.codeSpan(NoAttr, List.of("x")),
                L.fn(NoAttr, List.of("A note."))));

        assertEquals(5, node.nodes().size());
        assertEquals("Total: ", node.nodes().get(0));
        var span = assertInstanceOf(SpanNode.class, node.nodes().get(1));
        assertEquals(true, span.attrs().get("bold"));
        assertInstanceOf(CodeSpanNode.class, node.nodes().get(3));
        assertInstanceOf(FnNode.class, node.nodes().get(4));
    }

    @Test
    void a_wrong_item_is_refused_where_text_is_called_naming_its_place() {
        var block = assertThrows(IllegalArgumentException.class,
                () -> L.text(NoAttr, List.of("a", L.column(NoAttr, List.of()))));
        var number = assertThrows(IllegalArgumentException.class, () -> L.code(NoAttr, List.of(42)));
        var none = assertThrows(IllegalArgumentException.class, () -> L.fn(NoAttr, Arrays.asList("a", null)));

        assertEquals("item 2 of <text> is a <column>: a text holds strings and L.span, L.codeSpan and L.fn", block.getMessage());
        assertEquals("item 1 of <code> is a java.lang.Integer: a code holds strings and L.span, L.codeSpan and L.fn",
                number.getMessage());
        assertTrue(none.getMessage().startsWith("item 2 of <fn> is null"), none.getMessage());
    }

    @Test
    void a_text_at_holds_the_same_content() {
        var node = L.textAt(new TextAtAttr().x("0pt").y("0pt"), List.of("Page ", L.span(NoAttr, List.of("1"))));

        assertEquals("text-at", node.type());
        assertEquals(2, node.nodes().size());
        assertThrows(IllegalArgumentException.class,
                () -> L.textAt(new TextAtAttr().x("0pt").y("0pt"), List.of(L.rect(new RectAttr()))));
    }

    @Test
    void span_takes_attributes_first_and_content_second() {
        var span = L.span(new SpanAttr().color("primary"), List.of("$100"));

        assertEquals("primary", span.attrs().get("color"));
        assertEquals(List.of("$100"), span.nodes());
    }

    @Test
    void a_null_child_is_refused_naming_its_place() {
        var error = assertThrows(NullPointerException.class,
                () -> L.column(NoAttr, Arrays.asList(L.text(NoAttr, List.of("x")), null)));

        assertEquals("child 2 of <column> is null", error.getMessage());
    }

    @Test
    void a_builder_refuses_attributes_without_one_it_needs() {
        var table = assertThrows(IllegalArgumentException.class, () -> L.table(new TableAttr(), List.of()));
        var img = assertThrows(NullPointerException.class, () -> L.img(null));

        assertEquals("<table> needs cols: set it with .cols(…)", table.getMessage());
        assertEquals("<img> needs its attributes", img.getMessage());
    }

    @Test
    void attributes_are_read_when_the_builder_is_called() {
        var attrs = new TextAttr().bold(true);
        var node = L.text(attrs, List.of("x"));
        attrs.bold(false);

        assertEquals(true, node.attrs().get("bold"));
    }

    @Test
    void a_text_of_strings_and_a_span_renders_as_a_tree_and_as_its_xml() {
        var doc = L.document(new DocumentAttr().size("a4"), List.of(
                L.section(NoAttr, List.of(
                        L.layout(NoAttr, List.of(
                                L.text(new TextAttr().fontSize("14pt"),
                                        List.of("Total: ", L.span(new SpanAttr().bold(true), List.of("$100"))))))))));
        var engine = L.engine().setLicenseKey("test-key");

        var fromTree = engine.render(doc);
        var fromXml = engine.render(L.toXml(doc));

        assertTrue(new String(fromTree, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-"));
        assertEquals(Snapshots.sha256(fromXml), Snapshots.sha256(fromTree));
    }
}
