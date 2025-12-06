import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.character.behavior.AwakeState;
import com.eleven.pet.character.behavior.StateRegistry;
import com.eleven.pet.core.GameException;
import com.eleven.pet.environment.time.GameClock;
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
 * Tests for PetController's autosave functionality.
 */
public class PetControllerTest {

    /**
     * Simple no-op encryption service for testing purposes.
     */
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

    /**
     * Mock persistence service that tracks save calls.
     */
    private static class MockPersistenceService extends PersistenceService {
        private final AtomicInteger saveCallCount = new AtomicInteger(0);
        private boolean throwOnSave = false;
        
        public MockPersistenceService(Path savePath) {
            super(new NoOpEncryptionService(), savePath);
        }
        
        @Override
        public void save(PetModel model) throws GameException {
            saveCallCount.incrementAndGet();
            if (throwOnSave) {
                throw new GameException("Test error during save");
            }
            super.save(model);
        }
        
        public int getSaveCallCount() {
            return saveCallCount.get();
        }
        
        public void setThrowOnSave(boolean throwOnSave) {
            this.throwOnSave = throwOnSave;
        }
    }

    private PetModel model;
    private GameClock clock;
    private MockPersistenceService persistence;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Manually register states for testing
        StateRegistry registry = StateRegistry.getInstance();
        registry.registerState(new AwakeState());
        registry.registerState(new AsleepState());

        clock = new GameClock();
        model = PetFactory.createNewPet("TestPet", null, clock);
        persistence = new MockPersistenceService(tempDir.resolve("test-save.dat"));
    }

    @Test
    void testStopAutosaveWhenTimerNotInitialized() {
        // Stopping autosave before initialization should not throw
        PetController controller = new PetController(model, clock, null, persistence);
        assertDoesNotThrow(controller::stopAutosave);
    }

    @Test
    void testShutdownPerformsSave() {
        PetController controller = new PetController(model, clock, null, persistence);
        
        // Shutdown should save the game
        controller.shutdown();
        
        assertEquals(1, persistence.getSaveCallCount(), "Shutdown should trigger one save");
    }

    @Test
    void testShutdownHandlesSaveError() {
        PetController controller = new PetController(model, clock, null, persistence);
        persistence.setThrowOnSave(true);
        
        // Shutdown with save error should not throw
        assertDoesNotThrow(controller::shutdown);
    }

    @Test
    void testMultipleShutdownCallsAreIdempotent() {
        PetController controller = new PetController(model, clock, null, persistence);
        
        controller.shutdown();
        controller.shutdown();
        
        // Only the first shutdown should trigger a save
        assertEquals(1, persistence.getSaveCallCount(), "Multiple shutdowns should only save once");
    }

    @Test
    void testControllerCreationWithNullComponents() {
        // Controller should be able to handle null weather and clock
        PetController controller = new PetController(model, null, null, persistence);
        assertNotNull(controller);
    }

    @Test
    void testShutdownStopsAutosaveAndSaves() {
        PetController controller = new PetController(model, clock, null, persistence);
        
        // Shutdown should stop autosave (if running) and save
        controller.shutdown();
        
        // Should have saved once
        assertEquals(1, persistence.getSaveCallCount(), "Shutdown should save once");
        
        // Stopping autosave again should work without error
        assertDoesNotThrow(controller::stopAutosave);
    }
}
