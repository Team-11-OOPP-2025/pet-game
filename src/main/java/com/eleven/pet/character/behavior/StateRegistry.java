package com.eleven.pet.character.behavior;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Registry for {@link PetState} implementations.
 *
 * <p>This class is a lazy-initialized singleton that discovers available
 * {@code PetState} implementations via the Java Service Provider Interface (SPI)
 * on construction. It also provides programmatic registration and lookup by
 * state name.</p>
 */
public class StateRegistry {
    /**
     * Singleton instance of the registry.
     */
    private static StateRegistry instance;

    /**
     * Map of state name to {@link PetState} instance.
     */
    private final Map<String, PetState> stateMap;

    /**
     * Constructs the registry and attempts to load states using SPI.
     *
     * <p>This constructor is private to enforce the singleton pattern.</p>
     */
    private StateRegistry() {
        this.stateMap = new HashMap<>();
        loadStatesViaSPI();
    }

    /**
     * Returns the singleton instance of the {@code StateRegistry}.
     *
     * @return the shared {@code StateRegistry} instance
     */
    public static StateRegistry getInstance() {
        if (instance == null) {
            instance = new StateRegistry();
        }
        return instance;
    }

    /**
     * Retrieves a registered {@link PetState} by its name.
     *
     * @param name the name of the state to retrieve
     * @return the {@code PetState} registered under {@code name}, or {@code null} if none
     */
    public PetState getState(String name) {
        return stateMap.get(name);
    }

    /**
     * Registers a {@link PetState} instance in the registry.
     *
     * <p>If a state with the same name already exists it will be replaced.</p>
     *
     * @param state the state instance to register; must not be {@code null}
     */
    public void registerState(PetState state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        stateMap.put(state.getStateName(), state);
    }

    /**
     * Loads available {@link PetState} implementations using the ServiceLoader SPI.
     *
     * <p>If no implementations are discovered, a fallback manual registration is
     * performed for known built-in states.</p>
     */
    private void loadStatesViaSPI() {
        ServiceLoader<PetState> loader = ServiceLoader.load(PetState.class);
        for (PetState state : loader) {
            stateMap.put(state.getStateName(), state);
            System.out.println("Loaded state: " + state.getStateName());
        }

        // Fallback: if no states were loaded via SPI, manually register them
        if (stateMap.isEmpty()) {
            System.out.println("No states loaded via SPI, registering manually...");
            registerState(new AwakeState());
            registerState(new AsleepState());
        }
    }
}