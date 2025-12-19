package com.eleven.pet.ui;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

import static com.eleven.pet.ui.ViewConstants.FONT_FAMILY;

public class TutorialView extends StackPane {
    private final Runnable onFinish;
    private final List<Node> highlightTargets;
    private int stepIndex = 0;

    // Instructions corresponding to targets
    private final String[] steps = {
            "Welcome to Björni!\n\nThis is your new virtual pet companion.",
            "Check Stats!\n\nKeep an eye on Hunger, Happiness,\nEnergy, and Cleanliness here.",
            "Play Minigames!\n\nClick the TV to play games and earn happiness.\nBe careful - playing costs energy!",
            "Daily Rewards!\n\nClick the REWARDS button to claim\nyour daily rewards.",
            "Inventory!\n\nClick INVENTORY to open your Inventory\nand give your pet treats.",
            "Clean Björni!\n\nClick INVENTORY and find the soap(s) to clean your pet.\nCleaning requires energy!",
            "Sleep!\n\nWhen it's time to sleep,\nclick this button. Sleeping restores energy!"
    };

    private Label textLabel;
    private Button nextBtn;
    private Pane overlayPane;
    private VBox contentBox;

    /**
     * @param highlightTargets List of Nodes to highlight. Must match steps length (use null for no highlight).
     */
    public TutorialView(List<Node> highlightTargets, Runnable onFinish) {
        this.highlightTargets = highlightTargets;
        this.onFinish = onFinish;
        initializeUI();
    }

    private void initializeUI() {
        // Dynamic Overlay Pane (for the spotlight effect)
        overlayPane = new Pane();
        overlayPane.setPickOnBounds(false);

        // Content Box (The text dialog)
        contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxSize(700, 400);
        contentBox.setPadding(new Insets(30));
        contentBox.setStyle(
                ViewConstants.STYLE_CONTENT_BOX
        );

        textLabel = new Label();
        textLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        textLabel.setTextFill(Color.web("#8b4513"));
        textLabel.setWrapText(true);
        textLabel.setTextAlignment(TextAlignment.CENTER);
        textLabel.setMinHeight(120);

        nextBtn = new Button("NEXT");
        nextBtn.getStyleClass().addAll(ViewConstants.PIXEL_BUTTON_STYLE_CLASS, ViewConstants.PIXEL_BUTTON_PRIMARY);
        nextBtn.setPrefWidth(120);
        nextBtn.setOnAction(_ -> nextStep());

        contentBox.getChildren().addAll(textLabel, nextBtn);

        // Add layers: Overlay first (background), then Content Box
        getChildren().addAll(overlayPane, contentBox);

        updateStep();
    }

    private void nextStep() {
        stepIndex++;
        if (stepIndex >= steps.length) {
            onFinish.run();
        } else {
            updateStep();
        }
    }

    private void updateStep() {
        // Update Text
        textLabel.setText(steps[stepIndex]);
        if (stepIndex == steps.length - 1) {
            nextBtn.setText("LET'S PLAY!");
            nextBtn.getStyleClass().add(ViewConstants.PIXEL_BUTTON_GOLD);
        }

        // Draw Spotlight
        drawSpotlight(highlightTargets.size() > stepIndex ? highlightTargets.get(stepIndex) : null);
    }

    /**
     * Creates a dark overlay with a hole cut out around the target node.
     */
    private void drawSpotlight(Node target) {
        // Create base dark rectangle covering the whole screen
        Rectangle screenRect = new Rectangle(0, 0, getWidth(), getHeight());

        Shape overlayShape = screenRect;

        if (target != null && target.getScene() != null) {
            // 1. Get bounds of target in Scene coordinates (this includes the global scaling)
            Bounds targetInScene = target.localToScene(target.getBoundsInLocal());

            // 2. Transform Scene bounds back to Local coordinates of this overlay view
            // This un-does the scaling so the cutout matches the 1920x1080 design space
            Bounds targetInLocal = this.sceneToLocal(targetInScene);

            if (targetInLocal != null) {
                double x = targetInLocal.getMinX();
                double y = targetInLocal.getMinY();
                double w = targetInLocal.getWidth();
                double h = targetInLocal.getHeight();

                // Create the cutout shape (slightly larger than target)
                Rectangle cutout = new Rectangle(x - 10, y - 10, w + 20, h + 20);

                // Subtract cutout from screen
                overlayShape = Shape.subtract(screenRect, cutout);
            }
        }

        overlayShape.setFill(Color.rgb(0, 0, 0, 0.75));

        // Rebuild overlay pane
        overlayPane.getChildren().clear();
        overlayPane.getChildren().add(overlayShape);

        // Ensure content box stays on top visually (StackPane order handles this, but good to be sure)
        contentBox.toFront();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        // Redraw spotlight to match new dimensions
        if (highlightTargets != null && !highlightTargets.isEmpty()) {
            drawSpotlight(highlightTargets.size() > stepIndex ? highlightTargets.get(stepIndex) : null);
        }
    }
}