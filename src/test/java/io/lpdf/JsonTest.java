package io.lpdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The JSON the SDK writes and reads by hand: the requests and the data it sends, and the engine's answers. */
class JsonTest {
    @Test
    void an_answer_is_read_with_its_escapes_decoded() {
        var answer = Json.readObject("{\"a\": \"say \\\"hi\\\"\\n\\u00e9\\ud83d\\ude00\\/\", \"b\": [1, -2.5, true, null],"
                + " \"c\": 12345678901234567890, \"d\": {}}");

        assertEquals("say \"hi\"\n\u00e9\uD83D\uDE00/", answer.get("a"));
        assertEquals(Arrays.asList(1L, -2.5, true, null), answer.get("b"));
        assertEquals(new BigInteger("12345678901234567890"), answer.get("c"));
        assertEquals(Map.of(), answer.get("d"));
    }

    @Test
    void a_string_with_nothing_escaped_is_read_as_it_is() {
        var pdf = "JVBERi0xLjcK".repeat(100_000);

        assertEquals(pdf, Json.readObject("{\"pdf\":\"" + pdf + "\"}").get("pdf"));
    }

    @Test
    void an_answer_that_is_not_a_json_object_is_refused() {
        var error = assertThrows(IllegalStateException.class, () -> Json.readObject("[1]"));

        assertEquals("The engine's answer is not JSON: an object expected at 0 of 3 characters.", error.getMessage());
    }

    @Test
    void a_control_character_in_a_string_is_refused_naming_it_and_its_place() {
        var error = assertThrows(IllegalArgumentException.class, () -> Json.checkData("{\"a\":\"x\ty\"}"));

        assertEquals("The data is not JSON: a control character, U+0009, at 7 of 11 characters, which a string holds "
                + "only escaped, as \\u0009.", error.getMessage());
    }

    @Test
    void half_of_a_character_is_refused_escaped_or_not() {
        var escaped = assertThrows(IllegalArgumentException.class, () -> Json.checkData("{\"a\":\"\\ud800\"}"));
        var raw = assertThrows(IllegalArgumentException.class, () -> Json.checkData("{\"a\":\"\uD800\"}"));

        assertEquals("The data is not JSON: half of a character, U+D800, at 6 of 14 characters, without its other half.",
                escaped.getMessage());
        assertEquals("The data is not JSON: half of a character, U+D800, at 6 of 9 characters, without its other half.",
                raw.getMessage());
    }

    @Test
    void a_number_past_the_largest_float_is_refused() {
        var error = assertThrows(IllegalArgumentException.class, () -> Json.checkData("{\"a\":1e400}"));

        assertEquals("The data has a number past the largest the engine holds, about 1.8e308, at 5 of 11 characters.",
                error.getMessage());
    }

    @Test
    void data_nested_past_the_limit_is_refused_naming_it() {
        Json.checkData(nested(Json.MAX_DATA_DEPTH));
        var error = assertThrows(IllegalArgumentException.class, () -> Json.checkData(nested(127)));

        assertEquals("The data nests its objects and arrays more than 126 levels deep, past what the engine reads: the "
                + "next one opens at 130 of 258 characters.", error.getMessage());
    }

    /** A request body of nothing but brackets is refused where it is set, not with a StackOverflowError. */
    @Test
    void data_nested_far_past_the_limit_is_refused_without_running_out_of_stack() {
        var text = "[".repeat(100_000);

        var error = assertThrows(IllegalArgumentException.class, () -> Json.checkData(text));

        assertEquals("The data nests its objects and arrays more than 126 levels deep, past what the engine reads: the "
                + "next one opens at 126 of 100000 characters.", error.getMessage());
    }

    @Test
    void a_string_is_written_with_what_json_escapes_escaped() {
        assertEquals("\"a\\\"b\\\\c\\n\\t\\u0001\\u001f\u007f\u00e9\uD83D\uDE00/\"",
                Json.write("a\"b\\c\n\t\u0001\u001f\u007f\u00e9\uD83D\uDE00/"));
    }

    @Test
    void data_is_written_within_the_depth_the_engine_reads() {
        assertEquals(nested(Json.MAX_DATA_DEPTH), Json.write(new Json.Data(nestedData(Json.MAX_DATA_DEPTH))));

        var error = assertThrows(IllegalArgumentException.class, () -> Json.write(new Json.Data(nestedData(127))));

        assertEquals("The data nests its maps, lists, arrays and records more than 126 levels deep, past what the "
                + "engine reads, or holds itself", error.getMessage());
    }

    @Test
    void data_that_holds_itself_is_refused_without_running_out_of_stack() {
        var data = new HashMap<String, Object>();
        data.put("self", data);

        assertThrows(IllegalArgumentException.class, () -> Json.write(new Json.Data(data)));
    }

    @Test
    void a_number_json_has_none_for_is_refused_in_the_data() {
        var error = assertThrows(IllegalArgumentException.class,
                () -> Json.write(new Json.Data(Map.of("total", Double.NaN))));

        assertEquals("The data holds NaN, which JSON has no number for", error.getMessage());
    }

    /** JSON of {@code depth} objects and arrays, one inside the other: an object, then arrays. */
    static String nested(int depth) {
        return "{\"a\":" + "[".repeat(depth - 1) + "]".repeat(depth - 1) + "}";
    }

    /** The data of {@link #nested}, as maps and lists. */
    static Map<String, Object> nestedData(int depth) {
        Object value = List.of();
        for (var i = 2; i < depth; i++) value = List.of(value);
        return Map.of("a", value);
    }
}
