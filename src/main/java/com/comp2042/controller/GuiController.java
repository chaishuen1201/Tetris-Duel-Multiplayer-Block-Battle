package com.comp2042.controller;

import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.model.DownData;
import com.comp2042.model.ViewData;
import com.comp2042.view.GameOverPanel;
import com.comp2042.view.PausePanel;
import com.comp2042.view.MainMenuPanel;
import com.comp2042.view.NotificationPanel;
import com.comp2042.view.SettingsPanel;
import com.comp2042.view.MultiplayerScreen;
import com.comp2042.view.SinglePlayerScreen;
import com.comp2042.view.GameViewRenderer;
import com.comp2042.model.HighScoreManager;
import com.comp2042.model.SimpleBoard;
import com.comp2042.util.KeyBindingsManager;
import com.comp2042.controller.manager.AudioManager;
import com.comp2042.controller.manager.TimerManager;
import com.comp2042.controller.manager.GameStateManager;
import com.comp2042.controller.manager.GameLoopManager;
import com.comp2042.controller.manager.PanelCoordinator;
import com.comp2042.controller.manager.GarbageManager;
import com.comp2042.controller.manager.MultiplayerViewManager;
import com.comp2042.controller.manager.SinglePlayerViewManager;
import com.comp2042.controller.manager.CountdownManager;
import com.comp2042.controller.input.InputHandler;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.control.Label;
import javafx.scene.Parent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Main GUI controller that coordinates all UI components and game logic.
 * This class serves as the central coordinator for the entire game application,
 * managing both single player and multiplayer game modes. It handles user input
 * through keyboard events, coordinates panel visibility through PanelCoordinator,
 * manages audio playback through AudioManager, controls game timers through
 * TimerManager, and maintains game state through GameStateManager. The controller
 * initializes all game components, sets up event listeners and callbacks, delegates
 * specialized functionality to manager classes (GameLoopManager, GarbageManager,
 * MultiplayerViewManager, SinglePlayerViewManager, CountdownManager), and coordinates
 * view updates through GameViewRenderer. It implements JavaFX Initializable to set
 * up the UI when the FXML is loaded, and provides methods for game control (start,
 * pause, new game, quit), view updates (brick refresh, background refresh, next bricks,
 * hold brick), and property binding (score, level, lines).
 */
public class GuiController implements Initializable {

    /**
     * Default constructor. Initializes the controller.
     * The actual initialization is performed by the initialize() method
     * which is called by JavaFX when the FXML is loaded.
     */
    public GuiController() {
        // Default constructor - initialization handled by initialize() method
    }

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
    @FXML private VBox centerVBox; // The main center container for both single player and multiplayer
    @FXML private HBox singlePlayerWrapper; // The wrapper containing single player panels
    
    // Multiplayer game controllers
    private GameController gameController1, gameController2;
    private InputEventListener eventListener1, eventListener2;
    
    // Store scene filter handlers to avoid duplicates
    private javafx.event.EventHandler<KeyEvent> sceneKeyPressedHandler;
    private javafx.event.EventHandler<KeyEvent> sceneKeyReleasedHandler;
    
    // Store original panels to restore later
    private VBox originalLeftPanel, originalRightPanel;

    private InputEventListener eventListener;
    // Timer management - delegated to TimerManager
    private final TimerManager timerManager = new TimerManager();
    // Game state management - delegated to GameStateManager
    private final GameStateManager gameStateManager;
    // Game loop management - delegated to GameLoopManager
    private final GameLoopManager gameLoopManager;
    private int currentLevel = 1;
    private javafx.beans.value.ChangeListener<? super Number> levelChangeListener;

    private HighScoreManager highScoreManager = new HighScoreManager();
    
    // Audio management - delegated to AudioManager
    private final AudioManager audioManager = new AudioManager();
    
    // Input handling - delegated to InputHandler (dependency injection)
    private final InputHandler inputHandler = new InputHandler(KeyBindingsManager.getInstance());
    
    // Panel visibility management - delegated to PanelCoordinator
    private final PanelCoordinator panelCoordinator = new PanelCoordinator();
    
    // Rendering - delegated to GameViewRenderer
    private final GameViewRenderer gameViewRenderer = new GameViewRenderer();
    
    // Settings management - delegated to SettingsController
    private final SettingsController settingsController = new SettingsController();
    
    // Garbage management - delegated to GarbageManager
    private final GarbageManager garbageManager;
    
    // Multiplayer view management - delegated to MultiplayerViewManager
    private final MultiplayerViewManager multiplayerViewManager;
    
    // Single player view management - delegated to SinglePlayerViewManager
    private SinglePlayerViewManager singlePlayerViewManager;
    
    // Countdown management - delegated to CountdownManager
    private CountdownManager countdownManager;
    
    // Initialize GameStateManager with dependencies
    {
        gameStateManager = new GameStateManager(audioManager, timerManager);
        gameLoopManager = new GameLoopManager(gameStateManager);
        garbageManager = new GarbageManager(gameStateManager);
        garbageManager.setAudioManager(audioManager);
        multiplayerViewManager = new MultiplayerViewManager(
            gameStateManager,
            gameLoopManager,
            panelCoordinator,
            timerManager,
            audioManager,
            inputHandler,
            garbageManager
        );
    }

    /**
     * Initializes the GUI controller and all game components.
     * Sets up fonts, initializes screens, managers, panels, input handlers, and audio.
     * Configures callbacks and event listeners for game state management.
     * 
     * @param _location The location used to resolve relative paths for the root object (not used)
     * @param _resources The resources used to localize the root object (not used)
     */
    @Override
    public void initialize(URL _location, ResourceBundle _resources) {
        // Parameters are required by Initializable interface but not used
        try {
            URL fontUrl = getClass().getClassLoader().getResource("font/digital.ttf");
            if (fontUrl != null) {
                Font.loadFont(fontUrl.toExternalForm(), 38);
            } else {
                System.out.println("Font not found: font/digital.ttf");
            }
        } catch (Exception e) {
            System.out.println("Font not found, using default font: " + e.getMessage());
        }
        
        try {
            URL fontUrl = getClass().getClassLoader().getResource("font/PublicPixel-rv0pA.ttf");
            if (fontUrl != null) {
                Font.loadFont(fontUrl.toExternalForm(), 48);
            } else {
                System.out.println("Font not found: font/PublicPixel-rv0pA.ttf");
            }
        } catch (Exception e) {
            System.out.println("PublicPixel font not found, using default font: " + e.getMessage());
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
        
        // Initialize garbage manager with screen references
        garbageManager.setSinglePlayerScreen(singlePlayerScreen);
        garbageManager.setGameOverCallback(this::gameOver);
        
        // Initialize single player UI panels
        singlePlayerScreen.initializeGamePanel();
        singlePlayerScreen.initializeHoldPanel();
        singlePlayerScreen.initializeNextBricksPanel();
        
        // Initialize single player view manager
        singlePlayerViewManager = new SinglePlayerViewManager(gameStateManager, panelCoordinator);
        singlePlayerViewManager.setSinglePlayerScreen(singlePlayerScreen);
        
        // Set single player view manager in garbage manager
        garbageManager.setSinglePlayerViewManager(singlePlayerViewManager);
        
        // Initialize default values for score/level/lines using GameStateManager
        gameStateManager.initializeInfoLabels(scoreLabel, levelLabel, linesLabel);
        
        // Initialize audio manager
        audioManager.initialize();
        
        // Initialize timer manager
        timerManager.setSinglePlayerTimerLabel(timerLabel);
        
        // Initialize panel coordinator with all panel references
        panelCoordinator.setGameBoard(gameBoard);
        panelCoordinator.setGamePanel(gamePanel);
        panelCoordinator.setBrickPanel(brickPanel);
        panelCoordinator.setGhostPanel(ghostPanel);
        panelCoordinator.setGameOverPanel(gameOverPanel);
        panelCoordinator.setPausePanel(pausePanel);
        panelCoordinator.setSettingsPanel(settingsPanel);
        panelCoordinator.setNextBricksPanel(nextBricksPanel);
        panelCoordinator.setHoldBrickPanel(holdBrickPanel);
        panelCoordinator.setScoreLabel(scoreLabel);
        panelCoordinator.setLevelLabel(levelLabel);
        panelCoordinator.setLinesLabel(linesLabel);
        panelCoordinator.setMainMenuPanel(mainMenuPanel);
        panelCoordinator.setCountdownLabel(countdownLabel);
        
        // Set reference to the centered layout container wrapper (new layout structure)
        // The gameBoard is inside an HBox, which is inside a VBox (game-area-container)
        if (gameBoard != null && gameBoard.getParent() != null) {
            javafx.scene.Parent parent = gameBoard.getParent();
            if (parent instanceof HBox) {
                HBox hboxWrapper = (HBox) parent;
                panelCoordinator.setGameContainerWrapper(hboxWrapper);
                // Get the VBox or StackPane that contains the HBox
                if (hboxWrapper.getParent() != null) {
                    panelCoordinator.setGameContainerParent(hboxWrapper.getParent());
                }
            }
        }
        
        // Initialize game state manager
        initializeGameStateManager();
        
        // Initialize game loop manager
        initializeGameLoopManager();
        
        // Initialize multiplayer screen
        multiplayerScreen = new MultiplayerScreen();
        // Update settings controller with multiplayer screen and view manager references
        settingsController.setMultiplayerScreen(multiplayerScreen);
        settingsController.setMultiplayerViewManager(multiplayerViewManager);
        // Update garbage manager with multiplayer screen reference
        garbageManager.setMultiplayerScreen(multiplayerScreen);
        
        // Set MultiplayerScreen reference for winning panel management in GameStateManager
        gameStateManager.setMultiplayerScreen(multiplayerScreen);
        
        // Initialize multiplayer view manager with UI components
        multiplayerViewManager.setMultiplayerScreen(multiplayerScreen);
        multiplayerViewManager.setSinglePlayerScreen(singlePlayerScreen);
        multiplayerViewManager.setGameBoard(gameBoard);
        multiplayerViewManager.setCenterVBox(centerVBox); // Set the center container for multiplayer
        multiplayerViewManager.setHoldBrickPanel(holdBrickPanel);
        multiplayerViewManager.setNextBricksPanel(nextBricksPanel);
        multiplayerViewManager.setScoreLabel(scoreLabel);
        multiplayerViewManager.setLevelLabel(levelLabel);
        multiplayerViewManager.setLinesLabel(linesLabel);
        multiplayerViewManager.setSettingsPanel(settingsPanel);
        multiplayerViewManager.setRootBorderPaneSupplier(this::getRootBorderPane);
        multiplayerViewManager.setGameControllerFactory(playerNumber -> new GameController(this, playerNumber));
        multiplayerViewManager.setGameControllerSyncCallback((gc1, gc2) -> {
            this.gameController1 = gc1;
            this.gameController2 = gc2;
        });
        multiplayerViewManager.setOriginalPanelSyncCallback((left, right) -> {
            this.originalLeftPanel = left;
            this.originalRightPanel = right;
        });
        
        setupMultiplayerScreenCallbacks();

        // Initialize panel states
        panelCoordinator.initializePanelStates();
        
        if (pausePanel != null) {
            // Constrain pause panel to preferred size (same as multiplayer)
            pausePanel.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
            initializePausePanel();
        }
        
        // Initialize countdown manager
        countdownManager = new CountdownManager(audioManager, panelCoordinator, gameLoopManager);
        countdownManager.setCountdownLabel(countdownLabel);
        countdownManager.setGameStack(gameStack);
        countdownManager.setGhostPanel(ghostPanel);
        countdownManager.setSinglePlayerScreen(singlePlayerScreen);
        countdownManager.setSettingsController(settingsController);
        countdownManager.setOnCountdownComplete(this::actuallyStartGame);
        
        // Initialize countdown label
        if (countdownLabel != null) {
            countdownLabel.setAlignment(javafx.geometry.Pos.CENTER);
        }
        
        // Timer label initialization is handled by TimerManager
        
        // Keep next bricks panel container visible, but hide the brick previews until game starts
        // This must be done after initializeNextBricksPanel() to ensure panes exist
        if (nextBricksPanel != null) {
            // Hide the individual brick panes - ensure they're hidden at main menu
            List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
            if (nextBrickPanes != null && !nextBrickPanes.isEmpty()) {
                panelCoordinator.hideNextBrickPanesManaged(nextBrickPanes);
            }
        }

        // Set up main menu panel
        if (mainMenuPanel != null) {
            audioManager.setupButtonWithSound(mainMenuPanel.getPlayButton(), () -> startGame());
            audioManager.setupButtonWithSound(mainMenuPanel.getMultiButton(), () -> multiplayerViewManager.showMultiplayer());
            audioManager.setupButtonWithSound(mainMenuPanel.getSettingsButton(), () -> settingsController.showSettings());
            audioManager.setupButtonWithSound(mainMenuPanel.getQuitButton(), () -> quitGame());
        }
        
        // Initialize settings controller
        settingsController.initialize(
            settingsPanel,
            audioManager,
            panelCoordinator,
            gameStateManager,
            multiplayerScreen,
            gameStack,
            mainMenuPanel,
            ghostPanel
        );
        
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
    
    /**
     * Initializes the game state manager with callbacks for UI updates and game state changes.
     * Sets up handlers for pause panel, game over panel, main menu, and game loop operations.
     */
    private void initializeGameStateManager() {
        // Set up callbacks for UI updates
        gameStateManager.setOnShowPausePanel(() -> {
            panelCoordinator.showPausePanel();
        });
        
        gameStateManager.setOnHidePausePanel(() -> {
            panelCoordinator.hidePausePanel();
        });
        
        gameStateManager.setOnShowMultiplayerPausePanel(() -> {
            if (multiplayerScreen != null) multiplayerScreen.showPausePanel();
        });
        
        gameStateManager.setOnHideMultiplayerPausePanel(() -> {
            if (multiplayerScreen != null) multiplayerScreen.hidePausePanel();
        });
        
        gameStateManager.setOnStartGarbageProcessingTimelines(() -> gameLoopManager.startGarbageProcessingTimelines());
        gameStateManager.setOnStopGarbageProcessingTimelines(() -> gameLoopManager.stopGarbageProcessingTimelines());
        
        gameStateManager.setOnUpdateTimelineRate(() -> gameLoopManager.updateTimelineRate());
        gameStateManager.setOnUpdateTimelineRate1(() -> gameLoopManager.updateTimelineRate(1));
        gameStateManager.setOnUpdateTimelineRate2(() -> gameLoopManager.updateTimelineRate(2));
        
        // Note: MultiplayerScreen reference will be set after it's created (see below)
        gameStateManager.setOnRestartGame(() -> multiplayerViewManager.restartMultiplayerGame());
        gameStateManager.setOnQuitToMenu(() -> multiplayerViewManager.quitToMainMenuFromMultiplayer());
        
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
                    // Clear the game board display - delegate to screen class which uses renderer
                    if (singlePlayerScreen != null) {
                        singlePlayerScreen.clearAllPanels();
                    }
                    // Clear current brick display and ghost panel
                    if (brickPanel != null) {
                        brickPanel.getChildren().clear();
                    }
                    if (ghostPanel != null) {
                        ghostPanel.getChildren().clear();
                    }
                    
                    // Hide game over panel and show main menu
                    panelCoordinator.hideGameOverPanel();
                    panelCoordinator.hideBrickPanel();
                    panelCoordinator.hideGhostPanel();
                    panelCoordinator.showMainMenuPanel();
                    gameStateManager.resetGameStates();
                    if (gameLoopManager.getTimeLine() != null) gameLoopManager.getTimeLine().stop();
                    
                    // Reset timer
                    timerManager.resetSinglePlayerTimer();
                    
                    // Stop game over sound and play main menu music
                    audioManager.playMainMenuMusic();
                });
                
                // Set up button sounds
                gameOverPanel.setupButtonSounds(audioManager);
                
                panelCoordinator.showGameOverPanel();
            }
        });
        
        gameStateManager.setOnHideGameOverPanel(() -> {
            panelCoordinator.hideGameOverPanel();
        });
        
        gameStateManager.setOnShowMainMenu(() -> {
            panelCoordinator.showMainMenuPanel();
        });
        
        gameStateManager.setOnHideMainMenu(() -> {
            panelCoordinator.hideMainMenuPanel();
        });
        
        gameStateManager.setOnRequestFocus(() -> {
            if (gameBoard != null) gameBoard.requestFocus();
        });
    }
    
    /**
     * Initializes the game loop manager with callbacks for move down operations,
     * garbage processing, and UI refresh operations.
     */
    private void initializeGameLoopManager() {
        // Set up callbacks for move down operations
        gameLoopManager.setMoveDownCallbacks(new GameLoopManager.MoveDownCallbacks() {
            @Override
            public void onMultiplayerMoveDown(DownData downData, int playerNumber) {
                refreshBrickForPlayer(downData.getViewData(), playerNumber);
            }
            
            @Override
            public void onSinglePlayerMoveDown(DownData downData) {
                if (singlePlayerViewManager != null) {
                    singlePlayerViewManager.refreshBrick(downData.getViewData());
                }
            }
            
            @Override
            public void onRequestFocus() {
                if (gameBoard != null) gameBoard.requestFocus();
            }
            
            @Override
            public void onShowNotification(String message) {
                showNotification(message);
            }
            
            @Override
            public void onPlayLineClear() {
                audioManager.playLineClear();
            }
        });
        
        // Set up callbacks for garbage processing
        gameLoopManager.setGarbageProcessingCallbacks(playerNumber -> {
            garbageManager.processGarbageQueue(playerNumber);
        });
        
        // Countdown callbacks are now handled by CountdownManager
    }
    
    /**
     * Initializes the input handler with callbacks for single player and multiplayer actions.
     * Sets up keyboard event handling for both game modes.
     */
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
                return gameLoopManager.getTimeLine();
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
            public boolean isCountdownActive() {
                // Check if countdown label is visible to determine if countdown is active
                return countdownLabel != null && countdownLabel.isVisible();
            }
            
            @Override
            public void refreshBrick(ViewData viewData) {
                if (singlePlayerViewManager != null) {
                    singlePlayerViewManager.refreshBrick(viewData);
                }
            }
            
            @Override
            public void moveDown(MoveEvent event) {
                gameLoopManager.moveDown(event);
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
                gameLoopManager.updateTimelineRate();
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
                if (multiplayerViewManager != null) {
                    multiplayerViewManager.setPlayer1Ready(ready);
                }
            }
            
            @Override
            public void setPlayer2Ready(boolean ready) {
                if (multiplayerViewManager != null) {
                    multiplayerViewManager.setPlayer2Ready(ready);
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
                return gameLoopManager.getTimeLine1();
            }
            
            @Override
            public Timeline getTimeline2() {
                return gameLoopManager.getTimeLine2();
            }
            
            @Override
            public void refreshBrick(ViewData viewData, int playerNumber) {
                refreshBrickForPlayer(viewData, playerNumber);
            }
            
            @Override
            public void moveDown(MoveEvent event, int playerNumber) {
                gameLoopManager.moveDown(event, playerNumber);
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
                gameLoopManager.updateTimelineRate(playerNumber);
            }
            
            @Override
            public void updateReadyLabels() {
                if (multiplayerViewManager != null) {
                    multiplayerViewManager.updateReadyLabels();
                }
            }
            
            @Override
            public void checkBothReady() {
                if (multiplayerViewManager != null) {
                    multiplayerViewManager.checkBothReady();
                }
            }
        });
    }
    
    /**
     * Sets up callbacks for the multiplayer screen including pause panel actions,
     * game start/restart/quit handlers, and ready state management.
     */
    private void setupMultiplayerScreenCallbacks() {
        if (multiplayerScreen == null) return;
        
        // Set up pause panel action handler for multiplayer
        PausePanelActionHandler pausePanelHandler = new PausePanelActionHandler(this);
        multiplayerScreen.setPausePanelActionHandler(pausePanelHandler);
        
        multiplayerScreen.setCallbacks(
            (r) -> multiplayerViewManager.startMultiplayerGame(), // onStartGame
            (r) -> multiplayerViewManager.restartMultiplayerGame(), // onRestartGame
            (r) -> multiplayerViewManager.quitToMainMenuFromMultiplayer(), // onQuitToMenu
            (r) -> resumeMultiplayerGame(), // onResumeGame
            (r) -> settingsController.showSettingsFromPause(), // onShowSettings
            () -> multiplayerViewManager.updateReadyLabels(), // onUpdateReadyLabels
            () -> multiplayerViewManager.checkBothReady(), // onCheckBothReady
            (ready) -> {}, // onSetPlayer1Ready (handled internally)
            (ready) -> {}, // onSetPlayer2Ready (handled internally)
            (parent) -> {}, // onGetRootBorderPane (not needed)
            (panel) -> {} // onSetSettingsPanel (not needed)
        );
        
        // Set callback for starting game when both players are ready
        multiplayerViewManager.setOnStartGameCallback(() -> multiplayerViewManager.startMultiplayerGame());
    }
    
    
    
    /**
     * Resumes the multiplayer game by toggling the pause state.
     * Delegates to pauseGame to ensure consistent pause/resume logic.
     */
    private void resumeMultiplayerGame() {
        // Resume the game by calling pauseGame which toggles the pause state
        // This ensures all pause/resume logic is handled consistently
        pauseGame(null);
    }
    
    /**
     * Gets the root BorderPane from the scene hierarchy.
     * Traverses up from the gameBoard to find the root BorderPane.
     * 
     * @return The root BorderPane, or null if not found
     */
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

    /**
     * Initializes the game view for single player mode.
     * 
     * @param boardMatrix The initial board state matrix
     * @param brick The initial brick view data
     */
    public void initGameView(int[][] boardMatrix, ViewData brick) {
        initGameView(boardMatrix, brick, 0);
    }
    
    /**
     * Initializes the game view for the specified player.
     * Sets up the initial display, creates game timelines, and updates next bricks display.
     * 
     * @param boardMatrix The initial board state matrix
     * @param brick The initial brick view data
     * @param playerNumber The player number (0 for single player, 1 or 2 for multiplayer)
     */
    public void initGameView(int[][] boardMatrix, ViewData brick, int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0) {
            // Initialize multiplayer game view
            if (multiplayerScreen != null) {
                gameViewRenderer.refreshBrick(multiplayerScreen, brick, playerNumber);
                gameViewRenderer.refreshGameBackground(multiplayerScreen, boardMatrix, playerNumber);
                
                // Update next bricks display for multiplayer
                GameController controller = (playerNumber == 1) ? gameController1 : gameController2;
                if (controller != null && controller.getBoard() instanceof SimpleBoard) {
                    SimpleBoard simpleBoard = (SimpleBoard) controller.getBoard();
                    gameViewRenderer.updateNextBricks(multiplayerScreen, simpleBoard.getNextBricks(), playerNumber);
                }
            }
            
            if (playerNumber == 1 || playerNumber == 2) {
                gameLoopManager.createMultiplayerTimeline(playerNumber);
            }
        } else {
            // Single player game view
            if (singlePlayerViewManager != null) {
                singlePlayerViewManager.refreshBrick(brick);
                singlePlayerViewManager.refreshGameBackground(boardMatrix);
                
                // Store brick data for countdown refresh
                singlePlayerViewManager.setCurrentBrickData(brick);
                
                // Update next bricks display for single player
                // Visibility logic is handled inside updateNextBricks
                if (eventListener instanceof GameController) {
                    GameController gameController = (GameController) eventListener;
                    if (gameController.getBoard() instanceof SimpleBoard) {
                        SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                        singlePlayerViewManager.updateNextBricks(simpleBoard.getNextBricks());
                    }
                }
            }

            gameLoopManager.createSinglePlayerTimeline();

            if (gameBoard != null) gameBoard.requestFocus();
        }
    }


    /**
     * Clears all multiplayer game panels (game panels, brick panels, ghost panels)
     * and side panels (hold, next, score, level) by setting all rectangles to transparent/empty state
     * and resetting labels to default values.
     */
    void clearMultiplayerGamePanels() {
        if (multiplayerViewManager != null) {
            multiplayerViewManager.clearMultiplayerGamePanels();
        }
    }
    
    /**
     * Stops garbage processing timelines (delegates to GameLoopManager).
     */
    void stopGarbageProcessingTimelines() {
        gameLoopManager.stopGarbageProcessingTimelines();
    }

    /**
     * Helper method to refresh brick display for a player.
     * Handles both single player and multiplayer modes.
     * 
     * @param viewData The view data containing brick information
     * @param playerNumber The player number (0 for single player, 1 or 2 for multiplayer)
     */
    private void refreshBrickForPlayer(ViewData viewData, int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0 && multiplayerScreen != null) {
            if (!gameStateManager.isPaused()) {
                gameViewRenderer.refreshBrick(multiplayerScreen, viewData, playerNumber);
            }
        } else if (singlePlayerViewManager != null) {
            singlePlayerViewManager.refreshBrick(viewData);
        }
    }

    private void showNotification(String message) {
        if (groupNotification != null) {
            NotificationPanel panel = new NotificationPanel(message);
            groupNotification.getChildren().add(panel);
            panel.showScore(groupNotification.getChildren());
        }
    }

    /**
     * Sets the event listener for single player mode.
     * 
     * @param listener The InputEventListener to handle game events
     */
    public void setEventListener(InputEventListener listener) { 
        this.eventListener = listener;
        gameLoopManager.setEventListener(listener);
        if (singlePlayerScreen != null) {
            singlePlayerScreen.setEventListener(listener);
        }
    }
    
    /**
     * Sets the event listener for the specified player.
     * 
     * @param listener The InputEventListener to handle game events
     * @param playerNumber The player number (0 for single player, 1 or 2 for multiplayer)
     */
    public void setEventListener(InputEventListener listener, int playerNumber) {
        if (playerNumber == 1) {
            this.eventListener1 = listener;
            gameLoopManager.setEventListener1(listener);
            multiplayerViewManager.setEventListener1(listener);
        } else if (playerNumber == 2) {
            this.eventListener2 = listener;
            gameLoopManager.setEventListener2(listener);
            multiplayerViewManager.setEventListener2(listener);
        } else {
            this.eventListener = listener;
            gameLoopManager.setEventListener(listener);
            // For single player, also set the eventListener in SinglePlayerScreen
            // This is needed for ghost piece rendering
            if (singlePlayerScreen != null) {
                singlePlayerScreen.setEventListener(listener);
            }
        }
    }

    /**
     * Binds the score property to the GUI for single player mode.
     * 
     * @param score The IntegerProperty representing the score
     */
    public void bindScore(IntegerProperty score) {
        bindScore(score, 0);
    }
    
    /**
     * Binds the score property to the GUI for the specified player.
     * 
     * @param score The IntegerProperty representing the score
     * @param playerNumber The player number (0 for single player, 1 or 2 for multiplayer)
     */
    public void bindScore(IntegerProperty score, int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0) {
            multiplayerViewManager.bindScore(score, playerNumber);
        } else if (playerNumber == 0) {
            // Single player mode - use SinglePlayerScreen
            if (singlePlayerScreen != null) {
                singlePlayerScreen.bindScore(score);
            }
        }
    }

    /**
     * Binds the level property to the GUI for single player mode.
     * 
     * @param level The IntegerProperty representing the level
     */
    public void bindLevel(IntegerProperty level) {
        bindLevel(level, 0);
    }
    
    /**
     * Binds the level property to the GUI for the specified player.
     * For multiplayer mode, delegates to MultiplayerViewManager. For single player,
     * binds to the level label and sets up a level change listener for level-up sounds
     * and timeline rate updates.
     * 
     * @param level The IntegerProperty representing the level
     * @param playerNumber The player number (0 for single player, 1 or 2 for multiplayer)
     */
    public void bindLevel(IntegerProperty level, int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0) {
            multiplayerViewManager.bindLevel(level, playerNumber);
        } else if (playerNumber == 0) {
            // Single player mode - use SinglePlayerScreen
            if (singlePlayerScreen != null && level != null) {
                singlePlayerScreen.bindLevel(level);
                
                // Create and store new listener
                levelChangeListener = (obs, oldVal, newVal) -> {
                    int oldLevel = oldVal.intValue();
                    int newLevel = newVal.intValue();
                    currentLevel = newLevel;
                    gameLoopManager.setCurrentLevel(currentLevel);
                    gameLoopManager.updateTimelineRate();
                    // Play level-up sound when level increases
                    if (newLevel > oldLevel) {
                        audioManager.playLevelUp();
                    }
                };
                level.addListener(levelChangeListener);
                currentLevel = level.get();
                gameLoopManager.setCurrentLevel(currentLevel);
                gameLoopManager.updateTimelineRate();
            }
        }
    }

    /**
     * Binds the lines property to the GUI for single player mode.
     * 
     * @param lines The IntegerProperty representing the number of lines cleared
     */
    public void bindLines(IntegerProperty lines) {
        bindLines(lines, 0);
    }
    
    /**
     * Binds the lines property to the GUI for the specified player.
     * Note: For multiplayer, lines binding is not used in side panels but kept for compatibility.
     * 
     * @param lines The IntegerProperty representing the number of lines cleared
     * @param playerNumber The player number (0 for single player, 1 or 2 for multiplayer)
     */
    public void bindLines(IntegerProperty lines, int playerNumber) {
        // For multiplayer, lines binding is not used in side panels, but we keep the method for compatibility
        if (playerNumber == 0 && singlePlayerScreen != null && lines != null) {
            singlePlayerScreen.bindLines(lines);
        }
    }
    
    /**
     * Sends garbage lines to the opponent in multiplayer mode.
     * Delegates to GarbageManager to handle the garbage sending logic.
     * 
     * @param fromPlayerNumber The player number (1 or 2) who is sending garbage
     * @param numGarbageLines The number of garbage lines to send
     */
    public void sendGarbageToOpponent(int fromPlayerNumber, int numGarbageLines) {
        garbageManager.sendGarbageToOpponent(fromPlayerNumber, numGarbageLines);
    }
    

    /**
     * Handles game over for single player mode.
     */
    public void gameOver() {
        gameOver(0);
    }
    
    /**
     * Handles game over for the specified player.
     * Updates timeline references and delegates to GameStateManager.
     * 
     * @param playerNumber The player number (0 for single player, 1 or 2 for multiplayer)
     */
    public void gameOver(int playerNumber) {
        // Update GameStateManager with current timeline references
        gameStateManager.setTimeLine(gameLoopManager.getTimeLine());
        gameStateManager.setTimeLine1(gameLoopManager.getTimeLine1());
        gameStateManager.setTimeLine2(gameLoopManager.getTimeLine2());
        
        // Delegate to GameStateManager
        gameStateManager.gameOver(playerNumber);
    }

    /**
     * Starts a new game session.
     * Resets the game state, creates a new game board, and sets up bindings.
     * 
     * @param actionEvent The action event that triggered this method (can be null)
     */
    /**
     * Starts a new game, resetting the current game state.
     * Updates GameStateManager with current timeline reference and delegates
     * to GameStateManager to handle the new game initialization.
     * 
     * @param actionEvent The action event that triggered this method (can be null)
     */
    public void newGame(ActionEvent actionEvent) {
        // Update GameStateManager with current timeline reference
        gameStateManager.setTimeLine(gameLoopManager.getTimeLine());
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
        settingsController.ensureSettingsPanelInGameStack();
        
        // Make game panel and brick panel visible for new game
        panelCoordinator.showGamePanel();
        panelCoordinator.showBrickPanel();
        if (ghostPanel != null) {
            // Check if ghost piece checkbox is selected
            boolean showGhost = settingsController.isGhostPieceEnabled();
            panelCoordinator.showGhostPanel(showGhost);
        }
    }

    /**
     * Starts a new game session.
     * Initiates the game start process through GameStateManager, hides the main menu,
     * and starts the countdown sequence if the game hasn't started yet.
     */
    public void startGame() {
        gameStateManager.startGame();
        if (!gameStateManager.isGameStarted() && mainMenuPanel != null) {
            panelCoordinator.hideMainMenuPanel();
            // Start countdown - delegated to CountdownManager
            countdownManager.startCountdown();
        }
    }
    
    /**
     * Actually starts the game after the countdown completes.
     * Initializes game state, starts music, sets up bindings, and makes panels visible.
     */
    private void actuallyStartGame() {
        // Update GameStateManager with current timeline reference
        gameStateManager.setTimeLine(gameLoopManager.getTimeLine());
        
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
        settingsController.ensureSettingsPanelInGameStack();
        
        // Make game panel, brick panel and ghost panel visible
        panelCoordinator.showGamePanel();
        panelCoordinator.showBrickPanel();
        if (ghostPanel != null) {
            // Check if ghost piece checkbox is selected
            boolean showGhost = settingsController.isGhostPieceEnabled();
            panelCoordinator.showGhostPanel(showGhost);
        }
        
        // Show and initialize next bricks panel for single player mode
        if (!gameStateManager.isMultiplayerMode() && nextBricksPanel != null && singlePlayerScreen != null) {
            panelCoordinator.showNextBricksPanelManaged();
            singlePlayerScreen.initializeNextBricksPanel();
            
            // Show the brick preview panes now that game has started
            List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
            if (nextBrickPanes != null) {
                panelCoordinator.showNextBrickPanes(nextBrickPanes);
            }
            
            // Update next bricks with initial bricks from the board
            if (eventListener instanceof GameController) {
                GameController gameController = (GameController) eventListener;
                if (gameController.getBoard() instanceof SimpleBoard) {
                    SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                    if (singlePlayerViewManager != null) {
                        singlePlayerViewManager.updateNextBricks(simpleBoard.getNextBricks());
                    }
                }
            }
        }
        
        // Refresh the brick display with stored brick data
        // The brick data was stored when initGameView was called
        if (singlePlayerViewManager != null) {
            ViewData currentBrickData = singlePlayerViewManager.getCurrentBrickData();
            if (currentBrickData != null) {
                singlePlayerViewManager.refreshBrick(currentBrickData);
            }
        }
        
        // Update GameStateManager with current timeline reference
        gameStateManager.setTimeLine(gameLoopManager.getTimeLine());
        
        // Start timer for single player mode (handled by GameStateManager)
        if (!gameStateManager.isMultiplayerMode()) {
            timerManager.startSinglePlayerTimer();
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

    /**
     * Quits the game application by exiting the JavaFX platform.
     */
    public void quitGame() {
        javafx.application.Platform.exit();
    }

    /**
     * Toggles the pause state of the game.
     * Updates timeline references and delegates to GameStateManager.
     * 
     * @param actionEvent The action event that triggered this method (can be null)
     */
    public void pauseGame(ActionEvent actionEvent) {
        // Update GameStateManager with current timeline references
        gameStateManager.setTimeLine(gameLoopManager.getTimeLine());
        gameStateManager.setTimeLine1(gameLoopManager.getTimeLine1());
        gameStateManager.setTimeLine2(gameLoopManager.getTimeLine2());
        gameStateManager.setGarbageProcessTimeline1(gameLoopManager.getGarbageProcessTimeline1());
        gameStateManager.setGarbageProcessTimeline2(gameLoopManager.getGarbageProcessTimeline2());
        
        // Delegate to GameStateManager
        gameStateManager.pauseGame();
    }
    
    /**
     * Initializes the pause panel with action handlers.
     */
    private void initializePausePanel() {
        if (pausePanel == null) return;
        PausePanelActionHandler handler = new PausePanelActionHandler(this);
        handler.setupPausePanelActions(pausePanel);
    }
    
    
    // Getters and setters for PausePanelActionHandler
    /**
     * Gets the game state manager.
     * @return The GameStateManager instance
     */
    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }
    
    /**
     * Gets the panel coordinator.
     * @return The PanelCoordinator instance
     */
    public PanelCoordinator getPanelCoordinator() {
        return panelCoordinator;
    }
    
    /**
     * Gets the multiplayer screen.
     * @return The MultiplayerScreen instance
     */
    public MultiplayerScreen getMultiplayerScreen() {
        return multiplayerScreen;
    }

    
    /**
     * Refreshes the game background display for a multiplayer player.
     * 
     * @param board The 2D integer array representing the game board matrix
     * @param playerNumber The player number (1 or 2) whose background should be refreshed
     */
    public void refreshMultiplayerGameBackground(int[][] board, int playerNumber) {
        if (multiplayerScreen != null) {
            gameViewRenderer.refreshGameBackground(multiplayerScreen, board, playerNumber);
        }
    }
    
    /**
     * Updates the next bricks display for a multiplayer player.
     * 
     * @param nextBricks The list of Brick objects representing upcoming pieces
     * @param playerNumber The player number (1 or 2) whose next bricks should be updated
     */
    public void updateMultiplayerNextBricks(List<com.comp2042.bricks.Brick> nextBricks, int playerNumber) {
        if (multiplayerScreen != null) {
            gameViewRenderer.updateNextBricks(multiplayerScreen, nextBricks, playerNumber);
        }
    }
    
    /**
     * Updates the hold brick display for a multiplayer player.
     * 
     * @param heldBrick The Brick object being held, or null if no brick is held
     * @param playerNumber The player number (1 or 2) whose hold brick should be updated
     */
    public void updateMultiplayerHoldBrick(com.comp2042.bricks.Brick heldBrick, int playerNumber) {
        if (multiplayerScreen != null) {
            gameViewRenderer.updateHoldBrick(multiplayerScreen, heldBrick, playerNumber);
        }
    }

    /**
     * Refreshes the game background display for single player mode.
     * 
     * @param board The 2D integer array representing the game board matrix
     */
    public void refreshSinglePlayerGameBackground(int[][] board) {
        if (singlePlayerViewManager != null) {
            singlePlayerViewManager.refreshGameBackground(board);
        }
    }
    
    /**
     * Updates the next bricks display for single player mode.
     * 
     * @param nextBricks The list of Brick objects representing upcoming pieces
     */
    public void updateSinglePlayerNextBricks(List<com.comp2042.bricks.Brick> nextBricks) {
        if (singlePlayerViewManager != null) {
            singlePlayerViewManager.updateNextBricks(nextBricks);
        }
    }
    
    /**
     * Updates the hold brick display for single player mode.
     * 
     * @param heldBrick The Brick object being held, or null if no brick is held
     */
    public void updateSinglePlayerHoldBrick(com.comp2042.bricks.Brick heldBrick) {
        if (singlePlayerViewManager != null) {
            singlePlayerViewManager.updateHoldBrick(heldBrick);
        }
    }
    
    /**
     * Gets the multiplayer view manager instance.
     * 
     * @return The MultiplayerViewManager instance
     */
    public MultiplayerViewManager getMultiplayerViewManager() {
        return multiplayerViewManager;
    }
    
    /**
     * Gets the pause panel instance.
     * 
     * @return The PausePanel instance
     */
    public PausePanel getPausePanel() {
        return pausePanel;
    }
    
    /**
     * Gets the single player screen instance.
     * 
     * @return The SinglePlayerScreen instance
     */
    public SinglePlayerScreen getSinglePlayerScreen() {
        return singlePlayerScreen;
    }
    
    /**
     * Gets the audio manager instance.
     * 
     * @return The AudioManager instance
     */
    public AudioManager getAudioManager() {
        return audioManager;
    }
    
    /**
     * Gets the timer manager instance.
     * 
     * @return The TimerManager instance
     */
    public TimerManager getTimerManager() {
        return timerManager;
    }
    
    /**
     * Gets the input handler instance.
     * 
     * @return The InputHandler instance
     */
    public InputHandler getInputHandler() {
        return inputHandler;
    }
    
    /**
     * Gets the single player game loop timeline.
     * 
     * @return The Timeline for single player game loop
     */
    public Timeline getTimeLine() {
        return gameLoopManager.getTimeLine();
    }
    
    /**
     * Gets the player 1 game loop timeline for multiplayer mode.
     * 
     * @return The Timeline for player 1's game loop
     */
    public Timeline getTimeLine1() {
        return gameLoopManager.getTimeLine1();
    }
    
    /**
     * Gets the player 2 game loop timeline for multiplayer mode.
     * 
     * @return The Timeline for player 2's game loop
     */
    public Timeline getTimeLine2() {
        return gameLoopManager.getTimeLine2();
    }

    /**
     * Sets the game controller for player 1 in multiplayer mode.
     * 
     * @param gameController1 The GameController for player 1
     */
    public void setGameController1(GameController gameController1) {
        this.gameController1 = gameController1;
        multiplayerViewManager.setGameController1(gameController1);
    }
    
    /**
     * Sets the game controller for player 2 in multiplayer mode.
     * 
     * @param gameController2 The GameController for player 2
     */
    public void setGameController2(GameController gameController2) {
        this.gameController2 = gameController2;
        multiplayerViewManager.setGameController2(gameController2);
    }
    
    /**
     * Gets the event listener for single player mode.
     * 
     * @return The InputEventListener for single player
     */
    public InputEventListener getEventListener() {
        return eventListener;
    }

    
    /**
     * Sets the input event listener for player 1.
     * @param eventListener1 The InputEventListener for player 1
     */
    public void setEventListener1(InputEventListener eventListener1) {
        this.eventListener1 = eventListener1;
    }
    
    /**
     * Sets the input event listener for player 2.
     * @param eventListener2 The InputEventListener for player 2
     */
    public void setEventListener2(InputEventListener eventListener2) {
        this.eventListener2 = eventListener2;
    }

    
    // Setters for compatibility (no-op since GameLoopManager manages timelines)
    /**
     * Sets the timeline for player 1 (no-op, managed by GameLoopManager).
     * @param timeLine1 The timeline for player 1 (not used)
     */
    public void setTimeLine1(Timeline timeLine1) {
        // No-op: GameLoopManager manages timelines
    }
    
    /**
     * Sets the timeline for player 2 (no-op, managed by GameLoopManager).
     * @param timeLine2 The timeline for player 2 (not used)
     */
    public void setTimeLine2(Timeline timeLine2) {
        // No-op: GameLoopManager manages timelines
    }
    
    /**
     * Sets the garbage process timeline for player 1 (no-op, managed by GameLoopManager).
     * @param garbageProcessTimeline1 The garbage process timeline for player 1 (not used)
     */
    public void setGarbageProcessTimeline1(Timeline garbageProcessTimeline1) {
        // No-op: GameLoopManager manages timelines
    }
    
    /**
     * Sets the garbage process timeline for player 2 (no-op, managed by GameLoopManager).
     * @param garbageProcessTimeline2 The garbage process timeline for player 2 (not used)
     */
    public void setGarbageProcessTimeline2(Timeline garbageProcessTimeline2) {
        // No-op: GameLoopManager manages timelines
    }
    
    /**
     * Gets the game board border pane.
     * @return The BorderPane representing the game board
     */
    public BorderPane getGameBoard() {
        return gameBoard;
    }
    
    /**
     * Gets the game stack pane.
     * @return The StackPane containing game layers
     */
    public StackPane getGameStack() {
        return gameStack;
    }
    
    /**
     * Gets the game panel grid pane.
     * @return The GridPane representing the game panel
     */
    public GridPane getGamePanel() {
        return gamePanel;
    }
    
    /**
     * Gets the brick panel grid pane.
     * @return The GridPane for displaying the current brick
     */
    public GridPane getBrickPanel() {
        return brickPanel;
    }
    
    /**
     * Gets the ghost panel grid pane.
     * @return The GridPane for displaying the ghost brick preview
     */
    public GridPane getGhostPanel() {
        return ghostPanel;
    }
    
    /**
     * Gets the settings panel.
     * @return The SettingsPanel instance
     */
    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }
    
    /**
     * Gets the settings controller.
     * @return The SettingsController instance
     */
    public SettingsController getSettingsController() {
        return settingsController;
    }
    
    /**
     * Gets the main menu panel.
     * @return The MainMenuPanel instance
     */
    public MainMenuPanel getMainMenuPanel() {
        return mainMenuPanel;
    }
    
    /**
     * Gets the game over panel.
     * @return The GameOverPanel instance
     */
    public GameOverPanel getGameOverPanel() {
        return gameOverPanel;
    }
    
    /**
     * Gets the next bricks panel.
     * @return The VBox containing the next bricks preview
     */
    public VBox getNextBricksPanel() {
        return nextBricksPanel;
    }
    
    /**
     * Gets the hold brick panel grid pane.
     * @return The GridPane for displaying the held brick
     */
    public GridPane getHoldBrickPanel() {
        return holdBrickPanel;
    }
    
    /**
     * Gets the score label.
     * @return The Label displaying the score
     */
    public Label getScoreLabel() {
        return scoreLabel;
    }
    
    /**
     * Gets the level label.
     * @return The Label displaying the level
     */
    public Label getLevelLabel() {
        return levelLabel;
    }
    
    /**
     * Gets the lines label.
     * @return The Label displaying the lines cleared
     */
    public Label getLinesLabel() {
        return linesLabel;
    }
    
    /**
     * Gets the original left panel.
     * @return The VBox representing the original left panel
     */
    public VBox getOriginalLeftPanel() {
        return originalLeftPanel;
    }
    
    /**
     * Gets the original right panel.
     * @return The VBox representing the original right panel
     */
    public VBox getOriginalRightPanel() {
        return originalRightPanel;
    }
    
    /**
     * Gets the scene key pressed event handler.
     * @return The EventHandler for key pressed events
     */
    public javafx.event.EventHandler<KeyEvent> getSceneKeyPressedHandler() {
        return multiplayerViewManager.getSceneKeyPressedHandler();
    }
    
    /**
     * Gets the scene key released event handler.
     * @return The EventHandler for key released events
     */
    public javafx.event.EventHandler<KeyEvent> getSceneKeyReleasedHandler() {
        return multiplayerViewManager.getSceneKeyReleasedHandler();
    }
    
    /**
     * Sets the scene key pressed event handler.
     * @param sceneKeyPressedHandler The EventHandler for key pressed events
     */
    public void setSceneKeyPressedHandler(javafx.event.EventHandler<KeyEvent> sceneKeyPressedHandler) {
        this.sceneKeyPressedHandler = sceneKeyPressedHandler;
        multiplayerViewManager.setSceneKeyPressedHandler(sceneKeyPressedHandler);
    }
    
    /**
     * Sets the scene key released event handler.
     * @param sceneKeyReleasedHandler The EventHandler for key released events
     */
    public void setSceneKeyReleasedHandler(javafx.event.EventHandler<KeyEvent> sceneKeyReleasedHandler) {
        this.sceneKeyReleasedHandler = sceneKeyReleasedHandler;
        multiplayerViewManager.setSceneKeyReleasedHandler(sceneKeyReleasedHandler);
    }
    
}

