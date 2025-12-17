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
import java.util.Collections;
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
    }

    @Override
    public CompletableFuture<PlayerRegistration> registerPlayer() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(AUTH_URL))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
                
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

    public String getPlayerId() {
        return playerId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setCredentials(String playerId, String secretKey) {
        this.playerId = playerId;
        this.secretKey = secretKey;
    }

    @Override
    public void submitScore(String playerName, MinigameResult result) {
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
            String signature = signatureGenerator.calculateHMAC(jsonBody, secretKey);

            HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("X-HMAC-Signature", signature)
                    .header("X-Player-ID", playerId)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

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
                        System.err.println("Failed to parse leaderboard scores: " + e.getMessage());
                        return Collections.<LeaderboardEntry>emptyList();
                    }
                });
    }
}