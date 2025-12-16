import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.character.behavior.AwakeState;
import com.eleven.pet.character.behavior.StateRegistry;
import com.eleven.pet.core.GameException;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.storage.EncryptionService;
import com.eleven.pet.storage.PersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PetController}, focusing on autosave, shutdown behavior,
 * and interaction with {@link PersistenceService}.
 */
public class PetControllerTest {

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

    /**
     * Mock persistence service that tracks how many times {@link #save(PetModel)} is called
     * and can optionally throw to simulate save failures.
     */
    private static class MockPersistenceService extends PersistenceService {
        private final AtomicInteger saveCallCount = new AtomicInteger(0);
        private boolean throwOnSave = false;

        public MockPersistenceService(Path savePath) {
            super(new NoOpEncryptionService(), savePath);
        }

        /**
         * Increments the save call counter and optionally throws a {@link GameException}
         * to simulate a persistence error before delegating to the real implementation.
         */
        @Override
        public void save(PetModel model) throws GameException {
            saveCallCount.incrementAndGet();
            if (throwOnSave) {
                throw new GameException("Test error during save");
            }
            super.save(model);
        }

        /**
         * @return the number of times {@link #save(PetModel)} has been invoked.
         */
        public int getSaveCallCount() {
            return saveCallCount.get();
        }

        /**
         * Enables or disables throwing a {@link GameException} during save.
         *
         * @param throwOnSave whether saves should fail with an exception
         */
        public void setThrowOnSave(boolean throwOnSave) {
            this.throwOnSave = throwOnSave;
        }
    }

    private MockPersistenceService persistence;
    private PetController controller;
    private WeatherSystem weatherSystem;

    @TempDir
    Path tempDir;

    /**
     * Sets up a fresh {@link PetModel}, {@link GameClock}, and mock persistence
     * before each test, and registers the basic pet states.
     */
    @BeforeEach
    void setUp() {
        // Manually register states for testing
        StateRegistry registry = StateRegistry.getInstance();
        registry.registerState(new AwakeState());
        registry.registerState(new AsleepState());

        GameClock clock = new GameClock();
        PetModel model = PetFactory.createNewPet("TestPet", null, clock);
        persistence = new MockPersistenceService(tempDir.resolve("test-save.dat"));
        weatherSystem = new WeatherSystem();
        // leaderboard is null as it's not needed for these tests
        controller = new PetController(model, clock, weatherSystem, persistence, null);
    }

    /**
     * Ensures that calling {@link PetController#stopAutosave()} before autosave
     * has been initialized does not throw an exception.
     */
    @Test
    void testStopAutosaveWhenTimerNotInitialized() {
        // Stopping autosave before initialization should not throw
        assertDoesNotThrow(controller::stopAutosave);
    }

    /**
     * Verifies that {@link PetController#shutdown()} triggers a single save operation.
     */
    @Test
    void testShutdownPerformsSave() {
        // Shutdown should save the game
        controller.shutdown();

        assertEquals(1, persistence.getSaveCallCount(), "Shutdown should trigger one save");
    }

    /**
     * Verifies that {@link PetController#shutdown()} swallows save errors and
     * does not propagate {@link GameException} thrown by persistence.
     */
    @Test
    void testShutdownHandlesSaveError() {
        persistence.setThrowOnSave(true);

        // Shutdown with save error should not throw
        assertDoesNotThrow(controller::shutdown);
    }

    /**
     * Ensures that multiple calls to {@link PetController#shutdown()} are idempotent
     * and only cause a single save.
     */
    @Test
    void testMultipleShutdownCallsAreIdempotent() {
        controller.shutdown();
        controller.shutdown();

        // Only the first shutdown should trigger a save
        assertEquals(1, persistence.getSaveCallCount(), "Multiple shutdowns should only save once");
    }

    /**
     * Verifies that {@link PetController} can be created with {@code null} weather
     * and clock dependencies without failing.
     */
    @Test
    void testControllerCreationWithNullComponents() {
        assertNotNull(controller);
    }

    /**
     * Ensures that shutdown both stops autosave (if running) and performs a save,
     * and that stopping autosave afterwards is safe.
     */
    @Test
    void testShutdownStopsAutosaveAndSaves() {
        // Shutdown should stop autosave (if running) and save
        controller.shutdown();

        // Should have saved once
        assertEquals(1, persistence.getSaveCallCount(), "Shutdown should save once");

        // Stopping autosave again should work without error
        assertDoesNotThrow(controller::stopAutosave);
    }

    @Test
    void changeWeather() {
        WeatherState weater = weatherSystem.getCurrentWeather();
        controller.debugChangeWeather();
        assertNotEquals(weater, weatherSystem.getCurrentWeather());
    }
}
