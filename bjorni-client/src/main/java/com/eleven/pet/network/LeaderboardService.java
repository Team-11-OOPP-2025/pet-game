package com.eleven.pet.network;

import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.shared.LeaderboardEntry;

public interface LeaderboardService {
    /**
     * Submits a player's score to the global leaderboard.
     *
     * @param playerName the name of the player
     * @param result     the result of the minigame
     */
    void submitScore(String playerName, MinigameResult result);

    LeaderboardEntry getTopScores(int limit);
}
