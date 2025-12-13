package com.eleven.pet.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/**
 * Registry for all items in the game.
 */
public class ItemRegistry {
    private static final Map<Integer, Item> registry = new HashMap<>();
    private static final Random random = new Random();

    // Static block to register default items
    static {
        // Register default items here
        ItemRegistry.register(new FoodItem(0, "Apple", 10));
        ItemRegistry.register(new FoodItem(1, "Banana", 15));
        ItemRegistry.register(new FoodItem(2, "Pear", 5));
        ItemRegistry.register(new FoodItem(3, "Grape", -15));
        ItemRegistry.register(new FoodItem(4, "Golden Apple", 50));
        
        // Create sleep recovery potion definition
        StatPotionDefinition sleepBoostDef = new StatPotionDefinition(
            "Sleep Recovery Potion", 
            "SLEEP_RECOVERY", 
            10,  // 10 sekduration
            2.0   // 2x multiplier
        );
        ItemRegistry.register(new StatPotion(5, sleepBoostDef, 2));
    }

    /**
     * Registers an {@link Item} in the registry.
     *
     * @param item The item to register.
     */
    public static void register(Item item) {
        registry.put(item.id(), item);
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param id The ID of the item.
     * @return {@link Item} with the specified ID, or null if not found.
     */
    public static Item get(int id) {
        return registry.get(id);
    }

    /**
     * Retrieves a random item from the registry.
     *
     * @return A random {@link Item}, or null if registry is empty.
     */
    public static Item getRandomItem() {
        if (registry.isEmpty()) {
            return null;
        }
        List<Item> items = new ArrayList<>(registry.values());
        return items.get(random.nextInt(items.size()));
    }
}