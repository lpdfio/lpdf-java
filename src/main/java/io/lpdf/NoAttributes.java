package io.lpdf;

/**
 * The type of {@link L#NoAttr}: "no attributes". Each builder of {@link L} and {@link LMarkdown} whose attributes are
 * optional has an overload that takes it, so <code>L.column(NoAttr, List.of(…))</code> reads as it does in the other
 * SDKs. <code>img</code>, <code>barcode</code>, <code>region</code>, <code>field</code> and the others that need
 * attributes have none, so <code>L.img(NoAttr)</code> doesn't compile, where it would build a node the engine refuses.
 */
public final class NoAttributes {
    static final NoAttributes INSTANCE = new NoAttributes();

    private NoAttributes() {
    }

    @Override
    public String toString() {
        return "NoAttr";
    }
}
