package com.eleven.pet.inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all items in the game.
 */
public class ItemRegistry {
    private static final Map<Integer, Item> registry = new HashMap<>();

    // Static block to register default items
    static {
        // Register default items here
        ItemRegistry.register(new FoodItem(0, "Apple", "apple", 10));
        ItemRegistry.register(new FoodItem(1, "Banana", "banana", 15));
        ItemRegistry.register(new FoodItem(2, "Pear", "pear", 5));
        ItemRegistry.register(new FoodItem(3, "Grape", "grape", -15));
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
}