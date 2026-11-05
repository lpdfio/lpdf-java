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

/**
 * JSON by hand, so the SDK depends on Endive alone: a writer for the requests and the document tree, and a reader for
 * the engine's answers and the data a caller gives as JSON. Java has no JSON of its own, and a library such as Jackson
 * would clash with the version an app already has.
 */
final class Json {
    private Json() {
    }

    /**
     * The deepest the data of a render may nest its objects and arrays, the outermost included. The engine reads its
     * request with serde_json's limit, which stops at the 128th level, and the request is the first of them: data
     * nested 127 levels deep stops the render with "recursion limit exceeded". The SDK refuses it where it is given.
     */
    static final int MAX_DATA_DEPTH = 126;

    /** The deepest an answer of the engine nests, with room to spare: its answers are flat. */
    private static final int MAX_ANSWER_DEPTH = 16;

    /** A number with more characters than this may be past the largest a 64-bit float holds, without an exponent. */
    private static final int MOST_SAFE_DIGITS = 300;

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    /** JSON text that is written as it is, such as the data a caller gives as JSON, checked by {@link #checkData}. */
    record Raw(String text) {
    }

    /** The data of a render, written as any value is, within the depth the engine reads and with finite numbers. */
    record Data(Object value) {
    }

    // ── Writer ────────────────────────────────────────────────────────────────

    /**
     * {@code value} as JSON: a map as an object, a list, a collection or an array as an array, a string, a number, a
     * boolean or null as itself, a record as an object of its components, and a node of a document or attributes as
     * their tree.
     *
     * @throws IllegalArgumentException for a value of any other type, naming it, or for {@link Data} nested deeper than
     *                                  the engine reads or holding a number that is not finite
     */
    static String write(Object value) {
        var out = new StringBuilder();
        write(out, value, false, 0);
        return out.toString();
    }

    /** Writes {@code value}, which is in the data when {@code data} is set, {@code depth} objects and arrays deep. */
    private static void write(StringBuilder out, Object value, boolean data, int depth) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof Raw raw) {
            out.append(raw.text());
        } else if (value instanceof Data inData) {
            write(out, inData.value(), true, 0);
        } else if (value instanceof CharSequence || value instanceof Character) {
            quote(out, value.toString());
        } else if (value instanceof Boolean flag) {
            out.append(flag.booleanValue());
        } else if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte
                || value instanceof BigInteger) {
            out.append(value);
        } else if (value instanceof Double || value instanceof Float) {
            var number = ((Number) value).doubleValue();
            if (Double.isFinite(number)) {
                out.append(value);
            } else if (data) {
                throw new IllegalArgumentException("The data holds " + value + ", which JSON has no number for");
            } else {
                // JSON has no NaN or infinity: such an attribute goes as its text, which the engine refuses by name.
                quote(out, value.toString());
            }
        } else if (value instanceof BigDecimal decimal) {
            out.append(decimal.toString());
        } else if (value instanceof KitNode node) {
            write(out, node.tree(), data, depth);
        } else if (value instanceof Attributes attributes) {
            write(out, attributes.values(), data, depth);
        } else if (value instanceof Map<?, ?> map) {
            var inner = nest(data, depth);
            out.append('{');
            var first = true;
            for (var entry : map.entrySet()) {
                if (!first) out.append(',');
                first = false;
                quote(out, String.valueOf(entry.getKey()));
                out.append(':');
                write(out, entry.getValue(), data, inner);
            }
            out.append('}');
        } else if (value instanceof Collection<?> items) {
            var inner = nest(data, depth);
            out.append('[');
            var first = true;
            for (var item : items) {
                if (!first) out.append(',');
                first = false;
                write(out, item, data, inner);
            }
            out.append(']');
        } else if (value.getClass().isArray()) {
            var inner = nest(data, depth);
            out.append('[');
            for (int i = 0, n = Array.getLength(value); i < n; i++) {
                if (i > 0) out.append(',');
                write(out, Array.get(value, i), data, inner);
            }
            out.append(']');
        } else if (value instanceof Enum<?> constant) {
            quote(out, constant.name());
        } else if (value instanceof Record record) {
            write(out, components(record), data, depth);
        } else {
            throw new IllegalArgumentException("cannot write a " + value.getClass().getName()
                    + " as JSON: give maps, lists, arrays, records, strings, numbers, booleans and null");
        }
    }

    /**
     * The depth inside an object or array that opens at {@code depth}.
     *
     * @throws IllegalArgumentException in the data, past the depth the engine reads: data that holds itself ends here
     */
    private static int nest(boolean data, int depth) {
        if (data && depth == MAX_DATA_DEPTH) {
            throw new IllegalArgumentException("The data nests its maps, lists, arrays and records more than "
                    + MAX_DATA_DEPTH + " levels deep, past what the engine reads, or holds itself");
        }
        return depth + 1;
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

    /** Writes {@code text} as a JSON string: the runs of characters that need no escape are copied whole. */
    private static void quote(StringBuilder out, String text) {
        out.append('"');
        var run = 0;
        for (int i = 0, n = text.length(); i < n; i++) {
            var c = text.charAt(i);
            if (c >= 0x20 && c != '"' && c != '\\') continue;
            out.append(text, run, i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> out.append("\\u00").append(HEX[c >> 4]).append(HEX[c & 0xf]);
            }
            run = i + 1;
        }
        out.append(text, run, text.length()).append('"');
    }

    // ── Reader ────────────────────────────────────────────────────────────────

    /**
     * The JSON object of the engine's answer: strings, numbers as a {@code Long}, a {@code BigInteger} or a
     * {@code Double}, booleans and null, and objects and arrays as maps and lists.
     *
     * @throws IllegalStateException when {@code text} is not a JSON object
     */
    static Map<String, Object> readObject(String text) {
        var reader = new Reader(text, true, MAX_ANSWER_DEPTH, "The engine's answer", IllegalStateException::new);
        reader.space();
        if (reader.peek() != '{') throw reader.expected("an object");
        var value = reader.value(0);
        reader.end();
        @SuppressWarnings("unchecked")
        var object = (Map<String, Object>) value;
        return object;
    }

    /**
     * Checks that {@code text} is one JSON value the engine reads as the data of a render, which then goes to the
     * engine as it is: JSON as strict as the engine reads it, nested {@link #MAX_DATA_DEPTH} levels at most, with no
     * number past the largest a 64-bit float holds. Nothing is built.
     *
     * @throws IllegalArgumentException when the engine would refuse it, saying where
     */
    static void checkData(String text) {
        var reader = new Reader(text, false, MAX_DATA_DEPTH, "The data", IllegalArgumentException::new);
        reader.value(0);
        reader.end();
    }

    /**
     * A reader of JSON text, as strict as the engine's: RFC 8259's JSON, with no control character in a string and no
     * half of a character, escaped or not. It builds the values it reads, or only checks them, which allocates
     * nothing. A string with nothing escaped in it is cut from the text whole.
     */
    private static final class Reader {
        private final String text;
        private final int length;
        private final boolean build;
        private final int maxDepth;
        private final String what;
        private final Function<String, RuntimeException> error;
        private int at;

        /**
         * @param build    whether to build the values, or only check them
         * @param maxDepth the deepest the objects and arrays may nest, the outermost included
         * @param what     what the text is, which its errors name: <code>The data</code>
         * @param error    the exception of an error, from its message
         */
        Reader(String text, boolean build, int maxDepth, String what, Function<String, RuntimeException> error) {
            this.text = text;
            this.length = text.length();
            this.build = build;
            this.maxDepth = maxDepth;
            this.what = what;
            this.error = error;
        }

        char peek() {
            return at < length ? text.charAt(at) : '\0';
        }

        void space() {
            while (at < length) {
                var c = text.charAt(at);
                if (c != ' ' && c != '\n' && c != '\r' && c != '\t') return;
                at++;
            }
        }

        /** Checks that only spaces follow the value. */
        void end() {
            space();
            if (at < length) throw expected("the end");
        }

        // ── Values ────────────────────────────────────────────────────────────

        /** The value at the reader, inside {@code depth} objects and arrays: null when the values are not built. */
        Object value(int depth) {
            space();
            var c = peek();
            if (c == '{') return object(depth + 1);
            if (c == '[') return array(depth + 1);
            if (c == '"') return string();
            if (c == 't') return literal("true", Boolean.TRUE);
            if (c == 'f') return literal("false", Boolean.FALSE);
            if (c == 'n') return literal("null", null);
            if (c == '-' || isDigit(c)) return number();
            throw expected("a value");
        }

        private Object literal(String word, Object value) {
            if (!text.startsWith(word, at)) throw expected("a value");
            at += word.length();
            return value;
        }

        private Map<String, Object> object(int depth) {
            if (depth > maxDepth) throw tooDeep();
            var object = build ? new LinkedHashMap<String, Object>() : null;
            at++;
            space();
            if (peek() == '}') {
                at++;
                return object;
            }
            while (true) {
                space();
                if (peek() != '"') throw expected("a name");
                var name = string();
                space();
                if (peek() != ':') throw expected("':'");
                at++;
                var value = value(depth);
                if (build) object.put(name, value);
                space();
                var c = peek();
                if (c != ',' && c != '}') throw expected("',' or '}'");
                at++;
                if (c == '}') return object;
            }
        }

        private List<Object> array(int depth) {
            if (depth > maxDepth) throw tooDeep();
            var array = build ? new ArrayList<Object>() : null;
            at++;
            space();
            if (peek() == ']') {
                at++;
                return array;
            }
            while (true) {
                var value = value(depth);
                if (build) array.add(value);
                space();
                var c = peek();
                if (c != ',' && c != ']') throw expected("',' or ']'");
                at++;
                if (c == ']') return array;
            }
        }

        // ── Strings ───────────────────────────────────────────────────────────

        /** The string at the reader, cut from the text whole when nothing in it is escaped. */
        private String string() {
            var start = ++at;
            while (at < length) {
                var c = text.charAt(at);
                if (c == '"') {
                    at++;
                    return build ? text.substring(start, at - 1) : null;
                }
                if (c == '\\') return escaped(start);
                character(c);
            }
            throw expected("the end of a string");
        }

        /** The rest of a string with escapes in it, from {@code start}, its first character. */
        private String escaped(int start) {
            var out = build ? new StringBuilder(at - start + 16).append(text, start, at) : null;
            while (at < length) {
                var c = text.charAt(at);
                if (c == '"') {
                    at++;
                    return build ? out.toString() : null;
                }
                if (c != '\\') {
                    var from = at;
                    character(c);
                    if (build) out.append(text, from, at);
                    continue;
                }
                var escape = at;
                if (++at >= length) throw expected("an escape");
                var e = text.charAt(at++);
                switch (e) {
                    case '"', '\\', '/' -> append(out, e);
                    case 'b' -> append(out, '\b');
                    case 'f' -> append(out, '\f');
                    case 'n' -> append(out, '\n');
                    case 'r' -> append(out, '\r');
                    case 't' -> append(out, '\t');
                    case 'u' -> {
                        var unit = hex(escape);
                        if (Character.isLowSurrogate(unit)) throw loneSurrogate(unit, escape);
                        append(out, unit);
                        if (Character.isHighSurrogate(unit)) {
                            // Its other half must follow it, as an escape too.
                            if (!text.startsWith("\\u", at)) throw loneSurrogate(unit, escape);
                            var second = at;
                            at += 2;
                            var low = hex(second);
                            if (!Character.isLowSurrogate(low)) throw loneSurrogate(unit, escape);
                            append(out, low);
                        }
                    }
                    default -> {
                        at = escape;
                        throw expected("an escape");
                    }
                }
            }
            throw expected("the end of a string");
        }

        /** Steps over the character {@code c} of a string, at the reader: one char, or two for a surrogate pair. */
        private void character(char c) {
            if (c < 0x20) {
                throw fail("is not JSON: a control character, U+" + hex4(c) + ", at " + at + " of " + length
                        + " characters, which a string holds only escaped, as \\u" + hex4(c) + ".");
            }
            if (Character.isHighSurrogate(c) && at + 1 < length && Character.isLowSurrogate(text.charAt(at + 1))) {
                at += 2;
            } else if (Character.isSurrogate(c)) {
                throw loneSurrogate(c, at);
            } else {
                at++;
            }
        }

        /** The UTF-16 unit of the four hex digits after the <code>\\u</code> at {@code escape}, which ends at the reader. */
        private char hex(int escape) {
            if (at + 4 > length) {
                at = escape;
                throw expected("\\u and four hex digits");
            }
            var unit = 0;
            for (var i = 0; i < 4; i++) {
                var digit = hexDigit(text.charAt(at + i));
                if (digit < 0) {
                    at = escape;
                    throw expected("\\u and four hex digits");
                }
                unit = unit << 4 | digit;
            }
            at += 4;
            return (char) unit;
        }

        private void append(StringBuilder out, char c) {
            if (build) out.append(c);
        }

        // ── Numbers ───────────────────────────────────────────────────────────

        /** The number at the reader: a {@code Long}, a {@code BigInteger} or a {@code Double}, or null when not built. */
        private Number number() {
            var start = at;
            if (peek() == '-') at++;
            if (peek() == '0') at++;
            else digits();
            var whole = true;
            if (peek() == '.') {
                at++;
                digits();
                whole = false;
            }
            var exponent = peek() == 'e' || peek() == 'E';
            if (exponent) {
                at++;
                if (peek() == '+' || peek() == '-') at++;
                digits();
                whole = false;
            }
            // The engine reads a number it can't hold as a whole one as a 64-bit float, and refuses one past the
            // largest, which only an exponent or some 300 digits can make.
            Double real = null;
            if (exponent || at - start > MOST_SAFE_DIGITS) {
                real = Double.parseDouble(text.substring(start, at));
                if (Double.isInfinite(real)) {
                    throw fail("has a number past the largest the engine holds, about 1.8e308, at " + start + " of "
                            + length + " characters.");
                }
            }
            if (!build) return null;
            if (!whole) return real != null ? real : Double.valueOf(text.substring(start, at));
            var digits = text.substring(start, at);
            // A whole number past a long's range is still JSON.
            try {
                return Long.parseLong(digits);
            } catch (NumberFormatException e) {
                return new BigInteger(digits);
            }
        }

        /** Steps over one or more digits. */
        private void digits() {
            if (!isDigit(peek())) throw expected("a digit");
            do at++;
            while (isDigit(peek()));
        }

        // ── Errors ────────────────────────────────────────────────────────────

        RuntimeException expected(String expected) {
            return fail("is not JSON: " + expected + " expected at " + at + " of " + length + " characters.");
        }

        private RuntimeException tooDeep() {
            return fail("nests its objects and arrays more than " + maxDepth + " levels deep, past what the engine "
                    + "reads: the next one opens at " + at + " of " + length + " characters.");
        }

        private RuntimeException loneSurrogate(char unit, int position) {
            return fail("is not JSON: half of a character, U+" + hex4(unit) + ", at " + position + " of " + length
                    + " characters, without its other half.");
        }

        private RuntimeException fail(String message) {
            return error.apply(what + " " + message);
        }

        private static boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }

        private static int hexDigit(char c) {
            if (c >= '0' && c <= '9') return c - '0';
            if (c >= 'a' && c <= 'f') return c - 'a' + 10;
            if (c >= 'A' && c <= 'F') return c - 'A' + 10;
            return -1;
        }

        private static String hex4(char c) {
            return String.format("%04X", (int) c);
        }
    }
}
