package com.eleven.pet.model.items;

import com.eleven.pet.model.Item;
import com.eleven.pet.model.PetModel;

public class MedicineItem implements Item {

    private final String name;
    private final int price;

    public MedicineItem(String name, int price) {
        this.name = name;
        this.price = price;
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
        // TODO: koppla mot rätt metod i PetModel
        // Exempel:
        // pet.giveMedicine();
    }
}
