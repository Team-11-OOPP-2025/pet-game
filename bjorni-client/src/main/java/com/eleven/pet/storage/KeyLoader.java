package com.eleven.pet.storage;

import com.eleven.pet.core.GameException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility class for obtaining {@link javax.crypto.SecretKey} instances used for
 * encrypting and decrypting save data.
 * <p>
 * In production, a Base64-encoded key is expected to be provided via a system
 * property. For local development a deterministic device-specific key can be
 * generated instead.
 * </p>
 */
public class KeyLoader {
    private final static String ENV_KEY_NAME = "ENCRYPTION.KEY";

    /**
     * Load the AES encryption key from a system property.
     * <p>
     * The key must be Base64-encoded and stored under the property name
     * {@value ENV_KEY_NAME}.
     * </p>
     *
     * @return a {@link SecretKey} suitable for AES encryption
     * @throws GameException if the property is missing or cannot be decoded
     */
    public static SecretKey loadKey() throws GameException {
        String keyString = System.getProperty(ENV_KEY_NAME);

        if (keyString == null || keyString.isEmpty()) {
            throw new GameException("Encryption key not found in properties: " + ENV_KEY_NAME);
        }

        try {
            // Decode the Base64-encoded key and create SecretKey
            byte[] decodedKey = Base64.getDecoder().decode(keyString);
            return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        } catch (IllegalArgumentException e) {
            throw new GameException("Invalid key format in environment variable", e);
        }
    }

    /**
     * Generate a deterministic, device-specific development key.
     * <p>
     * The same environment will always produce the same key, but different
     * devices should produce different keys. This is intended only for local
     * development where a real secret-management solution is not available.
     * </p>
     *
     * @return a pseudo-random {@link SecretKey} derived from local machine data
     * @throws RuntimeException if key derivation fails for any reason
     */
    public static SecretKey generateDevKey() {
        try {
            // Collect a few relatively stable, OS-agnostic properties
            String seed =
                    System.getProperty("os.name", "") + "|" +
                            System.getProperty("os.arch", "") + "|" +
                            System.getProperty("user.name", "") + "|" +
                            java.net.InetAddress.getLocalHost().getHostName();

            // Fixed salt to stabilize the derivation
            String salt = "com.eleven.pet.devkey.v1";

            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((seed + "|" + salt).getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Use first 32 bytes (256 bits) as AES key material
            return new SecretKeySpec(hash, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate deterministic dev key", e);
        }
    }
}
