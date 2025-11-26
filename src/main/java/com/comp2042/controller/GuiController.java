package com.comp2042.controller;

import com.comp2042.event.EventSource;
import com.comp2042.event.EventType;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.logic.bricks.Brick;
import com.comp2042.model.DownData;
import com.comp2042.model.ViewData;
import com.comp2042.view.ColorStrategy;
import com.comp2042.view.GameOverPanel;
import com.comp2042.view.PausePanel;
import com.comp2042.view.MainMenuPanel;
import com.comp2042.view.NotificationPanel;
import com.comp2042.view.SettingsPanel;
import com.comp2042.view.WinningPanel;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    // Use constants from GameConstants instead of magic numbers
    private static final int BRICK_SIZE = GameConstants.BRICK_SIZE;
    private static final int GRID_GAP = GameConstants.GRID_GAP;
    private static final int BOARD_WIDTH = GameConstants.BOARD_WIDTH;
    private static final int BOARD_HEIGHT = GameConstants.BOARD_HEIGHT;
    private static final int GAME_PANEL_WIDTH = GameConstants.GAME_PANEL_WIDTH;
    private static final int GAME_PANEL_HEIGHT = GameConstants.GAME_PANEL_HEIGHT;
    private static final double SOFT_DROP_RATE = GameConstants.SOFT_DROP_RATE;
    private ViewData currentBrickData; // Store current brick data for countdown refresh

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

    // Multiplayer fields
    private HBox multiplayerContainer;
    private boolean isMultiplayerMode = false;
    private PausePanel multiplayerPausePanel; // Separate pause panel for multiplayer
    private javafx.scene.layout.StackPane multiplayerPauseOverlay; // Overlay for multiplayer pause panel
    private javafx.scene.layout.StackPane multiplayerSettingsOverlay; // Overlay for multiplayer settings panel
    private javafx.scene.layout.StackPane multiplayerWrapper; // Wrapper StackPane to overlay pause panel on multiplayer container
    private javafx.scene.layout.StackPane multiplayerReadyOverlay; // Overlay for ready panel
    private javafx.scene.layout.StackPane multiplayerWinningOverlay; // Overlay for winning panel
    private BorderPane readyPanel; // Ready panel for multiplayer
    private com.comp2042.view.WinningPanel multiplayerWinningPanel; // Winning panel for multiplayer
    private boolean player1Ready = false;
    private boolean player2Ready = false;
    private GridPane gamePanel1, gamePanel2;
    private GridPane brickPanel1, brickPanel2;
    private GridPane ghostPanel1, ghostPanel2;
    private StackPane gameStack1, gameStack2;
    private Rectangle[][] displayMatrix1, displayMatrix2;
    
    // Multiplayer side panels
    private VBox leftPanelPlayer1, rightPanelPlayer2;
    private GridPane holdBrickPanel1, holdBrickPanel2;
    private VBox nextBricksPanel1, nextBricksPanel2;
    private Rectangle[][] holdBrickRectangles1, holdBrickRectangles2;
    private List<GridPane> nextBrickPanes1 = new ArrayList<>();
    private List<GridPane> nextBrickPanes2 = new ArrayList<>();
    private Label scoreLabel1, scoreLabel2;
    private Label levelLabel1, levelLabel2;
    
    // Multiplayer game controllers and timelines
    private GameController gameController1, gameController2;
    private Timeline timeLine1, timeLine2;
    private Timeline garbageProcessTimeline1, garbageProcessTimeline2; // Timelines for processing garbage queues
    private InputEventListener eventListener1, eventListener2;
    private ViewData currentBrickData1, currentBrickData2;
    private BooleanProperty isGameOver1 = new SimpleBooleanProperty(false);
    private BooleanProperty isGameOver2 = new SimpleBooleanProperty(false);
    
    // Store scene filter handlers to avoid duplicates
    private javafx.event.EventHandler<KeyEvent> sceneKeyPressedHandler;
    private javafx.event.EventHandler<KeyEvent> sceneKeyReleasedHandler;
    
    // Store original panels to restore later
    private VBox originalLeftPanel, originalRightPanel;

    private Rectangle[][] displayMatrix;
    private InputEventListener eventListener;
    private Timeline timeLine;
    // Timer management - delegated to TimerManager
    private final TimerManager timerManager = new TimerManager();
    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);
    private boolean gameStarted = false;
    private int currentLevel = 1;
    private javafx.beans.value.ChangeListener<? super Number> levelChangeListener;

    private Rectangle[][] holdBrickRectangles;
    private List<GridPane> nextBrickPanes = new ArrayList<>();
    private HighScoreManager highScoreManager = new HighScoreManager();
    
    // Audio management - delegated to AudioManager
    private final AudioManager audioManager = new AudioManager();
    
    // Input handling - delegated to InputHandler (dependency injection)
    private final InputHandler inputHandler = new InputHandler(KeyBindingsManager.getInstance());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        initializeGamePanel();
        initializeHoldPanel();
        initializeNextBricksPanel();
        initializeInfoPanel();
        
        // Initialize audio manager
        audioManager.initialize();
        
        // Initialize timer manager
        timerManager.setSinglePlayerTimerLabel(timerLabel);

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
                GuiController.this.refreshBrick(viewData);
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
                return player1Ready;
            }
            
            @Override
            public boolean isPlayer2Ready() {
                return player2Ready;
            }
            
            @Override
            public void setPlayer1Ready(boolean ready) {
                player1Ready = ready;
            }
            
            @Override
            public void setPlayer2Ready(boolean ready) {
                player2Ready = ready;
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
                GuiController.this.updateReadyLabels();
            }
            
            @Override
            public void checkBothReady() {
                GuiController.this.checkBothReady();
            }
        });
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
                if (ghostPanel1 != null) {
                    ghostPanel1.setVisible(newVal && gameStarted);
                }
                if (ghostPanel2 != null) {
                    ghostPanel2.setVisible(newVal && gameStarted);
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
            
            if (isMultiplayerMode) {
                // Hide pause overlay for multiplayer
                if (multiplayerPauseOverlay != null) {
                    multiplayerPauseOverlay.setVisible(false);
                    multiplayerPauseOverlay.setManaged(false);
                    multiplayerPauseOverlay.setMouseTransparent(true);
                }
                // Move settings panel from gameStack to multiplayer overlay if needed
                if (multiplayerSettingsOverlay != null) {
                    // Remove from gameStack if it's there
                    if (gameStack != null && gameStack.getChildren().contains(settingsPanel)) {
                        gameStack.getChildren().remove(settingsPanel);
                    }
                    // Add to multiplayer overlay if not already there
                    if (!multiplayerSettingsOverlay.getChildren().contains(settingsPanel)) {
                        settingsPanel.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
                        multiplayerSettingsOverlay.getChildren().add(settingsPanel);
                    }
                    // Make sure settings panel is visible and managed
                    settingsPanel.setVisible(true);
                    settingsPanel.setManaged(true);
                    // Show settings overlay for multiplayer (this should be on top of pause overlay)
                    multiplayerSettingsOverlay.setVisible(true);
                    multiplayerSettingsOverlay.setManaged(true);
                    multiplayerSettingsOverlay.setMouseTransparent(false);
                    // Ensure settings overlay is on top by bringing it to front
                    if (multiplayerWrapper != null && multiplayerWrapper.getChildren().contains(multiplayerSettingsOverlay)) {
                        multiplayerWrapper.getChildren().remove(multiplayerSettingsOverlay);
                        multiplayerWrapper.getChildren().add(multiplayerSettingsOverlay);
                    }
                }
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
                    if (multiplayerSettingsOverlay != null && multiplayerSettingsOverlay.getChildren().contains(settingsPanel)) {
                        multiplayerSettingsOverlay.getChildren().remove(settingsPanel);
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
        if (multiplayerWinningOverlay != null && multiplayerWinningPanel != null) {
            // Stop multiplayer timer
            stopMultiplayerTimer();
            
            // Set the winner
            multiplayerWinningPanel.setWinner(winnerPlayerNumber);
            // Set the time used
            multiplayerWinningPanel.setTimeUsed(timerManager.getMultiplayerElapsedSeconds());
            // Show winning overlay
            multiplayerWinningOverlay.setVisible(true);
            multiplayerWinningOverlay.setManaged(true);
            multiplayerWinningOverlay.setMouseTransparent(false);
            // Bring winning overlay to front
            if (multiplayerWrapper != null && multiplayerWrapper.getChildren().contains(multiplayerWinningOverlay)) {
                multiplayerWrapper.getChildren().remove(multiplayerWinningOverlay);
                multiplayerWrapper.getChildren().add(multiplayerWinningOverlay);
            }
            // Ensure container and wrapper maintain their sizes and layout
            // The overlay should not affect the underlying container sizing
            if (multiplayerContainer != null) {
                multiplayerContainer.setManaged(true);
            }
            if (multiplayerWrapper != null) {
                multiplayerWrapper.setManaged(true);
            }
            // Play winner sound
            audioManager.playWinner();
        }
    }
    
    private void hideWinningPanel() {
        if (multiplayerWinningOverlay != null) {
            multiplayerWinningOverlay.setVisible(false);
            multiplayerWinningOverlay.setManaged(false);
            multiplayerWinningOverlay.setMouseTransparent(true);
        }
    }
    
    private void hideSettings() {
        if (settingsPanel != null) {
            if (isMultiplayerMode) {
                // Hide settings panel first
                settingsPanel.setVisible(false);
                // Hide settings overlay for multiplayer
                if (multiplayerSettingsOverlay != null) {
                    multiplayerSettingsOverlay.setVisible(false);
                    multiplayerSettingsOverlay.setManaged(false);
                    multiplayerSettingsOverlay.setMouseTransparent(true);
                }
                // Note: Keep settingsPanel in multiplayerSettingsOverlay for next time
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
                    if (isMultiplayerMode) {
                        // Make sure pause overlay is restored properly
                        if (multiplayerPauseOverlay != null && multiplayerPausePanel != null) {
                            // Ensure pause panel is visible
                            multiplayerPausePanel.setVisible(true);
                            // Show and enable pause overlay
                            multiplayerPauseOverlay.setVisible(true);
                            multiplayerPauseOverlay.setManaged(true);
                            multiplayerPauseOverlay.setMouseTransparent(false);
                        }
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
        
        // Always re-initialize multiplayer panels to ensure consistent sizing
        // This fixes issues where container size differs between fresh launch and restart
        initializeMultiplayerPanels();
        
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
            gameBoard.setManaged(false); // Remove from layout calculations
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
                // Ensure VBox alignment is CENTER for vertical centering
                centerVBox.setAlignment(javafx.geometry.Pos.CENTER);
                // Ensure VBox fills available space both horizontally and vertically
                centerVBox.setFillWidth(true);
                
                // Create wrapper StackPane to overlay pause panel on multiplayer container
                if (multiplayerWrapper == null) {
                    multiplayerWrapper = new javafx.scene.layout.StackPane();
                    multiplayerWrapper.setAlignment(javafx.geometry.Pos.CENTER);
                    multiplayerWrapper.setMaxWidth(Double.MAX_VALUE);
                    multiplayerWrapper.setMaxHeight(Double.MAX_VALUE);
                    
                    // Add multiplayer container to wrapper
                    multiplayerWrapper.getChildren().add(multiplayerContainer);
                    
                    // Create pause panel overlay for multiplayer mode
                    multiplayerPauseOverlay = new javafx.scene.layout.StackPane();
                    multiplayerPauseOverlay.setAlignment(javafx.geometry.Pos.CENTER);
                    // Set overlay to fill available space so it can center the pause panel
                    multiplayerPauseOverlay.setMaxWidth(Double.MAX_VALUE);
                    multiplayerPauseOverlay.setMaxHeight(Double.MAX_VALUE);
                    multiplayerPauseOverlay.setPickOnBounds(true);
                    multiplayerPauseOverlay.setMouseTransparent(false);
                    
                    // Create a duplicate pause panel for multiplayer
                    multiplayerPausePanel = new PausePanel();
                    initializeMultiplayerPausePanel();
                    // Set the pause panel to use its preferred size and not expand
                    // This ensures it matches the size in single player mode
                    multiplayerPausePanel.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
                    multiplayerPauseOverlay.getChildren().add(multiplayerPausePanel);
                    multiplayerPauseOverlay.setVisible(false);
                    multiplayerPauseOverlay.setManaged(false);
                    
                    // Add pause overlay to wrapper (on top of multiplayer container)
                    multiplayerWrapper.getChildren().add(multiplayerPauseOverlay);
                    
                    // Create settings panel overlay for multiplayer mode
                    multiplayerSettingsOverlay = new javafx.scene.layout.StackPane();
                    multiplayerSettingsOverlay.setAlignment(javafx.geometry.Pos.CENTER);
                    multiplayerSettingsOverlay.setMaxWidth(Double.MAX_VALUE);
                    multiplayerSettingsOverlay.setMaxHeight(Double.MAX_VALUE);
                    multiplayerSettingsOverlay.setPickOnBounds(true);
                    multiplayerSettingsOverlay.setMouseTransparent(false);
                    multiplayerSettingsOverlay.setVisible(false);
                    multiplayerSettingsOverlay.setManaged(false);
                    
                    // Add settings overlay to wrapper (on top of pause overlay)
                    // Note: settingsPanel will be moved here dynamically when needed
                    multiplayerWrapper.getChildren().add(multiplayerSettingsOverlay);
                    
                    // Create winning panel overlay for multiplayer mode
                    multiplayerWinningOverlay = new javafx.scene.layout.StackPane();
                    multiplayerWinningOverlay.setAlignment(javafx.geometry.Pos.CENTER);
                    // Set overlay to fill available space but not affect container sizing
                    multiplayerWinningOverlay.setMaxWidth(Double.MAX_VALUE);
                    multiplayerWinningOverlay.setMaxHeight(Double.MAX_VALUE);
                    multiplayerWinningOverlay.setPickOnBounds(true);
                    multiplayerWinningOverlay.setMouseTransparent(false);
                    // Initially not managed - will be set to managed when shown
                    multiplayerWinningOverlay.setManaged(false);
                    
                    // Create winning panel
                    multiplayerWinningPanel = new WinningPanel();
                    multiplayerWinningPanel.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
                    setupWinningPanelActions(multiplayerWinningPanel);
                    multiplayerWinningOverlay.getChildren().add(multiplayerWinningPanel);
                    multiplayerWinningOverlay.setVisible(false);
                    
                    // Add winning overlay to wrapper (on top of settings overlay)
                    multiplayerWrapper.getChildren().add(multiplayerWinningOverlay);
                    
                    // Add wrapper to center VBox
                    centerVBox.getChildren().add(multiplayerWrapper);
                }
                
                multiplayerContainer.setVisible(true);
                multiplayerContainer.setManaged(true);
                multiplayerWrapper.setVisible(true);
                multiplayerWrapper.setManaged(true);
                
                // Request immediate layout update
                centerVBox.requestLayout();
            }
        }
        
        // Show ready panel instead of starting game immediately
        showReadyPanel();
        
        // Attach keyboard handlers to the scene for ready state
        Platform.runLater(() -> {
            attachKeyboardHandlersToScene();
        });
    }
    
    private void showReadyPanel() {
        // Reset ready states
        player1Ready = false;
        player2Ready = false;
        
        // Create ready panel if it doesn't exist
        if (readyPanel == null) {
            readyPanel = new BorderPane();
            readyPanel.getStyleClass().add("ready-panel");
            
            VBox mainContainer = new VBox(30);
            mainContainer.getStyleClass().add("ready-container");
            mainContainer.setAlignment(javafx.geometry.Pos.CENTER);
            
            // Title
            Label titleLabel = new Label("GET READY!");
            titleLabel.getStyleClass().add("ready-title");
            
            // Create two separate boxes for each player
            HBox playersContainer = new HBox(40);
            playersContainer.setAlignment(javafx.geometry.Pos.CENTER);
            
            // Player 1 box
            VBox player1Box = new VBox(15);
            player1Box.getStyleClass().add("ready-player-box");
            player1Box.setAlignment(javafx.geometry.Pos.CENTER);
            player1Box.setMinWidth(300);
            player1Box.setMinHeight(150);
            
            Label player1Title = new Label("PLAYER 1");
            player1Title.getStyleClass().add("ready-player-title");
            
            Label player1Label = new Label("Press SPACE to ready");
            player1Label.getStyleClass().add("ready-instruction");
            player1Label.setId("player1ReadyLabel");
            
            player1Box.getChildren().addAll(player1Title, player1Label);
            
            // Player 2 box
            VBox player2Box = new VBox(15);
            player2Box.getStyleClass().add("ready-player-box");
            player2Box.setAlignment(javafx.geometry.Pos.CENTER);
            player2Box.setMinWidth(300);
            player2Box.setMinHeight(150);
            
            Label player2Title = new Label("PLAYER 2");
            player2Title.getStyleClass().add("ready-player-title");
            
            Label player2Label = new Label("Press ENTER to ready");
            player2Label.getStyleClass().add("ready-instruction");
            player2Label.setId("player2ReadyLabel");
            
            player2Box.getChildren().addAll(player2Title, player2Label);
            
            playersContainer.getChildren().addAll(player1Box, player2Box);
            
            mainContainer.getChildren().addAll(titleLabel, playersContainer);
            readyPanel.setCenter(mainContainer);
        }
        
        // Create ready overlay if it doesn't exist
        if (multiplayerReadyOverlay == null) {
            multiplayerReadyOverlay = new javafx.scene.layout.StackPane();
            multiplayerReadyOverlay.setAlignment(javafx.geometry.Pos.CENTER);
            multiplayerReadyOverlay.setMaxWidth(Double.MAX_VALUE);
            multiplayerReadyOverlay.setMaxHeight(Double.MAX_VALUE);
            multiplayerReadyOverlay.setPickOnBounds(true);
            multiplayerReadyOverlay.setMouseTransparent(false);
            
            readyPanel.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
            multiplayerReadyOverlay.getChildren().add(readyPanel);
        }
        
        // Add ready overlay to wrapper if it exists, otherwise add to centerVBox
        if (multiplayerWrapper != null) {
            // Check if ready overlay is already in wrapper
            if (!multiplayerWrapper.getChildren().contains(multiplayerReadyOverlay)) {
                multiplayerWrapper.getChildren().add(multiplayerReadyOverlay);
            }
            multiplayerReadyOverlay.setVisible(true);
            multiplayerReadyOverlay.setManaged(true);
        } else {
            // Fallback: add to centerVBox if wrapper doesn't exist yet
            BorderPane rootPane = getRootBorderPane();
            if (rootPane != null && rootPane.getCenter() instanceof VBox) {
                VBox centerVBox = (VBox) rootPane.getCenter();
                if (!centerVBox.getChildren().contains(multiplayerReadyOverlay)) {
                    centerVBox.getChildren().add(multiplayerReadyOverlay);
                }
                multiplayerReadyOverlay.setVisible(true);
                multiplayerReadyOverlay.setManaged(true);
            }
        }
        
        // Update ready labels
        updateReadyLabels();
    }
    
    private void updateReadyLabels() {
        if (readyPanel == null) return;
        
        javafx.scene.Node center = readyPanel.getCenter();
        if (center instanceof VBox) {
            VBox mainContainer = (VBox) center;
            for (javafx.scene.Node node : mainContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox playersContainer = (HBox) node;
                    for (javafx.scene.Node playerBoxNode : playersContainer.getChildren()) {
                        if (playerBoxNode instanceof VBox) {
                            VBox playerBox = (VBox) playerBoxNode;
                            for (javafx.scene.Node labelNode : playerBox.getChildren()) {
                                if (labelNode instanceof Label) {
                                    Label label = (Label) labelNode;
                                    if ("player1ReadyLabel".equals(label.getId())) {
                                        if (player1Ready) {
                                            label.setText("READY");
                                            if (!label.getStyleClass().contains("ready-confirmed")) {
                                                label.getStyleClass().add("ready-confirmed");
                                            }
                                        } else {
                                            label.setText("Press SPACE to ready");
                                            label.getStyleClass().remove("ready-confirmed");
                                        }
                                    } else if ("player2ReadyLabel".equals(label.getId())) {
                                        if (player2Ready) {
                                            label.setText("READY");
                                            if (!label.getStyleClass().contains("ready-confirmed")) {
                                                label.getStyleClass().add("ready-confirmed");
                                            }
                                        } else {
                                            label.setText("Press ENTER to ready");
                                            label.getStyleClass().remove("ready-confirmed");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void checkBothReady() {
        if (player1Ready && player2Ready) {
            // Both players are ready, start the game
            if (multiplayerReadyOverlay != null) {
                multiplayerReadyOverlay.setVisible(false);
                multiplayerReadyOverlay.setManaged(false);
            }
            startMultiplayerGame();
        }
    }
    
    private void attachKeyboardHandlersToScene() {
        // Get the scene from any node (preferably gameBoard or multiplayerContainer)
        javafx.scene.Scene scene = null;
        if (gameBoard != null && gameBoard.getScene() != null) {
            scene = gameBoard.getScene();
        } else if (multiplayerContainer != null && multiplayerContainer.getScene() != null) {
            scene = multiplayerContainer.getScene();
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
        // Hide ready panel if still visible
        if (multiplayerReadyOverlay != null) {
            multiplayerReadyOverlay.setVisible(false);
            multiplayerReadyOverlay.setManaged(false);
        }
        
        // Create game controllers for both players
        gameController1 = new GameController(this, 1);
        gameController2 = new GameController(this, 2);
        
        // Reset game over states
        isGameOver1.set(false);
        isGameOver2.set(false);
        
        // Hide winning panel if visible
        hideWinningPanel();
        
        // Reset hard drop processing flags
        // Hard drop processing flags are now managed by InputHandler
        
        // Reset ready states
        player1Ready = false;
        player2Ready = false;
        
        // Start the game
        gameStarted = true;
        isPause.set(false);
        
        // Hide pause panel if visible
        if (multiplayerPauseOverlay != null) {
            multiplayerPauseOverlay.setVisible(false);
            multiplayerPauseOverlay.setManaged(false);
            multiplayerPauseOverlay.setMouseTransparent(true);
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
        if (brickPanel1 != null) {
            brickPanel1.setVisible(true);
        }
        if (brickPanel2 != null) {
            brickPanel2.setVisible(true);
        }
        if (ghostPanel1 != null && settingsPanel != null) {
            javafx.scene.control.CheckBox ghostCheckBox = settingsPanel.getGhostPieceCheckBox();
            boolean showGhost = ghostCheckBox != null && ghostCheckBox.isSelected();
            ghostPanel1.setVisible(showGhost);
        }
        if (ghostPanel2 != null && settingsPanel != null) {
            javafx.scene.control.CheckBox ghostCheckBox = settingsPanel.getGhostPieceCheckBox();
            boolean showGhost = ghostCheckBox != null && ghostCheckBox.isSelected();
            ghostPanel2.setVisible(showGhost);
        }
        
        // Make sure keyboard handlers are attached to scene
        Platform.runLater(() -> {
            attachKeyboardHandlersToScene();
            // Request focus on a visible component
            if (multiplayerContainer != null) {
                multiplayerContainer.setFocusTraversable(true);
                multiplayerContainer.requestFocus();
            }
        });
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
    
    private VBox createPlayerSidePanel(int playerNumber, int targetHeight, double scale) {
        VBox playerPanel = new VBox((int)(20 * scale)); // Scale spacing
        playerPanel.getStyleClass().add("side-panel");
        // Scale side panel width proportionally
        int sidePanelWidth = (int)(150 * scale); // Scale from base 150px
        playerPanel.setPrefWidth(sidePanelWidth);
        playerPanel.setMaxWidth(sidePanelWidth);
        
        // Calculate component sizes to match target height
        // Hold label: ~25px * scale
        // Hold panel: scaled
        // Spacing: 20px * scale
        // Next label: ~25px * scale
        // Next bricks: scaled to fill remaining space
        
        // Hold brick panel (no player label here - it's on the game field)
        VBox holdBox = new VBox();
        holdBox.getStyleClass().add("info-box");
        Label holdLabel = new Label("HOLD");
        holdLabel.getStyleClass().add("panel-title");
        
        GridPane holdPanel = new GridPane();
        holdPanel.setVgap(1);
        holdPanel.setHgap(1);
        holdPanel.getStyleClass().add("brick-preview");
        // Scale hold panel proportionally
        int holdPanelSize = (int)(100 * scale);
        holdPanel.setPrefWidth(holdPanelSize);
        holdPanel.setPrefHeight(holdPanelSize);
        holdPanel.setMaxWidth(holdPanelSize);
        holdPanel.setMaxHeight(holdPanelSize);
        
        Rectangle[][] holdRectangles = new Rectangle[4][4];
        int brickSize = (int)((BRICK_SIZE - 10) * scale); // Scale brick size
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Rectangle rect = new Rectangle(brickSize, brickSize);
                rect.setFill(Color.TRANSPARENT);
                rect.setStroke(Color.gray(0.3));
                holdRectangles[i][j] = rect;
                holdPanel.add(rect, j, i);
            }
        }
        
        // Store reference based on player number
        if (playerNumber == 1) {
            holdBrickPanel1 = holdPanel;
            holdBrickRectangles1 = holdRectangles;
        } else {
            holdBrickPanel2 = holdPanel;
            holdBrickRectangles2 = holdRectangles;
        }
        
        holdBox.getChildren().addAll(holdLabel, holdPanel);
        
        // Score panel
        VBox scoreBox = new VBox();
        scoreBox.getStyleClass().add("info-box");
        Label scoreTitleLabel = new Label("SCORE");
        scoreTitleLabel.getStyleClass().add("panel-title");
        
        Label scoreValueLabel = new Label("0");
        scoreValueLabel.getStyleClass().add("score-display");
        
        // Store reference based on player number
        if (playerNumber == 1) {
            scoreLabel1 = scoreValueLabel;
        } else {
            scoreLabel2 = scoreValueLabel;
        }
        
        scoreBox.getChildren().addAll(scoreTitleLabel, scoreValueLabel);
        
        // Level panel
        VBox levelBox = new VBox();
        levelBox.getStyleClass().add("info-box");
        Label levelTitleLabel = new Label("LEVEL");
        levelTitleLabel.getStyleClass().add("panel-title");
        
        Label levelValueLabel = new Label("1");
        levelValueLabel.getStyleClass().add("info-display");
        
        // Store reference based on player number
        if (playerNumber == 1) {
            levelLabel1 = levelValueLabel;
        } else {
            levelLabel2 = levelValueLabel;
        }
        
        levelBox.getChildren().addAll(levelTitleLabel, levelValueLabel);
        
        // Next bricks panel - only show 1 brick
        VBox nextBox = new VBox();
        nextBox.getStyleClass().add("info-box");
        Label nextLabel = new Label("NEXT");
        nextLabel.getStyleClass().add("panel-title");
        
        VBox nextBricksContainer = new VBox((int)(10 * scale)); // Scale spacing
        nextBricksContainer.getStyleClass().add("next-bricks-panel");
        // Calculate next bricks container size - only need space for 1 brick now
        int nextBrickSize = (int)(80 * scale);
        int nextBricksWidth = (int)(120 * scale);
        int nextBricksHeight = nextBrickSize; // Only need height for 1 brick
        
        nextBricksContainer.setPrefWidth(nextBricksWidth);
        nextBricksContainer.setPrefHeight(nextBricksHeight);
        nextBricksContainer.setMaxWidth(nextBricksWidth);
        nextBricksContainer.setMaxHeight(nextBricksHeight);
        
        // Create only 1 next brick preview
        List<GridPane> nextPanes = new ArrayList<>();
        GridPane pane = new GridPane();
        pane.setVgap(1);
        pane.setHgap(1);
        pane.setPrefSize(nextBrickSize, nextBrickSize);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                Rectangle rect = new Rectangle(brickSize, brickSize);
                rect.setFill(Color.TRANSPARENT);
                pane.add(rect, c, r);
            }
        }
        nextPanes.add(pane);
        nextBricksContainer.getChildren().add(pane);
        
        // Store reference based on player number
        if (playerNumber == 1) {
            nextBricksPanel1 = nextBricksContainer;
            nextBrickPanes1 = nextPanes;
        } else {
            nextBricksPanel2 = nextBricksContainer;
            nextBrickPanes2 = nextPanes;
        }
        
        nextBox.getChildren().addAll(nextLabel, nextBricksContainer);
        
        // Add all components to player panel: Hold, Score, Level, Next
        playerPanel.getChildren().addAll(holdBox, scoreBox, levelBox, nextBox);
        
        return playerPanel;
    }
    
    private void initializeMultiplayerPanels() {
        // Use larger scale factor to make everything bigger
        // Increased from 0.75 to 0.85 for larger display
        double scale = 0.85;
        
        // Calculate game board dimensions based on scale
        int scaledBrickSize = (int)(BRICK_SIZE * scale);
        int scaledPanelWidth = (int)(GAME_PANEL_WIDTH * scale);
        int scaledPanelHeight = (int)(GAME_PANEL_HEIGHT * scale); // ~510px at 0.85 scale
        
        // Calculate side panel dimensions to match game board height
        // Side panel needs to match the scaled game board height
        int targetSidePanelHeight = scaledPanelHeight; // Match game board height
        
        // Clear existing container if it exists (for restart scenarios)
        // This ensures consistent sizing between fresh launch and restart
        boolean containerExisted = (multiplayerContainer != null);
        if (multiplayerContainer != null) {
            // Remove container from wrapper if it exists, so we can re-add it properly
            if (multiplayerWrapper != null && multiplayerWrapper.getChildren().contains(multiplayerContainer)) {
                multiplayerWrapper.getChildren().remove(multiplayerContainer);
            }
            multiplayerContainer.getChildren().clear();
        } else {
            multiplayerContainer = new HBox(30);
            multiplayerContainer.getStyleClass().add("multiplayer-container");
        }
        
        multiplayerContainer.setAlignment(javafx.geometry.Pos.CENTER);
        // Set container to use preferred size and not expand beyond window
        multiplayerContainer.setMaxWidth(Double.MAX_VALUE);
        multiplayerContainer.setMaxHeight(Double.MAX_VALUE);
        // Initially set to not managed so it doesn't affect layout until shown
        multiplayerContainer.setManaged(false);
        multiplayerContainer.setVisible(false);
        
        // Create Player 1 container (side panel + game field)
        VBox player1Container = createPlayerContainer(1, scaledBrickSize, scaledPanelWidth, scaledPanelHeight, scale);
        
        // Create VS column with timer at top and VS label below
        VBox vsColumn = new VBox(10);
        vsColumn.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Create timer label for multiplayer (aligned with player titles)
        Label multiplayerTimerLabel = new Label("00:00");
        multiplayerTimerLabel.getStyleClass().add("multiplayer-timer-label");
        multiplayerTimerLabel.setAlignment(javafx.geometry.Pos.CENTER);
        multiplayerTimerLabel.setMaxWidth(Double.MAX_VALUE);
        timerManager.setMultiplayerTimerLabel(multiplayerTimerLabel);
        
        // Create VS label
        Label vsLabel = new Label("VS");
        vsLabel.getStyleClass().add("vs-label");
        
        // Add timer at top, then VS label below
        vsColumn.getChildren().addAll(multiplayerTimerLabel, vsLabel);
        
        // Create Player 2 container (game field + side panel)
        VBox player2Container = createPlayerContainer(2, scaledBrickSize, scaledPanelWidth, scaledPanelHeight, scale);
        
        multiplayerContainer.getChildren().addAll(player1Container, vsColumn, player2Container);
        
        // Re-add container to wrapper if it existed and wrapper exists
        if (containerExisted && multiplayerWrapper != null) {
            // Add container back to wrapper (should be first child, before overlays)
            if (!multiplayerWrapper.getChildren().contains(multiplayerContainer)) {
                multiplayerWrapper.getChildren().add(0, multiplayerContainer);
            }
        }
    }
    
    private VBox createPlayerContainer(int playerNumber, int brickSize, int panelWidth, int panelHeight, double scale) {
        // Outer VBox container with player label at top
        VBox playerContainer = new VBox(10);
        playerContainer.setAlignment(javafx.geometry.Pos.CENTER);
        playerContainer.getStyleClass().add("player-container");
        
        // Player label at the top, centered above entire container
        Label playerLabel = new Label("PLAYER " + playerNumber);
        playerLabel.getStyleClass().add("player-title");
        playerLabel.setAlignment(javafx.geometry.Pos.CENTER);
        playerLabel.setMaxWidth(Double.MAX_VALUE); // Allow label to span full width for centering
        
        // Inner HBox containing side panel and game field
        HBox contentContainer = new HBox(15);
        contentContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Create side panel (Hold + Next) - scale to match game board height
        VBox sidePanel = createPlayerSidePanel(playerNumber, panelHeight, scale);
        
        // Create game field (without player label)
        VBox gameField = createPlayerGameField(playerNumber, brickSize, panelWidth, panelHeight);
        
        // For Player 1: side panel on left, game field on right
        // For Player 2: game field on left, side panel on right
        if (playerNumber == 1) {
            contentContainer.getChildren().addAll(sidePanel, gameField);
            // Store reference to Player 1 side panel
            leftPanelPlayer1 = sidePanel;
        } else {
            contentContainer.getChildren().addAll(gameField, sidePanel);
            // Store reference to Player 2 side panel
            rightPanelPlayer2 = sidePanel;
        }
        
        // Add player label at top, then content container
        playerContainer.getChildren().addAll(playerLabel, contentContainer);
        
        return playerContainer;
    }
    
    private VBox createPlayerGameField(int playerNumber, int brickSize, int panelWidth, int panelHeight) {
        // Game field container - no player label here, it's at the top of player container
        VBox gameFieldContainer = new VBox();
        gameFieldContainer.setAlignment(javafx.geometry.Pos.CENTER);
        gameFieldContainer.getStyleClass().add("player-field");
        

        BorderPane gameFieldBoard = new BorderPane();
        gameFieldBoard.getStyleClass().add("gameBoard");

        int borderOffset = 4; // Border width from CSS
        int gridHeight = panelHeight + 1; // Grid extends 1px to close gap
        int totalHeight = gridHeight;
        gameFieldBoard.setPrefHeight(totalHeight);
        gameFieldBoard.setMaxHeight(totalHeight);
        gameFieldBoard.setMinHeight(totalHeight);
        gameFieldBoard.setPrefWidth(panelWidth + (borderOffset * 2));
        gameFieldBoard.setMaxWidth(panelWidth + (borderOffset * 2));
        gameFieldBoard.setMinWidth(panelWidth + (borderOffset * 2));
        
        // Use a simple Pane with exact pixel positioning to eliminate any gaps
        Pane borderWrapper = new Pane();
        borderWrapper.setPrefSize(panelWidth + (borderOffset * 2), totalHeight);
        borderWrapper.setMaxSize(panelWidth + (borderOffset * 2), totalHeight);
        borderWrapper.setMinSize(panelWidth + (borderOffset * 2), totalHeight);
        // Remove any default padding
        borderWrapper.setPadding(javafx.geometry.Insets.EMPTY);
        
        StackPane gameFieldStack = new StackPane();
        // Set size to match grid dimensions, add 1px height to close bottom gap
        gameFieldStack.setPrefSize(panelWidth, panelHeight + 1);
        gameFieldStack.setMaxSize(panelWidth, panelHeight + 1);
        gameFieldStack.setMinSize(panelWidth, panelHeight + 1);
        // Remove any default padding/alignment that might cause gaps
        gameFieldStack.setPadding(javafx.geometry.Insets.EMPTY);
        gameFieldStack.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        
        // Position the StackPane exactly at the border offset using layoutX/Y
        // Align with the 4px border on top, extend 1px at bottom to close gap
        gameFieldStack.setLayoutX(borderOffset);
        gameFieldStack.setLayoutY(borderOffset); // Align with top border
        // Enable pixel snapping for crisp rendering
        borderWrapper.setSnapToPixel(true);
        gameFieldStack.setSnapToPixel(true);
        
        // Initialize display matrix for this player
        Rectangle[][] matrix;
        if (playerNumber == 1) {
            if (displayMatrix1 == null) {
                displayMatrix1 = new Rectangle[BOARD_HEIGHT][BOARD_WIDTH];
            }
            matrix = displayMatrix1;
        } else {
            if (displayMatrix2 == null) {
                displayMatrix2 = new Rectangle[BOARD_HEIGHT][BOARD_WIDTH];
            }
            matrix = displayMatrix2;
        }
        
        // Create game panel - set exact dimensions, add 1px height to close bottom gap
        GridPane gameFieldPanel = new GridPane();
        gameFieldPanel.setHgap(GRID_GAP);
        gameFieldPanel.setVgap(GRID_GAP);
        gameFieldPanel.setPrefSize(panelWidth, panelHeight + 1);
        gameFieldPanel.setMaxSize(panelWidth, panelHeight + 1);
        gameFieldPanel.setMinSize(panelWidth, panelHeight + 1);
        initializeGameFieldPanel(gameFieldPanel, brickSize, matrix);
        
        // Create brick panel - set exact dimensions, add 1px height to close bottom gap
        GridPane gameFieldBrickPanel = new GridPane();
        gameFieldBrickPanel.setHgap(GRID_GAP);
        gameFieldBrickPanel.setVgap(GRID_GAP);
        gameFieldBrickPanel.setPrefSize(panelWidth, panelHeight + 1);
        gameFieldBrickPanel.setMaxSize(panelWidth, panelHeight + 1);
        gameFieldBrickPanel.setMinSize(panelWidth, panelHeight + 1);
        gameFieldBrickPanel.setMouseTransparent(true);
        initializeBrickPanel(gameFieldBrickPanel, brickSize);
        
        // Create ghost panel - set exact dimensions, add 1px height to close bottom gap
        GridPane gameFieldGhostPanel = new GridPane();
        gameFieldGhostPanel.setHgap(GRID_GAP);
        gameFieldGhostPanel.setVgap(GRID_GAP);
        gameFieldGhostPanel.setPrefSize(panelWidth, panelHeight + 1);
        gameFieldGhostPanel.setMaxSize(panelWidth, panelHeight + 1);
        gameFieldGhostPanel.setMinSize(panelWidth, panelHeight + 1);
        gameFieldGhostPanel.setMouseTransparent(true);
        initializeBrickPanel(gameFieldGhostPanel, brickSize);
        
        // Store references based on player number
        if (playerNumber == 1) {
            gamePanel1 = gameFieldPanel;
            brickPanel1 = gameFieldBrickPanel;
            ghostPanel1 = gameFieldGhostPanel;
            gameStack1 = gameFieldStack;
        } else {
            gamePanel2 = gameFieldPanel;
            brickPanel2 = gameFieldBrickPanel;
            ghostPanel2 = gameFieldGhostPanel;
            gameStack2 = gameFieldStack;
        }
        
        // Add panels to stack (layered)
        gameFieldStack.getChildren().addAll(gameFieldPanel, gameFieldGhostPanel, gameFieldBrickPanel);
        
        // Add StackPane to wrapper and set wrapper as center
        borderWrapper.getChildren().add(gameFieldStack);
        gameFieldBoard.setCenter(borderWrapper);
        // Add only game field board (no player label - it's at the top of player container)
        gameFieldContainer.getChildren().add(gameFieldBoard);
        
        return gameFieldContainer;
    }
    
    private void initializeGameFieldPanel(GridPane panel, int brickSize, Rectangle[][] matrix) {
        panel.getChildren().clear();
        panel.getColumnConstraints().clear();
        panel.getRowConstraints().clear();
        
        for (int c = 0; c < BOARD_WIDTH; c++) {
            ColumnConstraints cc = new ColumnConstraints(brickSize);
            cc.setPrefWidth(brickSize);
            cc.setMinWidth(brickSize);
            cc.setMaxWidth(brickSize);
            panel.getColumnConstraints().add(cc);
        }
        
        for (int r = 0; r < BOARD_HEIGHT; r++) {
            RowConstraints rc = new RowConstraints(brickSize);
            rc.setPrefHeight(brickSize);
            rc.setMinHeight(brickSize);
            rc.setMaxHeight(brickSize);
            panel.getRowConstraints().add(rc);
        }
        
        // Initialize rectangles in the matrix
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                Rectangle rect = new Rectangle(brickSize, brickSize);
                rect.setFill(Color.TRANSPARENT);
                rect.setStroke(Color.gray(0.2));
                rect.setStrokeWidth(0.5);
                matrix[i][j] = rect;
                panel.add(rect, j, i);
            }
        }
    }
    
    private void initializeBrickPanel(GridPane panel, int brickSize) {
        panel.getChildren().clear();
        panel.getColumnConstraints().clear();
        panel.getRowConstraints().clear();
        
        for (int c = 0; c < BOARD_WIDTH; c++) {
            ColumnConstraints cc = new ColumnConstraints(brickSize);
            panel.getColumnConstraints().add(cc);
        }
        
        for (int r = 0; r < BOARD_HEIGHT; r++) {
            RowConstraints rc = new RowConstraints(brickSize);
            panel.getRowConstraints().add(rc);
        }
    }
    
    // Audio methods are now handled by AudioManager

    private void initializeGamePanel() {
        if (gamePanel != null) {
            gamePanel.getChildren().clear();
            gamePanel.getColumnConstraints().clear();
            gamePanel.getRowConstraints().clear();
            gamePanel.setPrefSize(GAME_PANEL_WIDTH, GAME_PANEL_HEIGHT);

            for (int c = 0; c < BOARD_WIDTH; c++) {
                ColumnConstraints cc = new ColumnConstraints(BRICK_SIZE);
                cc.setPrefWidth(BRICK_SIZE);
                cc.setMinWidth(BRICK_SIZE);
                cc.setMaxWidth(BRICK_SIZE);
                gamePanel.getColumnConstraints().add(cc);
            }
            for (int r = 0; r < BOARD_HEIGHT; r++) {
                RowConstraints rc = new RowConstraints(BRICK_SIZE);
                rc.setPrefHeight(BRICK_SIZE);
                rc.setMinHeight(BRICK_SIZE);
                rc.setMaxHeight(BRICK_SIZE);
                gamePanel.getRowConstraints().add(rc);
            }

            displayMatrix = new Rectangle[BOARD_HEIGHT][BOARD_WIDTH];
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    Rectangle rect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.gray(0.2));
                    rect.setStrokeWidth(0.5);
                    displayMatrix[i][j] = rect;
                    gamePanel.add(rect, j, i);
                }
            }
        }

        if (brickPanel != null) {
            brickPanel.getChildren().clear();
            brickPanel.getColumnConstraints().clear();
            brickPanel.getRowConstraints().clear();
            for (int c = 0; c < BOARD_WIDTH; c++) {
                ColumnConstraints cc = new ColumnConstraints(BRICK_SIZE);
                brickPanel.getColumnConstraints().add(cc);
            }
            for (int r = 0; r < BOARD_HEIGHT; r++) {
                RowConstraints rc = new RowConstraints(BRICK_SIZE);
                brickPanel.getRowConstraints().add(rc);
            }
            brickPanel.setMouseTransparent(true);
        }

        if (ghostPanel != null) {
            ghostPanel.getChildren().clear();
            ghostPanel.getColumnConstraints().clear();
            ghostPanel.getRowConstraints().clear();
            for (int c = 0; c < BOARD_WIDTH; c++) {
                ColumnConstraints cc = new ColumnConstraints(BRICK_SIZE);
                ghostPanel.getColumnConstraints().add(cc);
            }
            for (int r = 0; r < BOARD_HEIGHT; r++) {
                RowConstraints rc = new RowConstraints(BRICK_SIZE);
                ghostPanel.getRowConstraints().add(rc);
            }
            ghostPanel.setMouseTransparent(true);
        }
    }

    private void initializeHoldPanel() {
        if (holdBrickPanel != null) {
            holdBrickPanel.getChildren().clear();
            holdBrickRectangles = new Rectangle[4][4];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    Rectangle rect = new Rectangle(BRICK_SIZE - 10, BRICK_SIZE - 10);
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.gray(0.3));
                    holdBrickRectangles[i][j] = rect;
                    holdBrickPanel.add(rect, j, i);
                }
            }
        }
    }

    private void initializeNextBricksPanel() {
        if (nextBricksPanel != null) {
            nextBrickPanes.clear();
            nextBricksPanel.getChildren().clear();
            for (int i = 0; i < 3; i++) {
                GridPane pane = new GridPane();
                pane.setVgap(1);
                pane.setHgap(1);
                pane.setPrefSize(80, 80);
                for (int r = 0; r < 4; r++) {
                    for (int c = 0; c < 4; c++) {
                        Rectangle rect = new Rectangle(BRICK_SIZE - 10, BRICK_SIZE - 10);
                        rect.setFill(Color.TRANSPARENT);
                        pane.add(rect, c, r);
                    }
                }
                nextBrickPanes.add(pane);
                nextBricksPanel.getChildren().add(pane);
            }
        }
    }

    private void initializeInfoPanel() {
        // Don't set text directly for scoreLabel - it will be bound to the score property
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

    // Key handling methods are now handled by InputHandler

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        initGameView(boardMatrix, brick, 0);
    }
    
    public void initGameView(int[][] boardMatrix, ViewData brick, int playerNumber) {
        if (isMultiplayerMode && playerNumber > 0) {
            // Initialize multiplayer game view
            refreshBrick(brick, playerNumber);
            refreshGameBackground(boardMatrix, playerNumber);
            
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
            refreshBrick(brick);
            refreshGameBackground(boardMatrix);

            if (timeLine != null) timeLine.stop();
            timeLine = new Timeline(new KeyFrame(Duration.millis(400), ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))));
            timeLine.setCycleCount(Timeline.INDEFINITE);

            updateTimelineRate();

            if (gameBoard != null) gameBoard.requestFocus();
        }
    }

    private javafx.scene.paint.Paint getFillColor(int brickType) {
        return ColorStrategy.getColorForBrickType(brickType);
    }

    private void refreshBrick(ViewData brick) {
        refreshBrick(brick, 0);
    }
    
    private void refreshBrick(ViewData brick, int playerNumber) {
        // Always store the current brick data
        if (brick != null) {
            if (playerNumber == 1) {
                currentBrickData1 = brick;
            } else if (playerNumber == 2) {
                currentBrickData2 = brick;
            } else {
                currentBrickData = brick;
            }
        }

        // Don't show brick if game hasn't started (menu is visible)
        if (!gameStarted && playerNumber == 0) {
            return;
        }
        if (isMultiplayerMode && !gameStarted) {
            return;
        }

        GridPane currentBrickPanel = brickPanel;
        GridPane currentGhostPanel = ghostPanel;
        InputEventListener currentEventListener = eventListener;
        int scaledBrickSize = BRICK_SIZE;
        
        if (isMultiplayerMode && playerNumber > 0) {
            currentBrickPanel = (playerNumber == 1) ? brickPanel1 : brickPanel2;
            currentGhostPanel = (playerNumber == 1) ? ghostPanel1 : ghostPanel2;
            currentEventListener = (playerNumber == 1) ? eventListener1 : eventListener2;
            // For multiplayer, use scaled brick size
            double scale = 0.85;
            scaledBrickSize = (int)(BRICK_SIZE * scale);
        }

        if (!isPause.get() && currentBrickPanel != null && brick != null) {
            // Clear both panels
            currentBrickPanel.getChildren().clear();
            if (currentGhostPanel != null) {
                currentGhostPanel.getChildren().clear();
            }
            
            int[][] data = brick.getBrickData();
            int offsetX = brick.getXPosition();
            int offsetY = brick.getYPosition();
            
            // Draw ghost piece first (behind the actual brick)
            if (currentGhostPanel != null && currentEventListener instanceof GameController) {
                GameController gameController = (GameController) currentEventListener;
                if (gameController.getBoard() instanceof SimpleBoard) {
                    SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                    java.awt.Point ghostPos = simpleBoard.getGhostPosition();
                    
                    int ghostX = (int) ghostPos.getX();
                    int ghostY = (int) ghostPos.getY();
                    
                    // Only show ghost if it's below the current position
                    // Individual cell bounds are checked inside the loop
                    if (ghostY > offsetY) {
                        for (int i = 0; i < data.length; i++) {
                            for (int j = 0; j < data[i].length; j++) {
                                if (data[j][i] != 0) {
                                    int cellX = ghostX + i;
                                    int cellY = ghostY + j;
                                    
                                    // Only draw if the cell is within the board bounds
                                    // This allows partial ghost display when brick is at left/right walls
                                    if (cellX >= 0 && cellY >= 0 && cellX < BOARD_WIDTH && cellY < BOARD_HEIGHT) {
                                        Rectangle ghostRect = new Rectangle(scaledBrickSize, scaledBrickSize);
                                        
                                        // Get the brick's color and make it semi-transparent
                                        javafx.scene.paint.Paint brickColor = getFillColor(data[j][i]);
                                        if (brickColor instanceof javafx.scene.paint.Color) {
                                            javafx.scene.paint.Color color = (javafx.scene.paint.Color) brickColor;
                                            // Create a semi-transparent version (30% opacity)
                                            javafx.scene.paint.Color ghostColor = new javafx.scene.paint.Color(
                                                color.getRed(),
                                                color.getGreen(),
                                                color.getBlue(),
                                                0.3
                                            );
                                            ghostRect.setFill(ghostColor);
                                            // Use a slightly darker stroke for visibility
                                            javafx.scene.paint.Color strokeColor = new javafx.scene.paint.Color(
                                                color.getRed() * 0.7,
                                                color.getGreen() * 0.7,
                                                color.getBlue() * 0.7,
                                                0.5
                                            );
                                            ghostRect.setStroke(strokeColor);
                                            ghostRect.setStrokeWidth(1.5);
                                        } else {
                                            // Fallback if color is not a Color object
                                            ghostRect.setFill(javafx.scene.paint.Color.rgb(255, 255, 255, 0.3));
                                            ghostRect.setStroke(javafx.scene.paint.Color.rgb(200, 200, 200, 0.5));
                                            ghostRect.setStrokeWidth(1.5);
                                        }
                                        
                                        ghostRect.setArcHeight(5);
                                        ghostRect.setArcWidth(5);
                                        currentGhostPanel.add(ghostRect, cellX, cellY);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Draw actual brick on top
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    if (data[j][i] != 0) {
                        Rectangle rect = new Rectangle(scaledBrickSize, scaledBrickSize);
                        rect.setFill(getFillColor(data[j][i]));
                        rect.setArcHeight(5);
                        rect.setArcWidth(5);
                        currentBrickPanel.add(rect, offsetX + i, offsetY + j);
                    }
                }
            }
        }
    }

    public void refreshGameBackground(int[][] board) {
        refreshGameBackground(board, 0);
    }
    
    public void refreshGameBackground(int[][] board, int playerNumber) {
        if (isMultiplayerMode && playerNumber > 0) {
            Rectangle[][] matrix = (playerNumber == 1) ? displayMatrix1 : displayMatrix2;
            if (matrix != null) {
                for (int i = 0; i < Math.min(BOARD_HEIGHT, board.length); i++) {
                    for (int j = 0; j < Math.min(BOARD_WIDTH, board[i].length); j++) {
                        if (matrix[i][j] != null) matrix[i][j].setFill(getFillColor(board[i][j]));
                    }
                }
            }
        } else {
            if (displayMatrix != null) {
                for (int i = 0; i < Math.min(BOARD_HEIGHT, board.length); i++) {
                    for (int j = 0; j < Math.min(BOARD_WIDTH, board[i].length); j++) {
                        if (displayMatrix[i][j] != null) displayMatrix[i][j].setFill(getFillColor(board[i][j]));
                    }
                }
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
        // Clear displayMatrix1 (Player 1 game panel)
        if (displayMatrix1 != null) {
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    if (displayMatrix1[i][j] != null) {
                        displayMatrix1[i][j].setFill(Color.TRANSPARENT);
                    }
                }
            }
        }
        
        // Clear displayMatrix2 (Player 2 game panel)
        if (displayMatrix2 != null) {
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    if (displayMatrix2[i][j] != null) {
                        displayMatrix2[i][j].setFill(Color.TRANSPARENT);
                    }
                }
            }
        }
        
        // Clear brickPanel1 (Player 1 current brick) - re-initialize to ensure grid structure exists
        if (brickPanel1 != null) {
            brickPanel1.getChildren().clear();
            // Re-initialize the grid structure
            double scale = 0.85;
            int brickSize = (int)(BRICK_SIZE * scale);
            initializeBrickPanel(brickPanel1, brickSize);
        }
        
        // Clear brickPanel2 (Player 2 current brick) - re-initialize to ensure grid structure exists
        if (brickPanel2 != null) {
            brickPanel2.getChildren().clear();
            // Re-initialize the grid structure
            double scale = 0.85;
            int brickSize = (int)(BRICK_SIZE * scale);
            initializeBrickPanel(brickPanel2, brickSize);
        }
        
        // Clear ghostPanel1 (Player 1 ghost piece) - re-initialize to ensure grid structure exists
        if (ghostPanel1 != null) {
            ghostPanel1.getChildren().clear();
            // Re-initialize the grid structure
            double scale = 0.85;
            int brickSize = (int)(BRICK_SIZE * scale);
            initializeBrickPanel(ghostPanel1, brickSize);
        }
        
        // Clear ghostPanel2 (Player 2 ghost piece) - re-initialize to ensure grid structure exists
        if (ghostPanel2 != null) {
            ghostPanel2.getChildren().clear();
            // Re-initialize the grid structure
            double scale = 0.85;
            int brickSize = (int)(BRICK_SIZE * scale);
            initializeBrickPanel(ghostPanel2, brickSize);
        }
        
        // Clear and re-initialize hold panels
        double scale = 0.85;
        int holdBrickSize = (int)((BRICK_SIZE - 10) * scale);
        
        if (holdBrickPanel1 != null) {
            holdBrickPanel1.getChildren().clear();
            // Initialize rectangles array if needed
            if (holdBrickRectangles1 == null) {
                holdBrickRectangles1 = new Rectangle[4][4];
            }
            // Re-initialize hold panel structure
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    Rectangle rect = new Rectangle(holdBrickSize, holdBrickSize);
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.gray(0.3));
                    holdBrickRectangles1[i][j] = rect;
                    holdBrickPanel1.add(rect, j, i);
                }
            }
        }
        if (holdBrickPanel2 != null) {
            holdBrickPanel2.getChildren().clear();
            // Initialize rectangles array if needed
            if (holdBrickRectangles2 == null) {
                holdBrickRectangles2 = new Rectangle[4][4];
            }
            // Re-initialize hold panel structure
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    Rectangle rect = new Rectangle(holdBrickSize, holdBrickSize);
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.gray(0.3));
                    holdBrickRectangles2[i][j] = rect;
                    holdBrickPanel2.add(rect, j, i);
                }
            }
        }
        
        // Clear and re-initialize next bricks panels
        int nextBrickSize = (int)(80 * scale);
        
        if (nextBricksPanel1 != null) {
            nextBricksPanel1.getChildren().clear();
            // Re-initialize next bricks panel structure (only 1 brick for multiplayer)
            if (nextBrickPanes1 != null) {
                nextBrickPanes1.clear();
                GridPane pane = new GridPane();
                pane.setVgap(1);
                pane.setHgap(1);
                pane.setPrefSize(nextBrickSize, nextBrickSize);
                for (int r = 0; r < 4; r++) {
                    for (int c = 0; c < 4; c++) {
                        Rectangle rect = new Rectangle(holdBrickSize, holdBrickSize);
                        rect.setFill(Color.TRANSPARENT);
                        pane.add(rect, c, r);
                    }
                }
                nextBrickPanes1.add(pane);
                nextBricksPanel1.getChildren().add(pane);
            }
        }
        if (nextBricksPanel2 != null) {
            nextBricksPanel2.getChildren().clear();
            // Re-initialize next bricks panel structure (only 1 brick for multiplayer)
            if (nextBrickPanes2 != null) {
                nextBrickPanes2.clear();
                GridPane pane = new GridPane();
                pane.setVgap(1);
                pane.setHgap(1);
                pane.setPrefSize(nextBrickSize, nextBrickSize);
                for (int r = 0; r < 4; r++) {
                    for (int c = 0; c < 4; c++) {
                        Rectangle rect = new Rectangle(holdBrickSize, holdBrickSize);
                        rect.setFill(Color.TRANSPARENT);
                        pane.add(rect, c, r);
                    }
                }
                nextBrickPanes2.add(pane);
                nextBricksPanel2.getChildren().add(pane);
            }
        }
        
        // Reset score labels
        if (scoreLabel1 != null) {
            if (scoreLabel1.textProperty().isBound()) {
                scoreLabel1.textProperty().unbind();
            }
            scoreLabel1.setText("0");
        }
        if (scoreLabel2 != null) {
            if (scoreLabel2.textProperty().isBound()) {
                scoreLabel2.textProperty().unbind();
            }
            scoreLabel2.setText("0");
        }
        
        // Reset level labels
        if (levelLabel1 != null) {
            if (levelLabel1.textProperty().isBound()) {
                levelLabel1.textProperty().unbind();
            }
            levelLabel1.setText("1");
        }
        if (levelLabel2 != null) {
            if (levelLabel2.textProperty().isBound()) {
                levelLabel2.textProperty().unbind();
            }
            levelLabel2.setText("1");
        }
        
        // Clear stored brick data
        currentBrickData1 = null;
        currentBrickData2 = null;
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
                    refreshBrick(downData.getViewData());
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
        if (playerNumber == 1) {
            if (scoreLabel1 != null && score != null) {
                scoreLabel1.textProperty().unbind();
                scoreLabel1.textProperty().bind(score.asString("%d"));
            }
        } else if (playerNumber == 2) {
            if (scoreLabel2 != null && score != null) {
                scoreLabel2.textProperty().unbind();
                scoreLabel2.textProperty().bind(score.asString("%d"));
            }
        } else {
            if (scoreLabel != null && score != null) {
                scoreLabel.textProperty().unbind();
                scoreLabel.textProperty().bind(score.asString("Score: %d"));
            }
        }
    }

    public void bindLevel(IntegerProperty level) {
        bindLevel(level, 0);
    }
    
    public void bindLevel(IntegerProperty level, int playerNumber) {
        if (playerNumber == 1) {
            // Player 1 multiplayer
            if (levelLabel1 != null && level != null) {
                levelLabel1.textProperty().unbind();
                levelLabel1.textProperty().bind(level.asString("%d"));
            }
        } else if (playerNumber == 2) {
            // Player 2 multiplayer
            if (levelLabel2 != null && level != null) {
                levelLabel2.textProperty().unbind();
                levelLabel2.textProperty().bind(level.asString("%d"));
            }
        } else {
            // Single player (playerNumber == 0)
            if (levelLabel != null && level != null) {
                levelLabel.textProperty().unbind();
                levelLabel.textProperty().bind(level.asString("Level: %d"));
                
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
        if (playerNumber == 0 && linesLabel != null && lines != null) {
            linesLabel.textProperty().unbind();
            linesLabel.textProperty().bind(lines.asString("Lines: %d"));
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
            if (multiplayerPauseOverlay != null) {
                multiplayerPauseOverlay.setVisible(false);
                multiplayerPauseOverlay.setManaged(false);
                multiplayerPauseOverlay.setMouseTransparent(true);
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
                if (multiplayerWinningOverlay != null) {
                    multiplayerWinningOverlay.setVisible(false);
                    multiplayerWinningOverlay.setManaged(false);
                    multiplayerWinningOverlay.setMouseTransparent(true);
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
        if (multiplayerPauseOverlay != null) {
            multiplayerPauseOverlay.setVisible(false);
            multiplayerPauseOverlay.setManaged(false);
            multiplayerPauseOverlay.setMouseTransparent(true);
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
                if (displayMatrix != null) {
                    for (int i = 0; i < displayMatrix.length; i++) {
                        for (int j = 0; j < displayMatrix[i].length; j++) {
                            if (displayMatrix[i][j] != null) {
                                displayMatrix[i][j].setFill(getFillColor(0));
                            }
                        }
                    }
                }
                // Clear next bricks display
                if (nextBrickPanes != null) {
                    for (GridPane pane : nextBrickPanes) {
                        if (pane != null) {
                            pane.getChildren().clear();
                            // Re-initialize empty cells
                            for (int r = 0; r < 4; r++) {
                                for (int c = 0; c < 4; c++) {
                                    Rectangle rect = new Rectangle(BRICK_SIZE - 10, BRICK_SIZE - 10);
                                    rect.setFill(Color.TRANSPARENT);
                                    pane.add(rect, c, r);
                                }
                            }
                        }
                    }
                }
                // Clear hold brick display
                if (holdBrickRectangles != null) {
                    for (int i = 0; i < holdBrickRectangles.length; i++) {
                        for (int j = 0; j < holdBrickRectangles[i].length; j++) {
                            if (holdBrickRectangles[i][j] != null) {
                                holdBrickRectangles[i][j].setFill(Color.TRANSPARENT);
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
            if (multiplayerSettingsOverlay != null && multiplayerSettingsOverlay.getChildren().contains(settingsPanel)) {
                multiplayerSettingsOverlay.getChildren().remove(settingsPanel);
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
            if (multiplayerSettingsOverlay != null && multiplayerSettingsOverlay.getChildren().contains(settingsPanel)) {
                multiplayerSettingsOverlay.getChildren().remove(settingsPanel);
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
        if (!isMultiplayerMode && nextBricksPanel != null) {
            nextBricksPanel.setVisible(true);
            nextBricksPanel.setManaged(true);
            initializeNextBricksPanel();
            
            // Update next bricks with initial bricks from the board
            if (eventListener instanceof GameController) {
                GameController gameController = (GameController) eventListener;
                if (gameController.getBoard() instanceof SimpleBoard) {
                    SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                    updateNextBricks(simpleBoard.getNextBricks(), 0);
                }
            }
        }
        
        // Refresh the brick display with stored brick data
        // The brick data was stored when initGameView was called
        if (currentBrickData != null) {
            refreshBrick(currentBrickData);
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
                if (multiplayerPauseOverlay != null && multiplayerPausePanel != null) {
                    multiplayerPauseOverlay.setVisible(true);
                    multiplayerPauseOverlay.setManaged(true);
                    multiplayerPauseOverlay.setMouseTransparent(false);
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
                if (multiplayerPauseOverlay != null) {
                    multiplayerPauseOverlay.setVisible(false);
                    multiplayerPauseOverlay.setManaged(false);
                    multiplayerPauseOverlay.setMouseTransparent(true);
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
    
    private void initializeMultiplayerPausePanel() {
        if (multiplayerPausePanel == null) return;
        setupPausePanelActions(multiplayerPausePanel);
    }
    
    private void setupWinningPanelActions(WinningPanel panel) {
        // Restart action - start a new multiplayer game
        panel.setOnRestartAction(() -> {
            // Clear all multiplayer game panels and side panels before restarting
            clearMultiplayerGamePanels();
            
            // Re-initialize multiplayer panels to ensure consistent container and side panel sizing
            // This fixes issues where sizes differ after restart
            initializeMultiplayerPanels();
            
            // Ensure container and wrapper are properly sized and visible
            if (multiplayerContainer != null) {
                multiplayerContainer.setVisible(true);
                multiplayerContainer.setManaged(true);
            }
            if (multiplayerWrapper != null) {
                multiplayerWrapper.setVisible(true);
                multiplayerWrapper.setManaged(true);
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
        });
        
        // Main menu action - go back to main menu
        // Restore everything to the exact initial state when game launches
        panel.setOnMainMenuAction(() -> {
            // Clear all multiplayer game panels and side panels before quitting
            clearMultiplayerGamePanels();
            
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
            
            // Hide winning panel
            hideWinningPanel();
            
            // Hide pause panels
            if (pausePanel != null) {
                pausePanel.setVisible(false);
            }
            if (multiplayerPauseOverlay != null) {
                multiplayerPauseOverlay.setVisible(false);
                multiplayerPauseOverlay.setManaged(false);
                multiplayerPauseOverlay.setMouseTransparent(true);
            }
            
            // Hide game over panel if visible
            if (gameOverPanel != null) {
                gameOverPanel.setVisible(false);
            }
            
            // Stop game music and play main menu music
            audioManager.stopAll();
            audioManager.playMainMenuMusic();
            
            // Reset multiplayer mode flag
            if (isMultiplayerMode) {
                isMultiplayerMode = false;
                
                // Clear multiplayer controllers and listeners
                gameController1 = null;
                gameController2 = null;
                eventListener1 = null;
                eventListener2 = null;
                timeLine1 = null;
                timeLine2 = null;
                garbageProcessTimeline1 = null;
                garbageProcessTimeline2 = null;
            }
            
            // Hide multiplayer container and overlays
            if (multiplayerContainer != null) {
                multiplayerContainer.setVisible(false);
                multiplayerContainer.setManaged(false);
            }
            if (multiplayerWrapper != null) {
                multiplayerWrapper.setVisible(false);
                multiplayerWrapper.setManaged(false);
            }
            if (multiplayerSettingsOverlay != null) {
                multiplayerSettingsOverlay.setVisible(false);
                multiplayerSettingsOverlay.setManaged(false);
                multiplayerSettingsOverlay.setMouseTransparent(true);
            }
            if (multiplayerReadyOverlay != null) {
                multiplayerReadyOverlay.setVisible(false);
                multiplayerReadyOverlay.setManaged(false);
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
            
            // Restore original left and right panels if they were hidden
            BorderPane rootPane = getRootBorderPane();
            if (rootPane != null) {
                // Restore left panel - ensure it's properly set back and functional
                if (originalLeftPanel != null) {
                    originalLeftPanel.setVisible(true);
                    originalLeftPanel.setManaged(true);
                    rootPane.setLeft(originalLeftPanel);
                    // Force layout update to ensure panel is properly displayed
                    originalLeftPanel.requestLayout();
                } else {
                    // If originalLeftPanel wasn't stored, try to restore from rootPane
                    javafx.scene.Node leftNode = rootPane.getLeft();
                    if (leftNode != null) {
                        leftNode.setVisible(true);
                        leftNode.setManaged(true);
                        // requestLayout() is only available on Parent, not Node
                        if (leftNode instanceof javafx.scene.Parent) {
                            ((javafx.scene.Parent) leftNode).requestLayout();
                        }
                    }
                }
                
                // Restore right panel - ensure it's properly set back and functional
                if (originalRightPanel != null) {
                    originalRightPanel.setVisible(true);
                    originalRightPanel.setManaged(true);
                    rootPane.setRight(originalRightPanel);
                    // Force layout update to ensure panel is properly displayed
                    originalRightPanel.requestLayout();
                } else {
                    // If originalRightPanel wasn't stored, try to restore from rootPane
                    javafx.scene.Node rightNode = rootPane.getRight();
                    if (rightNode != null) {
                        rightNode.setVisible(true);
                        rightNode.setManaged(true);
                        // requestLayout() is only available on Parent, not Node
                        if (rightNode instanceof javafx.scene.Parent) {
                            ((javafx.scene.Parent) rightNode).requestLayout();
                        }
                    }
                }
                
                // Force root pane to update layout
                rootPane.requestLayout();
            }
            
            // Show gameBoard (main menu is inside it) and restore keyboard handlers
            if (gameBoard != null) {
                gameBoard.setVisible(true);
                gameBoard.setManaged(true);
                gameBoard.setFocusTraversable(true);
                // Restore node-level keyboard handlers for single player mode
                gameBoard.setOnKeyPressed(e -> inputHandler.handleKeyPress(e, isMultiplayerMode, gameStarted));
                gameBoard.setOnKeyReleased(e -> inputHandler.handleKeyRelease(e, isMultiplayerMode));
            }
            
            // Restore game panel visibility (grid background) - same as initial state
            if (gamePanel != null) {
                gamePanel.setVisible(true);
            }
            
            // Hide brick panel and ghost panel when main menu is visible (same as initial state)
            if (brickPanel != null) {
                brickPanel.setVisible(false);
            }
            if (ghostPanel != null) {
                ghostPanel.setVisible(false);
            }
            
            // Show main menu panel (it's inside gameBoard)
            if (mainMenuPanel != null) {
                mainMenuPanel.setVisible(true);
                mainMenuPanel.setManaged(true);
            }
            
            // Re-initialize left and right panel contents to ensure they're functional
            // The panels are restored, but their contents (hold panel, next bricks, labels) need to be visible
            // and properly initialized - always re-initialize to ensure correct state
            
            // Ensure all children of left panel VBox are visible
            VBox leftPanelToProcess = null;
            if (originalLeftPanel != null && originalLeftPanel instanceof VBox) {
                leftPanelToProcess = (VBox) originalLeftPanel;
            } else if (rootPane != null && rootPane.getLeft() instanceof VBox) {
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
            } else if (rootPane != null && rootPane.getRight() instanceof VBox) {
                rightPanelToProcess = (VBox) rootPane.getRight();
            }
            if (rightPanelToProcess != null) {
                for (javafx.scene.Node child : rightPanelToProcess.getChildren()) {
                    child.setVisible(true);
                    child.setManaged(true);
                }
            }
            
            // Re-initialize hold panel (left panel)
            if (holdBrickPanel != null) {
                holdBrickPanel.setVisible(true);
                holdBrickPanel.setManaged(true);
                initializeHoldPanel();
            }
            
            // Ensure labels are visible and have default text (left panel)
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
            
            // Re-initialize next bricks panel (right panel)
            if (nextBricksPanel != null) {
                nextBricksPanel.setVisible(true);
                nextBricksPanel.setManaged(true);
                initializeNextBricksPanel();
            }
            
            // Clear multiplayer controllers
            gameController1 = null;
            gameController2 = null;
            eventListener1 = null;
            eventListener2 = null;
            timeLine1 = null;
            timeLine2 = null;
            garbageProcessTimeline1 = null;
            garbageProcessTimeline2 = null;
            
            // Stop multiplayer timer
            stopMultiplayerTimer();
            
            // Reset ready states
            player1Ready = false;
            player2Ready = false;
            
            isMultiplayerMode = false;
            
            // Request focus on gameBoard so it can receive keyboard events
            Platform.runLater(() -> {
                if (gameBoard != null) {
                    gameBoard.requestFocus();
                }
            });
        });
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
                initializeMultiplayerPanels();
                
                // Ensure container and wrapper are properly sized and visible
                if (multiplayerContainer != null) {
                    multiplayerContainer.setVisible(true);
                    multiplayerContainer.setManaged(true);
                }
                if (multiplayerWrapper != null) {
                    multiplayerWrapper.setVisible(true);
                    multiplayerWrapper.setManaged(true);
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
                if (multiplayerPauseOverlay != null && multiplayerPausePanel != null) {
                    multiplayerPauseOverlay.setVisible(false);
                    multiplayerPauseOverlay.setManaged(false);
                    multiplayerPauseOverlay.setMouseTransparent(true);
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
            if (multiplayerPauseOverlay != null) {
                multiplayerPauseOverlay.setVisible(false);
                multiplayerPauseOverlay.setManaged(false);
                multiplayerPauseOverlay.setMouseTransparent(true);
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
                if (displayMatrix != null) {
                    for (int i = 0; i < displayMatrix.length; i++) {
                        for (int j = 0; j < displayMatrix[i].length; j++) {
                            if (displayMatrix[i][j] != null) {
                                displayMatrix[i][j].setFill(getFillColor(0));
                            }
                        }
                    }
                }
                // Clear next bricks display
                if (nextBrickPanes != null) {
                    for (GridPane pane : nextBrickPanes) {
                        if (pane != null) {
                            pane.getChildren().clear();
                            // Re-initialize empty cells
                            for (int r = 0; r < 4; r++) {
                                for (int c = 0; c < 4; c++) {
                                    Rectangle rect = new Rectangle(BRICK_SIZE - 10, BRICK_SIZE - 10);
                                    rect.setFill(Color.TRANSPARENT);
                                    pane.add(rect, c, r);
                                }
                            }
                        }
                    }
                }
                // Clear hold brick display
                if (holdBrickRectangles != null) {
                    for (int i = 0; i < holdBrickRectangles.length; i++) {
                        for (int j = 0; j < holdBrickRectangles[i].length; j++) {
                            if (holdBrickRectangles[i][j] != null) {
                                holdBrickRectangles[i][j].setFill(Color.TRANSPARENT);
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
                if (multiplayerContainer != null) {
                    multiplayerContainer.setVisible(false);
                    multiplayerContainer.setManaged(false);
                }
                if (multiplayerWrapper != null) {
                    multiplayerWrapper.setVisible(false);
                    multiplayerWrapper.setManaged(false);
                }
                if (multiplayerPauseOverlay != null) {
                    multiplayerPauseOverlay.setVisible(false);
                    multiplayerPauseOverlay.setManaged(false);
                    multiplayerPauseOverlay.setMouseTransparent(true);
                }
                
                // Hide winning panel if visible
                hideWinningPanel();
                
                // Hide and clear settings overlay
                if (multiplayerSettingsOverlay != null) {
                    multiplayerSettingsOverlay.setVisible(false);
                    multiplayerSettingsOverlay.setManaged(false);
                    multiplayerSettingsOverlay.setMouseTransparent(true);
                    // Move settings panel back to gameStack for single player mode
                    if (settingsPanel != null) {
                        // Hide settings panel first
                        settingsPanel.setVisible(false);
                        // Remove from multiplayer overlay if it's there
                        if (multiplayerSettingsOverlay.getChildren().contains(settingsPanel)) {
                            multiplayerSettingsOverlay.getChildren().remove(settingsPanel);
                        }
                    }
                }
                
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
                if (nextBricksPanel != null) {
                    nextBricksPanel.setVisible(true);
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
        List<GridPane> panes;
        int brickSize;
        
        if (isMultiplayerMode && playerNumber > 0) {
            panes = (playerNumber == 1) ? nextBrickPanes1 : nextBrickPanes2;
            double scale = 0.85;
            brickSize = (int)((BRICK_SIZE - 10) * scale);
        } else {
            panes = nextBrickPanes;
            brickSize = BRICK_SIZE - 10;
        }
        
        if (panes == null || panes.isEmpty()) return;
        
        // For multiplayer, only show the first brick (next brick)
        int maxBricks = (isMultiplayerMode && playerNumber > 0) ? 1 : panes.size();
        
        for (int i = 0; i < Math.min(maxBricks, panes.size()); i++) {
            GridPane pane = panes.get(i);
            if (pane == null) continue;
            pane.getChildren().clear();
            if (i < nextBricks.size()) {
                int[][] shape = nextBricks.get(i).getShapeMatrix().get(0);
                for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                        if (shape[r][c] != 0) {
                            Rectangle rect = new Rectangle(brickSize, brickSize);
                            rect.setFill(getFillColor(shape[r][c]));
                            rect.setArcHeight(5);
                            rect.setArcWidth(5);
                            pane.add(rect, c, r);
                        }
                    }
                }
            }
        }
    }

    public void updateHoldBrick(Brick heldBrick) {
        updateHoldBrick(heldBrick, 0);
    }
    
    public void updateHoldBrick(Brick heldBrick, int playerNumber) {
        Rectangle[][] rectangles;
        
        if (isMultiplayerMode && playerNumber > 0) {
            rectangles = (playerNumber == 1) ? holdBrickRectangles1 : holdBrickRectangles2;
        } else {
            rectangles = holdBrickRectangles;
        }
        
        if (rectangles == null) return;
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (rectangles[i][j] != null) rectangles[i][j].setFill(Color.TRANSPARENT);
        if (heldBrick != null) {
            int[][] shape = heldBrick.getShapeMatrix().get(0);
            for (int i = 0; i < shape.length; i++)
                for (int j = 0; j < shape[i].length; j++)
                    if (shape[i][j] != 0 && rectangles[i][j] != null)
                        rectangles[i][j].setFill(getFillColor(shape[i][j]));
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
