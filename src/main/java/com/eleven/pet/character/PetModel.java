package com.eleven.pet.character;

import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.character.behavior.AwakeState;
import com.eleven.pet.character.behavior.PetState;
import com.eleven.pet.character.behavior.StateRegistry;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.time.TimeListener;
import com.eleven.pet.environment.weather.WeatherListener;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.Inventory;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.inventory.ItemRegistry;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.minigames.impl.GuessingGame;
import com.eleven.pet.minigames.impl.TimingGame;
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
    private final PetStats stats = new PetStats();
    private final ObjectProperty<PetState> currentState;
    private final WeatherSystem weatherSystem;
    private final GameClock clock;
    private final Inventory inventory = new Inventory();
    private double hungerDecayAccum = 0.0;
    private double cleanlinessDecayAccum = 0.0;

    private boolean sleptThisNight = false;
    private boolean passedEightAM = false;

    private double currentSleepDuration = 0.0;
    private int hoursSleptRewardCount = 0;

    public PetModel(String name, WeatherSystem weatherSystem, GameClock clock) {
        this.name = name;
        this.weatherSystem = weatherSystem;
        this.clock = clock;

        // Initialize stats
        stats.registerStat(PetStats.STAT_HUNGER, 50);
        stats.registerStat(PetStats.STAT_HAPPINESS, 50);
        stats.registerStat(PetStats.STAT_ENERGY, 50);
        stats.registerStat(PetStats.STAT_CLEANLINESS, 50);

        // Initialize default state
        StateRegistry registry = StateRegistry.getInstance();
        PetState awakeState = registry.getState(AwakeState.STATE_NAME);
        this.currentState = new SimpleObjectProperty<>(awakeState);


        // Subscribe to environment systems
        if (clock != null) clock.subscribe(this);

        if (weatherSystem != null) weatherSystem.subscribe(this);
        replenishDailyFood();
    }

    // State management
    public void changeState(PetState newState) {
        if (newState == null) return;
        currentState.set(newState);
        System.out.println(name + " changed state to: " + newState.getStateName());
    }

    public PetState getCurrentState() {
        return currentState.get();
    }

    public ReadOnlyObjectProperty<PetState> getStateProperty() {
        return currentState;
    }

    public double getCurrentGameHour() {
        if (clock == null) return 0.0;
        double gameTime = clock.getGameTime();
        double normalizedTime = gameTime / GameConfig.DAY_LENGTH_SECONDS;
        return (normalizedTime * 24.0) % 24.0;
    }

    // Consumable interaction
    public boolean performConsume(Item item) {
        return currentState.get().handleConsume(this, item);
    }

    // Actions
    public void requestSleepInteraction() {
        currentState.get().handleSleep(this);
    }

    public void performClean() {
        currentState.get().handleClean(this);
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
        if (clock == null) return false;
        if (currentState.get() instanceof AsleepState || sleptThisNight) return false;

        double hour = getCurrentGameHour();
        int energy = stats.getStat(PetStats.STAT_ENERGY).get();

        boolean isNight = hour >= GameConfig.HOUR_SLEEP_WINDOW_START || hour < GameConfig.HOUR_SLEEP_WINDOW_END;
        //boolean isTired = energy <= GameConfig.SLEEP_ENERGY_THRESHOLD;

        return isNight;
    }

    // Stat decay system
    public void applyStatDecay(PetModel pet, double timeDelta) {
        hungerDecayAccum      -= GameConfig.HUNGER_DECAY_RATE * timeDelta;
        cleanlinessDecayAccum -= GameConfig.CLEANLINESS_DECAY_RATE * timeDelta;

        int hungerDelta = 0;
        if (hungerDecayAccum <= -1.0 || hungerDecayAccum >= 1.0) {
            hungerDelta = (int) Math.floor(hungerDecayAccum);
            hungerDecayAccum -= hungerDelta;
            pet.getStats().modifyStat(PetStats.STAT_HUNGER, hungerDelta);
        }

        int cleanDelta = 0;
        if (cleanlinessDecayAccum <= -1.0 || cleanlinessDecayAccum >= 1.0) {
            cleanDelta = (int) Math.floor(cleanlinessDecayAccum);
            cleanlinessDecayAccum -= cleanDelta;
            pet.getStats().modifyStat(PetStats.STAT_CLEANLINESS, cleanDelta);
        }

        //pet.getStats().calculateDerivedHappiness();
    }

    // Environment listeners
    @Override
    public void onTick(double timeDelta) {
        if (currentState.get() != null) {
            currentState.get().onTick(this, timeDelta);
        }
    }

    @Override
    public void onWeatherChange(WeatherState newWeather) {
        // TODO: Implement weather change reaction (modify happiness based on weather)
    }
}