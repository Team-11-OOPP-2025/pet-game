package com.eleven.pet.server.controller;

import com.eleven.pet.shared.LeaderboardEntry;
import com.eleven.pet.shared.Signature;

import tools.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing leaderboard operations.
 * <p>
 * This controller handles the secure submission of scores and the retrieval of leaderboard rankings.
 * <p>
 * <strong>Security:</strong> Submissions are protected via HMAC signatures. The controller delegates
 * key retrieval to the {@link AuthController} to verify the authenticity of the request payload.
 * <p>
 * <strong>Concurrency:</strong> The underlying score storage is synchronized to handle concurrent
 * read/write operations safely.
 */
@RestController
@RequestMapping("/api/v1/leaderboard")
public class ScoreController {
    // Replace this with a dependency-injected database or persistent storage
    private final List<LeaderboardEntry> scores = new ArrayList<>();
    private final Signature signatureUtil = new Signature();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    private final AuthController authController;

    /**
     * Constructs the controller with the required authentication service.
     *
     * @param authController the controller used to retrieve shared secret keys for player validation.
     */
    public ScoreController(AuthController authController) {
        this.authController = authController;
    }

    /**
     * Securely accepts a new score submission.
     * <p>
     * Endpoint: {@code POST /api/v1/leaderboard}
     * <p>
     * <strong>Verification Steps:</strong>
     * <ol>
     * <li>Retrieves the secret key for the given {@code X-Player-ID}.</li>
     * <li>Re-calculates the HMAC signature of the request body using that key.</li>
     * <li>Compares the calculated signature against the provided {@code X-HMAC-Signature}.</li>
     * </ol>
     * If verification fails, a {@link SecurityException} is thrown.
     *
     * @param hmacSignature the cryptographic signature provided in the {@code X-HMAC-Signature} header.
     * @param playerId      the ID of the player provided in the {@code X-Player-ID} header.
     * @param entry         the {@link LeaderboardEntry} payload containing score details.
     * @throws SecurityException if the player ID is unknown or the signature does not match.
     * @throws Exception         if JSON serialization or HMAC calculation fails.
     */
    @PostMapping
    public void submitScore(
        @RequestHeader(value = "X-HMAC-Signature") String hmacSignature,
        @RequestHeader(value = "X-Player-ID") String playerId,
        @RequestBody LeaderboardEntry entry) throws Exception {

        // 1. Retrieve the specific key for this player
        String secretKey = authController.getSharedKey(playerId);

        // 2. Safety Check: If the key is null, this player isn't registered!
        if (secretKey == null) {
             // Ideally, this should also be generic "Unauthorized" to prevent User Enumeration
            throw new SecurityException("Unknown Player ID: " + playerId);
        }

        // 3. Re-calculate the HMAC using the retrieved key
        String jsonBody = jsonMapper.writeValueAsString(entry);
        String calculatedSignature = signatureUtil.calculateHMAC(jsonBody, secretKey);

        // 4. Verify match
        if (!calculatedSignature.equals(hmacSignature)) {
            // Principle of least knowledge: Do not reveal specific details about why auth failed
            throw new SecurityException("Unauthorized");
        }

        // If we pass all checks, accept the score
        synchronized (scores) {
            scores.add(entry);
        }
    }

    /**
     * Retrieves the latest scores, sorted by timestamp.
     * <p>
     * Endpoint: {@code GET /api/v1/leaderboard}
     * <p>
     * The results are sorted in descending order (newest first).
     *
     * @param limit the maximum number of entries to return (default: 50).
     * @return a list of {@link LeaderboardEntry} objects.
     */
    @GetMapping
    public List<LeaderboardEntry> getScores(@RequestParam(value = "limit", defaultValue = "50") int limit) {
        synchronized (scores) {
            return scores.stream()
                .sorted(Comparator.comparingLong(LeaderboardEntry::timeStamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
        }
    }
}