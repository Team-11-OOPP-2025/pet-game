package com.eleven.pet.environment.weather;

import com.eleven.pet.vfx.effects.NoParticleEffect;
import com.eleven.pet.vfx.effects.ParticleEffect;
import com.google.auto.service.AutoService;

/**
 * Cloudy weather state.
 * <p>
 * Has a neutral effect on happiness and no particle effects.
 * </p>
 */
@AutoService(WeatherState.class)
public class CloudyState implements WeatherState {
    private static final ParticleEffect NO_PARTICLES = new NoParticleEffect();
    
    @Override
    public String getName() {
        return "Cloudy";
    }

    @Override
    public double getHappinessModifier() {
        return 1.0; // Neutral effect
    }
    
    @Override
    public ParticleEffect getParticleEffect() {
        return NO_PARTICLES;
    }
}