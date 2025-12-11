package com.eleven.pet.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class LeavesParticle extends Particle {
    // TODO: Implement leaves particle with swaying motion
    
    public LeavesParticle(double x, double y) {
        super(x, y);
        // Stub implementation - to be completed later
        this.velocity_x = 0;
        this.velocity_y = 50;
        this.size = 5;
        this.color = Color.BROWN;
    }
    
    @Override
    public void update(double deltaTime) {
        // Stub implementation - to be completed later
        y += velocity_y * deltaTime;
    }
    
    @Override
    public void render(GraphicsContext gc) {
        // Stub implementation - to be completed later
        gc.setFill(color);
        gc.fillOval(x, y, size, size);
    }
}
