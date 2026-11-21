// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * The cipher a PDF is encrypted with: AES-256, or RC4-128 for readers that cannot open AES-256. PDF 2.0 deprecates
 * RC4-128.
 */
public final class Cipher {
    private Cipher() {
    }

    public static final String AES_256 = "aes-256";
    public static final String RC4_128 = "rc4-128";
}
