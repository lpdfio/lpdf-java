package io.lpdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * What the engine makes of a license key. A key that comes back licensed is signed with the key this build trusts, which
 * a test has no way to make: these are the answers every build gives.
 */
class LicenseCheckTest {
    @Test
    void without_a_key_the_engine_is_not_licensed() {
        var check = L.engine().checkLicenseKey();

        assertEquals(new LicenseCheck("no_key", null, null, null, null, null), check);
        assertFalse(check.isLicensed());
    }

    @Test
    void a_key_that_is_not_a_token_is_malformed() {
        var check = L.engine().checkLicenseKey("not-a-key");

        assertEquals("malformed", check.status());
        assertFalse(check.isLicensed());
    }

    @Test
    void the_key_checked_is_the_one_set_on_the_engine() {
        var engine = L.engine().setLicenseKey("not-a-key");

        assertEquals("malformed", engine.checkLicenseKey().status());
        assertEquals("no_key", engine.setLicenseKey("").checkLicenseKey().status());
    }
}
