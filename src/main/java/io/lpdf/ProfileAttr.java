// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * Declares an ICC color profile that the profile attribute of output names: the profile of the printing press a PDF/X
 * file is made for, which the printer gives out. The SDK reads the file from src, or the profile was loaded on the
 * engine under ref or, with no ref, under its own name. The engine bundles no press profile.
 */
public final class ProfileAttr extends Attributes {
    /** Attributes with none set. */
    public ProfileAttr() {
        super("profile", "name");
    }

    /**
     * The name that the profile attribute of output uses to pick this profile: lowercase letters, digits and -,
     * starting with a letter.
     */
    public ProfileAttr name(String name) {
        set("name", name);
        return this;
    }

    /** The key the profile was loaded under on the engine, when that is not its name. */
    public ProfileAttr ref(String ref) {
        set("ref", ref);
        return this;
    }

    /** A path the SDK reads the ICC profile from, when the profile was not loaded on the engine. */
    public ProfileAttr src(String src) {
        set("src", src);
        return this;
    }
}
