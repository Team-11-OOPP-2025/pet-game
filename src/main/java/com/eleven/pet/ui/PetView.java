package com.eleven.pet.ui;

import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.ui.PetAvatarView;
import com.eleven.pet.core.AssetLoader;
import com.eleven.pet.daily_reward.DailyRewardView;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.ui.InventoryView;
import javafx.animation.*;
import javafx.geometry.Insets;
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
    private final AssetLoader assetLoader;

    private StackPane worldLayer; 
    private StackPane uiLayer;   

    private PetAvatarView petAvatarView;
    private InventoryView inventoryView;
    private WorldView worldView;
    private HUDView hudView;
    private DailyRewardView dailyRewardView;
    

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
        this.assetLoader = AssetLoader.getInstance();
    }

    public Pane initializeUI() {
        StackPane root = new StackPane();

        // 1. Create the Layers
        worldLayer = new StackPane(); 
        uiLayer = new StackPane(); 
        uiLayer.setPickOnBounds(false); 

        // 2. Initialize Components
        worldView = new WorldView(clock, weatherSystem);
        petAvatarView = new PetAvatarView(model, controller);
        petAvatarView.setPickOnBounds(false);

        inventoryView = new InventoryView(model, controller);
        hudView = new HUDView(model, controller, clock);
        
        // Pass controller here
        dailyRewardView = new DailyRewardView(model, controller);

        // 3. Compose World
        worldView.getTvClickArea().setOnMouseClicked(_ -> enterMinigameMode());
        worldLayer.getChildren().addAll(worldView, petAvatarView);

        // 4. Compose UI
        uiLayer.getChildren().addAll(hudView, inventoryView, dailyRewardView);
        
        // 5. Setup Daily Rewards Trigger
        setupRewardTrigger(uiLayer);

        // 6. Add to Root
        root.getChildren().addAll(worldLayer, uiLayer);

        return root;
    }

    private void setupRewardTrigger(StackPane root) {
        Button btn = new Button("🎁 REWARDS");
        btn.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 10 20; -fx-border-color: black; -fx-border-width: 2px; -fx-border-radius: 20;");
        btn.setOnAction(e -> dailyRewardView.toggle(true));
        
        StackPane.setAlignment(btn, Pos.TOP_RIGHT);
        StackPane.setMargin(btn, new Insets(20, 20, 0, 0));
        root.getChildren().add(btn);
    }

    // =============================================================
    // EXISTING ZOOM LOGIC
    // =============================================================

    private void enterMinigameMode() {
        if (isGameMode) return;
        isGameMode = true;

        StackPane tvClickArea = worldView.getTvClickArea();

        double sceneW = worldLayer.getWidth();
        double sceneH = worldLayer.getHeight();

        double tvCenterX = tvClickArea.getLayoutX() + (tvClickArea.getWidth() / 2);
        double tvCenterY = tvClickArea.getLayoutY() + (tvClickArea.getHeight() / 2);

        double transX = ((sceneW / 2) - tvCenterX + VIEW_OFFSET_X) * ZOOM_FACTOR;
        double transY = ((sceneH / 2) - tvCenterY) * ZOOM_FACTOR;

        ParallelTransition pt = new ParallelTransition();

        Timeline zoom = new Timeline(
                new KeyFrame(Duration.millis(TRANSITION_DURATION_MS),
                        new KeyValue(worldLayer.scaleXProperty(), ZOOM_FACTOR, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.scaleYProperty(), ZOOM_FACTOR, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.translateXProperty(), transX, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.translateYProperty(), transY, Interpolator.EASE_BOTH)
                )
        );

        TranslateTransition movePet = new TranslateTransition(Duration.millis(TRANSITION_DURATION_MS), petAvatarView);
        movePet.setToX(300);
        movePet.setToY(-75);

        ScaleTransition scalePet = new ScaleTransition(Duration.millis(TRANSITION_DURATION_MS), petAvatarView);
        scalePet.setToX(0.5);
        scalePet.setToY(0.5);

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
        tvClickArea.getChildren().clear(); 

        ParallelTransition pt = new ParallelTransition();

        Timeline zoomOut = new Timeline(
                new KeyFrame(Duration.millis(TRANSITION_DURATION_MS),
                        new KeyValue(worldLayer.scaleXProperty(), 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.scaleYProperty(), 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.translateXProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.translateYProperty(), 0, Interpolator.EASE_BOTH)
                )
        );

        TranslateTransition movePet = new TranslateTransition(Duration.millis(TRANSITION_DURATION_MS), petAvatarView);
        movePet.setToX(0);
        movePet.setToY(0);

        ScaleTransition scalePet = new ScaleTransition(Duration.millis(TRANSITION_DURATION_MS), petAvatarView);
        scalePet.setToX(1);
        scalePet.setToY(1);

        FadeTransition fadeUI = new FadeTransition(Duration.millis(500), uiLayer);
        fadeUI.setToValue(1.0);
        fadeUI.setDelay(Duration.millis(300));

        pt.getChildren().addAll(zoomOut, movePet, scalePet, fadeUI);
        pt.play();
    }

    private void loadGameContent() {
        StackPane tvClickArea = worldView.getTvClickArea();
        tvClickArea.getChildren().clear();

        Pane gamePane = controller.getMinigamePane();

        if (gamePane != null) {
            gamePane.prefWidthProperty().bind(tvClickArea.widthProperty());
            gamePane.prefHeightProperty().bind(tvClickArea.heightProperty());

            Button exitBtn = new Button("X");
            exitBtn.setStyle("-fx-background-color: rgba(255,0,0,0.5); -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
            exitBtn.setOnAction(_ -> exitMinigameMode());
            StackPane.setAlignment(exitBtn, Pos.TOP_RIGHT);

            tvClickArea.getChildren().addAll(gamePane, exitBtn);
        }
    }
}