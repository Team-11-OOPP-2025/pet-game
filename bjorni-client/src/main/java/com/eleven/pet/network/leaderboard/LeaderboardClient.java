package com.eleven.pet.network.leaderboard;

import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.shared.LeaderboardEntry;
import com.eleven.pet.shared.Signature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LeaderboardClient implements LeaderboardService {
    private static final String API_URL = "http://localhost:8080/api/v1/leaderboard";

    private final HttpClient httpClient;
    private final ObjectMapper jsonMapper;
    private final Signature signatureGenerator;

    public LeaderboardClient() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.jsonMapper = new ObjectMapper();
        this.signatureGenerator = new Signature();
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
                    result.gameName(),
                    System.currentTimeMillis()
            );

            String jsonBody = jsonMapper.writeValueAsString(entry);

            // TODO: Change to inject shared secret on build or default to hardcoded during developemnt env.
            String signature = signatureGenerator.calculateHMAC(jsonBody, "SHARED_KEY");

            // Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("X-HMAC-Signature", signature)
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

    /**
     * Fetches the top scores asynchronously.
     *
     * @return A Future containing the list of scores.
     */
    @Override
    public CompletableFuture<List<LeaderboardEntry>> getTopScores(int limit) {
        return null;
    }
}
