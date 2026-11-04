package io.lpdf;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The attributes of an element, set with the fluent setters of its class: {@code new TextAttr().font("heading")}. A
 * builder of {@link L} takes them as they are when it is called, so a later change to the object doesn't change the
 * node, and it throws when an attribute the element needs is missing.
 */
public abstract class Attributes {
    private final String element;
    private final String[] required;
    private final Map<String, Object> values = new LinkedHashMap<>();

    /**
     * @param element  the element's name, as the schema writes it, for the message of a missing attribute
     * @param required the attributes the element needs
     */
    Attributes(String element, String... required) {
        this.element = element;
        this.required = required;
    }

    /** Sets the attribute {@code name}, by the schema's name, or removes it when {@code value} is null. */
    final void set(String name, Object value) {
        if (value == null) values.remove(name);
        else values.put(name, value);
    }

    /** The value of the attribute {@code name}, or null when it is not set. */
    final Object get(String name) {
        return values.get(name);
    }

    /**
     * The attributes that are set, by the schema's names, as they are now: what they hold copied too, so the attributes
     * of a font in a document's assets are its own map.
     *
     * @throws IllegalArgumentException when one the element needs is not set, here or in what it holds
     */
    final Map<String, Object> values() {
        for (var name : required) {
            if (!values.containsKey(name)) {
                throw new IllegalArgumentException(
                        "<" + element + "> needs " + name + ": set it with ." + camel(name) + "(…)");
            }
        }
        var copy = new LinkedHashMap<String, Object>();
        values.forEach((name, value) -> copy.put(name, copyOf(value)));
        return Collections.unmodifiableMap(copy);
    }

    /** A value as it is now: attributes as their values, and a list or a map of them copied. */
    private static Object copyOf(Object value) {
        if (value instanceof Attributes attributes) return attributes.values();
        if (value instanceof List<?> list) return list.stream().map(Attributes::copyOf).toList();
        if (value instanceof Map<?, ?> map) {
            var copy = new LinkedHashMap<Object, Object>();
            map.forEach((key, item) -> copy.put(key, copyOf(item)));
            return Collections.unmodifiableMap(copy);
        }
        return value;
    }

    /** The name of an attribute's setter: {@code font-size} is {@code fontSize}. */
    private static String camel(String name) {
        var text = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            var c = name.charAt(i);
            if (c == '-' && i + 1 < name.length()) text.append(Character.toUpperCase(name.charAt(++i)));
            else text.append(c);
        }
        return text.toString();
    }

    /** The attributes of {@code attributes}, or none when it is null. */
    static Map<String, Object> of(Attributes attributes) {
        return attributes == null ? Map.of() : attributes.values();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + values;
    }
}
