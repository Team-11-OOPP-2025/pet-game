package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;

public class FoodItem implements Item {
    private final int id;
    private final int healAmount;
    private final String name;

    public FoodItem(int id, String name, int healAmount) {
        this.id = id;
        this.name = name;
        this.healAmount = healAmount;

    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean use(PetModel pet) {
        // Let the Item handle the Pet's stats so the behavior is delegated to the Item
        // rather then the Model itself for better separation of concerns
        // This also allows for easier addition of new Item types in the future
        return pet.getStats().modifyStat(PetStats.STAT_HUNGER, healAmount);
    }
}
