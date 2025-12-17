package com.eleven.pet.network.leaderboard;

import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.shared.Signature;
import com.eleven.pet.shared.model.LeaderboardEntry;
import com.eleven.pet.shared.model.PlayerRegistration;
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

/**
 * Implementation of the {@link LeaderboardService} that communicates with a remote REST API.
 * <p>
 * This client handles the lifecycle of leaderboard interactions, including:
 * <ul>
 * <li>Player registration and credential management.</li>
 * <li>Secure score submission using HMAC signatures.</li>
 * <li>Retrieval of top leaderboard scores.</li>
 * </ul>
 * <p>
 * The client uses {@link java.net.http.HttpClient} for asynchronous network operations
 * and Jackson for JSON serialization/deserialization.
 */
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

    /**
     * Asynchronously registers a new player with the authentication service.
     *
     * @return a {@link CompletableFuture} containing the {@link PlayerRegistration} details
     * (Player ID and Secret Key) upon success.
     * @throws RuntimeException if the response body cannot be parsed.
     */
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

    /**
     * Manually sets the client credentials.
     * <p>
     * Useful if credentials are loaded from storage rather than generating a new registration.
     * </p>
     * @param playerId  the unique player identifier.
     * @param secretKey the secret key for request signing.
     */
    @Override
    public void setCredentials(String playerId, String secretKey) {
        this.playerId = playerId;
        this.secretKey = secretKey;
    }

    /**
     * Submits a score to the leaderboard if the player won the game.
     * <p>
     * This method constructs a {@link LeaderboardEntry}, serializes it to JSON,
     * and signs the request using an HMAC signature in the {@code X-HMAC-Signature} header.
     * <p>
     * This is a "fire-and-forget" operation; errors are logged to {@code System.err}
     * but do not interrupt the flow of the calling thread.
     *
     * @param playerName the name of the player submitting the score.
     * @param result     the result of the minigame containing win status and game name.
     */
    @Override
    public void submitScore(String playerName, MinigameResult result) {
        if (!result.won()) {
            return;
        }

        try {
            LeaderboardEntry entry = new LeaderboardEntry(
                    playerName,
                    1,
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

    /**
     * Asynchronously retrieves the top scores from the leaderboard.
     *
     * @param limit the maximum number of scores to return.
     * @return a {@link CompletableFuture} containing a list of {@link LeaderboardEntry} objects.
     * If parsing fails or an error occurs, an empty list is returned.
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
                        return jsonMapper.readValue(body, new TypeReference<>() {
                        });
                    } catch (Exception e) {
                        System.err.println("Failed to parse leaderboard scores: " + e.getMessage());
                        return Collections.emptyList();
                    }
                });
    }
}