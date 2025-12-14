package com.eleven.pet.character.ui;

import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.character.SpriteSheetAnimation;
import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.character.behavior.PetState;
import com.eleven.pet.core.AssetLoader;
import com.eleven.pet.vfx.ParticleSystem;
import com.eleven.pet.vfx.effects.DustParticleEffect;
import com.eleven.pet.vfx.effects.NoParticleEffect;
import com.eleven.pet.vfx.effects.ParticleEffect;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class PetAvatarView extends StackPane {
    private final PetModel model;
    private final PetController controller;
    private final AssetLoader assetLoader;

    private ImageView petImageView;
    private ParticleSystem dustParticleSystem;
    private ParticleEffect currentDustEffect;

    private static final int SHEET_WIDTH = 309;
    private static final int SHEET_HEIGHT = 460;
    private static final int GRID_COLS = 2;
    
    // Dust particle thresholds
    private static final int DIRTY_THRESHOLD = 50; // Below 50% cleanliness
    private static final int VERY_DIRTY_THRESHOLD = 25; // Below 25% cleanliness
    private static final int LIGHT_DUST_PARTICLES = 40;
    private static final int HEAVY_DUST_PARTICLES = 75;

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

    public PetAvatarView(PetModel model, PetController controller) {
        this.model = model;
        this.controller = controller;
        this.assetLoader = AssetLoader.getInstance();

        setupPetLayer();
        setupDustParticles();
        loadAssets();
        initializeAnimations();
        bindData();
        startRenderLoop();
        refreshPetState(model.getStatProperty(PetStats.STAT_HAPPINESS).get());
        updateDustEffect(model.getStatProperty(PetStats.STAT_CLEANLINESS).get());
    }

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
    
    private void setupDustParticles() {
        // Create a particle system for dust effects
        dustParticleSystem = new ParticleSystem(SHEET_WIDTH, SHEET_HEIGHT);
        var dustCanvas = dustParticleSystem.getCanvas();
        dustCanvas.setMouseTransparent(true);
        
        // Match the canvas size to the pet image
        dustCanvas.widthProperty().bind(petImageView.fitWidthProperty());
        dustCanvas.heightProperty().bind(petImageView.fitHeightProperty());
        
        // Add canvas directly to StackPane with same alignment and margin as pet image
        setAlignment(dustCanvas, Pos.BOTTOM_CENTER);
        setMargin(dustCanvas, new javafx.geometry.Insets(0, 0, 20, 0));
        
        getChildren().add(dustCanvas);
    }

    private void loadAssets() {
        sheetHappy = assetLoader.getImage("sprites/happy/SpriteSheetHappy");
        sheetNeutral = assetLoader.getImage("sprites/idle/SpriteSheetNeutral");
        sheetSad = assetLoader.getImage("sprites/sad/SpriteSheetSad");
        sheetCrying = assetLoader.getImage("sprites/sad/SpriteSheetCrying");
        sheetSleeping = assetLoader.getImage("sprites/sleeping/SpriteSheetSleeping");
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
        var happinessStat = model.getStatProperty(PetStats.STAT_HAPPINESS);
        var cleanlinessStat = model.getStatProperty(PetStats.STAT_CLEANLINESS);
        
        // Listen for State Changes (Sleeping, Awake, etc.)
        model.getStateProperty().addListener((_, _, _) -> refreshPetState(happinessStat.get()));

        // Listen for Happiness Changes (Updates Emotion)
        if (happinessStat != null) {
            happinessStat.addListener((_, _, happiness) -> refreshPetState(happiness.intValue()));
        }
        
        // Listen for Cleanliness Changes (Updates Dust Particles)
        if (cleanlinessStat != null) {
            cleanlinessStat.addListener((_, _, cleanliness) -> updateDustEffect(cleanliness.intValue()));
        }
    }

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
    
    private void updateDustEffect(int cleanliness) {
        ParticleEffect newEffect;
        
        if (cleanliness < VERY_DIRTY_THRESHOLD) {
            // Very dirty (below 25%) - lots of dust
            newEffect = new DustParticleEffect(HEAVY_DUST_PARTICLES);
        } else if (cleanliness < DIRTY_THRESHOLD) {
            // Dirty (below 50%) - some dust
            newEffect = new DustParticleEffect(LIGHT_DUST_PARTICLES);
        } else {
            // Clean - no dust
            newEffect = new NoParticleEffect();
        }
        
        // Only update if the effect changed
        if (currentDustEffect == null || !currentDustEffect.equals(newEffect)) {
            if (currentDustEffect != null) {
                currentDustEffect.stop(dustParticleSystem);
            }
            currentDustEffect = newEffect;
            currentDustEffect.start(dustParticleSystem);
        }
    }
}