package com.eleven.pet.storage;

import com.eleven.pet.core.GameException;

import java.io.InputStream;
import java.io.OutputStream;

public interface EncryptionService {
    OutputStream wrapOutputStream(OutputStream out) throws GameException;

    InputStream wrapInputStream(InputStream in) throws GameException;
}
