package com.eleven.pet.vfx.effects;

import com.eleven.pet.vfx.ParticleSystem;
import com.eleven.pet.vfx.RainParticleFactory;

public class RainParticleEffect implements ParticleEffect {
    private final int intensity;
    private static final RainParticleFactory RAIN_FACTORY = new RainParticleFactory();

    public RainParticleEffect(int intensity) {
        this.intensity = intensity;
    }

    @Override
    public void start(ParticleSystem system) {
        if (system != null) {
            system.setParticleFactory(RAIN_FACTORY);
            system.startAnimation(intensity);
        }
    }

    @Override
    public void stop(ParticleSystem system) {
        if (system != null) {
            system.stopAnimation();
        }
    }
    
    public int getIntensity() {
        return intensity;
    }
}
