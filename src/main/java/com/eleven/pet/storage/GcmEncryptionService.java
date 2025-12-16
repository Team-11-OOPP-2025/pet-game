package com.eleven.pet.storage;

import com.eleven.pet.core.GameException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * AES-GCM based implementation of {@link EncryptionService} that supports
 * streaming encryption and decryption.
 * <p>
 * A fresh random IV is generated for each encryption stream and written as the
 * first {@value #GCM_IV_LENGTH} bytes of the ciphertext stream. The same IV is
 * read back when initializing the decryption stream.
 * </p>
 */
public class GcmEncryptionService implements EncryptionService {
    /**
     * Secret AES key used for encryption and decryption.
     */
    private final SecretKey key;

    /**
     * Length of the GCM IV in bytes (96 bits). Recommended value for AES-GCM.
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * Authentication tag length in bits used by GCM.
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Secure random generator for IV creation.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Create a new {@code GcmEncryptionService} using the provided AES key.
     *
     * @param key AES {@link SecretKey} used for both encryption and decryption
     */
    public GcmEncryptionService(SecretKey key) {
        this.key = key;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation writes a random IV to the underlying stream first,
     * then returns a {@link javax.crypto.CipherOutputStream} that performs
     * AES/GCM/NoPadding encryption.
     * </p>
     */
    public OutputStream wrapOutputStream(OutputStream out) throws GameException {
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            RANDOM.nextBytes(iv);

            // Write IV header immediately so the decryptor can find it later
            out.write(iv);

            // Initialize the Cipher
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            // Return a CipherOutputStream
            // This acts as a funnel: Plaintext -> CIpher -> Ciphertext -> File
            return new javax.crypto.CipherOutputStream(out, cipher);

        } catch (Exception e) {
            throw new GameException("Failed to create encryption stream", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation reads the IV from the first {@value #GCM_IV_LENGTH}
     * bytes of the provided stream, initializes an AES-GCM cipher, and returns
     * a {@link CipherInputStream} that decrypts data on the fly.
     * </p>
     */
    @Override
    public InputStream wrapInputStream(InputStream in) throws GameException {
        try {
            // Read IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            int bytesRead = in.readNBytes(iv, 0, iv.length);

            if (bytesRead < GCM_IV_LENGTH) {
                throw new GameException("Stream too short: missing IV");
            }

            // Init Cipher
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            return new CipherInputStream(in, cipher);
        } catch (IOException | GeneralSecurityException e) {
            throw new GameException("Failed to init decryption", e);
        }
    }
}