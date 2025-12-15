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
 * Visual representation of the pet character.
 * <p>
 * This view is responsible for:
 * <ul>
 *     <li>Loading and managing pet sprite sheets</li>
 *     <li>Running sprite-sheet based animations</li>
 *     <li>Reacting to {@link PetModel} state and stat changes (e.g. happiness, sleep)</li>
 * </ul>
 * </p>
 */
public class PetAvatarView extends StackPane {

    /**
     * Backing model providing current stats and state of the pet.
     */
    private final PetModel model;

    /**
     * Controller used to derive higher-level emotional state from raw stats.
     */
    private final PetController controller;

    /**
     * Global asset loader for obtaining sprite sheet images.
     */
    private final AssetLoader assetLoader;

    /**
     * Image node used to render the current frame of the active animation.
     */
    private ImageView petImageView;

    /**
     * Width in pixels of each sprite frame in the sprite sheets.
     */
    private static final int SHEET_WIDTH = 309;

    /**
     * Height in pixels of each sprite frame in the sprite sheets.
     */
    private static final int SHEET_HEIGHT = 460;

    /**
     * Number of columns of frames in each sprite sheet.
     */
    private static final int GRID_COLS = 2;

    // Animations
    /**
     * Default/idle (neutral) animation.
     */
    private SpriteSheetAnimation animNeutral;

    /**
     * Animation used when the pet is very happy.
     */
    private SpriteSheetAnimation animHappy;

    /**
     * Animation used when the pet is sad.
     */
    private SpriteSheetAnimation animSad;

    /**
     * Animation used when the pet is very sad (crying).
     */
    private SpriteSheetAnimation animCrying;

    /**
     * Animation used while the pet is asleep.
     */
    private SpriteSheetAnimation animSleeping;

    /**
     * Currently active animation being rendered.
     */
    private SpriteSheetAnimation activeAnimation;

    /**
     * Currently active sprite sheet image backing {@link #activeAnimation}.
     */
    private Image activeSpriteSheet;

    /**
     * JavaFX timer driving the animation update/render loop.
     */
    private AnimationTimer renderLoop;

    /**
     * Timestamp of the last rendered frame in nanoseconds, used to compute delta time.
     */
    private long lastFrameTime;

    /**
     * Loaded sprite sheet used for neutral emotion.
     */
    private Image sheetNeutral;

    /**
     * Loaded sprite sheet used for sad emotion.
     */
    private Image sheetSad;

    /**
     * Loaded sprite sheet used for sleeping state.
     */
    private Image sheetSleeping;

    /**
     * Loaded sprite sheet used for crying emotion.
     */
    private Image sheetCrying;

    /**
     * Loaded sprite sheet used for happy emotion.
     */
    private Image sheetHappy;

    /**
     * Creates a new {@code PetAvatarView} bound to the given model and controller.
     * <p>
     * The constructor:
     * <ul>
     *     <li>Sets up the image layer</li>
     *     <li>Loads all sprite assets</li>
     *     <li>Initializes animations and the render loop</li>
     *     <li>Binds to model properties to react to state/stat changes</li>
     * </ul>
     * </p>
     *
     * @param model      pet data model providing stats and state; may not be {@code null}
     * @param controller controller used to calculate emotions from stats; may not be {@code null}
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
     * Initializes and configures the {@link ImageView} used to display
     * the pet sprite, and adds it to this {@link StackPane}.
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
     * Loads all required sprite sheet images via the {@link AssetLoader}.
     * <p>
     * Images are cached in dedicated fields and reused by animations.
     */
    private void loadAssets() {
        sheetHappy = assetLoader.getImage("sprites/happy/SpriteSheetHappy");
        sheetNeutral = assetLoader.getImage("sprites/idle/SpriteSheetNeutral");
        sheetSad = assetLoader.getImage("sprites/sad/SpriteSheetSad");
        sheetCrying = assetLoader.getImage("sprites/sad/SpriteSheetCrying");
        sheetSleeping = assetLoader.getImage("sprites/sleeping/SpriteSheetSleeping");
    }

    /**
     * Creates and configures all {@link SpriteSheetAnimation} instances
     * for the different emotional and sleep states.
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
     * Starts the main render loop using a {@link AnimationTimer}.
     * <p>
     * The loop:
     * <ul>
     *     <li>Computes frame delta time</li>
     *     <li>Updates the active animation</li>
     *     <li>Renders the next frame to the {@link ImageView}</li>
     * </ul>
     * </p>
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
     * Renders the current frame of the {@link #activeAnimation} to the
     * {@link #petImageView}, updating both the viewport and backing image
     * if necessary.
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
     * Resolves which sprite sheet image should be used for the given animation.
     *
     * @param anim the animation whose backing sprite sheet should be determined
     * @return the corresponding {@link Image}, or {@code null} if none is found
     */
    private Image resolveSheetForAnimation(SpriteSheetAnimation anim) {
        if (anim == animSleeping) return sheetSleeping;
        if (anim == animSad) return sheetSad;
        if (anim == animCrying) return sheetCrying;
        if (anim == animHappy) return sheetHappy;
        return sheetNeutral;
    }

    /**
     * Switches the currently active animation to the specified one.
     * <p>
     * If the new animation is already active, nothing happens. Otherwise, the
     * previous animation is paused and the new one is reset and played.
     * </p>
     *
     * @param newAnim the animation to become active; may be {@code null}
     */
    private void changeAnimation(SpriteSheetAnimation newAnim) {
        if (activeAnimation == newAnim) return;

        if (activeAnimation != null) activeAnimation.pause();
        activeAnimation = newAnim;
        activeAnimation.reset();
        activeAnimation.play();
    }

    /**
     * Binds listeners to the {@link PetModel}'s properties so that changes
     * in state (e.g. asleep/awake) and happiness automatically update the
     * visual representation.
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