import com.eleven.pet.inventory.Inventory;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.inventory.ItemRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
