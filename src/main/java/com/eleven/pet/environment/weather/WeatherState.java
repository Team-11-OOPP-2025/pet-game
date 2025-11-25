package com.eleven.pet.environment.weather;

import com.eleven.pet.particle.ParticleEffect;

public interface WeatherState {
    String getName();
    String getOverlayImageName();
    double getOverlayOpacity();
    double getHappinessModifier();
    ParticleEffect getParticleEffect();
}
