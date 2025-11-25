package com.eleven.pet.environment.weather.states;

import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.particle.NoParticleEffect;
import com.eleven.pet.particle.ParticleEffect;

public class CloudyState implements WeatherState {
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
        return new NoParticleEffect();
    }
}
