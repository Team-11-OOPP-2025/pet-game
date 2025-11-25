package com.eleven.pet.persistence;

import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.SecretKey;

public class GcmEncryptionService implements EncryptionService {
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    private final SecretKey key;
    
    public GcmEncryptionService(SecretKey key) {
        this.key = key;
    }
    
    @Override
    public void encrypt(InputStream in, OutputStream out) throws Exception {
    }
    
    @Override
    public void decrypt(InputStream in, OutputStream out) throws Exception {
    }
}
