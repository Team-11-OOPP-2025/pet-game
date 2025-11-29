package com.eleven.pet.service;

import com.eleven.pet.data.ItemRegistry;
import com.eleven.pet.model.Consumable;
import com.eleven.pet.model.Inventory;
import com.eleven.pet.persistence.PetDataDTO;

import java.util.HashMap;
import java.util.Map;

public class PersistenceHandler {
    public void saveInventoryToDTO(Inventory inventory, PetDataDTO dto) {
        Map<Integer, Integer> saveMap = new HashMap<>();

        for (Consumable item : inventory.allItems()) {
            saveMap.put(item.getId(), inventory.getQuantity(item));
        }

        dto.setInventoryData(saveMap);
    }

    public void loadInventoryFromDTO(PetDataDTO dto, Inventory inventory) {
        Map<Integer, Integer> loadedMap = dto.getInventoryData();
        if (loadedMap == null) return;

        for (Map.Entry<Integer, Integer> entry : loadedMap.entrySet()) {
            int id = entry.getKey();
            int qty = entry.getValue();

            Consumable item = ItemRegistry.get(id);

            if (item != null) {
                inventory.add(item, qty);
            }
        }
    }
}