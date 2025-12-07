package com.eleven.pet.character;

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
    
    public int getFrameX() {
        return (currentFrame % columns) * frameWidth;
    }
    
    public int getFrameY() {
        return (currentFrame / columns) * frameHeight;
    }
    
    public void play() {
        isPlaying = true;
    }
    
    public void pause() {
        isPlaying = false;
    }
    
    public void reset() {
        currentFrame = 0;
        elapsedTime = 0;
    }
    
    public void setLoop(boolean loop) {
        this.loop = loop;
    }
    
    public int getCurrentFrame() {
        return currentFrame;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public int getFrameWidth() {
        return frameWidth;
    }
    
    public int getFrameHeight() {
        return frameHeight;
    }
}
