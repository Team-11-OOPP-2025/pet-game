import com.eleven.pet.service.persistence.GcmEncryptionService;
import com.eleven.pet.service.persistence.KeyLoader;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GCMEncryptionTest {
    @Test
    void roundTripEncryptionProducesOriginalData() {
        SecretKey key = KeyLoader.generateDevKey();
        GcmEncryptionService gcmService = new GcmEncryptionService(key);
        String data = "This is some test data for encryption";

        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes());
        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        gcmService.encrypt(in, encryptedOut);

        ByteArrayInputStream encryptedIn = new ByteArrayInputStream(encryptedOut.toByteArray());
        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
        gcmService.decrypt(encryptedIn, decryptedOut);

        String decrypted = decryptedOut.toString();
        assertEquals(data, decrypted);
    }

    @Test
    void encryptAndDecryptEmptyInputStream() {
        SecretKey key = KeyLoader.generateDevKey();
        GcmEncryptionService gcmService = new GcmEncryptionService(key);

        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        gcmService.encrypt(in, encryptedOut);

        ByteArrayInputStream encryptedIn = new ByteArrayInputStream(encryptedOut.toByteArray());
        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
        gcmService.decrypt(encryptedIn, decryptedOut);

        assertEquals(0, decryptedOut.toByteArray().length);
    }

    @Test
    void decryptWithMissingIvThrowsGameException() {
        SecretKey key = KeyLoader.generateDevKey();
        GcmEncryptionService gcmService = new GcmEncryptionService(key);

        ByteArrayInputStream encryptedIn = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();

        assertThrows(
                com.eleven.pet.service.persistence.GameException.class,
                () -> gcmService.decrypt(encryptedIn, decryptedOut)
        );
    }

    @Test
    void decryptWithCorruptedCipherTextThrowsGameException() {
        SecretKey key = KeyLoader.generateDevKey();
        GcmEncryptionService gcmService = new GcmEncryptionService(key);

        byte[] corrupted = new byte[32];
        new java.security.SecureRandom().nextBytes(corrupted);

        ByteArrayInputStream encryptedIn = new ByteArrayInputStream(corrupted);
        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();

        assertThrows(
                com.eleven.pet.service.persistence.GameException.class,
                () -> gcmService.decrypt(encryptedIn, decryptedOut)
        );
    }

}
