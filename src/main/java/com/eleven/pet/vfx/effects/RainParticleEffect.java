package com.eleven.pet.vfx.effects;

import com.eleven.pet.vfx.ParticleSystem;
import com.eleven.pet.vfx.RainParticleFactory;

public record RainParticleEffect(int intensity) implements ParticleEffect {
    private static final RainParticleFactory RAIN_FACTORY = new RainParticleFactory();

    @Override
    public void start(ParticleSystem system) {
        if (system != null) {
            system.setParticleFactory(RAIN_FACTORY);
            system.startAnimation(intensity);
        }
    }

    @Override
    public void stop(ParticleSystem system) {
        if (system != null) {
            system.stopAnimation();
        }
    }
}
