package com.comp2042.controller;

import com.comp2042.event.EventSource;
import com.comp2042.event.EventType;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.logic.bricks.Brick;
import com.comp2042.model.DownData;
import com.comp2042.model.ViewData;
import com.comp2042.view.GameOverPanel;
import com.comp2042.view.PausePanel;
import com.comp2042.view.MainMenuPanel;
import com.comp2042.view.NotificationPanel;
import com.comp2042.view.SettingsPanel;
import com.comp2042.view.MultiplayerScreen;
import com.comp2042.view.SinglePlayerScreen;
import com.comp2042.model.HighScoreManager;
import com.comp2042.model.SimpleBoard;
import com.comp2042.util.MatrixOperations;
import com.comp2042.util.KeyBindingsManager;
import com.comp2042.controller.manager.AudioManager;
import com.comp2042.controller.manager.TimerManager;
import com.comp2042.controller.manager.GameStateManager;
import com.comp2042.controller.input.InputHandler;
import com.comp2042.controller.PausePanelActionHandler;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.control.Label;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.Parent;
import javafx.application.Platform;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    // Single player screen - manages all single player UI
    private SinglePlayerScreen singlePlayerScreen;
    
    // Multiplayer fields
    private MultiplayerScreen multiplayerScreen;
    
    // FXML components - kept for initialization and access
    @FXML private BorderPane gameBoard;
    @FXML private StackPane gameStack;
    @FXML private GridPane gamePanel;
    @FXML private Group groupNotification;
    @FXML private GridPane brickPanel;
    @FXML private GridPane ghostPanel;
    @FXML private GameOverPanel gameOverPanel;
    @FXML private PausePanel pausePanel;
    @FXML private SettingsPanel settingsPanel;
    @FXML private VBox nextBricksPanel;
    @FXML private GridPane holdBrickPanel;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label linesLabel;
    @FXML private MainMenuPanel mainMenuPanel;
    @FXML private Label countdownLabel;
    @FXML private Label timerLabel;
    
    // Multiplayer game controllers and timelines
    private GameController gameController1, gameController2;
    private Timeline timeLine1, timeLine2;
    private Timeline garbageProcessTimeline1, garbageProcessTimeline2; // Timelines for processing garbage queues
    private InputEventListener eventListener1, eventListener2;
    
    // Store scene filter handlers to avoid duplicates
    private javafx.event.EventHandler<KeyEvent> sceneKeyPressedHandler;
    private javafx.event.EventHandler<KeyEvent> sceneKeyReleasedHandler;
    
    // Store original panels to restore later
    private VBox originalLeftPanel, originalRightPanel;

    private InputEventListener eventListener;
    private Timeline timeLine;
    // Timer management - delegated to TimerManager
    private final TimerManager timerManager = new TimerManager();
    // Game state management - delegated to GameStateManager
    private final GameStateManager gameStateManager;
    private int currentLevel = 1;
    private javafx.beans.value.ChangeListener<? super Number> levelChangeListener;

    private HighScoreManager highScoreManager = new HighScoreManager();
    
    // Audio management - delegated to AudioManager
    private final AudioManager audioManager = new AudioManager();
    
    // Input handling - delegated to InputHandler (dependency injection)
    private final InputHandler inputHandler = new InputHandler(KeyBindingsManager.getInstance());
    
    // Initialize GameStateManager with dependencies
    {
        gameStateManager = new GameStateManager(audioManager, timerManager);
    }

    @Override
    public void initialize(URL _location, ResourceBundle _resources) {
        // Parameters are required by Initializable interface but not used
        try {
            Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
        } catch (Exception e) {
            System.out.println("Font not found, using default font");
        }
        
        try {
            Font.loadFont(getClass().getClassLoader().getResource("PublicPixel-rv0pA.ttf").toExternalForm(), 48);
        } catch (Exception e) {
            System.out.println("PublicPixel font not found, using default font");
        }

        // Initialize single player screen
        singlePlayerScreen = new SinglePlayerScreen();
        singlePlayerScreen.setGameBoard(gameBoard);
        singlePlayerScreen.setGameStack(gameStack);
        singlePlayerScreen.setGamePanel(gamePanel);
        singlePlayerScreen.setGroupNotification(groupNotification);
        singlePlayerScreen.setBrickPanel(brickPanel);
        singlePlayerScreen.setGhostPanel(ghostPanel);
        singlePlayerScreen.setGameOverPanel(gameOverPanel);
        singlePlayerScreen.setPausePanel(pausePanel);
        singlePlayerScreen.setNextBricksPanel(nextBricksPanel);
        singlePlayerScreen.setHoldBrickPanel(holdBrickPanel);
        singlePlayerScreen.setScoreLabel(scoreLabel);
        singlePlayerScreen.setLevelLabel(levelLabel);
        singlePlayerScreen.setLinesLabel(linesLabel);
        singlePlayerScreen.setCountdownLabel(countdownLabel);
        singlePlayerScreen.setTimerLabel(timerLabel);
        
        // Initialize single player UI panels
        singlePlayerScreen.initializeGamePanel();
        singlePlayerScreen.initializeHoldPanel();
        singlePlayerScreen.initializeNextBricksPanel();
        singlePlayerScreen.initializeInfoPanel();
        
        // Initialize audio manager
        audioManager.initialize();
        
        // Initialize timer manager
        timerManager.setSinglePlayerTimerLabel(timerLabel);
        
        // Initialize game state manager
        initializeGameStateManager();
        
        // Initialize multiplayer screen
        multiplayerScreen = new MultiplayerScreen();
        setupMultiplayerScreenCallbacks();

        if (gameOverPanel != null) gameOverPanel.setVisible(false);
        if (pausePanel != null) {
            pausePanel.setVisible(false);
            // Constrain pause panel to preferred size (same as multiplayer)
            pausePanel.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
            initializePausePanel();
        }
        
        // Initialize countdown label
        if (countdownLabel != null) {
            countdownLabel.setVisible(false);
            countdownLabel.setAlignment(javafx.geometry.Pos.CENTER);
        }
        
        // Timer label initialization is handled by TimerManager

        // Hide brick panel and ghost panel when main menu is visible
        if (brickPanel != null) {
            brickPanel.setVisible(false);
        }
        if (ghostPanel != null) {
            ghostPanel.setVisible(false);
        }
        
        // Keep next bricks panel container visible, but hide the brick previews until game starts
        // This must be done after initializeNextBricksPanel() to ensure panes exist
        if (nextBricksPanel != null) {
            nextBricksPanel.setVisible(true);
            // Hide the individual brick panes - ensure they're hidden at main menu
            List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
            if (nextBrickPanes != null && !nextBrickPanes.isEmpty()) {
                for (GridPane pane : nextBrickPanes) {
                    if (pane != null) {
                        pane.setVisible(false);
                        pane.setManaged(true); // Keep managed so layout works, just hide visually
                    }
                }
            }
        }

        // Set up main menu panel
        if (mainMenuPanel != null) {
            mainMenuPanel.getPlayButton().setOnAction(e -> startGame());
            mainMenuPanel.getMultiButton().setOnAction(e -> showMultiplayer());
            mainMenuPanel.getSettingsButton().setOnAction(e -> showSettings());
            mainMenuPanel.getQuitButton().setOnAction(e -> quitGame());
        }
        
        // Initialize settings panel
        initializeSettingsPanel();
        
        // Initialize input handler with callbacks
        initializeInputHandler();

        if (gameBoard != null) {
            gameBoard.setFocusTraversable(true);
            gameBoard.requestFocus();
            gameBoard.setOnKeyPressed(e -> inputHandler.handleKeyPress(e, gameStateManager.isMultiplayerMode(), gameStateManager.isGameStarted()));
            gameBoard.setOnKeyReleased(e -> inputHandler.handleKeyRelease(e, gameStateManager.isMultiplayerMode()));
        }
        
        // Start main menu music
        audioManager.playMainMenuMusic();
    }
    
    // Audio initialization is now handled by AudioManager
    
    private void initializeGameStateManager() {
        // Set up callbacks for UI updates
        gameStateManager.setOnShowPausePanel(() -> {
            if (pausePanel != null) pausePanel.setVisible(true);
        });
        
        gameStateManager.setOnHidePausePanel(() -> {
            if (pausePanel != null) pausePanel.setVisible(false);
        });
        
        gameStateManager.setOnShowMultiplayerPausePanel(() -> {
            if (multiplayerScreen != null) multiplayerScreen.showPausePanel();
        });
        
        gameStateManager.setOnHideMultiplayerPausePanel(() -> {
            if (multiplayerScreen != null) multiplayerScreen.hidePausePanel();
        });
        
        gameStateManager.setOnStartGarbageProcessingTimelines(this::startGarbageProcessingTimelines);
        gameStateManager.setOnStopGarbageProcessingTimelines(this::stopGarbageProcessingTimelines);
        
        gameStateManager.setOnStartMultiplayerTimer(this::startMultiplayerTimer);
        gameStateManager.setOnStopMultiplayerTimer(this::stopMultiplayerTimer);
        gameStateManager.setOnPauseMultiplayerTimer(this::pauseMultiplayerTimer);
        gameStateManager.setOnResumeMultiplayerTimer(this::resumeMultiplayerTimer);
        gameStateManager.setOnResetMultiplayerTimer(this::resetMultiplayerTimer);
        
        gameStateManager.setOnStartSinglePlayerTimer(this::startTimer);
        gameStateManager.setOnStopSinglePlayerTimer(this::stopTimer);
        gameStateManager.setOnPauseSinglePlayerTimer(this::pauseTimer);
        gameStateManager.setOnResumeSinglePlayerTimer(this::resumeTimer);
        gameStateManager.setOnResetSinglePlayerTimer(this::resetTimer);
        
        gameStateManager.setOnUpdateTimelineRate(this::updateTimelineRate);
        gameStateManager.setOnUpdateTimelineRate1(() -> updateTimelineRate(1));
        gameStateManager.setOnUpdateTimelineRate2(() -> updateTimelineRate(2));
        
        gameStateManager.setOnShowWinningPanel(() -> {
            int winnerPlayerNumber = gameStateManager.isGameOver1() ? 2 : 1;
            showWinningPanel(winnerPlayerNumber);
        });
        
        gameStateManager.setOnHideWinningPanel(this::hideWinningPanel);
        
        gameStateManager.setOnShowGameOverPanel(() -> {
            if (gameOverPanel != null) {
                // Get current score from scoreLabel
                int currentScore = 0;
                if (scoreLabel != null) {
                    try {
                        String scoreText = scoreLabel.getText();
                        if (scoreText != null && scoreText.contains(":")) {
                            String scoreValue = scoreText.substring(scoreText.indexOf(":") + 1).trim();
                            currentScore = Integer.parseInt(scoreValue);
                        }
                    } catch (Exception e) {
                        // If parsing fails, use 0
                    }
                }
                
                // Add score to high scores
                highScoreManager.addScore(currentScore);
                
                // Update game over panel
                gameOverPanel.setCurrentScore(currentScore);
                gameOverPanel.setTimeUsed(timerManager.getSinglePlayerElapsedSeconds());
                gameOverPanel.setHighScores(highScoreManager.getTop3Scores());
                
                // Set button actions
                gameOverPanel.setOnYesAction(() -> {
                    newGame(null);
                });
                
                gameOverPanel.setOnNoAction(() -> {
                    // Go back to main menu and reset the game
                    if (eventListener != null) {
                        eventListener.createNewGame();
                    }
                    // Clear the game board display
                    if (singlePlayerScreen != null) {
                        Rectangle[][] displayMatrix = singlePlayerScreen.getDisplayMatrix();
                        if (displayMatrix != null) {
                            for (int i = 0; i < displayMatrix.length; i++) {
                                for (int j = 0; j < displayMatrix[i].length; j++) {
                                    if (displayMatrix[i][j] != null) {
                                        displayMatrix[i][j].setFill(Color.TRANSPARENT);
                                    }
                                }
                            }
                        }
                        // Clear next bricks display
                        List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
                        if (nextBrickPanes != null) {
                            for (GridPane pane : nextBrickPanes) {
                                if (pane != null) {
                                    pane.getChildren().clear();
                                    // Re-initialize empty cells
                                    for (int r = 0; r < 4; r++) {
                                        for (int c = 0; c < 4; c++) {
                                            Rectangle rect = new Rectangle(GameConstants.BRICK_SIZE - 10, GameConstants.BRICK_SIZE - 10);
                                            rect.setFill(Color.TRANSPARENT);
                                            pane.add(rect, c, r);
                                        }
                                    }
                                }
                            }
                        }
                        // Clear hold brick display
                        Rectangle[][] holdBrickRectangles = singlePlayerScreen.getHoldBrickRectangles();
                        if (holdBrickRectangles != null) {
                            for (int i = 0; i < holdBrickRectangles.length; i++) {
                                for (int j = 0; j < holdBrickRectangles[i].length; j++) {
                                    if (holdBrickRectangles[i][j] != null) {
                                        holdBrickRectangles[i][j].setFill(Color.TRANSPARENT);
                                    }
                                }
                            }
                        }
                    }
                    // Clear current brick display and ghost panel
                    if (brickPanel != null) {
                        brickPanel.getChildren().clear();
                        brickPanel.setVisible(false);
                    }
                    if (ghostPanel != null) {
                        ghostPanel.getChildren().clear();
                        ghostPanel.setVisible(false);
                    }
                    
                    // Hide game over panel and show main menu
                    if (gameOverPanel != null) gameOverPanel.setVisible(false);
                    if (mainMenuPanel != null) mainMenuPanel.setVisible(true);
                    gameStateManager.resetGameStates();
                    if (timeLine != null) timeLine.stop();
                    
                    // Reset timer
                    resetTimer();
                    
                    // Stop game over sound and play main menu music
                    audioManager.playMainMenuMusic();
                });
                
                gameOverPanel.setVisible(true);
            }
        });
        
        gameStateManager.setOnHideGameOverPanel(() -> {
            if (gameOverPanel != null) gameOverPanel.setVisible(false);
        });
        
        gameStateManager.setOnShowMainMenu(() -> {
            if (mainMenuPanel != null) mainMenuPanel.setVisible(true);
        });
        
        gameStateManager.setOnHideMainMenu(() -> {
            if (mainMenuPanel != null) mainMenuPanel.setVisible(false);
        });
        
        gameStateManager.setOnRequestFocus(() -> {
            if (gameBoard != null) gameBoard.requestFocus();
        });
    }
    
    private void initializeInputHandler() {
        inputHandler.setSettingsPanel(settingsPanel);
        
        // Set up single player callbacks
        inputHandler.setSinglePlayerCallbacks(new InputHandler.SinglePlayerCallbacks() {
            @Override
            public InputEventListener getEventListener() {
                return eventListener;
            }
            
            @Override
            public Timeline getTimeline() {
                return timeLine;
            }
            
            @Override
            public boolean isPaused() {
                return gameStateManager.isPaused();
            }
            
            @Override
            public boolean isGameOver() {
                return gameStateManager.isGameOver();
            }
            
            @Override
            public boolean isGameStarted() {
                return gameStateManager.isGameStarted();
            }
            
            @Override
            public void refreshBrick(ViewData viewData) {
                if (singlePlayerScreen != null) {
                    singlePlayerScreen.refreshBrick(viewData, gameStateManager.isGameStarted());
                }
            }
            
            @Override
            public void moveDown(MoveEvent event) {
                GuiController.this.moveDown(event);
            }
            
            @Override
            public void pauseGame() {
                GuiController.this.pauseGame(null);
            }
            
            @Override
            public void startGame() {
                GuiController.this.startGame();
            }
            
            @Override
            public void newGame() {
                GuiController.this.newGame(null);
            }
            
            @Override
            public void showNotification(String message) {
                GuiController.this.showNotification(message);
            }
            
            @Override
            public void playLineClear() {
                audioManager.playLineClear();
            }
            
            @Override
            public void updateTimelineRate() {
                GuiController.this.updateTimelineRate();
            }
        });
        
        // Set up multiplayer callbacks
        inputHandler.setMultiplayerCallbacks(new InputHandler.MultiplayerCallbacks() {
            @Override
            public boolean isMultiplayerMode() {
                return gameStateManager.isMultiplayerMode();
            }
            
            @Override
            public boolean isGameStarted() {
                return gameStateManager.isGameStarted();
            }
            
            @Override
            public boolean isPaused() {
                return gameStateManager.isPaused();
            }
            
            @Override
            public boolean isGameOver1() {
                return gameStateManager.isGameOver1();
            }
            
            @Override
            public boolean isGameOver2() {
                return gameStateManager.isGameOver2();
            }
            
            @Override
            public boolean isPlayer1Ready() {
                return multiplayerScreen != null && multiplayerScreen.isPlayer1Ready();
            }
            
            @Override
            public boolean isPlayer2Ready() {
                return multiplayerScreen != null && multiplayerScreen.isPlayer2Ready();
            }
            
            @Override
            public void setPlayer1Ready(boolean ready) {
                if (multiplayerScreen != null) {
                    multiplayerScreen.setPlayer1Ready(ready);
                }
            }
            
            @Override
            public void setPlayer2Ready(boolean ready) {
                if (multiplayerScreen != null) {
                    multiplayerScreen.setPlayer2Ready(ready);
                }
            }
            
            @Override
            public InputEventListener getEventListener1() {
                return eventListener1;
            }
            
            @Override
            public InputEventListener getEventListener2() {
                return eventListener2;
            }
            
            @Override
            public Timeline getTimeline1() {
                return timeLine1;
            }
            
            @Override
            public Timeline getTimeline2() {
                return timeLine2;
            }
            
            @Override
            public void refreshBrick(ViewData viewData, int playerNumber) {
                GuiController.this.refreshBrick(viewData, playerNumber);
            }
            
            @Override
            public void moveDown(MoveEvent event, int playerNumber) {
                GuiController.this.moveDown(event, playerNumber);
            }
            
            @Override
            public void pauseGame() {
                GuiController.this.pauseGame(null);
            }
            
            @Override
            public void playLineClear() {
                audioManager.playLineClear();
            }
            
            @Override
            public void updateTimelineRate(int playerNumber) {
                GuiController.this.updateTimelineRate(playerNumber);
            }
            
            @Override
            public void updateReadyLabels() {
                if (multiplayerScreen != null) {
                    multiplayerScreen.updateReadyLabels();
                }
            }
            
            @Override
            public void checkBothReady() {
                if (multiplayerScreen != null) {
                    multiplayerScreen.checkBothReady();
                }
            }
        });
    }
    
    private void setupMultiplayerScreenCallbacks() {
        if (multiplayerScreen == null) return;
        
        // Set up pause panel action handler for multiplayer
        PausePanelActionHandler pausePanelHandler = new PausePanelActionHandler(this);
        multiplayerScreen.setPausePanelActionHandler(pausePanelHandler);
        
        multiplayerScreen.setCallbacks(
            (r) -> startMultiplayerGame(), // onStartGame
            (r) -> restartMultiplayerGame(), // onRestartGame
            (r) -> quitToMainMenuFromMultiplayer(), // onQuitToMenu
            (r) -> resumeMultiplayerGame(), // onResumeGame
            (r) -> showSettingsFromPause(), // onShowSettings
            () -> multiplayerScreen.updateReadyLabels(), // onUpdateReadyLabels
            () -> multiplayerScreen.checkBothReady(), // onCheckBothReady
            (ready) -> {}, // onSetPlayer1Ready (handled internally)
            (ready) -> {}, // onSetPlayer2Ready (handled internally)
            () -> attachKeyboardHandlersToScene(), // onAttachKeyboardHandlers
            (parent) -> {}, // onGetRootBorderPane (not needed)
            (panel) -> {} // onSetSettingsPanel (not needed)
        );
    }
    
    private void initializeSettingsPanel() {
        if (settingsPanel != null) {
            settingsPanel.setVisible(false);
            
            // Set up volume slider
            javafx.scene.control.Slider volumeSlider = settingsPanel.getVolumeSlider();
            volumeSlider.setValue(audioManager.getVolume() * 100); // Convert to percentage
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double newVolume = newVal.doubleValue() / 100.0;
                audioManager.setVolume(newVolume);
                // If user moves slider while muted, unmute
                if (audioManager.isMuted() && newVal.doubleValue() != oldVal.doubleValue()) {
                    audioManager.toggleMute();
                    Button muteBtn = settingsPanel.getMuteButton();
                    if (muteBtn != null) {
                        muteBtn.setText("🔊");
                    }
                }
            });
            
            // Set up mute button
            Button muteButton = settingsPanel.getMuteButton();
            muteButton.setOnAction(e -> {
                boolean isMuted = audioManager.toggleMute();
                muteButton.setText(isMuted ? "🔇" : "🔊");
            });
            
            // Set up ghost piece checkbox
            javafx.scene.control.CheckBox ghostPieceCheckBox = settingsPanel.getGhostPieceCheckBox();
            ghostPieceCheckBox.setSelected(true); // Default to checked
                    ghostPieceCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                // Update ghost panel for single player
                if (ghostPanel != null) {
                    ghostPanel.setVisible(newVal && gameStateManager.isGameStarted());
                }
                // Update ghost panels for multiplayer
                if (gameStateManager.isMultiplayerMode() && multiplayerScreen != null) {
                    multiplayerScreen.setBrickPanelsVisible(newVal && gameStateManager.isGameStarted(), settingsPanel);
                }
            });
            
            // Set up back button
            settingsPanel.setOnBackAction(() -> hideSettings());
        }
    }
    
    // Volume and mute operations are now handled by AudioManager
    
    private void showSettings() {
        if (settingsPanel != null && mainMenuPanel != null) {
            mainMenuPanel.setVisible(false);
            settingsPanel.setVisible(true);
            // Refresh controls display to show current bindings
            settingsPanel.updateControlsDisplay();
            // Request focus on settings panel to receive key events
            settingsPanel.requestFocus();
        }
    }
    
    void showSettingsFromPause() {
            if (settingsPanel != null) {
            // Refresh controls display to show current bindings
            settingsPanel.updateControlsDisplay();
            // Request focus on settings panel to receive key events
            settingsPanel.requestFocus();
            
            if (gameStateManager.isMultiplayerMode() && multiplayerScreen != null) {
                // Hide pause overlay for multiplayer
                multiplayerScreen.hidePausePanel();
                // Remove settings panel from gameStack if it's there (needed for multiplayer)
                if (gameStack != null && settingsPanel != null) {
                    javafx.scene.Parent currentParent = settingsPanel.getParent();
                    if (currentParent != null && currentParent == gameStack) {
                        gameStack.getChildren().remove(settingsPanel);
                    }
                }
                // Show settings overlay
                multiplayerScreen.showSettingsOverlay(settingsPanel);
            } else {
                // Hide pause panel for single player
                if (pausePanel != null) {
                    pausePanel.setVisible(false);
                }
                // Ensure settings panel is in gameStack for single player
                if (gameStack != null && settingsPanel != null) {
                    // Remove from any other parent first (defensive check)
                    javafx.scene.Parent currentParent = settingsPanel.getParent();
                    if (currentParent != null && currentParent != gameStack) {
                        if (currentParent instanceof javafx.scene.layout.Pane) {
                            ((javafx.scene.layout.Pane) currentParent).getChildren().remove(settingsPanel);
                        }
                    }
                    // Remove from multiplayer overlay if it's there (defensive check)
                    if (gameStateManager.isMultiplayerMode() && multiplayerScreen != null) {
                        multiplayerScreen.hideSettingsOverlay();
                    }
                    // Add back to gameStack if not already there (defensive check)
                    if (!gameStack.getChildren().contains(settingsPanel)) {
                        gameStack.getChildren().add(settingsPanel);
                    }
                    // Bring settings panel to front in gameStack (so it's on top of other elements)
                    gameStack.getChildren().remove(settingsPanel);
                    gameStack.getChildren().add(settingsPanel);
                }
                // Show settings panel (it's in gameStack for single player)
                if (settingsPanel != null) {
                    // Ensure it's managed and visible
                    settingsPanel.setManaged(true);
                    settingsPanel.setVisible(true);
                    // Force a layout update to ensure it's displayed
                    if (gameStack != null) {
                        gameStack.requestLayout();
                    }
                }
            }
        }
    }
    
    private void showWinningPanel(int winnerPlayerNumber) {
        if (multiplayerScreen != null) {
            // Stop multiplayer timer
            stopMultiplayerTimer();
            
            // Show winning panel
            multiplayerScreen.showWinningPanel(winnerPlayerNumber, timerManager.getMultiplayerElapsedSeconds());
            // Play winner sound
            audioManager.playWinner();
        }
    }
    
    void hideWinningPanel() {
        if (multiplayerScreen != null) {
            multiplayerScreen.hideWinningPanel();
        }
    }
    
    private void hideSettings() {
        if (settingsPanel != null) {
            if (gameStateManager.isMultiplayerMode() && multiplayerScreen != null) {
                // Hide settings overlay for multiplayer
                multiplayerScreen.hideSettingsOverlay();
                settingsPanel.setVisible(false);
            } else {
                // Hide settings panel for single player
                settingsPanel.setVisible(false);
                // Note: Keep settingsPanel in gameStack for next time
            }
            
            // If main menu is visible, show it
            if (mainMenuPanel != null && !mainMenuPanel.isVisible()) {
                // Check if we should return to pause menu or main menu
                if (gameStateManager.isPaused() && gameStateManager.isGameStarted()) {
                    // Return to pause menu
                    if (gameStateManager.isMultiplayerMode() && multiplayerScreen != null) {
                        // Make sure pause overlay is restored properly
                        multiplayerScreen.showPausePanel();
                    } else {
                        if (pausePanel != null) {
                            pausePanel.setVisible(true);
                        }
                    }
                } else {
                    // Return to main menu
                    if (mainMenuPanel != null) {
                        mainMenuPanel.setVisible(true);
                    }
                }
            } else if (mainMenuPanel != null) {
                mainMenuPanel.setVisible(true);
            }
        }
    }
    
    private void showMultiplayer() {
        if (mainMenuPanel != null) {
            mainMenuPanel.setVisible(false);
        }
        
        gameStateManager.setMultiplayerMode(true);
        
        if (multiplayerScreen == null) {
            multiplayerScreen = new MultiplayerScreen();
            setupMultiplayerScreenCallbacks();
        }
        
        // Re-initialize multiplayer panels to ensure consistent sizing
        multiplayerScreen.initializeMultiplayerPanels();
        
        // Register timer label with TimerManager
        if (multiplayerScreen.getTimerLabel() != null) {
            timerManager.setMultiplayerTimerLabel(multiplayerScreen.getTimerLabel());
        }
        
        // Get root BorderPane to hide original left and right panels
        BorderPane rootPane = getRootBorderPane();
        if (rootPane != null) {
            // Store original panels if not already stored
            if (originalLeftPanel == null && rootPane.getLeft() instanceof VBox) {
                originalLeftPanel = (VBox) rootPane.getLeft();
            }
            if (originalRightPanel == null && rootPane.getRight() instanceof VBox) {
                originalRightPanel = (VBox) rootPane.getRight();
            }
            
            // Hide original left and right panels
            if (rootPane.getLeft() != null) {
                rootPane.getLeft().setVisible(false);
                rootPane.getLeft().setManaged(false);
            }
            if (rootPane.getRight() != null) {
                rootPane.getRight().setVisible(false);
                rootPane.getRight().setManaged(false);
            }
        }
        
        // Hide single player game board
        if (gameBoard != null) {
            gameBoard.setVisible(false);
            gameBoard.setManaged(false);
        }
        
        // Hide original single player panels
        if (holdBrickPanel != null) {
            holdBrickPanel.setVisible(false);
        }
        if (nextBricksPanel != null) {
            nextBricksPanel.setVisible(false);
        }
        if (scoreLabel != null) {
            scoreLabel.setVisible(false);
        }
        if (levelLabel != null) {
            levelLabel.setVisible(false);
        }
        if (linesLabel != null) {
            linesLabel.setVisible(false);
        }
        
        // Show multiplayer container in the center area
        if (gameBoard != null && gameBoard.getParent() != null) {
            Parent parent = gameBoard.getParent();
            if (parent instanceof VBox) {
                VBox centerVBox = (VBox) parent;
                centerVBox.setAlignment(javafx.geometry.Pos.CENTER);
                centerVBox.setFillWidth(true);
                
                // Get wrapper from MultiplayerScreen
                StackPane wrapper = multiplayerScreen.getWrapper();
                if (wrapper != null && !centerVBox.getChildren().contains(wrapper)) {
                    centerVBox.getChildren().add(wrapper);
                }
                
                multiplayerScreen.show();
                
                // Request immediate layout update
                centerVBox.requestLayout();
            }
        }
        
        // Show ready panel instead of starting game immediately
        multiplayerScreen.showReadyPanel();
        
        // Attach keyboard handlers to the scene for ready state
        Platform.runLater(() -> {
            attachKeyboardHandlersToScene();
        });
    }
    
    // showReadyPanel, updateReadyLabels, and checkBothReady are now in MultiplayerScreen
    
    private void attachKeyboardHandlersToScene() {
        // Get the scene from any node (preferably gameBoard)
        javafx.scene.Scene scene = null;
        if (gameBoard != null && gameBoard.getScene() != null) {
            scene = gameBoard.getScene();
        } else if (multiplayerScreen != null && multiplayerScreen.getContainer() != null && multiplayerScreen.getContainer().getScene() != null) {
            scene = multiplayerScreen.getContainer().getScene();
        }
        
        if (scene == null) {
            return;
        }
        
        // Remove existing handlers if they exist to avoid duplicates
        if (sceneKeyPressedHandler != null) {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, sceneKeyPressedHandler);
        }
        if (sceneKeyReleasedHandler != null) {
            scene.removeEventFilter(KeyEvent.KEY_RELEASED, sceneKeyReleasedHandler);
        }
        
        if (gameStateManager.isMultiplayerMode()) {
            // Remove node handlers from gameBoard in multiplayer mode to avoid duplicate processing
            if (gameBoard != null) {
                gameBoard.setOnKeyPressed(null);
                gameBoard.setOnKeyReleased(null);
            }
            
            // Add handlers as filters so they work regardless of focus
            // Store handlers so we can remove them later
            sceneKeyPressedHandler = e -> {
                if (gameStateManager.isMultiplayerMode() && !e.isConsumed()) {
                    inputHandler.handleKeyPress(e, gameStateManager.isMultiplayerMode(), gameStateManager.isGameStarted());
                }
            };
            sceneKeyReleasedHandler = e -> {
                if (gameStateManager.isMultiplayerMode() && !e.isConsumed()) {
                    inputHandler.handleKeyRelease(e, gameStateManager.isMultiplayerMode());
                }
            };
            
            scene.addEventFilter(KeyEvent.KEY_PRESSED, sceneKeyPressedHandler);
            scene.addEventFilter(KeyEvent.KEY_RELEASED, sceneKeyReleasedHandler);
        } else {
            // Single player mode - use node-level handlers (set in initialize())
            // Clear any scene filters for single player - we don't need them
            // Node-level handlers are already set on gameBoard in initialize()
            if (gameBoard != null) {
                gameBoard.setFocusTraversable(true);
            }
        }
    }
    
    private void startMultiplayerGame() {
        if (multiplayerScreen != null) {
            multiplayerScreen.hideReadyPanel();
        }
        
        // Create game controllers for both players
        gameController1 = new GameController(this, 1);
        gameController2 = new GameController(this, 2);
        
        // Update GameStateManager with controllers and listeners
        gameStateManager.setGameController1(gameController1);
        gameStateManager.setGameController2(gameController2);
        gameStateManager.setEventListener1(eventListener1);
        gameStateManager.setEventListener2(eventListener2);
        gameStateManager.setTimeLine1(timeLine1);
        gameStateManager.setTimeLine2(timeLine2);
        gameStateManager.setGarbageProcessTimeline1(garbageProcessTimeline1);
        gameStateManager.setGarbageProcessTimeline2(garbageProcessTimeline2);
        gameStateManager.setMultiplayerMode(true);
        
        if (multiplayerScreen != null) {
            multiplayerScreen.setGameControllers(gameController1, gameController2);
            multiplayerScreen.setEventListeners(eventListener1, eventListener2);
        }
        
        // Hide winning panel if visible
        if (multiplayerScreen != null) {
            multiplayerScreen.hideWinningPanel();
        }
        
        // Reset ready states
        if (multiplayerScreen != null) {
            multiplayerScreen.setPlayer1Ready(false);
            multiplayerScreen.setPlayer2Ready(false);
        }
        
        // Delegate to GameStateManager
        gameStateManager.startMultiplayerGame();
        
        // Make brick panels and ghost panels visible
        if (multiplayerScreen != null && settingsPanel != null) {
            multiplayerScreen.setBrickPanelsVisible(true, settingsPanel);
        }
        
        // Make sure keyboard handlers are attached to scene
        Platform.runLater(() -> {
            attachKeyboardHandlersToScene();
            // Request focus on a visible component
            if (multiplayerScreen != null && multiplayerScreen.getContainer() != null) {
                multiplayerScreen.getContainer().setFocusTraversable(true);
                multiplayerScreen.getContainer().requestFocus();
            }
        });
    }
    
    private void restartMultiplayerGame() {
        // Clear all multiplayer game panels
        if (multiplayerScreen != null) {
            multiplayerScreen.clearGamePanels();
            multiplayerScreen.initializeMultiplayerPanels();
            // Register timer label with TimerManager
            if (multiplayerScreen.getTimerLabel() != null) {
                timerManager.setMultiplayerTimerLabel(multiplayerScreen.getTimerLabel());
            }
        }
        
        // Ensure container and wrapper are properly sized and visible
        if (multiplayerScreen != null) {
            multiplayerScreen.show();
        }
        
        // Update GameStateManager with current references
        gameStateManager.setGameController1(gameController1);
        gameStateManager.setGameController2(gameController2);
        gameStateManager.setTimeLine1(timeLine1);
        gameStateManager.setTimeLine2(timeLine2);
        gameStateManager.setGarbageProcessTimeline1(garbageProcessTimeline1);
        gameStateManager.setGarbageProcessTimeline2(garbageProcessTimeline2);
        
        // Delegate to GameStateManager
        gameStateManager.restartMultiplayerGame();
    }
    
    public void quitToMainMenuFromMultiplayer() {
        // Clear all multiplayer game panels
        if (multiplayerScreen != null) {
            multiplayerScreen.clearGamePanels();
        }
        
        // Update GameStateManager with current references
        gameStateManager.setTimeLine(timeLine);
        gameStateManager.setTimeLine1(timeLine1);
        gameStateManager.setTimeLine2(timeLine2);
        
        // Delegate state management to GameStateManager
        gameStateManager.quitToMainMenu();
        
        // Clear multiplayer references
        gameStateManager.clearMultiplayerReferences();
        
        // Hide multiplayer screen first
        if (multiplayerScreen != null) {
            multiplayerScreen.hide();
            multiplayerScreen.hideWinningPanel();
            multiplayerScreen.hidePausePanel();
            multiplayerScreen.hideSettingsOverlay();
        }
        
        // Remove multiplayer wrapper and container from center VBox
        if (gameBoard != null && gameBoard.getParent() != null) {
            Parent parent = gameBoard.getParent();
            if (parent instanceof VBox) {
                VBox centerVBox = (VBox) parent;
                if (multiplayerScreen != null) {
                    // Remove wrapper if present
                    StackPane wrapper = multiplayerScreen.getWrapper();
                    if (wrapper != null && centerVBox.getChildren().contains(wrapper)) {
                        centerVBox.getChildren().remove(wrapper);
                    }
                    // Also check and remove container directly if it was added separately
                    HBox container = multiplayerScreen.getContainer();
                    if (container != null && centerVBox.getChildren().contains(container)) {
                        centerVBox.getChildren().remove(container);
                    }
                }
                // Ensure gameBoard is visible and request layout update
                if (gameBoard != null) {
                    gameBoard.setVisible(true);
                    gameBoard.setManaged(true);
                }
                centerVBox.requestLayout();
            }
        }
        
        // Hide pause panels
        if (pausePanel != null) {
            pausePanel.setVisible(false);
        }
        
        // Restore original panels
        BorderPane rootPane = getRootBorderPane();
        if (rootPane != null) {
            if (originalLeftPanel != null) {
                originalLeftPanel.setVisible(true);
                originalLeftPanel.setManaged(true);
                rootPane.setLeft(originalLeftPanel);
                // Force layout update to ensure panel is properly displayed
                originalLeftPanel.requestLayout();
            }
            if (originalRightPanel != null) {
                originalRightPanel.setVisible(true);
                originalRightPanel.setManaged(true);
                rootPane.setRight(originalRightPanel);
                // Force layout update to ensure panel is properly displayed
                originalRightPanel.requestLayout();
            }
            
            // Ensure all children of left panel VBox are visible
            VBox leftPanelToProcess = null;
            if (originalLeftPanel != null && originalLeftPanel instanceof VBox) {
                leftPanelToProcess = (VBox) originalLeftPanel;
            } else if (rootPane.getLeft() instanceof VBox) {
                leftPanelToProcess = (VBox) rootPane.getLeft();
            }
            if (leftPanelToProcess != null) {
                for (javafx.scene.Node child : leftPanelToProcess.getChildren()) {
                    child.setVisible(true);
                    child.setManaged(true);
                }
            }
            
            // Ensure all children of right panel VBox are visible
            VBox rightPanelToProcess = null;
            if (originalRightPanel != null && originalRightPanel instanceof VBox) {
                rightPanelToProcess = (VBox) originalRightPanel;
            } else if (rootPane.getRight() instanceof VBox) {
                rightPanelToProcess = (VBox) rootPane.getRight();
            }
            if (rightPanelToProcess != null) {
                for (javafx.scene.Node child : rightPanelToProcess.getChildren()) {
                    child.setVisible(true);
                    child.setManaged(true);
                }
            }
            
            // Force root pane to update layout
            rootPane.requestLayout();
        }
        
        // Re-initialize hold panel (left panel)
        if (holdBrickPanel != null) {
            holdBrickPanel.setVisible(true);
            holdBrickPanel.setManaged(true);
            if (singlePlayerScreen != null) {
                singlePlayerScreen.initializeHoldPanel();
            }
        }
        
        // Keep next bricks panel container visible, but hide the brick previews when returning to main menu
        if (nextBricksPanel != null) {
            nextBricksPanel.setVisible(true);
            nextBricksPanel.setManaged(true);
            // Still initialize it so it's ready when game starts
            if (singlePlayerScreen != null) {
                singlePlayerScreen.initializeNextBricksPanel();
                // Hide the individual brick panes
                List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
                if (nextBrickPanes != null) {
                    for (GridPane pane : nextBrickPanes) {
                        if (pane != null) {
                            pane.setVisible(false);
                        }
                    }
                }
            }
        }
        
        // Show single player labels and reset them
        if (scoreLabel != null) {
            scoreLabel.setVisible(true);
            scoreLabel.setManaged(true);
            // Unbind if bound to clear any old bindings
            if (scoreLabel.textProperty().isBound()) {
                scoreLabel.textProperty().unbind();
            }
            scoreLabel.setText("0");
        }
        if (levelLabel != null) {
            levelLabel.setVisible(true);
            levelLabel.setManaged(true);
            // Unbind if bound to clear any old bindings
            if (levelLabel.textProperty().isBound()) {
                levelLabel.textProperty().unbind();
            }
            levelLabel.setText("1");
        }
        if (linesLabel != null) {
            linesLabel.setVisible(true);
            linesLabel.setManaged(true);
            // Unbind if bound to clear any old bindings
            if (linesLabel.textProperty().isBound()) {
                linesLabel.textProperty().unbind();
            }
            linesLabel.setText("0");
        }
        
        // Show single player game board
        if (gameBoard != null) {
            gameBoard.setVisible(true);
            gameBoard.setManaged(true);
        }
        
        // Show main menu
        if (mainMenuPanel != null) {
            mainMenuPanel.setVisible(true);
        }
        
        // Stop game music and play main menu music
        audioManager.stopGameMusic();
        audioManager.playMainMenuMusic();
    }
    
    private void resumeMultiplayerGame() {
        // Resume the game by calling pauseGame which toggles the pause state
        // This ensures all pause/resume logic is handled consistently
        pauseGame(null);
    }
    
    BorderPane getRootBorderPane() {
        // Get root BorderPane from scene (FXML root is BorderPane)
        if (gameBoard != null && gameBoard.getScene() != null) {
            javafx.scene.Node root = gameBoard.getScene().getRoot();
            if (root instanceof BorderPane) {
                return (BorderPane) root;
            }
        }
        // Fallback: traverse up from gameBoard
        if (gameBoard != null) {
            Parent node = gameBoard.getParent();
            while (node != null) {
                if (node instanceof BorderPane && node.getParent() == null) {
                    // This is likely the root
                    return (BorderPane) node;
                }
                if (node.getParent() == null && node instanceof BorderPane) {
                    return (BorderPane) node;
                }
                node = node.getParent();
            }
        }
        return null;
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        initGameView(boardMatrix, brick, 0);
    }
    
    public void initGameView(int[][] boardMatrix, ViewData brick, int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0) {
            // Initialize multiplayer game view
            refreshBrick(brick, playerNumber);
            refreshGameBackground(boardMatrix, playerNumber);
            
            // Update next bricks display for multiplayer
            GameController controller = (playerNumber == 1) ? gameController1 : gameController2;
            if (controller != null && controller.getBoard() instanceof SimpleBoard) {
                SimpleBoard simpleBoard = (SimpleBoard) controller.getBoard();
                updateNextBricks(simpleBoard.getNextBricks(), playerNumber);
            }
            
            if (playerNumber == 1) {
                if (timeLine1 != null) timeLine1.stop();
                timeLine1 = new Timeline(new KeyFrame(Duration.millis(400), ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD), 1)));
                timeLine1.setCycleCount(Timeline.INDEFINITE);
                updateTimelineRate(1);
            } else if (playerNumber == 2) {
                if (timeLine2 != null) timeLine2.stop();
                timeLine2 = new Timeline(new KeyFrame(Duration.millis(400), ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD), 2)));
                timeLine2.setCycleCount(Timeline.INDEFINITE);
                updateTimelineRate(2);
            }
        } else {
            // Single player game view
            if (singlePlayerScreen != null) {
                singlePlayerScreen.refreshBrick(brick, gameStateManager.isGameStarted());
                singlePlayerScreen.refreshGameBackground(boardMatrix);
                
                // Store brick data for countdown refresh
                singlePlayerScreen.setCurrentBrickData(brick);
                
                // Update next bricks display for single player (only if game has started)
                // Don't show next bricks during main menu or countdown
                if (gameStateManager.isGameStarted() && eventListener instanceof GameController) {
                    GameController gameController = (GameController) eventListener;
                    if (gameController.getBoard() instanceof SimpleBoard) {
                        SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                        singlePlayerScreen.updateNextBricks(simpleBoard.getNextBricks(), gameStateManager.isGameStarted());
                    }
                }
            }

            if (timeLine != null) timeLine.stop();
            timeLine = new Timeline(new KeyFrame(Duration.millis(400), ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))));
            timeLine.setCycleCount(Timeline.INDEFINITE);

            updateTimelineRate();

            if (gameBoard != null) gameBoard.requestFocus();
        }
    }

    private void refreshBrick(ViewData brick, int playerNumber) {
        // Use MultiplayerScreen for multiplayer mode
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0 && multiplayerScreen != null) {
            if (!gameStateManager.isPaused()) {
                multiplayerScreen.refreshBrick(brick, playerNumber);
            }
            return;
        }
        
        // Single player mode - use SinglePlayerScreen
        if (singlePlayerScreen != null) {
            singlePlayerScreen.refreshBrick(brick, gameStateManager.isGameStarted());
        }
    }

    public void refreshGameBackground(int[][] board) {
        refreshGameBackground(board, 0);
    }
    
    public void refreshGameBackground(int[][] board, int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0 && multiplayerScreen != null) {
            multiplayerScreen.refreshGameBackground(board, playerNumber);
        } else {
            // Single player mode - use SinglePlayerScreen
            if (singlePlayerScreen != null) {
                singlePlayerScreen.refreshGameBackground(board);
            }
        }
    }

    private void moveDown(MoveEvent event) {
        moveDown(event, 0);
    }
    
    /**
     * Clears all multiplayer game panels (game panels, brick panels, ghost panels)
     * and side panels (hold, next, score, level) by setting all rectangles to transparent/empty state
     * and resetting labels to default values.
     */
    void clearMultiplayerGamePanels() {
        if (multiplayerScreen != null) {
            multiplayerScreen.clearGamePanels();
        }
    }
    
    private void moveDown(MoveEvent event, int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0) {
            boolean isGameOver = (playerNumber == 1) ? gameStateManager.isGameOver1() : gameStateManager.isGameOver2();
            InputEventListener listener = (playerNumber == 1) ? eventListener1 : eventListener2;
            
            if (!gameStateManager.isGameStarted() || gameStateManager.isPaused() || isGameOver) {
                return;
            }
            if (listener != null) {
                DownData downData = listener.onDownEvent(event);
                if (downData != null) {
                    if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                        // Play line clear sound
                        audioManager.playLineClear();
                    }
                    refreshBrick(downData.getViewData(), playerNumber);
                }
            }
        } else {
            if (!gameStateManager.isGameStarted() || gameStateManager.isPaused() || gameStateManager.isGameOver()) {
                return;
            }
            if (eventListener != null) {
                DownData downData = eventListener.onDownEvent(event);
                if (downData != null) {
                    if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                        showNotification("+" + downData.getClearRow().getScoreBonus());
                        // Play line clear sound
                        audioManager.playLineClear();
                    }
                    refreshBrick(downData.getViewData(), 0);
                }
            }
            if (gameBoard != null) gameBoard.requestFocus();
        }
    }

    private void showNotification(String message) {
        if (groupNotification != null) {
            NotificationPanel panel = new NotificationPanel(message);
            groupNotification.getChildren().add(panel);
            panel.showScore(groupNotification.getChildren());
        }
    }

    public void setEventListener(InputEventListener listener) { 
        this.eventListener = listener;
        if (singlePlayerScreen != null) {
            singlePlayerScreen.setEventListener(listener);
        }
    }
    
    public void setEventListener(InputEventListener listener, int playerNumber) {
        if (playerNumber == 1) {
            this.eventListener1 = listener;
        } else if (playerNumber == 2) {
            this.eventListener2 = listener;
        } else {
            this.eventListener = listener;
            // For single player, also set the eventListener in SinglePlayerScreen
            // This is needed for ghost piece rendering
            if (singlePlayerScreen != null) {
                singlePlayerScreen.setEventListener(listener);
            }
        }
    }

    public void bindScore(IntegerProperty score) {
        bindScore(score, 0);
    }
    
    public void bindScore(IntegerProperty score, int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0 && multiplayerScreen != null) {
            multiplayerScreen.bindScore(score, playerNumber);
        } else if (playerNumber == 0) {
            // Single player mode - use SinglePlayerScreen
            if (singlePlayerScreen != null) {
                singlePlayerScreen.bindScore(score);
            }
        }
    }

    public void bindLevel(IntegerProperty level) {
        bindLevel(level, 0);
    }
    
    public void bindLevel(IntegerProperty level, int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0 && multiplayerScreen != null) {
            multiplayerScreen.bindLevel(level, playerNumber);
        } else if (playerNumber == 0) {
            // Single player mode - use SinglePlayerScreen
            if (singlePlayerScreen != null && level != null) {
                singlePlayerScreen.bindLevel(level);
                
                // Create and store new listener
                levelChangeListener = (obs, oldVal, newVal) -> {
                    currentLevel = newVal.intValue();
                    updateTimelineRate();
                };
                level.addListener(levelChangeListener);
                currentLevel = level.get();
                updateTimelineRate();
            }
        }
    }

    public void bindLines(IntegerProperty lines) {
        bindLines(lines, 0);
    }
    
    public void bindLines(IntegerProperty lines, int playerNumber) {
        // For multiplayer, lines binding is not used in side panels, but we keep the method for compatibility
        if (playerNumber == 0 && singlePlayerScreen != null && lines != null) {
            singlePlayerScreen.bindLines(lines);
        }
    }
    
    /**
     * Sends garbage to the opponent's queue when lines are cleared in multiplayer mode.
     * @param fromPlayerNumber The player number who cleared lines (1 or 2)
     * @param numGarbageLines Number of garbage lines to send
     */
    public void sendGarbageToOpponent(int fromPlayerNumber, int numGarbageLines) {
        if (!gameStateManager.isMultiplayerMode() || numGarbageLines <= 0) {
            return;
        }
        
        // Determine opponent's player number
        int opponentNumber = (fromPlayerNumber == 1) ? 2 : 1;
        
        // Get the opponent's game controller
        GameController opponentController = (opponentNumber == 1) ? gameController1 : gameController2;
        
        if (opponentController != null) {
            SimpleBoard opponentBoard = opponentController.getSimpleBoard();
            if (opponentBoard != null) {
                opponentBoard.addGarbageToQueue(numGarbageLines);
                // Process garbage immediately instead of waiting for timeline
                // This ensures garbage appears right away
                Platform.runLater(() -> {
                    if (opponentBoard.getPendingGarbageCount() > 0) {
                        processGarbageQueue(opponentNumber);
                    }
                });
            }
        }
    }
    
    /**
     * Processes garbage queue for a player, adding pending garbage lines to the board.
     * This should be called periodically or after certain game events.
     * @param playerNumber The player number (1 or 2)
     */
    private void processGarbageQueue(int playerNumber) {
        if (!gameStateManager.isMultiplayerMode() || playerNumber <= 0) {
            return;
        }
        
        boolean isGameOver = (playerNumber == 1) ? gameStateManager.isGameOver1() : gameStateManager.isGameOver2();
        if (isGameOver || gameStateManager.isPaused()) {
            return;
        }
        
        GameController controller = (playerNumber == 1) ? gameController1 : gameController2;
        if (controller == null) {
            return;
        }
        
        SimpleBoard board = controller.getSimpleBoard();
        if (board == null) {
            return;
        }
        
        // Only process if there's pending garbage
        if (board.getPendingGarbageCount() > 0) {
            // Process one garbage line at a time to give player time to react
            boolean potentialGameOver = board.processGarbageQueue();
            
            // Refresh the display
            refreshGameBackground(board.getBoardMatrix(), playerNumber);
            
            // Check if game over after adding garbage
            if (potentialGameOver) {
                // Get current view data to check brick position
                ViewData viewData = board.getViewData();
                if (viewData != null) {
                    // Check if the current brick position is blocked
                    if (MatrixOperations.intersect(board.getBoardMatrix(), 
                            viewData.getBrickData(), 
                            viewData.getXPosition(), 
                            viewData.getYPosition())) {
                        gameOver(playerNumber);
                    }
                }
            }
        }
    }
    
    /**
     * Starts garbage processing timelines for both players in multiplayer mode.
     * Garbage lines appear gradually (one every 2 seconds) to give players time to react.
     */
    private void startGarbageProcessingTimelines() {
        if (!gameStateManager.isMultiplayerMode()) {
            return;
        }
        
        // Stop existing timelines if any
        if (garbageProcessTimeline1 != null) {
            garbageProcessTimeline1.stop();
        }
        if (garbageProcessTimeline2 != null) {
            garbageProcessTimeline2.stop();
        }
        
        // Create timeline for player 1 - process garbage every 2 seconds
        garbageProcessTimeline1 = new Timeline(new KeyFrame(Duration.seconds(2), ae -> {
            if (!gameStateManager.isPaused() && !gameStateManager.isGameOver1()) {
                processGarbageQueue(1);
            }
        }));
        garbageProcessTimeline1.setCycleCount(Timeline.INDEFINITE);
        
        // Create timeline for player 2 - process garbage every 2 seconds
        garbageProcessTimeline2 = new Timeline(new KeyFrame(Duration.seconds(2), ae -> {
            if (!gameStateManager.isPaused() && !gameStateManager.isGameOver2()) {
                processGarbageQueue(2);
            }
        }));
        garbageProcessTimeline2.setCycleCount(Timeline.INDEFINITE);
        
        // Start both timelines
        garbageProcessTimeline1.play();
        garbageProcessTimeline2.play();
    }
    
    /**
     * Stops garbage processing timelines.
     */
    void stopGarbageProcessingTimelines() {
        if (garbageProcessTimeline1 != null) {
            garbageProcessTimeline1.stop();
        }
        if (garbageProcessTimeline2 != null) {
            garbageProcessTimeline2.stop();
        }
    }

    public void gameOver() {
        gameOver(0);
    }
    
    public void gameOver(int playerNumber) {
        // Update GameStateManager with current timeline references
        gameStateManager.setTimeLine(timeLine);
        gameStateManager.setTimeLine1(timeLine1);
        gameStateManager.setTimeLine2(timeLine2);
        
        // Delegate to GameStateManager
        gameStateManager.gameOver(playerNumber);
    }

    public void newGame(ActionEvent actionEvent) {
        // Update GameStateManager with current timeline reference
        gameStateManager.setTimeLine(timeLine);
        gameStateManager.setEventListener(eventListener);
        
        // Delegate to GameStateManager
        gameStateManager.newGame();
        
        if (eventListener != null) eventListener.createNewGame();
        
        // Ensure score, level, and lines bindings are active after creating new game
        if (eventListener instanceof GameController) {
            GameController gameController = (GameController) eventListener;
            bindScore(gameController.getScoreProperty());
            if (gameController.getBoard() instanceof SimpleBoard) {
                SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                bindLevel(simpleBoard.levelProperty(), 0);
                bindLines(simpleBoard.linesProperty(), 0);
            }
        }
        
        if (gameBoard != null) {
            // Ensure keyboard handlers are attached for single player mode
            if (!gameStateManager.isMultiplayerMode()) {
                gameBoard.setOnKeyPressed(e -> inputHandler.handleKeyPress(e, gameStateManager.isMultiplayerMode(), gameStateManager.isGameStarted()));
                gameBoard.setOnKeyReleased(e -> inputHandler.handleKeyRelease(e, gameStateManager.isMultiplayerMode()));
            }
            gameBoard.setFocusTraversable(true);
            gameBoard.requestFocus();
        }
        
        // Ensure settings panel is in gameStack for single player mode (defensive check)
        if (!gameStateManager.isMultiplayerMode() && gameStack != null && settingsPanel != null) {
            // Remove from multiplayer overlay if it's there
            if (multiplayerScreen != null) {
                multiplayerScreen.hideSettingsOverlay();
            }
            // Ensure it's in gameStack
            if (!gameStack.getChildren().contains(settingsPanel)) {
                gameStack.getChildren().add(settingsPanel);
            }
            // Hide settings panel (it will be shown when needed)
            settingsPanel.setVisible(false);
        }
        
        // Make game panel and brick panel visible for new game
        if (gamePanel != null) {
            gamePanel.setVisible(true);
        }
        if (brickPanel != null) {
            brickPanel.setVisible(true);
        }
        if (ghostPanel != null && settingsPanel != null) {
            // Check if ghost piece checkbox is selected
            javafx.scene.control.CheckBox ghostCheckBox = settingsPanel.getGhostPieceCheckBox();
            boolean showGhost = ghostCheckBox != null && ghostCheckBox.isSelected();
            ghostPanel.setVisible(showGhost);
        }
    }

    public void startGame() {
        gameStateManager.startGame();
        if (!gameStateManager.isGameStarted() && mainMenuPanel != null) {
            mainMenuPanel.setVisible(false);
            // Start countdown
            startCountdown();
        }
    }
    
    private void startCountdown() {
        if (countdownLabel == null) {
            // If countdown label doesn't exist, start game immediately
            actuallyStartGame();
            return;
        }
        
        // Stop main menu music during countdown
        audioManager.stopMainMenuMusic();
        
        // Make game panel visible during countdown so grid is visible
        if (gamePanel != null) {
            gamePanel.setVisible(true);
        }
        // Make brick panel and ghost panel visible during countdown
        if (brickPanel != null) {
            brickPanel.setVisible(true);
        }
        if (ghostPanel != null && settingsPanel != null) {
            // Check if ghost piece checkbox is selected
            javafx.scene.control.CheckBox ghostCheckBox = settingsPanel.getGhostPieceCheckBox();
            boolean showGhost = ghostCheckBox != null && ghostCheckBox.isSelected();
            ghostPanel.setVisible(showGhost);
        }
        
        // Hide next bricks during countdown - they'll be shown after countdown completes
        if (singlePlayerScreen != null) {
            List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
            if (nextBrickPanes != null) {
                for (GridPane pane : nextBrickPanes) {
                    if (pane != null) {
                        pane.setVisible(false);
                    }
                }
            }
        }
        
        // Play countdown sound
        audioManager.playCountdown();
        
        // Make countdown label visible and center it
        countdownLabel.setVisible(true);
        countdownLabel.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Center the label in the StackPane
        if (gameStack != null) {
            StackPane.setAlignment(countdownLabel, javafx.geometry.Pos.CENTER);
        }
        
        // Ensure label fills the StackPane for proper centering
        countdownLabel.setMaxWidth(Double.MAX_VALUE);
        countdownLabel.setMaxHeight(Double.MAX_VALUE);
        
        // Show initial countdown number (3)
        countdownLabel.setText("3");
        
        // Create countdown timeline: 3, 2, 1, then start game
        Timeline countdownTimeline = new Timeline();
        
        // Countdown from 3 to 1 (each number shows for 1 second)
        for (int i = 3; i >= 1; i--) {
            final int count = i;
            KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(3 - count + 1), 
                e -> {
                    if (countdownLabel != null && count > 1) {
                        countdownLabel.setText(String.valueOf(count - 1));
                    }
                }
            );
            countdownTimeline.getKeyFrames().add(keyFrame);
        }
        
        // After countdown, start the game
        KeyFrame startGameFrame = new KeyFrame(
            Duration.seconds(3),
            e -> {
                if (countdownLabel != null) {
                    countdownLabel.setVisible(false);
                }
                actuallyStartGame();
            }
        );
        countdownTimeline.getKeyFrames().add(startGameFrame);
        
        countdownTimeline.play();
    }
    
    private void actuallyStartGame() {
        // Update GameStateManager with current timeline reference
        gameStateManager.setTimeLine(timeLine);
        
        // Delegate state management to GameStateManager
        gameStateManager.actuallyStartGame();
        
        // Stop main menu music and start game music (handled by GameStateManager, but we need to ensure it's called)
        audioManager.playGameMusic();
        
        // Ensure score, level, and lines bindings are active
        if (eventListener instanceof GameController) {
            GameController gameController = (GameController) eventListener;
            bindScore(gameController.getScoreProperty());
            if (gameController.getBoard() instanceof SimpleBoard) {
                SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                bindLevel(simpleBoard.levelProperty(), 0);
                bindLines(simpleBoard.linesProperty(), 0);
            }
        }
        
        // Ensure settings panel is in gameStack for single player mode (defensive check)
        if (!gameStateManager.isMultiplayerMode() && gameStack != null && settingsPanel != null) {
            // Remove from multiplayer overlay if it's there
            if (multiplayerScreen != null) {
                multiplayerScreen.hideSettingsOverlay();
            }
            // Ensure it's in gameStack
            if (!gameStack.getChildren().contains(settingsPanel)) {
                gameStack.getChildren().add(settingsPanel);
            }
            // Hide settings panel (it will be shown when needed)
            settingsPanel.setVisible(false);
        }
        
        // Make game panel, brick panel and ghost panel visible
        if (gamePanel != null) {
            gamePanel.setVisible(true);
        }
        if (brickPanel != null) {
            brickPanel.setVisible(true);
        }
        if (ghostPanel != null && settingsPanel != null) {
            // Check if ghost piece checkbox is selected
            javafx.scene.control.CheckBox ghostCheckBox = settingsPanel.getGhostPieceCheckBox();
            boolean showGhost = ghostCheckBox != null && ghostCheckBox.isSelected();
            ghostPanel.setVisible(showGhost);
        }
        
        // Show and initialize next bricks panel for single player mode
        if (!gameStateManager.isMultiplayerMode() && nextBricksPanel != null && singlePlayerScreen != null) {
            nextBricksPanel.setVisible(true);
            nextBricksPanel.setManaged(true);
            singlePlayerScreen.initializeNextBricksPanel();
            
            // Show the brick preview panes now that game has started
            List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
            if (nextBrickPanes != null) {
                for (GridPane pane : nextBrickPanes) {
                    if (pane != null) {
                        pane.setVisible(true);
                    }
                }
            }
            
            // Update next bricks with initial bricks from the board
            if (eventListener instanceof GameController) {
                GameController gameController = (GameController) eventListener;
                if (gameController.getBoard() instanceof SimpleBoard) {
                    SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                    singlePlayerScreen.updateNextBricks(simpleBoard.getNextBricks(), gameStateManager.isGameStarted());
                }
            }
        }
        
        // Refresh the brick display with stored brick data
        // The brick data was stored when initGameView was called
        if (singlePlayerScreen != null) {
            ViewData currentBrickData = singlePlayerScreen.getCurrentBrickData();
            if (currentBrickData != null) {
                singlePlayerScreen.refreshBrick(currentBrickData, gameStateManager.isGameStarted());
            }
        }
        
        // Update GameStateManager with current timeline reference
        gameStateManager.setTimeLine(timeLine);
        
        // Start timer for single player mode (handled by GameStateManager callback)
        if (!gameStateManager.isMultiplayerMode()) {
            startTimer();
        }
        
        if (gameBoard != null) {
            // Ensure keyboard handlers are attached for single player mode
            if (!gameStateManager.isMultiplayerMode()) {
                gameBoard.setOnKeyPressed(e -> inputHandler.handleKeyPress(e, gameStateManager.isMultiplayerMode(), gameStateManager.isGameStarted()));
                gameBoard.setOnKeyReleased(e -> inputHandler.handleKeyRelease(e, gameStateManager.isMultiplayerMode()));
            }
            gameBoard.setFocusTraversable(true);
            gameBoard.requestFocus();
        }
    }

    public void quitGame() {
        javafx.application.Platform.exit();
    }

    public void pauseGame(ActionEvent actionEvent) {
        // Update GameStateManager with current timeline references
        gameStateManager.setTimeLine(timeLine);
        gameStateManager.setTimeLine1(timeLine1);
        gameStateManager.setTimeLine2(timeLine2);
        gameStateManager.setGarbageProcessTimeline1(garbageProcessTimeline1);
        gameStateManager.setGarbageProcessTimeline2(garbageProcessTimeline2);
        
        // Delegate to GameStateManager
        gameStateManager.pauseGame();
    }
    
    private void initializePausePanel() {
        if (pausePanel == null) return;
        PausePanelActionHandler handler = new PausePanelActionHandler(this);
        handler.setupPausePanelActions(pausePanel);
    }
    

    public void updateNextBricks(List<Brick> nextBricks) {
        updateNextBricks(nextBricks, 0);
    }
    
    public void updateNextBricks(List<Brick> nextBricks, int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0 && multiplayerScreen != null) {
            multiplayerScreen.updateNextBricks(nextBricks, playerNumber);
            return;
        }
        
        // Single player mode - use SinglePlayerScreen
        if (singlePlayerScreen != null) {
            singlePlayerScreen.updateNextBricks(nextBricks, gameStateManager.isGameStarted());
        }
    }

    public void updateHoldBrick(Brick heldBrick) {
        updateHoldBrick(heldBrick, 0);
    }
    
    public void updateHoldBrick(Brick heldBrick, int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0 && multiplayerScreen != null) {
            multiplayerScreen.updateHoldBrick(heldBrick, playerNumber);
            return;
        }
        
        // Single player mode - use SinglePlayerScreen
        if (singlePlayerScreen != null) {
            singlePlayerScreen.updateHoldBrick(heldBrick);
        }
    }

    private void updateTimelineRate() {
        updateTimelineRate(0);
    }
    
    private void updateTimelineRate(int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0) {
            Timeline timeline = (playerNumber == 1) ? timeLine1 : timeLine2;
            GameController controller = (playerNumber == 1) ? gameController1 : gameController2;
            
            if (timeline != null && controller != null && controller.getBoard() instanceof SimpleBoard) {
                SimpleBoard board = (SimpleBoard) controller.getBoard();
                int level = board.levelProperty().get();
                double rate = 1.0 + (Math.max(1, level) - 1) * 0.25;
                timeline.setRate(rate);
            }
        } else {
            if (timeLine != null) {
                double rate = 1.0 + (Math.max(1, currentLevel) - 1) * 0.25;
                timeLine.setRate(rate);
            }
        }
    }
    
    // Timer methods are now handled by TimerManager
    private void startTimer() {
        if (!gameStateManager.isMultiplayerMode()) {
            timerManager.startSinglePlayerTimer();
        }
    }
    
    private void stopTimer() {
        timerManager.stopSinglePlayerTimer();
    }
    
    private void pauseTimer() {
        timerManager.pauseSinglePlayerTimer();
    }
    
    private void resumeTimer() {
        timerManager.resumeSinglePlayerTimer();
    }
    
    private void resetTimer() {
        timerManager.resetSinglePlayerTimer();
    }
    
    private void startMultiplayerTimer() {
        if (gameStateManager.isMultiplayerMode()) {
            timerManager.startMultiplayerTimer();
        }
    }
    
    void stopMultiplayerTimer() {
        timerManager.stopMultiplayerTimer();
    }
    
    private void pauseMultiplayerTimer() {
        timerManager.pauseMultiplayerTimer();
    }
    
    private void resumeMultiplayerTimer() {
        timerManager.resumeMultiplayerTimer();
    }
    
    private void resetMultiplayerTimer() {
        timerManager.resetMultiplayerTimer();
    }
    
    // Getters and setters for PausePanelActionHandler
    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }
    
    public MultiplayerScreen getMultiplayerScreen() {
        return multiplayerScreen;
    }
    
    public PausePanel getPausePanel() {
        return pausePanel;
    }
    
    public SinglePlayerScreen getSinglePlayerScreen() {
        return singlePlayerScreen;
    }
    
    public AudioManager getAudioManager() {
        return audioManager;
    }
    
    public InputHandler getInputHandler() {
        return inputHandler;
    }
    
    public Timeline getTimeLine() {
        return timeLine;
    }
    
    public Timeline getTimeLine1() {
        return timeLine1;
    }
    
    public Timeline getTimeLine2() {
        return timeLine2;
    }
    
    public GameController getGameController1() {
        return gameController1;
    }
    
    public GameController getGameController2() {
        return gameController2;
    }
    
    public void setGameController1(GameController gameController1) {
        this.gameController1 = gameController1;
    }
    
    public void setGameController2(GameController gameController2) {
        this.gameController2 = gameController2;
    }
    
    public InputEventListener getEventListener() {
        return eventListener;
    }
    
    public InputEventListener getEventListener1() {
        return eventListener1;
    }
    
    public InputEventListener getEventListener2() {
        return eventListener2;
    }
    
    public void setEventListener1(InputEventListener eventListener1) {
        this.eventListener1 = eventListener1;
    }
    
    public void setEventListener2(InputEventListener eventListener2) {
        this.eventListener2 = eventListener2;
    }
    
    public Timeline getGarbageProcessTimeline1() {
        return garbageProcessTimeline1;
    }
    
    public Timeline getGarbageProcessTimeline2() {
        return garbageProcessTimeline2;
    }
    
    public void setGarbageProcessTimeline1(Timeline garbageProcessTimeline1) {
        this.garbageProcessTimeline1 = garbageProcessTimeline1;
    }
    
    public void setGarbageProcessTimeline2(Timeline garbageProcessTimeline2) {
        this.garbageProcessTimeline2 = garbageProcessTimeline2;
    }
    
    public void setTimeLine1(Timeline timeLine1) {
        this.timeLine1 = timeLine1;
    }
    
    public void setTimeLine2(Timeline timeLine2) {
        this.timeLine2 = timeLine2;
    }
    
    public BorderPane getGameBoard() {
        return gameBoard;
    }
    
    public StackPane getGameStack() {
        return gameStack;
    }
    
    public GridPane getGamePanel() {
        return gamePanel;
    }
    
    public GridPane getBrickPanel() {
        return brickPanel;
    }
    
    public GridPane getGhostPanel() {
        return ghostPanel;
    }
    
    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }
    
    public MainMenuPanel getMainMenuPanel() {
        return mainMenuPanel;
    }
    
    public GameOverPanel getGameOverPanel() {
        return gameOverPanel;
    }
    
    public VBox getNextBricksPanel() {
        return nextBricksPanel;
    }
    
    public GridPane getHoldBrickPanel() {
        return holdBrickPanel;
    }
    
    public Label getScoreLabel() {
        return scoreLabel;
    }
    
    public Label getLevelLabel() {
        return levelLabel;
    }
    
    public Label getLinesLabel() {
        return linesLabel;
    }
    
    public VBox getOriginalLeftPanel() {
        return originalLeftPanel;
    }
    
    public VBox getOriginalRightPanel() {
        return originalRightPanel;
    }
    
    public javafx.event.EventHandler<KeyEvent> getSceneKeyPressedHandler() {
        return sceneKeyPressedHandler;
    }
    
    public javafx.event.EventHandler<KeyEvent> getSceneKeyReleasedHandler() {
        return sceneKeyReleasedHandler;
    }
    
    public void setSceneKeyPressedHandler(javafx.event.EventHandler<KeyEvent> sceneKeyPressedHandler) {
        this.sceneKeyPressedHandler = sceneKeyPressedHandler;
    }
    
    public void setSceneKeyReleasedHandler(javafx.event.EventHandler<KeyEvent> sceneKeyReleasedHandler) {
        this.sceneKeyReleasedHandler = sceneKeyReleasedHandler;
    }
    

    
}
