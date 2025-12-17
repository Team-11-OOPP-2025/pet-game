package com.eleven.pet.shared;

/**
 * Immutable data carrier representing a single score submission or leaderboard row.
 * <p>
 * This record serves as the primary DTO (Data Transfer Object) for the leaderboard API.
 * It is serialized to JSON for network transmission in both:
 * <ul>
 * <li>Score submissions (Client &rarr; Server)</li>
 * <li>Leaderboard retrieval (Server &rarr; Client)</li>
 * </ul>
 *
 * @param playerName The display name of the player who achieved the result.
 * @param won        Indicates if the game session resulted in a win.
 * (Note: Typically, only winning scores are submitted to the leaderboard).
 * @param gameName   The identifier of the specific minigame played (e.g., "FoodCatch", "PetPet").
 * @param timeStamp  The timestamp of the achievement in milliseconds since the Unix Epoch.
 */
public record LeaderboardEntry(String playerName, boolean won, String gameName, long timeStamp) {
}