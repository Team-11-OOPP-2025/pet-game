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
 * AES-GCM encryption implementation that supports streaming encryption and decryption.
 *
 * <p>The implementation writes a randomly generated IV (12 bytes) at the beginning of the
 * encrypted stream so the recipient can read it and initialize the cipher for decryption.
 * Tag length is {@code GCM_TAG_LENGTH} bits and is passed to {@link GCMParameterSpec}.</p>
 *
 * <p>Note: this class does not close the provided {@link InputStream} or {@link OutputStream};
 * the caller is responsible for closing streams.</p>
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
     * Create a new {@code GcmEncryptionService} using the provided AES {@code key}.
     *
     * @param key the AES {@link SecretKey} to use for encryption and decryption
     */
    public GcmEncryptionService(SecretKey key) {
        this.key = key;
    }

    /**
     * Wraps an {@link OutputStream} with an encryption layer.
     * <p>
     * Any data written to the returned stream is encrypted using AES-GCM and then
     * written to the underlying {@code out} stream. The IV is written immediately
     * to the underlying stream header.
     * </p>
     *
     * @param out the destination stream for the encrypted data
     * @return an output stream that encrypts data as it is written
     * @throws GameException if initialization fails
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