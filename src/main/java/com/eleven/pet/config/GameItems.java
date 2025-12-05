package com.eleven.pet.config;

import com.eleven.pet.model.items.FoodItem;
import com.eleven.pet.model.items.ItemRegistry;

public class GameItems {
    public static final int ID_APPLE = 0;

    public static void init() {
        ItemRegistry.register(new FoodItem(ID_APPLE, "Apple", 1));
    }
}