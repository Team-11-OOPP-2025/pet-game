package com.eleven.pet.vfx.effects;

import com.eleven.pet.vfx.ParticleSystem;

/**
 * Abstraction for particle-based visual effects that can be applied to
 * a {@link ParticleSystem}.
 */
public interface ParticleEffect {

    /**
     * Starts this particle effect on the given particle system.
     *
     * @param system the particle system on which to start the effect;
     *               may be {@code null}, in which case implementations
     *               are expected to do nothing
     */
    void start(ParticleSystem system);

    /**
     * Stops this particle effect on the given particle system.
     *
     * @param system the particle system on which to stop the effect;
     *               may be {@code null}, in which case implementations
     *               are expected to do nothing
     */
    void stop(ParticleSystem system);
}
