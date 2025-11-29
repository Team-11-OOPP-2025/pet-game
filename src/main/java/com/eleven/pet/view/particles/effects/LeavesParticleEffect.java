package com.eleven.pet.view.particles.effects;

import com.eleven.pet.view.particles.ParticleSystem;

public class LeavesParticleEffect implements ParticleEffect {
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
