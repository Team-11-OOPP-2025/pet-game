package com.eleven.pet.inventory;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private final Map<Integer, IntegerProperty> items = new HashMap<>();

    /**
     * Add multiple items.
     *
     * @param item     non-null item
     * @param quantity number to add (0 = no-op, negative = IllegalArgumentException)
     */
    public void add(Item item, int quantity) {
        if (item == null || quantity <= 0) return;
        IntegerProperty count = items.computeIfAbsent(item.id(), _ -> new SimpleIntegerProperty(0));
        count.set(count.get() + quantity);
    }

    /**
     * Consume one item instance.
     *
     * @return 1 if an item was consumed; 0 if none is consumed (item not found or quantity is zero)
     */
    public boolean remove(Item item, int quantity) {
        if (item == null || quantity <= 0) return false;
        IntegerProperty count = items.get(item.id());
        if (count == null || count.get() < quantity) return false;

        int newCount = count.get() - quantity;
        count.set(newCount);

        return true;
    }

    // Get how many of that item we have
    public int getQuantity(Item item) {
        IntegerProperty count = items.get(item.id());
        return count == null ? 0 : count.get();
    }

    public boolean has(Item item) {
        return getQuantity(item) > 0;
    }

    // Item ID and quantity map for persistence
    public Map<Integer, Integer> getAllOwnedItems() {
        Map<Integer, Integer> result = new HashMap<>();
        for (Integer id : items.keySet()) {
            Item item = ItemRegistry.get(id);
            int qty;
            if (item == null) continue;
            if ((qty = getQuantity(item)) <= 0) continue;
            result.put(item.id(), qty);
        }
        return Collections.unmodifiableMap(result);
    }

    // Property for JavaFX binding
    public IntegerProperty amountProperty(Item item) {
        return items.computeIfAbsent(item.id(), _ -> new SimpleIntegerProperty(0));
    }
}
