import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.inventory.Inventory;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.inventory.ItemRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InventoryTest {

    Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
    }

    @Test
    void testAddItem() {
        Item apple = ItemRegistry.get(0);
        inventory.add(apple, 1);

        assertEquals(1, inventory.getQuantity(apple));
    }

    @Test
    void testRemoveItem() {
        Item apple = ItemRegistry.get(0);
        inventory.add(apple, 2);
        inventory.remove(apple, 1);

        assertEquals(1, inventory.getQuantity(apple));
    }

    @Test
    void testReplenishItem() {
        PetModel pet = PetFactory.createNewPet("TestPet", null, null, null);
        int size = pet.getInventory().getAllOwnedItems().size();
        int min = 1;
        int max = 5;
        assertTrue((size >= min && size <= max), "owned items size not within range");
    }
}
