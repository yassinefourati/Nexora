package com.fourati.platform.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM symmetric encryption.
 *
 * Why AES-256-GCM?
 *   - Authenticated encryption: detects tampering (unlike AES-CBC)
 *   - 256-bit key: NIST-approved, quantum-resistant margin
 *   - GCM mode: fast, parallelizable, no padding vulnerabilities
 *
 * Use for:
 *   - Encrypting sensitive DB columns (PII, tokens, API credentials)
 *   - Encrypting data in transit without TLS (e.g. files, messages)
 *   - NOT for password storage — use BCrypt for that
 *
 * Key management:
 *   Store the key in environment variables or a secrets manager (AWS Secrets Manager, Vault).
 *   NEVER hardcode keys or commit them to source control.
 *
 * Example:
 *   // Generate once and store securely:
 *   String key = EncryptionUtils.generateBase64Key();  // store this in env vars
 *
 *   // Encrypt before storing:
 *   String encrypted = EncryptionUtils.encrypt(userEmail, System.getenv("ENCRYPTION_KEY"));
 *
 *   // Decrypt when reading:
 *   String plain = EncryptionUtils.decrypt(encrypted, System.getenv("ENCRYPTION_KEY"));
 */
public final class EncryptionUtils {

	private static final String ALGORITHM = "AES/GCM/NoPadding";
	private static final int KEY_BITS = 256;
	private static final int IV_BYTES = 12; // 96-bit IV recommended for GCM
	private static final int TAG_BITS = 128;  // authentication tag length

    private static final SecureRandom RANDOM = new SecureRandom();

    private EncryptionUtils() {}

    /**
     * Generates a new random AES-256 key and returns it as Base64.
     * Call once per environment and store the result securely.
     */
    public static String generateBase64Key() {
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(KEY_BITS, RANDOM);
            return Base64.getEncoder().encodeToString(gen.generateKey().getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException("Key generation failed", e);
        }
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     *
     * @param plaintext    the text to encrypt
     * @param base64Key    Base64-encoded AES-256 key (from generateBase64Key())
     * @return Base64-encoded ciphertext (includes prepended IV)
     */
    public static String encrypt(String plaintext, String base64Key) {
		if (plaintext == null)
			return null;
        try {
            byte[] iv  = new byte[IV_BYTES];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, decodeKey(base64Key), new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext: [12 bytes IV][ciphertext + 16 bytes tag]
            ByteBuffer buf = ByteBuffer.allocate(IV_BYTES + ciphertext.length);
            buf.put(iv);
            buf.put(ciphertext);
            return Base64.getEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            throw new IllegalArgumentException("Encryption failed", e);
        }
    }

    /**
     * Decrypts AES-256-GCM ciphertext produced by encrypt().
     *
     * @param base64Cipher Base64-encoded ciphertext (with prepended IV)
     * @param base64Key    the same key used during encryption
     * @return original plaintext
     * @throws IllegalArgumentException if decryption fails (wrong key or tampered data)
     */
    public static String decrypt(String base64Cipher, String base64Key) {
		if (base64Cipher == null)
			return null;
        try {
            byte[] data = Base64.getDecoder().decode(base64Cipher);
            ByteBuffer buf = ByteBuffer.wrap(data);

            byte[] iv = new byte[IV_BYTES];
            buf.get(iv);
            byte[] ciphertext = new byte[buf.remaining()];
            buf.get(ciphertext);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, decodeKey(base64Key), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Decryption failed — wrong key or tampered data", e);
        }
    }

    /** Encrypts bytes and returns Base64. */
    public static String encryptBytes(byte[] data, String base64Key) {
        return encrypt(Base64.getEncoder().encodeToString(data), base64Key);
    }

    /** Decrypts and returns raw bytes. */
    public static byte[] decryptBytes(String base64Cipher, String base64Key) {
        return Base64.getDecoder().decode(decrypt(base64Cipher, base64Key));
    }

    private static SecretKey decodeKey(String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("AES-256 key must be 32 bytes (256 bits)");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }
}
