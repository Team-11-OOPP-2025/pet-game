package com.eleven.pet.vfx.effects;

import com.eleven.pet.vfx.ParticleSystem;
import com.eleven.pet.vfx.TreatParticleFactory;

public record TreatParticleEffect(int intensity) implements ParticleEffect {
    private static final TreatParticleFactory TREAT_PARTICLE_FACTORY = new TreatParticleFactory();

    @Override
    public void start(ParticleSystem system) {
        if (system != null) {
            system.setParticleFactory(TREAT_PARTICLE_FACTORY);
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
