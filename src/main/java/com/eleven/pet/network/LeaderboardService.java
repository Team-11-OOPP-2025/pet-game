package com.eleven.pet.network;

import com.eleven.pet.minigames.MinigameResult;

public interface LeaderboardService {
    /**
     * Submits a player's score to the global leaderboard.
     *
     * @param playerName the name of the player
     * @param result     the result of the minigame
     */
    void submitScore(String playerName, MinigameResult result);

    LeaderboardResult getTopScores(int limit);
}
