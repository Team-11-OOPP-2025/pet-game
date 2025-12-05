import com.eleven.pet.behavior.AsleepState;
import com.eleven.pet.behavior.StateRegistry;
import com.eleven.pet.config.GameConfig;
import com.eleven.pet.model.Inventory;
import com.eleven.pet.model.PetFactory;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;
import com.eleven.pet.service.persistence.EncryptionService;
import com.eleven.pet.service.persistence.GameException;
import com.eleven.pet.service.persistence.PersistenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceTest {

    private static class NoOpEncryptionService implements EncryptionService {
        @Override
        public void encrypt(InputStream in, OutputStream out) throws GameException {
            try {
                in.transferTo(out);
            } catch (Exception e) {
                throw new GameException("NoOp encrypt failed", e);
            }
        }

        @Override
        public void decrypt(InputStream in, OutputStream out) throws GameException {
            try {
                in.transferTo(out);
            } catch (Exception e) {
                throw new GameException("NoOp decrypt failed", e);
            }
        }
    }

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadRoundTripRestoresModelState() {
        Path savePath = tempDir.resolve("roundtrip.dat");
        EncryptionService encryptionService = new NoOpEncryptionService();
        PersistenceService service = new PersistenceService(encryptionService, savePath);

        PetModel original = PetFactory.createNewPet("Fluffy", null, null);
        original.changeState(StateRegistry.getInstance().getState(AsleepState.STATE_NAME));

        PetStats stats = original.getStats();
        stats.registerStat(PetStats.STAT_HAPPINESS, 50);
        stats.registerStat(PetStats.STAT_HUNGER, GameConfig.MIN_STAT_VALUE);
        Inventory inventory = original.getInventory();
        original.setSleepStartTime(1234L);
        original.setSleptThisNight(true);

        service.save(original);

        assertTrue(Files.exists(savePath));

        PetModel loaded = service.load(null, null).orElseThrow(() -> new AssertionError("Loaded model should not be empty"));

        assertEquals(original.getName(), loaded.getName());
        assertEquals(50, loaded.getStats().getStat(PetStats.STAT_HAPPINESS).get());
        assertEquals(GameConfig.MIN_STAT_VALUE, loaded.getStats().getStat(PetStats.STAT_HUNGER).get());
        assertEquals(1234L, loaded.getSleepStartTime());
        assertTrue(loaded.isSleptThisNight());
        assertEquals(inventory.getAllOwnedItems(), loaded.getInventory().getAllOwnedItems());
        assertEquals(original.getCurrentState(), loaded.getCurrentState());
    }

    @Test
    void loadWhenFileMissingReturnsNull() {
        Path missingPath = tempDir.resolve("missing.dat");
        EncryptionService encryptionService = new NoOpEncryptionService();
        PersistenceService service = new PersistenceService(encryptionService, missingPath);

        assertTrue(service.load(null, null).isEmpty(), "Expected empty Optional when loading from missing file");
    }
}
