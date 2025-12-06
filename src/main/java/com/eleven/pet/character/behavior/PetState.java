package com.eleven.pet.character.behavior;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.inventory.Item;

public interface PetState {
    boolean handleConsume(PetModel pet, Item item);
    void handlePlay(PetModel pet);
    void handleSleep(PetModel pet);
    void handleClean(PetModel pet);
    void onTick(PetModel pet);
    String getStateName();

    void onEnter(PetModel pet);
    void onExit(PetModel pet);
}


