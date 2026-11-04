// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * Declares a font that text can be set in by name, with the font attribute. A built-in PDF font is named by core; any
 * other font is a file the SDK reads from src, or one loaded on the engine under ref or, with no ref, under the font's
 * own name.
 */
public final class FontAttr extends Attributes {
    /** Attributes with none set. */
    public FontAttr() {
        super("font", "name");
    }

    /**
     * The name that the font attribute of a text uses to pick this font: lowercase letters, digits and -, starting with
     * a letter.
     */
    public FontAttr name(String name) {
        set("name", name);
        return this;
    }

    /** A built-in PDF font, which needs no file. */
    public FontAttr core(String core) {
        set("core", core);
        return this;
    }

    /** The key the font was loaded under on the engine, when that is not its name. */
    public FontAttr ref(String ref) {
        set("ref", ref);
        return this;
    }

    /** A path the SDK reads the font file from, when the font was not loaded on the engine. */
    public FontAttr src(String src) {
        set("src", src);
        return this;
    }
}
