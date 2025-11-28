package com.eleven.pet.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Inventory {

    // List<Item> i UML, men gör den direkt som ObservableList
    private final ObservableList<Item> items =
            FXCollections.observableArrayList();

    public void addItem(Item item) {
        items.add(item);
    }

    /**
     * Add multiple copies of the same item type.
     */
    public void addItem(Item item, int amount) {
        for (int i = 0; i < amount; i++) {
            items.add(item);
        }
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public void useItem(Item item, PetModel pet) {
        // Använd bara om den faktiskt finns i inventory
        if (items.remove(item)) {
            item.use(pet);
        }
    }

    public int getAmount(Class<? extends Item> type) {
        int count = 0;
        for (Item item : items) {
            if (type.isInstance(item)) {
                count++;
            }
        }
        return count;
    }
}
