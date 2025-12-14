package com.eleven.pet.inventory;

/**
 * Pure Data Class. Defines "What makes a Stat Potion a Stat Potion".
 */
public record StatPotionDefinition(String name, String statType, int effectDuration, double multiplier) {
    // Custom constructor that calls the canonical one
    public StatPotionDefinition {
        // You can add validation or transformation logic here if needed
    }
}