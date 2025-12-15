package com.eleven.pet.daily_reward;

import com.eleven.pet.character.SpriteSheetAnimation;
import com.eleven.pet.core.AssetLoader;
import com.eleven.pet.inventory.Item;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.util.Random;

/**
 * Visual JavaFX component that represents a single reward chest.
 * <p>
 * It is responsible for playing the open animation, spawning particles,
 * and briefly displaying the rewarded item when opened. Game logic
 * callbacks are delegated via {@link #setOnOpen(Runnable)}.
 */
public class ChestComponent extends StackPane {

    // --- Configuration Constants ---
    // These should match the single frame size
    private static final int SHEET_WIDTH = 150;
    private static final int SHEET_HEIGHT = 118;
    private static final int GRID_COLS = 1;
    private static final int TOTAL_FRAMES = 2;
    private static final float FRAME_DURATION = 0.1f;
    private static final double SCALE = 1.0; 

    // --- Components ---
    private final SpriteSheetAnimation animLogic;
    private final ImageView spriteView;
    private final Pane particleLayer;
    private AnimationTimer gameLoop;

    // --- State ---
    private boolean isOpened = false;
    private Runnable onOpenCallback; // Optional: Hook for your main view
    private Chest chestModel; // Add chest model reference

    /**
     * Creates a new chest component bound to the given chest model.
     *
     * @param chest the chest model that describes the contained reward
     */
    public ChestComponent(Chest chest) {
        this.chestModel = chest;
        // 1. Load Image
        Image sheetChest = AssetLoader.getInstance().getImage("chest/Chest");
        
        // 2. Calculate Frame Sizes
        // Since GRID_COLS = 1, the frames are vertical.
        // We must divide total height by number of rows (TOTAL_FRAMES)
        int frameW = (int) sheetChest.getWidth() / GRID_COLS;
        int frameH = (int) sheetChest.getHeight() / TOTAL_FRAMES; 

        // 3. Setup Layout
        this.setAlignment(Pos.BOTTOM_CENTER);
        
        // 4. Setup Sprite Logic (Your Class)
        // Pass the calculated single frame dimensions
        animLogic = new SpriteSheetAnimation(frameW, frameH, GRID_COLS, TOTAL_FRAMES, FRAME_DURATION);
        animLogic.setLoop(false);

        // 5. Setup JavaFX ImageView
        spriteView = new ImageView(sheetChest);
        // Fix: Set viewport to ONLY the first frame initially
        spriteView.setViewport(new Rectangle2D(0, 0, frameW, frameH));
        spriteView.setFitWidth(frameW * SCALE);
        spriteView.setFitHeight(frameH * SCALE);
        spriteView.setSmooth(false); // Pixel art look

        // 6. Setup Particle Layer (Overlay)
        particleLayer = new Pane();
        particleLayer.setPickOnBounds(false); // Let clicks pass through to the chest
        particleLayer.setPrefSize(frameW * SCALE, frameH * SCALE);

        // Add to StackPane
        this.getChildren().addAll(spriteView, particleLayer);

        // 7. Start Animation Loop
        startGameLoop();
        
        // 8. Setup Mouse Interactions
        setupInteractions();
    }

    /**
     * Registers an action to be executed when the chest is opened.
     * <p>
     * Typical usage is to claim the reward in the controller or model.
     *
     * @param action callback invoked once when the chest is successfully opened
     */
    public void setOnOpen(Runnable action) {
        this.onOpenCallback = action;
    }

    /**
     * Starts the internal {@link AnimationTimer} that advances the
     * spritesheet animation and updates the chest's viewport each frame.
     */
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                
                float deltaTime = (now - lastTime) / 1_000_000_000.0f;
                
                // Update Logic
                animLogic.update(deltaTime);
                
                // Update Visuals (Only if playing to save resources)
                if (animLogic.isPlaying()) {
                    int x = animLogic.getFrameX();
                    int y = animLogic.getFrameY();
                    spriteView.setViewport(new Rectangle2D(x, y, animLogic.getFrameWidth(), animLogic.getFrameHeight()));
                }

                lastTime = now;
            }
        };
        gameLoop.start();
    }

    /**
     * Wires mouse hover and click handlers for the chest:
     * <ul>
     * <li>Hover: Move up (translateY) and show hand cursor if not opened.</li>
     * <li>Click: triggers {@link #performOpen()} if not opened.</li>
     * </ul>
     */
    private void setupInteractions() {
        // Hover
        this.setOnMouseEntered(e -> {
            if (!isOpened) {
                // Use Translation instead of Scale to avoid pixel distortion and size conflicts
                this.setTranslateY(-10); 
                this.setCursor(Cursor.HAND);
            }
        });
        this.setOnMouseExited(e -> {
            // Reset position
            this.setTranslateY(0);
            this.setCursor(Cursor.DEFAULT);
        });

        // Click
        this.setOnMouseClicked(e -> {
            if (!isOpened) {
                performOpen();
            }
        });
    }

    /**
     * Handles the visual and logical flow when the chest is opened.
     * <p>
     * Plays the chest animation, spawns particles, invokes the
     * on-open callback, and shows the rewarded item.
     * This method is a no-op if the model is already opened.
     */
    private void performOpen() {
        if (chestModel != null && !chestModel.isOpened()) {
            isOpened = true;
            this.setTranslateY(0); // Reset hover elevation
            animLogic.play(); // Play the spritesheet animation
            spawnParticles(); // Fire particles
            
            // Trigger callback if one exists (This actually adds the item to inventory via DailyRewardView -> Chest.open)
            if (onOpenCallback != null) {
                onOpenCallback.run();
            }

            // VISUAL: Show the item gained
            showItemReward();
        }
    }

    /**
     * Visually displays the rewarded item above the chest using a
     * short float-up, scale, and fade animation.
     * <p>
     * If the underlying chest has no item, nothing is shown.
     */
    private void showItemReward() {
        Item item = chestModel.getItem();
        if (item == null) return;

        // Container for item icon + text
        VBox rewardContainer = new VBox(5);
        rewardContainer.setAlignment(Pos.CENTER);
        rewardContainer.setMouseTransparent(true); // Don't block clicks

        // Item Image
        Image itemImg = AssetLoader.getInstance().getImage("items/" + item.imageFileName());
        ImageView itemIcon = new ImageView(itemImg);
        itemIcon.setFitWidth(40);
        itemIcon.setFitHeight(40);
        itemIcon.setEffect(new DropShadow(10, Color.GOLD));

        // Quantity/Name Text
        Label itemLabel = new Label(chestModel.getQuantity() + "x " + item.name());
        itemLabel.setTextFill(Color.WHITE);
        itemLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        itemLabel.setStyle("-fx-effect: dropshadow(one-pass-box, black, 2, 0.5, 0, 0);");

        rewardContainer.getChildren().addAll(itemIcon, itemLabel);
        
        // Position at center of chest initially
        rewardContainer.setTranslateX(this.getWidth() / 2 - 20); // Center adjustment approx
        rewardContainer.setTranslateY(this.getHeight() / 2 - 40); 
        
        particleLayer.getChildren().add(rewardContainer);

        // Animation: Float Up + Scale + Fade
        TranslateTransition moveUp = new TranslateTransition(Duration.millis(1500), rewardContainer);
        moveUp.setByY(-80);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), rewardContainer);
        scale.setFromX(0.0); scale.setFromY(0.0);
        scale.setToX(1.5); scale.setToY(1.5);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), rewardContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.millis(1000)); // Wait before fading

        ParallelTransition seq = new ParallelTransition(moveUp, scale, fadeOut);
        seq.setOnFinished(e -> particleLayer.getChildren().remove(rewardContainer));
        seq.play();
    }

    /**
     * Spawns a burst of gold particle circles that move outward and fade,
     * used to give visual feedback when the chest is opened.
     */
    private void spawnParticles() {
        Random rand = new Random();
        double w = spriteView.getFitWidth();
        double h = spriteView.getFitHeight();

        for (int i = 0; i < 15; i++) {
            Circle p = new Circle(4, Color.GOLD);
            // Start from center of chest
            p.setTranslateX(w / 2);
            p.setTranslateY(h / 2);
            
            particleLayer.getChildren().add(p);

            // Movement Animation
            TranslateTransition move = new TranslateTransition(Duration.millis(800), p);
            move.setByX((rand.nextDouble() - 0.5) * 100);
            move.setByY(-80 - (rand.nextDouble() * 50)); // Move Up

            // Fade Animation
            FadeTransition fade = new FadeTransition(Duration.millis(800), p);
            fade.setFromValue(1);
            fade.setToValue(0);

            ParallelTransition pt = new ParallelTransition(move, fade);
            pt.setOnFinished(ev -> particleLayer.getChildren().remove(p)); // Cleanup
            pt.play();
        }
    }
}