package com.eleven.pet.vfx;

public class RainParticleFactory implements ParticleFactory {
    private static final double INITIAL_VELOCITY_Y = 200.0; // Fast downward velocity
    private static final double VELOCITY_VARIANCE = 50.0; // Variance for natural look

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
