package com.eleven.pet.service.persistence;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * AES-GCM encryption implementation.
 */
public class GcmEncryptionService implements EncryptionService {
    private final SecretKey key;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    public GcmEncryptionService(SecretKey key) {
        this.key = key;
    }

    @Override
    public void encrypt(InputStream in, OutputStream out) throws GameException {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            // Write IV at the beginning of the output to allow decryption (end works, but then we can't stream)
            out.write(iv);

            // Encrypt data
            doAesGcm(in, out, cipher);

        } catch (IOException | GeneralSecurityException e) {
            throw new GameException("Encryption failed", e);
        }
    }

    @Override
    public void decrypt(InputStream in, OutputStream out) throws GameException {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            // We use DataInputStream to ensure we read the full IV bytes!
            DataInputStream dis = new DataInputStream(in);
            try {
                dis.readFully(iv);
            } catch (EOFException e) {
                throw new GameException("Invalid encrypted file - missing IV", e);
            }

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            // Decrypt data
            doAesGcm(dis, out, cipher);

        } catch (IOException | GeneralSecurityException e) {
            throw new GameException("Decryption failed", e);
        }
    }

    public void doAesGcm(InputStream in, OutputStream out, Cipher cipher) throws IOException, GeneralSecurityException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            byte[] encrypted = cipher.update(buffer, 0, bytesRead);
            if (encrypted != null) {
                out.write(encrypted);
            }
        }

        // Write final block
        byte[] finalBlock = cipher.doFinal();
        if (finalBlock != null) {
            out.write(finalBlock);
        }
    }
}