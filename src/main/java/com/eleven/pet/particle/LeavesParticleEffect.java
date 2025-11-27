package com.eleven.pet.particle;

public class LeavesParticleEffect implements ParticleEffect {
    @SuppressWarnings("unused")
    private final int intensity;
    
    public LeavesParticleEffect(int intensity) {
        this.intensity = intensity;
    }
    
    @Override
    public void start(ParticleSystem system) {
    }
    
    @Override
    public void stop(ParticleSystem system) {
    }
}
