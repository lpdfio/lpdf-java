// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * The words of the <code>allow</code> attribute of the output, separated by spaces: what a reader of an encrypted PDF
 * may do.
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
