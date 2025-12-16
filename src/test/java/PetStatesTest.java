import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.character.behavior.AwakeState;
import com.eleven.pet.character.behavior.PetState;
import com.eleven.pet.character.behavior.StateRegistry;
import com.eleven.pet.inventory.Item;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for pet behavior states and the {@link StateRegistry}.
 */
public class PetStatesTest {

    /**
     * Ensures that {@link AwakeState} exposes the correct state name constant
     * and allows playing minigames.
     */
    @Test
    void testAwakeState() {
        PetModel pet = PetFactory.createNewPet("TestPet", null, null);
        AwakeState awakeState = new AwakeState();
        assertEquals(AwakeState.STATE_NAME, awakeState.getStateName());
        assertTrue(awakeState.canPlay(pet), "Awake pet should be able to play");
    }

    /**
     * Ensures that {@link AsleepState} exposes the correct state name constant
     * and prevents playing minigames.
     */
    @Test
    void testAsleepState() {
        AsleepState asleepState = new AsleepState();
        assertEquals(AsleepState.STATE_NAME, asleepState.getStateName());
        assertFalse(asleepState.canPlay(null), "Asleep pet should not be able to play");
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
            public boolean canPlay(PetModel pet) {
                return true;
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