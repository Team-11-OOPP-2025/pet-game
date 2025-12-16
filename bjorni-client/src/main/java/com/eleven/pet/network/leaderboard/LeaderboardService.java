package com.eleven.pet.network.leaderboard;

import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.shared.LeaderboardEntry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LeaderboardService {
    /**
     * Submits a player's score to the global leaderboard.
     *
     * @param playerName the name of the player
     * @param result     the result of the minigame
     */
    void submitScore(String playerName, MinigameResult result);

    /**
     * Retrieves the top scores from the global leaderboard.
     *
     * @param limit the maximum number of top scores to retrieve
     * @return a CompletableFuture that resolves to a list of LeaderboardEntry objects
     */
    CompletableFuture<List<LeaderboardEntry>> getTopScores(int limit);
}
