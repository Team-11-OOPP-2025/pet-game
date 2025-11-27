// src/main/java/com/eleven/pet/model/Inventory.java
package com.eleven.pet.model;

import java.util.HashMap;
import java.util.Map;

public class Inventory {

    private final Map<String, Integer> items = new HashMap<>();

    public void addItem(String name, int amount) {
        items.merge(name, amount, Integer::sum);
    }

    public int getItemCount(String name) {
        return items.getOrDefault(name, 0);
    }
}
