package com.eleven.pet.behavior;

import com.eleven.pet.model.Consumable;
import com.eleven.pet.model.PetModel;
import com.google.auto.service.AutoService;

@AutoService(PetState.class)
public class AsleepState implements PetState {
    public static final String STATE_NAME = "asleep";
    
    @Override
    public boolean handleConsume(PetModel pet, Consumable item) {
        // TODO: Implement consume behavior for asleep state
        return false;
    }
    
    @Override
    public void handlePlay(PetModel pet) {
        // TODO: Implement play behavior for asleep state
    }
    
    @Override
    public void handleSleep(PetModel pet) {
        // TODO: Implement sleep behavior for asleep state
    }
    
    @Override
    public void handleClean(PetModel pet) {
        // TODO: Implement clean behavior for asleep state
    }
    
    @Override
    public void onTick(PetModel pet) {
        // TODO: Implement tick behavior for asleep state (e.g., energy recovery, wake up logic)
    }
    
    @Override
    public String getStateName() {
        return STATE_NAME;
    }
}
