package com.eleven.pet.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Inventory {
    private final Map<Consumable, IntegerProperty> items;
    
    public Inventory() {
        this.items = new HashMap<>();
    }
    
    public void add(Consumable item, int qty) {
        items.computeIfAbsent(item, k -> new SimpleIntegerProperty(0))
             .set(items.get(item).get() + qty);
    }
    
    public boolean remove(Consumable item, int qty) {
        IntegerProperty prop = items.get(item);
        if (prop == null || prop.get() < qty) {
            return false;
        }
        prop.set(prop.get() - qty);
        return true;
    }
    
    public int getQuantity(Consumable item) {
        IntegerProperty prop = items.get(item);
        return prop == null ? 0 : prop.get();
    }
    
    public IntegerProperty getQuantityProperty(Consumable item) {
        return items.computeIfAbsent(item, k -> new SimpleIntegerProperty(0));
    }
    
    public boolean has(Consumable item) {
        return getQuantity(item) > 0;
    }
    
    public Set<Consumable> allItems() {
        return items.keySet();
    }
}
