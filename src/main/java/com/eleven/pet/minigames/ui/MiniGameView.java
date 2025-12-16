package com.eleven.pet.minigames.ui;

import com.eleven.pet.minigames.Minigame;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * The "TV Screen" container for minigames.
 * <p>
 * This view acts as a host for any {@link Minigame}. It provides the
 * shared background styling (the TV screen look) and renders the
 * game-specific UI inside it.
 * </p>
 */
public class MiniGameView extends StackPane {

    /**
     * Creates a new view that displays the given game.
     *
     * @param gamePane the initialized minigame to display
     */
    public MiniGameView(Pane gamePane) {
        // TV Screen styling
        setStyle("-fx-background-color: #222; -fx-background-radius: 4;");
        setPadding(new Insets(10));

        if (gamePane != null) {
            // MVC: The View asks the Controller (Game) for its specific UI
            getChildren().add(gamePane);
        }
    }
}