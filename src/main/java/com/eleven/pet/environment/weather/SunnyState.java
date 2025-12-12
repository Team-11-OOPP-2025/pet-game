package com.eleven.pet.environment.weather;

import com.eleven.pet.vfx.effects.NoParticleEffect;
import com.eleven.pet.vfx.effects.ParticleEffect;
import com.google.auto.service.AutoService;

@AutoService(WeatherState.class)
public class SunnyState implements WeatherState {
    private static final ParticleEffect NO_PARTICLES = new NoParticleEffect();
    
    @Override
    public String getName() {
        return "Sunny";
    }

    @Override
    public double getHappinessModifier() {
        return 1.2; // Sunny weather boosts happiness
    }
    
    @Override
    public ParticleEffect getParticleEffect() {
        return NO_PARTICLES;
    }
}