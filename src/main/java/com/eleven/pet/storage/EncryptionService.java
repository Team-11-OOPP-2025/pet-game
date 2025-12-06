package com.eleven.pet.storage;

import com.eleven.pet.core.GameException;

import java.io.InputStream;
import java.io.OutputStream;

public interface EncryptionService {
    void encrypt(InputStream in, OutputStream out) throws GameException;

    void decrypt(InputStream in, OutputStream out) throws GameException;
}
