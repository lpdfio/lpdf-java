// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A level of PDF/A, the ISO 19005 standard for files kept for the long term: 2b keeps the look of every page, 2u adds
 * that all text maps to Unicode, so it can be searched and copied, and 2a adds to 2u the tagged structure a screen
 * reader reads. 3a, 3b and 3u are the same levels in part 3, which also carries files of any kind, as a Factur-X or
 * ZUGFeRD e-invoice carries its XML.
 */
public final class PdfALevel {
    private PdfALevel() {
    }

    public static final String LEVEL_2A = "2a";
    public static final String LEVEL_2B = "2b";
    public static final String LEVEL_2U = "2u";
    public static final String LEVEL_3A = "3a";
    public static final String LEVEL_3B = "3b";
    public static final String LEVEL_3U = "3u";
}
