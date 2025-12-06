package com.eleven.pet.vfx;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

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
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setParticleFactory(ParticleFactory factory) {
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
