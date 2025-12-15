package com.eleven.pet.core;

/**
 * Plain Java entry point used to launch the JavaFX application.
 *
 * <p>This indirection avoids JavaFX launcher limitations and allows
 * tools to run the game from a standard {@code public static void main}.
 * </p>
 */
public class Launcher {

    /**
     * JVM entry point.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        MainApp.initializeApplication(args);
    }
}
