package com.eleven.pet.persistence;

import com.eleven.pet.config.GameConfig;

import java.util.HashMap;
import java.util.Map;

/*
 * Data Transfer Object for saving and loading pet data.
 */
public class PetDataDTO {
    private int version;
    private String petName;
    private String currentStateName;
    private Map<String, Integer> statsData;
    private long lastSaveTimestamp;
    private long sleepStartTime;
    private boolean sleptThisNight;
    private Map<Integer, Integer> inventoryData;

    public PetDataDTO() {
        this.version = GameConfig.SAVE_FILE_VERSION;
        this.statsData = new HashMap<>();
        this.inventoryData = new HashMap<>();
    }

    public PetDataDTO(int version) {
        this.version = version;
        this.statsData = new HashMap<>();
        this.inventoryData = new HashMap<>();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getCurrentStateName() {
        return currentStateName;
    }

    public void setCurrentStateName(String currentStateName) {
        this.currentStateName = currentStateName;
    }

    public Map<String, Integer> getStatsData() {
        return statsData;
    }

    public void setStatsData(Map<String, Integer> statsData) {
        this.statsData = statsData;
    }

    public long getLastSaveTimestamp() {
        return lastSaveTimestamp;
    }

    public void setLastSaveTimestamp(long lastSaveTimestamp) {
        this.lastSaveTimestamp = lastSaveTimestamp;
    }

    public long getSleepStartTime() {
        return sleepStartTime;
    }

    public void setSleepStartTime(long sleepStartTime) {
        this.sleepStartTime = sleepStartTime;
    }

    public boolean isSleptThisNight() {
        return sleptThisNight;
    }

    public void setSleptThisNight(boolean sleptThisNight) {
        this.sleptThisNight = sleptThisNight;
    }

    public Map<Integer, Integer> getInventoryData() {
        return inventoryData;
    }

    public void setInventoryData(Map<Integer, Integer> inventoryData) {
        this.inventoryData = inventoryData;
    }
}
