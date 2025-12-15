// src/main/java/com/eleven/pet/minigames/ui/MiniGameView.java
package com.eleven.pet.minigames.ui;

import com.eleven.pet.minigames.Minigame;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;

/**
 * The "TV Screen" container for minigames.
 * <p>
 * This view acts as a host for any {@link Minigame}. It provides the
 * shared background styling (the TV screen look) and renders the
 * game-specific content by calling {@link Minigame#getView()}.
 * </p>
 */
public class MiniGameView extends StackPane {

    /**
     * Creates a new view that displays the given game.
     *
     * @param game the initialized minigame to display
     */
    public MiniGameView(Minigame game) {
        // TV Screen styling
        setStyle("-fx-background-color: #222; -fx-background-radius: 4;");
        setPadding(new Insets(10));

        if (game != null) {
            // MVC: The View asks the Controller (Game) for its specific UI
            getChildren().add(game.getView());
        }
    }
}