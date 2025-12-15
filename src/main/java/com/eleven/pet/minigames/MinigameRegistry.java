// src/main/java/com/eleven/pet/minigames/MinigameRegistry.java
package com.eleven.pet.minigames;

import com.eleven.pet.minigames.impl.GuessingGame;
import com.eleven.pet.minigames.impl.TimingGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Registry for available minigames.
 * <p>
 * Allows for retrieving a random game without coupling the consumer to specific
 * game implementations.
 * </p>
 */
public class MinigameRegistry {
    private static MinigameRegistry instance;
    private final List<Class<? extends Minigame>> games = new ArrayList<>();
    private final Random random = new Random();

    private MinigameRegistry() {
        // Register default games
        registerGame(GuessingGame.class);
        registerGame(TimingGame.class);
    }

    public static MinigameRegistry getInstance() {
        if (instance == null) {
            instance = new MinigameRegistry();
        }
        return instance;
    }

    public void registerGame(Class<? extends Minigame> gameClass) {
        games.add(gameClass);
    }

    public Minigame getRandomGame() {
        if (games.isEmpty()) return null;
        try {
            Class<? extends Minigame> gameClass = games.get(random.nextInt(games.size()));
            return gameClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.err.println("Failed to instantiate minigame: " + e.getMessage());
            return null;
        }
    }
}