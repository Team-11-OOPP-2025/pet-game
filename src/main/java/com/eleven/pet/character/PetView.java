package com.eleven.pet.character;

import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.character.behavior.PetState;
import com.eleven.pet.core.AssetLoader;
import com.eleven.pet.environment.time.DayCycle;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.inventory.ItemRegistry;
import com.eleven.pet.vfx.ParticleSystem;
import com.eleven.pet.vfx.effects.ParticleEffect;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class PetView {
    private final PetModel model;
    private final PetController controller;
    private final GameClock clock;
    private final WeatherSystem weatherSystem;
    private final AssetLoader assetLoader;
    private final ParticleSystem particleSystem;

    private StackPane worldLayer; // Zoomable layer (BG + Pet + TV)
    private StackPane uiLayer;    // Static layer (HUD + Controls)
    private StackPane tvClickArea; // The clickable TV box

    private StackPane inventoryModal;

    private ImageView backgroundView;
    private ImageView petImageView;
    private Label timeLabel;

    private Rectangle hungerFill;
    private Rectangle energyFill;
    private Rectangle cleanFill;
    private Rectangle happinessFill;

    private StackPane sleepBtnContainer;

    private static final int SHEET_WIDTH = 309;
    private static final int SHEET_HEIGHT = 460;
    private static final int GRID_COLS = 2;

    // Animations
    private SpriteSheetAnimation animNeutral;
    private SpriteSheetAnimation animHappy;
    private SpriteSheetAnimation animSad;
    private SpriteSheetAnimation animCrying;
    private SpriteSheetAnimation animSleeping;

    private SpriteSheetAnimation activeAnimation;
    private Image activeSpriteSheet;
    private AnimationTimer renderLoop;
    private long lastFrameTime;

    private Image sheetNeutral, sheetSad, sheetSleeping, sheetCrying, sheetHappy, backgroundDay;

    // Zoom State
    private boolean isGameMode = false;
    private static final double ZOOM_FACTOR = 3.0;

    public PetView(PetModel model, PetController controller, GameClock clock, WeatherSystem weather) {
        this.model = model;
        this.controller = controller;
        this.clock = clock;
        this.weatherSystem = weather;
        this.assetLoader = AssetLoader.getInstance();
        this.particleSystem = new ParticleSystem(624, 351); // Match scene dimensions

        loadAssets();
        initializeAnimations();
    }

    public Pane initializeUI() {
        StackPane root = new StackPane();

        // 1. Create the Layers
        worldLayer = new StackPane(); // This will scale up
        uiLayer = new StackPane(); // This stays static
        uiLayer.setPickOnBounds(false); // Allow clicking through empty UI space

        // 2. Setup World (Background -> Particles -> TV -> Pet)
        setupBackgroundLayer(worldLayer);
        setupParticleLayer(worldLayer);
        setupTVLayer(worldLayer);
        setupPetLayer(worldLayer);

        // 3. Setup UI (HUD -> Controls)
        setupHUDLayer(uiLayer);
        setupControlLayer(uiLayer);
        setupInventoryUI(uiLayer);

        // 4. Add to Root
        root.getChildren().addAll(worldLayer, uiLayer);

        bindData();
        observeEnvironment();
        startRenderLoop();
        refreshPetState();

        return root;
    }

    private void setupInventoryUI(StackPane root) {
        inventoryModal = new StackPane();
        inventoryModal.setVisible(false); // Hidden by default

        // Darkened background (Clicking this closes the inventory)
        Region backdrop = new Region();
        backdrop.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        backdrop.setOnMouseClicked(e -> toggleInventory(false));

        // The Main Panel
        VBox inventoryPanel = new VBox(10);
        inventoryPanel.setMaxSize(350, 250);
        inventoryPanel.setStyle("-fx-background-color: #fdf5e6; -fx-background-radius: 15; -fx-border-color: #8b4513; -fx-border-width: 4; -fx-border-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);");
        inventoryPanel.setAlignment(Pos.TOP_CENTER);

        // Title
        Label title = new Label("INVENTORY");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#8b4513"));

        // Grid of Items
        TilePane itemGrid = new TilePane();
        itemGrid.setHgap(10);
        itemGrid.setVgap(10);
        itemGrid.setPrefColumns(3);
        itemGrid.setAlignment(Pos.CENTER);

        // Add items that are ALREADY in the inventory map
        model.getInventory().getItems().forEach((id, qtyProp) -> {
            if (qtyProp.get() > 0) {
                Item item = ItemRegistry.get(id);
                if (item != null) {
                    StackPane slot = createItemSlot(item);
                    slot.setUserData(id); // Store ID to find it later for removal
                    itemGrid.getChildren().add(slot);
                }
            }
        });

        // Listen for changes in the inventory map (ADDITIONS and REMOVALS)
        model.getInventory().getItems().addListener((MapChangeListener<Integer, IntegerProperty>) change -> {
            // CASE A: New Item Type Added to Map
            if (change.wasAdded()) {
                // Check if we already have a slot (just in case)
                boolean exists = itemGrid.getChildren().stream()
                        .anyMatch(node -> node.getUserData().equals(change.getKey()));

                if (!exists) {
                    Item item = ItemRegistry.get(change.getKey());
                    if (item != null) {
                        StackPane slot = createItemSlot(item);
                        slot.setUserData(change.getKey());
                        itemGrid.getChildren().add(slot);
                    }
                }
            }
        });

        ScrollPane scroll = new ScrollPane(itemGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Close Button
        Button closeBtn = new Button("CLOSE");
        closeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        closeBtn.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white; -fx-background-radius: 20;");
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setOnAction(e -> toggleInventory(false));

        inventoryPanel.getChildren().addAll(title, scroll, closeBtn);

        // Layout: Add backdrop then panel
        inventoryModal.getChildren().add(backdrop);
        inventoryModal.getChildren().add(inventoryPanel);

        // POSITIONING: Bottom Left, aligned above the Feed Button
        StackPane.setAlignment(inventoryPanel, Pos.BOTTOM_LEFT);
        // Margin Bottom = 90 (Button Y) + 50 (Button Height) + 10 (Gap) = 150
        StackPane.setMargin(inventoryPanel, new Insets(0, 0, 150, 20));

        root.getChildren().add(inventoryModal);
    }

    private StackPane createItemSlot(Item item) {
        StackPane slot = new StackPane();
        slot.setPrefSize(70, 70);
        slot.setStyle("-fx-background-color: white; -fx-border-color: #d2b48c; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        slot.setCursor(Cursor.HAND);

        String lowerName = item.name().toLowerCase();
        javafx.scene.Node iconNode;

        // Try a conventional path in the asset loader: items/<name>
        Image itemImage = assetLoader.getImage("items/" + lowerName);

        ImageView iv = new ImageView(itemImage);
        iv.setFitWidth(70);
        iv.setFitHeight(70);
        iv.setPreserveRatio(true);
        iconNode = iv;

        // Ensure icon is centered and visible
        StackPane.setAlignment(iconNode, Pos.CENTER);
        StackPane.setMargin(iconNode, new Insets(-5, 0, 0, 0));

        // Item Name (Tooltip style, small at bottom)
        Label name = new Label(item.name());
        name.setTextFill(Color.BLACK);
        name.setFont(Font.font("Arial", 9));
        StackPane.setAlignment(name, Pos.BOTTOM_CENTER);
        StackPane.setMargin(name, new Insets(0, 0, 5, 0));

        // Quantity Badge
        Label qty = new Label();
        qty.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        qty.setTextFill(Color.WHITE);
        qty.setStyle("-fx-background-color: red; -fx-background-radius: 10; -fx-padding: 1 5;");
        StackPane.setAlignment(qty, Pos.TOP_RIGHT);
        StackPane.setMargin(qty, new Insets(-5, -5, 0, 0));

        // Bind Quantity
        IntegerProperty amountProp = model.getInventory().amountProperty(item);
        qty.textProperty().bind(Bindings.convert(amountProp));

        // Auto-remove slot when quantity hits 0
        amountProp.addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() <= 0) {
                if (slot.getParent() instanceof Pane) {
                    ((Pane) slot.getParent()).getChildren().remove(slot);
                }
            }
        });

        // Tooltip with preview
        Tooltip tooltip = new Tooltip();
        tooltip.setText("Name: " + item.name() + "\nHeal: " + item.statsRestore() + "\nDescription: " + item.description());
        tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: rgba(50, 50, 50, 0.9); -fx-text-fill: white;");
        Tooltip.install(slot, tooltip);

        // Interaction
        slot.setOnMouseClicked(e -> {
            controller.handleConsumeAction(item);
            ScaleTransition st = new ScaleTransition(Duration.millis(100), slot);
            st.setFromX(1.0);
            st.setFromY(1.0);
            st.setToX(0.9);
            st.setToY(0.9);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            st.play();
        });

        slot.getChildren().addAll(iconNode, name, qty);
        return slot;
    }

    private void toggleInventory(boolean show) {
        if (show) {
            inventoryModal.setVisible(true);
            inventoryModal.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(200), inventoryModal);
            ft.setToValue(1.0);
            ft.play();
        } else {
            FadeTransition ft = new FadeTransition(Duration.millis(200), inventoryModal);
            ft.setToValue(0);
            ft.setOnFinished(e -> inventoryModal.setVisible(false));
            ft.play();
        }
    }

    private void setupControlLayer(StackPane root) {
        // TODO: Update to be a beautiful icon instead of TEXT!
        StackPane feedBtnContainer = createActionButton("FEED", Color.WHITE, 120, () -> toggleInventory(true));
        addToLayout(root, feedBtnContainer, Pos.BOTTOM_LEFT, 0, 0, 90, 20);

        StackPane cleanBtnContainer = createActionButton("CLEAN", Color.WHITE, 120, controller::handleCleanAction);
        addToLayout(root, cleanBtnContainer, Pos.BOTTOM_LEFT, 0, 0, 90, 150);

        sleepBtnContainer = createActionButton("SLEEP", Color.web("#3498db"), 120, controller::handleSleepAction);
        ((Button) sleepBtnContainer.getChildren().get(1)).setTextFill(Color.WHITE);
        sleepBtnContainer.setVisible(false);
        addToLayout(root, sleepBtnContainer, Pos.BOTTOM_LEFT, 0, 0, 150, 20);

        StackPane playBtnContainer = createActionButton("PLAY", Color.WHITE, 140, controller::handlePlayAction);
        addToLayout(root, playBtnContainer, Pos.BOTTOM_RIGHT, 0, 20, 90, 0);
    }

    private void setupBackgroundLayer(StackPane container) {
        backgroundView = new ImageView();
        backgroundView.setPreserveRatio(false);
        // Bind to the container so it resizes dynamically
        backgroundView.fitWidthProperty().bind(container.widthProperty());
        backgroundView.fitHeightProperty().bind(container.heightProperty());

        if (clock != null) updateBackground(clock.getCycle());
        else backgroundView.setImage(backgroundDay);

        container.getChildren().add(backgroundView);
    }

    private void setupParticleLayer(StackPane container) {
        // Reference canvas dimensions (same as TV layer)
        double REF_WIDTH = 624;
        double REF_HEIGHT = 351;
        
        // Window coordinates - positioned above the pet (center-top area)
        // The pet is at bottom-center, so window should be center-upper area
        double WINDOW_X = 230;      // Moved to the right
        double WINDOW_Y = 20;       // Upper part of the screen
        double WINDOW_WIDTH = 120;  // Window width (skinnier)
        double WINDOW_HEIGHT = 200; // Window height (stretched down to pet's head)
        
        // Add particle canvas for weather effects (only over window)
        var canvas = particleSystem.getCanvas();
        canvas.setMouseTransparent(true); // Don't block mouse events
        
        // Create a pane to position the particle canvas
        Pane particlePane = new Pane();
        particlePane.setPickOnBounds(false);
        particlePane.getChildren().add(canvas);
        
        // Position and size the canvas to match the window area
        canvas.layoutXProperty().bind(container.widthProperty().multiply(WINDOW_X / REF_WIDTH));
        canvas.layoutYProperty().bind(container.heightProperty().multiply(WINDOW_Y / REF_HEIGHT));
        canvas.widthProperty().bind(container.widthProperty().multiply(WINDOW_WIDTH / REF_WIDTH));
        canvas.heightProperty().bind(container.heightProperty().multiply(WINDOW_HEIGHT / REF_HEIGHT));
        
        container.getChildren().add(particlePane);
    }

    private void setupTVLayer(StackPane container) {
        // We use a transparent Pane overlay to position the TV click area
        Pane tvOverlay = new Pane();
        tvOverlay.setPickOnBounds(false); // Only the TV box should be clickable

        tvClickArea = new StackPane();
        tvClickArea.setCursor(Cursor.HAND);

        // Reference canvas dimensions that the UI layout was originally designed for.
        // These are used to compute relative positions and sizes so the TV area scales
        // correctly on different window sizes.
        double REF_WIDTH = 624;
        double REF_HEIGHT = 351;

        // TV rectangle coordinates and size in the reference canvas (pixels).
        // They represent the TV's top-left corner (TV_X, TV_Y) and its width/height.
        double TV_X = 462;
        double TV_Y = 142;
        double TV_WIDTH = 112;
        double TV_HEIGHT = 72;

        // Bind the tvClickArea position and preferred size to the container's size,
        // preserving the same relative position/size as in the reference layout.
        tvClickArea.layoutXProperty().bind(container.widthProperty().multiply(TV_X / REF_WIDTH));
        tvClickArea.layoutYProperty().bind(container.heightProperty().multiply(TV_Y / REF_HEIGHT));
        tvClickArea.prefWidthProperty().bind(container.widthProperty().multiply(TV_WIDTH / REF_WIDTH));
        tvClickArea.prefHeightProperty().bind(container.heightProperty().multiply(TV_HEIGHT / REF_HEIGHT));

        tvClickArea.setOnMouseClicked(e -> enterMinigameMode());

        tvOverlay.getChildren().add(tvClickArea);
        container.getChildren().add(tvOverlay);
    }

    private void setupPetLayer(StackPane container) {
        petImageView = new ImageView();
        petImageView.setFitWidth(SHEET_WIDTH);
        petImageView.setFitHeight(SHEET_HEIGHT);
        petImageView.setPreserveRatio(true);
        petImageView.setStyle("-fx-cursor: hand;");

        StackPane.setAlignment(petImageView, Pos.BOTTOM_CENTER);
        StackPane.setMargin(petImageView, new Insets(0, 0, 20, 0));

        container.getChildren().add(petImageView);
    }

    private void enterMinigameMode() {
        if (isGameMode) return;
        isGameMode = true;

        // Calculate Center Points
        double sceneW = worldLayer.getWidth();
        double sceneH = worldLayer.getHeight();

        // The center of the TV (Target)
        double tvCenterX = tvClickArea.getLayoutX() + (tvClickArea.getWidth() / 2);
        double tvCenterY = tvClickArea.getLayoutY() + (tvClickArea.getHeight() / 2);

        // Justification: We shift the view center 100px to the Right.
        // This pushes the TV 100px to the Left of the screen, leaving empty space on the Right.
        double viewOffsetX = 100;

        // Formula: (ScreenCenter - ObjectCenter + Offset) * ZoomFactor
        // We add Offset to shift the camera focus point
        double transX = ((sceneW / 2) - tvCenterX + viewOffsetX) * ZOOM_FACTOR;
        double transY = ((sceneH / 2) - tvCenterY) * ZOOM_FACTOR;

        ParallelTransition pt = new ParallelTransition();

        // Zoom and Pan the World
        Timeline zoom = new Timeline(
                new KeyFrame(Duration.millis(800),
                        new KeyValue(worldLayer.scaleXProperty(), ZOOM_FACTOR, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.scaleYProperty(), ZOOM_FACTOR, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.translateXProperty(), transX, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.translateYProperty(), transY, Interpolator.EASE_BOTH)
                )
        );

        // Move Pet to the side
        TranslateTransition movePet = new TranslateTransition(Duration.millis(800), petImageView);

        // MATH DERIVATION:
        // We calculate the delta required to move from Current Layout Position to Target (Right of TV)
        // Target X (600) - Start X (approx 250) = 350
        // Target Y (-20) - Start Y (approx 180) = -200
        // Using "movePet.setByX" would also work, but setToX works on translation property directly.
        movePet.setToX(350);
        movePet.setToY(-200);

        // Scale Pet Down (Perspective effect - 0.5 is 50% of the ORIGINAL size, making it look deeper in scene)
        ScaleTransition scalePet = new ScaleTransition(Duration.millis(800), petImageView);
        scalePet.setToX(0.5);
        scalePet.setToY(0.5);

        // Fade out UI
        FadeTransition fadeUI = new FadeTransition(Duration.millis(300), uiLayer);
        fadeUI.setToValue(0);

        pt.getChildren().addAll(zoom, movePet, scalePet, fadeUI);
        pt.setOnFinished(_ -> {
            loadGameContent();
        });
        pt.play();
    }

    private void exitMinigameMode() {
        if (!isGameMode) return;
        isGameMode = false;

        tvClickArea.getChildren().clear(); // Remove game pane

        ParallelTransition pt = new ParallelTransition();

        // Reset World Scale and Position
        Timeline zoomOut = new Timeline(
                new KeyFrame(Duration.millis(800),
                        new KeyValue(worldLayer.scaleXProperty(), 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.scaleYProperty(), 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.translateXProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(worldLayer.translateYProperty(), 0, Interpolator.EASE_BOTH)
                )
        );

        // Reset Pet Position
        TranslateTransition movePet = new TranslateTransition(Duration.millis(800), petImageView);
        movePet.setToX(0);
        movePet.setToY(0);

        // Reset Pet Scale (Back to original)
        ScaleTransition scalePet = new ScaleTransition(Duration.millis(800), petImageView);
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
            exitBtn.setOnAction(e -> exitMinigameMode());
            StackPane.setAlignment(exitBtn, Pos.TOP_RIGHT);

            tvClickArea.getChildren().addAll(gamePane, exitBtn);
        }
    }

    private void setupHUDLayer(StackPane root) {
        // Stats
        StackPane happyBar = createStatBar("Happiness", "😃", Color.web("#f4d03f"), 225, 38);
        happinessFill = (Rectangle) happyBar.getChildren().get(1);
        addToLayout(root, happyBar, Pos.TOP_LEFT, 90, 0, 0, 20);

        StackPane hungerBar = createStatBar("Hunger", "🍖 ", Color.web("#2ecc71"), 150, 25);
        hungerFill = (Rectangle) hungerBar.getChildren().get(1);
        addToLayout(root, hungerBar, Pos.TOP_LEFT, 148, 0, 0, 20);

        StackPane energyBar = createStatBar("Energy", "⚡️ ", Color.web("#f39c12"), 150, 25);
        energyFill = (Rectangle) energyBar.getChildren().get(1);
        addToLayout(root, energyBar, Pos.TOP_LEFT, 193, 0, 0, 20);

        StackPane cleanBar = createStatBar("Clean", "🧽 ", Color.web("#3498db"), 150, 25);
        cleanFill = (Rectangle) cleanBar.getChildren().get(1);
        addToLayout(root, cleanBar, Pos.TOP_LEFT, 238, 0, 0, 20);

        timeLabel = createClockWidget();
        addToLayout(root, timeLabel, Pos.TOP_CENTER, 20, 0, 0, 0);
    }

    private StackPane createStatBar(String label, String icon, Color color, double width, double height) {
        StackPane container = new StackPane();
        container.setMinSize(width, height);
        container.setMaxSize(width, height);

        Rectangle border = new Rectangle(width, height, Color.WHITE);
        border.setStroke(Color.BLACK);
        border.setStrokeWidth(3);

        Rectangle fill = new Rectangle(0, height, color);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);

        Text iconText = new Text(icon);
        iconText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        StackPane.setAlignment(iconText, Pos.CENTER_LEFT);
        StackPane.setMargin(iconText, new Insets(0, 0, 0, 10));

        Text labelText = new Text(label);
        labelText.setFont(Font.font("Arial", FontWeight.BOLD, label.equals("Happiness") ? 16 : 14));
        StackPane.setAlignment(labelText, Pos.CENTER_LEFT);
        StackPane.setMargin(labelText, new Insets(0, 0, 0, label.equals("Happiness") ? 35 : 25));

        container.getChildren().addAll(border, fill, iconText, labelText);
        return container;
    }

    private StackPane createActionButton(String text, Color bg, double width, Runnable action) {
        StackPane container = new StackPane();
        container.setMaxSize(width, 50);

        Rectangle border = new Rectangle(width, 50, bg);
        border.setStroke(Color.BLACK);
        border.setStrokeWidth(3);

        Button btn = new Button(text);
        btn.setPrefSize(width, 50);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setTextFill(bg.equals(Color.WHITE) ? Color.BLACK : Color.WHITE);
        btn.setStyle("-fx-background-color: transparent;");
        btn.setOnAction(_ -> {
            if (controller != null) action.run();
        });

        container.getChildren().addAll(border, btn);
        return container;
    }

    private Label createClockWidget() {
        Label lbl = new Label("00:00");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        lbl.setTextFill(Color.WHITE);
        lbl.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10; -fx-padding: 10 20;");
        return lbl;
    }

    private void addToLayout(StackPane root, javafx.scene.Node node, Pos pos, double t, double r, double b, double l) {
        StackPane.setAlignment(node, pos);
        StackPane.setMargin(node, new Insets(t, r, b, l));
        root.getChildren().add(node);
    }

    private void loadAssets() {
        sheetHappy = assetLoader.getImage("sprites/happy/SpriteSheetHappy");
        sheetNeutral = assetLoader.getImage("sprites/idle/SpriteSheetNeutral");
        sheetSad = assetLoader.getImage("sprites/sad/SpriteSheetSad");
        sheetCrying = assetLoader.getImage("sprites/sad/SpriteSheetCrying");
        sheetSleeping = assetLoader.getImage("sprites/sleeping/SpriteSheetSleeping");
        backgroundDay = assetLoader.getImage("backgrounds/DAY");
    }

    private void initializeAnimations() {
        animNeutral = new SpriteSheetAnimation(SHEET_WIDTH, SHEET_HEIGHT, GRID_COLS, 3, 1.0f);
        animNeutral.setLoop(true);

        animHappy = new SpriteSheetAnimation(SHEET_WIDTH, SHEET_HEIGHT, GRID_COLS, 4, 1f);
        animHappy.setLoop(true);

        animSad = new SpriteSheetAnimation(SHEET_WIDTH, SHEET_HEIGHT, GRID_COLS, 2, 1f);
        animSad.setLoop(true);

        animCrying = new SpriteSheetAnimation(SHEET_WIDTH, SHEET_HEIGHT, GRID_COLS, 2, 1f);
        animCrying.setLoop(true);

        animSleeping = new SpriteSheetAnimation(SHEET_WIDTH, SHEET_HEIGHT, GRID_COLS, 2, 1.5f);
        animSleeping.setLoop(true);
    }

    private void startRenderLoop() {
        lastFrameTime = System.nanoTime();
        renderLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                float deltaTime = (now - lastFrameTime) / 1_000_000_000.0f;
                lastFrameTime = now;

                if (activeAnimation != null) {
                    activeAnimation.update(deltaTime);
                    renderFrame();
                }
            }
        };
        renderLoop.start();
    }

    private void renderFrame() {
        if (petImageView == null || activeAnimation == null) return;

        petImageView.setViewport(new Rectangle2D(
                activeAnimation.getFrameX(),
                activeAnimation.getFrameY(),
                activeAnimation.getFrameWidth(),
                activeAnimation.getFrameHeight()
        ));

        Image correctSheet = resolveSheetForAnimation(activeAnimation);
        if (correctSheet != null && correctSheet != activeSpriteSheet) {
            activeSpriteSheet = correctSheet;
            petImageView.setImage(activeSpriteSheet);
        }
    }

    private Image resolveSheetForAnimation(SpriteSheetAnimation anim) {
        if (anim == animSleeping) return sheetSleeping;
        if (anim == animSad) return sheetSad;
        if (anim == animCrying) return sheetCrying;
        if (anim == animHappy) return sheetHappy;
        return sheetNeutral;
    }

    private void changeAnimation(SpriteSheetAnimation newAnim) {
        if (activeAnimation == newAnim) return;

        if (activeAnimation != null) activeAnimation.pause();
        activeAnimation = newAnim;
        activeAnimation.reset();
        activeAnimation.play();
    }

    private void bindData() {
        if (model == null) return;

        // Stats
        PetStats stats = model.getStats();
        if (stats != null) {
            bindBar(stats.getStat(PetStats.STAT_HUNGER), hungerFill, 150);
            bindBar(stats.getStat(PetStats.STAT_ENERGY), energyFill, 150);
            bindBar(stats.getStat(PetStats.STAT_CLEANLINESS), cleanFill, 150);

            var happyStat = stats.getStat(PetStats.STAT_HAPPINESS);
            if (happyStat != null) {
                happyStat.addListener((obs, old, val) -> {
                    updateFill(happinessFill, val.intValue(), 225);
                    refreshPetState();
                });
                updateFill(happinessFill, happyStat.get(), 225);
            }
        }

        model.getStateProperty().addListener((obs, old, state) -> refreshPetState());
    }

    private void bindBar(javafx.beans.value.ObservableValue<Number> stat, Rectangle fill, double maxW) {
        if (stat != null) {
            stat.addListener((obs, old, val) -> updateFill(fill, val.intValue(), maxW));
            updateFill(fill, stat.getValue().intValue(), maxW);
        }
    }

    private void updateFill(Rectangle rect, int value, double maxWidth) {
        rect.setWidth(maxWidth * (value / 100.0));
    }

    private void observeEnvironment() {
        if (clock == null) return;

        clock.cycleProperty().addListener((obs, old, cycle) -> updateBackground(cycle));

        clock.gameTimeProperty().addListener((obs, old, time) -> {
            double t = time.doubleValue();
            updateClockLabel(t);
            boolean canSleep = controller.isSleepAllowed();
            sleepBtnContainer.setVisible(canSleep);
        });
        
        // Listen for weather changes and update particle effects
        if (weatherSystem != null) {
            weatherSystem.getWeatherProperty().addListener((obs, oldWeather, newWeather) -> {
                updateWeatherEffects(newWeather);
            });
            
            // Set initial weather effect if weather is already set
            if (weatherSystem.getCurrentWeather() != null) {
                updateWeatherEffects(weatherSystem.getCurrentWeather());
            }
        }
    }
    
    private void updateWeatherEffects(WeatherState weather) {
        if (weather == null || particleSystem == null) return;
        
        // Stop any existing particle effects first
        particleSystem.stopAnimation();
        
        // Start new effect if one exists for this weather
        ParticleEffect effect = weather.getParticleEffect();
        if (effect != null) {
            effect.start(particleSystem);
        }
    }

    private void refreshPetState() {
        if (model == null) return;

        PetState currentState = model.getCurrentState();

        if (currentState instanceof AsleepState) {
            changeAnimation(animSleeping);
            toggleSleepButton(true);
        } else {
            switch (controller.calculateEmotion()) {
                case VERY_HAPPY:
                    changeAnimation(animHappy);
                    break;
                case SAD:
                    changeAnimation(animSad);
                    break;
                case VERY_SAD:
                    changeAnimation(animCrying);
                    break;
                case NEUTRAL:
                default:
                    changeAnimation(animNeutral);
                    break;
            }
            toggleSleepButton(false);
        }
    }

    private void updateBackground(DayCycle cycle) {
        Image bg = assetLoader.getImage("backgrounds/" + cycle.name());
        if (bg != null) backgroundView.setImage(bg);
    }

    private void updateClockLabel(double time) {
        int hours = (int) time % 24;
        timeLabel.setText(String.format("%02d:00", hours));
    }

    private void toggleSleepButton(boolean isSleeping) {
        sleepBtnContainer.setDisable(isSleeping);
        sleepBtnContainer.setOpacity(isSleeping ? 0.5 : 1.0);
    }
}