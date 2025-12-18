package com.eleven.pet.shared.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing a single entry in the leaderboard.
 * <p>
 * This class encapsulates the details of a player's achievement in a minigame,
 * including their name, score, the game played, and the timestamp of the achievement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {
    /**
     * The display name of the player who achieved the result.
     */
    public String playerName;
    /**
     * The score achieved by the player. 1 or 0
     */
    public long score;
    /**
     * The identifier of the specific minigame played (e.g., "TimingGame", "GuessingGame").
     */
    public String gameName;
    /**
     * The timestamp of the achievement in milliseconds since the Unix Epoch.
     */
    public long timestamp;
}