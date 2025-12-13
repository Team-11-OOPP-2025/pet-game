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

public class InventoryView extends StackPane {
    private final PetModel model;
    private final PetController controller;
    private final AssetLoader assetLoader;

    public InventoryView(PetModel model, PetController controller) {
        this.model = model;
        this.controller = controller;
        this.assetLoader = AssetLoader.getInstance();

        setupInventoryUI();
        controller.inventoryOpenProperty().addListener((_, _, isOpen) -> toggleInventory(isOpen));
    }

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
        closeBtn.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        closeBtn.setStyle(STYLE_CLOSE_BTN);
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setOnAction(_ -> controller.setInventoryOpen(false));

        inventoryPanel.getChildren().addAll(title, scroll, closeBtn);

        getChildren().addAll(backdrop, inventoryPanel);

        // Positioning: Bottom Left, aligned above the Feed Button
        setAlignment(inventoryPanel, Pos.BOTTOM_LEFT);
        setMargin(inventoryPanel, new Insets(0, 0, 150, 20));
    }

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

    private StackPane createItemSlot(Item item) {
        StackPane slot = new StackPane();
        slot.setPrefSize(70, 70);
        slot.setStyle(STYLE_ITEM_SLOT);
        slot.setCursor(Cursor.HAND);

        String lowerName = item.name().toLowerCase();
        Image itemImage = assetLoader.getImage("items/" + lowerName);

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