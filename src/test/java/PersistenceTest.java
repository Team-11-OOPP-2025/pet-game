import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.character.behavior.StateRegistry;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.inventory.Inventory;
import com.eleven.pet.storage.EncryptionService;
import com.eleven.pet.storage.PersistenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceTest {

    /**
     * A mock encryption service that does not modify the data.
     * It simply passes the streams through, allowing us to inspect the saved file
     * as plain text or JSON if needed, and isolating tests from encryption logic.
     */
    private static class NoOpEncryptionService implements EncryptionService {
        @Override
        public OutputStream wrapOutputStream(OutputStream out) {
            // Pass-through: Do not encrypt, just return the original stream
            return out;
        }

        @Override
        public InputStream wrapInputStream(InputStream in) {
            // Pass-through: Do not decrypt, just return the original stream
            return in;
        }
    }

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadRoundTripRestoresModelState() {
        Path savePath = tempDir.resolve("roundtrip.dat");

        // Use the NoOp service so we test serialization logic without encryption complexity
        EncryptionService encryptionService = new NoOpEncryptionService();
        PersistenceService service = new PersistenceService(encryptionService, savePath);

        PetModel original = PetFactory.createNewPet("Fluffy", null, null);
        original.changeState(StateRegistry.getInstance().getState(AsleepState.STATE_NAME));

        PetStats stats = original.getStats();
        stats.registerStat(PetStats.STAT_HAPPINESS, 50);
        stats.registerStat(PetStats.STAT_HUNGER, GameConfig.MIN_STAT_VALUE);
        Inventory inventory = original.getInventory();
        original.setCurrentSleepDuration(1234L);
        original.setSleptThisNight(true);

        service.save(original);

        assertTrue(Files.exists(savePath));

        PetModel loaded = service.load(null, null)
                .orElseThrow(() -> new AssertionError("Loaded model should not be empty"));

        assertEquals(original.getName(), loaded.getName());
        assertEquals(50, loaded.getStats().getStat(PetStats.STAT_HAPPINESS).get());
        assertEquals(GameConfig.MIN_STAT_VALUE, loaded.getStats().getStat(PetStats.STAT_HUNGER).get());
        assertEquals(1234L, loaded.getCurrentSleepDuration());
        assertTrue(loaded.isSleptThisNight());
        assertTrue(Math.abs(inventory.getAllOwnedItems().size() - loaded.getInventory().getAllOwnedItems().size()) <= 3,
                "Owned items count should be within a range of 3");
        assertEquals(original.getCurrentState(), loaded.getCurrentState());
    }

    @Test
    void loadWhenFileMissingReturnsNull() {
        Path missingPath = tempDir.resolve("missing.dat");
        EncryptionService encryptionService = new NoOpEncryptionService();
        PersistenceService service = new PersistenceService(encryptionService, missingPath);

        assertTrue(service.load(null, null).isEmpty(),
                "Expected empty Optional when loading from missing file");
    }
}