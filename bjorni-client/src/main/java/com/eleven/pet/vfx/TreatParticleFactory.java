package com.eleven.pet.vfx;

import java.util.Random;

public class TreatParticleFactory implements ParticleFactory {

    private static final Random RANDOM = new Random();

    public TreatParticle createParticle(double width, double height) {
        // Create treat particles randomly across the visible width
        double x = RANDOM.nextDouble() * width;
        // Start just above the visible screen area so they fall into view
        double y = -50;

        return new TreatParticle(x, y);
    }
}