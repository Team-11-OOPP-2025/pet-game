package com.eleven.pet.environment.weather.states;

import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.particle.ParticleEffect;
import com.eleven.pet.particle.RainParticleEffect;
import com.google.auto.service.AutoService;

@AutoService(WeatherState.class)
public class RainyState implements WeatherState {
    @Override
    public String getName() {
        return "Rainy";
    }
    
    @Override
    public String getOverlayImageName() {
        return "rain_overlay.png";
    }
    
    @Override
    public double getOverlayOpacity() {
        return 0.3;
    }
    
    @Override
    public double getHappinessModifier() {
        return -0.3; // Slightly negative
    }
    
    @Override
    public ParticleEffect getParticleEffect() {
        return new RainParticleEffect(50);
    }
}
