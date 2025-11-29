package com.eleven.pet.behavior;

import com.eleven.pet.model.Consumable;
import com.eleven.pet.model.PetModel;

public interface PetState {
    boolean handleConsume(PetModel pet, Consumable item);
    void handlePlay(PetModel pet);
    void handleSleep(PetModel pet);
    void handleClean(PetModel pet);
    void onTick(PetModel pet);
    String getStateName();
}
