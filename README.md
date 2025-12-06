# Virtual Pet Evolution (Björni)

🐻 Meet Björni

Björni is more than just a collection of pixels; he is a fully simulated digital companion designed to live on your desktop. Built with a focus on autonomous behavior and environmental awareness, Björni mimics the lifecycle of a real pet through advanced state management and environmental simulation.

Unlike static virtual pets that wait for user input, Björni lives in a breathing world:

- **He follows the Sun**: Driven by an internal `GameClock`, Björni knows when it is day or night. When the sun sets, his energy drains faster, and he will eventually need to sleep to recover.
- **He feels the Weather**: The `WeatherSystem` determines if it is raining, sunny, or cloudy. Björni's happiness fluctuates based on these conditions—he prefers sunny days but might need extra attention during a storm.
- **He has Complex Needs**: Björni is governed by a strict state machine. You cannot force him to play when he is asleep, and you cannot feed him if he is too full. You must learn his schedule to keep him happy.

**The Björni Personality**  
- *Species*: Digital Brown Bear (*Ursus digitalis*)  
- *Favorite Weather*: Sunshine (High Happiness Modifier)  
- *Sleep Schedule*: Strictly nocturnal (sleeps when `DayCycle = NIGHT`)  
- *Temperament*: Generally happy, but will refuse to interact if his Energy drops below critical levels.

💡 **Implementation Tip: "The Hibernation Feature"**  
When the user closes the application, Björni enters **Hibernation Mode** (serialization). The game uses **AES-GCM encryption** to secure his "cave" (the save file), ensuring that his state is perfectly preserved until he wakes up again. This ties technical complexity (encryption and persistence) to the bear theme in a way that is easy to explain during presentations or exams.

---

## Project Overview

**Virtual Pet Evolution** is a robust, model-heavy desktop simulation game built with **Java** and **JavaFX**. Far from a simple toy application, it uses an enterprise-style **Model–View–Controller (MVC)** architecture to simulate a living digital pet that reacts dynamically to a simulated environment. The project demonstrates advanced software engineering principles, including:

- Cryptographic persistence with AES-GCM
- Multithreaded and time-based simulation (game loop, autosave)
- A decoupled, state-driven behavior system
- Separation of concerns between UI, logic, and environment simulation

---

## Features

- **Living Desktop Pet**: Interact with Björni by feeding, playing, cleaning, and letting him sleep.
- **Dynamic Day/Night Cycle**: A `GameClock` drives a `DayCycle` (DAY/NIGHT), affecting Björni’s energy and the visual background.
- **Reactive Weather System**: The `WeatherSystem` cycles between sunny, rainy, and cloudy conditions, each with different happiness modifiers.
- **Needs & Stats System**: Hunger, happiness, energy, and cleanliness are tracked and decay over time using configurable rates from `GameConfig`.
- **State-Driven Behavior**: `AwakeState` and `AsleepState` enforce realistic constraints (no playing while asleep, no overfeeding, etc.).
- **Secure Save System**: Game state is serialized into a `PetDataDTO` and encrypted using `GcmEncryptionService` so players cannot tamper with save files.
- **Autosave & Hibernation**: A background autosave routine persists Björni’s state at regular intervals and on shutdown.
- **JavaFX UI with Visual Feedback**: A responsive UI displays stats, weather, time of day, and Björni’s current sprite.
- **Particle Effects**: Weather and special events can trigger particle effects (e.g. rain) rendered on a JavaFX `Canvas`.

---

## Architectural Overview

The project follows a **model-heavy MVC architecture** with clear separation of responsibilities:

- **Model ("The Brain")** – package `com.eleven.pet.minigames`
  - `PetModel`: Core domain object representing Björni, holding his name, `PetStats`, current `PetState`, and references to `WeatherSystem` and `GameClock`.
  - `PetStats`: Manages numeric stats (hunger, happiness, energy, cleanliness) as JavaFX `IntegerProperty` values with validation.
  - `PetFactory`: Responsible for correctly constructing new `PetModel` instances and wiring them to environment systems.

- **View ("The Face")** – package `com.eleven.pet.vfx`
  - `PetView`: Builds the JavaFX scene (backgrounds, pet sprite, stat bars, buttons) and binds UI elements to the model’s observable properties.
  - `AssetLoader`: Singleton that caches JavaFX `Image` assets (pet sprites, backgrounds, overlays) to avoid repeated disk I/O.
  - `view.particles.*`: Implements a reusable particle system for visual effects such as rain.

- **Controller ("The Orchestrator")** – package `com.eleven.pet.controller`
  - `PetController`: Handles user input (feed, play, sleep, clean), triggers model actions, coordinates autosave, and communicates with the persistence layer.

Bootstrapping and high-level wiring happen in:

- `MainApp`: JavaFX `Application` that creates the `GameClock`, `WeatherSystem`, persistence services, `PetModel`, `PetController`, and `PetView`, then starts the main game loop.
- `Launcher`: Convenience launcher class for certain environments/IDEs.

### Key Design Patterns

- **State Pattern**
  - `PetState` defines the interface for behaviors like `handleFeed`, `handlePlay`, `handleSleep`, and `onTick`.
  - `AwakeState` and `AsleepState` provide concrete behavior depending on whether Björni is awake or sleeping.
  - `StateRegistry` (singleton) discovers and provides state implementations, using Java’s Service Provider Interface (SPI) via `@AutoService`.

- **Observer / Listener Pattern**
  - `GameClock` notifies registered `TimeListener` instances (including `PetModel`) on each tick.
  - `WeatherSystem` notifies `WeatherListener` instances when weather changes.
  - This event-driven approach keeps the model reactive without tight coupling.

- **Strategy / State for Weather**
  - `WeatherState` defines the interface for weather behavior (name, overlay image, opacity, happiness modifier, particle effect).
  - `SunnyState`, `RainyState`, and `CloudyState` implement `WeatherState` and can be swapped at runtime.

- **Factory Pattern**
  - `PetFactory` encapsulates the logic for creating new `PetModel` instances with properly initialized stats and subscriptions.

- **Singleton Pattern**
  - `AssetLoader` and `StateRegistry` are singletons to provide centralized access to resources and pet states.

- **DTO (Data Transfer Object)**
  - `PetDataDTO` decouples the persisted representation of the game from the live `PetModel`, making saves versionable and robust.

---

## Core Systems

### Game Clock & Day/Night Cycle

- `GameClock` maintains an internal `gameTime` and exposes a computed `DayCycle` (`DAY` or `NIGHT`) based on thresholds from `GameConfig`.
- Classes implementing `TimeListener` (such as `PetModel`) receive regular `onTick` callbacks with a time delta.
- The UI uses this information via `PetView` to switch between day/night backgrounds and to influence stat decay and sleep behavior.

### Weather System

- `WeatherSystem` holds the current `WeatherState` and a list of available weather strategies (`SunnyState`, `RainyState`, `CloudyState`).
- It periodically changes weather (based on `GameConfig.WEATHER_CHANGE_INTERVAL`) and notifies `WeatherListener`s.
- `WeatherState` instances define:
  - A display name
  - Overlay image and opacity
  - A happiness modifier applied to Björni’s mood
  - An associated `ParticleEffect` for visuals

### Pet State & Behavior

- `PetModel` delegates behavior to its current `PetState` implementation.
- `AwakeState` allows interactions like feeding and playing, and applies appropriate stat changes over time.
- `AsleepState` focuses on energy recovery and blocks certain actions (e.g. play) until Björni wakes up.
- `StateRegistry` provides a central lookup for states by name, which is also used when loading from save files.

### Persistence & Encryption

- `PersistenceService` is responsible for:
  - Serializing `PetModel` into a `PetDataDTO`
  - Writing the DTO to disk using an `ObjectMapper` (JSON)
  - Encrypting and decrypting data using an `EncryptionService`
- `GcmEncryptionService` implements `EncryptionService` using **AES-GCM** for authenticated encryption.
- `KeyLoader` can load a key from the environment or generate a development key on first run.
- `GameException` wraps persistence and encryption failures with domain-specific error information.

### Particles & Visual Effects

- The `view.particles` package defines:
  - `Particle`: Base class for individual particles (position, velocity, size, color).
  - `ParticleSystem`: Manages particle creation, updates, and rendering on a JavaFX `Canvas` via an `AnimationTimer`.
  - `ParticleFactory` and concrete factories (e.g. `RainParticleFactory`) for different visual styles.
  - `effects.*` such as `ParticleEffect`, `NoParticleEffect`, and `RainParticleEffect` for starting/stopping visual effects.
- Weather and other gameplay events can trigger different particle strategies for added immersion.

---

## Technical Stack

- **Language**: Java 23 (configured via Maven compiler plugin)
- **UI Framework**: JavaFX (base, controls, graphics)
- **Build Tool**: Maven
- **Security**: `javax.crypto` (AES/GCM/NoPadding) wrapped by `GcmEncryptionService`
- **Concurrency & Timing**:
  - `javafx.animation.AnimationTimer` for the main game loop
  - JavaFX `Timeline` for autosave and weather change timers
- **Dependency Injection / Configuration**: Manual wiring via `MainApp` and `GameConfig` static configuration values

---

## Setup & Run

### Prerequisites

- **JDK 23+** installed and available on `PATH`.
- **Maven** installed (or use the Maven wrapper if added later).
- An environment capable of running JavaFX (JavaFX dependencies are managed via Maven in `pom.xml`).

### Clone the Repository

```bash
git clone https://github.com/Team-11-OOPP-2025/pet-game.git
cd pet-game
```

### Run with Maven

From the project root (where `pom.xml` is located), run:

```bash
mvn clean javafx:run
```

This will:

- Download dependencies (JavaFX, AutoService, Gson).
- Compile the project with Java 23.
- Launch the JavaFX application, starting `com.eleven.pet.core.MainApp`.

On first launch, the game will generate or load a cryptographic key and create a secure save file for Björni’s hibernation data.

### Running from an IDE (IntelliJ IDEA)

1. Open the project folder in IntelliJ.
2. Ensure the correct JDK (23+) is configured for the project.
3. Let IntelliJ import the Maven project.
4. Run the `MainApp` class as a JavaFX application.

---

## Design & Architecture Deep Dive

The heart of the simulation is the **domain model**. `PetModel` acts as the single source of truth for Björni’s state, composed of `PetStats` (numeric properties) and a current `PetState` implementation. Instead of scattering rules across the UI or controller, all game rules—such as when Björni can be fed, how quickly his stats decay, and how he reacts to weather—are encapsulated in the model and its collaborators. `PetFactory` ensures new pets and loaded pets are constructed consistently, subscribing them to the `GameClock` and `WeatherSystem` where appropriate.

`PetController` acts as the orchestrator between JavaFX and the model. It receives user actions from `PetView` (button clicks, etc.), translates them into method calls on `PetModel`, and coordinates cross-cutting concerns like autosave and pause/resume. Thanks to the listener and state patterns, the controller doesn’t need to know the details of how time or weather are implemented—it just wires the systems together and lets them communicate via interfaces.

Cross-cutting systems are modeled explicitly:

- **Time** is handled by `GameClock`, `DayCycle`, and `TimeListener` implementations that react on every tick.
- **Environment & weather** live in `environment.weather` as `WeatherSystem` and multiple `WeatherState` strategies.
- **Behavior** is captured in `behavior` via the `PetState` hierarchy and `StateRegistry` for lookup and transitions.
- **Persistence & security** are encapsulated in `PersistenceService`, `PetDataDTO`, `EncryptionService`, `GcmEncryptionService`, and `KeyLoader`.

This composition-based design makes it easy to extend or swap out behaviors without modifying core classes.

---

## Extensibility & Future Work

The architecture is intentionally designed to be **extensible**:

- **New Pet States**: Implement a new `PetState` (e.g. `SickState`, `PlayfulState`), annotate/register it so `StateRegistry` can discover it, and define how interactions and stat decay change in that state.
- **New Weather Types**: Create additional `WeatherState` implementations (e.g. `SnowyState`, `StormyState`) and plug them into `WeatherSystem`. Add matching assets via `AssetLoader` and custom `ParticleEffect`s for visuals.
- **New Particle Effects**: Extend `Particle`, `ParticleFactory`, and `ParticleEffect` to support new visuals tied to achievements, evolution stages, or mood.
- **Richer Pet Progression**: Add extra stats or milestones to `PetStats` and evolve Björni’s appearance and behavior over time.

Possible future enhancements include:

- Multiple pets or different species managed by an extended `PetFactory`.
- A settings/options menu for tuning decay rates from `GameConfig`.
- Localization support for multiple languages.
- Additional mini-games or training activities that integrate with the existing stats and state machine.

This combination of a clear domain model, well-defined interfaces, and modular subsystems makes **Virtual Pet Evolution (Björni)** a strong foundation for experimentation, teaching, and further game development.

---

## Authors

This project was created by a team of four developers:

- Filip Helin ([@Cleanmain](https://github.com/Cleanmain))
- Hugo Dörrich ([@hugodor2005](https://github.com/hugodor2005))
- Melwin Heimby ([@heim1024](https://github.com/heim1024))
- Raghib Hussain ([@Anajrim01](https://github.com/Anajrim01))

## Acknowledgements

Documentation written by GPT-5.1 based on project structure and rough draft write-up.  
Proofread and edited by **Raghib Hussain**.

## License
This project is licensed under the GNU General Public License v3.0 (GPL‑3.0) - see the [LICENSE](LICENSE) file for details.
