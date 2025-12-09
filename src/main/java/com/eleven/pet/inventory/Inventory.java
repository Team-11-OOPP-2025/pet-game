package com.eleven.pet.inventory;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the inventory of items owned by the player.
 */
public class Inventory {
    private final Map<Integer, IntegerProperty> items = FXCollections.observableHashMap();

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

        // Don't keep zero-quantity items in the inventory
        if (newCount == 0) {
            items.remove(item.id());
        }

        return true;
    }

    /**
     * Get the quantity of a specific item.
     *
     * @param item non-null item
     * @return quantity owned (0 if none)
     */
    public int getQuantity(Item item) {
        IntegerProperty count = items.get(item.id());
        return count == null ? 0 : count.get();
    }

    /**
     * Check if the inventory has at least one of the specified item.
     *
     * @param item non-null item
     * @return true if at least one is owned; false otherwise
     */
    public boolean has(Item item) {
        return getQuantity(item) > 0;
    }

    /**
     * Get a map of all owned items and their quantities.
     *
     * @return unmodifiable map of item IDs to quantities
     */
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

    /**
     * Get the IntegerProperty representing the quantity of a specific item.
     *
     * @param item non-null item
     * @return IntegerProperty for the item's quantity
     */
    public IntegerProperty amountProperty(Item item) {
        return items.computeIfAbsent(item.id(), _ -> new SimpleIntegerProperty(0));
    }

    public ObservableMap<Integer, IntegerProperty> getItems() {
        return (ObservableMap<Integer, IntegerProperty>) items;
    }
}
