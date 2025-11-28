package com.eleven.pet.view.particles.effects;

import com.eleven.pet.view.particles.ParticleSystem;

public interface ParticleEffect {
    void start(ParticleSystem system);

    void stop(ParticleSystem system);
}
