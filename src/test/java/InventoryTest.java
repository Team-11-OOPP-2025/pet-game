import com.eleven.pet.model.Inventory;
import com.eleven.pet.model.items.FoodItem;
import org.junit.jupiter.api.Test;

public class InventoryTest {

    @Test
    void testAddItem() {
        Inventory inventory = new Inventory();
        FoodItem apple = new FoodItem("Apple", 10);
        inventory.add(apple, 1);

        assert (inventory.getQuantity(apple) == 1);
    }

    @Test
    void testRemoveItem() {
        Inventory inventory = new Inventory();
        FoodItem banana = new FoodItem("Banana", 15);
        inventory.add(banana, 2);
        inventory.remove(banana, 1);

        assert (inventory.getQuantity(banana) == 1);
    }
}
