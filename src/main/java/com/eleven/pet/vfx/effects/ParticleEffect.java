package com.eleven.pet.vfx.effects;

import com.eleven.pet.vfx.ParticleSystem;

public interface ParticleEffect {
    void start(ParticleSystem system);

    void stop(ParticleSystem system);
}
