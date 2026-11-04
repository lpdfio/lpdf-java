package io.lpdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The data a template binds, given as objects or as JSON text. */
class DataTest {
    private static final String TEMPLATE = """
            <lpdf version="1"><document><section><layout>
              <text data-value="customer.name"/>
              <text data-value="total"/>
            </layout></section></document></lpdf>""";

    private final PdfEngine engine = L.engine();

    @Test
    void json_text_binds_as_the_same_data_given_as_objects_does() {
        var fromJson = engine.render(TEMPLATE, new RenderOptions().dataJson("""
                { "customer": { "name": "Ada Lovelace" }, "total": 800.5 }"""));
        var fromObjects = engine.render(TEMPLATE,
                new RenderOptions().data(Map.of("customer", Map.of("name", "Ada Lovelace"), "total", 800.5)));
        var other = engine.render(TEMPLATE, new RenderOptions().dataJson("""
                { "customer": { "name": "Charles Babbage" }, "total": 800.5 }"""));

        assertArrayEquals(fromObjects, fromJson);
        assertFalse(Arrays.equals(fromJson, other), "the data is not what made the PDF");
    }

    @Test
    void text_that_is_not_json_is_refused_where_it_is_set() {
        var options = new RenderOptions();

        var error = assertThrows(IllegalArgumentException.class, () -> options.dataJson("{ \"total\": }"));
        assertEquals("The data is not JSON: a value expected at 11 of 12 characters.", error.getMessage());
        for (var text : List.of("", "{} {}", "{ \"a\": 01 }", "{ \"a\": +1 }", "{ 'a': 1 }", "[1, 2,]")) {
            assertThrows(IllegalArgumentException.class, () -> options.dataJson(text), text);
        }
    }

    @Test
    void every_json_value_is_taken() {
        for (var text : List.of("{}", "[]", "null", "\"a\"", "-0.5e-3", "12345678901234567890", " { \"a\": [true, false, null] } ")) {
            new RenderOptions().dataJson(text);
        }
    }

    @Test
    void the_data_set_last_is_the_one_used() {
        var options = new RenderOptions().data(Map.of("total", 1)).dataJson("{\"total\": 2}");
        assertNull(options.data());
        assertEquals("{\"total\": 2}", options.dataJson());

        options.data(Map.of("total", 3));
        assertNull(options.dataJson());
        assertTrue(options.data() instanceof Map<?, ?>);
    }
}
