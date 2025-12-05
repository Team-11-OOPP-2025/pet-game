package com.eleven.pet.view.particles.effects;

import com.eleven.pet.view.particles.ParticleSystem;

public class NoParticleEffect implements ParticleEffect {
    @Override
    public void start(ParticleSystem system) {
        // No particles to start
        if (system != null) {
            system.stopAnimation();
        }
    }

    @Override
    public void stop(ParticleSystem system) {
        // Nothing to stop
        if (system != null) {
            system.stopAnimation();
        }
    }
}
