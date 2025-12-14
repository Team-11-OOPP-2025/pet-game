package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;

public interface Item {
    int id();

    String name();

    String imageFileName();

    String description();

    int statsRestore();

    boolean use(PetModel pet);
}
