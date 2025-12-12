package com.eleven.pet.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RainParticle extends Particle {
    private static final double GRAVITY = 200.0; // Pixels per second squared
    private static final double MAX_VELOCITY = 400.0; // Terminal velocity
    private final double length;

    public RainParticle(double x, double y, double velocityY) {
        super(x, y);
        this.velocity_x = 0;
        this.velocity_y = velocityY;
        this.size = 2.0;
        this.length = 8.0 + Math.random() * 4.0; // Length between 8-12 pixels
        this.color = Color.rgb(150, 180, 220, 0.6); // Light blue with transparency
    }

    @Override
    public void update(double deltaTime) {
        // Apply gravity for acceleration
        velocity_y += GRAVITY * deltaTime;
        
        // Cap at terminal velocity
        if (velocity_y > MAX_VELOCITY) {
            velocity_y = MAX_VELOCITY;
        }
        
        // Update position
        x += velocity_x * deltaTime;
        y += velocity_y * deltaTime;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setStroke(color);
        gc.setLineWidth(size);
        
        // Draw rain as a line falling downward
        gc.strokeLine(x, y, x, y + length);
    }
}
