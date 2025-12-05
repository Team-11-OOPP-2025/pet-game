import com.eleven.pet.config.GameItems;
import com.eleven.pet.model.Inventory;
import com.eleven.pet.model.items.Item;
import com.eleven.pet.model.items.ItemRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InventoryTest {

    Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
        GameItems.init();
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
