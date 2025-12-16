package com.eleven.pet.vfx.effects;

import com.eleven.pet.vfx.ParticleSystem;
import com.eleven.pet.vfx.RainParticleFactory;

/**
 * Particle effect that renders a rain animation using a {@link ParticleSystem}.
 * <p>
 * The {@code intensity} controls how many rain particles are spawned
 * when the animation is started.
 * </p>
 */
public record RainParticleEffect(int intensity) implements ParticleEffect {

    /**
     * Shared particle factory used to create rain particles.
     */
    private static final RainParticleFactory RAIN_FACTORY = new RainParticleFactory();

    @Override
    public void start(ParticleSystem system) {
        /**
         * Starts the rain effect on the given particle system.
         * <p>
         * If the system is {@code null}, this method does nothing.
         *
         * @param system the particle system on which to start the rain animation
         */
        if (system != null) {
            system.setParticleFactory(RAIN_FACTORY);
            system.startAnimation(intensity);
        }
    }

    @Override
    public void stop(ParticleSystem system) {
        /**
         * Stops the rain effect on the given particle system.
         * <p>
         * If the system is {@code null}, this method does nothing.
         *
         * @param system the particle system on which to stop the rain animation
         */
        if (system != null) {
            system.stopAnimation();
        }
    }
}
