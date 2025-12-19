package com.eleven.pet.core;

/**
 * Unchecked exception type for game-specific error conditions.
 *
 * <p>Use this to wrap errors that are meaningful at the game domain level
 * (configuration issues, data loading failures, etc.).
 * </p>
 */
public class GameException extends RuntimeException {

    /**
     * Creates a new {@code GameException} with the given detail message.
     *
     * @param message human-readable description of the error
     */
    public GameException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code GameException} with the given detail message
     * and underlying cause.
     *
     * @param message human-readable description of the error
     * @param cause   the underlying cause, may be {@code null}
     */
    public GameException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the detail message string of this throwable.
     *
     * <p>Currently delegates directly to {@link RuntimeException#getMessage()}.
     * </p>
     *
     * @return the detail message string
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
