package com.eleven.pet.minigames;

import javafx.scene.layout.Pane;

import java.util.function.Consumer;

public interface GameSession {
    /**
     * Returns the UI for this specific session
     */
    Pane getView();

    /**
     * Starts the game logic
     */
    void start(Consumer<MinigameResult> onFinish);
}