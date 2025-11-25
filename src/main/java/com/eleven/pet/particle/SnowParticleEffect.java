package com.eleven.pet.particle;

public class SnowParticleEffect implements ParticleEffect {
    private final int intensity;
    
    public SnowParticleEffect(int intensity) {
        this.intensity = intensity;
    }
    
    @Override
    public void start(ParticleSystem system) {
    }
    
    @Override
    public void stop(ParticleSystem system) {
    }
}
