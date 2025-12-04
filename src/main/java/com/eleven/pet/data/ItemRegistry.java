package com.eleven.pet.data;

import com.eleven.pet.model.items.Item;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    private static final Map<Integer, Item> registry = new HashMap<>();

    public static void register(Item item) {
        registry.put(item.id(), item);
    }

    public static Item get(int id) {
        return registry.get(id);
    }
}