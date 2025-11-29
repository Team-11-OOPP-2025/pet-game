package com.eleven.pet.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.HashMap;
import java.util.Map;

public class Inventory {

    // Key: item name (e.g. "Apple")
    private final Map<String, IntegerProperty> items = new HashMap<>();

    // Add single
    public void addItem(Item item) {
        addItem(item, 1);
    }

    // Add multiple items
    public void addItem(Item item, int amount) {
        if (amount <= 0) return;

        String name = item.getName();
        IntegerProperty prop = items.computeIfAbsent(
                name, n -> new SimpleIntegerProperty(0)
        );
        prop.set(prop.get() + amount);
    }

    // Consume one item
    public boolean consumeItem(Item item) {
        String name = item.getName();
        IntegerProperty prop = items.get(name);

        if (prop == null || prop.get() <= 0) return false;

        prop.set(prop.get() - 1);
        return true;
    }

    // Get how many of that item we have
    public int getAmount(Item item) {
        return amountProperty(item).get();
    }

    // Property for JavaFX binding
    public IntegerProperty amountProperty(Item item) {
        return items.computeIfAbsent(
                item.getName(),
                n -> new SimpleIntegerProperty(0)
        );
    }
}
