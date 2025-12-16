package com.eleven.pet.network;

import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.shared.LeaderboardEntry;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LeaderboardClient implements LeaderboardService {
    private static final String API_URL = "http://localhost:8000/api/v1/leaderboard/";

    private final HttpClient httpClient;
    private final ObjectMapper jsonMapper;

    public LeaderboardClient() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.jsonMapper = new ObjectMapper();
    }

    /**
     * @param playerName the name of the player
     * @param result     the result of the minigame
     */
    @Override
    public void submitScore(String playerName, MinigameResult result) {
        // Leaderboard only cares about wins
        if (!result.won()) {
            return;
        }

        try {
            // TODO: Add unique identifier to the client
            LeaderboardEntry entry = new LeaderboardEntry(
                    playerName,
                    true,
                    // result.gameName() // TODO: Update MiniGame Record to include gamename.
                    "a game",
                    System.currentTimeMillis()
            );

            String jsonBody = jsonMapper.writeValueAsString(entry);

            // Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
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
        } catch (Exception e) {
            // Replace with Log4j... hopefully no vuln this time xD
            e.printStackTrace();
        }
    }

    @Override
    public LeaderboardEntry getTopScores(int limit) {
        // if this method is empty then hugo dorrich has failed us
        // may god save the queen
        // from us...
        // thanks
        return null;
    }
}
