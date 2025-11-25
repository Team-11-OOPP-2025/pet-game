package com.eleven.pet.particle;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Particle {
    protected double x;
    protected double y;
    protected double velocityX;
    protected double velocityY;
    protected double size;
    protected Color color;
    
    public Particle(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void update(double deltaTime) {
    }
    
    public void render(GraphicsContext gc) {
    }
    
    public boolean isOffScreen(double height) {
        return false;
    }
}
