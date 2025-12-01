package com.eleven.pet.model;

import com.eleven.pet.model.items.Item;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private final Map<String, IntegerProperty> items = new HashMap<>();

    /**
     * Add multiple items.
     *
     * @param item     non-null item
     * @param quantity number to add (0 = no-op, negative = IllegalArgumentException)
     */
    public void add(Item item, int quantity) {
        if (item == null || quantity <= 0) return;
        IntegerProperty count = items.computeIfAbsent(item.name(), _ -> new SimpleIntegerProperty(0));
        count.set(count.get() + quantity);
    }

    /**
     * Consume one item instance.
     *
     * @return 1 if an item was consumed; 0 if none is consumed (item not found or quantity is zero)
     */
    public boolean remove(Item item, int quantity) {
        if (item == null || quantity <= 0) return false;
        IntegerProperty count = items.get(item.name());
        if (count == null || count.get() < quantity) return false;

        int newCount = count.get() - quantity;
        if (newCount == 0) {
            items.remove(item.name());
        } else {
            count.set(newCount);
        }
        return true;
    }

    // Get how many of that item we have
    public int getQuantity(Item item) {
        IntegerProperty count = items.get(item.name());
        return count == null ? 0 : count.get();
    }

    public boolean has(Item item) {
        return getQuantity(item) > 0;
    }

    public Map<String, IntegerProperty> getAll() {
        // Don't allow external modification
        return Collections.unmodifiableMap(items);
    }
}
