package com.eleven.pet.vfx.effects;

import com.eleven.pet.vfx.ParticleSystem;

public class RainParticleEffect implements ParticleEffect {
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
