package com.eleven.pet.model.items;

import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;

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
    public void use(PetModel pet) {
        pet.getStats().modifyStat(PetStats.STAT_HUNGER, healAmount);
    }
}
