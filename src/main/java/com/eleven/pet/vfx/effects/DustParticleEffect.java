package com.eleven.pet.vfx.effects;

import com.eleven.pet.vfx.DustParticleFactory;
import com.eleven.pet.vfx.ParticleSystem;

public record DustParticleEffect(int intensity) implements ParticleEffect {
    private static final DustParticleFactory DUST_FACTORY = new DustParticleFactory();

    @Override
    public void start(ParticleSystem system) {
        if (system != null) {
            system.setParticleFactory(DUST_FACTORY);
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
