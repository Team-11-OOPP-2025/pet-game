package com.eleven.pet.model;

import javafx.beans.property.IntegerProperty;

import java.util.Map;
import java.util.Set;

public class Inventory {
    private final Map<Consumable, IntegerProperty> items;
    
    public Inventory() {
        // TODO: Initialize inventory storage
        this.items = null;
    }
    
    public void add(Consumable item, int quantity) {
        // TODO: Implement add item to inventory
    }
    
    public boolean remove(Consumable item, int quantity) {
        // TODO: Implement remove item from inventory
        return false;
    }
    
    public int getQuantity(Consumable item) {
        // TODO: Implement get quantity of item
        return 0;
    }
    
    public boolean has(Consumable item) {
        // TODO: Implement check if item exists in inventory
        return false;
    }
    
    public Set<Consumable> allItems() {
        // TODO: Implement return all items in inventory
        return null;
    }
}
