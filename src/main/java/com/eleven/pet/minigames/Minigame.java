package com.eleven.pet.minigames;

import com.eleven.pet.character.PetModel;

public interface Minigame {
    String getName();
    MinigameResult play(PetModel pet);
}
