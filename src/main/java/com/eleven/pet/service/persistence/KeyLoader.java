package com.eleven.pet.service.persistence;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility for loading encryption keys from environment variables.
 */
public class KeyLoader {
    private final static String ENV_KEY_NAME = "ENCRYPTION.KEY";

    /**
     * Load the encryption key from environment variable.
     *
     * @return SecretKey instance
     * @throws GameException if the key is not found or invalid
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
     * Generate a deterministic, device-specific dev key.
     * Same device \= same key, different devices \= different keys.
     * WARNING: Not secure, only for development use!
     *
     * @return SecretKey instance
     * @throws RuntimeException if key generation fails
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
