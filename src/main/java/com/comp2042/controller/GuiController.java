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
import com.comp2042.controller.input.InputHandler;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
// Audio management is now handled by AudioManager
import javafx.scene.Parent;
import javafx.application.Platform;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    // Single player screen - manages all single player UI
    private SinglePlayerScreen singlePlayerScreen;
    
    // Multiplayer fields
    private boolean isMultiplayerMode = false;
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
    private BooleanProperty isGameOver1 = new SimpleBooleanProperty(false);
    private BooleanProperty isGameOver2 = new SimpleBooleanProperty(false);
    
    // Store scene filter handlers to avoid duplicates
    private javafx.event.EventHandler<KeyEvent> sceneKeyPressedHandler;
    private javafx.event.EventHandler<KeyEvent> sceneKeyReleasedHandler;
    
    // Store original panels to restore later
    private VBox originalLeftPanel, originalRightPanel;

    private InputEventListener eventListener;
    private Timeline timeLine;
    // Timer management - delegated to TimerManager
    private final TimerManager timerManager = new TimerManager();
    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);
    private boolean gameStarted = false;
    private int currentLevel = 1;
    private javafx.beans.value.ChangeListener<? super Number> levelChangeListener;

    private HighScoreManager highScoreManager = new HighScoreManager();
    
    // Audio management - delegated to AudioManager
    private final AudioManager audioManager = new AudioManager();
    
    // Input handling - delegated to InputHandler (dependency injection)
    private final InputHandler inputHandler = new InputHandler(KeyBindingsManager.getInstance());

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
            gameBoard.setOnKeyPressed(e -> inputHandler.handleKeyPress(e, isMultiplayerMode, gameStarted));
            gameBoard.setOnKeyReleased(e -> inputHandler.handleKeyRelease(e, isMultiplayerMode));
        }
        
        // Start main menu music
        audioManager.playMainMenuMusic();
    }
    
    // Audio initialization is now handled by AudioManager
    
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
                return isPause.get();
            }
            
            @Override
            public boolean isGameOver() {
                return isGameOver.get();
            }
            
            @Override
            public boolean isGameStarted() {
                return gameStarted;
            }
            
            @Override
            public void refreshBrick(ViewData viewData) {
                if (singlePlayerScreen != null) {
                    singlePlayerScreen.refreshBrick(viewData, gameStarted);
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
                return isMultiplayerMode;
            }
            
            @Override
            public boolean isGameStarted() {
                return gameStarted;
            }
            
            @Override
            public boolean isPaused() {
                return isPause.get();
            }
            
            @Override
            public boolean isGameOver1() {
                return isGameOver1.get();
            }
            
            @Override
            public boolean isGameOver2() {
                return isGameOver2.get();
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
                    ghostPanel.setVisible(newVal && gameStarted);
                }
                // Update ghost panels for multiplayer
                if (isMultiplayerMode && multiplayerScreen != null) {
                    multiplayerScreen.setBrickPanelsVisible(newVal && gameStarted, settingsPanel);
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
    
    private void showSettingsFromPause() {
        if (settingsPanel != null) {
            // Refresh controls display to show current bindings
            settingsPanel.updateControlsDisplay();
            // Request focus on settings panel to receive key events
            settingsPanel.requestFocus();
            
            if (isMultiplayerMode && multiplayerScreen != null) {
                // Hide pause overlay for multiplayer
                multiplayerScreen.hidePausePanel();
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
                    if (isMultiplayerMode && multiplayerScreen != null) {
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
    
    private void hideWinningPanel() {
        if (multiplayerScreen != null) {
            multiplayerScreen.hideWinningPanel();
        }
    }
    
    private void hideSettings() {
        if (settingsPanel != null) {
            if (isMultiplayerMode && multiplayerScreen != null) {
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
                if (isPause.get() && gameStarted) {
                    // Return to pause menu
                    if (isMultiplayerMode && multiplayerScreen != null) {
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
        
        isMultiplayerMode = true;
        
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
        
        if (isMultiplayerMode) {
            // Remove node handlers from gameBoard in multiplayer mode to avoid duplicate processing
            if (gameBoard != null) {
                gameBoard.setOnKeyPressed(null);
                gameBoard.setOnKeyReleased(null);
            }
            
            // Add handlers as filters so they work regardless of focus
            // Store handlers so we can remove them later
            sceneKeyPressedHandler = e -> {
                if (isMultiplayerMode && !e.isConsumed()) {
                    inputHandler.handleKeyPress(e, isMultiplayerMode, gameStarted);
                }
            };
            sceneKeyReleasedHandler = e -> {
                if (isMultiplayerMode && !e.isConsumed()) {
                    inputHandler.handleKeyRelease(e, isMultiplayerMode);
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
        
        if (multiplayerScreen != null) {
            multiplayerScreen.setGameControllers(gameController1, gameController2);
            multiplayerScreen.setEventListeners(eventListener1, eventListener2);
        }
        
        // Reset game over states
        isGameOver1.set(false);
        isGameOver2.set(false);
        
        // Hide winning panel if visible
        if (multiplayerScreen != null) {
            multiplayerScreen.hideWinningPanel();
        }
        
        // Reset ready states
        if (multiplayerScreen != null) {
            multiplayerScreen.setPlayer1Ready(false);
            multiplayerScreen.setPlayer2Ready(false);
        }
        
        // Start the game
        gameStarted = true;
        isPause.set(false);
        
        // Hide pause panel if visible
        if (multiplayerScreen != null) {
            multiplayerScreen.hidePausePanel();
        }
        if (pausePanel != null) {
            pausePanel.setVisible(false);
        }
        
        // Start both timelines
        if (timeLine1 != null) {
            timeLine1.play();
        }
        if (timeLine2 != null) {
            timeLine2.play();
        }
        
        // Start garbage processing timelines
        startGarbageProcessingTimelines();
        
        // Start multiplayer timer
        startMultiplayerTimer();
        
        // Start game music
        audioManager.playGameMusic();
        
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
        if (multiplayerScreen != null) {
            multiplayerScreen.hideWinningPanel();
        }
        
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
        startGarbageProcessingTimelines();
        
        // Restart multiplayer timer
        resetMultiplayerTimer();
        startMultiplayerTimer();
        
        // Restart music
        audioManager.playGameMusic();
    }
    
    private void quitToMainMenuFromMultiplayer() {
        // Clear all multiplayer game panels
        if (multiplayerScreen != null) {
            multiplayerScreen.clearGamePanels();
        }
        
        // Stop all timelines
        if (timeLine != null) timeLine.stop();
        if (timeLine1 != null) timeLine1.stop();
        if (timeLine2 != null) timeLine2.stop();
        stopGarbageProcessingTimelines();
        
        // Stop multiplayer timer
        stopMultiplayerTimer();
        
        // Reset pause state
        isPause.set(false);
        
        // Reset game states
        isGameOver.set(false);
        isGameOver1.set(false);
        isGameOver2.set(false);
        gameStarted = false;
        isMultiplayerMode = false;
        
        // Hide multiplayer screen
        if (multiplayerScreen != null) {
            multiplayerScreen.hide();
            multiplayerScreen.hideWinningPanel();
            multiplayerScreen.hidePausePanel();
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
    
    private BorderPane getRootBorderPane() {
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
    
    // Old multiplayer UI methods removed - now in MultiplayerScreen
    // createPlayerSidePanel, initializeMultiplayerPanels, createPlayerContainer, 
    // createPlayerGameField, initializeGameFieldPanel are now in MultiplayerScreen
    
    // Audio methods are now handled by AudioManager

    // Single player UI initialization methods are now in SinglePlayerScreen

    // Key handling methods are now handled by InputHandler

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        initGameView(boardMatrix, brick, 0);
    }
    
    public void initGameView(int[][] boardMatrix, ViewData brick, int playerNumber) {
        if (isMultiplayerMode && playerNumber > 0) {
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
                singlePlayerScreen.refreshBrick(brick, gameStarted);
                singlePlayerScreen.refreshGameBackground(boardMatrix);
                
                // Store brick data for countdown refresh
                singlePlayerScreen.setCurrentBrickData(brick);
                
                // Update next bricks display for single player (only if game has started)
                // Don't show next bricks during main menu or countdown
                if (gameStarted && eventListener instanceof GameController) {
                    GameController gameController = (GameController) eventListener;
                    if (gameController.getBoard() instanceof SimpleBoard) {
                        SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                        singlePlayerScreen.updateNextBricks(simpleBoard.getNextBricks(), gameStarted);
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
        if (isMultiplayerMode && playerNumber > 0 && multiplayerScreen != null) {
            if (!isPause.get()) {
                multiplayerScreen.refreshBrick(brick, playerNumber);
            }
            return;
        }
        
        // Single player mode - use SinglePlayerScreen
        if (singlePlayerScreen != null) {
            singlePlayerScreen.refreshBrick(brick, gameStarted);
        }
    }

    public void refreshGameBackground(int[][] board) {
        refreshGameBackground(board, 0);
    }
    
    public void refreshGameBackground(int[][] board, int playerNumber) {
        if (isMultiplayerMode && playerNumber > 0 && multiplayerScreen != null) {
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
    private void clearMultiplayerGamePanels() {
        if (multiplayerScreen != null) {
            multiplayerScreen.clearGamePanels();
        }
        // Fallback code removed - multiplayerScreen should always be initialized
    }
    
    private void moveDown(MoveEvent event, int playerNumber) {
        if (isMultiplayerMode && playerNumber > 0) {
            BooleanProperty isGameOverProp = (playerNumber == 1) ? isGameOver1 : isGameOver2;
            InputEventListener listener = (playerNumber == 1) ? eventListener1 : eventListener2;
            
            if (!gameStarted || isPause.get() || isGameOverProp.get()) {
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
            if (!gameStarted || isPause.get() || isGameOver.get()) {
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
        }
    }

    public void bindScore(IntegerProperty score) {
        bindScore(score, 0);
    }
    
    public void bindScore(IntegerProperty score, int playerNumber) {
        if (isMultiplayerMode && playerNumber > 0 && multiplayerScreen != null) {
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
        if (isMultiplayerMode && playerNumber > 0 && multiplayerScreen != null) {
            multiplayerScreen.bindLevel(level, playerNumber);
        } else if (playerNumber == 0) {
            // Single player mode - use SinglePlayerScreen
            if (singlePlayerScreen != null && level != null) {
                singlePlayerScreen.bindLevel(level);
                
                // Remove old listener if it exists to avoid duplicates
                if (levelChangeListener != null) {
                    // Try to remove from the previous level property if we can find it
                    // Since we don't have a reference to the old property, we'll just create a new listener
                    // The old listener will become unreachable and be garbage collected
                }
                
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
        if (!isMultiplayerMode || numGarbageLines <= 0) {
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
        if (!isMultiplayerMode || playerNumber <= 0) {
            return;
        }
        
        BooleanProperty isGameOverProp = (playerNumber == 1) ? isGameOver1 : isGameOver2;
        if (isGameOverProp.get() || isPause.get()) {
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
        if (!isMultiplayerMode) {
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
            if (!isPause.get() && !isGameOver1.get()) {
                processGarbageQueue(1);
            }
        }));
        garbageProcessTimeline1.setCycleCount(Timeline.INDEFINITE);
        
        // Create timeline for player 2 - process garbage every 2 seconds
        garbageProcessTimeline2 = new Timeline(new KeyFrame(Duration.seconds(2), ae -> {
            if (!isPause.get() && !isGameOver2.get()) {
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
    private void stopGarbageProcessingTimelines() {
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
        if (isMultiplayerMode && playerNumber > 0) {
            // Handle multiplayer game over
            if (playerNumber == 1) {
                if (timeLine1 != null) timeLine1.stop();
                isGameOver1.set(true);
                // Stop the other player's game as well (but don't mark them as game over so winning panel shows)
                if (timeLine2 != null) timeLine2.stop();
            } else if (playerNumber == 2) {
                if (timeLine2 != null) timeLine2.stop();
                isGameOver2.set(true);
                // Stop the other player's game as well (but don't mark them as game over so winning panel shows)
                if (timeLine1 != null) timeLine1.stop();
            }
            
            // Stop garbage processing timelines
            stopGarbageProcessingTimelines();
            
            // Don't clear game panels here - keep them visible so players can see the final game state
            // Game panels will be cleared when restarting or quitting
            
            // Hide pause panel if visible
            if (multiplayerScreen != null) {
                multiplayerScreen.hidePausePanel();
            }
            if (pausePanel != null) {
                pausePanel.setVisible(false);
            }
            isPause.set(false);
            
            // Stop game music when game ends in multiplayer mode
            audioManager.stopGameMusic();
            
            // If only one player is game over, show winning panel for the other player
            // Also set the winning player's game over flag to prevent further input
            if (isGameOver1.get() && !isGameOver2.get()) {
                // Player 1 lost, Player 2 wins - stop Player 2 from controlling
                isGameOver2.set(true);
                showWinningPanel(2);
            } else if (isGameOver2.get() && !isGameOver1.get()) {
                // Player 2 lost, Player 1 wins - stop Player 1 from controlling
                isGameOver1.set(true);
                showWinningPanel(1);
            } else if (isGameOver1.get() && isGameOver2.get()) {
                // Both players are game over
                // Hide winning panel if visible
                if (multiplayerScreen != null) {
                    multiplayerScreen.hideWinningPanel();
                }
                // Play game over sound (game music already stopped above)
                audioManager.playGameOver();
                // Could show a multiplayer game over screen here
            }
            return;
        }
        
        // Single player game over
        if (timeLine != null) timeLine.stop();
        
        // Stop timer
        stopTimer();
        
        // Stop game music and play game over sound
        audioManager.stopGameMusic();
        audioManager.playGameOver();
        
        // Hide pause panel if visible
        if (pausePanel != null) {
            pausePanel.setVisible(false);
        }
        if (multiplayerScreen != null) {
            multiplayerScreen.hidePausePanel();
        }
        isPause.set(false);
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
                    // Reset the game board state
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
                isGameOver.set(false);
                gameStarted = false;
                if (timeLine != null) timeLine.stop();
                
                // Reset timer
                resetTimer();
                
                // Stop game over sound and play main menu music
                audioManager.playMainMenuMusic();
            });
            
            gameOverPanel.setVisible(true);
        }
        isGameOver.set(true);
    }

    public void newGame(ActionEvent actionEvent) {
        if (timeLine != null) timeLine.stop();
        if (gameOverPanel != null) gameOverPanel.setVisible(false);
        
        // Reset timer for new game
        resetTimer();
        
        // Stop game over sound and start game music
        audioManager.playGameMusic();
        
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
            if (!isMultiplayerMode) {
                gameBoard.setOnKeyPressed(e -> inputHandler.handleKeyPress(e, isMultiplayerMode, gameStarted));
                gameBoard.setOnKeyReleased(e -> inputHandler.handleKeyRelease(e, isMultiplayerMode));
            }
            gameBoard.setFocusTraversable(true);
            gameBoard.requestFocus();
        }
        if (timeLine != null) timeLine.play();
        
        // Start timer for new game (single player only)
        if (!isMultiplayerMode) {
            startTimer();
        }
        
        isPause.set(false);
        isGameOver.set(false);
        gameStarted = true;
        if (mainMenuPanel != null) {
            mainMenuPanel.setVisible(false);
        }
        if (pausePanel != null) {
            pausePanel.setVisible(false);
        }
        
        // Ensure settings panel is in gameStack for single player mode (defensive check)
        if (!isMultiplayerMode && gameStack != null && settingsPanel != null) {
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
        if (!gameStarted && mainMenuPanel != null) {
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
        gameStarted = true;
        isGameOver.set(false);
        isPause.set(false);
        
        // Stop main menu music and start game music
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
        if (!isMultiplayerMode && gameStack != null && settingsPanel != null) {
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
        if (!isMultiplayerMode && nextBricksPanel != null && singlePlayerScreen != null) {
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
                    singlePlayerScreen.updateNextBricks(simpleBoard.getNextBricks(), gameStarted);
                }
            }
        }
        
        // Refresh the brick display with stored brick data
        // The brick data was stored when initGameView was called
        if (singlePlayerScreen != null) {
            ViewData currentBrickData = singlePlayerScreen.getCurrentBrickData();
            if (currentBrickData != null) {
                singlePlayerScreen.refreshBrick(currentBrickData, gameStarted);
            }
        }
        
        if (timeLine != null) {
            timeLine.play();
        }
        
        // Start timer for single player mode
        if (!isMultiplayerMode) {
            startTimer();
        }
        
        if (gameBoard != null) {
            // Ensure keyboard handlers are attached for single player mode
            if (!isMultiplayerMode) {
                gameBoard.setOnKeyPressed(e -> inputHandler.handleKeyPress(e, isMultiplayerMode, gameStarted));
                gameBoard.setOnKeyReleased(e -> inputHandler.handleKeyRelease(e, isMultiplayerMode));
            }
            gameBoard.setFocusTraversable(true);
            gameBoard.requestFocus();
        }
    }

    public void quitGame() {
        javafx.application.Platform.exit();
    }

    public void pauseGame(ActionEvent actionEvent) {
        // Only allow pause/resume when game is actually playing
        if (!gameStarted) {
            return; // Game hasn't started, don't allow pause
        }
        
        // Check if game is over - if so, don't allow pause
        if (isMultiplayerMode) {
            if (isGameOver1.get() || isGameOver2.get()) {
                return; // Game is over, don't allow pause
            }
        } else {
            if (isGameOver.get()) {
                return; // Game is over, don't allow pause
            }
        }
        
        if (isMultiplayerMode) {
            if (!isPause.get()) {
                // Pause the game
                if (timeLine1 != null) timeLine1.pause();
                if (timeLine2 != null) timeLine2.pause();
                // Pause game music - AudioManager doesn't have pause, so we stop it
                audioManager.stopGameMusic();
                // Pause multiplayer timer
                pauseMultiplayerTimer();
                isPause.set(true);
                // Pause garbage processing timelines
                if (garbageProcessTimeline1 != null) {
                    garbageProcessTimeline1.pause();
                }
                if (garbageProcessTimeline2 != null) {
                    garbageProcessTimeline2.pause();
                }
                // Show pause panel for multiplayer
                if (multiplayerScreen != null) {
                    multiplayerScreen.showPausePanel();
                }
            } else {
                // Resume the game
                if (timeLine1 != null) { timeLine1.play(); updateTimelineRate(1); }
                if (timeLine2 != null) { timeLine2.play(); updateTimelineRate(2); }
                // Resume game music
                audioManager.playGameMusic();
                // Resume multiplayer timer
                resumeMultiplayerTimer();
                isPause.set(false);
                // Resume garbage processing timelines
                if (garbageProcessTimeline1 != null) {
                    garbageProcessTimeline1.play();
                }
                if (garbageProcessTimeline2 != null) {
                    garbageProcessTimeline2.play();
                }
                // Hide pause panel for multiplayer
                if (multiplayerScreen != null) {
                    multiplayerScreen.hidePausePanel();
                }
            }
        } else {
            if (!isPause.get()) {
                // Pause the game
                if (timeLine != null) timeLine.pause();
                // Pause game music - AudioManager doesn't have pause, so we stop it
                audioManager.stopGameMusic();
                // Pause timer
                pauseTimer();
                isPause.set(true);
                // Show pause panel
                if (pausePanel != null) {
                    pausePanel.setVisible(true);
                }
            } else {
                // Resume the game
                if (timeLine != null) { timeLine.play(); updateTimelineRate(); }
                // Resume game music
                audioManager.playGameMusic();
                // Resume timer
                resumeTimer();
                isPause.set(false);
                // Hide pause panel
                if (pausePanel != null) {
                    pausePanel.setVisible(false);
                }
            }
        }
        if (gameBoard != null) gameBoard.requestFocus();
    }
    
    private void initializePausePanel() {
        if (pausePanel == null) return;
        setupPausePanelActions(pausePanel);
    }
    
    private void setupPausePanelActions(PausePanel panel) {
        // Resume action - just resume the game
        panel.setOnResumeAction(() -> {
            pauseGame(null);
        });
        
        // Settings action - show settings panel
        panel.setOnSettingsAction(() -> {
            showSettingsFromPause();
        });
        
        // Restart action - start a new game
        panel.setOnRestartAction(() -> {
            if (isMultiplayerMode) {
                // Clear all multiplayer game panels and side panels before restarting
                clearMultiplayerGamePanels();
                
                // Re-initialize multiplayer panels to ensure consistent container and side panel sizing
                // This fixes issues where sizes differ after restart
                if (multiplayerScreen != null) {
                    multiplayerScreen.initializeMultiplayerPanels();
                    multiplayerScreen.show();
                }
                
                // For multiplayer, restart both games
                if (gameController1 != null) {
                    gameController1.createNewGame();
                }
                if (gameController2 != null) {
                    gameController2.createNewGame();
                }
                isPause.set(false);
                if (pausePanel != null) {
                    pausePanel.setVisible(false);
                }
                if (multiplayerScreen != null) {
                    multiplayerScreen.hidePausePanel();
                }
                // Reset game over states
                isGameOver1.set(false);
                isGameOver2.set(false);
                
                // Hide winning panel if visible
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
                startGarbageProcessingTimelines();
                
                // Restart multiplayer timer
                resetMultiplayerTimer();
                startMultiplayerTimer();
                
                // Restart music
                audioManager.playGameMusic();
            } else {
                // For single player, use newGame method
                newGame(null);
                if (pausePanel != null) {
                    pausePanel.setVisible(false);
                }
            }
        });
        
        // Quit to main menu action
        panel.setOnQuitAction(() -> {
            // Stop all timelines
            if (timeLine != null) timeLine.stop();
            if (timeLine1 != null) timeLine1.stop();
            if (timeLine2 != null) timeLine2.stop();
            
            // Hide pause panels
            if (pausePanel != null) {
                pausePanel.setVisible(false);
            }
            if (multiplayerScreen != null) {
                multiplayerScreen.hidePausePanel();
            }
            
            // Reset pause state
            isPause.set(false);
            
            // Reset game states
            isGameOver.set(false);
            isGameOver1.set(false);
            isGameOver2.set(false);
            gameStarted = false;
            
            // Hide winning panel if visible
            hideWinningPanel();
            
            // Hide game over panel if visible
            if (gameOverPanel != null) {
                gameOverPanel.setVisible(false);
            }
            
            // Stop game music and play main menu music
            audioManager.stopAll();
            audioManager.playMainMenuMusic();
            
            // Hide bottom panel
            // For single player mode, clear the game board display
            if (!isMultiplayerMode) {
                // Reset the game board state
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
                // Hide game panel (background grid) but keep gameBoard visible for main menu
                if (gamePanel != null) {
                    gamePanel.setVisible(false);
                }
                // Keep gameBoard visible so main menu can be shown
                // The main menu panel is inside gameBoard's StackPane
            }
            
            // If multiplayer, restore single player view
            if (isMultiplayerMode) {
                // Clear all multiplayer game panels and bricks before quitting
                clearMultiplayerGamePanels();
                
                isMultiplayerMode = false;
                
                // Clear multiplayer controllers and listeners
                stopGarbageProcessingTimelines();
                stopMultiplayerTimer();
                gameController1 = null;
                gameController2 = null;
                eventListener1 = null;
                eventListener2 = null;
                timeLine1 = null;
                timeLine2 = null;
                garbageProcessTimeline1 = null;
                garbageProcessTimeline2 = null;
                
                // Hide multiplayer container and wrapper
                if (multiplayerScreen != null) {
                    multiplayerScreen.hide();
                    multiplayerScreen.hidePausePanel();
                    multiplayerScreen.hideWinningPanel();
                    multiplayerScreen.hideSettingsOverlay();
                    // Move settings panel back to gameStack for single player mode
                    if (settingsPanel != null) {
                        settingsPanel.setVisible(false);
                    }
                }
                
                // Hide winning panel if visible
                hideWinningPanel();
                
                // Ensure settings panel is in gameStack for single player mode (do this regardless of overlay state)
                if (settingsPanel != null && gameStack != null) {
                    // Remove from any other parent first (defensive)
                    javafx.scene.Parent currentParent = settingsPanel.getParent();
                    if (currentParent != null && currentParent != gameStack) {
                        if (currentParent instanceof javafx.scene.layout.Pane) {
                            ((javafx.scene.layout.Pane) currentParent).getChildren().remove(settingsPanel);
                        }
                    }
                    // Ensure it's in gameStack
                    if (!gameStack.getChildren().contains(settingsPanel)) {
                        gameStack.getChildren().add(settingsPanel);
                    }
                    // Ensure it's properly configured
                    settingsPanel.setVisible(false);
                    settingsPanel.setManaged(true);
                }
                
                // Remove scene event filters from multiplayer mode
                if (gameBoard != null && gameBoard.getScene() != null) {
                    javafx.scene.Scene scene = gameBoard.getScene();
                    if (sceneKeyPressedHandler != null) {
                        scene.removeEventFilter(KeyEvent.KEY_PRESSED, sceneKeyPressedHandler);
                    }
                    if (sceneKeyReleasedHandler != null) {
                        scene.removeEventFilter(KeyEvent.KEY_RELEASED, sceneKeyReleasedHandler);
                    }
                    sceneKeyPressedHandler = null;
                    sceneKeyReleasedHandler = null;
                }
                
                // Restore original panels
                BorderPane rootPane = getRootBorderPane();
                if (rootPane != null) {
                    if (originalLeftPanel != null) {
                        originalLeftPanel.setVisible(true);
                        originalLeftPanel.setManaged(true);
                        rootPane.setLeft(originalLeftPanel);
                    }
                    if (originalRightPanel != null) {
                        originalRightPanel.setVisible(true);
                        originalRightPanel.setManaged(true);
                        rootPane.setRight(originalRightPanel);
                    }
                }
                // Show single player game board
                if (gameBoard != null) {
                    gameBoard.setVisible(true);
                    gameBoard.setManaged(true);
                    gameBoard.setFocusTraversable(true);
                    // Restore node-level keyboard handlers for single player mode
                    gameBoard.setOnKeyPressed(e -> inputHandler.handleKeyPress(e, isMultiplayerMode, gameStarted));
                    gameBoard.setOnKeyReleased(e -> inputHandler.handleKeyRelease(e, isMultiplayerMode));
                }
                
                // Restore game panel visibility (grid background) for main menu
                if (gamePanel != null) {
                    gamePanel.setVisible(true);
                }
                
                // Show single player panels
                if (holdBrickPanel != null) {
                    holdBrickPanel.setVisible(true);
                }
                // Keep next bricks panel container visible, but hide the brick previews
                if (nextBricksPanel != null && singlePlayerScreen != null) {
                    nextBricksPanel.setVisible(true);
                    // Hide the individual brick panes when main menu is visible
                    List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
                    if (nextBrickPanes != null) {
                        for (GridPane pane : nextBrickPanes) {
                            if (pane != null) {
                                pane.setVisible(false);
                            }
                        }
                    }
                }
                if (scoreLabel != null) {
                    scoreLabel.setVisible(true);
                }
                if (levelLabel != null) {
                    levelLabel.setVisible(true);
                }
                if (linesLabel != null) {
                    linesLabel.setVisible(true);
                }
                
                // Ensure single player eventListener exists - if not, create a new GameController
                // This handles the case where single player was never initialized or was cleared
                if (eventListener == null) {
                    new GameController(this);
                    // Hide next brick panes after GameController creation (it may have called updateNextBricks)
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
                } else {
                    // If eventListener exists, rebind level and lines to ensure they work
                    // This is needed in case the bindings were broken
                    if (eventListener instanceof GameController) {
                        GameController gameController = (GameController) eventListener;
                        if (gameController.getBoard() instanceof SimpleBoard) {
                            SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                            bindLevel(simpleBoard.levelProperty(), 0);
                            bindLines(simpleBoard.linesProperty(), 0);
                        }
                    }
                }
                
                // Request focus on gameBoard so it can receive keyboard events
                Platform.runLater(() -> {
                    if (gameBoard != null) {
                        gameBoard.requestFocus();
                    }
                });
            }
            
            // Show main menu
            if (mainMenuPanel != null) {
                mainMenuPanel.setVisible(true);
            }
            
            // Ensure gameBoard is visible for single player mode (main menu is inside it)
            if (!isMultiplayerMode && gameBoard != null) {
                gameBoard.setVisible(true);
                gameBoard.setManaged(true);
            }
            
            // Hide game board panels
            if (brickPanel != null) {
                brickPanel.setVisible(false);
            }
            if (ghostPanel != null) {
                ghostPanel.setVisible(false);
            }
            // Show game panel (background grid) for main menu - it should be visible
            if (!isMultiplayerMode && gamePanel != null) {
                gamePanel.setVisible(true);
            }
        });
    }

    public void updateNextBricks(List<Brick> nextBricks) {
        updateNextBricks(nextBricks, 0);
    }
    
    public void updateNextBricks(List<Brick> nextBricks, int playerNumber) {
        if (isMultiplayerMode && playerNumber > 0 && multiplayerScreen != null) {
            multiplayerScreen.updateNextBricks(nextBricks, playerNumber);
            return;
        }
        
        // Single player mode - use SinglePlayerScreen
        if (singlePlayerScreen != null) {
            singlePlayerScreen.updateNextBricks(nextBricks, gameStarted);
        }
    }

    public void updateHoldBrick(Brick heldBrick) {
        updateHoldBrick(heldBrick, 0);
    }
    
    public void updateHoldBrick(Brick heldBrick, int playerNumber) {
        if (isMultiplayerMode && playerNumber > 0 && multiplayerScreen != null) {
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
        if (isMultiplayerMode && playerNumber > 0) {
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
        if (!isMultiplayerMode) {
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
        timerManager.stopSinglePlayerTimer();
        // Reset is handled by startTimer
    }
    
    private void startMultiplayerTimer() {
        if (isMultiplayerMode) {
            timerManager.startMultiplayerTimer();
        }
    }
    
    private void stopMultiplayerTimer() {
        timerManager.stopMultiplayerTimer();
    }
    
    private void pauseMultiplayerTimer() {
        timerManager.pauseMultiplayerTimer();
    }
    
    private void resumeMultiplayerTimer() {
        timerManager.resumeMultiplayerTimer();
    }
    
    private void resetMultiplayerTimer() {
        timerManager.stopMultiplayerTimer();
    }

    
}
