package com.eleven.pet.character;

import com.eleven.pet.core.GameConfig;

/**
 * Pure data record defining core behavioral parameters for a pet species.
 *
 * <p>Instances describe how quickly stats change over time for a given species,
 * e.g. a dog vs. a cat. These values are used by {@link PetModel} when
 * applying stat decay and sleep rewards.</p>
 *
 * @param speciesName          display name of the species (e.g. "Dog", "Bear")
 * @param happinessDecayRate   rate at which happiness decreases per game hour
 * @param hungerDecayRate      rate at which hunger decreases per game hour
 * @param cleanlinessDecayRate rate at which cleanliness decreases per game hour
 * @param energyPerHour        energy gained per hour of sleep
 * @param happinessPerHour     happiness gained per hour of sleep
 */
public record PetDefinition(
        String speciesName,
        double happinessDecayRate,
        double hungerDecayRate,
        double cleanlinessDecayRate,
        int energyPerHour,
        int happinessPerHour
) {

    /**
     * Creates a {@code PetDefinition} with baseline values from {@link GameConfig}.
     *
     * @param speciesName display name of the species
     */
    public PetDefinition(String speciesName) {
        this(speciesName, GameConfig.HAPPINESS_DECAY_RATE, GameConfig.HUNGER_DECAY_RATE, GameConfig.CLEANLINESS_DECAY_RATE, GameConfig.SLEEP_ENERGY_PER_HOUR, GameConfig.SLEEP_HAPPINESS_PER_HOUR);
    }

}