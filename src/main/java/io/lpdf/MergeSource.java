package io.lpdf;

/**
 * One file for {@link PdfEngine#merge}: its name and the PDF's bytes. It needs <code>name</code> and <code>pdf</code>.
 */
public final class MergeSource extends Attributes {
    /** A source with nothing set. */
    public MergeSource() {
        super("merge-source", "name", "pdf");
    }

    /** The file's name, such as <code>contract-b.pdf</code>. Every error about the file names it. */
    public MergeSource name(String name) {
        set("name", name);
        return this;
    }

    /** The PDF's bytes. */
    public MergeSource pdf(byte[] pdf) {
        set("pdf", pdf);
        return this;
    }
}
