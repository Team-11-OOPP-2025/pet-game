package com.eleven.pet.model.items;

import com.eleven.pet.model.PetModel;

public interface Item {
    int id();

    String name();

    void use(PetModel pet);
}
