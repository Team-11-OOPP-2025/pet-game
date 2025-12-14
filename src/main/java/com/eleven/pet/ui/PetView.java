package com.eleven.pet.ui;

import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.character.ui.PetAvatarView;
import com.eleven.pet.core.AssetLoader;
import com.eleven.pet.daily_reward.Chest;
import com.eleven.pet.daily_reward.ChestComponent;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.ui.InventoryView;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class PetView {
    private final PetModel model;
    private final PetController controller;
    private final GameClock clock;
    private final WeatherSystem weatherSystem;
    private final AssetLoader assetLoader;

    private StackPane worldLayer; // Zoomable layer (BG + Pet + TV)
    private StackPane uiLayer;    // Static layer (HUD + Controls + Modals)

    private PetAvatarView petAvatarView;
    private InventoryView inventoryView;
    private WorldView worldView;
    private HUDView hudView;
    
    // Daily Rewards
    private StackPane dailyRewardModal;

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
        worldView.getTvClickArea().setOnMouseClicked(_ -> enterMinigameMode());
        worldLayer.getChildren().addAll(worldView, petAvatarView);

        // 4. Compose UI
        uiLayer.getChildren().addAll(hudView, inventoryView);
        
        // 5. Setup Daily Rewards (The new addition)
        setupDailyRewardUI(uiLayer);
        setupRewardTrigger(uiLayer);

        // 6. Add to Root
        root.getChildren().addAll(worldLayer, uiLayer);

        // 7. Update Potion Timers in HUD (Simple implementation injection)
        // Note: Realistically this should be in HUDView, but for this merge we attach here
        // or ensure HUDView has labels. For now, we rely on the console/backend for potion effects.

        return root;
    }

    // =============================================================
    // DAILY REWARD SYSTEM INTEGRATION
    // =============================================================

    private void setupDailyRewardUI(StackPane root) {
        dailyRewardModal = new StackPane();
        dailyRewardModal.setVisible(false); // Hidden by default

        // Backdrop
        Region backdrop = new Region();
        backdrop.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        backdrop.setOnMouseClicked(e -> toggleDailyReward(false));

        // Main Panel
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxSize(800, 400);
        panel.setStyle("-fx-background-color: #fdf5e6; -fx-background-radius: 20; -fx-border-color: #8b4513; -fx-border-width: 5; -fx-padding: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5);");

        Label title = new Label("DAILY REWARDS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#8b4513"));

        Label subTitle = new Label("Select a chest to claim your prize!");
        subTitle.setFont(Font.font("Arial", 16));
        subTitle.setTextFill(Color.web("#555"));

        // Row of 5 Chests
        HBox chestRow = new HBox(30);
        chestRow.setAlignment(Pos.CENTER);

        for (int i = 0; i < 5; i++) {
            Chest chestModel = new Chest(); 
            ChestComponent visualChest = new ChestComponent(chestModel);
            visualChest.setScaleX(0.9);
            visualChest.setScaleY(0.9);

            visualChest.setOnOpen(() -> {
                chestModel.open(model);
            });

            chestRow.getChildren().add(visualChest);
        }

        Button closeBtn = new Button("CLOSE");
        closeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        closeBtn.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 10 30;");
        closeBtn.setOnAction(e -> toggleDailyReward(false));

        panel.getChildren().addAll(title, subTitle, chestRow, closeBtn);
        dailyRewardModal.getChildren().addAll(backdrop, panel);
        
        root.getChildren().add(dailyRewardModal);
    }

    private void setupRewardTrigger(StackPane root) {
        Image iconImg = assetLoader.getImage("chest/Chest_Icon"); // Ensure you have an icon or it uses fallback
        
        // If image loading fails (returns placeholder), use a styled button
        // Logic checks if asset exists essentially by checking if we get the generic placeholder? 
        // AssetLoader returns a placeholder if missing. Let's just assume we use a button if image looks small/placeholder-y or just use button for now to be safe.
        
        Button btn = new Button("🎁 REWARDS");
        btn.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 10 20; -fx-border-color: black; -fx-border-width: 2px; -fx-border-radius: 20;");
        btn.setOnAction(e -> toggleDailyReward(true));
        
        StackPane.setAlignment(btn, Pos.TOP_RIGHT);
        StackPane.setMargin(btn, new Insets(20, 20, 0, 0));
        root.getChildren().add(btn);
    }

    private void toggleDailyReward(boolean show) {
        if (show) {
            dailyRewardModal.setVisible(true);
            dailyRewardModal.setOpacity(0);
            
            FadeTransition ft = new FadeTransition(Duration.millis(300), dailyRewardModal);
            ft.setToValue(1.0);
            
            ScaleTransition st = new ScaleTransition(Duration.millis(300), dailyRewardModal.getChildren().get(1)); 
            st.setFromX(0.8); st.setFromY(0.8);
            st.setToX(1.0); st.setToY(1.0);

            ParallelTransition pt = new ParallelTransition(ft, st);
            pt.play();
        } else {
            FadeTransition ft = new FadeTransition(Duration.millis(200), dailyRewardModal);
            ft.setToValue(0);
            ft.setOnFinished(e -> dailyRewardModal.setVisible(false));
            ft.play();
        }
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