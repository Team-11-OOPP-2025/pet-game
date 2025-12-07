package com.eleven.pet.character;

import lombok.Data;

/**
 * Class representing a sprite sheet animation
 */
@Data
public class SpriteSheetAnimation {
    private final int frameWidth;
    private final int frameHeight;
    private final int columns;
    private final int totalFrames;
    private final float frameDuration;

    private int currentFrame;
    private float elapsedTime;
    private boolean isPlaying;
    private boolean loop;

    /**
     * Constructor for SpriteSheetAnimation
     *
     * @param frameWidth    Width of each frame in the sprite sheet
     * @param frameHeight   Height of each frame in the sprite sheet
     * @param columns       Number of columns in the sprite sheet
     * @param totalFrames   Total number of frames in the animation
     * @param frameDuration Duration of each frame in seconds
     */
    public SpriteSheetAnimation(int frameWidth, int frameHeight, int columns, int totalFrames, float frameDuration) {
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.columns = columns;
        this.totalFrames = totalFrames;
        this.frameDuration = frameDuration;
        this.currentFrame = 0;
        this.elapsedTime = 0;
        this.isPlaying = false;
        this.loop = true;
    }

    /**
     * Update the animation based on the elapsed time
     */
    public void update(float deltaTime) {
        if (!isPlaying) return;

        elapsedTime += deltaTime;

        if (elapsedTime >= frameDuration) {
            elapsedTime -= frameDuration;
            currentFrame++;

            if (currentFrame >= totalFrames) {
                if (loop) {
                    currentFrame = 0;
                } else {
                    currentFrame = totalFrames - 1;
                    isPlaying = false;
                }
            }
        }
    }

    /**
     * Get the X coordinate of the current frame in the sprite sheet
     */
    public int getFrameX() {
        return (currentFrame % columns) * frameWidth;
    }

    /**
     * Get the Y coordinate of the current frame in the sprite sheet
     */
    public int getFrameY() {
        return (currentFrame / columns) * frameHeight;
    }

    /**
     * Start or resume the animation
     */
    public void play() {
        isPlaying = true;
    }

    /**
     * Pause the animation
     */
    public void pause() {
        isPlaying = false;
    }

    /**
     * Reset the animation to the first frame
     */
    public void reset() {
        currentFrame = 0;
        elapsedTime = 0;
    }
}
