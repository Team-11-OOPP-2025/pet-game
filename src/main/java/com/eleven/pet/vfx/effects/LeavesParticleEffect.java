package com.eleven.pet.vfx.effects;

import com.eleven.pet.vfx.ParticleSystem;

public class LeavesParticleEffect implements ParticleEffect {
    private final int intensity;

    public LeavesParticleEffect(int intensity) {
        this.intensity = intensity;
    }

    @Override
    public void start(ParticleSystem system) {
    }

    @Override
    public void stop(ParticleSystem system) {
    }
}
