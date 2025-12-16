package com.eleven.pet.ui;

import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.ui.PetAvatarView;
import com.eleven.pet.core.AssetLoader;
import com.eleven.pet.daily_reward.ui.DailyRewardView;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.ui.InventoryView;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;

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

    private Button rewardBtn;


    // Zoom State
    private boolean isGameMode = false;
    private static final double ZOOM_FACTOR = 3.0;
    private static final double VIEW_OFFSET_X = 100.0;
    private static final double TRANSITION_DURATION_MS = 800.0;

    /**
     * Creates the main pet view.
     *
     * @param model         the pet model containing stats and state
     * @param controller    controller handling user actions and game logic
     * @param clock         game clock driving time-based UI updates
     * @param weatherSystem weather system used for world background effects
     */
    public PetView(PetModel model, PetController controller, GameClock clock, WeatherSystem weatherSystem) {
        this.model = model;
        this.controller = controller;
        this.clock = clock;
        this.weatherSystem = weatherSystem;
        this.assetLoader = AssetLoader.getInstance();
    }

    /**
     * Builds and wires the full UI hierarchy for the pet screen.
     * <p>
     * This includes the world layer (background + pet avatar) and the
     * UI layer (HUD, inventory, daily rewards, etc.).
     *
     * @return the root {@link Pane} to be attached to the scene
     */
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

        if (!model.isTutorialCompleted()) {
            controller.initTutorialLogic();
            final TutorialView[] tutorialRef = new TutorialView[1];

            // Define targets matching the steps in TutorialView
            // 0: Welcome (Null)
            // 1: Stats (StatsBox)
            // 2: TV (TvClickArea)
            // 3: Daily Rewards
            // 4: Inventory (Feed Button)
            // 5: Clean (Clean Button)
            // 6: Sleep (Sleep Button)
            List<Node> targets = Arrays.asList(
                    null,
                    hudView.getStatsBox(),
                    worldView.getTvClickArea(),
                    rewardBtn,
                    hudView.getFeedBtn(),
                    hudView.getCleanBtn(),
                    hudView.getSleepBtn()
            );

            tutorialRef[0] = new TutorialView(targets, () -> {
                root.getChildren().remove(tutorialRef[0]);
                controller.completeTutorial();
            });

            root.getChildren().add(tutorialRef[0]);
        }
        return root;
    }

    private void setupRewardTrigger(StackPane root) {
        // Load the chest image
        Image chestImage = assetLoader.getImage("chest/Chest");
        ImageView chestIcon = new ImageView(chestImage);

        // Set viewport to the first frame (150x118) to avoid showing the whole sprite sheet
        chestIcon.setViewport(new Rectangle2D(0, 0, 150, 118));
        chestIcon.setFitWidth(30);
        chestIcon.setFitHeight(24);
        chestIcon.setPreserveRatio(true);

        // Create Bounce Animation
        TranslateTransition bounce = new TranslateTransition(Duration.millis(600), chestIcon);
        bounce.setByY(-2);
        bounce.setCycleCount(Animation.INDEFINITE);
        bounce.setAutoReverse(true);
        bounce.play();

        // Create Button with Icon
        rewardBtn = new Button(" REWARDS", chestIcon);
        rewardBtn.setContentDisplay(ContentDisplay.LEFT);

        // Remove inline styles and use CSS classes
        rewardBtn.getStyleClass().addAll(ViewConstants.PIXEL_BUTTON_STYLE_CLASS, ViewConstants.PIXEL_BUTTON_GOLD);

        rewardBtn.setOnAction(_ -> dailyRewardView.toggle(true));

        StackPane.setAlignment(rewardBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(rewardBtn, new Insets(20, 20, 0, 0));
        root.getChildren().add(rewardBtn);
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
            // Also styling exit button
            exitBtn.getStyleClass().addAll("pixel-btn", "pixel-btn-danger");
            exitBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");

            exitBtn.setOnAction(_ -> exitMinigameMode());
            StackPane.setAlignment(exitBtn, Pos.TOP_RIGHT);

            tvClickArea.getChildren().addAll(gamePane, exitBtn);
        }
    }
}