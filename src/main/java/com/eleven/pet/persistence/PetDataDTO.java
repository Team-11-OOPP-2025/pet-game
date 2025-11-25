package com.eleven.pet.persistence;

import java.util.Map;

public class PetDataDTO {
    private int version;
    private String petName;
    private String currentStateName;
    private Map<String, Integer> statsData;
    private long lastSaveTimestamp;
    
    public PetDataDTO() {
    }
    
    public PetDataDTO(int version) {
        this.version = version;
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
    
    public void setPetName(String name) {
        this.petName = name;
    }
    
    public String getCurrentStateName() {
        return currentStateName;
    }
    
    public void setCurrentStateName(String name) {
        this.currentStateName = name;
    }
    
    public Map<String, Integer> getStatsData() {
        return statsData;
    }
    
    public void setStatsData(Map<String, Integer> data) {
        this.statsData = data;
    }
    
    public long getLastSaveTimestamp() {
        return lastSaveTimestamp;
    }
    
    public void setLastSaveTimestamp(long ts) {
        this.lastSaveTimestamp = ts;
    }
}
