package com.eleven.pet.network.leaderboard;

import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.shared.LeaderboardEntry;
import com.eleven.pet.shared.PlayerRegistration;
import com.eleven.pet.shared.Signature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LeaderboardClient implements LeaderboardService {
    private static final String BASE_URL = "http://localhost:8080/api/v1";
    private static final String API_URL = BASE_URL + "/leaderboard";
    private static final String AUTH_URL = BASE_URL + "/auth/register";

    private final HttpClient httpClient;
    private final ObjectMapper jsonMapper;
    private final Signature signatureGenerator;

    private String playerId;
    private String secretKey;

    public LeaderboardClient() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.jsonMapper = new ObjectMapper();
        this.signatureGenerator = new Signature();
        registerPlayer().thenAccept(registration -> {
            this.playerId = registration.getPlayerId();
            this.secretKey = registration.getSecretKey();
        }).join();
    }

    public CompletableFuture<PlayerRegistration> registerPlayer() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(AUTH_URL)).POST(HttpRequest.BodyPublishers.noBody()).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    try {
                        return jsonMapper.readValue(body, PlayerRegistration.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse registration response", e);
                    }
                });
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
            LeaderboardEntry entry = new LeaderboardEntry(
                    playerName,
                    true,
                    result.gameName(),
                    System.currentTimeMillis()
            );

            String jsonBody = jsonMapper.writeValueAsString(entry);
            
            // Calculate signature using the secure secret key
            String signature = signatureGenerator.calculateHMAC(jsonBody, secretKey);

            // Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("X-HMAC-Signature", signature)
                    .header("X-Player-ID", playerId)
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
            e.printStackTrace();
        }
    }

    /**
     * Fetches the top scores asynchronously.
     */
    @Override
    public CompletableFuture<List<LeaderboardEntry>> getTopScores(int limit) {
        // 1. Build the GET request with the limit parameter
        String urlWithParams = API_URL + "?limit=" + limit;
        
        HttpRequest request = HttpRequest.newBuilder(URI.create(urlWithParams))
                .GET()
                .build();

        // 2. Send it asynchronously
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    try {
                        // 3. Deserialize the JSON List into Java Objects
                        return jsonMapper.readValue(body, new TypeReference<List<LeaderboardEntry>>() {});
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse leaderboard scores", e);
                    }
                });
    }
}