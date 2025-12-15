package com.eleven.pet.core;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Main game engine responsible for updating the game state, including
 * the game clock and weather system.
 *
 * <p>This class owns the main animation loop and schedules periodic
 * weather changes.
 */
public class GameEngine {
    private final PetModel model;
    private final GameClock clock;
    private final WeatherSystem weatherSystem;

    private AnimationTimer gameLoop;
    private Timeline weatherTimer;
    private long lastFrameTime = 0;
    private boolean isRunning = false;

    /**
     * Constructs a {@code GameEngine} with the specified sprites model,
     * game clock, and weather system.
     *
     * @param model         the sprites model to manage
     * @param clock         the game clock for time management
     * @param weatherSystem the weather system for environmental effects
     */
    public GameEngine(PetModel model, GameClock clock, WeatherSystem weatherSystem) {
        this.model = model;
        this.clock = clock;
        this.weatherSystem = weatherSystem;
    }

    /**
     * Starts the game engine, including the main game loop and weather updates.
     *
     * <p>If the engine is already running, this method is a no-op.
     */
    public void start() {
        if (isRunning) return;

        // Start the visual/logic loop (60 FPS)
        lastFrameTime = System.nanoTime();
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(now);
            }
        };
        gameLoop.start();

        startWeatherTimer();

        isRunning = true;
    }

    /**
     * Stops the game engine, including the main game loop and weather updates.
     *
     * <p>After calling this method, {@link #start()} can be invoked again
     * to resume execution.
     */
    public void stop() {
        if (gameLoop != null) gameLoop.stop();
        if (weatherTimer != null) weatherTimer.stop();
        isRunning = false;
    }

    /**
     * Updates the game state based on the elapsed time since the last frame.
     *
     * <p>The delta time is computed from {@code now} and used to advance the
     * {@link GameClock}. When a new in‑game day starts, the pet's daily food
     * is replenished.
     *
     * @param now the current time in nanoseconds, provided by {@link AnimationTimer}
     */
    private void update(long now) {
        // Calculate delta time (in seconds)
        double deltaSeconds = (now - lastFrameTime) / 1_000_000_000.0;
        lastFrameTime = now;

        boolean newDayStarted = clock.tick(deltaSeconds);
        if (newDayStarted) {
            model.replenishDailyFood();
        }
    }

    /**
     * Initializes and starts the weather change timer.
     *
     * <p>Applies an initial weather change immediately, then schedules
     * recurring changes based on {@link GameConfig#WEATHER_CHANGE_INTERVAL}.
     */
    private void startWeatherTimer() {
        // Set initial weather immediately
        weatherSystem.changeWeather();
        
        weatherTimer = new Timeline(new KeyFrame(
                Duration.seconds(GameConfig.WEATHER_CHANGE_INTERVAL),
                _ -> weatherSystem.changeWeather()
        ));
        weatherTimer.setCycleCount(Timeline.INDEFINITE);
        weatherTimer.play();
    }
}