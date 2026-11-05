// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A QR code, a Code 128 or an EAN-13. It is never stretched. With only a width or a height, the other follows the
 * barcode's proportions. With both, they are a maximum: the barcode takes the largest size that fits in them, and its
 * box becomes that size, which the parent's align places. To put barcodes in slots of one size, wrap each in a frame of
 * that size.
 */
public final class BarcodeAttr extends Attributes {
    /** Attributes with none set. */
    public BarcodeAttr() {
        super("barcode", "type", "data");
    }

    public BarcodeAttr type(String type) {
        set("type", type);
        return this;
    }

    public BarcodeAttr data(String data) {
        set("data", data);
        return this;
    }

    /**
     * Height of the whole barcode, a length or a name from the height row of the tokens, with the digits of hrt inside
     * it: they take the bottom 12pt and the bars the rest. A barcode is never stretched. With only a height, the width
     * follows the barcode's proportions. With both width and height, they are a maximum: the barcode takes the largest
     * size that fits in them, and its box becomes that size. A QR code is square, so it takes the smaller of the two.
     * Without a size, a code128 is as wide as its bars at 1pt per module and its bars are 40pt high, and an ean13 is
     * the standard symbol, 0.33mm per module and bars 22.85mm high. The digits of hrt add 12pt to either.
     */
    public BarcodeAttr height(String height) {
        set("height", height);
        return this;
    }

    public BarcodeAttr ec(String ec) {
        set("ec", ec);
        return this;
    }

    public BarcodeAttr hrt(boolean hrt) {
        set("hrt", hrt);
        return this;
    }

    public BarcodeAttr color(String color) {
        set("color", color);
        return this;
    }

    public BarcodeAttr background(String background) {
        set("background", background);
        return this;
    }

    /**
     * Width of the element. A length fixes it. fill, in a row, takes the width its line leaves after the other
     * children, shared equally if several on the line use fill, and never moves to the next line; in a column it takes
     * the full width.
     */
    public BarcodeAttr width(String width) {
        set("width", width);
        return this;
    }

    /**
     * What the barcode is for, which a screen reader reads in a tagged document, one with pdfua or a pdfa of level a,
     * in place of its data, which means nothing read aloud: "QR code to pay this invoice". alt="" marks it as
     * decoration. A PDF/UA document needs it on every barcode of the layout; at PDF/A level a without pdfua, a barcode
     * without it is decoration. A document that is not tagged ignores it.
     */
    public BarcodeAttr alt(String alt) {
        set("alt", alt);
        return this;
    }

    public BarcodeAttr paginate(String paginate) {
        set("paginate", paginate);
        return this;
    }

    public BarcodeAttr debug(boolean debug) {
        set("debug", debug);
        return this;
    }

    /**
     * Puts the element, and everything in it, in an optional content group, which the viewer shows or hides. print:
     * printed, and not shown on screen. screen: shown on screen, and not printed. The element keeps its room either
     * way, so nothing moves. An element with a group inside an element with another shows only where both show.
     * Acrobat, Chrome and Firefox switch the groups; Preview and Safari show everything, on screen and in print. Hidden
     * content is still in the file, where it can be selected and copied: it is not a way to keep anything secret. A
     * link in print content is drawn and does nothing, and a form field can't be in print content: use a frame with a
     * border for a box filled in by hand. With pdfa or pdfua on the output, everything shows. With pdfx, the file is
     * the print version: screen content stays in it, off, and a reader can turn it on in a layers panel.
     */
    public BarcodeAttr ocg(String ocg) {
        set("ocg", ocg);
        return this;
    }
}
