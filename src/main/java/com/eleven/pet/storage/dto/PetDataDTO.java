package com.eleven.pet.storage.dto;

import com.eleven.pet.core.GameConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class PetDataDTO {
    private String version = GameConfig.SAVE_FILE_VERSION;
    private String petName;
    private String currentStateName;
    private long lastSaveTimestamp;
    private double sleepStartTime;
    private boolean sleptThisNight;

    private Map<String, Integer> statsData = new HashMap<>();
    private Map<Integer, Integer> inventoryData = new HashMap<>();

    public PetDataDTO(String version) {
        this.version = version;
    }
}