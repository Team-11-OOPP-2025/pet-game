import com.eleven.pet.behavior.AsleepState;
import com.eleven.pet.behavior.StateRegistry;
import com.eleven.pet.config.GameConfig;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
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

import static org.junit.jupiter.api.Assertions.*;

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
    void saveAndLoadRoundTripRestoresModelState() throws Exception {
        Path savePath = tempDir.resolve("roundtrip.dat");
        EncryptionService encryptionService = new NoOpEncryptionService();
        PersistenceService service = new PersistenceService(encryptionService, savePath);

        WeatherSystem weatherSystem = new WeatherSystem();
        GameClock gameClock = new GameClock();
        PetModel original = PetFactory.createNewPet("Fluffy", weatherSystem, gameClock);
        original.changeState(StateRegistry.getInstance().getState(AsleepState.STATE_NAME));

        PetStats stats = original.getStats();
        stats.registerStat(PetStats.STAT_HAPPINESS, 50);
        stats.registerStat(PetStats.STAT_HUNGER, GameConfig.MIN_STAT_VALUE);
        Inventory inventory = original.getInventory();
        original.setSleepStartTime(1234L);
        original.setSleptThisNight(true);

        service.save(original);

        assertTrue(Files.exists(savePath));

        PetModel loaded = service.load(weatherSystem, gameClock);

        assertEquals(original.getName(), loaded.getName());
        assertEquals(50, loaded.getStats().getStat(PetStats.STAT_HAPPINESS).get());
        assertEquals(GameConfig.MIN_STAT_VALUE, loaded.getStats().getStat(PetStats.STAT_HUNGER).get());
        assertEquals(1234L, loaded.getSleepStartTime());
        assertTrue(loaded.getSleptThisNight());
        assertEquals(inventory.getAllOwnedItems(), loaded.getInventory().getAllOwnedItems());
        assertEquals(original.getCurrentState(), loaded.getCurrentState());
    }

    @Test
    void loadWhenFileMissingThrowsGameException() {
        Path missingPath = tempDir.resolve("missing.dat");
        EncryptionService encryptionService = new NoOpEncryptionService();
        PersistenceService service = new PersistenceService(encryptionService, missingPath);

        WeatherSystem weatherSystem = new WeatherSystem();
        GameClock gameClock = new GameClock();

        assertThrows(GameException.class, () -> service.load(weatherSystem, gameClock));
    }
}
