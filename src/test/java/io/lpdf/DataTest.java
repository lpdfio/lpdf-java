package io.lpdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** The data a template binds, given as objects (maps, lists, records) or as JSON text. */
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

    /**
     * Text the SDK takes is text the engine reads, and text it refuses is text the engine would refuse: the engine is
     * sent each text as it is, past the SDK's check.
     */
    @ParameterizedTest
    @MethodSource("dataTexts")
    void json_text_is_refused_where_it_is_set_when_the_engine_would_refuse_it(String json, boolean read) {
        assertEquals(read, engineReads(json), "the engine");
        assertEquals(read, sdkTakes(json), "the SDK");
    }

    static Stream<Arguments> dataTexts() {
        return Stream.of(
                Arguments.of("{\"a\":\"\\ud83d\\ude00\"}", true),
                Arguments.of("{\"a\":\"\uD83D\uDE00\"}", true),
                Arguments.of("{\"a\":\"\\u00E9\\/\"}", true),
                Arguments.of("{\"a\":\"\u007f\"}", true),
                Arguments.of("{\"a\":-0, \"b\":1E+2, \"c\":1e-400, \"d\":1.7976931348623157e308}", true),
                Arguments.of("{\"a\":123456789012345678901234567890}", true),
                Arguments.of("{\"a\":1, \"a\":2}", true),
                Arguments.of(JsonTest.nested(Json.MAX_DATA_DEPTH), true),
                Arguments.of("{\"a\":\"\t\"}", false),
                Arguments.of("{\"a\":\"line 1\nline 2\"}", false),
                Arguments.of("{\"a\":\"\0\"}", false),
                Arguments.of("{\"a\":\"\\u+041\"}", false),
                Arguments.of("{\"a\":\"\\u004\"}", false),
                Arguments.of("{\"a\":\"\\x41\"}", false),
                Arguments.of("{\"a\":\"\\ud800\"}", false),
                Arguments.of("{\"a\":\"\\udc00\"}", false),
                Arguments.of("{\"a\":\"\\ud800\\u0041\"}", false),
                Arguments.of("{\"a\":1e400}", false),
                Arguments.of("{\"a\":-1.8e308}", false),
                Arguments.of("{\"a\":" + "1".repeat(400) + "}", false),
                Arguments.of("{\"a\":1.}", false),
                Arguments.of("{\"a\":.5}", false),
                Arguments.of("\uFEFF{}", false),
                Arguments.of("{}\u00a0", false),
                Arguments.of(JsonTest.nested(Json.MAX_DATA_DEPTH + 1), false));
    }

    @Test
    void data_nested_as_deep_as_the_engine_reads_renders_given_as_text_and_as_objects() {
        var fromJson = engine.render(TEMPLATE, new RenderOptions().dataJson(JsonTest.nested(Json.MAX_DATA_DEPTH)));
        var fromObjects = engine.render(TEMPLATE,
                new RenderOptions().data(JsonTest.nestedData(Json.MAX_DATA_DEPTH)));

        assertArrayEquals(fromJson, fromObjects);
    }

    /** Whether the engine reads {@code json} as the data of a render, sent to it as it is. */
    private static boolean engineReads(String json) {
        var request = new LinkedHashMap<String, Object>();
        request.put("method", "render_pdf");
        request.put("key", "");
        request.put("input", TEMPLATE);
        request.put("fonts", Map.of());
        request.put("data", new Json.Raw(json));
        try {
            new WasmRunner(EngineOptions.DEFAULT_TIMEOUT, EngineOptions.DEFAULT_MAX_MEMORY_MB).pdf(request);
            return true;
        } catch (EngineException e) {
            assertTrue(e.getMessage().startsWith("request parse error: "), e.getMessage());
            return false;
        }
    }

    private static boolean sdkTakes(String json) {
        try {
            new RenderOptions().dataJson(json);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Test
    void every_json_value_is_taken() {
        for (var text : List.of("{}", "[]", "null", "\"a\"", "-0.5e-3", "12345678901234567890", " { \"a\": [true, false, null] } ")) {
            new RenderOptions().dataJson(text);
        }
    }

    record Item(String label) {
    }

    record Items(List<Item> items, boolean premium) {
    }

    @Test
    void a_record_binds_as_an_object_of_its_components() {
        var xml = """
                <lpdf version="1"><document><section><layout>
                  <column data-source="items"><text data-value="label">Fallback</text></column>
                  <text data-if="premium">Premium only</text>
                </layout></section></document></lpdf>""";

        var fromRecord = engine.render(xml, new RenderOptions().data(
                new Items(List.of(new Item("Alpha"), new Item("Beta"), new Item("Gamma")), false)));
        var fromJson = engine.render(xml, new RenderOptions().dataJson("""
                {"items": [{"label": "Alpha"}, {"label": "Beta"}, {"label": "Gamma"}], "premium": false}"""));
        var premium = engine.render(xml, new RenderOptions().data(
                new Items(List.of(new Item("Alpha"), new Item("Beta"), new Item("Gamma")), true)));

        assertArrayEquals(fromJson, fromRecord);
        assertFalse(Arrays.equals(fromRecord, premium), "data-if read no data");
    }

    @Test
    void a_template_renders_its_own_content_without_data() {
        var fallback = engine.render(TEMPLATE);
        var bound = engine.render(TEMPLATE, new RenderOptions().dataJson("{\"customer\": {\"name\": \"Ada\"}, \"total\": 1}"));

        assertFalse(Arrays.equals(fallback, bound));
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
