// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A run of code inside a text: its words in Courier on a light tint, the tint drawn behind them. It is the inline twin
 * of code, and like a span it is not broken inside, so it goes to the next line whole, and a run longer than a whole
 * line breaks at the last character that fits, so nothing runs past the box. White space in it collapses as in the text
 * around it.
 */
public final class CodeSpanAttr extends Attributes {
    /** Attributes with none set. */
    public CodeSpanAttr() {
        super("code-span");
    }

    public CodeSpanAttr font(String font) {
        set("font", font);
        return this;
    }

    public CodeSpanAttr color(String color) {
        set("color", color);
        return this;
    }

    /** The tint drawn behind the run. Default #f3f4f6. */
    public CodeSpanAttr background(String background) {
        set("background", background);
        return this;
    }
}
