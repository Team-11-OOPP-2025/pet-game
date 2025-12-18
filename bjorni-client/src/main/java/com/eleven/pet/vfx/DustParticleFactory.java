package com.eleven.pet.vfx;

public class DustParticleFactory implements ParticleFactory {
    @Override
    public Particle createParticle(double width, double height) {
        // Create dust particles only on the pet's body area (center 80% width, middle 85% height)
        // This prevents dust from appearing in empty sprite areas
        double centerX = width * 0.5;
        double centerY = width * 0.5;
        
        double usableWidth = width * 0.8;
        double usableHeight = height * 0.85;
        
        double x = centerX + (Math.random() - 0.5) * usableWidth;
        double y = centerY + (Math.random() - 0.5) * usableHeight;
        
        return new DustParticle(x, y);
    }
}
