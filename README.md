# Virtual Pet Evolution (Björni)

## 🐻 Meet Björni

Björni is more than just a collection of pixels; he is a fully simulated digital companion designed to live on your
desktop. Built with a focus on autonomous behavior and environmental awareness, Björni mimics the lifecycle of a real
pet through advanced state management and environmental simulation.

Unlike static virtual pets that wait for user input, Björni lives in a breathing world:

- **He follows the Sun**: Driven by an internal GameClock, Björni knows when it is day or night. When the sun sets, his
  energy drains faster, and he will eventually need to sleep to recover.
- **He feels the Weather**: The WeatherSystem determines if it is raining, sunny, or cloudy. Björni's happiness
  fluctuates based on these conditions—he prefers sunny days but might need extra attention during a storm.
- **He has Complex Needs**: Björni is governed by a strict state machine. You cannot force him to play when he is
  asleep, and you cannot feed him if he is too full. You must learn his schedule to keep him happy.

### The Björni Personality

- **Species**: Digital Brown Bear (*Ursus digitalis*)
- **Favorite Weather**: Sunshine (High Happiness Modifier)
- **Sleep Schedule**: Strictly nocturnal (sleeps when DayCycle = NIGHT)
- **Temperament**: Generally happy, but will refuse to interact if his Energy drops below critical levels.

> **💡 Implementation Tip: "The Hibernation Feature"**  
> When the user closes the application, Björni enters Hibernation Mode (serialization). The game uses AES-GCM encryption
> to secure his "cave" (the save file), ensuring that his state is perfectly preserved until he wakes up again. This
> ties
> technical complexity (encryption and persistence) to the bear theme in a way that is easy to explain during
> presentations or exams.

## Project Overview

Virtual Pet Evolution is a robust, model-heavy desktop simulation game built with Java and JavaFX. Far from a simple toy
application, it uses a Package-by-Feature architecture to organize code by domain (Character, Environment, Storage)
rather than technical layer. The project demonstrates advanced software engineering principles, including:

- Cryptographic persistence with AES-GCM
- Decoupled Game Loop architecture (GameEngine)
- State-driven behavior system (Finite State Machine)
- Separation of concerns between UI, logic, and environment simulation

## Features

- **Living Desktop Pet**: Interact with Björni by feeding, playing, cleaning, and letting him sleep.
- **Dynamic Day/Night Cycle**: A GameClock drives a DayCycle (DAY/NIGHT), affecting Björni's energy and the visual
  background.
- **Reactive Weather System**: The WeatherSystem cycles between sunny, rainy, and cloudy conditions, each with different
  happiness modifiers.
- **Needs & Stats System**: Hunger, happiness, energy, and cleanliness are tracked and decay over time using
  configurable rates from GameConfig.
- **State-Driven Behavior**: AwakeState and AsleepState enforce realistic constraints (no playing while asleep, no
  overfeeding, etc.).
- **Secure Save System**: Game state is serialized into a PetDataDTO and encrypted using GcmEncryptionService so players
  cannot tamper with save files.
- **Autosave & Hibernation**: A background autosave routine persists Björni's state at regular intervals and on
  shutdown.
- **JavaFX UI with Visual Feedback**: A responsive UI displays stats, weather, time of day, and Björni's current sprite.
- **Particle Effects**: Weather and special events can trigger particle effects (e.g. rain) rendered on a JavaFX Canvas.

## Architectural Overview

The project follows a Package-by-Feature architecture to ensure high cohesion and low coupling.

### 1. Core Engine (`com.eleven.pet.core`)

The "Engine Room" of the application.

- **MainApp**: The entry point. Handles Dependency Injection (wiring), Window setup, and clean shutdown logic.
- **GameEngine**: Encapsulates the AnimationTimer. It handles the "Game Loop," calculating delta time (dt) and ticking
  the systems 60 times per second.
- **AssetLoader**: Singleton that caches JavaFX Image assets (pet sprites, backgrounds) to avoid repeated disk I/O.

### 2. Character Feature (`com.eleven.pet.character`)

Represents the "Model" and "View" of the Pet entity itself.

**Model ("The Brain")**:

- **PetModel**: Core domain object representing Björni, holding his name, PetStats, current PetState.
- **PetStats**: Manages numeric stats (hunger, happiness) with validation.
- **behavior.***: Contains the State Machine logic (PetState, AwakeState, AsleepState).

**View ("The Face")**:

- **PetView**: Builds the JavaFX scene and binds UI elements to the model's observable properties.

**Controller ("The Orchestrator")**:

- **PetController**: Handles user input, triggers model actions, and communicates with the persistence layer.

### 3. Environment Systems (`com.eleven.pet.environment`)

- **Time**: GameClock and DayCycle manage the flow of time and notify listeners.
- **Weather**: WeatherSystem cycles through WeatherState strategies (Sunny, Rainy), applying global modifiers.

### 4. Storage & Persistence (`com.eleven.pet.storage`)

Handles the "Hibernation" system.

- **PersistenceService**: The facade for saving/loading data.
- **GcmEncryptionService**: Implements AES/GCM/NoPadding encryption to secure the save file.
- **PetDataDTO**: Decouples the persisted JSON structure from the runtime PetModel.

### 5. Visual Effects (`com.eleven.pet.vfx`)

A standalone particle engine.

- **ParticleSystem**: Manages the lifecycle of thousands of particles on a Canvas.
- **effects.***: Concrete implementations like RainEffect or SnowEffect.

## Key Design Patterns

### State Pattern

- **PetState** defines the interface for behaviors like `handleFeed`, `handlePlay`, `handleSleep`, and `onTick`.
- **AwakeState** and **AsleepState** provide concrete behavior depending on whether Björni is awake or sleeping.
- **StateRegistry** (singleton) discovers and provides state implementations.

### Observer / Listener Pattern

- **GameClock** notifies registered TimeListener instances (including PetModel) on each tick.
- **WeatherSystem** notifies WeatherListener instances when weather changes.

### Strategy / State for Weather

- **WeatherState** defines the interface for weather behavior.
- **SunnyState**, **RainyState**, and **CloudyState** implement WeatherState and can be swapped at runtime.

### Game Loop Pattern

Implemented in GameEngine, separating the simulation update (tick) from the rendering frame.

### Factory Pattern

**PetFactory** encapsulates the logic for creating new PetModel instances with properly initialized stats.

### Singleton Pattern

**AssetLoader** is a singleton to provide centralized access to resources.

## Core Systems

### Game Clock & Day/Night Cycle

GameClock maintains an internal `gameTime` and exposes a computed DayCycle (DAY or NIGHT) based on thresholds from
GameConfig. Classes implementing TimeListener (such as PetModel) receive regular `onTick` callbacks with a time delta.
The UI uses this information via PetView to switch between day/night backgrounds and to influence stat decay and sleep
behavior.

### Weather System

WeatherSystem holds the current WeatherState and a list of available weather strategies. It periodically changes weather
and notifies WeatherListeners. WeatherState instances define display name, overlay image, happiness modifiers, and
associated ParticleEffect.

### Persistence & Encryption

- **PersistenceService** is responsible for serializing PetModel into a PetDataDTO.
- **GcmEncryptionService** implements EncryptionService using AES-GCM for authenticated encryption.
- **KeyLoader** can load a key from the environment or generate a development key on first run.

## Technical Stack

- **Language**: Java 23 (configured via Maven compiler plugin)
- **UI Framework**: JavaFX (base, controls, graphics)
- **Build Tool**: Maven
- **Security**: `javax.crypto` (AES/GCM/NoPadding) wrapped by GcmEncryptionService
- **Concurrency & Timing**:
    - `javafx.animation.AnimationTimer` (Game Loop)
    - JavaFX Timeline (Autosave/Weather timers)

## Setup & Run

### Prerequisites

- JDK 23+ installed and available on PATH.
- Maven installed (or use the Maven wrapper if added later).

### Clone the Repository

```
git clone https://github.com/Team-11-OOPP-2025/pet-game.git
cd pet-game
```

### Run with Maven

From the project root (where `pom.xml` is located), run:

```
mvn clean javafx:run
```

This will compile the project and launch the `com.eleven.pet.core.Launcher` class. On first launch, the game will
generate a cryptographic key and create a secure save file for Björni's hibernation data.

> **Note**: If you see a console warning about Unsupported JavaFX configuration on newer JDKs, this is expected and does
> not affect gameplay.

### Running from an IDE (IntelliJ IDEA)

1. Open the project folder in IntelliJ.
2. Ensure the correct JDK (23+) is configured for the project.
3. Reload the Maven project.
4. Run the `Launcher` class (or `MainApp`).

> **Tip**: If you see a "Restricted method" warning, add `--enable-native-access=ALL-UNNAMED` to your VM Options.

## Design & Architecture Deep Dive

The heart of the simulation is the domain model. **PetModel** acts as the single source of truth for Björni's state,
composed of **PetStats** (numeric properties) and a current **PetState** implementation. Instead of scattering rules
across the UI or controller, all game rules—such as when Björni can be fed, how quickly his stats decay, and how he
reacts to weather—are encapsulated in the model and its collaborators.

**PetController** acts as the orchestrator between JavaFX and the model. It receives user actions from PetView (button
clicks, etc.), translates them into method calls on PetModel, and coordinates cross-cutting concerns like autosave.
Thanks to the listener and state patterns, the controller doesn't need to know the details of how time or weather are
implemented—it just wires the systems together and lets them communicate via interfaces.

This composition-based design makes it easy to extend or swap out behaviors without modifying core classes.

## Extensibility & Future Work

The architecture is intentionally designed to be extensible:

- **New Pet States**: Implement a new PetState (e.g. SickState, PlayfulState) and register it.
- **New Weather Types**: Create additional WeatherState implementations (e.g. SnowyState) and plug them into
  WeatherSystem.
- **New Minigames**: Add logic to the minigames package; the modular structure ensures minigames don't tangle with core
  pet logic.
- **Richer Pet Progression**: Add extra stats or milestones to PetStats and evolve Björni's appearance over time.

## Authors

This project was created by a team of four developers:

- Filip Helin ([@Cleanmain](https://github.com/Cleanmain))
- Hugo Dörrich ([@hugodor2005](https://github.com/hugodor2005))
- Melwin Heimby ([@heim1024](https://github.com/heim1024))
- Raghib Hussain ([@Anajrim01](https://github.com/Anajrim01))

## Acknowledgements

- Documentation written by GPT-5.1 based on project structure and rough draft write-up.
- Proofread and edited by the team.

## License

This project is licensed under the GNU General Public License v3.0 (GPL‑3.0) - see
the [LICENSE](https://github.com/Team-11-OOPP-2025/pet-game/blob/main/LICENSE) file for details.