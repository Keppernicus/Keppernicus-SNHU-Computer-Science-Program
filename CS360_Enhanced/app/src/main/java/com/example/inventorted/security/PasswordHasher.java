package com.example.inventorted.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *  This is an iterated & salted  password hashing for the login credentials.
 * It replaces the old scheme that stored the password as plaintext in the users
 * table.
 * The algorithm is pbkdf2 with hmac-sha256,  part of the java standard library
 * and is available on android from api 26, which is this app's minsdk, so it needs
 * no third party dependencies.
 *
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;


    /** expensive on purpose. higher iterations raise the cost of an offline cracking attempt */

    private static final int ITERATIONS = 120_000;

    private final SecureRandom random = new SecureRandom();

    /** derives a salted hash and returns the encoded string to store. */
    public String hash(String password) {
        char[] chars = (password == null ? "" : password).toCharArray();
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        try {
            byte[] derived = deriveKey(chars, salt, ITERATIONS);
            return ITERATIONS
                    + ":" + Base64.getEncoder().encodeToString(salt)
                    + ":" + Base64.getEncoder().encodeToString(derived);
        } finally {
            java.util.Arrays.fill(chars, '\0'); // doesn't leave the password sitting in the array
        }
    }

    /**
     * Verifies a candidate password against a previously stored encoded hash.
     *returns false on any malformed input.
     */
    public boolean verify(String password, String stored) {
        if (stored == null) return false;
        String[] parts = stored.split(":");
        if (parts.length != 3) return false;

        char[] chars = (password == null ? "" : password).toCharArray();
        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            byte[] actual = deriveKey(chars, salt, iterations);
            return constantTimeEquals(expected, actual);
        } catch (RuntimeException e) {
            return false; // bad number, bad base64, etc.
        } finally {
            java.util.Arrays.fill(chars, '\0');
        }
    }

    private byte[] deriveKey(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 unavailable", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** length aware constant time comparison to avoid leaking in timing. */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        return java.security.MessageDigest.isEqual(a, b);
    }
}