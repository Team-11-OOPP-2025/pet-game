package com.eleven.pet.minigames;

public interface Minigame {
    /**
     * The name used in menus or debug logs
     */
    String getName();

    /**
     * Creates a fresh instance of the game. Call this every time you want to play.
     */
    GameSession createSession();
}