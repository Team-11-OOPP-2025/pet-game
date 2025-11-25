package com.eleven.pet.util;

public class GameException extends Exception {
    private final String message;
    
    public GameException(String message) {
        super(message);
        this.message = message;
    }
    
    public GameException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
