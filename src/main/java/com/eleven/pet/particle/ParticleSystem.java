package com.eleven.pet.particle;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

@SuppressWarnings({"unused", "all"})
public class ParticleSystem {
    private final List<Object> particles; // TODO: Change to List<Particle> when Particle class is created
    private final Canvas canvas;
    private final GraphicsContext graphicsContext;
    private AnimationTimer animationLoop;
    private Object particleFactory; // TODO: Change to ParticleFactory when interface is created
    private int targetParticleCount;
    private long lastUpdate;
    
    public ParticleSystem(double width, double height) {
        this.particles = new ArrayList<>();
        this.canvas = new Canvas(width, height);
        this.graphicsContext = canvas.getGraphicsContext2D();
    }
    
    public Canvas getCanvas() {
        return canvas;
    }
    
    public void setParticleFactory(Object factory) { // TODO: Change to ParticleFactory when interface is created
        this.particleFactory = factory;
    }
    
    public void startAnimation(int particleCount) {
    }
    
    public void stopAnimation() {
    }
    
    private void update(double deltaTime) {
    }
    
    private void render() {
    }
}
