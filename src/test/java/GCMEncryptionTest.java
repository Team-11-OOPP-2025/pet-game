import com.eleven.pet.core.GameException;
import com.eleven.pet.storage.GcmEncryptionService;
import com.eleven.pet.storage.KeyLoader;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GCMEncryptionTest {

    @Test
    void roundTripEncryptionProducesOriginalData() throws Exception {
        SecretKey key = KeyLoader.generateDevKey();
        GcmEncryptionService gcmService = new GcmEncryptionService(key);
        String data = "This is some test data for encryption";

        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        try (OutputStream wrapped = gcmService.wrapOutputStream(encryptedOut)) {
            wrapped.write(data.getBytes());
        }

        ByteArrayInputStream encryptedIn = new ByteArrayInputStream(encryptedOut.toByteArray());
        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();

        try (InputStream wrapped = gcmService.wrapInputStream(encryptedIn)) {
            wrapped.transferTo(decryptedOut);
        }

        String decrypted = decryptedOut.toString();
        assertEquals(data, decrypted);
    }

    @Test
    void encryptAndDecryptEmptyInputStream() throws Exception {
        SecretKey key = KeyLoader.generateDevKey();
        GcmEncryptionService gcmService = new GcmEncryptionService(key);

        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        try (OutputStream _ = gcmService.wrapOutputStream(encryptedOut)) {
            // Write nothing
        }

        ByteArrayInputStream encryptedIn = new ByteArrayInputStream(encryptedOut.toByteArray());
        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();

        try (InputStream wrapped = gcmService.wrapInputStream(encryptedIn)) {
            wrapped.transferTo(decryptedOut);
        }

        assertEquals(0, decryptedOut.toByteArray().length);
    }

    @Test
    void decryptWithMissingIvThrowsGameException() {
        SecretKey key = KeyLoader.generateDevKey();
        GcmEncryptionService gcmService = new GcmEncryptionService(key);

        ByteArrayInputStream encryptedIn = new ByteArrayInputStream(new byte[0]);

        assertThrows(
                GameException.class,
                () -> gcmService.wrapInputStream(encryptedIn)
        );
    }

    @Test
    void decryptWithCorruptedCipherTextThrowsIOException() {
        SecretKey key = KeyLoader.generateDevKey();
        GcmEncryptionService gcmService = new GcmEncryptionService(key);

        byte[] corrupted = new byte[32];
        new java.security.SecureRandom().nextBytes(corrupted);

        ByteArrayInputStream encryptedIn = new ByteArrayInputStream(corrupted);

        // Wrapping succeeds (reads IV)
        InputStream wrapped = gcmService.wrapInputStream(encryptedIn);

        // Reading fails (Auth Tag mismatch)
        assertThrows(
                IOException.class,
                () -> wrapped.transferTo(new ByteArrayOutputStream())
        );
    }
}