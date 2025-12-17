package com.eleven.pet.server.controller;

import com.eleven.pet.shared.model.PlayerRegistration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST Controller responsible for player authentication and credential management.
 * <p>
 * This controller handles the initial registration process, generating cryptographic
 * keys that the client must use to sign future requests (e.g., leaderboard submissions).
 * </p>
 * <strong>Storage Note:</strong> This implementation uses an in-memory {@link ConcurrentHashMap}
 * to store credentials. All registered players and keys will be lost if the server is restarted.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for player authentication and credential management")
public class AuthController {

    /**
     * In-memory storage mapping PlayerID -> SecretKey.
     * Thread-safe to allow concurrent registrations.
     */
    // Future improvement could be to use Redis or another in-memory DB for persistence across restarts
    private final ConcurrentHashMap<String, String> validKeys = new ConcurrentHashMap<>();

    /**
     * Registers a new player and generates a unique set of credentials.
     * <p>
     * Endpoint: {@code POST /api/v1/auth/register}
     * </p>
     * Generates a random UUID for the Player ID and a random UUID for the Secret Key.
     * These are stored internally and returned to the client.
     *
     * @return a {@link PlayerRegistration} object containing the new {@code playerId}
     * and {@code secretKey}.
     */
    @Operation(summary = "Register a new player",
            description = "Generates and returns a unique Player ID and Secret Key for authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered a new player and generated credentials.")
    })
    @PostMapping("/register")
    public PlayerRegistration register() {
        String playerId = UUID.randomUUID().toString();

        // UUID is sufficiently random for secret keys in this context
        // however, in a more serious environment using a cryptographic RNG would be advisable
        String secretKey = UUID.randomUUID().toString();

        validKeys.put(playerId, secretKey);

        return new PlayerRegistration(playerId, secretKey);
    }

    /**
     * Retrieves the secret key associated with a given player ID.
     * <p>
     * This method is intended for internal server use (e.g., by the {@code LeaderboardController})
     * to validate HMAC signatures on incoming requests.
     * </p>
     *
     * @param playerId the unique identifier of the player.
     * @return the associated secret key, or {@code null} if the player ID is not found.
     */
    public String getSharedKey(String playerId) {
        return validKeys.get(playerId);
    }
}
