package io.lpdf;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * JSON by hand, so the SDK depends on Endive alone: a writer for the requests and the document tree, and a reader for
 * the engine's answers and the data a caller gives as JSON. Java has no JSON of its own, and a library such as Jackson
 * would clash with the version an app already has.
 */
final class Json {
    private Json() {
    }

    /** JSON text that is written as it is, such as the data a caller gives as JSON. */
    record Raw(String text) {
    }

    // ── Writer ────────────────────────────────────────────────────────────────

    /**
     * {@code value} as JSON: a map as an object, a list, a collection or an array as an array, a string, a number, a
     * boolean or null as itself, a record as an object of its components, and a node of a document or attributes as
     * their tree.
     *
     * @throws IllegalArgumentException for a value of any other type, naming it
     */
    static String write(Object value) {
        var out = new StringBuilder();
        write(out, value);
        return out.toString();
    }

    private static void write(StringBuilder out, Object value) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof Raw raw) {
            out.append(raw.text());
        } else if (value instanceof CharSequence || value instanceof Character) {
            quote(out, value.toString());
        } else if (value instanceof Boolean flag) {
            out.append(flag.booleanValue());
        } else if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte
                || value instanceof BigInteger) {
            out.append(value);
        } else if (value instanceof Double || value instanceof Float) {
            var number = ((Number) value).doubleValue();
            // JSON has no NaN or infinity: such a number goes as its text, which the engine refuses by name.
            if (Double.isFinite(number)) out.append(value);
            else quote(out, value.toString());
        } else if (value instanceof BigDecimal decimal) {
            out.append(decimal.toString());
        } else if (value instanceof KitNode node) {
            write(out, node.tree());
        } else if (value instanceof Attributes attributes) {
            write(out, attributes.values());
        } else if (value instanceof Map<?, ?> map) {
            out.append('{');
            var first = true;
            for (var entry : map.entrySet()) {
                if (!first) out.append(',');
                first = false;
                quote(out, String.valueOf(entry.getKey()));
                out.append(':');
                write(out, entry.getValue());
            }
            out.append('}');
        } else if (value instanceof Collection<?> items) {
            out.append('[');
            var first = true;
            for (var item : items) {
                if (!first) out.append(',');
                first = false;
                write(out, item);
            }
            out.append(']');
        } else if (value.getClass().isArray()) {
            out.append('[');
            for (int i = 0, n = Array.getLength(value); i < n; i++) {
                if (i > 0) out.append(',');
                write(out, Array.get(value, i));
            }
            out.append(']');
        } else if (value instanceof Enum<?> constant) {
            quote(out, constant.name());
        } else if (value instanceof Record record) {
            write(out, components(record));
        } else {
            throw new IllegalArgumentException("cannot write a " + value.getClass().getName()
                    + " as JSON: give maps, lists, arrays, records, strings, numbers, booleans and null");
        }
    }

    /** The components of a record, by name, in their order. */
    private static Map<String, Object> components(Record record) {
        var values = new LinkedHashMap<String, Object>();
        for (var component : record.getClass().getRecordComponents()) {
            try {
                var accessor = component.getAccessor();
                accessor.setAccessible(true);
                values.put(component.getName(), accessor.invoke(record));
            } catch (IllegalAccessException | RuntimeException e) {
                throw new IllegalArgumentException("cannot read the component " + component.getName() + " of "
                        + record.getClass().getName() + ": make the record public", e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException("the component " + component.getName() + " of "
                        + record.getClass().getName() + " threw " + e.getCause(), e.getCause());
            }
        }
        return values;
    }

    private static void quote(StringBuilder out, String text) {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            var c = text.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        out.append('"');
    }

    // ── Reader ────────────────────────────────────────────────────────────────

    /**
     * The JSON object of the engine's answer: strings, numbers as a {@code Long} or a {@code Double}, booleans and null,
     * and objects and arrays as maps and lists.
     *
     * @throws IllegalStateException when {@code text} is not a JSON object
     */
    static Map<String, Object> readObject(String text) {
        var reader = new Reader(text,
                message -> new IllegalStateException("The engine's answer is not the JSON it should be: " + message));
        reader.space();
        if (reader.peek() != '{') throw reader.error("an object");
        var value = reader.value();
        reader.space();
        if (reader.at < text.length()) throw reader.error("the end");
        @SuppressWarnings("unchecked")
        var object = (Map<String, Object>) value;
        return object;
    }

    /**
     * Checks that {@code text} is one JSON value, for JSON that goes to the engine as it is.
     *
     * @param what what the text is, which the error names: <code>The data</code>
     * @throws IllegalArgumentException when it is not JSON, saying where
     */
    static void check(String text, String what) {
        var reader = new Reader(text, message -> new IllegalArgumentException(what + " is not JSON: " + message));
        reader.value();
        reader.space();
        if (reader.at < text.length()) throw reader.error("the end");
    }

    /** A number as JSON writes one: no leading zeros, no plus sign, digits on both sides of a point. */
    private static final Pattern NUMBER = Pattern.compile("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?");

    private static final class Reader {
        private final String text;
        private final Function<String, RuntimeException> error;
        private int at;

        /** A reader of {@code text} whose errors {@code error} makes from what was expected and where. */
        Reader(String text, Function<String, RuntimeException> error) {
            this.text = text;
            this.error = error;
        }

        char peek() {
            return at < text.length() ? text.charAt(at) : '\0';
        }

        void space() {
            while (at < text.length() && " \t\r\n".indexOf(text.charAt(at)) >= 0) at++;
        }

        RuntimeException error(String expected) {
            return error.apply(expected + " expected at " + at + " of " + text.length() + " characters.");
        }

        Object value() {
            space();
            var c = peek();
            if (c == '{') return object();
            if (c == '[') return array();
            if (c == '"') return string();
            if (text.startsWith("true", at)) { at += 4; return Boolean.TRUE; }
            if (text.startsWith("false", at)) { at += 5; return Boolean.FALSE; }
            if (text.startsWith("null", at)) { at += 4; return null; }
            if (c == '-' || (c >= '0' && c <= '9')) return number();
            throw error("a value");
        }

        Map<String, Object> object() {
            var object = new LinkedHashMap<String, Object>();
            at++;
            space();
            if (peek() == '}') { at++; return object; }
            while (true) {
                space();
                if (peek() != '"') throw error("a name");
                var name = string();
                space();
                if (peek() != ':') throw error("':'");
                at++;
                object.put(name, value());
                space();
                var c = peek();
                at++;
                if (c == '}') return object;
                if (c != ',') throw error("',' or '}'");
            }
        }

        List<Object> array() {
            var array = new ArrayList<Object>();
            at++;
            space();
            if (peek() == ']') { at++; return array; }
            while (true) {
                array.add(value());
                space();
                var c = peek();
                at++;
                if (c == ']') return array;
                if (c != ',') throw error("',' or ']'");
            }
        }

        String string() {
            at++;
            var out = new StringBuilder();
            while (true) {
                if (at >= text.length()) throw error("the end of a string");
                var c = text.charAt(at++);
                if (c == '"') return out.toString();
                if (c != '\\') {
                    out.append(c);
                    continue;
                }
                if (at >= text.length()) throw error("an escape");
                var e = text.charAt(at++);
                switch (e) {
                    case '"', '\\', '/' -> out.append(e);
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case 'u' -> {
                        if (at + 4 > text.length()) throw error("four hex digits");
                        try {
                            out.append((char) Integer.parseInt(text.substring(at, at + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw error("four hex digits");
                        }
                        at += 4;
                    }
                    default -> throw error("an escape");
                }
            }
        }

        Number number() {
            var start = at;
            if (peek() == '-') at++;
            while (at < text.length() && "0123456789+-.eE".indexOf(text.charAt(at)) >= 0) at++;
            var number = text.substring(start, at);
            if (!NUMBER.matcher(number).matches()) {
                at = start;
                throw error("a number");
            }
            if (number.contains(".") || number.contains("e") || number.contains("E")) return Double.parseDouble(number);
            // A whole number past a long's range is still JSON.
            try {
                return Long.parseLong(number);
            } catch (NumberFormatException e) {
                return new BigInteger(number);
            }
        }
    }
}
