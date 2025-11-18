package com.eleven.pet;

import com.eleven.pet.model.PetModel;
import com.eleven.pet.view.PetView;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApp extends Application {
    private PetModel petModel;
    private long lastUpdateTime = 0;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Create the pet model
            petModel = new PetModel("MyPet");
            
            // Create the view
            PetView view = new PetView(petModel, null);
            Pane root = view.initializeUI();

            // Set window size to match the pixel art aesthetic (16:9-ish ratio)
            Scene scene = new Scene(root, 800, 600);

            primaryStage.setTitle("Pet Game");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
            
            // Start the game loop
            startGameLoop();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fatal Error: Could not start application. Check KeyLoader or JavaFX setup.");
        }
    }
    
    /**
     * Starts the main game loop that updates the GameClock.
     */
    private void startGameLoop() {
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }
                
                // Calculate elapsed time in seconds
                double elapsedSeconds = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;
                
                // Update the game clock
                if (petModel != null && petModel.getGameClock() != null) {
                    boolean newDayStarted = petModel.getGameClock().tick(elapsedSeconds);
                    
                    if (newDayStarted) {
                        // Trigger any daily events here (like replenishDailyFood)
                        petModel.replenishDailyFood();
                    }
                }
            }
        };
        
        gameLoop.start();
        System.out.println("✓ Game loop started!");
    }

    /// Initializes and launches the JavaFX application.
    public static void initializeApplication(String[] args) {
        launch(args);
    }
}

