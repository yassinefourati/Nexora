package com.fourati.platform.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Hashing and token utilities.
 * Use for: API keys, password reset tokens, HMAC signatures, content fingerprinting.
 *
 * Note: for password storage use Spring Security's BCryptPasswordEncoder, not SHA-256.
 */
public final class HashUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    private HashUtils() {}

    /** SHA-256 hex digest of a string. */
    public static String sha256(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** SHA-256 hex digest of raw bytes. */
    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * HMAC-SHA256 hex signature — use for webhook payload verification,
     * signed tokens, or API request signing.
     */
    public static String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(
                mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 failed", e);
        }
    }

    /**
     * Generates a cryptographically secure random token (URL-safe Base64, no padding).
     * generateToken(32) → 43 chars, e.g. "dGhpcyBpcyBhIHNlY3VyZSB0b2tlbg"
     */
    public static String generateToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Generates a prefixed API key: "prefix_<url-safe-random>".
     * generateApiKey("x") → "x_dGhpcyBpcyBhIHNlY3VyZSB0b2tlbg"
     */
    public static String generateApiKey(String prefix) {
        return prefix + "_" + generateToken(32);
    }

    /**
     * Constant-time comparison — prevents timing attacks when comparing tokens.
     * Use instead of String.equals() for security-sensitive comparisons.
     */
    public static boolean safeEquals(String a, String b) {
		if (a == null || b == null)
			return false;
        return MessageDigest.isEqual(
            a.getBytes(StandardCharsets.UTF_8),
            b.getBytes(StandardCharsets.UTF_8)
        );
    }

    /** MD5 hex digest — for non-security uses (e.g. Gravatar URL generation). */
    public static String md5(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }
}
