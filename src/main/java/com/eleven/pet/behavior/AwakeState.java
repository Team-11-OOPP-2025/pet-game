package com.eleven.pet.behavior;

import com.eleven.pet.model.Consumable;
import com.eleven.pet.model.PetModel;
import com.google.auto.service.AutoService;

@AutoService(PetState.class)
public class AwakeState implements PetState {
    public static final String STATE_NAME = "awake";
    
    @Override
    public boolean handleConsume(PetModel pet, Consumable item) {
        // TODO: Implement consume behavior for awake state
        return false;
    }
    
    @Override
    public void handlePlay(PetModel pet) {
        // TODO: Implement play behavior for awake state
    }
    
    @Override
    public void handleSleep(PetModel pet) {
        // TODO: Implement sleep behavior for awake state
    }
    
    @Override
    public void handleClean(PetModel pet) {
        // TODO: Implement clean behavior for awake state
    }
    
    @Override
    public void onTick(PetModel pet) {
        // TODO: Implement tick behavior for awake state (e.g., stat decay)
    }
    
    @Override
    public String getStateName() {
        return STATE_NAME;
    }

    @Override
    public void onEnter(PetModel pet) {
        //Todo: Implement onEnter behavior for asleep state
        return;
    }

    @Override
    public void onExit(PetModel pet) {
        //Todo: Implement onExit behavior for asleep state
        return;
    }
}
