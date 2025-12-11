package com.eleven.pet.vfx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class ParticleSystem {
    private final List<Particle> particles;
    private final Canvas canvas;
    private final GraphicsContext graphicsContext;
    private AnimationTimer animationLoop;
    private ParticleFactory particleFactory;
    private int targetParticleCount;
    private long lastUpdate;

    public ParticleSystem(double width, double height) {
        this.particles = new ArrayList<>();
        this.canvas = new Canvas(width, height);
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.lastUpdate = System.nanoTime();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setParticleFactory(ParticleFactory factory) {
        this.particleFactory = factory;
    }

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

    public void stopAnimation() {
        if (animationLoop != null) {
            animationLoop.stop();
            animationLoop = null;
        }
        particles.clear();
    }

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

    private void render() {
        // Clear canvas
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Render all particles
        for (Particle particle : particles) {
            particle.render(graphicsContext);
        }
    }
    
    public int getParticleCount() {
        return particles.size();
    }
}
