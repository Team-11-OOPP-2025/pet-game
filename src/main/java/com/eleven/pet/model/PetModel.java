package com.eleven.pet.model;

import com.eleven.pet.behavior.AsleepState;
import com.eleven.pet.behavior.AwakeState;
import com.eleven.pet.behavior.PetState;
import com.eleven.pet.behavior.StateRegistry;
import com.eleven.pet.config.GameConfig;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.clock.TimeListener;
import com.eleven.pet.environment.weather.WeatherListener;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.items.Item;
import com.eleven.pet.model.items.ItemRegistry;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
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
    private boolean passedEightAM = false; // Track if we've passed 8 AM check
    private boolean isSleepingWithTimeAcceleration = false;

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

        // Initialize default state
        StateRegistry registry = StateRegistry.getInstance();
        PetState awakeState = registry.getState(AwakeState.STATE_NAME);
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

    public String getName(){
        return name;
    }

    public PetStats getStats() {
        return stats;
    }


    // --- Sleep / clock helpers used by AwakeState & AsleepState ---

    public GameClock getClock() {
        return clock;
    }

    public boolean isSleepingWithTimeAcceleration() {
        return isSleepingWithTimeAcceleration;
    }

    public boolean isPassedEightAM() {
        return passedEightAM;
    }

    public void setPassedEightAM(boolean passedEightAM) {
        this.passedEightAM = passedEightAM;
    }


    // Consumable interaction
    public boolean performConsume(Item item) {
        // TODO: Implement consumable interaction
        return currentState.get().handleConsume(this, item);
    }

    // Actions
    public void performSleep() {
        if (clock != null) {
            // Double the time scale for sleep
            clock.setTimeScale(2.0);
            isSleepingWithTimeAcceleration = true;
            sleepStartTime = clock.getGameTime();
            passedEightAM = false;
            System.out.println(name + " is going to sleep. Time is accelerating...");
        }

        // Change to asleep state
        StateRegistry registry = StateRegistry.getInstance();
        PetState asleepState = registry.getState(AsleepState.STATE_NAME);
        if (asleepState != null) {
            changeState(asleepState);
        }
    }

    public void wakeUp() {
        if (clock != null) {
            // Restore normal time scale
            clock.setTimeScale(1.0);
            isSleepingWithTimeAcceleration = false;
            System.out.println(name + " woke up. Time has returned to normal.");
        }

        // Change to awake state
        StateRegistry registry = StateRegistry.getInstance();
        PetState awakeState = registry.getState(AwakeState.STATE_NAME);
        if (awakeState != null) {
            changeState(awakeState);
        }
    }



    public void performClean() {
        stats.modifyStat(PetStats.STAT_CLEANLINESS, 30);
        System.out.println(name + " is now cleaner! Cleanliness increased.");
    }

    public void resetSleepFlag() {
        sleptThisNight = false;
    }

    public boolean hasSleptThisNight() {
        return sleptThisNight;
    }

    public void setSleptThisNight(boolean b) {
        this.sleptThisNight = b;
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
        // TODO: Delegate this behavior to the PetState
        // Create list of available minigames
        // TODO: Use autoservice to populate the minigame list
        List<Minigame> availableGames = new ArrayList<>();
        availableGames.add(new TimingGame());
        availableGames.add(new GuessingGame());

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
        // No clock -> no prompt
        if (clock == null) {
            return false;
        }

        // Already asleep or has slept this night -> no prompt
        if (currentState.get() instanceof AsleepState || sleptThisNight) {
            return false;
        }

        // Current in-game hour
        double gameTime = clock.getGameTime();
        double normalizedTime = gameTime / GameConfig.DAY_LENGTH_SECONDS; // 0.0–1.0
        double hour = (normalizedTime * 24.0) % 24.0;

        // Pet's energy level
        int energy = stats.getStat(PetStats.STAT_ENERGY).get();

        boolean isLate = hour >= 20.0 || hour < 2.0; // “night” window: 20:00–02:00
        boolean isTired = energy <= 40;

        return isLate && isTired;
    }


    // Environment listeners
    @Override
    public void onTick(double timeDelta) {
        if (currentState.get() != null) {
            currentState.get().onTick(this);
        }

        // Keep happiness in sync with the core stats
        stats.calculateDerivedHappiness();
    }

    @Override
    public void onWeatherChange(WeatherState newWeather) {
        // TODO: Implement weather change reaction (modify happiness based on weather)
    }


}