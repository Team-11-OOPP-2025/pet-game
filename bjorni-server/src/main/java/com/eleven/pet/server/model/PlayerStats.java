package com.eleven.pet.server.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PlayerStats {
    public String playerName;
    public long totalWins;
    public long lastTimestamp;

    Map<String, Integer> gameFrequency = new HashMap<>(); // To track "Top Played Game"

    public PlayerStats(String playerName) {
        this.playerName = playerName;
        this.totalWins = 0;
        this.lastTimestamp = System.currentTimeMillis();
    }

    /**
     * Records a win for the player in a specific game.
     *
     * @param game The name of the game in which the player won.
     */
    public void recordWin(String game) {
        this.totalWins++;
        this.gameFrequency.merge(game, 1, Integer::sum);
        this.lastTimestamp = System.currentTimeMillis();
    }

    /**
     * Determines the most frequently played game by the player.
     *
     * @return The name of the top played game, or "Unknown" if no games have been played.
     */
    public String getTopPlayedGame() {
        return gameFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }
}