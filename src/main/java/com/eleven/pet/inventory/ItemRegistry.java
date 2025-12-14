package com.eleven.pet.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Registry for all items in the game.
 */
public class ItemRegistry {
    private static final Map<Integer, Item> registry = new HashMap<>();
    private static final Random random = new Random();

    // Static block to register default items
    static {
        // Food Items
        /*ItemRegistry.register(new FoodItem(0, "Apple", "apple", 10));
        ItemRegistry.register(new FoodItem(1, "Banana", "banana", 15));
        ItemRegistry.register(new FoodItem(2, "Pear", "pear", 5));
        ItemRegistry.register(new FoodItem(3, "Grape", "grape", -15));
        */
        // Potion Items
        StatPotionDefinition sleepBoostDef = new StatPotionDefinition(
            "EnergyPotion", 
            "ENERGY", 
            10,  // 10 sec duration
            10.0   // 10x multiplier
        );
        ItemRegistry.register(new StatPotion(0, sleepBoostDef));
    }

    /**
     * Registers an {@link Item} in the registry.
     */
    public static void register(Item item) {
        registry.put(item.id(), item);
    }

    /**
     * Retrieves an item by its ID.
     */
    public static Item get(int id) {
        return registry.get(id);
    }
    
    /**
     * Retrieves a random item from the registry.
     */
    public static Item getRandomItem() {
        if (registry.isEmpty()) {
            return null;
        }
        List<Item> items = new ArrayList<>(registry.values());
        return items.get(random.nextInt(items.size()));
    }
}