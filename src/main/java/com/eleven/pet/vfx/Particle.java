package com.eleven.pet.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Particle {
    protected double x, y, velocity_x, velocity_y, size;
    protected Color color;
    protected boolean alive;

    /**
     * Constructor to initialize particle position.
     *
     * @param x initial x position
     * @param y initial y position
     */
    public Particle(double x, double y) {
        this.x = x;
        this.y = y;
        this.alive = true;
    }

    /**
     * Update particle position and state based on delta time.
     *
     * @param deltaTime time elapsed since last update in seconds
     */
    public abstract void update(double deltaTime);

    /**
     * Render the particle to the graphics context.
     *
     * @param gc graphics context to render to
     */
    public abstract void render(GraphicsContext gc);

    /**
     * Check if particle is still alive and should be rendered/updated.
     *
     * @return true if particle is alive
     */
    public boolean isAlive() {
        return alive;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVelocityX() {
        return velocity_x;
    }

    public double getVelocityY() {
        return velocity_y;
    }
}
