package com.eleven.pet.behavior;

import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.items.Item;

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


