package com.eleven.pet.character.behavior;

import java.util.Map;

/**
 * Pure Data Class. Defines "What makes a Dog a Dog".
 */

public record PetDefinition(String speciesName, float happinessDecayRate, float hungerDecayRate, float energyDecayRate, float cleanlinessDecayRate, Map<String, Integer> initialStats) {

// Still not sure what the map should be
}