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
            "Play Minigames!\n\nClick the TV to play games\nand earn happiness points.",
            "Inventory!\n\nClick FEED to open your Inventory\nand give your pet treats.",
            "Sleep!\n\nWhen Energy is low at night,\nthis button lets Björni sleep."
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
        contentBox.setMaxSize(400, 300);
        contentBox.setPadding(new Insets(30));
        contentBox.setStyle(
                ViewConstants.STYLE_CONTENT_BOX
        );

        textLabel = new Label();
        textLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
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
            nextBtn.getStyleClass().add(ViewConstants.PIXEL_BUTTON_NEXT);
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

        if (target != null) {
            // Calculate bounds of target relative to this view
            Bounds bounds = target.localToScene(target.getBoundsInLocal());
            // Since TutorialView is root (or fills root), scene coords are roughly local coords.
            // However, to be precise if TutorialView isn't 0,0:
            Bounds myBounds = this.localToScene(this.getBoundsInLocal());

            double x = bounds.getMinX() - myBounds.getMinX();
            double y = bounds.getMinY() - myBounds.getMinY();

            // Create the cutout shape (slightly larger than target)
            Rectangle cutout = new Rectangle(x - 10, y - 10, bounds.getWidth() + 20, bounds.getHeight() + 20);

            // Subtract cutout from screen
            overlayShape = Shape.subtract(screenRect, cutout);
        }

        overlayShape.setFill(Color.rgb(0, 0, 0, 0.75));

        // Rebuild overlay pane
        overlayPane.getChildren().clear();
        overlayPane.getChildren().add(overlayShape);

        // Ensure content box stays on top visually (StackPane order handles this, but good to be sure)
        contentBox.toFront();
    }

    // Ensure the spotlight redraws if window resizes
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        // Redraw spotlight to match new dimensions
        if (highlightTargets != null && !highlightTargets.isEmpty()) {
            drawSpotlight(highlightTargets.size() > stepIndex ? highlightTargets.get(stepIndex) : null);
        }
    }
}