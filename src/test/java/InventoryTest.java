import com.eleven.pet.model.Inventory;
import com.eleven.pet.model.items.FoodItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InventoryTest {

    @Test
    void testAddItem() {
        Inventory inventory = new Inventory();
        FoodItem apple = new FoodItem("Apple", 10);
        inventory.add(apple, 1);

        assertEquals(1, inventory.getQuantity(apple));
    }

    @Test
    void testRemoveItem() {
        Inventory inventory = new Inventory();
        FoodItem banana = new FoodItem("Banana", 15);
        inventory.add(banana, 2);
        inventory.remove(banana, 1);

        assertEquals(1, inventory.getQuantity(banana));
    }
}
