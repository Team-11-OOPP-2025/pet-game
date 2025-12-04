package com.eleven.pet.service.persistence;

import java.io.InputStream;
import java.io.OutputStream;

public interface EncryptionService {
    void encrypt(InputStream in, OutputStream out) throws GameException;

    void decrypt(InputStream in, OutputStream out) throws GameException;
}
