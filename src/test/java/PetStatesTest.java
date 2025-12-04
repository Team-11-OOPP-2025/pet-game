import com.eleven.pet.behavior.AwakeState;
import com.eleven.pet.behavior.PetState;
import com.eleven.pet.behavior.StateRegistry;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.items.Item;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;


public class PetStatesTest {

    @Test
    void testStateNames() {
        AwakeState awakeState = new AwakeState();
        assertEquals("awake", awakeState.getStateName());
    }

    @Test
    void testRegistrySingleton() {
        StateRegistry instance1 = StateRegistry.getInstance();
        StateRegistry instance2 = StateRegistry.getInstance();

        assertSame(instance1, instance2, "getInstance() should return the same instance");
    }

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
            public void handlePlay(PetModel pet) {
            }

            @Override
            public void handleSleep(PetModel pet) {
            }

            @Override
            public void handleClean(PetModel pet) {
            }

            @Override
            public void onTick(PetModel pet) {
            }

            @Override
            public String getStateName() {
                return "TestState";
            }

            @Override
            public void onEnter(PetModel pet) {
            }

            @Override
            public void onExit(PetModel pet) {
            }
        };

        registry.registerState(mockState);
        PetState retrieved = registry.getState("TestState");

        assertSame(mockState, retrieved, "Retrieved state should be the same as registered state");
        assertEquals("TestState", retrieved.getStateName(), "State name should match");
    }
}
