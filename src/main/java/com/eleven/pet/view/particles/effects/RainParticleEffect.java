package com.eleven.pet.view.particles.effects;

import com.eleven.pet.view.particles.ParticleSystem;

public class RainParticleEffect implements ParticleEffect {
    @SuppressWarnings("unused")
    private final int intensity;

    public RainParticleEffect(int intensity) {
        this.intensity = intensity;
    }

    @Override
    public void start(ParticleSystem system) {
    }

    @Override
    public void stop(ParticleSystem system) {
    }
}
