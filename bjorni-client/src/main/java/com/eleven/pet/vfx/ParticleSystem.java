package com.eleven.pet.vfx;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages and renders a collection of {@link Particle} instances on a
 * {@link Canvas} using a JavaFX {@link AnimationTimer}.
 */
public class ParticleSystem {
    private final List<Particle> particles;
    private final Canvas canvas;
    private final GraphicsContext graphicsContext;
    private AnimationTimer animationLoop;
    private ParticleFactory particleFactory;
    private int targetParticleCount;
    private long lastUpdate;

    /**
     * Creates a particle system that renders to an internal {@link Canvas}
     * with the specified size.
     *
     * @param width  width of the canvas in pixels
     * @param height height of the canvas in pixels
     */
    public ParticleSystem(double width, double height) {
        this.particles = new ArrayList<>();
        this.canvas = new Canvas(width, height);
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.lastUpdate = System.nanoTime();
    }

    /**
     * Starts the animation loop and keeps spawning particles up to the
     * specified target count.
     *
     * @param particleCount desired number of particles to maintain
     */
    public void startAnimation(int particleCount) {
        this.targetParticleCount = particleCount;

        if (animationLoop != null) {
            animationLoop.stop();
        }

        animationLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0; // Convert to seconds
                lastUpdate = now;

                update(deltaTime);
                render();
            }
        };

        animationLoop.start();
    }

    /**
     * Stops the animation loop, removes all particles, and clears the canvas.
     */
    public void stopAnimation() {
        if (animationLoop != null) {
            animationLoop.stop();
            animationLoop = null;
        }
        particles.clear();

        // Clear canvas to remove remaining particles
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Updates all particles and spawns new ones while the current number
     * of particles is below the target count.
     *
     * @param deltaTime time elapsed since the last update in seconds
     */
    private void update(double deltaTime) {
        // Create new particles if below target count
        while (particles.size() < targetParticleCount && particleFactory != null) {
            Particle newParticle = particleFactory.createParticle(canvas.getWidth(), canvas.getHeight());
            if (newParticle != null) {
                particles.add(newParticle);
            }
        }

        // Update existing particles
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.update(deltaTime);

            // Remove particles that are off-screen or dead
            if (!particle.isAlive() ||
                    particle.getY() > canvas.getHeight() + 50 ||
                    particle.getX() < -50 ||
                    particle.getX() > canvas.getWidth() + 50) {
                iterator.remove();
            }
        }
    }

    /**
     * Clears the canvas and renders all active particles.
     */
    private void render() {
        // Clear canvas
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Render all particles
        for (Particle particle : particles) {
            particle.render(graphicsContext);
        }
    }

    /**
     * Returns the current number of active particles.
     *
     * @return number of particles managed by this system
     */
    public int getParticleCount() {
        return particles.size();
    }

    /**
     * Returns the {@link Canvas} on which the particles are rendered.
     *
     * @return the canvas used by this particle system
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Sets the {@link ParticleFactory} used to create new particles when
     * the system needs to spawn additional particles.
     *
     * @param factory factory responsible for creating new particles
     */
    public void setParticleFactory(ParticleFactory factory) {
        this.particleFactory = factory;
    }
}
