package com.eleven.pet.model.items;

import com.eleven.pet.model.Item;
import com.eleven.pet.model.PetModel;

public class FoodItem implements Item {

    private final String name;
    private final int price;
    private final int healAmount;

    public FoodItem(String name, int price, int healAmount) {
        this.name = name;
        this.price = price;
        this.healAmount = healAmount;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public void use(PetModel pet) {
        // TODO: justera efter hur PetModel ser ut sen
        // Exempel:
        // pet.increaseHunger(-healAmount); // blir mindre hungrig
        // eller: pet.heal(healAmount);
    }

    public int getHealAmount() {
        return healAmount;
    }
}
