package com.eleven.pet.state;

import com.eleven.pet.model.PetModel;

public interface PetState {
    void handleFeed(PetModel pet);
    void handlePlay(PetModel pet);
    void handleSleep(PetModel pet);
    void handleClean(PetModel pet);
    void onTick(PetModel pet);
    String getStateName();
}
