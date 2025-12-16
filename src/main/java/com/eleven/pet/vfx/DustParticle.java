package com.eleven.pet.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DustParticle extends Particle {
    private static final double DRIFT_SPEED = 15.0; // Slow drift
    private static final double FLOAT_AMPLITUDE = 10.0; // How much it floats up and down
    private final double baseY;
    private final double floatSpeed;
    private double lifetime;
    private final double maxLifetime;
    private final double phase; // For wave motion

    public DustParticle(double x, double y) {
        super(x, y);
        this.baseY = y;
        this.size = 4.0 + Math.random() * 5.0; // Size between 4-9 pixels
        
        // Brownish/grayish dust color with higher opacity
        int grayValue = 80 + (int)(Math.random() * 60);
        this.color = Color.rgb(grayValue, grayValue - 15, grayValue - 25, 0.6 + Math.random() * 0.3);
        
        this.velocity_x = -DRIFT_SPEED + Math.random() * (DRIFT_SPEED * 2); // Random drift
        this.floatSpeed = 1.0 + Math.random() * 2.0; // Random float speed
        this.phase = Math.random() * Math.PI * 2; // Random starting phase
        
        this.lifetime = 0;
        this.maxLifetime = 3.0 + Math.random() * 4.0; // Lives 3-7 seconds
        this.alive = true;
    }

    @Override
    public void update(double deltaTime) {
        lifetime += deltaTime;
        
        // Check if particle should die
        if (lifetime >= maxLifetime) {
            alive = false;
            return;
        }
        
        // Gentle floating motion using sine wave
        double floatOffset = Math.sin((lifetime * floatSpeed) + phase) * FLOAT_AMPLITUDE;
        y = baseY + floatOffset;
        
        // Slow horizontal drift
        x += velocity_x * deltaTime;
        
        // Fade out near the end of lifetime
        double fadeThreshold = maxLifetime * 0.7;
        if (lifetime > fadeThreshold) {
            double fadeProgress = (lifetime - fadeThreshold) / (maxLifetime - fadeThreshold);
            double originalOpacity = 0.4 + Math.random() * 0.3;
            double newOpacity = originalOpacity * (1.0 - fadeProgress);
            
            int grayValue = 100 + (int)(Math.random() * 50);
            color = Color.rgb(grayValue, grayValue - 10, grayValue - 20, newOpacity);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x - size / 2, y - size / 2, size, size);
    }
}
