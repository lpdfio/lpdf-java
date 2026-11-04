package io.lpdf;

import static io.lpdf.L.NoAttr;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * {@code NoAttr}, for an element written with no attributes. Java has no implicit conversion, so a builder takes it in
 * an overload of its own. The builders whose attributes the .NET SDK can't leave out have none: {@code L.table} needs
 * {@code cols}, and a layer's shapes their place.
 */
class NoAttributesTest {
    /** The builders that take no {@code NoAttr}, as the .NET SDK's attributes of them are not optional. */
    private static final Set<String> NEED_ATTRIBUTES = Set.of("img", "shape", "barcode", "region", "field", "bookmark",
            "link", "table", "rect", "line", "ellipse", "circle", "path", "textAt", "imgAt", "barcodeAt");

    @Test
    void every_builder_takes_no_attr_but_those_whose_attributes_cannot_be_left_out() {
        var wrong = new ArrayList<String>();
        for (var builder : List.of(L.class, LMarkdown.class)) {
            for (var method : builder.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers()) || method.getParameterCount() == 0) continue;
                if (!method.getParameterTypes()[0].getSimpleName().endsWith("Attr")) continue;
                var name = builder.getSimpleName() + "." + method.getName();
                var hasOverload = noAttrOverload(builder, method) != null;
                var needsAttributes = builder == L.class && NEED_ATTRIBUTES.contains(method.getName());
                if (hasOverload == needsAttributes) {
                    wrong.add(name + (hasOverload ? " takes NoAttr" : " takes no NoAttr"));
                }
            }
        }
        assertEquals(List.of(), wrong);
    }

    @Test
    void no_attr_builds_the_same_container_as_empty_attributes() {
        var withNoAttr = L.column(NoAttr, List.of(L.text(NoAttr, List.of("x"))));
        var withEmpty = L.column(new ColumnAttr(), List.of(L.text(new TextAttr(), List.of("x"))));

        assertEquals(Map.of(), withNoAttr.attrs());
        assertEquals(withEmpty.toString(), withNoAttr.toString());
    }

    @Test
    void no_attr_builds_the_same_section_as_empty_attributes() {
        var withNoAttr = L.section(NoAttr, List.of(L.layout(NoAttr, List.of(L.divider(NoAttr)))));
        var withEmpty = L.section(new SectionAttr(), List.of(L.layout(new LayoutAttr(), List.of(L.divider(new DividerAttr())))));

        assertEquals(Map.of(), withNoAttr.attrs());
        assertEquals(withEmpty.toString(), withNoAttr.toString());
    }

    /** The overload of {@code method} that takes {@code NoAttr} in place of its attributes, or null. */
    private static Method noAttrOverload(Class<?> builder, Method method) {
        var parameters = method.getParameterTypes().clone();
        parameters[0] = NoAttributes.class;
        try {
            return builder.getMethod(method.getName(), parameters);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
