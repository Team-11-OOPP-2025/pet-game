package com.eleven.pet.environment.weather;

import com.eleven.pet.vfx.effects.ParticleEffect;

/**
 * Represents a concrete kind of weather in the game (e.g. sunny, rainy).
 * <p>
 * Implementations are typically discovered via {@link java.util.ServiceLoader}
 * and used by {@link WeatherSystem}.
 * </p>
 */
public interface WeatherState {
    /**
     * Human-readable name of this weather state.
     *
     * @return non-null name, e.g. {@code "Sunny"}
     */
    String getName();

    /**
     * Multiplier applied to the pet's happiness when this weather is active.
     * <p>
     * Values &gt; 1 increase happiness, values &lt; 1 decrease it, and 1 is neutral.
     *
     * @return happiness modifier for this weather
     */
    double getHappinessModifier();

    /**
     * Visual particle effect associated with this weather.
     *
     * @return {@link ParticleEffect} to render while this weather is active
     */
    ParticleEffect getParticleEffect();
}
