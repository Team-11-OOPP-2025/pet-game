package com.eleven.pet.vfx;

/**
 * Factory for creating {@link RainParticle} instances with randomized
 * positions and velocities to simulate continuous rainfall.
 */
public class RainParticleFactory implements ParticleFactory {
    private static final double INITIAL_VELOCITY_Y = 200.0; // Fast downward velocity
    private static final double VELOCITY_VARIANCE = 50.0; // Variance for natural look

    /**
     * Creates a new {@link RainParticle} positioned randomly across the given
     * canvas width and spread vertically across and beyond the canvas height.
     *
     * @param width  the width of the drawing area in pixels
     * @param height the height of the drawing area in pixels
     * @return a newly created {@code RainParticle}
     */
    @Override
    public Particle createParticle(double width, double height) {
        // Random X position across the width
        double x = Math.random() * width;
        
        // Spread particles across the entire fall distance for continuous rain
        // Start from well above the screen to below it
        double y = -height - (Math.random() * height * 2);
        
        // High Y velocity with some variance
        double velocityY = INITIAL_VELOCITY_Y + (Math.random() * VELOCITY_VARIANCE);
        
        return new RainParticle(x, y, velocityY);
    }
}
