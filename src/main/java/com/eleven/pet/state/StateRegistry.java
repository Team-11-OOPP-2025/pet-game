package com.eleven.pet.state;

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
    
    private void loadStatesViaSPI() {
        ServiceLoader<PetState> loader = ServiceLoader.load(PetState.class);
        for (PetState state : loader) {
            stateMap.put(state.getStateName(), state);
            System.out.println("Loaded state: " + state.getStateName());
        }
    }
}
