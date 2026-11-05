package io.lpdf;

/**
 * What this build of the engine makes of a license key: see {@link PdfEngine#checkLicenseKey(String)}. Everything but
 * the status is there only once the key's signature verified, so an expired key still names its license while an
 * unreadable one says nothing further: an unverified key's contents are its author's claims, not facts.
 *
 * @param status  one of <code>licensed</code>, <code>no_key</code>, <code>expired</code>,
 *                <code>version_mismatch</code>, <code>wrong_product</code>, <code>unknown_key</code>,
 *                <code>bad_signature</code> or <code>malformed</code>
 * @param product which Codesense product the key was issued for
 * @param tier    <code>community</code>, <code>professional</code> or <code>enterprise</code>
 * @param expires when the key stops validating, ISO 8601 UTC; null on a version-locked key, which has no date at all
 * @param license the license number, as the customer reads it: <code>L-7K3M9Q</code>
 * @param key     which key this is on its license: 1, 2, 3 in issue order
 */
public record LicenseCheck(String status, String product, String tier, String expires, String license, Integer key) {
    /** True only for a key this engine renders with, without the attribution line. */
    public boolean isLicensed() {
        return "licensed".equals(status);
    }
}
