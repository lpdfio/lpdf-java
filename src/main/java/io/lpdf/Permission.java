// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * What a reader of an encrypted PDF may do without the owner password: all of what follows, or print (at full quality),
 * modify, copy, annotate, fill in forms, extract text for accessibility tools, and assemble (insert, delete and rotate
 * pages).
 */
public final class Permission {
    private Permission() {
    }

    public static final String ALL = "all";
    public static final String PRINT = "print";
    public static final String MODIFY = "modify";
    public static final String COPY = "copy";
    public static final String ANNOTATE = "annotate";
    public static final String FILL_FORMS = "fill-forms";
    public static final String ACCESSIBILITY = "accessibility";
    public static final String ASSEMBLE = "assemble";
}
