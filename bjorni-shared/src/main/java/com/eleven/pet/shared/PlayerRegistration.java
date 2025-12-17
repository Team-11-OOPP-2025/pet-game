package com.eleven.pet.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object (DTO) representing a successful player registration.
 * <p>
 * This class is used to securely transport the generated credentials from the
 * server to the client immediately after registration. It contains the
 * unique identity of the player and the shared secret used for HMAC signing.
 * </p>
 * <strong>Security Note:</strong> This object should only be transmitted over
 * secure channels (HTTPS) as it contains the sensitive {@code secretKey}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRegistration implements Serializable {

    /**
     * The unique public identifier for the player (UUID).
     * This ID is sent in the {@code X-Player-ID} header of subsequent requests.
     */
    private String playerId;

    /**
     * The private shared secret used to sign payloads.
     * This key must never be shared or logged in plain text.
     */
    private String secretKey;
}
