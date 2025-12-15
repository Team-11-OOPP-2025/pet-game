package com.eleven.pet.minigames;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;

/**
 * Registry for available minigames.
 * <p>
 * Allows for retrieving a random game without coupling the consumer to specific
 * game implementations.
 * </p>
 */
public class MinigameRegistry {
    private static MinigameRegistry instance;
    private final List<Minigame> games = new ArrayList<>();
    private final Random random = new Random();

    private MinigameRegistry() {
        ServiceLoader<Minigame> loader = ServiceLoader.load(Minigame.class);
        for (Minigame game : loader) {
            registerGame(game);
            System.out.println("Registered minigame: " + game.getName());
        }
    }

    public static MinigameRegistry getInstance() {
        if (instance == null) {
            instance = new MinigameRegistry();
        }
        return instance;
    }

    public void registerGame(Minigame game) {
        games.add(game);
    }

    public Minigame getRandomGame() {
        if (games.isEmpty()) return null;
        return games.get(random.nextInt(games.size()));
    }
}