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
 * <p>
 * Quantities are tracked per item ID and exposed via JavaFX properties
 * for UI binding.
 * </p>
 */
public class Inventory {
    private final Map<Integer, IntegerProperty> items = FXCollections.observableHashMap();
  
    /**
     * Adds multiple instances of an item to the inventory.
     *
     * @param item     item to add; ignored if {@code null}
     * @param quantity number to add; ignored if {@code <= 0}
     */
    public void add(Item item, int quantity) {
        if (item == null || quantity <= 0) return;
        IntegerProperty count = items.computeIfAbsent(item.id(), _ -> new SimpleIntegerProperty(0));
        count.set(count.get() + quantity);
    }

    /**
     * Removes multiple instances of an item from the inventory.
     *
     * @param item     item to remove; ignored if {@code null}
     * @param quantity number to remove; must be positive
     */
    public void remove(Item item, int quantity) {
        if (item == null || quantity <= 0) return;
        IntegerProperty count = items.get(item.id());
        if (count == null || count.get() < quantity) return;

        int newCount = count.get() - quantity;
        count.set(newCount);
        if (newCount == 0) {
            items.remove(item.id());
        }
    }

    /**
     * Check if the inventory has enough of an item to remove.
     *
     * @param item     item to check
     * @param quantity number to check for
     * @return {@code true} if the requested quantity can be removed,
     *         {@code false} if the item is missing or insufficient quantity is available
     */
    public boolean canRemove(Item item, int quantity) {
        if (item == null || quantity <= 0) return false;
        IntegerProperty count = items.get(item.id());
        return count != null && count.get() >= quantity;
    }

    /**
     * Gets the quantity of a specific item.
     *
     * @param item item whose quantity is queried
     * @return quantity owned, or {@code 0} if none
     */
    public int getQuantity(Item item) {
        IntegerProperty count = items.get(item.id());
        return count == null ? 0 : count.get();
    }

    /**
     * Checks whether the inventory contains at least one of the given item.
     *
     * @param item item to check
     * @return {@code true} if at least one instance is present
     */
    public boolean has(Item item) {
        return getQuantity(item) > 0;
    }

    /**
     * Returns an unmodifiable snapshot of all owned items and their quantities.
     *
     * @return map of item IDs to owned quantities (only items with quantity &gt; 0)
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
     * Gets the JavaFX property representing the quantity of a specific item.
     * <p>
     * If the item does not yet exist in the inventory, it will be added with quantity {@code 0}.
     * </p>
     *
     * @param item item whose quantity property is requested
     * @return {@link IntegerProperty} bound to the item's quantity
     */
    public IntegerProperty amountProperty(Item item) {
        return items.computeIfAbsent(item.id(), _ -> new SimpleIntegerProperty(0));
    }

    /**
     * Returns the observable map backing the inventory.
     * <p>
     * Keys are item IDs; values are quantity properties.
     * </p>
     *
     * @return observable map of item IDs to quantity properties
     */
    public ObservableMap<Integer, IntegerProperty> getItems() {
        return (ObservableMap<Integer, IntegerProperty>) items;
    }
}
