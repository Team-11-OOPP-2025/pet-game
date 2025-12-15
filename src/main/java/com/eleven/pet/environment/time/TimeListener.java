package com.eleven.pet.environment.time;

/**
 * Listener for game time progression events.
 * <p>
 * Implementations are notified on each game clock tick and can update
 * their internal state based on the elapsed (scaled) game time.
 */
public interface TimeListener {

    /**
     * Called when the {@link GameClock} advances.
     *
     * @param timeDelta the elapsed game time since the last tick, after applying
     *                  the current time scale (in seconds of in‑game time)
     */
    void onTick(double timeDelta);
}
