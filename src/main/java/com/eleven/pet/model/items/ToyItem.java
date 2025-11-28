package com.eleven.pet.model.items;

import com.eleven.pet.model.Item;
import com.eleven.pet.model.PetModel;

public class ToyItem implements Item {

    private final String name;
    private final int price;
    private final int happinessBoost;

    public ToyItem(String name, int price, int happinessBoost) {
        this.name = name;
        this.price = price;
        this.happinessBoost = happinessBoost;
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
        // TODO: koppla mot PetModel
        // Exempel:
        // pet.increaseHappiness(happinessBoost);
    }

    public int getHappinessBoost() {
        return happinessBoost;
    }
}
