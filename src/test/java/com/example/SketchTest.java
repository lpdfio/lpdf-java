package com.example;

import static io.lpdf.L.NoAttr;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lpdf.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The invoice of the SDK's design, as an app outside the package writes it: <code>import io.lpdf.*</code>, <code>L.</code>
 * on every builder, fluent attributes or <code>NoAttr</code>, and children as <code>List.of</code>.
 */
class SketchTest {
    @Test
    void the_invoice_of_the_design_compiles_and_renders() {
        var doc = L.document(new DocumentAttr().size("a4"), List.of(
            L.section(NoAttr, List.of(
                L.layout(new LayoutAttr().margin("40pt"), List.of(
                    L.column(new ColumnAttr().gap("m"), List.of(
                        L.text(new TextAttr().font("Helvetica-Bold").fontSize("xl"), List.of("Invoice 2026-114")),
                        L.table(new TableAttr().cols("1fr 80pt"), List.of(
                            L.tr(NoAttr, List.of(
                                L.td(NoAttr, List.of(L.text(NoAttr, List.of("Design assets")))),
                                L.td(new TdAttr().align("end"), List.of(L.text(NoAttr, List.of("$800.00"))))
                            ))
                        ))
                    ))
                ))
            ))
        ));

        byte[] pdf = L.engine().render(doc);

        assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));
    }
}
