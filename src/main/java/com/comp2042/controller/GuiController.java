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
            setupButtonWithSound(mainMenuPanel.getPlayButton(), () -> startGame());
            setupButtonWithSound(mainMenuPanel.getMultiButton(), () -> multiplayerViewManager.showMultiplayer());
            setupButtonWithSound(mainMenuPanel.getSettingsButton(), () -> settingsController.showSettings());
            setupButtonWithSound(mainMenuPanel.getQuitButton(), () -> quitGame());
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

    public void setEventListener(InputEventListener listener) { 
        this.eventListener = listener;
        gameLoopManager.setEventListener(listener);
        if (singlePlayerScreen != null) {
            singlePlayerScreen.setEventListener(listener);
        }
    }
    
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

    public void bindScore(IntegerProperty score) {
        bindScore(score, 0);
    }
    
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

    public void bindLevel(IntegerProperty level) {
        bindLevel(level, 0);
    }
    
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
     * Delegates to GarbageManager.
     * @param fromPlayerNumber The player number who cleared lines (1 or 2)
     * @param numGarbageLines Number of garbage lines to send
     */
    public void sendGarbageToOpponent(int fromPlayerNumber, int numGarbageLines) {
        garbageManager.sendGarbageToOpponent(fromPlayerNumber, numGarbageLines);
    }
    

    public void gameOver() {
        gameOver(0);
    }
    
    public void gameOver(int playerNumber) {
        // Update GameStateManager with current timeline references
        gameStateManager.setTimeLine(gameLoopManager.getTimeLine());
        gameStateManager.setTimeLine1(gameLoopManager.getTimeLine1());
        gameStateManager.setTimeLine2(gameLoopManager.getTimeLine2());
        
        // Delegate to GameStateManager
        gameStateManager.gameOver(playerNumber);
    }

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

    public void startGame() {
        gameStateManager.startGame();
        if (!gameStateManager.isGameStarted() && mainMenuPanel != null) {
            panelCoordinator.hideMainMenuPanel();
            // Start countdown - delegated to CountdownManager
            countdownManager.startCountdown();
        }
    }
    
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

    public void quitGame() {
        javafx.application.Platform.exit();
    }

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
    
    private void initializePausePanel() {
        if (pausePanel == null) return;
        PausePanelActionHandler handler = new PausePanelActionHandler(this);
        handler.setupPausePanelActions(pausePanel);
    }
    
    
    // Getters and setters for PausePanelActionHandler
    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }
    
    public PanelCoordinator getPanelCoordinator() {
        return panelCoordinator;
    }
    
    public MultiplayerScreen getMultiplayerScreen() {
        return multiplayerScreen;
    }

    
    public void refreshMultiplayerGameBackground(int[][] board, int playerNumber) {
        if (multiplayerScreen != null) {
            gameViewRenderer.refreshGameBackground(multiplayerScreen, board, playerNumber);
        }
    }
    
    public void updateMultiplayerNextBricks(List<com.comp2042.logic.bricks.Brick> nextBricks, int playerNumber) {
        if (multiplayerScreen != null) {
            gameViewRenderer.updateNextBricks(multiplayerScreen, nextBricks, playerNumber);
        }
    }
    
    public void updateMultiplayerHoldBrick(com.comp2042.logic.bricks.Brick heldBrick, int playerNumber) {
        if (multiplayerScreen != null) {
            gameViewRenderer.updateHoldBrick(multiplayerScreen, heldBrick, playerNumber);
        }
    }

    
    public void refreshSinglePlayerGameBackground(int[][] board) {
        if (singlePlayerViewManager != null) {
            singlePlayerViewManager.refreshGameBackground(board);
        }
    }
    
    public void updateSinglePlayerNextBricks(List<com.comp2042.logic.bricks.Brick> nextBricks) {
        if (singlePlayerViewManager != null) {
            singlePlayerViewManager.updateNextBricks(nextBricks);
        }
    }
    
    public void updateSinglePlayerHoldBrick(com.comp2042.logic.bricks.Brick heldBrick) {
        if (singlePlayerViewManager != null) {
            singlePlayerViewManager.updateHoldBrick(heldBrick);
        }
    }
    
    public MultiplayerViewManager getMultiplayerViewManager() {
        return multiplayerViewManager;
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
    
    public TimerManager getTimerManager() {
        return timerManager;
    }
    
    public InputHandler getInputHandler() {
        return inputHandler;
    }
    
    public Timeline getTimeLine() {
        return gameLoopManager.getTimeLine();
    }
    
    public Timeline getTimeLine1() {
        return gameLoopManager.getTimeLine1();
    }
    
    public Timeline getTimeLine2() {
        return gameLoopManager.getTimeLine2();
    }

    
    public void setGameController1(GameController gameController1) {
        this.gameController1 = gameController1;
        multiplayerViewManager.setGameController1(gameController1);
    }
    
    public void setGameController2(GameController gameController2) {
        this.gameController2 = gameController2;
        multiplayerViewManager.setGameController2(gameController2);
    }
    
    public InputEventListener getEventListener() {
        return eventListener;
    }

    
    public void setEventListener1(InputEventListener eventListener1) {
        this.eventListener1 = eventListener1;
    }
    
    public void setEventListener2(InputEventListener eventListener2) {
        this.eventListener2 = eventListener2;
    }

    
    // Setters for compatibility (no-op since GameLoopManager manages timelines)
    public void setTimeLine1(Timeline timeLine1) {
        // No-op: GameLoopManager manages timelines
    }
    
    public void setTimeLine2(Timeline timeLine2) {
        // No-op: GameLoopManager manages timelines
    }
    
    public void setGarbageProcessTimeline1(Timeline garbageProcessTimeline1) {
        // No-op: GameLoopManager manages timelines
    }
    
    public void setGarbageProcessTimeline2(Timeline garbageProcessTimeline2) {
        // No-op: GameLoopManager manages timelines
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
    
    public SettingsController getSettingsController() {
        return settingsController;
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
        return multiplayerViewManager.getSceneKeyPressedHandler();
    }
    
    public javafx.event.EventHandler<KeyEvent> getSceneKeyReleasedHandler() {
        return multiplayerViewManager.getSceneKeyReleasedHandler();
    }
    
    public void setSceneKeyPressedHandler(javafx.event.EventHandler<KeyEvent> sceneKeyPressedHandler) {
        this.sceneKeyPressedHandler = sceneKeyPressedHandler;
        multiplayerViewManager.setSceneKeyPressedHandler(sceneKeyPressedHandler);
    }
    
    public void setSceneKeyReleasedHandler(javafx.event.EventHandler<KeyEvent> sceneKeyReleasedHandler) {
        this.sceneKeyReleasedHandler = sceneKeyReleasedHandler;
        multiplayerViewManager.setSceneKeyReleasedHandler(sceneKeyReleasedHandler);
    }
    
    /**
     * Sets up a button with click and hover sound effects.
     * @param button The button to set up
     * @param action The action to execute when button is clicked
     */
    private void setupButtonWithSound(javafx.scene.control.Button button, Runnable action) {
        if (button == null) return;
        
        // Add click sound
        button.setOnAction(e -> {
            audioManager.playClickButton();
            if (action != null) {
                action.run();
            }
        });
        
        // Add hover sound
        button.setOnMouseEntered(e -> {
            audioManager.playHover();
        });
    }
    
}

