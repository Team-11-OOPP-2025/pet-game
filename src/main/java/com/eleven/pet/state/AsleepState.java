package com.eleven.pet.state;

import com.eleven.pet.model.PetModel;
import com.eleven.pet.config.GameConfig;

public class AsleepState implements PetState {
    public static final String STATE_NAME = "asleep";
    
    @Override
    public void handleFeed(PetModel pet) {
    }
    
    @Override
    public void handlePlay(PetModel pet) {
    }
    
    @Override
    public void handleSleep(PetModel pet) {
    }
    
    @Override
    public void handleClean(PetModel pet) {
    }
    
    @Override
    public void onTick(PetModel pet) {
    }
    
    @Override
    public String getStateName() {
        return STATE_NAME;
    }
}
