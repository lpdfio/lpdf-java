package io.lpdf;

/**
 * How the PDF file is written, apart from what its pages show: whether it is encrypted, and what a reader may do with
 * it, and the standards it meets. The passwords are not here: they come with the render call, since they change from
 * one render to the next. A document that asks for encryption renders only with an owner password, or when the render
 * asks to skip the encryption, as a preview does.
 */
public final class DocumentOutput extends Attributes {
    /** Attributes with none set. */
    public DocumentOutput() {
        super("output");
    }

    /**
     * Encrypts the PDF with this cipher, with the passwords given to the render call: aes-256, or rc4-128 for readers
     * that cannot open AES-256. Without it the PDF is not encrypted, and passwords given to the render call are an
     * error.
     */
    public DocumentOutput encrypt(String encrypt) {
        set("encrypt", encrypt);
        return this;
    }

    /**
     * What a reader may do without the owner password, separated by spaces: all, or any of print, modify, copy,
     * annotate, fill-forms, accessibility and assemble. A permission comes only from naming it: without allow, or with
     * allow="", nothing is allowed, and all allows everything. It needs encrypt. Readers that follow the standard honor
     * it, and a tool that does not can remove it, so use an open password when the content must stay private.
     */
    public DocumentOutput allow(String allow) {
        set("allow", allow);
        return this;
    }

    /**
     * Writes the PDF as PDF/A, for archives, courts, public bodies and banks: 2b, or 2u when its text must also be
     * searchable, or 2a when it must also be tagged, and 3a, 3b or 3u to carry attachments of any kind. The file
     * carries its fonts, the sRGB color profile and its metadata, and the render needs created_on, the date the file
     * carries. What PDF/A cannot hold is an error that says what to change: text in a built-in font such as the default
     * Helvetica (declare a font in assets), a font whose license forbids embedding, a character the font has no glyph
     * for, a CMYK color, a form field, the digits of a barcode, encrypt, and attachments in 2a, 2b or 2u. Level a, 2a
     * or 3a, tags the file as pdfua does, with what each text, heading, table, list, link and image is, but runs none
     * of PDF/UA's checks: a level-a file may have no title and no lang, and an img or a barcode without alt is
     * decoration. Write pdfua="1" beside it for a file a screen reader can use, which asks for those too. In level a, a
     * lang that is not a language tag is an error, and so is a character of Unicode's private use area, such as an icon
     * font's symbols, since level a gives each such character words of its own, which the engine does not write. With
     * 3a, 3b or 3u, an attachment named factur-x.xml, or xrechnung.xml for the XRECHNUNG profile, makes the file a
     * Factur-X or ZUGFeRD e-invoice: the engine reads the profile from BT-24 of that XML and names the invoice in the
     * file's metadata. The XML is attached as given, and its content is not checked: check it with the Factur-X
     * package's XSD and Schematron or a checker such as the Mustang CLI. A file named so in a document without 3a, 3b
     * or 3u is an error, and so are factur-x.xml and xrechnung.xml together.
     */
    public DocumentOutput pdfa(String pdfa) {
        set("pdfa", pdfa);
        return this;
    }

    /**
     * Writes the PDF as PDF/UA-1, a tagged PDF a screen reader can read: what each text, heading, table, list, link,
     * footnote and image is, in reading order, and the rest marked as decoration. It combines with pdfa. The render
     * needs created_on, the date the file carries. What PDF/UA cannot hold is an error that says what to change: no
     * title or lang on meta, an img or a barcode without alt (alt="" marks one as decoration), headings that do not
     * start at h1 or skip a level on the way down, text in a built-in font such as the default Helvetica (declare a
     * font in assets), a font whose license forbids embedding, a character the font has no glyph for, the digits of a
     * barcode, a form field, and an allow that leaves out accessibility. With pdfa 2a or 3a beside it, the file is
     * PDF/A level a and PDF/UA both.
     */
    public DocumentOutput pdfua(String pdfua) {
        set("pdfua", pdfua);
        return this;
    }

    /**
     * Writes the PDF as PDF/X-4, for a commercial printer: every page gets a TrimBox and a BleedBox, the file carries
     * the printer's color profile, named by profile, as its output intent, RGB colors and images are tied to the sRGB
     * profile, and the file carries its title, dates and version. The render needs created_on, the date the file
     * carries. Links are left out, since PDF/X allows nothing clickable on the printed page: their text prints as
     * without pdfx. What PDF/X cannot hold is an error that says what to change: no profile, a profile that is not a
     * printer profile, no title on meta, text in a built-in font such as the default Helvetica (declare a font in
     * assets), a font whose license forbids embedding, a character the font has no glyph for, the digits of a barcode,
     * a form field, a CMYK color with a profile that is not a CMYK one, encrypt, pdfa, and attachments. It combines
     * with pdfua.
     */
    public DocumentOutput pdfx(String pdfx) {
        set("pdfx", pdfx);
        return this;
    }

    /**
     * The profile of assets that a PDF/X file is made for: the ICC profile of the printing press, such as a FOGRA39 or
     * GRACoL one, which the printer gives out. It must be a printer profile, gray, RGB or CMYK. It needs pdfx, and pdfx
     * needs it.
     */
    public DocumentOutput profile(String profile) {
        set("profile", profile);
        return this;
    }

    /**
     * Writes the PDF linearized, for fast web view: page 1 comes first in the file, with hints that say where every
     * other page is, so a viewer that fetches the file from a server by byte ranges, as a browser does, shows page 1
     * while the rest downloads and fetches any other page on its own. It helps large documents read on the web, such as
     * statements, catalogues and books, and changes nothing a viewer shows. Each page then names only the fonts and
     * images it draws. With encrypt the file is encrypted first and linearized last. Any change to the file afterwards,
     * such as a stamp or a signature added by another tool, ends the linearization. It combines with every other
     * attribute of output. Without it, or with false, the file is not linearized.
     */
    public DocumentOutput linearize(boolean linearize) {
        set("linearize", linearize);
        return this;
    }

    /**
     * Refuses, in a Factur-X or ZUGFeRD e-invoice, the attachments the Factur-X spec does not allow, each with an error
     * that says what to change: an invoice file whose mime is not text/xml, an invoice file whose relationship its
     * profile does not allow (data for MINIMUM and BASIC WL; alternative, source or data for BASIC, EN 16931 and
     * EXTENDED; alternative for XRECHNUNG), and another file with no mime or with one that is not of a format the spec
     * lists: PDF, TXT, GIF, TIFF, JPG, CSV, XML, JSON, XLS, XLSX and ODS. Without it, or with false, every file is
     * written as the document gives it. It does nothing in a document without factur-x.xml or xrechnung.xml.
     */
    public DocumentOutput facturxStrict(boolean facturxStrict) {
        set("facturx-strict", facturxStrict);
        return this;
    }
}
