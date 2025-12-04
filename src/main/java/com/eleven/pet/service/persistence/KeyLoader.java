package com.eleven.pet.service.persistence;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility for loading encryption keys from environment variables.
 */
public class KeyLoader {
    private final static String ENV_KEY_NAME = "ENCRYPTION.KEY";

    public static SecretKey loadKey() throws GameException {
        String keyString = System.getProperty(ENV_KEY_NAME);

        if (keyString == null || keyString.isEmpty()) {
            throw new GameException("Encryption key not found in properties: " + ENV_KEY_NAME);
        }

        try {
            byte[] decodedKey = Base64.getDecoder().decode(keyString);
            return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        } catch (IllegalArgumentException e) {
            throw new GameException("Invalid key format in environment variable", e);
        }
    }

    /**
     * Generate a random key for development
     */
    public static SecretKey generateDevKey() {
        try {
            javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate dev key", e);
        }
    }
}
