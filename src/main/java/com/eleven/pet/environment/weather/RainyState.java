package com.eleven.pet.environment.weather;

import com.eleven.pet.vfx.effects.ParticleEffect;
import com.eleven.pet.vfx.effects.RainParticleEffect;
import com.google.auto.service.AutoService;

/**
 * Rainy weather state.
 * <p>
 * Slightly decreases the pet's happiness and shows a rain particle effect.
 * </p>
 */
@AutoService(WeatherState.class)
public class RainyState implements WeatherState {
    private static final ParticleEffect RAIN_EFFECT = new RainParticleEffect(300);

    @Override
    public String getName() {
        return "Rainy";
    }

    @Override
    public double getHappinessModifier() {
        return 0.8; // Rain slightly decreases happiness
    }

    @Override
    public ParticleEffect getParticleEffect() {
        return RAIN_EFFECT;
    }
}