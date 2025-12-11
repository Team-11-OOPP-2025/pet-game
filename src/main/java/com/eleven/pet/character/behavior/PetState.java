package com.eleven.pet.character.behavior;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.minigames.MinigameResult;

public interface PetState {
    boolean handleConsume(PetModel pet, Item item);

    MinigameResult handlePlay(PetModel pet);

    void handleSleep(PetModel pet);

    void handleClean(PetModel pet);

    void onTick(PetModel pet, double timeDelta);

    String getStateName();
}


