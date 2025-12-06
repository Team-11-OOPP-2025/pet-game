package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;

public interface Item {
    int id();

    String name();

    boolean use(PetModel pet);
}
