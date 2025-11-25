package com.eleven.pet.persistence;

import java.io.InputStream;
import java.io.OutputStream;

public interface EncryptionService {
    void encrypt(InputStream in, OutputStream out) throws Exception;
    void decrypt(InputStream in, OutputStream out) throws Exception;
}
