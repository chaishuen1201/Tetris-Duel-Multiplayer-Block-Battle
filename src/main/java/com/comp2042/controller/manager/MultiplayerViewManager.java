package com.comp2042.controller.manager;

import com.comp2042.controller.GameController;
import com.comp2042.controller.input.InputHandler;
import com.comp2042.view.MultiplayerScreen;
import com.comp2042.view.SettingsPanel;
import com.comp2042.view.SinglePlayerScreen;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.util.List;
import java.util.function.Supplier;

/**
 * Manages multiplayer view operations and UI state transitions.
 * Handles showing multiplayer screen, starting/restarting games, quitting to main menu, and keyboard handler attachment.
 */
public class MultiplayerViewManager {
    
    // Dependencies
    private final GameStateManager gameStateManager;
    private final GameLoopManager gameLoopManager;
    private final PanelCoordinator panelCoordinator;
    private final TimerManager timerManager;
    private final AudioManager audioManager;
    private final InputHandler inputHandler;
    private final GarbageManager garbageManager;
    
    // UI Components (set via setters)
    private MultiplayerScreen multiplayerScreen;
    private SinglePlayerScreen singlePlayerScreen;
    private BorderPane gameBoard;
    private GridPane holdBrickPanel;
    private VBox nextBricksPanel;
    private Label scoreLabel;
    private Label levelLabel;
    private Label linesLabel;
    private SettingsPanel settingsPanel;
    private VBox originalLeftPanel;
    private VBox originalRightPanel;
    
    // Game controllers and event listeners (set via setters)
    private GameController gameController1;
    private GameController gameController2;
    private com.comp2042.event.InputEventListener eventListener1;
    private com.comp2042.event.InputEventListener eventListener2;
    
    // Keyboard handlers
    private EventHandler<KeyEvent> sceneKeyPressedHandler;
    private EventHandler<KeyEvent> sceneKeyReleasedHandler;
    
    // Supplier for getting root BorderPane
    private Supplier<BorderPane> rootBorderPaneSupplier;
    
    // Supplier for creating game controllers
    private GameControllerFactory gameControllerFactory;
    
    // Callback to sync game controllers back to GuiController
    private GameControllerSyncCallback gameControllerSyncCallback;
    
    // Callback to sync original panels back to GuiController
    private OriginalPanelSyncCallback originalPanelSyncCallback;
    
    public interface GameControllerFactory {
        GameController createGameController(int playerNumber);
    }
    
    public interface GameControllerSyncCallback {
        void syncGameControllers(GameController gameController1, GameController gameController2);
    }
    
    public interface OriginalPanelSyncCallback {
        void syncOriginalPanels(VBox originalLeftPanel, VBox originalRightPanel);
    }
    
    public MultiplayerViewManager(
            GameStateManager gameStateManager,
            GameLoopManager gameLoopManager,
            PanelCoordinator panelCoordinator,
            TimerManager timerManager,
            AudioManager audioManager,
            InputHandler inputHandler,
            GarbageManager garbageManager) {
        this.gameStateManager = gameStateManager;
        this.gameLoopManager = gameLoopManager;
        this.panelCoordinator = panelCoordinator;
        this.timerManager = timerManager;
        this.audioManager = audioManager;
        this.inputHandler = inputHandler;
        this.garbageManager = garbageManager;
    }
    
    // Setters for UI components
    public void setMultiplayerScreen(MultiplayerScreen multiplayerScreen) {
        this.multiplayerScreen = multiplayerScreen;
    }
    
    public void setSinglePlayerScreen(SinglePlayerScreen singlePlayerScreen) {
        this.singlePlayerScreen = singlePlayerScreen;
    }
    
    public void setGameBoard(BorderPane gameBoard) {
        this.gameBoard = gameBoard;
    }
    
    public void setHoldBrickPanel(GridPane holdBrickPanel) {
        this.holdBrickPanel = holdBrickPanel;
    }
    
    public void setNextBricksPanel(VBox nextBricksPanel) {
        this.nextBricksPanel = nextBricksPanel;
    }
    
    public void setScoreLabel(Label scoreLabel) {
        this.scoreLabel = scoreLabel;
    }
    
    public void setLevelLabel(Label levelLabel) {
        this.levelLabel = levelLabel;
    }
    
    public void setLinesLabel(Label linesLabel) {
        this.linesLabel = linesLabel;
    }
    
    public void setSettingsPanel(SettingsPanel settingsPanel) {
        this.settingsPanel = settingsPanel;
    }
    
    public void setOriginalLeftPanel(VBox originalLeftPanel) {
        this.originalLeftPanel = originalLeftPanel;
    }
    
    public void setOriginalRightPanel(VBox originalRightPanel) {
        this.originalRightPanel = originalRightPanel;
    }
    
    public void setGameController1(GameController gameController1) {
        this.gameController1 = gameController1;
    }
    
    public void setGameController2(GameController gameController2) {
        this.gameController2 = gameController2;
    }
    
    public void setEventListener1(com.comp2042.event.InputEventListener eventListener1) {
        this.eventListener1 = eventListener1;
    }
    
    public void setEventListener2(com.comp2042.event.InputEventListener eventListener2) {
        this.eventListener2 = eventListener2;
    }
    
    public void setRootBorderPaneSupplier(Supplier<BorderPane> rootBorderPaneSupplier) {
        this.rootBorderPaneSupplier = rootBorderPaneSupplier;
    }
    
    public void setGameControllerFactory(GameControllerFactory gameControllerFactory) {
        this.gameControllerFactory = gameControllerFactory;
    }
    
    public void setGameControllerSyncCallback(GameControllerSyncCallback gameControllerSyncCallback) {
        this.gameControllerSyncCallback = gameControllerSyncCallback;
    }
    
    public void setOriginalPanelSyncCallback(OriginalPanelSyncCallback originalPanelSyncCallback) {
        this.originalPanelSyncCallback = originalPanelSyncCallback;
    }
    
    public EventHandler<KeyEvent> getSceneKeyPressedHandler() {
        return sceneKeyPressedHandler;
    }
    
    public EventHandler<KeyEvent> getSceneKeyReleasedHandler() {
        return sceneKeyReleasedHandler;
    }
    
    public void setSceneKeyPressedHandler(EventHandler<KeyEvent> sceneKeyPressedHandler) {
        this.sceneKeyPressedHandler = sceneKeyPressedHandler;
    }
    
    public void setSceneKeyReleasedHandler(EventHandler<KeyEvent> sceneKeyReleasedHandler) {
        this.sceneKeyReleasedHandler = sceneKeyReleasedHandler;
    }
    
    /**
     * Shows the multiplayer screen and sets up the UI for multiplayer mode.
     */
    public void showMultiplayer() {
        panelCoordinator.hideMainMenuPanel();
        
        gameStateManager.setMultiplayerMode(true);
        
        if (multiplayerScreen == null) {
            // MultiplayerScreen should be created and callbacks set up by GuiController
            return;
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
                panelCoordinator.setOriginalLeftPanel(originalLeftPanel);
            }
            if (originalRightPanel == null && rootPane.getRight() instanceof VBox) {
                originalRightPanel = (VBox) rootPane.getRight();
                panelCoordinator.setOriginalRightPanel(originalRightPanel);
            }
            
            // Sync panels back via callback if needed
            if (originalPanelSyncCallback != null) {
                originalPanelSyncCallback.syncOriginalPanels(originalLeftPanel, originalRightPanel);
            }
            
            // Hide original left and right panels
            panelCoordinator.hideOriginalLeftPanel(rootPane);
            panelCoordinator.hideOriginalRightPanel(rootPane);
        }
        
        // Hide single player game board
        panelCoordinator.hideGameBoard();
        
        // Hide original single player panels
        panelCoordinator.hideSinglePlayerPanels();
        
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
    
    /**
     * Attaches keyboard handlers to the scene for multiplayer mode.
     */
    public void attachKeyboardHandlersToScene() {
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
    
    /**
     * Starts a new multiplayer game.
     */
    public void startMultiplayerGame() {
        if (multiplayerScreen != null) {
            multiplayerScreen.hideReadyPanel();
        }
        
        // Create game controllers for both players
        if (gameControllerFactory != null) {
            gameController1 = gameControllerFactory.createGameController(1);
            gameController2 = gameControllerFactory.createGameController(2);
            
            // Sync controllers back to GuiController if callback is set
            if (gameControllerSyncCallback != null) {
                gameControllerSyncCallback.syncGameControllers(gameController1, gameController2);
            }
        }
        
        // Update GameLoopManager with controllers and listeners
        gameLoopManager.setGameController1(gameController1);
        gameLoopManager.setGameController2(gameController2);
        gameLoopManager.setEventListener1(eventListener1);
        gameLoopManager.setEventListener2(eventListener2);
        
        // Update GameStateManager with controllers and listeners
        gameStateManager.setGameController1(gameController1);
        gameStateManager.setGameController2(gameController2);
        gameStateManager.setEventListener1(eventListener1);
        gameStateManager.setEventListener2(eventListener2);
        gameStateManager.setTimeLine1(gameLoopManager.getTimeLine1());
        gameStateManager.setTimeLine2(gameLoopManager.getTimeLine2());
        gameStateManager.setGarbageProcessTimeline1(gameLoopManager.getGarbageProcessTimeline1());
        gameStateManager.setGarbageProcessTimeline2(gameLoopManager.getGarbageProcessTimeline2());
        gameStateManager.setMultiplayerMode(true);
        
        // Update GarbageManager with controllers
        garbageManager.setGameControllers(gameController1, gameController2);
        
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
    
    /**
     * Restarts the multiplayer game.
     */
    public void restartMultiplayerGame() {
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
        gameStateManager.setTimeLine1(gameLoopManager.getTimeLine1());
        gameStateManager.setTimeLine2(gameLoopManager.getTimeLine2());
        gameStateManager.setGarbageProcessTimeline1(gameLoopManager.getGarbageProcessTimeline1());
        gameStateManager.setGarbageProcessTimeline2(gameLoopManager.getGarbageProcessTimeline2());
        
        // Update GarbageManager with controllers
        garbageManager.setGameControllers(gameController1, gameController2);
        
        // Delegate to GameStateManager
        gameStateManager.restartMultiplayerGame();
    }
    
    /**
     * Quits from multiplayer mode back to the main menu.
     */
    public void quitToMainMenuFromMultiplayer() {
        // Clear all multiplayer game panels
        if (multiplayerScreen != null) {
            multiplayerScreen.clearGamePanels();
        }
        
        // Update GameStateManager with current references
        gameStateManager.setTimeLine(gameLoopManager.getTimeLine());
        gameStateManager.setTimeLine1(gameLoopManager.getTimeLine1());
        gameStateManager.setTimeLine2(gameLoopManager.getTimeLine2());
        
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
                panelCoordinator.showGameBoard();
                centerVBox.requestLayout();
            }
        }
        
        // Hide pause panels
        panelCoordinator.hidePausePanel();
        
        // Restore original panels
        BorderPane rootPane = getRootBorderPane();
        if (rootPane != null) {
            panelCoordinator.showOriginalLeftPanel(rootPane);
            panelCoordinator.showOriginalRightPanel(rootPane);
            
            // Ensure all children of left panel VBox are visible
            VBox leftPanelToProcess = null;
            if (originalLeftPanel != null && originalLeftPanel instanceof VBox) {
                leftPanelToProcess = (VBox) originalLeftPanel;
            } else if (rootPane.getLeft() instanceof VBox) {
                leftPanelToProcess = (VBox) rootPane.getLeft();
            }
            if (leftPanelToProcess != null) {
                panelCoordinator.showAllChildrenOfPanel(leftPanelToProcess);
            }
            
            // Ensure all children of right panel VBox are visible
            VBox rightPanelToProcess = null;
            if (originalRightPanel != null && originalRightPanel instanceof VBox) {
                rightPanelToProcess = (VBox) originalRightPanel;
            } else if (rootPane.getRight() instanceof VBox) {
                rightPanelToProcess = (VBox) rootPane.getRight();
            }
            if (rightPanelToProcess != null) {
                panelCoordinator.showAllChildrenOfPanel(rightPanelToProcess);
            }
            
            // Force root pane to update layout
            rootPane.requestLayout();
        }
        
        // Re-initialize hold panel (left panel)
        if (holdBrickPanel != null) {
            panelCoordinator.showHoldBrickPanelManaged();
            if (singlePlayerScreen != null) {
                singlePlayerScreen.initializeHoldPanel();
            }
        }
        
        // Keep next bricks panel container visible, but hide the brick previews when returning to main menu
        if (nextBricksPanel != null) {
            panelCoordinator.showNextBricksPanelManaged();
            // Still initialize it so it's ready when game starts
            if (singlePlayerScreen != null) {
                singlePlayerScreen.initializeNextBricksPanel();
                // Hide the individual brick panes
                List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
                if (nextBrickPanes != null) {
                    panelCoordinator.hideNextBrickPanes(nextBrickPanes);
                }
            }
        }
        
        // Show single player labels and reset them
        if (scoreLabel != null) {
            panelCoordinator.showScoreLabel();
            // Unbind if bound to clear any old bindings
            if (scoreLabel.textProperty().isBound()) {
                scoreLabel.textProperty().unbind();
            }
            scoreLabel.setText("0");
        }
        if (levelLabel != null) {
            panelCoordinator.showLevelLabel();
            // Unbind if bound to clear any old bindings
            if (levelLabel.textProperty().isBound()) {
                levelLabel.textProperty().unbind();
            }
            levelLabel.setText("1");
        }
        if (linesLabel != null) {
            panelCoordinator.showLinesLabel();
            // Unbind if bound to clear any old bindings
            if (linesLabel.textProperty().isBound()) {
                linesLabel.textProperty().unbind();
            }
            linesLabel.setText("0");
        }
        
        // Show single player game board
        panelCoordinator.showGameBoard();
        
        // Show main menu
        panelCoordinator.showMainMenuPanel();
        
        // Stop game music and play main menu music
        audioManager.stopGameMusic();
        audioManager.playMainMenuMusic();
    }
    
    /**
     * Gets the root BorderPane from the scene.
     */
    private BorderPane getRootBorderPane() {
        if (rootBorderPaneSupplier != null) {
            return rootBorderPaneSupplier.get();
        }
        return null;
    }
    
    // Getters for game controllers
    public GameController getGameController1() {
        return gameController1;
    }
    
    public GameController getGameController2() {
        return gameController2;
    }
}

