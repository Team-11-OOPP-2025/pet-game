package com.eleven.pet.vfx;

import com.eleven.pet.core.AssetLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class TreatParticle extends Particle {
    private static final Image TEXTURE = AssetLoader.getInstance().getImage("vfx/treat");
    private final double rotationSpeed;
    private double rotation = 0;
    private double vy; // Vertical velocity
    private double vx; // Horizontal velocity

    public TreatParticle(double x, double y) {
        super(x, y);
        this.size = 100; // Nice visible size for the treat
        this.vx = (Math.random() - 0.5) * 2; // Slight horizontal drift
        this.vy = 3 + Math.random() * 3; // Fall down
        this.rotationSpeed = (Math.random() - 0.5) * 5; // Spin a little
        this.alive = true;
    }

    @Override
    public void update(double deltaTime) {
        // Simple gravity physics
        x += vx;
        y += vy;
        rotation += rotationSpeed;

        // Accelerate downwards (gravity)
        vy += 0.1;

        // Kill if off screen (assuming standard height ~600-800)
        if (y > 1000) {
            alive = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.save();
        // Translate to center of particle for rotation
        gc.translate(x, y);
        gc.rotate(rotation);
        // Draw centered
        gc.drawImage(TEXTURE, -size / 2, -size / 2, size, size);
        gc.restore();
    }
}