package com.eleven.pet.character;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
import com.eleven.pet.inventory.StatPotionDefinition;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.minigames.impl.GuessingGame;
import com.eleven.pet.minigames.impl.TimingGame;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Data;

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
    private double happinessDecayAccum = 0.0;

    private boolean sleptThisNight = false;
    private boolean passedEightAM = false;
    private double currentSleepDuration = 0.0;
    private int hoursSleptRewardCount = 0;
    
    // Remaining time (in game hours) until the next reward can be claimed
    private double rewardCooldown = 0.0;

    private PetDefinition definition;

    // Thread-safe list to handle concurrent modifications during ticks
    private final List<ActivePotion> activePotions = new CopyOnWriteArrayList<>();

    // --- Inner Class to Track State ---
    private static class ActivePotion {
        private final StatPotionDefinition def;
        private double timeRemaining;

        public ActivePotion(StatPotionDefinition def) {
            this.def = def;
            this.timeRemaining = def.effectDuration();
        }

        void tick(double delta) {
            timeRemaining -= delta;
        }

        boolean isExpired() {
            return timeRemaining <= 0;
        }

        String getStatType() { return def.statType(); }
        double getMultiplier() { return def.multiplier(); }
        String getName() { return def.name(); }
    }

    public PetModel(String name, WeatherSystem weatherSystem, GameClock clock) {
        this.name = name;
        this.weatherSystem = weatherSystem;
        this.clock = clock;
        this.definition = new PetDefinition("Bear"); // Default definition

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

        // Set up daily inventory items
        replenishDailyFood();
    }

    // --- Potion Logic ---

    public double getStatMultiplier(String statName) {
        double totalMultiplier = 1.0;
        for (ActivePotion potion : activePotions) {
            if (potion.getStatType().equals(statName)) {
                totalMultiplier *= potion.getMultiplier();
            }
        }
        return totalMultiplier;
    }

    public double getPotionDuration(String statName) {
        double maxDuration = 0.0;
        for (ActivePotion potion : activePotions) {
            if (potion.getStatType().equals(statName)) {
                maxDuration = Math.max(maxDuration, potion.timeRemaining);
            }
        }
        return maxDuration;
    }

    public void addPotion(StatPotionDefinition def) {
        if (def != null) {
            activePotions.add(new ActivePotion(def));
            System.out.println("Effect Applied: " + def.name() + " (x" + def.multiplier() + " to " + def.statType() + ")");
        }
    }

    // --- Existing Methods ---

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

    public boolean performConsume(Item item) {
        return currentState.get().handleConsume(this, item);
    }

    public void requestSleepInteraction() {
        currentState.get().handleSleep(this);
    }

    public void performClean() {
        currentState.get().handleClean(this);
    }

    public boolean canPlayMinigame() {
        return true; 
    }

    private MinigameResult playMinigame(Minigame minigame) {
        if (minigame == null) return null;

        MinigameResult result = minigame.play(this);

        if (result != null) {
            // Apply happiness delta from the minigame result
            stats.modifyStat(PetStats.STAT_HAPPINESS, result.happinessDelta());
            System.out.println(result.message());
        }

        return result;
    }

    public MinigameResult playRandomMinigame() {
        List<Minigame> availableGames = new ArrayList<>();
        availableGames.add(new TimingGame());
        availableGames.add(new GuessingGame());
        Minigame randomGame = availableGames.get(random.nextInt(availableGames.size()));
        return playMinigame(randomGame);
    }

    public void replenishDailyFood() {
        System.out.println("Replenishing Daily Food");
        for (int i = 0; i < random.nextInt(1, 4); i++) {
            Item foodItem = ItemRegistry.get(i);
            int amount = random.nextInt(1, 4);
            inventory.add(foodItem, amount);
        }
    }

    public boolean shouldPromptSleep() {
        if (clock == null) return false;
        if (currentState.get() instanceof AsleepState || sleptThisNight) return false;

        double hour = getCurrentGameHour();
        return hour >= GameConfig.HOUR_SLEEP_WINDOW_START || hour < GameConfig.HOUR_SLEEP_WINDOW_END;
    }

    public void applyStatDecay(double timeDelta) {
        // Use definition rates if available, otherwise fallback or config
        double hungerRate = (definition != null) ? definition.hungerDecayRate() : GameConfig.HUNGER_DECAY_RATE;
        double cleanRate = (definition != null) ? definition.cleanlinessDecayRate() : GameConfig.CLEANLINESS_DECAY_RATE;

        hungerDecayAccum -= hungerRate * timeDelta;
        cleanlinessDecayAccum -= cleanRate * timeDelta;
        
        if (hungerDecayAccum <= -1.0 || hungerDecayAccum >= 1.0) {
            int hungerDelta = (int) Math.floor(hungerDecayAccum);
            hungerDecayAccum -= hungerDelta;
            stats.modifyStat(PetStats.STAT_HUNGER, hungerDelta);
        }

        if (cleanlinessDecayAccum <= -1.0 || cleanlinessDecayAccum >= 1.0) {
            int cleanDelta = (int) Math.floor(cleanlinessDecayAccum);
            cleanlinessDecayAccum -= cleanDelta;
            stats.modifyStat(PetStats.STAT_CLEANLINESS, cleanDelta);
        }

        double currentHunger = stats.getStat(PetStats.STAT_HUNGER).get();
        double currentClean = stats.getStat(PetStats.STAT_CLEANLINESS).get();

        double happinessRate = (definition != null) ? definition.happinessDecayRate() : GameConfig.HAPPINESS_DECAY_RATE;

        if (currentHunger < 30) happinessRate *= 2.0;
        if (currentClean < 30) happinessRate *= 1.5;
        
        // Apply weather happiness modifier
        if (weatherSystem != null && weatherSystem.getCurrentWeather() != null) {
            double weatherModifier = weatherSystem.getCurrentWeather().getHappinessModifier();
            happinessRate /= weatherModifier; // Higher modifier = slower decay 
        }

        happinessDecayAccum -= happinessRate * timeDelta;

        if (Math.abs(happinessDecayAccum) >= 1.0) {
            int happyDelta = (int) Math.floor(happinessDecayAccum);
            happinessDecayAccum -= happyDelta;
            stats.modifyStat(PetStats.STAT_HAPPINESS, happyDelta);
        }
    }

    @Override
    public void onTick(double timeDelta) {
        // 1. Update Potions
        for (ActivePotion potion : activePotions) {
            potion.tick(timeDelta);
            if (potion.isExpired()) {
                activePotions.remove(potion);
                System.out.println("Effect Expired: " + potion.getName());
            }
        }
        
        // 2. Update Reward Cooldown
        if (clock != null && rewardCooldown > 0) {
            // FIX: timeDelta is ALREADY scaled by GameClock, so we use it directly.
            // 1 unit of timeDelta = 1 in-game hour.
            rewardCooldown -= timeDelta; 
            if (rewardCooldown < 0) {
                rewardCooldown = 0;
            }
        }

        // 3. Existing State Logic
        if (currentState.get() != null) {
            currentState.get().onTick(this, timeDelta);
        }
    }

    @Override
    public void onWeatherChange(WeatherState newWeather) {
        System.out.println(name + " notices the weather changed to: " + newWeather.getName());
        // Weather effects are applied continuously through the happiness decay modifier
    }

    public void addToInventory(Item item, int quantity) {
        inventory.add(item, quantity);
    }
    
    public ReadOnlyIntegerProperty getStatProperty(String statName) {
        return stats.getStat(statName);
    }
}