package com.eleven.pet.storage.dto;

import com.eleven.pet.core.GameConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object representing all persistent data of a pet
 * that is stored in the save file.
 */
@Data
@NoArgsConstructor
public class PetDataDTO {

    /**
     * Save file format version used to handle compatibility between
     * different game versions.
     */
    private String version = GameConfig.SAVE_FILE_VERSION;

    /**
     * Display name of the pet chosen by the player.
     */
    private String petName;

    /**
     * Name of the current state of the pet (e.g. "AWAKE", "ASLEEP").
     */
    private String currentStateName;

    /**
     * Timestamp (epoch millis) when this data was last saved.
     */
    private long lastSaveTimestamp;

    /**
     * In‑game time (hours) when the pet started sleeping.
     */
    private double sleepStartTime;

    /**
     * Flag indicating whether the pet has already slept during
     * the current in‑game night.
     */
    private boolean sleptThisNight;

    /**
     * Remaining reward cooldown in in‑game hours.
     */
    private double rewardCooldown;

    /**
     * Serialized pet stats, keyed by stat identifier (e.g. "hunger", "happiness").
     */
    private Map<String, Integer> statsData = new HashMap<>();

    /**
     * Serialized inventory items, keyed by item ID and mapped to quantity.
     */
    private Map<Integer, Integer> inventoryData = new HashMap<>();

    /**
     * Creates a new DTO with an explicit save file version.
     *
     * @param version save file format version to store with this DTO
     */
    public PetDataDTO(String version) {
        this.version = version;
    }
}
