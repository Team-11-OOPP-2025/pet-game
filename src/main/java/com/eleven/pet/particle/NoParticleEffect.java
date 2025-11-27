package com.eleven.pet.particle;

public class NoParticleEffect implements ParticleEffect {
    
    @Override
    public void start(ParticleSystem system) {
        // No particles to start
        if (system != null) {
            system.stopAnimation();
        }
    }
    
    @Override
    public void stop(ParticleSystem system) {
        // Nothing to stop
        if (system != null) {
            system.stopAnimation();
        }
    }
}
