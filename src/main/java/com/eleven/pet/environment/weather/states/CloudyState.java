package com.eleven.pet.environment.weather.states;

import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.particle.NoParticleEffect;
import com.eleven.pet.particle.ParticleEffect;
import com.google.auto.service.AutoService;

@AutoService(WeatherState.class)
public class CloudyState implements WeatherState {
    @Override
    public String getName() {
        return "Cloudy";
    }
    
    @Override
    public String getOverlayImageName() {
        return "clouds_overlay.png";
    }
    
    @Override
    public double getOverlayOpacity() {
        return 0.15;
    }
    
    @Override
    public double getHappinessModifier() {
        return 0.0; // Neutral
    }
    
    @Override
    public ParticleEffect getParticleEffect() {
        return new NoParticleEffect();
    }
}
