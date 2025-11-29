package com.eleven.pet.model;

public interface Consumable {
    int getId();
    String getName();
    void applyEffect(PetModel pet);
}
