package com.eleven.pet.network;

import com.eleven.pet.minigames.MinigameResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LeaderboardClient implements LeaderboardService {
    private static final String API_URL = "https://example.com/api/v1/leaderboard/";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * @param playerName the name of the player
     * @param result     the result of the minigame
     */
    @Override
    public void submitScore(String playerName, MinigameResult result) {
        if (playerName == null || playerName.isEmpty() || result == null) {
            throw new IllegalArgumentException("Player name and result must be provided");
        }
        // Leaderboard only cares about wins
        if (!result.won()) {
            return;
        }

        // Build the HTTP request
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(String.format(
                        "{\"playerName\":\"%s\",\"identifier\":nothing_yet}",
                        playerName
                )))
                .build();

        // Asynchronously send the request to the server (non-blocking)
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(statusCode -> {
                    if (statusCode != 200) {
                        System.err.println("Failed to submit score: HTTP " + statusCode);
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Error submitting score: " + e.getMessage());
                    return null;
                });
    }
}
