package com.eleven.pet.data;

import com.eleven.pet.model.Consumable;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    private static final Map<Integer, Consumable> registry = new HashMap<>();

    public static void register(Consumable item) {
        registry.put(item.getId(), item);
    }

    public static Consumable get(int id) {
        return registry.get(id);
    }
}