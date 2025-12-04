package com.eleven.pet.service.persistence;

import com.eleven.pet.config.GameConfig;

import java.util.HashMap;
import java.util.Map;

/*
 * Data Transfer Object for saving and loading pet data.
 */
public class PetDataDTO {
    private String version;
    private String petName;
    private String currentStateName;
    private Map<String, Integer> statsData;
    private long lastSaveTimestamp;
    private double sleepStartTime;
    private boolean sleptThisNight;
    private Map<Integer, Integer> inventoryData;

    public PetDataDTO() {
        this.version = GameConfig.SAVE_FILE_VERSION;
        this.statsData = new HashMap<>();
        this.inventoryData = new HashMap<>();
    }

    public PetDataDTO(String version) {
        this.version = version;
        this.statsData = new HashMap<>();
        this.inventoryData = new HashMap<>();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
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

    public double getSleepStartTime() {
        return sleepStartTime;
    }

    public void setSleepStartTime(double sleepStartTime) {
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
