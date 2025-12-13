package com.eleven.pet.ui;

import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.ui.PetAvatarView;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.ui.InventoryView;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class PetView {
    private final PetModel model;
    private final PetController controller;
    private final GameClock clock;
    private final WeatherSystem weatherSystem;

    private StackPane worldLayer; // Zoomable layer (BG + Pet + TV)
    private StackPane uiLayer;    // Static layer (HUD + Controls)

    private PetAvatarView petAvatarView;
    private InventoryView inventoryView;
    private WorldView worldView;
    private HUDView hudView;

    // Zoom State
    private boolean isGameMode = false;
    private static final double ZOOM_FACTOR = 3.0;
    private static final double VIEW_OFFSET_X = 100.0;
    private static final double TRANSITION_DURATION_MS = 800.0;

    public PetView(PetModel model, PetController controller, GameClock clock, WeatherSystem weatherSystem) {
        this.model = model;
        this.controller = controller;
        this.clock = clock;
        this.weatherSystem = weatherSystem;
    }

    public Pane initializeUI() {
        StackPane root = new StackPane();

        // 1. Create the Layers
        worldLayer = new StackPane(); // This will scale up
        uiLayer = new StackPane(); // This stays static
        uiLayer.setPickOnBounds(false); // Allow clicking through empty UI space

        // 2. Initialize Components
        worldView = new WorldView(clock, weatherSystem);
        petAvatarView = new PetAvatarView(model, controller);
        // Ensure the pet container allows clicks to pass through transparent areas (to hit the TV)
        petAvatarView.setPickOnBounds(false);

        inventoryView = new InventoryView(model, controller);
        hudView = new HUDView(model, controller, clock);

        // 3. Compose World
        // WorldView manages the background elements.
        // We add the click handler to the TV area inside WorldView for consistency
        worldView.getTvClickArea().setOnMouseClicked(_ -> enterMinigameMode());

        worldLayer.getChildren().addAll(worldView, petAvatarView);

        // 4. Compose UI
        uiLayer.getChildren().addAll(hudView, inventoryView);

        // 5. Add to Root
        root.getChildren().addAll(worldLayer, uiLayer);

        return root;
    }

    private void enterMinigameMode() {
        if (isGameMode) return;
        isGameMode = true;

        StackPane tvClickArea = worldView.getTvClickArea();

        // Calculate Center Points
        double sceneW = worldLayer.getWidth();
        double sceneH = worldLayer.getHeight();

        // The center of the TV (Target)
        // Since tvClickArea is inside worldView, and worldView is inside worldLayer at (0,0),
        // and they share same dimensions mostly, we can use layout positions relative to parent.
        double tvCenterX = tvClickArea.getLayoutX() + (tvClickArea.getWidth() / 2);
        double tvCenterY = tvClickArea.getLayoutY() + (tvClickArea.getHeight() / 2);

        // Formula: (ScreenCenter - ObjectCenter + Offset) * ZoomFactor
        // We add Offset to shift the camera focus point
        double transX = ((sceneW / 2) - tvCenterX + VIEW_OFFSET_X) * ZOOM_FACTOR;
        double transY = ((sceneH / 2) - tvCenterY) * ZOOM_FACTOR;

        ParallelTransition pt = new ParallelTransition();

        // Zoom and Pan the World
        Timeline zoom = new Timeline(
                new KeyFrame(Duration.millis(TRANSITION_DURATION_MS),
                        new KeyValue(worldLayer.scaleXProperty(), ZOOM_FACTOR, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.scaleYProperty(), ZOOM_FACTOR, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.translateXProperty(), transX, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.translateYProperty(), transY, Interpolator.EASE_BOTH)
                )
        );

        // Move Pet to the side
        TranslateTransition movePet = new TranslateTransition(Duration.millis(TRANSITION_DURATION_MS), petAvatarView);

        // MATH DERIVATION:
        // We calculate the delta required to move from Current Layout Position to Target (Right of TV)
        // Target X (600) - Start X (approx 250) = 350
        // Target Y (-20) - Start Y (approx 180) = -200
        // Using "movePet.setByX" would also work, but setToX works on translation property directly.
        // TODO: Consider making these target positions dynamic based on TV position.
        // maybe if there's time later.
        movePet.setToX(300);
        movePet.setToY(-75);

        // Scale Pet Down (Perspective effect - 0.5 is 50% of the ORIGINAL size, making it look deeper in scene)
        ScaleTransition scalePet = new ScaleTransition(Duration.millis(TRANSITION_DURATION_MS), petAvatarView);
        scalePet.setToX(0.5);
        scalePet.setToY(0.5);

        // Fade out UI
        FadeTransition fadeUI = new FadeTransition(Duration.millis(300), uiLayer);
        fadeUI.setToValue(0);

        pt.getChildren().addAll(zoom, movePet, scalePet, fadeUI);
        pt.setOnFinished(_ -> loadGameContent());
        pt.play();
    }

    private void exitMinigameMode() {
        if (!isGameMode) return;
        isGameMode = false;

        StackPane tvClickArea = worldView.getTvClickArea();
        tvClickArea.getChildren().clear(); // Remove game pane

        ParallelTransition pt = new ParallelTransition();

        // Reset World Scale and Position
        Timeline zoomOut = new Timeline(
                new KeyFrame(Duration.millis(TRANSITION_DURATION_MS),
                        new KeyValue(worldLayer.scaleXProperty(), 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.scaleYProperty(), 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.translateXProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.translateYProperty(), 0, Interpolator.EASE_BOTH)
                )
        );

        // Reset Pet Position
        TranslateTransition movePet = new TranslateTransition(Duration.millis(TRANSITION_DURATION_MS), petAvatarView);
        movePet.setToX(0);
        movePet.setToY(0);

        // Reset Pet Scale (Back to original)
        ScaleTransition scalePet = new ScaleTransition(Duration.millis(TRANSITION_DURATION_MS), petAvatarView);
        scalePet.setToX(1);
        scalePet.setToY(1);

        // Fade UI In
        FadeTransition fadeUI = new FadeTransition(Duration.millis(500), uiLayer);
        fadeUI.setToValue(1.0);
        fadeUI.setDelay(Duration.millis(300));

        pt.getChildren().addAll(zoomOut, movePet, scalePet, fadeUI);
        pt.play();
    }

    private void loadGameContent() {
        StackPane tvClickArea = worldView.getTvClickArea();
        tvClickArea.getChildren().clear();

        // Get the pane from your controller
        Pane gamePane = controller.getMinigamePane();

        if (gamePane != null) {
            // Bind the game pane to fill the TV box area
            gamePane.prefWidthProperty().bind(tvClickArea.widthProperty());
            gamePane.prefHeightProperty().bind(tvClickArea.heightProperty());

            // TODO: Style the exit button properly
            Button exitBtn = new Button("X");
            exitBtn.setStyle("-fx-background-color: rgba(255,0,0,0.5); -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
            exitBtn.setOnAction(_ -> exitMinigameMode());
            StackPane.setAlignment(exitBtn, Pos.TOP_RIGHT);

            tvClickArea.getChildren().addAll(gamePane, exitBtn);
        }
    }
}