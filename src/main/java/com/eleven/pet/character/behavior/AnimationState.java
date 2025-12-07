
package com.eleven.pet.character.behavior;

public enum AnimationState {
    VERY_HAPPY(71, 100),    
    NEUTRAL(40, 70),        
    SAD(20, 39),        
    VERY_SAD(0, 19);             
    
    private final int minHappiness;
    private final int maxHappiness;
    
    AnimationState(int min, int max) {
        this.minHappiness = min;
        this.maxHappiness = max;
    }
    
    public static AnimationState fromHappiness(int happiness) {
        for (AnimationState state : values()) {
            if (happiness >= state.minHappiness && happiness <= state.maxHappiness) {
                return state;
            }
        }
        return NEUTRAL;
    }
    
    

    public int getMinHappiness() {
        return minHappiness;
    }
    
    public int getMaxHappiness() {
        return maxHappiness;
    }
}