package com.eleven.pet.minigames;

public class MinigameResult {
    private final boolean won;
    private final int happinessDelta;
    private final String message;
    
    public MinigameResult(boolean won, int happinessDelta, String message) {
        this.won = won;
        this.happinessDelta = happinessDelta;
        this.message = message;
    }
    
    public boolean isWon() {
        return won;
    }
    
    public int getHappinessDelta() {
        return happinessDelta;
    }
    
    public String getMessage() {
        return message;
    }
}
