package com.eleven.pet.model;

public interface Item {

    String getName();

    int getPrice();

    void use(PetModel pet);
}
