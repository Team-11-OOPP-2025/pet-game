package com.eleven.pet.character;

import lombok.Data;

/**
 * Class representing a sprite sheet animation.
 *
 * <p>The animation advances frames based on a fixed {@code frameDuration}.
 * It can be started, paused, reset and configured to loop or stop at
 * the last frame.</p>
 */
@Data
public class SpriteSheetAnimation {
    /** Width of a single frame in pixels. */
    private final int frameWidth;
    /** Height of a single frame in pixels. */
    private final int frameHeight;
    /** Number of columns in the sprite sheet. */
    private final int columns;
    /** Total number of frames available in the animation. */
    private final int totalFrames;
    /** Duration in seconds that each frame should be displayed. */
    private final float frameDuration;

    /** Index of the frame currently being displayed (0-based). */
    private int currentFrame;
    /** Accumulated time since the last frame change. */
    private float elapsedTime;
    /** Indicates whether the animation is currently playing. */
    private boolean isPlaying;
    /** If {@code true}, the animation restarts after the last frame. */
    private boolean loop;

    /**
     * Constructor for SpriteSheetAnimation.
     *
     * @param frameWidth    width of each frame in the sprite sheet, in pixels
     * @param frameHeight   height of each frame in the sprite sheet, in pixels
     * @param columns       number of columns in the sprite sheet
     * @param totalFrames   total number of frames in the animation
     * @param frameDuration duration of each frame in seconds
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
     * Update the animation based on the elapsed time.
     *
     * <p>If the animation is not playing, this method does nothing.
     * When the accumulated time exceeds {@code frameDuration}, the
     * {@code currentFrame} advances and wraps or stops depending on
     * the {@code loop} flag.</p>
     *
     * @param deltaTime time in seconds since the last update call
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
     * Get the X coordinate of the current frame in the sprite sheet.
     *
     * @return the X coordinate (in pixels) of the current frame
     */
    public int getFrameX() {
        return (currentFrame % columns) * frameWidth;
    }

    /**
     * Get the Y coordinate of the current frame in the sprite sheet.
     *
     * @return the Y coordinate (in pixels) of the current frame
     */
    public int getFrameY() {
        return (currentFrame / columns) * frameHeight;
    }

    /**
     * Start or resume the animation.
     *
     * <p>If the animation was paused, it continues from the current frame;
     * if it was already playing, this call has no effect.</p>
     */
    public void play() {
        isPlaying = true;
    }

    /**
     * Pause the animation without resetting the current frame.
     */
    public void pause() {
        isPlaying = false;
    }

    /**
     * Reset the animation to the first frame and clear elapsed time.
     *
     * <p>The animation is not automatically started; call {@link #play()}
     * to begin playback after resetting.</p>
     */
    public void reset() {
        currentFrame = 0;
        elapsedTime = 0;
    }
}
