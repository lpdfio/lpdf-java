// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** The values of the <code>type</code> attribute of a barcode: the kind of code. */
public final class BarcodeType {
    private BarcodeType() {
    }

    public static final String QR = "qr";
    public static final String CODE128 = "code128";
    public static final String EAN13 = "ean13";
    public static final String CODE39 = "code39";
    public static final String EAN8 = "ean8";
    public static final String UPCA = "upca";
    public static final String DATAMATRIX = "datamatrix";
    public static final String PDF417 = "pdf417";
    public static final String GS1_128 = "gs1-128";
    public static final String GS1_DATAMATRIX = "gs1-datamatrix";
}
