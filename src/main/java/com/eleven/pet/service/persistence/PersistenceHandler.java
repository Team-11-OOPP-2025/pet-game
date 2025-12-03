package com.eleven.pet.service.persistence;

import com.eleven.pet.data.ItemRegistry;
import com.eleven.pet.model.Inventory;
import com.eleven.pet.model.items.Item;

import java.util.HashMap;
import java.util.Map;

public class PersistenceHandler {
    public void saveInventoryToDTO(Inventory inventory, PetDataDTO dto) {
        Map<Integer, Integer> saveMap = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : inventory.getAll().entrySet()) {
            int id = entry.getKey();
            int qty = entry.getValue();
            saveMap.put(id, qty);
        }

        dto.setInventoryData(saveMap);
    }

    public void loadInventoryFromDTO(PetDataDTO dto, Inventory inventory) {
        Map<Integer, Integer> loadedMap = dto.getInventoryData();
        if (loadedMap == null) return;

        for (Map.Entry<Integer, Integer> entry : loadedMap.entrySet()) {
            int id = entry.getKey();
            int qty = entry.getValue();

            Item item = ItemRegistry.get(id);

            if (item != null) {
                inventory.add(item, qty);
            }
        }
    }
}