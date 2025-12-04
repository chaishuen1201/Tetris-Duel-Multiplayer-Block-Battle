package com.comp2042.controller.manager;

import com.comp2042.controller.GameController;
import com.comp2042.event.InputEventListener;
import com.comp2042.view.MultiplayerScreen;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Manages game state transitions and flags for both single player and multiplayer modes.
 * This class coordinates all game state changes including pause/resume, game start/restart,
 * game over conditions, and mode switching. It maintains state flags using JavaFX properties
 * for reactive UI binding, manages timeline playback (pause/play/stop), coordinates audio
 * playback, timer management, and UI panel visibility. The manager also handles winning
 * panel display in multiplayer mode and provides callbacks for UI updates. It serves as
 * a central coordinator for game state, ensuring consistent state transitions and proper
 * resource management across different game modes.
 */
public class GameStateManager {
    
    // Game state flags
    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver1 = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver2 = new SimpleBooleanProperty(false);
    private boolean gameStarted = false;
    private boolean isMultiplayerMode = false;
    
    // Dependencies
    private final AudioManager audioManager;
    private final TimerManager timerManager;
    
    // References to game components (set via setters)
    private Timeline timeLine;
    private Timeline timeLine1;
    private Timeline timeLine2;
    private Timeline garbageProcessTimeline1;
    private Timeline garbageProcessTimeline2;
    private GameController gameController1;
    private GameController gameController2;
    private InputEventListener eventListener;
    private InputEventListener eventListener1;
    private InputEventListener eventListener2;
    private MultiplayerScreen multiplayerScreen;
    
    // Callbacks for winning panel actions
    private Runnable onRestartGame;
    private Runnable onQuitToMenu;
    
    // Callbacks for UI updates (set via setters)
    private Runnable onShowPausePanel;
    private Runnable onHidePausePanel;
    private Runnable onShowMultiplayerPausePanel;
    private Runnable onHideMultiplayerPausePanel;
    private Runnable onStartGarbageProcessingTimelines;
    private Runnable onStopGarbageProcessingTimelines;
    private Runnable onUpdateTimelineRate;
    private Runnable onUpdateTimelineRate1;
    private Runnable onUpdateTimelineRate2;
    private Runnable onShowGameOverPanel;
    private Runnable onHideGameOverPanel;
    private Runnable onShowMainMenu;
    private Runnable onHideMainMenu;
    private Runnable onRequestFocus;
    
    /**
     * Creates a new GameStateManager with the specified dependencies.
     * 
     * @param audioManager The audio manager for controlling game music and sound effects
     * @param timerManager The timer manager for managing game timers
     */
    public GameStateManager(AudioManager audioManager, TimerManager timerManager) {
        this.audioManager = audioManager;
        this.timerManager = timerManager;
    }
    
    // Getters for state properties
    /**
     * Gets the pause state property for reactive binding.
     * 
     * @return The BooleanProperty representing the pause state
     */
    public BooleanProperty isPauseProperty() {
        return isPause;
    }
    
    /**
     * Checks if the game is currently paused.
     * 
     * @return true if the game is paused, false otherwise
     */
    public boolean isPaused() {
        return isPause.get();
    }
    
    /**
     * Gets the game over state property for single player mode (for reactive binding).
     * 
     * @return The BooleanProperty representing the game over state
     */
    public BooleanProperty isGameOverProperty() {
        return isGameOver;
    }
    
    /**
     * Checks if the single player game is over.
     * 
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return isGameOver.get();
    }
    
    /**
     * Gets the game over state property for player 1 in multiplayer mode (for reactive binding).
     * 
     * @return The BooleanProperty representing player 1's game over state
     */
    public BooleanProperty isGameOver1Property() {
        return isGameOver1;
    }
    
    /**
     * Checks if player 1's game is over in multiplayer mode.
     * 
     * @return true if player 1 has lost, false otherwise
     */
    public boolean isGameOver1() {
        return isGameOver1.get();
    }
    
    /**
     * Gets the game over state property for player 2 in multiplayer mode (for reactive binding).
     * 
     * @return The BooleanProperty representing player 2's game over state
     */
    public BooleanProperty isGameOver2Property() {
        return isGameOver2;
    }
    
    /**
     * Checks if player 2's game is over in multiplayer mode.
     * 
     * @return true if player 2 has lost, false otherwise
     */
    public boolean isGameOver2() {
        return isGameOver2.get();
    }
    
    /**
     * Checks if the game has started.
     * 
     * @return true if the game has started, false if it's in the initial/ready state
     */
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    /**
     * Checks if the game is in multiplayer mode.
     * 
     * @return true if in multiplayer mode, false for single player mode
     */
    public boolean isMultiplayerMode() {
        return isMultiplayerMode;
    }
    
    // Setters for game components
    /**
     * Sets the single player game loop timeline.
     * 
     * @param timeLine The Timeline for single player game loop
     */
    public void setTimeLine(Timeline timeLine) {
        this.timeLine = timeLine;
    }
    
    /**
     * Sets the player 1 game loop timeline for multiplayer mode.
     * 
     * @param timeLine1 The Timeline for player 1's game loop
     */
    public void setTimeLine1(Timeline timeLine1) {
        this.timeLine1 = timeLine1;
    }
    
    /**
     * Sets the player 2 game loop timeline for multiplayer mode.
     * 
     * @param timeLine2 The Timeline for player 2's game loop
     */
    public void setTimeLine2(Timeline timeLine2) {
        this.timeLine2 = timeLine2;
    }
    
    /**
     * Sets the garbage processing timeline for player 1 in multiplayer mode.
     * 
     * @param garbageProcessTimeline1 The Timeline for processing player 1's garbage queue
     */
    public void setGarbageProcessTimeline1(Timeline garbageProcessTimeline1) {
        this.garbageProcessTimeline1 = garbageProcessTimeline1;
    }
    
    /**
     * Sets the garbage processing timeline for player 2 in multiplayer mode.
     * 
     * @param garbageProcessTimeline2 The Timeline for processing player 2's garbage queue
     */
    public void setGarbageProcessTimeline2(Timeline garbageProcessTimeline2) {
        this.garbageProcessTimeline2 = garbageProcessTimeline2;
    }
    
    /**
     * Sets the game controller for player 1 in multiplayer mode.
     * 
     * @param gameController1 The GameController for player 1
     */
    public void setGameController1(GameController gameController1) {
        this.gameController1 = gameController1;
    }
    
    /**
     * Sets the game controller for player 2 in multiplayer mode.
     * 
     * @param gameController2 The GameController for player 2
     */
    public void setGameController2(GameController gameController2) {
        this.gameController2 = gameController2;
    }
    
    /**
     * Sets the event listener for single player mode.
     * 
     * @param eventListener The InputEventListener for single player
     */
    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }
    
    /**
     * Sets the event listener for player 1 in multiplayer mode.
     * 
     * @param eventListener1 The InputEventListener for player 1
     */
    public void setEventListener1(InputEventListener eventListener1) {
        this.eventListener1 = eventListener1;
    }
    
    /**
     * Sets the event listener for player 2 in multiplayer mode.
     * 
     * @param eventListener2 The InputEventListener for player 2
     */
    public void setEventListener2(InputEventListener eventListener2) {
        this.eventListener2 = eventListener2;
    }
    
    // Setters for callbacks
    /**
     * Sets the callback to show the single player pause panel.
     * 
     * @param callback The Runnable to execute when showing the pause panel
     */
    public void setOnShowPausePanel(Runnable callback) {
        this.onShowPausePanel = callback;
    }
    
    /**
     * Sets the callback to hide the single player pause panel.
     * 
     * @param callback The Runnable to execute when hiding the pause panel
     */
    public void setOnHidePausePanel(Runnable callback) {
        this.onHidePausePanel = callback;
    }
    
    /**
     * Sets the callback to show the multiplayer pause panel.
     * 
     * @param callback The Runnable to execute when showing the multiplayer pause panel
     */
    public void setOnShowMultiplayerPausePanel(Runnable callback) {
        this.onShowMultiplayerPausePanel = callback;
    }
    
    /**
     * Sets the callback to hide the multiplayer pause panel.
     * 
     * @param callback The Runnable to execute when hiding the multiplayer pause panel
     */
    public void setOnHideMultiplayerPausePanel(Runnable callback) {
        this.onHideMultiplayerPausePanel = callback;
    }
    
    /**
     * Sets the callback to start garbage processing timelines in multiplayer mode.
     * 
     * @param callback The Runnable to execute when starting garbage processing
     */
    public void setOnStartGarbageProcessingTimelines(Runnable callback) {
        this.onStartGarbageProcessingTimelines = callback;
    }
    
    /**
     * Sets the callback to stop garbage processing timelines in multiplayer mode.
     * 
     * @param callback The Runnable to execute when stopping garbage processing
     */
    public void setOnStopGarbageProcessingTimelines(Runnable callback) {
        this.onStopGarbageProcessingTimelines = callback;
    }
    
    /**
     * Sets the callback to update the single player timeline rate.
     * 
     * @param callback The Runnable to execute when updating timeline rate
     */
    public void setOnUpdateTimelineRate(Runnable callback) {
        this.onUpdateTimelineRate = callback;
    }
    
    /**
     * Sets the callback to update player 1's timeline rate in multiplayer mode.
     * 
     * @param callback The Runnable to execute when updating player 1's timeline rate
     */
    public void setOnUpdateTimelineRate1(Runnable callback) {
        this.onUpdateTimelineRate1 = callback;
    }
    
    /**
     * Sets the callback to update player 2's timeline rate in multiplayer mode.
     * 
     * @param callback The Runnable to execute when updating player 2's timeline rate
     */
    public void setOnUpdateTimelineRate2(Runnable callback) {
        this.onUpdateTimelineRate2 = callback;
    }
    
    /**
     * Sets the callback to show the game over panel.
     * 
     * @param callback The Runnable to execute when showing the game over panel
     */
    public void setOnShowGameOverPanel(Runnable callback) {
        this.onShowGameOverPanel = callback;
    }
    
    /**
     * Sets the callback to hide the game over panel.
     * 
     * @param callback The Runnable to execute when hiding the game over panel
     */
    public void setOnHideGameOverPanel(Runnable callback) {
        this.onHideGameOverPanel = callback;
    }
    
    /**
     * Sets the callback to show the main menu.
     * 
     * @param callback The Runnable to execute when showing the main menu
     */
    public void setOnShowMainMenu(Runnable callback) {
        this.onShowMainMenu = callback;
    }
    
    /**
     * Sets the callback to hide the main menu.
     * 
     * @param callback The Runnable to execute when hiding the main menu
     */
    public void setOnHideMainMenu(Runnable callback) {
        this.onHideMainMenu = callback;
    }
    
    /**
     * Sets the callback to request focus on the game window.
     * 
     * @param callback The Runnable to execute when requesting focus
     */
    public void setOnRequestFocus(Runnable callback) {
        this.onRequestFocus = callback;
    }
    
    /**
     * Pauses or resumes the game for both single player and multiplayer modes.
     * Toggles the pause state, pausing/resuming timelines, stopping/playing game music,
     * pausing/resuming timers, and showing/hiding pause panels. Only works when the game
     * has started and is not in a game over state. For multiplayer, pauses/resumes both
     * players' timelines and garbage processing timelines.
     */
    public void pauseGame() {
        // Only allow pause/resume when game is actually playing
        if (!gameStarted) {
            return;
        }
        
        // Check if game is over - if so, don't allow pause
        if (isMultiplayerMode) {
            if (isGameOver1.get() || isGameOver2.get()) {
                return;
            }
        } else {
            if (isGameOver.get()) {
                return;
            }
        }
        
        if (isMultiplayerMode) {
            if (!isPause.get()) {
                // Pause the game
                if (timeLine1 != null) timeLine1.pause();
                if (timeLine2 != null) timeLine2.pause();
                audioManager.stopGameMusic();
                timerManager.pauseMultiplayerTimer();
                isPause.set(true);
                if (garbageProcessTimeline1 != null) garbageProcessTimeline1.pause();
                if (garbageProcessTimeline2 != null) garbageProcessTimeline2.pause();
                if (onShowMultiplayerPausePanel != null) onShowMultiplayerPausePanel.run();
            } else {
                // Resume the game
                if (timeLine1 != null) {
                    timeLine1.play();
                    if (onUpdateTimelineRate1 != null) onUpdateTimelineRate1.run();
                }
                if (timeLine2 != null) {
                    timeLine2.play();
                    if (onUpdateTimelineRate2 != null) onUpdateTimelineRate2.run();
                }
                audioManager.playGameMusic();
                timerManager.resumeMultiplayerTimer();
                isPause.set(false);
                if (garbageProcessTimeline1 != null) garbageProcessTimeline1.play();
                if (garbageProcessTimeline2 != null) garbageProcessTimeline2.play();
                if (onHideMultiplayerPausePanel != null) onHideMultiplayerPausePanel.run();
            }
        } else {
            if (!isPause.get()) {
                // Pause the game
                if (timeLine != null) timeLine.pause();
                audioManager.stopGameMusic();
                timerManager.pauseSinglePlayerTimer();
                isPause.set(true);
                if (onShowPausePanel != null) onShowPausePanel.run();
            } else {
                // Resume the game
                if (timeLine != null) {
                    timeLine.play();
                    if (onUpdateTimelineRate != null) onUpdateTimelineRate.run();
                }
                audioManager.playGameMusic();
                timerManager.resumeSinglePlayerTimer();
                isPause.set(false);
                if (onHidePausePanel != null) onHidePausePanel.run();
            }
        }
        if (onRequestFocus != null) onRequestFocus.run();
    }
    
    /**
     * Initiates the start of a new single player game.
     * Hides the main menu if the game hasn't started yet. The actual game start
     * (after countdown) is handled by actuallyStartGame().
     */
    public void startGame() {
        if (!gameStarted && onHideMainMenu != null) {
            onHideMainMenu.run();
            // Countdown will be handled by GuiController
        }
    }
    
    /**
     * Actually starts the game after countdown completes (called by GuiController).
     * Sets game state flags, starts the game timeline, plays game music, starts
     * the single player timer, and requests focus on the game window.
     */
    public void actuallyStartGame() {
        gameStarted = true;
        isGameOver.set(false);
        isPause.set(false);
        
        audioManager.playGameMusic();
        
        if (timeLine != null) {
            timeLine.play();
        }
        
        timerManager.startSinglePlayerTimer();
        
        if (onRequestFocus != null) onRequestFocus.run();
    }
    
    /**
     * Starts a new game (restart from game over or pause menu).
     * Stops the current timeline, hides game over panel, resets the timer, plays
     * game music, creates a new game through the event listener, restarts the timeline,
     * resets game state flags, and hides panels. Used for restarting a game without
     * going through the countdown sequence.
     */
    public void newGame() {
        if (timeLine != null) timeLine.stop();
        if (onHideGameOverPanel != null) onHideGameOverPanel.run();
        
        timerManager.resetSinglePlayerTimer();
        
        audioManager.playGameMusic();
        
        if (eventListener != null) eventListener.createNewGame();
        
        if (timeLine != null) timeLine.play();
        
        timerManager.startSinglePlayerTimer();
        
        isPause.set(false);
        isGameOver.set(false);
        gameStarted = true;
        
        if (onHideMainMenu != null) onHideMainMenu.run();
        if (onHidePausePanel != null) onHidePausePanel.run();
        
        if (onRequestFocus != null) onRequestFocus.run();
    }
    
    /**
     * Handles game over condition for single player or multiplayer mode.
     * Stops timelines, timers, and game music. For multiplayer, determines the winner
     * if only one player loses, or handles a tie if both players lose. Shows appropriate
     * panels (game over or winning panel) and plays appropriate sound effects.
     * 
     * @param playerNumber The player number (0 for single player, 1 or 2 for multiplayer)
     */
    public void gameOver(int playerNumber) {
        if (isMultiplayerMode && playerNumber > 0) {
            // Handle multiplayer game over
            if (playerNumber == 1) {
                if (timeLine1 != null) timeLine1.stop();
                isGameOver1.set(true);
                if (timeLine2 != null) timeLine2.stop();
            } else if (playerNumber == 2) {
                if (timeLine2 != null) timeLine2.stop();
                isGameOver2.set(true);
                if (timeLine1 != null) timeLine1.stop();
            }
            
            if (onStopGarbageProcessingTimelines != null) {
                onStopGarbageProcessingTimelines.run();
            }
            
            // Stop multiplayer timer when game ends
            timerManager.stopMultiplayerTimer();
            
            if (onHideMultiplayerPausePanel != null) onHideMultiplayerPausePanel.run();
            if (onHidePausePanel != null) onHidePausePanel.run();
            isPause.set(false);
            
            audioManager.stopGameMusic();
            
            // If only one player is game over, show winning panel for the other player
            if (isGameOver1.get() && !isGameOver2.get()) {
                isGameOver2.set(true);
                int winnerPlayerNumber = 2;
                int timeUsed = timerManager.getMultiplayerElapsedSeconds();
                showWinningPanel(winnerPlayerNumber, timeUsed);
                audioManager.playWinner();
            } else if (isGameOver2.get() && !isGameOver1.get()) {
                isGameOver1.set(true);
                int winnerPlayerNumber = 1;
                int timeUsed = timerManager.getMultiplayerElapsedSeconds();
                showWinningPanel(winnerPlayerNumber, timeUsed);
                audioManager.playWinner();
            } else if (isGameOver1.get() && isGameOver2.get()) {
                hideWinningPanel();
                audioManager.playGameOver();
            }
            return;
        }
        
        // Single player game over
        if (timeLine != null) timeLine.stop();
        
        timerManager.stopSinglePlayerTimer();
        
        audioManager.stopGameMusic();
        audioManager.playGameOver();
        
        if (onHidePausePanel != null) onHidePausePanel.run();
        if (onHideMultiplayerPausePanel != null) onHideMultiplayerPausePanel.run();
        isPause.set(false);
        
        if (onShowGameOverPanel != null) onShowGameOverPanel.run();
        
        isGameOver.set(true);
    }
    
    /**
     * Starts a multiplayer game after countdown completes.
     * Resets game over states, sets game started flag, hides pause panels,
     * starts both players' timelines, starts garbage processing timelines,
     * starts the multiplayer timer, and plays game music.
     */
    public void startMultiplayerGame() {
        // Reset game over states
        isGameOver1.set(false);
        isGameOver2.set(false);
        
        // Start the game
        gameStarted = true;
        isPause.set(false);
        
        // Hide pause panel
        if (onHideMultiplayerPausePanel != null) onHideMultiplayerPausePanel.run();
        if (onHidePausePanel != null) onHidePausePanel.run();
        
        // Start both timelines
        if (timeLine1 != null) {
            timeLine1.play();
        }
        if (timeLine2 != null) {
            timeLine2.play();
        }
        
        // Start garbage processing timelines
        if (onStartGarbageProcessingTimelines != null) {
            onStartGarbageProcessingTimelines.run();
        }
        
        // Start multiplayer timer
        timerManager.startMultiplayerTimer();
        
        // Start game music
        audioManager.playGameMusic();
    }
    
    /**
     * Restarts a multiplayer game from the winning panel.
     * Creates new games for both players, resets game over states, hides the
     * winning panel, restarts all timelines and garbage processing, resets and
     * restarts the multiplayer timer, and plays game music.
     */
    public void restartMultiplayerGame() {
        // Restart both games
        if (gameController1 != null) {
            gameController1.createNewGame();
        }
        if (gameController2 != null) {
            gameController2.createNewGame();
        }
        
        // Reset game over states
        isGameOver1.set(false);
        isGameOver2.set(false);
        gameStarted = true;
        isPause.set(false);
        
        // Hide winning panel
        hideWinningPanel();
        
        // Restart timelines
        if (timeLine1 != null) {
            timeLine1.stop();
            timeLine1.play();
        }
        if (timeLine2 != null) {
            timeLine2.stop();
            timeLine2.play();
        }
        
        // Restart garbage processing timelines
        if (onStartGarbageProcessingTimelines != null) {
            onStartGarbageProcessingTimelines.run();
        }
        
        // Restart multiplayer timer
        timerManager.resetMultiplayerTimer();
        timerManager.startMultiplayerTimer();
        
        // Restart music
        audioManager.playGameMusic();
    }
    
    /**
     * Quits to main menu and resets all game states.
     * Stops all timelines, stops and resets all timers, resets all state flags,
     * hides all panels, stops game music, plays main menu music, and shows the main menu.
     * This method provides a complete reset to return to the initial application state.
     */
    public void quitToMainMenu() {
        // Stop all timelines
        if (timeLine != null) timeLine.stop();
        if (timeLine1 != null) timeLine1.stop();
        if (timeLine2 != null) timeLine2.stop();
        if (onStopGarbageProcessingTimelines != null) {
            onStopGarbageProcessingTimelines.run();
        }
        
        // Stop and reset timers
        timerManager.stopMultiplayerTimer();
        timerManager.resetMultiplayerTimer();
        timerManager.stopSinglePlayerTimer();
        timerManager.resetSinglePlayerTimer();
        
        // Reset pause state
        isPause.set(false);
        
        // Reset game states
        isGameOver.set(false);
        isGameOver1.set(false);
        isGameOver2.set(false);
        gameStarted = false;
        isMultiplayerMode = false;
        
        // Hide panels
        hideWinningPanel();
        if (onHideGameOverPanel != null) onHideGameOverPanel.run();
        if (onHidePausePanel != null) onHidePausePanel.run();
        if (onHideMultiplayerPausePanel != null) onHideMultiplayerPausePanel.run();
        
        // Stop game music and play main menu music
        audioManager.stopAll();
        audioManager.playMainMenuMusic();
        
        // Show main menu
        if (onShowMainMenu != null) onShowMainMenu.run();
    }
    
    /**
     * Resets all game states to initial values.
     * Sets all pause, game over, game started, and multiplayer mode flags to false.
     * Does not stop timelines or timers - use quitToMainMenu() for a complete reset.
     */
    public void resetGameStates() {
        isPause.set(false);
        isGameOver.set(false);
        isGameOver1.set(false);
        isGameOver2.set(false);
        gameStarted = false;
        isMultiplayerMode = false;
    }
    
    /**
     * Sets the multiplayer mode flag.
     * 
     * @param isMultiplayerMode true to enable multiplayer mode, false for single player mode
     */
    public void setMultiplayerMode(boolean isMultiplayerMode) {
        this.isMultiplayerMode = isMultiplayerMode;
    }
    
    /**
     * Clears all multiplayer references (controllers, listeners, timelines).
     * Sets all multiplayer-specific component references to null. Used when
     * switching from multiplayer mode back to single player mode to free resources.
     */
    public void clearMultiplayerReferences() {
        gameController1 = null;
        gameController2 = null;
        eventListener1 = null;
        eventListener2 = null;
        timeLine1 = null;
        timeLine2 = null;
        garbageProcessTimeline1 = null;
        garbageProcessTimeline2 = null;
    }
    
    /**
     * Sets the MultiplayerScreen reference for winning panel management.
     * 
     * @param screen The MultiplayerScreen instance containing winning panel components
     */
    public void setMultiplayerScreen(MultiplayerScreen screen) {
        this.multiplayerScreen = screen;
    }
    
    /**
     * Sets the callback for restarting the game from the winning panel.
     * 
     * @param callback The Runnable to execute when restart button is clicked
     */
    public void setOnRestartGame(Runnable callback) {
        this.onRestartGame = callback;
    }
    
    /**
     * Sets the callback for quitting to main menu from the winning panel.
     * 
     * @param callback The Runnable to execute when quit to menu button is clicked
     */
    public void setOnQuitToMenu(Runnable callback) {
        this.onQuitToMenu = callback;
    }
    
    // ========== Winning Panel Management ==========
    
    /**
     * Shows the winning panel for multiplayer game.
     * 
     * @param winnerPlayerNumber The player number who won (1 or 2)
     * @param timeUsed The time used in seconds
     */
    public void showWinningPanel(int winnerPlayerNumber, int timeUsed) {
        if (multiplayerScreen == null) {
            return;
        }
        
        com.comp2042.view.WinningPanel winningPanel = multiplayerScreen.getWinningPanel();
        javafx.scene.layout.StackPane winningOverlay = multiplayerScreen.getWinningOverlay();
        
        if (winningOverlay == null || winningPanel == null) {
            return;
        }
        
        winningPanel.setWinner(winnerPlayerNumber);
        winningPanel.setTimeUsed(timeUsed);
        winningOverlay.setVisible(true);
        winningOverlay.setManaged(true);
        winningOverlay.setMouseTransparent(false);
        
        javafx.scene.layout.StackPane wrapper = multiplayerScreen.getWrapper();
        if (wrapper != null) {
            // Ensure winning overlay is in the wrapper and brought to front
            if (wrapper.getChildren().contains(winningOverlay)) {
                wrapper.getChildren().remove(winningOverlay);
            }
            wrapper.getChildren().add(winningOverlay);
            wrapper.setManaged(true);
        }
        
        javafx.scene.layout.HBox container = multiplayerScreen.getContainer();
        if (container != null) {
            container.setManaged(true);
        }
        
        // Setup winning panel actions if not already set
        setupWinningPanelActions(winningPanel);
    }
    
    /**
     * Hides the winning panel overlay in multiplayer mode.
     * Makes the overlay invisible, unmanaged, and mouse-transparent.
     */
    public void hideWinningPanel() {
        if (multiplayerScreen == null) {
            return;
        }
        
        javafx.scene.layout.StackPane winningOverlay = multiplayerScreen.getWinningOverlay();
        if (winningOverlay != null) {
            winningOverlay.setVisible(false);
            winningOverlay.setManaged(false);
            winningOverlay.setMouseTransparent(true);
        }
    }
    
    /**
     * Sets up the winning panel action handlers for restart and quit to menu buttons.
     * Configures button click handlers and sets up button sound effects.
     * 
     * @param winningPanel The WinningPanel instance to set up
     */
    private void setupWinningPanelActions(com.comp2042.view.WinningPanel winningPanel) {
        if (winningPanel == null) {
            return;
        }
        
        winningPanel.setOnRestartAction(() -> {
            if (onRestartGame != null) {
                onRestartGame.run();
            }
        });
        
        winningPanel.setOnMainMenuAction(() -> {
            if (onQuitToMenu != null) {
                onQuitToMenu.run();
            }
        });
        
        // Set up button sounds
        winningPanel.setupButtonSounds(audioManager);
    }
    
    // ========== Default Values for Score/Level/Lines ==========
    
    /**
     * Initializes default values for score, level, and lines labels.
     * This is called when starting a new game or resetting game state.
     * 
     * @param scoreLabel The score label to initialize (can be null)
     * @param levelLabel The level label to initialize (can be null)
     * @param linesLabel The lines label to initialize (can be null)
     */
    public void initializeInfoLabels(javafx.scene.control.Label scoreLabel, 
                                     javafx.scene.control.Label levelLabel, 
                                     javafx.scene.control.Label linesLabel) {
        // Don't set text directly if label is bound - it will be bound to the score property
        // Only set initial text if not already bound
        if (scoreLabel != null && !scoreLabel.textProperty().isBound()) {
            scoreLabel.setText("0");
        }
        if (levelLabel != null && !levelLabel.textProperty().isBound()) {
            levelLabel.setText("1");
        }
        if (linesLabel != null && !linesLabel.textProperty().isBound()) {
            linesLabel.setText("0");
        }
    }
    
    /**
     * Resets score, level, and lines labels to default values.
     * Unbinds properties if bound, then sets default values.
     * 
     * @param scoreLabel The score label to reset (can be null)
     * @param levelLabel The level label to reset (can be null)
     * @param linesLabel The lines label to reset (can be null)
     */
    public void resetInfoLabels(javafx.scene.control.Label scoreLabel, 
                                javafx.scene.control.Label levelLabel, 
                                javafx.scene.control.Label linesLabel) {
        if (scoreLabel != null) {
            if (scoreLabel.textProperty().isBound()) {
                scoreLabel.textProperty().unbind();
            }
            scoreLabel.setText("0");
        }
        if (levelLabel != null) {
            if (levelLabel.textProperty().isBound()) {
                levelLabel.textProperty().unbind();
            }
            levelLabel.setText("1");
        }
        if (linesLabel != null) {
            if (linesLabel.textProperty().isBound()) {
                linesLabel.textProperty().unbind();
            }
            linesLabel.setText("0");
        }
    }
}

