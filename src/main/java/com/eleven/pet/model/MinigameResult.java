package com.eleven.pet.model;

public class MinigameResult {
    private final boolean won;
    private final int happinessDelta;
    private final String message;
    
    public MinigameResult(boolean won, int delta, String msg) {
        this.won = won;
        this.happinessDelta = delta;
        this.message = msg;
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
