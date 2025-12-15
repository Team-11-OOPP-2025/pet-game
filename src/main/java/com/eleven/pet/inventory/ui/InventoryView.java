package com.eleven.pet.inventory.ui;

import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.core.AssetLoader;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.inventory.ItemRegistry;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import static com.eleven.pet.ui.ViewConstants.*;

/**
 * JavaFX view responsible for rendering and animating the inventory overlay.
 * <p>
 * It observes {@link PetModel}'s inventory and displays each item as an
 * interactive slot that can be clicked to trigger consume actions on the
 * {@link PetController}. The view listens to the controller's
 * {@code inventoryOpenProperty} to show and hide itself with a fade animation.
 */
public class InventoryView extends StackPane {
    private final PetModel model;
    private final PetController controller;
    private final AssetLoader assetLoader;

    /**
     * Creates a new {@code InventoryView} bound to the given model and controller.
     *
     * @param model      the pet model that exposes the observable inventory
     * @param controller the controller that handles inventory open/close and item consumption
     */
    public InventoryView(PetModel model, PetController controller) {
        this.model = model;
        this.controller = controller;
        this.assetLoader = AssetLoader.getInstance();

        setupInventoryUI();
        controller.inventoryOpenProperty().addListener((_, _, isOpen) -> toggleInventory(isOpen));
    }

    /**
     * Builds the inventory overlay UI hierarchy and configures layout,
     * styling, and basic interactions.
     */
    private void setupInventoryUI() {
        setVisible(false);

        // Backdrop
        Region backdrop = new Region();
        backdrop.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        backdrop.setOnMouseClicked(_ -> controller.setInventoryOpen(false));

        // Main Panel
        VBox inventoryPanel = new VBox(10);
        inventoryPanel.setMaxSize(350, 250);
        inventoryPanel.setStyle(STYLE_INVENTORY_PANEL);
        inventoryPanel.setAlignment(Pos.TOP_CENTER);

        // Title
        Label title = new Label("INVENTORY");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#8b4513"));

        // Items Grid
        TilePane itemGrid = new TilePane();
        itemGrid.setHgap(10);
        itemGrid.setVgap(10);
        itemGrid.setPrefColumns(3);
        itemGrid.setAlignment(Pos.CENTER);

        populateInitialItems(itemGrid);
        listenForInventoryChanges(itemGrid);

        ScrollPane scroll = new ScrollPane(itemGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Close Button
        Button closeBtn = new Button("CLOSE");
        // Apply CSS classes
        closeBtn.getStyleClass().addAll("pixel-btn", "pixel-btn-danger");
        // closeBtn.setStyle(STYLE_CLOSE_BTN); // Removed old style
        
        closeBtn.setOnAction(_ -> controller.setInventoryOpen(false));

        inventoryPanel.getChildren().addAll(title, scroll, closeBtn);

        getChildren().addAll(backdrop, inventoryPanel);

        // Positioning: Bottom Left, aligned above the Feed Button
        setAlignment(inventoryPanel, Pos.BOTTOM_LEFT);
        setMargin(inventoryPanel, new Insets(0, 0, 150, 20));
    }

    /**
     * Populates the initial grid of item slots based on the current inventory
     * contents, only adding items that have a quantity greater than zero.
     *
     * @param itemGrid the {@link TilePane} that will hold the item slots
     */
    private void populateInitialItems(TilePane itemGrid) {
        model.getInventory().getItems().forEach((id, qtyProp) -> {
            if (qtyProp.get() > 0) {
                Item item = ItemRegistry.get(id);
                if (item != null) {
                    StackPane slot = createItemSlot(item);
                    slot.setUserData(id);
                    itemGrid.getChildren().add(slot);
                }
            }
        });
    }

    /**
     * Registers listeners on the inventory map so that new item slots are
     * created whenever a new item entry appears in the inventory.
     *
     * @param itemGrid the {@link TilePane} that will receive newly created slots
     */
    private void listenForInventoryChanges(TilePane itemGrid) {
        model.getInventory().getItems().addListener((MapChangeListener<Integer, IntegerProperty>) change -> {
            if (change.wasAdded()) {
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
    }

    /**
     * Creates a single item slot node for the given {@link Item}, including
     * image, name, quantity badge, tooltip, and click handler to consume it.
     * The slot automatically removes itself when the bound quantity reaches zero.
     *
     * @param item the inventory item to represent
     * @return a {@link StackPane} representing the visual slot for the item
     */
    private StackPane createItemSlot(Item item) {
        StackPane slot = new StackPane();
        slot.setPrefSize(70, 70);
        slot.setStyle(STYLE_ITEM_SLOT);
        slot.setCursor(Cursor.HAND);

        Image itemImage = assetLoader.getImage("items/" + item.imageFileName());

        ImageView iv = new ImageView(itemImage);
        iv.setFitWidth(70);
        iv.setFitHeight(70);
        iv.setPreserveRatio(true);

        StackPane.setAlignment(iv, Pos.CENTER);
        StackPane.setMargin(iv, new Insets(-5, 0, 0, 0));

        Label name = new Label(item.name());
        name.setTextFill(Color.BLACK);
        name.setFont(Font.font(FONT_FAMILY, 9));
        StackPane.setAlignment(name, Pos.BOTTOM_CENTER);
        StackPane.setMargin(name, new Insets(0, 0, 5, 0));

        Label qty = new Label();
        qty.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 10));
        qty.setTextFill(Color.WHITE);
        qty.setStyle("-fx-background-color: red; -fx-background-radius: 10; -fx-padding: 1 5;");
        StackPane.setAlignment(qty, Pos.TOP_RIGHT);
        StackPane.setMargin(qty, new Insets(-5, -5, 0, 0));

        IntegerProperty amountProp = model.getInventory().amountProperty(item);
        qty.textProperty().bind(Bindings.convert(amountProp));

        // Auto-remove slot when quantity hits 0
        amountProp.addListener((_, _, newVal) -> {
            if (newVal.intValue() <= 0) {
                if (slot.getParent() instanceof Pane) {
                    ((Pane) slot.getParent()).getChildren().remove(slot);
                }
            }
        });

        Tooltip tooltip = new Tooltip();
        tooltip.setText("Name: " + item.name() + "\nHeal: " + item.statsRestore() + "\nDescription: " + item.description());
        tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: rgba(50, 50, 50, 0.9); -fx-text-fill: white;");
        Tooltip.install(slot, tooltip);

        slot.setOnMouseClicked(_ -> {
            controller.handleConsumeAction(item);
            animateClick(slot);
        });

        slot.getChildren().addAll(iv, name, qty);
        return slot;
    }

    /**
     * Plays a short scale animation to visually acknowledge a click on the slot.
     *
     * @param slot the slot node that should be animated
     */
    private void animateClick(StackPane slot) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), slot);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(0.9);
        st.setToY(0.9);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    /**
     * Shows or hides the inventory overlay with a fade animation.
     *
     * @param show {@code true} to fade the inventory in, {@code false} to fade it out
     */
    public void toggleInventory(boolean show) {
        if (show) {
            setVisible(true);
            setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(200), this);
            ft.setToValue(1.0);
            ft.play();
        } else {
            FadeTransition ft = new FadeTransition(Duration.millis(200), this);
            ft.setToValue(0);
            ft.setOnFinished(_ -> setVisible(false));
            ft.play();
        }
    }
}