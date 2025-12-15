package com.eleven.pet.storage;

import com.eleven.pet.core.GameException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstraction for streaming encryption and decryption used by persistence.
 * <p>
 * Implementations wrap raw {@link java.io.InputStream} and
 * {@link java.io.OutputStream} instances with cipher streams that apply
 * encryption or decryption transparently as data is read or written.
 */
public interface EncryptionService {
    /**
     * Wrap the given {@link OutputStream} so that all bytes written to it
     * are encrypted before reaching the underlying stream.
     *
     * @param out the destination stream to wrap
     * @return an {@link OutputStream} that performs encryption on write
     * @throws GameException if the encryption stream cannot be initialized
     */
    OutputStream wrapOutputStream(OutputStream out) throws GameException;

    /**
     * Wrap the given {@link InputStream} so that all bytes read from it
     * are decrypted before being returned to the caller.
     *
     * @param in the source stream containing encrypted data
     * @return an {@link InputStream} that yields decrypted bytes
     * @throws GameException if the decryption stream cannot be initialized
     */
    InputStream wrapInputStream(InputStream in) throws GameException;
}
