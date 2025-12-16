package com.eleven.pet.character;

/**
 * Represents the emotional animation states a pet can have.
 *
 * <p>Used to drive UI animations and select appropriate visual or sound feedback
 * based on the pet's current mood.</p>
 */
public enum AnimationState {
    /**
     * Pet is very happy — highest positive mood.
     */
    VERY_HAPPY,

    /**
     * Pet is in a neutral mood.
     */
    NEUTRAL,

    /**
     * Pet is sad — negative mood.
     */
    SAD,

    /**
     * Pet is very sad — lowest mood state.
     */
    VERY_SAD
}