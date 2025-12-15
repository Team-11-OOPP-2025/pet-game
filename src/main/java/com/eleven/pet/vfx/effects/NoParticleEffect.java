package com.eleven.pet.vfx.effects;

import com.eleven.pet.vfx.ParticleSystem;

/**
 * {@link ParticleEffect} implementation that represents the absence of
 * any particle animation.
 * <p>
 * When started, it ensures that the {@link ParticleSystem} is not animating.
 */
public class NoParticleEffect implements ParticleEffect {

    /**
     * Ensures that no particle animation is running on the given system.
     * <p>
     * If the system is {@code null}, this method does nothing.
     *
     * @param system the particle system to stop, if non-null
     */
    @Override
    public void start(ParticleSystem system) {
        // No particles to start
        if (system != null) {
            system.stopAnimation();
        }
    }

    /**
     * Stops any particle animation on the given system.
     * <p>
     * If the system is {@code null}, this method does nothing.
     *
     * @param system the particle system to stop, if non-null
     */
    @Override
    public void stop(ParticleSystem system) {
        // Nothing to stop
        if (system != null) {
            system.stopAnimation();
        }
    }
}
