package com.eleven.pet.environment.weather;

import com.eleven.pet.vfx.effects.ParticleEffect;

public interface WeatherState {
    String getName();
    double getOverlayOpacity();
    double getHappinessModifier();
    ParticleEffect getParticleEffect();
}
