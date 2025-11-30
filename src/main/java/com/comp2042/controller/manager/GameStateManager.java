package com.comp2042.controller.manager;

import com.comp2042.controller.GameController;
import com.comp2042.event.InputEventListener;
import com.comp2042.view.MultiplayerScreen;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Manages game state transitions and flags.
 * Handles pause, start, new game, game over, and multiplayer game state management.
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
    
    public GameStateManager(AudioManager audioManager, TimerManager timerManager) {
        this.audioManager = audioManager;
        this.timerManager = timerManager;
    }
    
    // Getters for state properties
    public BooleanProperty isPauseProperty() {
        return isPause;
    }
    
    public boolean isPaused() {
        return isPause.get();
    }
    
    public BooleanProperty isGameOverProperty() {
        return isGameOver;
    }
    
    public boolean isGameOver() {
        return isGameOver.get();
    }
    
    public BooleanProperty isGameOver1Property() {
        return isGameOver1;
    }
    
    public boolean isGameOver1() {
        return isGameOver1.get();
    }
    
    public BooleanProperty isGameOver2Property() {
        return isGameOver2;
    }
    
    public boolean isGameOver2() {
        return isGameOver2.get();
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public boolean isMultiplayerMode() {
        return isMultiplayerMode;
    }
    
    // Setters for game components
    public void setTimeLine(Timeline timeLine) {
        this.timeLine = timeLine;
    }
    
    public void setTimeLine1(Timeline timeLine1) {
        this.timeLine1 = timeLine1;
    }
    
    public void setTimeLine2(Timeline timeLine2) {
        this.timeLine2 = timeLine2;
    }
    
    public void setGarbageProcessTimeline1(Timeline garbageProcessTimeline1) {
        this.garbageProcessTimeline1 = garbageProcessTimeline1;
    }
    
    public void setGarbageProcessTimeline2(Timeline garbageProcessTimeline2) {
        this.garbageProcessTimeline2 = garbageProcessTimeline2;
    }
    
    public void setGameController1(GameController gameController1) {
        this.gameController1 = gameController1;
    }
    
    public void setGameController2(GameController gameController2) {
        this.gameController2 = gameController2;
    }
    
    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }
    
    public void setEventListener1(InputEventListener eventListener1) {
        this.eventListener1 = eventListener1;
    }
    
    public void setEventListener2(InputEventListener eventListener2) {
        this.eventListener2 = eventListener2;
    }
    
    // Setters for callbacks
    public void setOnShowPausePanel(Runnable callback) {
        this.onShowPausePanel = callback;
    }
    
    public void setOnHidePausePanel(Runnable callback) {
        this.onHidePausePanel = callback;
    }
    
    public void setOnShowMultiplayerPausePanel(Runnable callback) {
        this.onShowMultiplayerPausePanel = callback;
    }
    
    public void setOnHideMultiplayerPausePanel(Runnable callback) {
        this.onHideMultiplayerPausePanel = callback;
    }
    
    public void setOnStartGarbageProcessingTimelines(Runnable callback) {
        this.onStartGarbageProcessingTimelines = callback;
    }
    
    public void setOnStopGarbageProcessingTimelines(Runnable callback) {
        this.onStopGarbageProcessingTimelines = callback;
    }
    
    public void setOnUpdateTimelineRate(Runnable callback) {
        this.onUpdateTimelineRate = callback;
    }
    
    public void setOnUpdateTimelineRate1(Runnable callback) {
        this.onUpdateTimelineRate1 = callback;
    }
    
    public void setOnUpdateTimelineRate2(Runnable callback) {
        this.onUpdateTimelineRate2 = callback;
    }
    
    public void setOnShowGameOverPanel(Runnable callback) {
        this.onShowGameOverPanel = callback;
    }
    
    public void setOnHideGameOverPanel(Runnable callback) {
        this.onHideGameOverPanel = callback;
    }
    
    public void setOnShowMainMenu(Runnable callback) {
        this.onShowMainMenu = callback;
    }
    
    public void setOnHideMainMenu(Runnable callback) {
        this.onHideMainMenu = callback;
    }
    
    public void setOnRequestFocus(Runnable callback) {
        this.onRequestFocus = callback;
    }
    
    /**
     * Pauses or resumes the game.
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
     * Starts a new single player game.
     */
    public void startGame() {
        if (!gameStarted && onHideMainMenu != null) {
            onHideMainMenu.run();
            // Countdown will be handled by GuiController
        }
    }
    
    /**
     * Actually starts the game after countdown (called by GuiController).
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
     * Handles game over for single player or multiplayer.
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
     * Starts a multiplayer game.
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
     * Restarts a multiplayer game.
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
     * Sets multiplayer mode flag.
     */
    public void setMultiplayerMode(boolean isMultiplayerMode) {
        this.isMultiplayerMode = isMultiplayerMode;
    }
    
    /**
     * Clears multiplayer references (controllers, listeners, timelines).
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
     */
    public void setMultiplayerScreen(MultiplayerScreen screen) {
        this.multiplayerScreen = screen;
    }
    
    /**
     * Sets the callback for restarting the game (from winning panel).
     */
    public void setOnRestartGame(Runnable callback) {
        this.onRestartGame = callback;
    }
    
    /**
     * Sets the callback for quitting to main menu (from winning panel).
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
     * Hides the winning panel.
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
     * Sets up the winning panel action handlers.
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

