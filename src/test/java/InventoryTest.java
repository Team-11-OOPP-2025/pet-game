import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.inventory.Inventory;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.inventory.ItemRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Inventory} behavior, including adding,
 * removing, and initial pet inventory contents.
 */
public class InventoryTest {

    Inventory inventory;

    /**
     * Initializes a fresh {@link Inventory} instance before each test.
     */
    @BeforeEach
    void setUp() {
        inventory = new Inventory();
    }

    /**
     * Verifies that adding an item increases its quantity as expected.
     */
    @Test
    void testAddItem() {
        Item apple = ItemRegistry.get(0);
        inventory.add(apple, 1);

        assertEquals(1, inventory.getQuantity(apple));
    }

    /**
     * Verifies that removing an item decreases its quantity as expected.
     */
    @Test
    void testRemoveItem() {
        Item apple = ItemRegistry.get(0);
        inventory.add(apple, 2);
        inventory.remove(apple, 1);

        assertEquals(1, inventory.getQuantity(apple));
    }

    /**
     * Ensures that a newly created pet starts with an inventory size
     * within the expected bounds.
     */
    @Test
    void testReplenishItem() {
        PetModel pet = PetFactory.createNewPet("TestPet", null, null);
        int size = pet.getInventory().getAllOwnedItems().size();
        int min = 1;
        int max = 5;
        assertTrue((size >= min && size <= max), "owned items size not within range");
    }
}
