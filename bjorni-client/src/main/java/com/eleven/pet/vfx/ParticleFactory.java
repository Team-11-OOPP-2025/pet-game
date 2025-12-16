package com.eleven.pet.vfx;

/**
 * Factory interface for creating {@link Particle} instances for a
 * particular canvas size.
 */
public interface ParticleFactory {

    /**
     * Creates a new {@link Particle} positioned relative to the given
     * canvas dimensions.
     *
     * @param width  width of the drawing area in pixels
     * @param height height of the drawing area in pixels
     * @return a new particle instance; may be {@code null} if creation fails
     */
    Particle createParticle(double width, double height);
}
