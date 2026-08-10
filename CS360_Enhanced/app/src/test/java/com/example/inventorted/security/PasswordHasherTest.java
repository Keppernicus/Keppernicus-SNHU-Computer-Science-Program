package com.example.inventorted.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * jvm unit tests for Passwordhasher. These run without an emulator for security behavior.
 * It checks a correct password verifies, the same password hashed twice
 * yields different output (proving the salt is random), and the plaintext never
 * appears anywhere.
 */
public class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    public void correctPasswordVerifies() {
        String stored = hasher.hash("hunter2");
        assertTrue(hasher.verify("hunter2", stored));
    }

    @Test
    public void wrongPasswordIsRejected() {
        String stored = hasher.hash("hunter2");
        assertFalse(hasher.verify("Hunter2", stored));
        assertFalse(hasher.verify("", stored));
    }

    @Test
    public void samePasswordProducesDifferentHashes() {
        // Random per-password salt means two hashes of the same password are different.
        assertNotEquals(hasher.hash("hunter2"), hasher.hash("hunter2"));
    }

    @Test
    public void bothIndependentHashesStillVerify() {
        assertTrue(hasher.verify("hunter2", hasher.hash("hunter2")));
        assertTrue(hasher.verify("hunter2", hasher.hash("hunter2")));
    }

    @Test
    public void storedHashDoesNotContainPlaintext() {
        assertFalse(hasher.hash("hunter2").contains("hunter2"));
    }

    @Test
    public void encodedFormatHasThreeParts() {
        assertTrue(hasher.hash("hunter2").split(":").length == 3);
    }

    @Test
    public void malformedStoredValueReturnsFalse() {
        assertFalse(hasher.verify("hunter2", "not-a-valid-hash"));
        assertFalse(hasher.verify("hunter2", null));
    }

    @Test
    public void nullPasswordIsHandled() {
        String stored = hasher.hash("hunter2");
        assertFalse(hasher.verify(null, stored));
    }
}