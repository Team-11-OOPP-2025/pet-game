package com.eleven.pet.model.items;

import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;

public record FoodItem(String name, int healAmount) implements Item {

    @Override
    public void use(PetModel pet) {
        pet.getStats().modifyStat(PetStats.STAT_HUNGER, healAmount);
    }
}
