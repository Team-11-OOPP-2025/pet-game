package com.eleven.pet.environment.weather.states;

import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.particle.ParticleEffect;
import com.eleven.pet.particle.RainParticleEffect;

public class RainyState implements WeatherState {
    @Override
    public String getName() {
        return null;
    }
    
    @Override
    public String getOverlayImageName() {
        return null;
    }
    
    @Override
    public double getOverlayOpacity() {
        return 0;
    }
    
    @Override
    public double getHappinessModifier() {
        return 0;
    }
    
    @Override
    public ParticleEffect getParticleEffect() {
        return new RainParticleEffect(0);
    }
}
