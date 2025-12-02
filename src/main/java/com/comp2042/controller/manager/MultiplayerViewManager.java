package com.comp2042.controller.manager;

import com.comp2042.controller.GameController;
import com.comp2042.controller.input.InputHandler;
import com.comp2042.view.MultiplayerScreen;
import com.comp2042.view.GameViewRenderer;
import com.comp2042.view.SettingsPanel;
import com.comp2042.view.SinglePlayerScreen;
import javafx.application.Platform;
import javafx.event.EventHandler;
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
 * Manages multiplayer view operations and UI state transitions for multiplayer mode.
 * This class coordinates the display and interaction of the multiplayer game screen,
 * handling UI setup, game initialization, ready state management, keyboard input handling,
 * score/level binding, panel visibility, and transitions between multiplayer and single
 * player modes. It integrates with game controllers, event listeners, view renderers,
 * and various managers to provide a complete multiplayer game experience. The manager
 * handles the ready panel system where both players must confirm readiness before
 * starting, manages keyboard event filtering at the scene level, and coordinates
 * cleanup when returning to the main menu.
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
    private final GameViewRenderer renderer = new GameViewRenderer();
    private BorderPane gameBoard;
    private VBox centerVBox; // The main center container for adding multiplayer screen
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
    
    // Level change listeners (to prevent duplicate listeners)
    private javafx.beans.value.ChangeListener<? super Number> levelChangeListener1;
    private javafx.beans.value.ChangeListener<? super Number> levelChangeListener2;
    
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
    
    // Callback for starting game when both players are ready
    private Runnable onStartGameCallback;
    
    /**
     * Factory interface for creating game controllers for multiplayer players.
     */
    public interface GameControllerFactory {
        /**
         * Creates a new game controller for the specified player.
         * 
         * @param playerNumber The player number (1 or 2) for which to create the controller
         * @return A new GameController instance for the specified player
         */
        GameController createGameController(int playerNumber);
    }
    
    /**
     * Callback interface for syncing game controllers back to the main controller.
     */
    public interface GameControllerSyncCallback {
        /**
         * Called to sync the created game controllers back to the main controller.
         * 
         * @param gameController1 The GameController for player 1
         * @param gameController2 The GameController for player 2
         */
        void syncGameControllers(GameController gameController1, GameController gameController2);
    }
    
    /**
     * Callback interface for syncing original panels back to the main controller.
     */
    public interface OriginalPanelSyncCallback {
        /**
         * Called to sync the original left and right panels back to the main controller.
         * 
         * @param originalLeftPanel The original left panel VBox
         * @param originalRightPanel The original right panel VBox
         */
        void syncOriginalPanels(VBox originalLeftPanel, VBox originalRightPanel);
    }
    
    /**
     * Creates a new MultiplayerViewManager with the specified dependencies.
     * 
     * @param gameStateManager The game state manager for checking game state
     * @param gameLoopManager The game loop manager for timeline operations
     * @param panelCoordinator The panel coordinator for managing panel visibility
     * @param timerManager The timer manager for multiplayer timer operations
     * @param audioManager The audio manager for playing sound effects
     * @param inputHandler The input handler for processing keyboard input
     * @param garbageManager The garbage manager for multiplayer garbage operations
     */
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
    /**
     * Sets the multiplayer screen reference.
     * 
     * @param multiplayerScreen The MultiplayerScreen instance for multiplayer mode
     */
    public void setMultiplayerScreen(MultiplayerScreen multiplayerScreen) {
        this.multiplayerScreen = multiplayerScreen;
    }
    
    /**
     * Sets the single player screen reference.
     * 
     * @param singlePlayerScreen The SinglePlayerScreen instance
     */
    public void setSinglePlayerScreen(SinglePlayerScreen singlePlayerScreen) {
        this.singlePlayerScreen = singlePlayerScreen;
    }
    
    /**
     * Sets the game board BorderPane reference.
     * 
     * @param gameBoard The BorderPane containing the game board
     */
    public void setGameBoard(BorderPane gameBoard) {
        this.gameBoard = gameBoard;
    }
    
    /**
     * Sets the center VBox container for adding multiplayer screen.
     * 
     * @param centerVBox The VBox container in the center area
     */
    public void setCenterVBox(VBox centerVBox) {
        this.centerVBox = centerVBox;
    }
    
    /**
     * Sets the hold brick panel GridPane reference.
     * 
     * @param holdBrickPanel The GridPane for displaying held bricks
     */
    public void setHoldBrickPanel(GridPane holdBrickPanel) {
        this.holdBrickPanel = holdBrickPanel;
    }
    
    /**
     * Sets the next bricks panel VBox reference.
     * 
     * @param nextBricksPanel The VBox container for next brick previews
     */
    public void setNextBricksPanel(VBox nextBricksPanel) {
        this.nextBricksPanel = nextBricksPanel;
    }
    
    /**
     * Sets the score label reference.
     * 
     * @param scoreLabel The Label for displaying score
     */
    public void setScoreLabel(Label scoreLabel) {
        this.scoreLabel = scoreLabel;
    }
    
    /**
     * Sets the level label reference.
     * 
     * @param levelLabel The Label for displaying level
     */
    public void setLevelLabel(Label levelLabel) {
        this.levelLabel = levelLabel;
    }
    
    /**
     * Sets the lines label reference.
     * 
     * @param linesLabel The Label for displaying lines cleared
     */
    public void setLinesLabel(Label linesLabel) {
        this.linesLabel = linesLabel;
    }
    
    /**
     * Sets the settings panel reference.
     * 
     * @param settingsPanel The SettingsPanel instance
     */
    public void setSettingsPanel(SettingsPanel settingsPanel) {
        this.settingsPanel = settingsPanel;
    }
    
    /**
     * Sets the original left panel VBox reference.
     * 
     * @param originalLeftPanel The original left panel VBox to restore later
     */
    public void setOriginalLeftPanel(VBox originalLeftPanel) {
        this.originalLeftPanel = originalLeftPanel;
    }
    
    /**
     * Sets the original right panel VBox reference.
     * 
     * @param originalRightPanel The original right panel VBox to restore later
     */
    public void setOriginalRightPanel(VBox originalRightPanel) {
        this.originalRightPanel = originalRightPanel;
    }
    
    /**
     * Sets the game controller for player 1.
     * 
     * @param gameController1 The GameController for player 1
     */
    public void setGameController1(GameController gameController1) {
        this.gameController1 = gameController1;
    }
    
    /**
     * Sets the game controller for player 2.
     * 
     * @param gameController2 The GameController for player 2
     */
    public void setGameController2(GameController gameController2) {
        this.gameController2 = gameController2;
    }
    
    /**
     * Sets the event listener for player 1.
     * 
     * @param eventListener1 The InputEventListener for player 1
     */
    public void setEventListener1(com.comp2042.event.InputEventListener eventListener1) {
        this.eventListener1 = eventListener1;
    }
    
    /**
     * Sets the event listener for player 2.
     * 
     * @param eventListener2 The InputEventListener for player 2
     */
    public void setEventListener2(com.comp2042.event.InputEventListener eventListener2) {
        this.eventListener2 = eventListener2;
    }
    
    /**
     * Sets the supplier for getting the root BorderPane.
     * 
     * @param rootBorderPaneSupplier The Supplier that provides the root BorderPane
     */
    public void setRootBorderPaneSupplier(Supplier<BorderPane> rootBorderPaneSupplier) {
        this.rootBorderPaneSupplier = rootBorderPaneSupplier;
    }
    
    /**
     * Sets the factory for creating game controllers.
     * 
     * @param gameControllerFactory The GameControllerFactory implementation
     */
    public void setGameControllerFactory(GameControllerFactory gameControllerFactory) {
        this.gameControllerFactory = gameControllerFactory;
    }
    
    /**
     * Sets the callback for syncing game controllers back to the main controller.
     * 
     * @param gameControllerSyncCallback The GameControllerSyncCallback implementation
     */
    public void setGameControllerSyncCallback(GameControllerSyncCallback gameControllerSyncCallback) {
        this.gameControllerSyncCallback = gameControllerSyncCallback;
    }
    
    /**
     * Sets the callback for syncing original panels back to the main controller.
     * 
     * @param originalPanelSyncCallback The OriginalPanelSyncCallback implementation
     */
    public void setOriginalPanelSyncCallback(OriginalPanelSyncCallback originalPanelSyncCallback) {
        this.originalPanelSyncCallback = originalPanelSyncCallback;
    }
    
    /**
     * Gets the scene key pressed event handler.
     * 
     * @return The EventHandler for key pressed events
     */
    public EventHandler<KeyEvent> getSceneKeyPressedHandler() {
        return sceneKeyPressedHandler;
    }
    
    /**
     * Gets the scene key released event handler.
     * 
     * @return The EventHandler for key released events
     */
    public EventHandler<KeyEvent> getSceneKeyReleasedHandler() {
        return sceneKeyReleasedHandler;
    }
    
    /**
     * Sets the scene key pressed event handler.
     * 
     * @param sceneKeyPressedHandler The EventHandler for key pressed events
     */
    public void setSceneKeyPressedHandler(EventHandler<KeyEvent> sceneKeyPressedHandler) {
        this.sceneKeyPressedHandler = sceneKeyPressedHandler;
    }
    
    /**
     * Sets the scene key released event handler.
     * 
     * @param sceneKeyReleasedHandler The EventHandler for key released events
     */
    public void setSceneKeyReleasedHandler(EventHandler<KeyEvent> sceneKeyReleasedHandler) {
        this.sceneKeyReleasedHandler = sceneKeyReleasedHandler;
    }
    
    /**
     * Shows the multiplayer screen and sets up the UI for multiplayer mode.
     * Hides the main menu, sets multiplayer mode flag, initializes multiplayer panels,
     * registers timer label, stores and hides original panels, shows the multiplayer
     * container, displays the ready panel, and attaches keyboard handlers to the scene.
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
        if (centerVBox != null) {
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
        
        // Show ready panel instead of starting game immediately
        showReadyPanel();
        
        // Attach keyboard handlers to the scene for ready state
        Platform.runLater(() -> {
            attachKeyboardHandlersToScene();
        });
    }
    
    /**
     * Attaches keyboard handlers to the scene for multiplayer mode.
     * Removes any existing handlers to avoid duplicates, then adds event filters
     * at the scene level so they work regardless of focus. For multiplayer mode,
     * uses scene-level filters; for single player mode, uses node-level handlers.
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
     * Starts a new multiplayer game after both players are ready.
     * Hides the ready panel, creates game controllers for both players, updates
     * managers with controllers and listeners, sets up garbage manager, hides
     * winning panel, resets ready states, starts the game through GameStateManager,
     * makes brick panels visible, and attaches keyboard handlers.
     */
    public void startMultiplayerGame() {
        hideReadyPanel();
        
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
        gameStateManager.hideWinningPanel();
        
        // Reset ready states
        if (multiplayerScreen != null) {
            multiplayerScreen.setPlayer1ReadyState(false);
            multiplayerScreen.setPlayer2ReadyState(false);
        }
        
        // Delegate to GameStateManager
        gameStateManager.startMultiplayerGame();
        
        // Make brick panels and ghost panels visible
        if (settingsPanel != null) {
            setBrickPanelsVisible(true, settingsPanel);
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
     * Restarts the multiplayer game from the winning panel.
     * Clears game panels, resets score and level labels, clears display matrices,
     * reinitializes multiplayer panels, updates managers with current references,
     * restarts the game through GameStateManager, makes brick panels visible,
     * and renders initial bricks for both players.
     */
    public void restartMultiplayerGame() {
        // Clear all multiplayer game panels
        if (multiplayerScreen != null) {
            resetScoreAndLevelLabels();
            // Clear stored brick data
            multiplayerScreen.setCurrentBrickData(null, 1);
            multiplayerScreen.setCurrentBrickData(null, 2);
            
            // Clear display matrices before initializing new panels
            renderer.clearDisplayMatrix(multiplayerScreen.getDisplayMatrix(1));
            renderer.clearDisplayMatrix(multiplayerScreen.getDisplayMatrix(2));
            
            // Initialize new panels (this creates completely new panels)
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
        
        // Make brick panels and ghost panels visible (respecting ghost piece setting)
        // Do this before rendering to ensure panels are ready
        if (settingsPanel != null) {
            setBrickPanelsVisible(true, settingsPanel);
        }
        
        // Render initial bricks after restart (createNewGame doesn't call initGameView)
        // Use Platform.runLater to ensure UI is updated after panels are set visible
        javafx.application.Platform.runLater(() -> {
            if (multiplayerScreen != null) {
                if (gameController1 != null && gameController1.getBoard() instanceof com.comp2042.model.SimpleBoard) {
                    com.comp2042.model.SimpleBoard simpleBoard1 = (com.comp2042.model.SimpleBoard) gameController1.getBoard();
                    renderer.refreshBrick(
                        multiplayerScreen,
                        gameController1.getBoard().getViewData(),
                        1
                    );
                    renderer.refreshGameBackground(
                        multiplayerScreen,
                        gameController1.getBoard().getBoardMatrix(),
                        1
                    );
                    renderer.updateNextBricks(multiplayerScreen, simpleBoard1.getNextBricks(), 1);
                    renderer.updateHoldBrick(multiplayerScreen, simpleBoard1.getHeldBrick(), 1);
                }
                if (gameController2 != null && gameController2.getBoard() instanceof com.comp2042.model.SimpleBoard) {
                    com.comp2042.model.SimpleBoard simpleBoard2 = (com.comp2042.model.SimpleBoard) gameController2.getBoard();
                    renderer.refreshBrick(
                        multiplayerScreen,
                        gameController2.getBoard().getViewData(),
                        2
                    );
                    renderer.refreshGameBackground(
                        multiplayerScreen,
                        gameController2.getBoard().getBoardMatrix(),
                        2
                    );
                    renderer.updateNextBricks(multiplayerScreen, simpleBoard2.getNextBricks(), 2);
                    renderer.updateHoldBrick(multiplayerScreen, simpleBoard2.getHeldBrick(), 2);
                }
            }
        });
    }
    
    /**
     * Quits from multiplayer mode back to the main menu.
     * Clears all multiplayer game panels, resets labels, updates GameStateManager,
     * clears multiplayer references, hides multiplayer screen and panels,
     * removes multiplayer wrapper from center VBox, restores original panels,
     * reinitializes single player panels, shows main menu, and switches audio
     * from game music to main menu music.
     */
    public void quitToMainMenuFromMultiplayer() {
        // Clear all multiplayer game panels
        if (multiplayerScreen != null) {
            renderer.clearBrickPanels(multiplayerScreen);
            resetScoreAndLevelLabels();
            // Clear stored brick data
            multiplayerScreen.setCurrentBrickData(null, 1);
            multiplayerScreen.setCurrentBrickData(null, 2);
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
            gameStateManager.hideWinningPanel();
            multiplayerScreen.hidePausePanel();
            multiplayerScreen.hideSettingsOverlay();
        }
        
        // Remove multiplayer wrapper and container from center VBox
        if (centerVBox != null) {
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
     * Gets the root BorderPane from the scene using the supplier.
     * 
     * @return The root BorderPane, or null if the supplier is not set
     */
    private BorderPane getRootBorderPane() {
        if (rootBorderPaneSupplier != null) {
            return rootBorderPaneSupplier.get();
        }
        return null;
    }
    
    /**
     * Sets the callback for starting the game when both players are ready.
     * 
     * @param callback The Runnable to execute when both players are ready
     */
    public void setOnStartGameCallback(Runnable callback) {
        this.onStartGameCallback = callback;
    }
    
    // ========== Ready Panel Management ==========
    
    /**
     * Shows the ready panel for multiplayer game start.
     * Resets ready states, creates the ready panel UI if it doesn't exist,
     * creates the ready overlay, adds it to the wrapper, and updates ready labels.
     * The ready panel displays instructions for both players to press their
     * respective keys (SPACE for player 1, ENTER for player 2) to indicate readiness.
     */
    public void showReadyPanel() {
        if (multiplayerScreen == null) {
            return;
        }
        
        // Reset ready states
        multiplayerScreen.setPlayer1ReadyState(false);
        multiplayerScreen.setPlayer2ReadyState(false);
        
        // Create ready panel if it doesn't exist
        javafx.scene.layout.BorderPane readyPanel = multiplayerScreen.getReadyPanel();
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
            
            // Store the ready panel
            multiplayerScreen.setReadyPanel(readyPanel);
        }
        
        // Create ready overlay if it doesn't exist
        StackPane readyOverlay = multiplayerScreen.getReadyOverlay();
        if (readyOverlay == null) {
            readyOverlay = new StackPane();
            readyOverlay.setAlignment(javafx.geometry.Pos.CENTER);
            readyOverlay.setMaxWidth(Double.MAX_VALUE);
            readyOverlay.setMaxHeight(Double.MAX_VALUE);
            readyOverlay.setPickOnBounds(true);
            readyOverlay.setMouseTransparent(false);
            
            readyPanel.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
            readyOverlay.getChildren().add(readyPanel);
            
            // Store the ready overlay
            multiplayerScreen.setReadyOverlay(readyOverlay);
        }
        
        // Add ready overlay to wrapper if it exists
        StackPane wrapper = multiplayerScreen.getWrapper();
        if (wrapper != null) {
            if (!wrapper.getChildren().contains(readyOverlay)) {
                wrapper.getChildren().add(readyOverlay);
            }
            readyOverlay.setVisible(true);
            readyOverlay.setManaged(true);
        }
        
        // Update ready labels
        updateReadyLabels();
    }
    
    /**
     * Hides the ready panel overlay.
     * Makes the ready overlay invisible and unmanaged.
     */
    public void hideReadyPanel() {
        if (multiplayerScreen == null) {
            return;
        }
        
        StackPane readyOverlay = multiplayerScreen.getReadyOverlay();
        if (readyOverlay != null) {
            readyOverlay.setVisible(false);
            readyOverlay.setManaged(false);
        }
    }
    
    /**
     * Sets player 1's ready state and updates the UI.
     * 
     * @param ready True to mark player 1 as ready, false otherwise
     */
    public void setPlayer1Ready(boolean ready) {
        if (multiplayerScreen == null) {
            return;
        }
        
        multiplayerScreen.setPlayer1ReadyState(ready);
        updateReadyLabels();
        checkBothReady();
    }
    
    /**
     * Sets player 2's ready state and updates the UI.
     * 
     * @param ready True to mark player 2 as ready, false otherwise
     */
    public void setPlayer2Ready(boolean ready) {
        if (multiplayerScreen == null) {
            return;
        }
        
        multiplayerScreen.setPlayer2ReadyState(ready);
        updateReadyLabels();
        checkBothReady();
    }
    
    /**
     * Updates the ready labels UI to reflect current ready states.
     * Changes the text and styling of player 1 and player 2 ready labels
     * based on whether each player is ready or not.
     */
    public void updateReadyLabels() {
        if (multiplayerScreen == null) {
            return;
        }
        
        Label p1Label = multiplayerScreen.getP1ReadyIcon();
        Label p2Label = multiplayerScreen.getP2ReadyIcon();
        
        if (p1Label != null) {
            if (multiplayerScreen.isPlayer1Ready()) {
                p1Label.setText("READY");
                if (!p1Label.getStyleClass().contains("ready-confirmed")) {
                    p1Label.getStyleClass().add("ready-confirmed");
                }
            } else {
                p1Label.setText("Press SPACE to ready");
                p1Label.getStyleClass().remove("ready-confirmed");
            }
        }
        
        if (p2Label != null) {
            if (multiplayerScreen.isPlayer2Ready()) {
                p2Label.setText("READY");
                if (!p2Label.getStyleClass().contains("ready-confirmed")) {
                    p2Label.getStyleClass().add("ready-confirmed");
                }
            } else {
                p2Label.setText("Press ENTER to ready");
                p2Label.getStyleClass().remove("ready-confirmed");
            }
        }
    }
    
    /**
     * Checks if both players are ready and starts the game if so.
     * Hides the ready panel and invokes the start game callback when
     * both players have indicated they are ready.
     */
    public void checkBothReady() {
        if (multiplayerScreen == null) {
            return;
        }
        
        if (multiplayerScreen.isPlayer1Ready() && multiplayerScreen.isPlayer2Ready()) {
            hideReadyPanel();
            if (onStartGameCallback != null) {
                onStartGameCallback.run();
            }
        }
    }
    
    /**
     * Clears all multiplayer game panels (game panels, brick panels, ghost panels)
     * and side panels (hold, next, score, level) by setting all rectangles to transparent/empty state
     * and resetting labels to default values.
     */
    public void clearMultiplayerGamePanels() {
        if (multiplayerScreen == null) {
            return;
        }
        
        renderer.clearBrickPanels(multiplayerScreen);
        resetScoreAndLevelLabels();
        
        // Clear stored brick data
        multiplayerScreen.setCurrentBrickData(null, 1);
        multiplayerScreen.setCurrentBrickData(null, 2);
    }
    
    // Getters for game controllers
    /**
     * Gets the game controller for player 1.
     * 
     * @return The GameController for player 1, or null if not set
     */
    public GameController getGameController1() {
        return gameController1;
    }
    
    /**
     * Gets the game controller for player 2.
     * 
     * @return The GameController for player 2, or null if not set
     */
    public GameController getGameController2() {
        return gameController2;
    }
    
    // ========== Score and Level Binding ==========
    
    /**
     * Binds the score property to the score label for a player.
     * Unbinds any existing binding before applying the new one to avoid conflicts.
     * 
     * @param score The score property to bind to the label
     * @param playerNumber The player number (1 or 2) whose score label to bind
     */
    public void bindScore(javafx.beans.property.IntegerProperty score, int playerNumber) {
        if (multiplayerScreen == null) {
            return;
        }
        
        javafx.scene.control.Label label = multiplayerScreen.getScoreLabel(playerNumber);
        if (label != null && score != null) {
            label.textProperty().unbind();
            label.textProperty().bind(score.asString("%d"));
        }
    }
    
    /**
     * Binds the level property to the level label for a player.
     * Unbinds any existing binding and removes old listeners before applying
     * the new binding. Also sets up a level change listener to play level-up
     * sound effects when the level increases. Prevents duplicate listeners
     * by storing and removing old listeners.
     * 
     * @param level The level property to bind to the label
     * @param playerNumber The player number (1 or 2) whose level label to bind
     */
    public void bindLevel(javafx.beans.property.IntegerProperty level, int playerNumber) {
        if (multiplayerScreen == null) {
            return;
        }
        
        javafx.scene.control.Label label = multiplayerScreen.getLevelLabel(playerNumber);
        if (label != null && level != null) {
            label.textProperty().unbind();
            label.textProperty().bind(level.asString("%d"));
            
            // Remove old listener if it exists to prevent duplicate listeners
            javafx.beans.value.ChangeListener<? super Number> oldListener = 
                (playerNumber == 1) ? levelChangeListener1 : levelChangeListener2;
            if (oldListener != null) {
                level.removeListener(oldListener);
            }
            
            // Create and store new listener for level-up sound
            javafx.beans.value.ChangeListener<? super Number> newListener = (obs, oldVal, newVal) -> {
                int oldLevel = oldVal.intValue();
                int newLevel = newVal.intValue();
                // Play level-up sound only when level increases from a valid level (>= 1)
                // This prevents false positives from initialization/reset (where oldLevel might be 0)
                if (oldLevel >= 1 && newLevel > oldLevel && audioManager != null) {
                    audioManager.playLevelUp();
                }
            };
            
            // Store the listener
            if (playerNumber == 1) {
                levelChangeListener1 = newListener;
            } else if (playerNumber == 2) {
                levelChangeListener2 = newListener;
            }
            
            // Add the new listener
            level.addListener(newListener);
        }
    }
    
    /**
     * Sets the visibility of brick panels and ghost panels for both players.
     * For ghost panels, also checks the ghost piece setting from the settings
     * panel to determine if they should be shown even when visible is true.
     * 
     * @param visible Whether the panels should be visible (ghost panels also require ghost setting enabled)
     * @param settingsPanel The settings panel to check ghost piece visibility setting
     */
    public void setBrickPanelsVisible(boolean visible, SettingsPanel settingsPanel) {
        if (multiplayerScreen == null) {
            return;
        }
        
        javafx.scene.layout.GridPane brickPanel1 = multiplayerScreen.getBrickPanel(1);
        javafx.scene.layout.GridPane brickPanel2 = multiplayerScreen.getBrickPanel(2);
        javafx.scene.layout.GridPane ghostPanel1 = multiplayerScreen.getGhostPanel(1);
        javafx.scene.layout.GridPane ghostPanel2 = multiplayerScreen.getGhostPanel(2);
        
        if (brickPanel1 != null) {
            brickPanel1.setVisible(visible);
            brickPanel1.setManaged(visible);
        }
        if (brickPanel2 != null) {
            brickPanel2.setVisible(visible);
            brickPanel2.setManaged(visible);
        }
        
        if (ghostPanel1 != null && settingsPanel != null) {
            javafx.scene.control.CheckBox ghostCheckBox = settingsPanel.getGhostPieceCheckBox();
            boolean showGhost = ghostCheckBox != null && ghostCheckBox.isSelected();
            boolean ghostVisible = showGhost && visible;
            ghostPanel1.setVisible(ghostVisible);
            ghostPanel1.setManaged(ghostVisible);
        }
        if (ghostPanel2 != null && settingsPanel != null) {
            javafx.scene.control.CheckBox ghostCheckBox = settingsPanel.getGhostPieceCheckBox();
            boolean showGhost = ghostCheckBox != null && ghostCheckBox.isSelected();
            boolean ghostVisible = showGhost && visible;
            ghostPanel2.setVisible(ghostVisible);
            ghostPanel2.setManaged(ghostVisible);
        }
    }
    
    /**
     * Resets score and level labels to default values for both players.
     * Unbinds any existing bindings before setting default text values.
     * Used when clearing game panels or restarting games.
     */
    private void resetScoreAndLevelLabels() {
        if (multiplayerScreen == null) {
            return;
        }
        
        // Reset score labels
        javafx.scene.control.Label scoreLabel1 = multiplayerScreen.getScoreLabel(1);
        if (scoreLabel1 != null) {
            if (scoreLabel1.textProperty().isBound()) {
                scoreLabel1.textProperty().unbind();
            }
            scoreLabel1.setText("0");
        }
        
        javafx.scene.control.Label scoreLabel2 = multiplayerScreen.getScoreLabel(2);
        if (scoreLabel2 != null) {
            if (scoreLabel2.textProperty().isBound()) {
                scoreLabel2.textProperty().unbind();
            }
            scoreLabel2.setText("0");
        }
        
        // Reset level labels
        javafx.scene.control.Label levelLabel1 = multiplayerScreen.getLevelLabel(1);
        if (levelLabel1 != null) {
            if (levelLabel1.textProperty().isBound()) {
                levelLabel1.textProperty().unbind();
            }
            levelLabel1.setText("1");
        }
        
        javafx.scene.control.Label levelLabel2 = multiplayerScreen.getLevelLabel(2);
        if (levelLabel2 != null) {
            if (levelLabel2.textProperty().isBound()) {
                levelLabel2.textProperty().unbind();
            }
            levelLabel2.setText("1");
        }
    }
}

