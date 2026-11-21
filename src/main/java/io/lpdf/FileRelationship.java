// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * How a file the PDF carries relates to the document: source, the file the document was made from; data, the data its
 * pages show, such as the XML of an e-invoice; alternative, the same content in another form; supplement, more than the
 * pages show; or unspecified.
 */
public final class FileRelationship {
    private FileRelationship() {
    }

    public static final String SOURCE = "source";
    public static final String DATA = "data";
    public static final String ALTERNATIVE = "alternative";
    public static final String SUPPLEMENT = "supplement";
    public static final String UNSPECIFIED = "unspecified";
}
