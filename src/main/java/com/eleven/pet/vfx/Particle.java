package com.eleven.pet.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Base class for all particles in the visual effects system. Concrete
 * subclasses implement their own update and render behavior.
 */
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
     * @return {@code true} if particle is alive; {@code false} otherwise
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Returns the current x position of the particle.
     *
     * @return x coordinate in pixels
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the current y position of the particle.
     *
     * @return y coordinate in pixels
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the current horizontal velocity.
     *
     * @return horizontal velocity in pixels per second
     */
    public double getVelocityX() {
        return velocity_x;
    }

    /**
     * Returns the current vertical velocity.
     *
     * @return vertical velocity in pixels per second
     */
    public double getVelocityY() {
        return velocity_y;
    }
}
