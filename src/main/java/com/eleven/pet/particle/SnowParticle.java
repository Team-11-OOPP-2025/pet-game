package com.eleven.pet.particle;

import javafx.scene.canvas.GraphicsContext;

public class SnowParticle extends Particle {
    @SuppressWarnings("unused")
    private double rotation;
    
    public SnowParticle(double x, double y) {
        super(x, y);
    }
    
    @Override
    public void render(GraphicsContext gc) {
    }
}
