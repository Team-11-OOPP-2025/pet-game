package com.eleven.pet.inventory;

import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.core.GameConfig;
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
        Inventory inv = pet.getInventory();
        int totalQuantity = inv.getAllOwnedItems().size();

        // mininum amount of item is either gonna be DAILY_FOOD_MIN or DAILY_CLEANING_MIN
        int perItemMin = Math.min(GameConfig.DAILY_FOOD_MIN, GameConfig.DAILY_CLEANING_MIN);

        // max is either gonna be DAILY_FOOD_MAX or DAILY_CLEANING_MAX times 5 (replenish loops 1..5 times)
        // However, this is a weak test since the randomness can be changed.
        int perItemMax = Math.max(GameConfig.DAILY_FOOD_MAX, GameConfig.DAILY_CLEANING_MAX);
        int maxPossible = perItemMax * 5; // replenish loops 1..5 times

        assertTrue(totalQuantity >= perItemMin && totalQuantity <= maxPossible,
                "total owned items quantity not within expected range");
    }
}
