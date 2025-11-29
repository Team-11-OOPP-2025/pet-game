package com.eleven.pet.model;

public interface Minigame {
    String getName();
    MinigameResult play(PetModel pet);
}
