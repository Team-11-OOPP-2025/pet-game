import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.behavior.AwakeState;
import com.eleven.pet.character.behavior.PetState;
import com.eleven.pet.character.behavior.StateRegistry;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.minigames.MinigameResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for pet behavior states and the {@link StateRegistry}.
 */
public class PetStatesTest {

    /**
     * Ensures that {@link AwakeState} exposes the correct state name constant.
     */
    @Test
    void testStateNames() {
        AwakeState awakeState = new AwakeState();
        assertEquals(AwakeState.STATE_NAME, awakeState.getStateName());
    }

    /**
     * Verifies that {@link StateRegistry} follows the singleton pattern.
     */
    @Test
    void testRegistrySingleton() {
        StateRegistry instance1 = StateRegistry.getInstance();
        StateRegistry instance2 = StateRegistry.getInstance();

        assertSame(instance1, instance2, "getInstance() should return the same instance");
    }

    /**
     * Verifies that states registered in {@link StateRegistry} can be retrieved by name.
     */
    @Test
    void testRegistryRetrieval() {
        StateRegistry registry = StateRegistry.getInstance();

        // Create a mock state
        PetState mockState = new PetState() {
            @Override
            public boolean handleConsume(PetModel pet, Item item) {
                return false;
            }

            @Override
            public MinigameResult handlePlay(PetModel pet) {
                return null;
            }

            @Override
            public void handleSleep(PetModel pet) {
            }

            @Override
            public void handleClean(PetModel pet) {
            }

            @Override
            public void onTick(PetModel pet, double timeDelta) {
            }

            @Override
            public String getStateName() {
                return "TestState";
            }
        };

        registry.registerState(mockState);
        PetState retrieved = registry.getState("TestState");

        assertSame(mockState, retrieved, "Retrieved state should be the same as registered state");
        assertEquals("TestState", retrieved.getStateName(), "State name should match");
    }
}
