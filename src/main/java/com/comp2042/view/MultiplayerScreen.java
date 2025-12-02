package com.comp2042.view;

import com.comp2042.controller.GameConstants;
import com.comp2042.controller.GameController;
import com.comp2042.event.InputEventListener;
import com.comp2042.model.ViewData;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages the multiplayer game screen UI components and rendering.
 * This class handles the complete UI setup for two-player Tetris gameplay, including
 * side-by-side game boards, score/level displays, next brick previews, hold brick displays,
 * ghost piece rendering, pause/winning/settings overlays, and ready state management.
 * The screen uses a scaled-down layout (85% scale) to fit both players' boards side-by-side
 * with a VS column in the middle showing the timer. It manages all UI components for both
 * players including game panels, brick panels, ghost panels, side panels with next bricks
 * and hold bricks, and overlay panels for pause, settings, winning, and ready states.
 * The class coordinates with GameController, InputEventListener, and various manager classes
 * to provide a complete multiplayer gaming experience with synchronized display updates.
 */
public class MultiplayerScreen {
    
    // Constants
    private static final int BRICK_SIZE = GameConstants.BRICK_SIZE;
    private static final int GRID_GAP = GameConstants.GRID_GAP;
    private static final int BOARD_WIDTH = GameConstants.BOARD_WIDTH;
    private static final int BOARD_HEIGHT = GameConstants.BOARD_HEIGHT;
    private static final int GAME_PANEL_WIDTH = GameConstants.GAME_PANEL_WIDTH;
    private static final int GAME_PANEL_HEIGHT = GameConstants.GAME_PANEL_HEIGHT;
    
    // Main container
    private HBox multiplayerContainer;
    private StackPane multiplayerWrapper;
    private StackPane multiplayerPauseOverlay;
    private StackPane multiplayerSettingsOverlay;
    private StackPane multiplayerReadyOverlay;
    private StackPane multiplayerWinningOverlay;
    
    // Panels
    private PausePanel multiplayerPausePanel;
    private BorderPane readyPanel;
    private WinningPanel multiplayerWinningPanel;
    
    // Ready states
    private boolean player1Ready = false;
    private boolean player2Ready = false;
    
    // Game panels for both players
    private GridPane gamePanel1, gamePanel2;
    private GridPane brickPanel1, brickPanel2;
    private GridPane ghostPanel1, ghostPanel2;
    private StackPane gameStack1, gameStack2;
    private Rectangle[][] displayMatrix1, displayMatrix2;
    
    // Side panels
    private VBox leftPanelPlayer1, rightPanelPlayer2;
    private GridPane holdBrickPanel1, holdBrickPanel2;
    private VBox nextBricksPanel1, nextBricksPanel2;
    private Rectangle[][] holdBrickRectangles1, holdBrickRectangles2;
    private List<GridPane> nextBrickPanes1 = new ArrayList<>();
    private List<GridPane> nextBrickPanes2 = new ArrayList<>();
    private Label scoreLabel1, scoreLabel2;
    private Label levelLabel1, levelLabel2;
    private Label multiplayerTimerLabel;
    
    // Callbacks for interaction with GuiController
    private Consumer<Runnable> onStartGame;
    private Consumer<Runnable> onRestartGame;
    private Consumer<Runnable> onQuitToMenu;
    private Consumer<Runnable> onResumeGame;
    private Consumer<Runnable> onShowSettings;
    private Runnable onUpdateReadyLabels;
    private Runnable onCheckBothReady;
    private Consumer<Boolean> onSetPlayer1Ready;
    private Consumer<Boolean> onSetPlayer2Ready;
    private Consumer<Parent> onGetRootBorderPane;
    private Consumer<SettingsPanel> onSetSettingsPanel;
    
    // Pause panel action handler (optional, for direct setup)
    private com.comp2042.controller.PausePanelActionHandler pausePanelActionHandler;
    
    // Game controllers and event listeners (needed for UI updates)
    private GameController gameController1, gameController2;
    private InputEventListener eventListener1, eventListener2;
    private ViewData currentBrickData1, currentBrickData2;
    
    /**
     * Creates a new MultiplayerScreen and initializes all UI panels.
     * Sets up the main container, wrapper, overlays, and game panels for both players.
     */
    public MultiplayerScreen() {
        initializePanels();
    }
    
    /**
     * Sets all callback functions for interaction with GuiController.
     * Configures callbacks for game control actions (start, restart, quit, resume, settings),
     * ready state management, and UI coordination. After setting callbacks, re-configures
     * the pause panel actions if a pause panel action handler is available.
     * 
     * @param onStartGame Callback for starting the game
     * @param onRestartGame Callback for restarting the game
     * @param onQuitToMenu Callback for quitting to main menu
     * @param onResumeGame Callback for resuming the game
     * @param onShowSettings Callback for showing settings
     * @param onUpdateReadyLabels Callback for updating ready state labels
     * @param onCheckBothReady Callback for checking if both players are ready
     * @param onSetPlayer1Ready Callback for setting player 1 ready state
     * @param onSetPlayer2Ready Callback for setting player 2 ready state
     * @param onGetRootBorderPane Callback for getting the root border pane
     * @param onSetSettingsPanel Callback for setting the settings panel
     */
    public void setCallbacks(
            Consumer<Runnable> onStartGame,
            Consumer<Runnable> onRestartGame,
            Consumer<Runnable> onQuitToMenu,
            Consumer<Runnable> onResumeGame,
            Consumer<Runnable> onShowSettings,
            Runnable onUpdateReadyLabels,
            Runnable onCheckBothReady,
            Consumer<Boolean> onSetPlayer1Ready,
            Consumer<Boolean> onSetPlayer2Ready,
            Consumer<Parent> onGetRootBorderPane,
            Consumer<SettingsPanel> onSetSettingsPanel) {
        this.onStartGame = onStartGame;
        this.onRestartGame = onRestartGame;
        this.onQuitToMenu = onQuitToMenu;
        this.onResumeGame = onResumeGame;
        this.onShowSettings = onShowSettings;
        this.onUpdateReadyLabels = onUpdateReadyLabels;
        this.onCheckBothReady = onCheckBothReady;
        this.onSetPlayer1Ready = onSetPlayer1Ready;
        this.onSetPlayer2Ready = onSetPlayer2Ready;
        this.onGetRootBorderPane = onGetRootBorderPane;
        this.onSetSettingsPanel = onSetSettingsPanel;
        
        // Re-setup pause panel actions now that callbacks are set
        // If pausePanelActionHandler is set, use it; otherwise use callback-based setup
        if (pausePanelActionHandler != null && multiplayerPausePanel != null) {
            pausePanelActionHandler.setupMultiplayerPausePanelActions(multiplayerPausePanel);
        } else {
            setupPausePanelActions();
        }
    }
    
    /**
     * Sets the game controllers for both players.
     * 
     * @param gameController1 The GameController for player 1
     * @param gameController2 The GameController for player 2
     */
    public void setGameControllers(GameController gameController1, GameController gameController2) {
        this.gameController1 = gameController1;
        this.gameController2 = gameController2;
    }
    
    /**
     * Sets the event listeners for both players.
     * 
     * @param eventListener1 The InputEventListener for player 1
     * @param eventListener2 The InputEventListener for player 2
     */
    public void setEventListeners(InputEventListener eventListener1, InputEventListener eventListener2) {
        this.eventListener1 = eventListener1;
        this.eventListener2 = eventListener2;
    }
    
    /**
     * Gets the main multiplayer container HBox.
     * 
     * @return The HBox container holding both players' game panels
     */
    public HBox getContainer() {
        return multiplayerContainer;
    }
    
    /**
     * Gets the game panel (background grid) for the specified player.
     * 
     * @param playerNumber The player number (1 or 2)
     * @return The GridPane representing the game board background for the player
     */
    public GridPane getGamePanel(int playerNumber) {
        return (playerNumber == 1) ? gamePanel1 : gamePanel2;
    }
    
    /**
     * Gets the brick panel for the specified player.
     * 
     * @param playerNumber The player number (1 or 2)
     * @return The GridPane for rendering the current falling brick
     */
    public GridPane getBrickPanel(int playerNumber) {
        return (playerNumber == 1) ? brickPanel1 : brickPanel2;
    }
    
    /**
     * Gets the ghost panel for the specified player.
     * 
     * @param playerNumber The player number (1 or 2)
     * @return The GridPane for rendering the ghost piece (landing position preview)
     */
    public GridPane getGhostPanel(int playerNumber) {
        return (playerNumber == 1) ? ghostPanel1 : ghostPanel2;
    }
    
    /**
     * Gets the display matrix for the specified player.
     * 
     * @param playerNumber The player number (1 or 2)
     * @return The 2D array of Rectangle objects representing the game board cells
     */
    public Rectangle[][] getDisplayMatrix(int playerNumber) {
        return (playerNumber == 1) ? displayMatrix1 : displayMatrix2;
    }
    
    /**
     * Gets the score label for the specified player.
     * 
     * @param playerNumber The player number (1 or 2)
     * @return The Label displaying the player's score
     */
    public Label getScoreLabel(int playerNumber) {
        return (playerNumber == 1) ? scoreLabel1 : scoreLabel2;
    }
    
    /**
     * Gets the level label for the specified player.
     * 
     * @param playerNumber The player number (1 or 2)
     * @return The Label displaying the player's level
     */
    public Label getLevelLabel(int playerNumber) {
        return (playerNumber == 1) ? levelLabel1 : levelLabel2;
    }
    
    /**
     * Gets the hold brick panel for the specified player.
     * 
     * @param playerNumber The player number (1 or 2)
     * @return The GridPane for displaying the held brick
     */
    public GridPane getHoldBrickPanel(int playerNumber) {
        return (playerNumber == 1) ? holdBrickPanel1 : holdBrickPanel2;
    }
    
    /**
     * Gets the next bricks panel container for the specified player.
     * 
     * @param playerNumber The player number (1 or 2)
     * @return The VBox container holding the next brick preview panes
     */
    public VBox getNextBricksPanel(int playerNumber) {
        return (playerNumber == 1) ? nextBricksPanel1 : nextBricksPanel2;
    }
    
    /**
     * Gets the hold brick rectangles array for the specified player.
     * 
     * @param playerNumber The player number (1 or 2)
     * @return The 2D array of Rectangle objects for rendering the held brick
     */
    public Rectangle[][] getHoldBrickRectangles(int playerNumber) {
        return (playerNumber == 1) ? holdBrickRectangles1 : holdBrickRectangles2;
    }
    
    /**
     * Gets the list of next brick preview panes for the specified player.
     * 
     * @param playerNumber The player number (1 or 2)
     * @return The list of GridPane objects for displaying upcoming bricks
     */
    public List<GridPane> getNextBrickPanes(int playerNumber) {
        return (playerNumber == 1) ? nextBrickPanes1 : nextBrickPanes2;
    }
    
    /**
     * Gets the game panel grid for player 1 (renderer access).
     * 
     * @return The GridPane for player 1's game board background
     */
    public GridPane getP1Grid() {
        return gamePanel1;
    }
    
    /**
     * Gets the game panel grid for player 2 (renderer access).
     * 
     * @return The GridPane for player 2's game board background
     */
    public GridPane getP2Grid() {
        return gamePanel2;
    }
    
    /**
     * Gets the ghost panel grid for player 1 (renderer access).
     * 
     * @return The GridPane for player 1's ghost piece
     */
    public GridPane getP1GhostGrid() {
        return ghostPanel1;
    }
    
    /**
     * Gets the ghost panel grid for player 2 (renderer access).
     * 
     * @return The GridPane for player 2's ghost piece
     */
    public GridPane getP2GhostGrid() {
        return ghostPanel2;
    }
    
    /**
     * Gets the event listener for the specified player.
     * 
     * @param playerNumber The player number (1 or 2)
     * @return The InputEventListener for the player
     */
    public InputEventListener getEventListener(int playerNumber) {
        return (playerNumber == 1) ? eventListener1 : eventListener2;
    }
    
    /**
     * Sets the current brick data for the specified player.
     * 
     * @param brick The ViewData containing the current brick information
     * @param playerNumber The player number (1 or 2)
     */
    public void setCurrentBrickData(ViewData brick, int playerNumber) {
        if (playerNumber == 1) {
            currentBrickData1 = brick;
        } else if (playerNumber == 2) {
            currentBrickData2 = brick;
        }
    }
    
    /**
     * Sets the hold brick rectangles array for the specified player.
     * 
     * @param rectangles The 2D array of Rectangle objects for rendering the held brick
     * @param playerNumber The player number (1 or 2)
     */
    public void setHoldBrickRectangles(Rectangle[][] rectangles, int playerNumber) {
        if (playerNumber == 1) {
            holdBrickRectangles1 = rectangles;
        } else if (playerNumber == 2) {
            holdBrickRectangles2 = rectangles;
        }
    }
    
    /**
     * Sets the list of next brick preview panes for the specified player.
     * 
     * @param panes The list of GridPane objects for displaying upcoming bricks
     * @param playerNumber The player number (1 or 2)
     */
    public void setNextBrickPanes(List<GridPane> panes, int playerNumber) {
        if (playerNumber == 1) {
            nextBrickPanes1 = panes;
        } else if (playerNumber == 2) {
            nextBrickPanes2 = panes;
        }
    }
    
    /**
     * Gets the ready panel BorderPane.
     * 
     * @return The BorderPane displaying the ready state UI
     */
    public BorderPane getReadyPanel() {
        return readyPanel;
    }
    
    /**
     * Sets the ready panel BorderPane.
     * 
     * @param panel The BorderPane to use for the ready state UI
     */
    public void setReadyPanel(BorderPane panel) {
        this.readyPanel = panel;
    }
    
    /**
     * Gets the ready overlay StackPane.
     * 
     * @return The StackPane overlay for the ready panel
     */
    public StackPane getReadyOverlay() {
        return multiplayerReadyOverlay;
    }
    
    /**
     * Sets the ready overlay StackPane.
     * 
     * @param overlay The StackPane overlay for the ready panel
     */
    public void setReadyOverlay(StackPane overlay) {
        this.multiplayerReadyOverlay = overlay;
    }
    
    /**
     * Gets the main wrapper StackPane containing all multiplayer UI components.
     * 
     * @return The StackPane wrapper for the multiplayer screen
     */
    public StackPane getWrapper() {
        return multiplayerWrapper;
    }
    
    /**
     * Gets the winning panel for displaying game winner.
     * 
     * @return The WinningPanel instance
     */
    public WinningPanel getWinningPanel() {
        return multiplayerWinningPanel;
    }
    
    /**
     * Gets the winning overlay StackPane.
     * 
     * @return The StackPane overlay for the winning panel
     */
    public StackPane getWinningOverlay() {
        return multiplayerWinningOverlay;
    }
    
    /**
     * Gets the ready icon label for player 1.
     * Searches through the ready panel structure to find the label with ID "player1ReadyLabel".
     * 
     * @return The Label displaying player 1's ready state, or null if not found
     */
    public Label getP1ReadyIcon() {
        if (readyPanel == null) return null;
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
                                        return label;
                                            }
                                        }
                                            }
                                        }
                                    }
                                }
                            }
                        }
        return null;
    }
    
    /**
     * Gets the ready icon label for player 2.
     * Searches through the ready panel structure to find the label with ID "player2ReadyLabel".
     * 
     * @return The Label displaying player 2's ready state, or null if not found
     */
    public Label getP2ReadyIcon() {
        if (readyPanel == null) return null;
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
                                    if ("player2ReadyLabel".equals(label.getId())) {
                                        return label;
                                    }
                                }
                            }
                                        }
                                    }
                                }
                            }
                        }
        return null;
    }
    
    /**
     * Checks if player 1 is ready.
     * 
     * @return true if player 1 is ready, false otherwise
     */
    public boolean isPlayer1Ready() {
        return player1Ready;
    }
    
    /**
     * Checks if player 2 is ready.
     * 
     * @return true if player 2 is ready, false otherwise
     */
    public boolean isPlayer2Ready() {
        return player2Ready;
    }
    
    /**
     * Sets the ready state for player 1.
     * 
     * @param ready true if player 1 is ready, false otherwise
     */
    public void setPlayer1ReadyState(boolean ready) {
        player1Ready = ready;
    }
    
    /**
     * Sets the ready state for player 2.
     * 
     * @param ready true if player 2 is ready, false otherwise
     */
    public void setPlayer2ReadyState(boolean ready) {
        player2Ready = ready;
    }
    
    /**
     * Shows the multiplayer screen by making the container and wrapper visible.
     */
    public void show() {
            if (multiplayerContainer != null) {
            multiplayerContainer.setVisible(true);
                multiplayerContainer.setManaged(true);
            }
            if (multiplayerWrapper != null) {
            multiplayerWrapper.setVisible(true);
                multiplayerWrapper.setManaged(true);
            }
    }
    
    /**
     * Hides the multiplayer screen by making the container and wrapper invisible.
     */
    public void hide() {
        if (multiplayerContainer != null) {
            multiplayerContainer.setVisible(false);
            multiplayerContainer.setManaged(false);
        }
        if (multiplayerWrapper != null) {
            multiplayerWrapper.setVisible(false);
            multiplayerWrapper.setManaged(false);
        }
    }
    
    
    /**
     * Shows the pause panel overlay.
     * Makes the pause overlay visible and enables mouse interaction.
     */
    public void showPausePanel() {
        if (multiplayerPauseOverlay != null && multiplayerPausePanel != null) {
            multiplayerPauseOverlay.setVisible(true);
            multiplayerPauseOverlay.setManaged(true);
            multiplayerPauseOverlay.setMouseTransparent(false);
        }
    }
    
    /**
     * Hides the pause panel overlay.
     * Makes the pause overlay invisible and disables mouse interaction.
     */
    public void hidePausePanel() {
        if (multiplayerPauseOverlay != null) {
            multiplayerPauseOverlay.setVisible(false);
            multiplayerPauseOverlay.setManaged(false);
            multiplayerPauseOverlay.setMouseTransparent(true);
        }
    }
    
    
    /**
     * Shows the settings panel overlay.
     * Adds the settings panel to the overlay if not already present, configures
     * its size constraints, makes it visible, and brings it to the front.
     * 
     * @param settingsPanel The SettingsPanel to display in the overlay
     */
    public void showSettingsOverlay(SettingsPanel settingsPanel) {
        if (multiplayerSettingsOverlay != null && settingsPanel != null) {
            if (!multiplayerSettingsOverlay.getChildren().contains(settingsPanel)) {
                multiplayerSettingsOverlay.getChildren().add(settingsPanel);
            }
            // Constrain settings panel to preferred size (same as pause panel)
            settingsPanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            // Ensure settings panel is visible and managed
            settingsPanel.setVisible(true);
            settingsPanel.setManaged(true);
            multiplayerSettingsOverlay.setVisible(true);
            multiplayerSettingsOverlay.setManaged(true);
            multiplayerSettingsOverlay.setMouseTransparent(false);
            
            // Bring settings overlay to front to ensure it's on top
            if (multiplayerWrapper != null) {
                if (multiplayerWrapper.getChildren().contains(multiplayerSettingsOverlay)) {
                    multiplayerWrapper.getChildren().remove(multiplayerSettingsOverlay);
                }
                multiplayerWrapper.getChildren().add(multiplayerSettingsOverlay);
            }
        }
    }
    
    /**
     * Hides the settings panel overlay.
     * Makes the settings overlay invisible and disables mouse interaction.
     */
    public void hideSettingsOverlay() {
        if (multiplayerSettingsOverlay != null) {
            multiplayerSettingsOverlay.setVisible(false);
            multiplayerSettingsOverlay.setManaged(false);
            multiplayerSettingsOverlay.setMouseTransparent(true);
        }
    }
    
    
    
    private void initializePanels() {
        double scale = 0.85;
        
        // Create main container
        multiplayerContainer = new HBox(30);
        multiplayerContainer.getStyleClass().add("multiplayer-container");
        multiplayerContainer.setAlignment(Pos.CENTER);
        multiplayerContainer.setMaxWidth(Double.MAX_VALUE);
        multiplayerContainer.setMaxHeight(Double.MAX_VALUE);
        multiplayerContainer.setManaged(false);
        multiplayerContainer.setVisible(false);
        
        // Create wrapper
        multiplayerWrapper = new StackPane();
        multiplayerWrapper.setAlignment(Pos.CENTER);
        multiplayerWrapper.setMaxWidth(Double.MAX_VALUE);
        multiplayerWrapper.setMaxHeight(Double.MAX_VALUE);
        multiplayerWrapper.getChildren().add(multiplayerContainer);
        
        // Create pause overlay
        multiplayerPauseOverlay = new StackPane();
        multiplayerPauseOverlay.setAlignment(Pos.CENTER);
        multiplayerPauseOverlay.setMaxWidth(Double.MAX_VALUE);
        multiplayerPauseOverlay.setMaxHeight(Double.MAX_VALUE);
        multiplayerPauseOverlay.setPickOnBounds(true);
        multiplayerPauseOverlay.setMouseTransparent(false);
        
        multiplayerPausePanel = new PausePanel();
        // Setup will be done by setPausePanelActionHandler or setupPausePanelActions
        if (pausePanelActionHandler != null) {
            pausePanelActionHandler.setupMultiplayerPausePanelActions(multiplayerPausePanel);
        } else {
            setupPausePanelActions();
        }
        multiplayerPausePanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        multiplayerPauseOverlay.getChildren().add(multiplayerPausePanel);
        multiplayerPauseOverlay.setVisible(false);
        multiplayerPauseOverlay.setManaged(false);
        multiplayerWrapper.getChildren().add(multiplayerPauseOverlay);
        
        // Create settings overlay
        multiplayerSettingsOverlay = new StackPane();
        multiplayerSettingsOverlay.setAlignment(Pos.CENTER);
        multiplayerSettingsOverlay.setMaxWidth(Double.MAX_VALUE);
        multiplayerSettingsOverlay.setMaxHeight(Double.MAX_VALUE);
        multiplayerSettingsOverlay.setPickOnBounds(true);
        multiplayerSettingsOverlay.setMouseTransparent(false);
        multiplayerSettingsOverlay.setVisible(false);
        multiplayerSettingsOverlay.setManaged(false);
        multiplayerWrapper.getChildren().add(multiplayerSettingsOverlay);
        
        // Create winning overlay
        multiplayerWinningOverlay = new StackPane();
        multiplayerWinningOverlay.setAlignment(Pos.CENTER);
        multiplayerWinningOverlay.setMaxWidth(Double.MAX_VALUE);
        multiplayerWinningOverlay.setMaxHeight(Double.MAX_VALUE);
        multiplayerWinningOverlay.setPickOnBounds(true);
        multiplayerWinningOverlay.setMouseTransparent(false);
        multiplayerWinningOverlay.setManaged(false);
        
        multiplayerWinningPanel = new WinningPanel();
        multiplayerWinningPanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        multiplayerWinningOverlay.getChildren().add(multiplayerWinningPanel);
        multiplayerWinningOverlay.setVisible(false);
        multiplayerWrapper.getChildren().add(multiplayerWinningOverlay);
        
        // Initialize game panels
        initializeMultiplayerPanels();
    }
    
    /**
     * Initializes or re-initializes the multiplayer game panels.
     * Creates scaled-down game panels for both players (85% scale), side panels
     * with next bricks and hold bricks, and a VS column with timer in the middle.
     * Clears any existing panels before creating new ones.
     */
    public void initializeMultiplayerPanels() {
        double scale = 0.85;
        int scaledBrickSize = (int)(BRICK_SIZE * scale);
        int scaledPanelWidth = (int)(GAME_PANEL_WIDTH * scale);
        int scaledPanelHeight = (int)(GAME_PANEL_HEIGHT * scale);
        
        // Clear existing container
        if (multiplayerContainer != null) {
            multiplayerContainer.getChildren().clear();
        }
        
        // Create Player 1 container
        VBox player1Container = createPlayerContainer(1, scaledBrickSize, scaledPanelWidth, scaledPanelHeight, scale);
        
        // Create VS column
        VBox vsColumn = new VBox(10);
        vsColumn.setAlignment(Pos.CENTER);
        
        // Create or reuse timer label
        // Note: Timer text initialization is handled by TimerManager when label is registered
        if (multiplayerTimerLabel == null) {
            multiplayerTimerLabel = new Label();
            multiplayerTimerLabel.getStyleClass().add("multiplayer-timer-label");
            multiplayerTimerLabel.setAlignment(Pos.CENTER);
            multiplayerTimerLabel.setMaxWidth(Double.MAX_VALUE);
        } else {
            // If label already exists, remove it from its current parent before reusing
            javafx.scene.Parent parent = multiplayerTimerLabel.getParent();
            if (parent != null && parent instanceof javafx.scene.layout.Pane) {
                ((javafx.scene.layout.Pane) parent).getChildren().remove(multiplayerTimerLabel);
            }
        }
        
        Label vsLabel = new Label("VS");
        vsLabel.getStyleClass().add("vs-label");
        
        vsColumn.getChildren().addAll(multiplayerTimerLabel, vsLabel);
        
        // Create Player 2 container
        VBox player2Container = createPlayerContainer(2, scaledBrickSize, scaledPanelWidth, scaledPanelHeight, scale);
        
        multiplayerContainer.getChildren().addAll(player1Container, vsColumn, player2Container);
    }
    
    private VBox createPlayerContainer(int playerNumber, int brickSize, int panelWidth, int panelHeight, double scale) {
        VBox playerContainer = new VBox(10);
        playerContainer.setAlignment(Pos.CENTER);
        playerContainer.getStyleClass().add("player-container");
        
        Label playerLabel = new Label("PLAYER " + playerNumber);
        playerLabel.getStyleClass().add("player-title");
        playerLabel.setAlignment(Pos.CENTER);
        playerLabel.setMaxWidth(Double.MAX_VALUE);
        
        HBox contentContainer = new HBox(15);
        contentContainer.setAlignment(Pos.CENTER);
        
        VBox sidePanel = createPlayerSidePanel(playerNumber, panelHeight, scale);
        VBox gameField = createPlayerGameField(playerNumber, brickSize, panelWidth, panelHeight);
        
        if (playerNumber == 1) {
            contentContainer.getChildren().addAll(sidePanel, gameField);
            leftPanelPlayer1 = sidePanel;
        } else {
            contentContainer.getChildren().addAll(gameField, sidePanel);
            rightPanelPlayer2 = sidePanel;
        }
        
        playerContainer.getChildren().addAll(playerLabel, contentContainer);
        return playerContainer;
    }
    
    private VBox createPlayerSidePanel(int playerNumber, int targetHeight, double scale) {
        VBox playerPanel = new VBox((int)(20 * scale));
        playerPanel.getStyleClass().add("side-panel");
        int sidePanelWidth = (int)(150 * scale);
        playerPanel.setPrefWidth(sidePanelWidth);
        playerPanel.setMaxWidth(sidePanelWidth);
        
        // Hold brick panel
        VBox holdBox = new VBox();
        holdBox.getStyleClass().add("info-box");
        Label holdLabel = new Label("HOLD");
        holdLabel.getStyleClass().add("panel-title");
        
        GridPane holdPanel = new GridPane();
        holdPanel.setVgap(1);
        holdPanel.setHgap(1);
        holdPanel.getStyleClass().add("brick-preview");
        int holdPanelSize = (int)(100 * scale);
        holdPanel.setPrefWidth(holdPanelSize);
        holdPanel.setPrefHeight(holdPanelSize);
        holdPanel.setMaxWidth(holdPanelSize);
        holdPanel.setMaxHeight(holdPanelSize);
        
        Rectangle[][] holdRectangles = new Rectangle[4][4];
        int brickSize = (int)((BRICK_SIZE - 10) * scale);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Rectangle rect = new Rectangle(brickSize, brickSize);
                rect.setFill(Color.TRANSPARENT);
                rect.setStroke(Color.gray(0.3));
                holdRectangles[i][j] = rect;
                holdPanel.add(rect, j, i);
            }
        }
        
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
        
        if (playerNumber == 1) {
            levelLabel1 = levelValueLabel;
        } else {
            levelLabel2 = levelValueLabel;
        }
        
        levelBox.getChildren().addAll(levelTitleLabel, levelValueLabel);
        
        // Next bricks panel
        VBox nextBox = new VBox();
        nextBox.getStyleClass().add("info-box");
        Label nextLabel = new Label("NEXT");
        nextLabel.getStyleClass().add("panel-title");
        
        VBox nextBricksContainer = new VBox((int)(10 * scale));
        nextBricksContainer.getStyleClass().add("next-bricks-panel");
        int nextBrickSize = (int)(80 * scale);
        int nextBricksWidth = (int)(120 * scale);
        int nextBricksHeight = nextBrickSize;
        
        nextBricksContainer.setPrefWidth(nextBricksWidth);
        nextBricksContainer.setPrefHeight(nextBricksHeight);
        nextBricksContainer.setMaxWidth(nextBricksWidth);
        nextBricksContainer.setMaxHeight(nextBricksHeight);
        
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
        
        if (playerNumber == 1) {
            nextBricksPanel1 = nextBricksContainer;
            nextBrickPanes1 = nextPanes;
        } else {
            nextBricksPanel2 = nextBricksContainer;
            nextBrickPanes2 = nextPanes;
        }
        
        nextBox.getChildren().addAll(nextLabel, nextBricksContainer);
        
        playerPanel.getChildren().addAll(holdBox, scoreBox, levelBox, nextBox);
        return playerPanel;
    }
    
    private VBox createPlayerGameField(int playerNumber, int brickSize, int panelWidth, int panelHeight) {
        VBox gameFieldContainer = new VBox();
        gameFieldContainer.setAlignment(Pos.CENTER);
        gameFieldContainer.getStyleClass().add("player-field");
        
        BorderPane gameFieldBoard = new BorderPane();
        gameFieldBoard.getStyleClass().add("gameBoard");
        
        int borderOffset = 4;
        int gridHeight = panelHeight + 1;
        int totalHeight = gridHeight;
        gameFieldBoard.setPrefHeight(totalHeight);
        gameFieldBoard.setMaxHeight(totalHeight);
        gameFieldBoard.setMinHeight(totalHeight);
        gameFieldBoard.setPrefWidth(panelWidth + (borderOffset * 2));
        gameFieldBoard.setMaxWidth(panelWidth + (borderOffset * 2));
        gameFieldBoard.setMinWidth(panelWidth + (borderOffset * 2));
        
        Pane borderWrapper = new Pane();
        borderWrapper.setPrefSize(panelWidth + (borderOffset * 2), totalHeight);
        borderWrapper.setMaxSize(panelWidth + (borderOffset * 2), totalHeight);
        borderWrapper.setMinSize(panelWidth + (borderOffset * 2), totalHeight);
        borderWrapper.setPadding(javafx.geometry.Insets.EMPTY);
        
        StackPane gameFieldStack = new StackPane();
        gameFieldStack.setPrefSize(panelWidth, panelHeight + 1);
        gameFieldStack.setMaxSize(panelWidth, panelHeight + 1);
        gameFieldStack.setMinSize(panelWidth, panelHeight + 1);
        gameFieldStack.setPadding(javafx.geometry.Insets.EMPTY);
        gameFieldStack.setAlignment(Pos.TOP_LEFT);
        
        gameFieldStack.setLayoutX(borderOffset);
        gameFieldStack.setLayoutY(borderOffset);
        borderWrapper.setSnapToPixel(true);
        gameFieldStack.setSnapToPixel(true);
        
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
        
        GridPane gameFieldPanel = new GridPane();
        gameFieldPanel.setHgap(GRID_GAP);
        gameFieldPanel.setVgap(GRID_GAP);
        gameFieldPanel.setPrefSize(panelWidth, panelHeight + 1);
        gameFieldPanel.setMaxSize(panelWidth, panelHeight + 1);
        gameFieldPanel.setMinSize(panelWidth, panelHeight + 1);
        initializeGameFieldPanel(gameFieldPanel, brickSize, matrix);
        
        GridPane gameFieldBrickPanel = new GridPane();
        gameFieldBrickPanel.setHgap(GRID_GAP);
        gameFieldBrickPanel.setVgap(GRID_GAP);
        gameFieldBrickPanel.setPrefSize(panelWidth, panelHeight + 1);
        gameFieldBrickPanel.setMaxSize(panelWidth, panelHeight + 1);
        gameFieldBrickPanel.setMinSize(panelWidth, panelHeight + 1);
        gameFieldBrickPanel.setMouseTransparent(true);
        initializeBrickPanel(gameFieldBrickPanel, brickSize);
        
        GridPane gameFieldGhostPanel = new GridPane();
        gameFieldGhostPanel.setHgap(GRID_GAP);
        gameFieldGhostPanel.setVgap(GRID_GAP);
        gameFieldGhostPanel.setPrefSize(panelWidth, panelHeight + 1);
        gameFieldGhostPanel.setMaxSize(panelWidth, panelHeight + 1);
        gameFieldGhostPanel.setMinSize(panelWidth, panelHeight + 1);
        gameFieldGhostPanel.setMouseTransparent(true);
        initializeBrickPanel(gameFieldGhostPanel, brickSize);
        
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
        
        gameFieldStack.getChildren().addAll(gameFieldPanel, gameFieldGhostPanel, gameFieldBrickPanel);
        
        borderWrapper.getChildren().add(gameFieldStack);
        gameFieldBoard.setCenter(borderWrapper);
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
    
    private void setupPausePanelActions() {
        if (multiplayerPausePanel == null) return;
        
        multiplayerPausePanel.setOnResumeAction(() -> {
            if (onResumeGame != null) {
                onResumeGame.accept(() -> {});
            }
        });
        
        multiplayerPausePanel.setOnSettingsAction(() -> {
            if (onShowSettings != null) {
                onShowSettings.accept(() -> {});
            }
        });
        
        multiplayerPausePanel.setOnRestartAction(() -> {
            if (onRestartGame != null) {
                onRestartGame.accept(() -> {});
            }
        });
        
        multiplayerPausePanel.setOnQuitAction(() -> {
            if (onQuitToMenu != null) {
                onQuitToMenu.accept(() -> {});
            }
        });
    }
    
    
    /**
     * Gets the multiplayer timer label.
     * 
     * @return The Label displaying the multiplayer game timer
     */
    public Label getTimerLabel() {
        // Return the timer label so it can be registered with TimerManager
        return multiplayerTimerLabel;
    }
    
    /**
     * Sets the pause panel action handler to use for setting up pause panel actions.
     * If set, this will be used instead of the callback-based setup.
     * @param handler The pause panel action handler
     */
    /**
     * Sets the pause panel action handler for configuring pause panel actions.
     * If set before callbacks are configured, the handler will be used when
     * callbacks are set. Otherwise, callback-based setup is used.
     * 
     * @param handler The PausePanelActionHandler instance to use
     */
    public void setPausePanelActionHandler(com.comp2042.controller.PausePanelActionHandler handler) {
        this.pausePanelActionHandler = handler;
        if (handler != null && multiplayerPausePanel != null) {
            handler.setupMultiplayerPausePanelActions(multiplayerPausePanel);
        }
    }

}

