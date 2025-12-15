package com.eleven.pet.character.ui;

import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.character.SpriteSheetAnimation;
import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.character.behavior.PetState;
import com.eleven.pet.core.AssetLoader;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Visual representation of the pet avatar.
 * <p>
 * Renders the pet using sprite-sheet based animations and reacts to changes
 * in the {@link PetModel} (state and happiness) via bound listeners.
 */
public class PetAvatarView extends StackPane {
    private final PetModel model;
    private final PetController controller;
    private final AssetLoader assetLoader;

    private ImageView petImageView;

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

    private Image sheetNeutral, sheetSad, sheetSleeping, sheetCrying, sheetHappy;

    /**
     * Creates a new {@code PetAvatarView} bound to the given model and controller.
     *
     * @param model      pet model providing state and stats
     * @param controller controller used to derive emotion from stats
     */
    public PetAvatarView(PetModel model, PetController controller) {
        this.model = model;
        this.controller = controller;
        this.assetLoader = AssetLoader.getInstance();

        setupPetLayer();
        loadAssets();
        initializeAnimations();
        bindData();
        startRenderLoop();
        refreshPetState(model.getStatProperty(PetStats.STAT_HAPPINESS).get());
    }

    /**
     * Creates and configures the {@link ImageView} used to render the pet.
     */
    private void setupPetLayer() {
        petImageView = new ImageView();
        petImageView.setFitWidth(SHEET_WIDTH);
        petImageView.setFitHeight(SHEET_HEIGHT);
        petImageView.setPreserveRatio(true);
        petImageView.setStyle("-fx-cursor: hand;");

        setAlignment(petImageView, Pos.BOTTOM_CENTER);
        setMargin(petImageView, new javafx.geometry.Insets(0, 0, 20, 0));

        getChildren().add(petImageView);
    }

    /**
     * Loads sprite-sheet images for the different pet emotions and states.
     */
    private void loadAssets() {
        sheetHappy = assetLoader.getImage("sprites/happy/SpriteSheetHappy");
        sheetNeutral = assetLoader.getImage("sprites/idle/SpriteSheetNeutral");
        sheetSad = assetLoader.getImage("sprites/sad/SpriteSheetSad");
        sheetCrying = assetLoader.getImage("sprites/sad/SpriteSheetCrying");
        sheetSleeping = assetLoader.getImage("sprites/sleeping/SpriteSheetSleeping");
    }

    /**
     * Initializes sprite-sheet animations for the supported emotions and states.
     */
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

    /**
     * Starts the JavaFX {@link AnimationTimer} render loop that advances the
     * active animation and updates the viewport.
     */
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

    /**
     * Renders the current frame of the active animation by updating the
     * {@link ImageView}'s viewport and sprite-sheet image when needed.
     */
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

    /**
     * Resolves the correct sprite-sheet image for the given animation instance.
     *
     * @param anim animation instance
     * @return matching sprite-sheet image, never {@code null} for known animations
     */
    private Image resolveSheetForAnimation(SpriteSheetAnimation anim) {
        if (anim == animSleeping) return sheetSleeping;
        if (anim == animSad) return sheetSad;
        if (anim == animCrying) return sheetCrying;
        if (anim == animHappy) return sheetHappy;
        return sheetNeutral;
    }

    /**
     * Switches the active animation to the given one, resetting it and starting
     * playback if it is different from the current animation.
     *
     * @param newAnim animation to activate
     */
    private void changeAnimation(SpriteSheetAnimation newAnim) {
        if (activeAnimation == newAnim) return;

        if (activeAnimation != null) activeAnimation.pause();
        activeAnimation = newAnim;
        activeAnimation.reset();
        activeAnimation.play();
    }

    /**
     * Binds listeners to the model's state and happiness properties so the view
     * can react and update the displayed animation automatically.
     */
    private void bindData() {
        if (model == null) return;
        var happinessStat = model.getStatProperty(PetStats.STAT_HAPPINESS);
        // Listen for State Changes (Sleeping, Awake, etc.)
        model.getStateProperty().addListener((_, _, _) -> refreshPetState(happinessStat.get()));

        // Listen for Happiness Changes (Updates Emotion)
        if (happinessStat != null) {
            happinessStat.addListener((_, _, happiness) -> refreshPetState(happiness.intValue()));
        }
    }

    /**
     * Refreshes the pet's visual state based on the current {@link PetState}
     * and the given happiness value. Chooses the appropriate animation
     * (sleeping, happy, neutral, sad, crying).
     *
     * @param happiness current happiness value from the model
     */
    private void refreshPetState(int happiness) {
        if (model == null) return;

        PetState currentState = model.getCurrentState();

        if (currentState instanceof AsleepState) {
            changeAnimation(animSleeping);
        } else {
            switch (controller.calculateEmotion(happiness)) {
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
        }
    }
}