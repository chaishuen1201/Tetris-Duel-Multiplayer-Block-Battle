# CW2025 Developing Maintainable Software (COMP2042 UNMC) - Tetris Game

## Table of Contents

### 1.0 GitHub Repository
- 1.1 [Repository Link](#11-repository-link)
- 1.2 [Git Branch Structure and Workflow](#12-git-branch-structure-and-workflow)
### 2.0 Setup & Compilation Instructions
- 2.1 [Environment Setup (Windows)](#21-environment-setup-windows)
- 2.2 [Project Setup in IntelliJ IDEA](#22-project-setup-in-intellij-idea)
- 2.3 [Running the Application (Game)](#23-running-the-application-game)
### 3.0 Features
- 3.1 [Implemented and Working Properly](#31-implemented-and-working-properly)
- 3.2 [Implemented but Not Working Properly](#32-implemented-but-not-working-properly)
- 3.3 [Features Not Implemented](#33-features-not-implemented)
### 4.0 New Java Classes
- 4.1 [Controller Package](#41-controller-package-comcomp2042controller)
- 4.2 [Model Package](#42-model-package-comcomp2042model)
- 4.3 [View Package](#43-view-package-comcomp2042view)
- 4.4 [Utility Package](#44-utility-package-comcomp2042util)
### 5.0 Modified Java Classes
- 5.1 [Relocated to Model Package](#51-relocated-to-model-package)
- 5.2 [Relocated to Event Package](#52-relocated-to-event-package)
- 5.3 [Relocated to Utility Package](#53-relocated-to-utility-package)
- 5.4 [Controller Classes](#54-controller-classes)
- 5.5 [View Classes](#55-view-classes)
- 5.6 [Main Class](#56-main-class)
- 5.7 [Logic Package](#57-logic-package-minimal-changes)
### 6.0 Unexpected Problems
- 6.1 [Unexpected Problems & Solutions](#61-unexpected-problems--solutions)
### 7.0 Testing
- 7.1 [Unit Tests](#71-unit-tests)
### 8.0 Project Structure
- 8.1 [Package Organization](#81-package-organization)
- 8.2 [Resource Files](#82-resource-files)


## 1.0 GitHub Repository
### 1.1 Repository Link
1. **Name:**  Lim Chai Shuen
2. **Student ID:** 20612781
3. **Link:** https://github.com/chaishuen1201/CW2025

### 1.2 Git Branch Structure and Workflow
The project follows a structured branching workflow to manage feature development, refactoring, and integration. The purpose of each branch is outlined below:

- master:
  The base branch forked from the original source code. This branch receives the final, fully tested, and production-ready version of the system after all development branches are merged.

- refactor-feature:
  A refactoring branch created from the initial fork. It focuses on cleaning up the original codebase, improving readability, and preparing the project for further feature development.

- version1:
  Development branch dedicated to enhancing the single-player mode. This branch adds gameplay improvements, UI adjustments, and additional logic specific to single-player functionality.

- version2:
  Development branch that builds on version1 and introduces the multiplayer mode. It includes new gameplay mechanics, UI layouts for two players, and handling of multiplayer interactions.

- refactor-phase2:
  A refactoring branch created after version2. Its purpose is to re-organize the multiplayer code, apply SOLID principles, improve maintainability, and remove duplicated or unnecessary logic.

---

## 2.0 Setup & Compilation Instructions

### 2.1 Environment Setup (Windows)

#### Step 1: Download Java 21
1. Go to [Oracle JDK 21 Downloads](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
    - *Note: Requires Oracle account. Alternatively, use [Eclipse Temurin JDK 21](https://adoptium.net/temurin/releases/?version=21) (OpenJDK, no login required)*
2. Click the link for **Windows x64 Installer** (.exe file) from Java SE Development Kit 21.0.8 section
3. Navigate to your Downloads folder and double-click on `jdk-21.0.8_windows-x64_bin.exe`
4. Follow the installation wizard:
   - Click "Yes" if prompted by User Account Control
   - Click "Next" on the welcome screen
   - Choose installation location (keep default: `C:\Program Files\Java\jdk-21.0.8\`)
   - Click "Next" to begin installation
   - Wait for installation to complete
   - Click "Close" when finished
  
#### Step 2: Install JavaFX SDK
1. Download the JavaFX SDK from Gluon: https://gluonhq.com/products/javafx/  
   (Choose **JavaFX 21 – Windows x64 SDK**)
2. Extract the file: `openjfx-21.0.9_windows-x64_bin-sdk.zip`
3. Right-click → **Extract All…**
4. Choose a location (recommended): `C:\Program Files\Java\`
5. After extraction, confirm the folder exists: `javafx-sdk-23.0.2`
6. Verify it contains:
   - `lib/` – JavaFX `.jar` files  
   - `bin/` – utilities  
   - `legal/` – license files

#### Step 3: Set Environment Variables
1. Press `Win + R`, type `sysdm.cpl`, and press Enter
2. Click "Environment Variables"
3. Under **System Variables**:
   - Click "New" and add:
     - Variable name: `JAVA_HOME`
     - Variable value: `C:\Program Files\Java\jdk-21.0.8`
   - Find "Path" variable, click "Edit"
   - Click "New" and add: `%JAVA_HOME%\bin`

#### Step 4: Verify Java Installation
Open a new Command Prompt and run:
```
java -version
```
Expected output: `java version "21.0.x"`

#### Step 5: Install IntelliJ IDEA
1. Go to [JetBrains IntelliJ IDEA Download](https://www.jetbrains.com/idea/download/?section=windows)
2. Download IntelliJ IDEA Community Edition (free version)
3. Run the installer and follow the setup wizard

### 2.2 Project Setup in IntelliJ IDEA

#### Step 1: Import Project
1. Open IntelliJ IDEA
2. Click **Open** (not Import)
3. Navigate to `C:\Users\chais\IdeaProjects\CW2025`
4. Select the project folder and click **Open**
5. IntelliJ will automatically detect it as a Maven project

#### Step 2: Configure SDK
1. Go to **File** → **Project Structure** (`Ctrl+Alt+Shift+S`)
2. Under **Project Settings** → **Project**:
   - Set **Project SDK**: Click "New..." → **JDK**
   - Navigate to: `C:\Program Files\Java\jdk-21.x.x`
   - Click **OK**
3. Set **Project language level**: **21 - Records, patterns, ...**

#### Step 3: Configure Maven
1. IntelliJ should automatically detect `pom.xml`
2. If not, right-click on `pom.xml` → **Maven** → **Sync Project**
3. Wait for dependencies to download (check progress bar)


### 2.3 Running the Application (Game)

#### Method 1: Using Maven Tool Window (Recommended)
1. On the right side of IntelliJ, click the **Maven** tool window
2. If not visible, go to **View → Tool Windows → Maven**
3. Expand your project tree:
   - **Lifecycle**: Contains standard Maven phases
     - Double-click `clean` to clean previous builds
     - Double-click `compile` to compile the project
     - Double-click `package` to create executable JAR
   - **Plugins → javafx**:
     - Double-click `javafx:run` to run the application directly

*Note: This method works if your `pom.xml` includes JavaFX Maven plugin configuration.*

#### Method 2: Using Run Configuration (Recommended for Module Path Setup)
1. Go to **Run → Edit Configurations**
2. Click **+** → **Application**
3. Configure:
   - **Name**: `Tetris`
   - **Main class**: `com.comp2042.Main`
   - **VM options** (adjust path to match your actual JavaFX location):
     ```
     --module-path "C:\path\to\javafx\lib"
     --add-modules javafx.controls,javafx.fxml,javafx.media
     ```
     *If you installed JavaFX elsewhere, update the path accordingly*
   - **Working directory**: `$ProjectFileDir$` (default)
4. Click **Apply** → **OK**
5. Now you can:
   - Click the green **Run** button ▶
   - Or click the dropdown next to the run button and select "Tetris"

---

## 3.0 Features

### 3.1 Implemented and Working Properly

1. **Main Menu System**
   - Professional main menu with navigation to single player, multiplayer, settings, and exit options
   - Smooth transitions between different game modes and screens
   - Background music integration for enhanced user experience

2. **Settings Panel**
   - Volume control slider for adjusting game audio
   - Customizable key bindings for all game controls
   - Support for rebinding controls for both single player and multiplayer modes
   - Key bindings saved persistently using properties file

3. **Hold Brick Function**
   - Ability to hold current brick and swap with previously held brick
   - Visual display panel showing the held brick
   - Follows standard Tetris hold mechanics

4. **Next Brick Preview**
   - Display of the next 3 upcoming bricks
   - Clear visual representation with proper scaling
   - Helps players plan their moves in advance

5. **Pause Menu**
   - Pause functionality with dedicated pause panel
   - Options to resume, restart, view settings, or quit to main menu
   - Game state properly frozen during pause
   - Separate pause panels for single player and multiplayer modes

6. **Extended Level System**
   - Increased playable levels from 5 to 15
   - Progressive difficulty with faster brick drop speeds
   - Level advancement based on lines cleared

7. **Live Statistics Display**
   - Real-time display of lines cleared count
   - Current score with proper scoring system
   - Current level indicator
   - Separate statistics for each player in multiplayer mode

8. **Countdown System**
   - 3-2-1 countdown before game starts in single player mode
   - Audio countdown with synchronized visual display
   - Prevents premature game start

9. **Ghost Piece**
   - Transparent preview of where the current brick will land
   - Uses distinct visual styling for clarity
   - Updates in real-time as brick moves horizontally

10. **Multiplayer Mode**
    - Full 1v1 multiplayer support on the same computer
    - Side-by-side board display for both players
    - Independent controls for Player 1 (WASD) and Player 2 (Arrow keys)
    - Synchronized game start with ready system
    - Competitive gameplay with garbage line mechanics

11. **Ready System (Multiplayer)**
    - Both players must ready up before game starts
    - Visual indicators showing ready status for each player
    - Countdown begins only when both players are ready

12. **Game Over Panel**
    - Displays final score and time played
    - Shows top 3 high scores (leaderboard)
    - Options to try again or return to main menu
    - Plays game over sound effect

13. **Winning Panel (Multiplayer)**
    - Displays winner when one player loses
    - Victory animation and sound effects
    - Options to restart match or return to main menu

14. **Timer System**
    - Tracks elapsed game time
    - Formatted display (MM:SS)
    - Shown on game over screen
    - Separate timers for single player and multiplayer modes

15. **Garbage System (Multiplayer)**
    - Sending garbage lines to opponent when clearing multiple lines
    - Garbage lines have random holes for clearability
    - Visual and audio feedback when garbage is sent/received
    - Adds strategic depth to multiplayer matches

16. **Leaderboard**
    - Tracks top 3 high scores
    - Persistent storage during game session
    - Displayed on game over screen

17. **Audio System**
    - Background music for main menu and gameplay
    - Sound effects for:
      - Line clears
      - Level up
      - Game over
      - Victory
      - Countdown
      - Button clicks and hover
      - Garbage attacks
    - Volume control through settings

18. **Enhanced Visual Design**
    - Custom color schemes for different brick types
    - Grid-based board with visual gaps between cells
    - Smooth rendering and animations
    - Proper scaling for multiplayer side-by-side view

### 3.2 Implemented but Not Working Properly

1. **Garbage Delay System (Partial Implementation)**
   - **Issue:** The garbage delay mechanism is partially implemented but not functioning as intended. While a timeline system exists in `GameLoopManager` to process garbage lines gradually (one every 2 seconds), the `GarbageManager.sendGarbageToOpponent()` method bypasses this delay and processes all garbage immediately upon sending.
   - **Current Behavior:** When a player clears lines, garbage is added to the opponent's queue but then processed immediately in the same frame, negating the intended delay mechanism. The timeline system exists but is effectively unused.
   - **Impact:** Players do not have the intended time window to counter incoming garbage by clearing their own lines, as garbage appears instantly rather than gradually.
   - **Attempted Solutions:** The code includes both immediate processing (for instant feedback) and timeline-based processing (for delayed delivery), but they conflict with each other. The immediate processing always takes precedence.


### 3.3 Features Not Implemented

1. **Garbage Delay System Visual Indicators**
   - While the garbage queue system and cancellation mechanics are implemented (see Section 3.2 for partial implementation issues), the following visual components have not been implemented:
     - Visual loading indicator showing pending garbage lines count
     - Progress bar or visual representation of garbage queue status
     - On-screen notification when garbage is cancelled by clearing lines
     - Visual feedback showing when garbage lines are being processed
   - **Reason:** Time constraints and complexity in implementing the visual UI components for garbage system feedback. The core logic exists but lacks user-facing indicators.

2. **Customizable Themes**
   - The game currently uses a fixed visual theme and does not support:
     - Multiple color schemes or palettes
     - Customizable block designs and textures
     - Background customization
     - Board appearance options
   - **Reason:** Focus was prioritized on core gameplay mechanics and multiplayer functionality rather than cosmetic customization

3. **Independent Audio Controls**
   - The audio settings currently only provide a single master volume control
   - Not implemented:
     - Separate volume sliders for background music and sound effects
     - Individual toggle switches to mute music or sound effects independently
     - Per-sound effect volume control
   - **Reason:** Implementing separate audio channels would require restructuring the AudioManager class and was deemed lower priority than gameplay features

4. **Persistent Leaderboard**
   - High scores are only stored during the current game session
   - Not implemented:
     - Saving high scores to disk
     - Player names for high scores
     - Historical statistics tracking
   - **Reason:** Focused on in-memory implementation first; persistent storage would require file I/O implementation and data serialization

---

## 4.0 New Java Classes

The project introduced **26 new Java classes** organized into structured packages for better maintainability and separation of concerns.

### 4.1 Controller Package (`com.comp2042.controller`)

#### 4.1.1 Main Controllers
   
- **`GameConstants.java`**
  - Location: `src/main/java/com/comp2042/controller/GameConstants.java`
  - Purpose: Centralized constants for board dimensions, game timing, and player numbers. Follows DRY principle and eliminates magic numbers throughout the codebase.

- **`SettingsController.java`**
  - Location: `src/main/java/com/comp2042/controller/SettingsController.java`
  - Purpose: Manages settings panel interactions, including volume control and key binding changes.

- **`PausePanelActionHandler.java`**
  - Location: `src/main/java/com/comp2042/controller/PausePanelActionHandler.java`
  - Purpose: Handles all pause panel button actions (resume, restart, settings, quit) for both single player and multiplayer modes.

#### 4.1.2 Input Handling (`com.comp2042.controller.input`)
- **`InputHandler.java`**
  - Location: `src/main/java/com/comp2042/controller/input/InputHandler.java`
  - Purpose: Processes keyboard input and translates key presses into game actions using the KeyBindingsManager.

#### 4.1.3 Manager Classes (`com.comp2042.controller.manager`)
- **`AudioManager.java`**
  - Location: `src/main/java/com/comp2042/controller/manager/AudioManager.java`
  - Purpose: Manages all audio operations including background music, sound effects, volume control, and mute functionality. Follows Single Responsibility Principle.

- **`CountdownManager.java`**
  - Location: `src/main/java/com/comp2042/controller/manager/CountdownManager.java`
  - Purpose: Manages the 3-2-1 countdown sequence before game start, including audio and visual synchronization.

- **`GameLoopManager.java`**
  - Location: `src/main/java/com/comp2042/controller/manager/GameLoopManager.java`
  - Purpose: Manages game loop timelines for automatic brick dropping. Handles both single player and multiplayer game loops with proper speed calculations based on level.

- **`GameStateManager.java`**
  - Location: `src/main/java/com/comp2042/controller/manager/GameStateManager.java`
  - Purpose: Manages game state transitions and flags (pause, game over, multiplayer states). Centralizes state management logic.

- **`GarbageManager.java`**
  - Location: `src/main/java/com/comp2042/controller/manager/GarbageManager.java`
  - Purpose: Handles garbage block sending and processing in multiplayer mode. Manages garbage queues and triggers garbage insertion when lines are cleared.

- **`MultiplayerViewManager.java`**
  - Location: `src/main/java/com/comp2042/controller/manager/MultiplayerViewManager.java`
  - Purpose: Manages multiplayer view operations including showing multiplayer screen, starting/restarting games, and handling view transitions.

- **`PanelCoordinator.java`**
  - Location: `src/main/java/com/comp2042/controller/manager/PanelCoordinator.java`
  - Purpose: Coordinates visibility and management of various UI panels (pause, game over, settings, main menu). Centralizes panel state management.

- **`SinglePlayerViewManager.java`**
  - Location: `src/main/java/com/comp2042/controller/manager/SinglePlayerViewManager.java`
  - Purpose: Manages single player view operations and UI state transitions.

- **`TimerManager.java`**
  - Location: `src/main/java/com/comp2042/controller/manager/TimerManager.java`
  - Purpose: Manages game timers for tracking elapsed time in both single player and multiplayer modes.

### 4.2 Model Package (`com.comp2042.model`)

- **`GarbageQueue.java`**
  - Location: `src/main/java/com/comp2042/model/GarbageQueue.java`
  - Purpose: Manages a queue of pending garbage lines that will be added to the playfield. Generates garbage lines with random holes and provides queue operations.

- **`HighScoreManager.java`**
  - Location: `src/main/java/com/comp2042/model/HighScoreManager.java`
  - Purpose: Manages high score tracking and leaderboard. Maintains top 3 scores in sorted order.

### 4.3 View Package (`com.comp2042.view`)

- **`MainMenuPanel.java`**
  - Location: `src/main/java/com/comp2042/view/MainMenuPanel.java`
  - Purpose: Main menu screen with buttons for single player, multiplayer, settings, and exit. Provides game mode selection interface.

- **`PausePanel.java`**
  - Location: `src/main/java/com/comp2042/view/PausePanel.java`
  - Purpose: Pause menu overlay with options to resume, restart, open settings, or quit to main menu.

- **`SettingsPanel.java`**
  - Location: `src/main/java/com/comp2042/view/SettingsPanel.java`
  - Purpose: Settings interface with volume slider and key binding customization for all game controls.

- **`SinglePlayerScreen.java`**
  - Location: `src/main/java/com/comp2042/view/SinglePlayerScreen.java`
  - Purpose: Single player game screen layout including game board, next brick preview, hold panel, and statistics display.

- **`MultiplayerScreen.java`**
  - Location: `src/main/java/com/comp2042/view/MultiplayerScreen.java`
  - Purpose: Multiplayer game screen with side-by-side boards for both players, ready system, and synchronized gameplay displays.

- **`WinningPanel.java`**
  - Location: `src/main/java/com/comp2042/view/WinningPanel.java`
  - Purpose: Victory screen shown in multiplayer when one player wins. Displays winner and provides restart/quit options.

- **`GameView.java`**
  - Location: `src/main/java/com/comp2042/view/GameView.java`
  - Purpose: Core game view interface defining contract for all view components.

- **`GameViewRenderer.java`**
  - Location: `src/main/java/com/comp2042/view/GameViewRenderer.java`
  - Purpose: Handles all rendering operations for game boards, bricks, ghost pieces, and UI elements. Centralizes visual rendering logic.

- **`ColorStrategy.java`**
  - Location: `src/main/java/com/comp2042/view/ColorStrategy.java`
  - Purpose: Defines color schemes for different brick types and game elements. Provides consistent color mapping across the application.

### 4.4 Utility Package (`com.comp2042.util`)

- **`KeyBindingsManager.java`**
  - Location: `src/main/java/com/comp2042/util/KeyBindingsManager.java`
  - Purpose: Manages customizable key bindings for all game controls. Handles loading/saving key configurations to properties file. Supports separate bindings for single player and both multiplayer players.

---

## 5.0 Modified Java Classes

The following classes from the original codebase were **significantly refactored and enhanced**:

### 5.1 Relocated to Model Package

1. **`Board.java`**
   - Original Location: `com.comp2042.Board`
   - New Location: `src/main/java/com/comp2042/model/Board.java`
   - **Modifications:**
     - Refactored to implement strategy pattern for board operations
     - Enhanced with hold brick functionality
     - Added support for garbage line insertion (multiplayer)
     - Integrated with new GarbageQueue system
     - Improved collision detection
     - Better separation of concerns

2. **`SimpleBoard.java`**
   - Original Location: `com.comp2042.SimpleBoard`
   - New Location: `src/main/java/com/comp2042/model/SimpleBoard.java`
   - **Modifications:**
     - Enhanced to work with garbage queue system
     - Added methods for garbage line insertion
     - Improved board state management
     - Better integration with new scoring system

3. **`ClearRow.java`**
   - Original Location: `com.comp2042.ClearRow`
   - New Location: `src/main/java/com/comp2042/model/ClearRow.java`
   - **Modifications:**
     - Enhanced scoring calculations for garbage system
     - Returns number of lines cleared for garbage calculation
     - Improved score bonus calculations

4. **`DownData.java`**
   - Original Location: `com.comp2042.DownData`
   - New Location: `src/main/java/com/comp2042/model/DownData.java`
   - **Modifications:** Minimal changes, primarily package relocation for better organization

5. **`NextShapeInfo.java`**
   - Original Location: `com.comp2042.NextShapeInfo`
   - New Location: `src/main/java/com/comp2042/model/NextShapeInfo.java`
   - **Modifications:**
     - Extended to support preview of next 3 bricks instead of just 1
     - Enhanced data structure to hold multiple upcoming bricks

6. **`Score.java`**
   - Original Location: `com.comp2042.Score`
   - New Location: `src/main/java/com/comp2042/model/Score.java`
   - **Modifications:**
     - Enhanced scoring system with level-based multipliers
     - Integration with HighScoreManager
     - Support for different scoring modes

7. **`ViewData.java`**
   - Original Location: `com.comp2042.ViewData`
   - New Location: `src/main/java/com/comp2042/model/ViewData.java`
   - **Modifications:**
     - Extended to include ghost piece position data
     - Added hold brick information
     - Enhanced with multiplayer state data

### 5.2 Relocated to Event Package

8. **`EventSource.java`**
   - Original Location: `com.comp2042.EventSource`
   - New Location: `src/main/java/com/comp2042/event/EventSource.java`
   - **Modifications:** Package relocation for better organization

9. **`EventType.java`**
   - Original Location: `com.comp2042.EventType`
   - New Location: `src/main/java/com/comp2042/event/EventType.java`
   - **Modifications:**
     - Added new event types: HOLD, PAUSE, HARD_DROP
     - Support for multiplayer-specific events

10. **`InputEventListener.java`**
    - Original Location: `com.comp2042.InputEventListener`
    - New Location: `src/main/java/com/comp2042/event/InputEventListener.java`
    - **Modifications:**
      - Added new event handlers: onHoldEvent(), onHardDropEvent()
      - Enhanced to support pause and new game events

11. **`MoveEvent.java`**
    - Original Location: `com.comp2042.MoveEvent`
    - New Location: `src/main/java/com/comp2042/event/MoveEvent.java`
    - **Modifications:** Package relocation with minor enhancements

### 5.3 Relocated to Utility Package

12. **`BrickRotator.java`**
    - Original Location: `com.comp2042.BrickRotator`
    - New Location: `src/main/java/com/comp2042/util/BrickRotator.java`
    - **Modifications:**
      - Enhanced rotation algorithms
      - Better wall kick implementations
      - Support for different brick types

13. **`MatrixOperations.java`**
    - Original Location: `com.comp2042.MatrixOperations`
    - New Location: `src/main/java/com/comp2042/util/MatrixOperations.java`
    - **Modifications:**
      - Added utility methods for garbage line insertion
      - Enhanced matrix manipulation operations
      - Improved performance

### 5.4 Controller Classes

14. **`GameController.java`**
    - Original Location: `com.comp2042.GameController`
    - New Location: `src/main/java/com/comp2042/controller/GameController.java`
    - **Modifications:**
      - Complete restructuring to support both single player and multiplayer modes
      - Added hold brick functionality
      - Integrated with garbage system for multiplayer
      - Enhanced event handling for new game mechanics
      - Support for hard drop and ghost piece
      - Better integration with manager classes
      - Improved game state management

15. **`GuiController.java`**
    - Original Location: `com.comp2042.GuiController`
    - New Location: `src/main/java/com/comp2042/controller/GuiController.java`
    - **Modifications:**
      - Massive refactoring to support new UI architecture
      - Integration with all manager classes (AudioManager, GameStateManager, etc.)
      - Support for main menu, pause menu, settings panel
      - Multiplayer screen management
      - Countdown system integration
      - Timer system integration
      - Enhanced with panel coordinator for better UI flow
      - Separated concerns into manager classes following Single Responsibility Principle
      - Added support for customizable key bindings

### 5.5 View Classes

16. **`GameOverPanel.java`**
    - Original Location: `com.comp2042.GameOverPanel`
    - New Location: `src/main/java/com/comp2042/view/GameOverPanel.java`
    - **Modifications:**
      - Enhanced visual design
      - Added high score display (leaderboard)
      - Time played display
      - Improved button interactions with audio feedback
      - Better integration with game restart flow

17. **`NotificationPanel.java`**
    - Original Location: `com.comp2042.NotificationPanel`
    - New Location: `src/main/java/com/comp2042/view/NotificationPanel.java`
    - **Modifications:**
      - Enhanced visual styling
      - Support for different notification types
      - Improved fade-in/fade-out animations

### 5.6 Main Class

18. **`Main.java`**
    - **Modifications:**
      - Minimal changes to maintain entry point
      - Updated to use GuiController with new architecture

### 5.7 Logic Package (Minimal Changes)

The brick classes in `com.comp2042.logic.bricks` package were **relocated but not significantly modified**:
- `Brick.java`
- `IBrick.java`, `JBrick.java`, `LBrick.java`, `OBrick.java`, `SBrick.java`, `TBrick.java`, `ZBrick.java`
- `BrickGenerator.java`
- `RandomBrickGenerator.java`

**Changes:** Package relocation from `com.comp2042` to `com.comp2042.logic.bricks` for better organization. Core brick logic remains largely unchanged.

---

## 6.0 Unexpected Problems
### 6.1 Unexpected Problems & Solutions

During the development of this enhanced Tetris game, several unexpected challenges were encountered:

1. **Multiplayer State Synchronization**
   - **Problem:** Coordinating game state between two independent game controllers in multiplayer mode proved challenging. Issues arose with ensuring both players start simultaneously, pause/unpause together, and handle game over conditions correctly.
   - **Solution:** Created a centralized `GameStateManager` class to coordinate state between both players. Implemented synchronized ready system and countdown mechanism to ensure both players start at the same time. Added flags and callbacks to ensure pause states are properly synchronized.

2. **Garbage Queue Processing Timing**
   - **Problem:** Initially, garbage lines were added to the opponent's board immediately upon line clears, which felt unfair and didn't allow players time to react. The immediate insertion also caused visual glitches when multiple garbage lines were sent rapidly.
   - **Solution:** Implemented a queue-based system with delayed processing using JavaFX Timeline. Garbage lines are now queued and processed gradually (one line every 2 seconds) to give players time to clear lines and reduce pending garbage. This required careful coordination between `GarbageManager`, `GarbageQueue`, and `GameLoopManager`.

3. **Key Binding Conflicts in Multiplayer**
   - **Problem:** When implementing multiplayer mode, conflicts arose between Player 1 and Player 2 key bindings. Some keys were being captured by both players or not properly routed to the correct player's controller.
   - **Solution:** Refactored input handling to use `KeyBindingsManager` with separate binding sets for single player, Player 1, and Player 2. Implemented `InputHandler` to properly route key events to the correct player based on the current game mode and active bindings.

4. **Panel Visibility and State Management**
   - **Problem:** Managing visibility of multiple overlapping panels (main menu, pause panel, game over panel, settings panel, winning panel) led to z-index issues and panels appearing/disappearing incorrectly during state transitions.
   - **Solution:** Created `PanelCoordinator` class to centralize panel visibility management. Implemented a stack-based approach where panels are properly layered and only one panel is visible at a time. Added explicit show/hide methods for all panel transitions.

5. **Game Loop Timeline Management**
   - **Problem:** Managing multiple JavaFX Timelines (one for each player in multiplayer, plus garbage processing timelines) caused issues with proper cleanup when restarting games or switching between single player and multiplayer modes. Timelines were sometimes not properly stopped, causing memory leaks and unexpected behavior.
   - **Solution:** Centralized timeline management in `GameLoopManager` with explicit start/stop methods. Added proper cleanup in `GameStateManager` to ensure all timelines are stopped during game over, pause, and restart operations. Implemented callback patterns to coordinate timeline lifecycle across managers.

6. **Audio Resource Loading**
   - **Problem:** Audio files sometimes failed to load or play, especially when the application was run from different directories or when resources were packaged in JAR files.
   - **Solution:** Used `getClass().getResource()` with proper resource paths to ensure audio files are loaded from the classpath correctly. Added error handling in `AudioManager` to gracefully handle missing audio files without crashing the application.

---

## 7.0 Testing

### 7.1 Unit Tests
The project includes comprehensive unit tests for core model and utility classes:

- **`ClearRowTest.java`** - Tests row clearing logic and score calculations
- **`DownDataTest.java`** - Tests down movement data handling
- **`ScoreTest.java`** - Tests scoring system and level progression
- **`SimpleBoardTest.java`** - Tests board operations and brick placement
- **`ViewDataTest.java`** - Tests view data encapsulation
- **`BrickRotatorTest.java`** - Tests brick rotation algorithms
- **`MatrixOperationsTest.java`** - Tests matrix manipulation utilities

---

## 8.0 Project Structure

```
CW2025/
├── src/
│   ├── main/
│   │   ├── java/com/comp2042/
│   │   │   ├── controller/                    # Game controllers and managers
│   │   │   │   ├── input/                    # Input handling
│   │   │   │   │   └── InputHandler.java    # Keyboard input processing
│   │   │   │   ├── manager/                  # Manager classes
│   │   │   │   │   ├── AudioManager.java    # Audio playback and volume control
│   │   │   │   │   ├── CountdownManager.java # Pre-game countdown system
│   │   │   │   │   ├── GameLoopManager.java  # Game loop and timeline management
│   │   │   │   │   ├── GameStateManager.java # Game state coordination
│   │   │   │   │   ├── GarbageManager.java   # Multiplayer garbage system
│   │   │   │   │   ├── MultiplayerViewManager.java # Multiplayer UI management
│   │   │   │   │   ├── PanelCoordinator.java # Panel visibility coordination
│   │   │   │   │   ├── SinglePlayerViewManager.java # Single player UI management
│   │   │   │   │   └── TimerManager.java     # Game timer tracking
│   │   │   │   ├── GameConstants.java       # Centralized game constants
│   │   │   │   ├── GameController.java      # Core game logic controller
│   │   │   │   ├── GuiController.java       # Main GUI coordinator
│   │   │   │   ├── PausePanelActionHandler.java # Pause menu actions
│   │   │   │   └── SettingsController.java  # Settings panel controller
│   │   │   ├── event/                        # Event system
│   │   │   │   ├── EventSource.java         # Event source enumeration
│   │   │   │   ├── EventType.java           # Event type enumeration
│   │   │   │   ├── InputEventListener.java  # Input event listener interface
│   │   │   │   └── MoveEvent.java           # Move event data class
│   │   │   ├── logic/                        # Game logic
│   │   │   │   ├── bricks/                  # Brick type implementations
│   │   │   │   │   ├── Brick.java           # Base brick class
│   │   │   │   │   ├── IBrick.java          # I-shaped brick
│   │   │   │   │   ├── JBrick.java          # J-shaped brick
│   │   │   │   │   ├── LBrick.java          # L-shaped brick
│   │   │   │   │   ├── OBrick.java          # O-shaped brick
│   │   │   │   │   ├── SBrick.java          # S-shaped brick
│   │   │   │   │   ├── TBrick.java          # T-shaped brick
│   │   │   │   │   └── ZBrick.java          # Z-shaped brick
│   │   │   │   ├── BrickGenerator.java      # Brick generation interface
│   │   │   │   └── RandomBrickGenerator.java # Random brick generator
│   │   │   ├── model/                       # Data models
│   │   │   │   ├── Board.java               # Board interface
│   │   │   │   ├── ClearRow.java            # Row clearing data
│   │   │   │   ├── DownData.java            # Down movement data
│   │   │   │   ├── GarbageQueue.java        # Garbage line queue
│   │   │   │   ├── HighScoreManager.java    # High score tracking
│   │   │   │   ├── NextShapeInfo.java       # Next brick preview data
│   │   │   │   ├── Score.java               # Score tracking
│   │   │   │   ├── SimpleBoard.java         # Board implementation
│   │   │   │   └── ViewData.java            # View data encapsulation
│   │   │   ├── util/                        # Utility classes
│   │   │   │   ├── BrickRotator.java        # Brick rotation algorithms
│   │   │   │   ├── KeyBindingsManager.java  # Key binding management
│   │   │   │   └── MatrixOperations.java    # Matrix manipulation utilities
│   │   │   ├── view/                        # UI components
│   │   │   │   ├── ColorStrategy.java       # Color scheme definitions
│   │   │   │   ├── GameOverPanel.java       # Game over screen
│   │   │   │   ├── GameView.java            # Game view interface
│   │   │   │   ├── GameViewRenderer.java    # Rendering operations
│   │   │   │   ├── MainMenuPanel.java       # Main menu screen
│   │   │   │   ├── MultiplayerScreen.java   # Multiplayer game screen
│   │   │   │   ├── NotificationPanel.java   # Notification display
│   │   │   │   ├── PausePanel.java          # Pause menu
│   │   │   │   ├── SettingsPanel.java        # Settings interface
│   │   │   │   ├── SinglePlayerScreen.java  # Single player game screen
│   │   │   │   └── WinningPanel.java        # Multiplayer victory screen
│   │   │   └── Main.java                    # Application entry point
│   │   └── resources/
│   │       ├── audio/                       # Sound effects and music
│   │       │   ├── 3-2-1-countdown.mp3
│   │       │   ├── A-Type Music (Korobeiniki).mp3
│   │       │   ├── click-button.mp3
│   │       │   ├── Game Over.mp3
│   │       │   ├── garbage.mp3
│   │       │   ├── hover.mp3
│   │       │   ├── level-up.mp3
│   │       │   ├── Stage Clear.mp3
│   │       │   ├── tetris-party-deluxe-main-menu-music.mp3
│   │       │   └── winners_W9Cpenj.mp3
│   │       ├── font/                        # Custom fonts
│   │       │   ├── digital.ttf
│   │       │   └── PublicPixel-rv0pA.ttf
│   │       ├── background_image.png         # Background image
│   │       ├── digital.ttf                  # Digital font (legacy location)
│   │       ├── gameLayout.fxml              # JavaFX UI layout
│   │       ├── PublicPixel-rv0pA.ttf        # Pixel font (legacy location)
│   │       └── window_style.css             # CSS styling
│   └── test/
│       └── java/com/comp2042/
│           ├── controller/
│           │   ├── manager/
│           │   │   └── GarbageManagerTest.java
│           │   └── GameControllerTest.java
│           ├── model/
│           │   ├── ClearRowTest.java
│           │   ├── DownDataTest.java
│           │   ├── GarbageQueueTest.java
│           │   ├── ScoreTest.java
│           │   ├── SimpleBoardTest.java
│           │   └── ViewDataTest.java
│           └── util/
│               ├── BrickRotatorTest.java
│               └── MatrixOperationsTest.java
├── target/                                 # Maven build output (generated)
│   ├── classes/                            # Compiled classes
│   ├── test-classes/                       # Compiled test classes
│   └── generated-sources/                 # Generated source files
├── pom.xml                                 # Maven project configuration
├── keybindings.properties                  # Key binding configuration file
├── mvnw                                    # Maven wrapper (Unix)
├── mvnw.cmd                                # Maven wrapper (Windows)
└── README.md                               # This documentation file
```

### 8.1 Package Organization

The project follows a layered architecture with clear separation of concerns:

- **`controller`**: Contains all game controllers and manager classes that coordinate game logic, UI, and system resources
- **`event`**: Implements the event-driven architecture for input handling and game actions
- **`logic`**: Contains core game logic including brick types and generation algorithms
- **`model`**: Data models representing game state, board, scores, and game entities
- **`util`**: Utility classes for common operations (rotation, matrix operations, key bindings)
- **`view`**: JavaFX UI components and rendering logic
- **`bricks`**: Concrete implementations of all Tetris brick types (I, J, L, O, S, T, Z)

### 8.2 Resource Files

- **Audio Files**: 10 audio files including background music, sound effects, and countdown audio
- **Fonts**: 2 custom fonts (Digital and Public Pixel) for retro game aesthetics
- **UI Resources**: FXML layout file and CSS stylesheet for JavaFX UI
- **Configuration**: `keybindings.properties` stores customizable key bindings persistently

