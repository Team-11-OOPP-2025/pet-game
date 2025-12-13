package com.eleven.pet.daily_reward;

import com.eleven.pet.character.SpriteSheetAnimation;
import com.eleven.pet.core.AssetLoader;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import java.util.Random;

public class ChestComponent extends StackPane {

    // --- Configuration Constants ---
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

    public ChestComponent(Chest chest) {
        this.chestModel = chest;
        // 1. Load Image
        Image sheetChest = AssetLoader.getInstance().getImage("chest/Chest");
        
        // 2. Calculate Frame Sizes
        int frameW = (int) sheetChest.getWidth() / GRID_COLS;
        int frameH = (int) sheetChest.getHeight(); // Assuming 1 row

        // 3. Setup Layout
        this.setAlignment(Pos.BOTTOM_CENTER);
        
        // 4. Setup Sprite Logic (Your Class)
        animLogic = new SpriteSheetAnimation(SHEET_WIDTH, SHEET_HEIGHT, GRID_COLS, TOTAL_FRAMES, FRAME_DURATION);
        animLogic.setLoop(false);

        // 5. Setup JavaFX ImageView
        spriteView = new ImageView(sheetChest);
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
     * Optional: Set code to run when the chest opens (e.g., give loot)
     */
    public void setOnOpen(Runnable action) {
        this.onOpenCallback = action;
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                
                float deltaTime = (now - lastTime) / 1_000_000_000.0f;
                
                // Update Logic
                animLogic.update(deltaTime);
                
                // Update Visuals
                int x = animLogic.getFrameX();
                int y = animLogic.getFrameY();
                spriteView.setViewport(new Rectangle2D(x, y, animLogic.getFrameWidth(), animLogic.getFrameHeight()));

                lastTime = now;
            }
        };
        gameLoop.start();
    }

    private void setupInteractions() {
        // Hover
        this.setOnMouseEntered(e -> {
            if (!isOpened) {
                this.setScaleX(1.1);
                this.setScaleY(1.1);
                this.setCursor(Cursor.HAND);
            }
        });
        this.setOnMouseExited(e -> {
            this.setScaleX(1.0);
            this.setScaleY(1.0);
            this.setCursor(Cursor.DEFAULT);
        });

        // Click
        this.setOnMouseClicked(e -> {
            if (!isOpened) {
                performOpen();
            }
        });
    }

    private void performOpen() {
        if (chestModel != null && !chestModel.isOpened()) {
            isOpened = true;
            animLogic.play(); // Play the spritesheet animation
            spawnParticles(); // Fire particles
            
            // Trigger callback if one exists
            if (onOpenCallback != null) {
                onOpenCallback.run();
            }
        }
    }

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