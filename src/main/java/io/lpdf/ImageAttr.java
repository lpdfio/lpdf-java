// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * Declares an image that an img refers to by name. The SDK reads the file from src, or the image was loaded on the
 * engine under ref or, with no ref, under its own name.
 */
public final class ImageAttr extends Attributes {
    /** Attributes with none set. */
    public ImageAttr() {
        super("image", "name");
    }

    /**
     * The name that the name attribute of an img uses to pick this image: lowercase letters, digits and -, starting
     * with a letter.
     */
    public ImageAttr name(String name) {
        set("name", name);
        return this;
    }

    /** The key the image was loaded under on the engine, when that is not its name. */
    public ImageAttr ref(String ref) {
        set("ref", ref);
        return this;
    }

    /** A path the SDK reads the image file from, when the image was not loaded on the engine. */
    public ImageAttr src(String src) {
        set("src", src);
        return this;
    }
}
