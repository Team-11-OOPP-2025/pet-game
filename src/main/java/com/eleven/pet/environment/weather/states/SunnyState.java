package com.eleven.pet.environment.weather.states;

import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.particle.NoParticleEffect;
import com.eleven.pet.particle.ParticleEffect;
import com.google.auto.service.AutoService;

@AutoService(WeatherState.class)
public class SunnyState implements WeatherState {
    @Override
    public String getName() {
        return "Sunny";
    }
    
    @Override
    public String getOverlayImageName() {
        return null; // No overlay for sunny
    }
    
    @Override
    public double getOverlayOpacity() {
        return 0.0;
    }
    
    @Override
    public double getHappinessModifier() {
        return 0.5; // Slightly positive
    }
    
    @Override
    public ParticleEffect getParticleEffect() {
        return new NoParticleEffect();
    }
}
