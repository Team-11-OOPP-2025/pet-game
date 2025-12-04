package com.eleven.pet.model;

import com.eleven.pet.behavior.PetState;
import com.eleven.pet.behavior.StateRegistry;
import com.eleven.pet.data.ItemRegistry;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.clock.TimeListener;
import com.eleven.pet.environment.weather.WeatherListener;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.items.Item;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

public class PetModel implements TimeListener, WeatherListener {
    private static final java.util.Random random = new java.util.Random();
    private final String name;
    private final PetStats stats;
    private final ObjectProperty<PetState> currentState;
    private final WeatherSystem weatherSystem;
    private final GameClock clock;
    private final Inventory inventory;

    private boolean sleptThisNight;
    private double sleepStartTime;
    private boolean passedEightAM = true; // Track if we've passed 8 AM check (starts true since game starts at 12:00)

    public PetModel(String name, WeatherSystem weatherSystem, GameClock clock) {
        this.name = name;
        this.weatherSystem = weatherSystem;
        this.clock = clock;
        this.inventory = new Inventory();
        this.sleptThisNight = false;
        this.sleepStartTime = 0;

        // Initialize stats
        this.stats = new PetStats();
        stats.registerStat(PetStats.STAT_HUNGER, 50);
        stats.registerStat(PetStats.STAT_HAPPINESS, 50);
        stats.registerStat(PetStats.STAT_ENERGY, 50);
        stats.registerStat(PetStats.STAT_CLEANLINESS, 50);

        // Initialize state
        StateRegistry registry = StateRegistry.getInstance();
        PetState awakeState = registry.getState("awake");
        this.currentState = new SimpleObjectProperty<>(awakeState);

        // Subscribe to environment systems
        if (clock != null) {
            clock.subscribe(this);
        }
        if (weatherSystem != null) {
            weatherSystem.subscribe(this);
        }
        replenishDailyFood();
    }

    // State management
    public void changeState(PetState newState) {
        if (newState != null) {
            // Call onExit on current state if exists
            PetState oldState = currentState.get();
            if (oldState != null) {
                oldState.onExit(this);
            }
            
            // Change to new state
            currentState.set(newState);
            System.out.println(name + " changed state to: " + newState.getStateName());
            
            // Call onEnter on new state
            newState.onEnter(this);
        }
    }

    public PetState getCurrentState() {
        return currentState.get();
    }

    public ReadOnlyObjectProperty<PetState> getStateProperty() {
        return currentState;
    }

    // Consumable interaction
    public boolean performConsume(Item item) {
        // TODO: Implement consumable interaction
        return currentState.get().handleConsume(this, item);
    }

    // Actions
    public void performSleep() {
        StateRegistry registry = StateRegistry.getInstance();
        changeState(registry.getState("asleep"));
    }

    public void wakeUp() {
        // TODO: Implement wake up logic
    }

    public void performClean() {
        // TODO: Implement clean logic
    }

    public void play() {
        if (currentState.get() != null) {
            currentState.get().handlePlay(this);
        }
    }

    public void clean() {
        performClean();
    }

    public void sleep() {
        if (currentState.get() != null) {
            currentState.get().handleSleep(this);
        }
    }

    public void resetSleepFlag() {
        sleptThisNight = false;
    }
    
    public boolean hasSleptThisNight() {
        return sleptThisNight;
    }
    

    // Minigame system
    public boolean canPlayMinigame() {
        // TODO: Implement minigame eligibility check
        return true; // For now, always allow minigames
    }

    public MinigameResult playMinigame(Minigame minigame) {
        if (minigame == null) return null;

        MinigameResult result = minigame.play(this);

        if (result != null) {
            // Apply happiness delta from the minigame result
            stats.modifyStat(PetStats.STAT_HAPPINESS, result.getHappinessDelta());
            System.out.println(result.getMessage());
        }

        return result;
    }

    public MinigameResult playRandomMinigame() {
        // Create list of available minigames
        List<Minigame> availableGames = new ArrayList<>();
        availableGames.add(new TimingGame());
        // Add more minigames here in the future

        // Pick a random minigame
        Minigame randomGame = availableGames.get(random.nextInt(availableGames.size()));
        return playMinigame(randomGame);
    }

    // Daily management
    public void replenishDailyFood() {
        Item apple = ItemRegistry.get(0);
        int amount = random.nextInt(3, 5);
        inventory.add(apple, amount);
    }

    public boolean shouldPromptSleep() {
        // TODO: Implement sleep prompt logic
        return false;
    }

    // Environment listeners
    @Override
    public void onTick(double timeDelta) {
        if (currentState.get() != null) {
            currentState.get().onTick(this);
        }
    }

    @Override
    public void onWeatherChange(WeatherState newWeather) {
        // TODO: Implement weather change reaction (modify happiness based on weather)
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public PetStats getStats() {
        return stats;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public WeatherSystem getWeatherSystem() {
        return weatherSystem;
    }

    public GameClock getGameClock() {
        return clock;
    }

    public boolean getSleptThisNight() {
        return sleptThisNight;
    }

    public void setSleptThisNight(boolean slept) {
        this.sleptThisNight = slept;
    }

    public double getSleepStartTime() {
        return sleepStartTime;
    }

    public void setSleepStartTime(double timestamp) {
        this.sleepStartTime = timestamp;
    }

    public boolean hasPassedEightAM() {
        return passedEightAM;
    }

    public void setPassedEightAM(boolean value) {
        passedEightAM = value;
    }
}
