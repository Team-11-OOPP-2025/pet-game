package com.eleven.pet.character.behavior;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class StateRegistry {
    private static StateRegistry instance;
    private final Map<String, PetState> stateMap;
    
    private StateRegistry() {
        this.stateMap = new HashMap<>();
        loadStatesViaSPI();
    }
    
    public static StateRegistry getInstance() {
        if (instance == null) {
            instance = new StateRegistry();
        }
        return instance;
    }
    
    public PetState getState(String name) {
        return stateMap.get(name);
    }
    
    public void registerState(PetState state) {
        stateMap.put(state.getStateName(), state);
    }
    
    private void loadStatesViaSPI() {
        ServiceLoader<PetState> loader = ServiceLoader.load(PetState.class);
        for (PetState state : loader) {
            stateMap.put(state.getStateName(), state);
            System.out.println("Loaded state: " + state.getStateName());
        }
        
        // Fallback: if no states were loaded via SPI, manually register them
        if (stateMap.isEmpty()) {
            System.out.println("No states loaded via SPI, registering manually...");
            registerState(new AwakeState());
            registerState(new AsleepState());
        }
    }
}
