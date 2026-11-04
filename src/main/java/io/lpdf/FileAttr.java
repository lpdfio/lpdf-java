// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A file the PDF carries, under the name a reader shows. The SDK reads it from src, or it was loaded on the engine
 * under ref or, with no ref, under its own name. A file with no bytes is an error, and so are two files with the same
 * name.
 */
public final class FileAttr extends Attributes {
    /** Attributes with none set. */
    public FileAttr() {
        super("file", "name");
    }

    /**
     * The name a reader shows the file under and saves it as, such as invoice.xml. Each file of the document has a name
     * of its own.
     */
    public FileAttr name(String name) {
        set("name", name);
        return this;
    }

    /** A path the SDK reads the file from, when the file was not loaded on the engine. */
    public FileAttr src(String src) {
        set("src", src);
        return this;
    }

    /** The key the file was loaded under on the engine, when that is not its name. */
    public FileAttr ref(String ref) {
        set("ref", ref);
        return this;
    }

    /**
     * The type of the file, such as text/xml, application/pdf or text/csv. Defaults to application/octet-stream, which
     * says only that it is a file, and to text/xml for the invoice of a Factur-X e-invoice.
     */
    public FileAttr mime(String mime) {
        set("mime", mime);
        return this;
    }

    /**
     * How the file relates to the document: source, data, alternative, supplement or unspecified, the default. The
     * invoice of a Factur-X e-invoice defaults to data for the MINIMUM and BASIC WL profiles, and to alternative for
     * the others, as the Factur-X spec asks.
     */
    public FileAttr relationship(String relationship) {
        set("relationship", relationship);
        return this;
    }

    /** A line about the file, which a reader shows beside its name. */
    public FileAttr description(String description) {
        set("description", description);
        return this;
    }
}
