package com.comp2042.controller.manager;

import com.comp2042.controller.GameController;
import com.comp2042.event.InputEventListener;
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
    
    // Callbacks for UI updates (set via setters)
    private Runnable onShowPausePanel;
    private Runnable onHidePausePanel;
    private Runnable onShowMultiplayerPausePanel;
    private Runnable onHideMultiplayerPausePanel;
    private Runnable onStartGarbageProcessingTimelines;
    private Runnable onStopGarbageProcessingTimelines;
    private Runnable onStartMultiplayerTimer;
    private Runnable onStopMultiplayerTimer;
    private Runnable onPauseMultiplayerTimer;
    private Runnable onResumeMultiplayerTimer;
    private Runnable onResetMultiplayerTimer;
    private Runnable onStartSinglePlayerTimer;
    private Runnable onStopSinglePlayerTimer;
    private Runnable onPauseSinglePlayerTimer;
    private Runnable onResumeSinglePlayerTimer;
    private Runnable onResetSinglePlayerTimer;
    private Runnable onUpdateTimelineRate;
    private Runnable onUpdateTimelineRate1;
    private Runnable onUpdateTimelineRate2;
    private Runnable onShowWinningPanel;
    private Runnable onHideWinningPanel;
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
    
    public void setOnStartMultiplayerTimer(Runnable callback) {
        this.onStartMultiplayerTimer = callback;
    }
    
    public void setOnStopMultiplayerTimer(Runnable callback) {
        this.onStopMultiplayerTimer = callback;
    }
    
    public void setOnPauseMultiplayerTimer(Runnable callback) {
        this.onPauseMultiplayerTimer = callback;
    }
    
    public void setOnResumeMultiplayerTimer(Runnable callback) {
        this.onResumeMultiplayerTimer = callback;
    }
    
    public void setOnResetMultiplayerTimer(Runnable callback) {
        this.onResetMultiplayerTimer = callback;
    }
    
    public void setOnStartSinglePlayerTimer(Runnable callback) {
        this.onStartSinglePlayerTimer = callback;
    }
    
    public void setOnStopSinglePlayerTimer(Runnable callback) {
        this.onStopSinglePlayerTimer = callback;
    }
    
    public void setOnPauseSinglePlayerTimer(Runnable callback) {
        this.onPauseSinglePlayerTimer = callback;
    }
    
    public void setOnResumeSinglePlayerTimer(Runnable callback) {
        this.onResumeSinglePlayerTimer = callback;
    }
    
    public void setOnResetSinglePlayerTimer(Runnable callback) {
        this.onResetSinglePlayerTimer = callback;
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
    
    public void setOnShowWinningPanel(Runnable callback) {
        this.onShowWinningPanel = callback;
    }
    
    public void setOnHideWinningPanel(Runnable callback) {
        this.onHideWinningPanel = callback;
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
                if (onPauseMultiplayerTimer != null) onPauseMultiplayerTimer.run();
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
                if (onResumeMultiplayerTimer != null) onResumeMultiplayerTimer.run();
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
                if (onPauseSinglePlayerTimer != null) onPauseSinglePlayerTimer.run();
                isPause.set(true);
                if (onShowPausePanel != null) onShowPausePanel.run();
            } else {
                // Resume the game
                if (timeLine != null) {
                    timeLine.play();
                    if (onUpdateTimelineRate != null) onUpdateTimelineRate.run();
                }
                audioManager.playGameMusic();
                if (onResumeSinglePlayerTimer != null) onResumeSinglePlayerTimer.run();
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
        
        if (onStartSinglePlayerTimer != null) {
            onStartSinglePlayerTimer.run();
        }
        
        if (onRequestFocus != null) onRequestFocus.run();
    }
    
    /**
     * Starts a new game (restart from game over or pause menu).
     */
    public void newGame() {
        if (timeLine != null) timeLine.stop();
        if (onHideGameOverPanel != null) onHideGameOverPanel.run();
        
        if (onResetSinglePlayerTimer != null) onResetSinglePlayerTimer.run();
        
        audioManager.playGameMusic();
        
        if (eventListener != null) eventListener.createNewGame();
        
        if (timeLine != null) timeLine.play();
        
        if (onStartSinglePlayerTimer != null) {
            onStartSinglePlayerTimer.run();
        }
        
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
            
            if (onHideMultiplayerPausePanel != null) onHideMultiplayerPausePanel.run();
            if (onHidePausePanel != null) onHidePausePanel.run();
            isPause.set(false);
            
            audioManager.stopGameMusic();
            
            // If only one player is game over, show winning panel for the other player
            if (isGameOver1.get() && !isGameOver2.get()) {
                isGameOver2.set(true);
                if (onShowWinningPanel != null) onShowWinningPanel.run();
            } else if (isGameOver2.get() && !isGameOver1.get()) {
                isGameOver1.set(true);
                if (onShowWinningPanel != null) onShowWinningPanel.run();
            } else if (isGameOver1.get() && isGameOver2.get()) {
                if (onHideWinningPanel != null) onHideWinningPanel.run();
                audioManager.playGameOver();
            }
            return;
        }
        
        // Single player game over
        if (timeLine != null) timeLine.stop();
        
        if (onStopSinglePlayerTimer != null) onStopSinglePlayerTimer.run();
        
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
        if (onStartMultiplayerTimer != null) {
            onStartMultiplayerTimer.run();
        }
        
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
        if (onHideWinningPanel != null) onHideWinningPanel.run();
        
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
        if (onResetMultiplayerTimer != null) onResetMultiplayerTimer.run();
        if (onStartMultiplayerTimer != null) {
            onStartMultiplayerTimer.run();
        }
        
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
        if (onStopMultiplayerTimer != null) onStopMultiplayerTimer.run();
        if (onResetMultiplayerTimer != null) onResetMultiplayerTimer.run();
        if (onStopSinglePlayerTimer != null) onStopSinglePlayerTimer.run();
        if (onResetSinglePlayerTimer != null) onResetSinglePlayerTimer.run();
        
        // Reset pause state
        isPause.set(false);
        
        // Reset game states
        isGameOver.set(false);
        isGameOver1.set(false);
        isGameOver2.set(false);
        gameStarted = false;
        isMultiplayerMode = false;
        
        // Hide panels
        if (onHideWinningPanel != null) onHideWinningPanel.run();
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
}

