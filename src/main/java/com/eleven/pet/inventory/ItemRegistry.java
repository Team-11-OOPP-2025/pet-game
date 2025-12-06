package com.eleven.pet.inventory;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    private static final Map<Integer, Item> registry = new HashMap<>();

    static {
        // Register default items here
        ItemRegistry.register(new FoodItem(0, "Apple", 1));
    }

    public static void register(Item item) {
        registry.put(item.id(), item);
    }

    public static Item get(int id) {
        return registry.get(id);
    }
}