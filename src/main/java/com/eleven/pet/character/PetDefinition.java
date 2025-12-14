package com.eleven.pet.character;

/**
 * Pure Data Class. Defines "What makes a Dog a Dog".
 */

public record PetDefinition(String speciesName, double happinessDecayRate, double hungerDecayRate, double cleanlinessDecayRate, int energyPerHour, int happinessPerHour) {
    
    // Default constructor with baseline values
    public PetDefinition(String speciesName) {
        this(speciesName, 0.3, 0.5, 0.2, 5, 2);
    }

}