# Björni by Team 11

## 🐻 Meet Björni

Björni is more than just a collection of pixels; he is a fully simulated digital companion designed to live on your desktop. Built with a focus on autonomous behavior and environmental awareness, Björni mimics the lifecycle of a real pet through advanced state management and environmental simulation.

Unlike static virtual pets that wait for user input, Björni lives in a breathing world:

* **He follows the Sun**: Driven by an internal `GameClock`, Björni knows when it is day or night. When the sun sets, his energy drains faster, and he will eventually need to sleep to recover.
* **He feels the Weather**: The `WeatherSystem` determines if it is raining, sunny, or cloudy. Björni's happiness fluctuates based on these conditions—he prefers sunny days but might need extra attention during a storm.
* **He has Complex Needs**: Björni is governed by a strict state machine. You cannot force him to play when he is asleep, and you cannot feed him if he is too full.

### The Björni Personality

* **Species**: Digital Brown Bear (*Ursus digitalis*)
* **Sleep Schedule**: Strictly nocturnal (sleeps when `DayCycle` = NIGHT).
* **Hibernation Feature**: When the application closes, Björni’s state is preserved using **AES-GCM encryption** to secure the save file.

## Project Overview

Virtual Pet Evolution is a robust, model-heavy desktop simulation game built with **Java 23** and **JavaFX 25**. The project has evolved into a professional **Multi-Module Maven Architecture**, separating the simulation logic, the server-side infrastructure, and shared data models.

### Architecture & Engineering Principles

* **Multi-Module Build**: Decomposed into `bjorni-shared`, `bjorni-server`, and `bjorni-client`.
* **Reactive MVC**: A View layer that observes the Model via JavaFX Properties.
* **Secure Network Integrity**: HMAC-signed score submissions to prevent data tampering.
* **Interface-Driven Design**: The UI is decoupled from network implementations via the `LeaderboardService` interface.

## Features

* **Living Desktop Pet**: Interact with Björni through feeding, playing, cleaning, and sleeping.
* **Global Leaderboard**: A secure, Spring Boot-backed system for submitting and fetching high scores.
* **Daily Reward System**: Interactive `Chest` components that grant random item rewards to the pet's inventory.
* **Embedded Minigames**: Fully interactive games (e.g., *Guessing Game*, *Timing Challenge*) that run directly on the virtual TV screen.
* **Secure Persistence**: Game state is serialized into a `PetDataDTO` and encrypted using the `GcmEncryptionService`.

## Module Breakdown

### 1. Shared Module (`bjorni-shared`)

Contains the common domain models and utilities used by both the client and the server.

* **Models**: `LeaderboardEntry`, `PlayerRegistration`.
* **Security**: `Signature` utility for HMAC calculation.

### 2. Server Module (`bjorni-server`)

A **Spring Boot 4.0** application providing the game's REST API.

* **AuthController**: Handles player registration and key management.
* **ScoreController**: Manages leaderboard data and validates signed score submissions.
* **OpenAPI Integration**: Automated documentation via SpringDoc.

### 3. Client Module (`bjorni-client`)

The primary JavaFX application and simulation engine.

* **Core Engine**: Manages the `GameLoop` and high-performance asset caching via `AssetLoader`.
* **Character Logic**: Implements the Finite State Machine for Björni's behavior.
* **Network Client**: An asynchronous `LeaderboardClient` using Java's `HttpClient` for non-blocking API calls.

## Key Design Patterns

* **State Pattern**: Used for `PetState` (Awake/Asleep) and `WeatherState` (Sunny/Rainy/Cloudy).
* **Strategy Pattern**: Employed in the `WeatherSystem` to swap environmental rules at runtime.
* **Dependency Inversion**: The `LeaderboardView` depends on the `LeaderboardService` interface rather than the concrete client.
* **Singleton**: The `AssetLoader` provides centralized, cached access to UI resources.

## Technical Stack

* **Client**: Java 23, JavaFX 25, Jackson (JSON), Lombok.
* **Server**: Spring Boot 4.0, SpringDoc OpenAPI, Mockito.
* **Security**: HMAC-SHA256 for network signatures, AES-GCM for file encryption.

## Setup & Run

### Prerequisites

* JDK 23+
* Maven 3.9+

### Running the Server

```bash
cd bjorni-server
mvn spring-boot:run

```

### Running the Client

```bash
cd bjorni-client
mvn javafx:run

```

## Design & Architecture Deep Dive

The heart of the simulation is the **PetModel**. Instead of scattering rules across the UI, all game rules—such as when Björni can be fed or how weather affects him—are encapsulated in the model.

**Extensibility via OCP**: The `PetModel` and UI bindings are designed to be Open for Extension. Adding a new stat (e.g., "Thirst") requires no changes to the binding logic in `HUDView` or `PetModel` structure, thanks to the generic property accessor pattern.

**Minigame Integration**: The Minigame system allows for infinite expandability. New games can be added by implementing the `Minigame` interface and registering them in the Controller. They automatically inherit the "TV Mode" zoom and display logic without touching the core rendering code.


## Authors

* Filip Helin ([@Cleanmain](https://github.com/Cleanmain))
* Hugo Dörrich ([@hugodor2005](https://github.com/hugodor2005))
* Melwin Heimby ([@heim1024](https://github.com/heim1024))
* Raghib Hussain ([@Anajrim01](https://github.com/Anajrim01))

## License

This project is licensed under the GNU General Public License v3.0 (GPL‑3.0).
