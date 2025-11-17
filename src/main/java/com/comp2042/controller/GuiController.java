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
import com.comp2042.view.MainMenuPanel;
import com.comp2042.view.NotificationPanel;
import com.comp2042.view.SettingsPanel;
import com.comp2042.model.HighScoreManager;
import com.comp2042.model.SimpleBoard;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.control.Label;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.Parent;
import javafx.application.Platform;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 30;
    private static final int GRID_GAP = 1;
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int GAME_PANEL_WIDTH = (BRICK_SIZE * BOARD_WIDTH) + (GRID_GAP * (BOARD_WIDTH - 1));
    private static final int GAME_PANEL_HEIGHT = (BRICK_SIZE * BOARD_HEIGHT) + (GRID_GAP * (BOARD_HEIGHT - 1));
    private static final double SOFT_DROP_RATE = 12.0;
    private ViewData currentBrickData; // Store current brick data for countdown refresh

    @FXML private BorderPane gameBoard;
    @FXML private StackPane gameStack;
    @FXML private GridPane gamePanel;
    @FXML private Group groupNotification;
    @FXML private GridPane brickPanel;
    @FXML private GridPane ghostPanel;
    @FXML private GameOverPanel gameOverPanel;
    @FXML private SettingsPanel settingsPanel;
    @FXML private VBox nextBricksPanel;
    @FXML private GridPane holdBrickPanel;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label linesLabel;
    @FXML private Button pauseButton;
    @FXML private MainMenuPanel mainMenuPanel;
    @FXML private Label countdownLabel;
    @FXML private HBox bottomPanel;

    // Multiplayer fields
    private HBox multiplayerContainer;
    private boolean isMultiplayerMode = false;
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
    
    // Store original panels to restore later
    private VBox originalLeftPanel, originalRightPanel;

    private Rectangle[][] displayMatrix;
    private InputEventListener eventListener;
    private Timeline timeLine;
    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);
    private boolean gameStarted = false;
    private int currentLevel = 1;

    private Rectangle[][] holdBrickRectangles;
    private List<GridPane> nextBrickPanes = new ArrayList<>();
    private HighScoreManager highScoreManager = new HighScoreManager();
    
    // Audio players
    private MediaPlayer countdownSound;
    private MediaPlayer gameMusic;
    private MediaPlayer gameOverSound;
    private MediaPlayer lineClearSound;
    private MediaPlayer mainMenuMusic;
    
    // Volume control
    private double currentVolume = 0.5; // Default volume (50%)
    private boolean isMuted = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        initializeAudio();

        if (gameOverPanel != null) gameOverPanel.setVisible(false);
        
        // Initialize countdown label
        if (countdownLabel != null) {
            countdownLabel.setVisible(false);
            countdownLabel.setAlignment(javafx.geometry.Pos.CENTER);
        }

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

        if (gameBoard != null) {
            gameBoard.setFocusTraversable(true);
            gameBoard.requestFocus();
            gameBoard.setOnKeyPressed(this::handleKeyPress);
            gameBoard.setOnKeyReleased(this::handleKeyRelease);
        }
        
        // Start main menu music
        playMainMenuMusic();
    }
    
    private void initializeAudio() {
        try {
            // Countdown sound
            URL countdownUrl = getClass().getClassLoader().getResource("audio/3-2-1-countdown.mp3");
            if (countdownUrl != null) {
                Media countdownMedia = new Media(countdownUrl.toExternalForm());
                countdownSound = new MediaPlayer(countdownMedia);
            }
            
            // Game music (looping)
            URL gameMusicUrl = getClass().getClassLoader().getResource("audio/A-Type Music (Korobeiniki).mp3");
            if (gameMusicUrl != null) {
                Media gameMusicMedia = new Media(gameMusicUrl.toExternalForm());
                gameMusic = new MediaPlayer(gameMusicMedia);
                gameMusic.setCycleCount(MediaPlayer.INDEFINITE);
            }
            
            // Game over sound
            URL gameOverUrl = getClass().getClassLoader().getResource("audio/Game Over.mp3");
            if (gameOverUrl != null) {
                Media gameOverMedia = new Media(gameOverUrl.toExternalForm());
                gameOverSound = new MediaPlayer(gameOverMedia);
            }
            
            // Line clear sound
            URL lineClearUrl = getClass().getClassLoader().getResource("audio/Stage Clear.mp3");
            if (lineClearUrl != null) {
                Media lineClearMedia = new Media(lineClearUrl.toExternalForm());
                lineClearSound = new MediaPlayer(lineClearMedia);
            }
            
            // Main menu music (looping)
            URL mainMenuUrl = getClass().getClassLoader().getResource("audio/tetris-party-deluxe-main-menu-music.mp3");
            if (mainMenuUrl != null) {
                Media mainMenuMedia = new Media(mainMenuUrl.toExternalForm());
                mainMenuMusic = new MediaPlayer(mainMenuMedia);
                mainMenuMusic.setCycleCount(MediaPlayer.INDEFINITE);
            }
            
            // Apply initial volume to all players
            applyVolumeToAllPlayers(currentVolume);
        } catch (Exception e) {
            System.out.println("Error loading audio files: " + e.getMessage());
        }
    }
    
    private void initializeSettingsPanel() {
        if (settingsPanel != null) {
            settingsPanel.setVisible(false);
            
            // Set up volume slider
            javafx.scene.control.Slider volumeSlider = settingsPanel.getVolumeSlider();
            volumeSlider.setValue(currentVolume * 100); // Convert to percentage
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentVolume = newVal.doubleValue() / 100.0;
                if (!isMuted) {
                    applyVolumeToAllPlayers(currentVolume);
                }
                // If user moves slider while muted, unmute
                if (isMuted && newVal.doubleValue() != oldVal.doubleValue()) {
                    isMuted = false;
                    Button muteBtn = settingsPanel.getMuteButton();
                    if (muteBtn != null) {
                        muteBtn.setText("🔊");
                    }
                }
            });
            
            // Set up mute button
            Button muteButton = settingsPanel.getMuteButton();
            muteButton.setOnAction(e -> toggleMute());
            
            // Set up ghost piece checkbox
            javafx.scene.control.CheckBox ghostPieceCheckBox = settingsPanel.getGhostPieceCheckBox();
            ghostPieceCheckBox.setSelected(true); // Default to checked
            ghostPieceCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (ghostPanel != null) {
                    ghostPanel.setVisible(newVal && gameStarted);
                }
            });
            
            // Set up back button
            settingsPanel.setOnBackAction(() -> hideSettings());
        }
    }
    
    private void applyVolumeToAllPlayers(double volume) {
        if (countdownSound != null) {
            countdownSound.setVolume(volume);
        }
        if (gameMusic != null) {
            gameMusic.setVolume(volume);
        }
        if (gameOverSound != null) {
            gameOverSound.setVolume(volume);
        }
        if (lineClearSound != null) {
            lineClearSound.setVolume(volume);
        }
        if (mainMenuMusic != null) {
            mainMenuMusic.setVolume(volume);
        }
    }
    
    private void toggleMute() {
        isMuted = !isMuted;
        Button muteButton = settingsPanel != null ? settingsPanel.getMuteButton() : null;
        
        if (isMuted) {
            applyVolumeToAllPlayers(0.0);
            if (muteButton != null) {
                muteButton.setText("🔇");
            }
        } else {
            applyVolumeToAllPlayers(currentVolume);
            if (muteButton != null) {
                muteButton.setText("🔊");
            }
        }
    }
    
    private void showSettings() {
        if (settingsPanel != null && mainMenuPanel != null) {
            mainMenuPanel.setVisible(false);
            settingsPanel.setVisible(true);
        }
    }
    
    private void hideSettings() {
        if (settingsPanel != null && mainMenuPanel != null) {
            settingsPanel.setVisible(false);
            mainMenuPanel.setVisible(true);
        }
    }
    
    private void showMultiplayer() {
        if (mainMenuPanel != null) {
            mainMenuPanel.setVisible(false);
        }
        
        isMultiplayerMode = true;
        
        // Create multiplayer container if it doesn't exist
        if (multiplayerContainer == null) {
            initializeMultiplayerPanels();
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
                // Add multiplayer container if not already added
                if (!centerVBox.getChildren().contains(multiplayerContainer)) {
                    centerVBox.getChildren().add(multiplayerContainer);
                }
                multiplayerContainer.setVisible(true);
                multiplayerContainer.setManaged(true);
                // Request immediate layout update
                centerVBox.requestLayout();
            }
        }
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
        
        // Add all components to player panel: Hold, Score, Next
        playerPanel.getChildren().addAll(holdBox, scoreBox, nextBox);
        
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
        
        multiplayerContainer = new HBox(30);
        multiplayerContainer.setAlignment(javafx.geometry.Pos.CENTER);
        multiplayerContainer.getStyleClass().add("multiplayer-container");
        // Initially set to not managed so it doesn't affect layout until shown
        multiplayerContainer.setManaged(false);
        multiplayerContainer.setVisible(false);
        
        // Create Player 1 container (side panel + game field)
        VBox player1Container = createPlayerContainer(1, scaledBrickSize, scaledPanelWidth, scaledPanelHeight, scale);
        
        // Create VS label
        Label vsLabel = new Label("VS");
        vsLabel.getStyleClass().add("vs-label");
        
        // Create Player 2 container (game field + side panel)
        VBox player2Container = createPlayerContainer(2, scaledBrickSize, scaledPanelWidth, scaledPanelHeight, scale);
        
        multiplayerContainer.getChildren().addAll(player1Container, vsLabel, player2Container);
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
        
        // Game field container - match exact height, adjust width proportionally
        BorderPane gameFieldBoard = new BorderPane();
        gameFieldBoard.getStyleClass().add("gameBoard");
        // Set exact height to match side panel, width scales proportionally
        gameFieldBoard.setPrefHeight(panelHeight);
        gameFieldBoard.setMaxHeight(panelHeight);
        gameFieldBoard.setMinHeight(panelHeight);
        gameFieldBoard.setPrefWidth(panelWidth);
        gameFieldBoard.setMaxWidth(panelWidth);
        gameFieldBoard.setMinWidth(panelWidth);
        
        StackPane gameFieldStack = new StackPane();
        // Match exact dimensions - fill the entire gameFieldBoard
        gameFieldStack.setPrefSize(panelWidth, panelHeight);
        gameFieldStack.setMaxSize(panelWidth, panelHeight);
        gameFieldStack.setMinSize(panelWidth, panelHeight);
        
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
        
        // Create game panel - set exact dimensions
        GridPane gameFieldPanel = new GridPane();
        gameFieldPanel.setHgap(GRID_GAP);
        gameFieldPanel.setVgap(GRID_GAP);
        gameFieldPanel.setPrefSize(panelWidth, panelHeight);
        gameFieldPanel.setMaxSize(panelWidth, panelHeight);
        gameFieldPanel.setMinSize(panelWidth, panelHeight);
        initializeGameFieldPanel(gameFieldPanel, brickSize, matrix);
        
        // Create brick panel - set exact dimensions
        GridPane gameFieldBrickPanel = new GridPane();
        gameFieldBrickPanel.setHgap(GRID_GAP);
        gameFieldBrickPanel.setVgap(GRID_GAP);
        gameFieldBrickPanel.setPrefSize(panelWidth, panelHeight);
        gameFieldBrickPanel.setMaxSize(panelWidth, panelHeight);
        gameFieldBrickPanel.setMinSize(panelWidth, panelHeight);
        gameFieldBrickPanel.setMouseTransparent(true);
        initializeBrickPanel(gameFieldBrickPanel, brickSize);
        
        // Create ghost panel - set exact dimensions
        GridPane gameFieldGhostPanel = new GridPane();
        gameFieldGhostPanel.setHgap(GRID_GAP);
        gameFieldGhostPanel.setVgap(GRID_GAP);
        gameFieldGhostPanel.setPrefSize(panelWidth, panelHeight);
        gameFieldGhostPanel.setMaxSize(panelWidth, panelHeight);
        gameFieldGhostPanel.setMinSize(panelWidth, panelHeight);
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
        
        gameFieldBoard.setCenter(gameFieldStack);
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
    
    private void playMainMenuMusic() {
        stopAllMusic();
        if (mainMenuMusic != null) {
            mainMenuMusic.play();
        }
    }
    
    private void stopAllMusic() {
        if (gameMusic != null) {
            gameMusic.stop();
        }
        if (mainMenuMusic != null) {
            mainMenuMusic.stop();
        }
    }

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

    private void handleKeyPress(KeyEvent keyEvent) {
        if (!gameStarted) {
            // If game hasn't started, Enter key can start it
            if (keyEvent.getCode() == KeyCode.ENTER) {
                startGame();
                keyEvent.consume();
            }
            return;
        }

        if (!isPause.get() && !isGameOver.get()) {
            switch (keyEvent.getCode()) {
                case LEFT, A -> { if (eventListener != null) refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER))); }
                case RIGHT, D -> { if (eventListener != null) refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER))); }
                case UP, W -> { if (eventListener != null) refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER))); }
                case DOWN, S -> { if (timeLine != null) timeLine.setRate(SOFT_DROP_RATE); moveDown(new MoveEvent(EventType.DOWN, EventSource.USER)); }
                case SPACE -> { 
                    if (eventListener != null) {
                        // Hard drop can also clear lines
                        DownData downData = eventListener.onHardDropEvent(new MoveEvent(EventType.HARD_DROP, EventSource.USER));
                        if (downData != null) {
                            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                                showNotification("+" + downData.getClearRow().getScoreBonus());
                                // Play line clear sound
                                if (lineClearSound != null) {
                                    lineClearSound.stop();
                                    lineClearSound.seek(Duration.ZERO);
                                    lineClearSound.play();
                                }
                            }
                            refreshBrick(downData.getViewData());
                        }
                    }
                }
                case C, SHIFT -> { if (eventListener != null) refreshBrick(eventListener.onHoldEvent(new MoveEvent(EventType.HOLD, EventSource.USER))); }
            }
        }
        if (keyEvent.getCode() == KeyCode.N) newGame(null);
        if (keyEvent.getCode() == KeyCode.P) pauseGame(null);
        keyEvent.consume();
    }

    private void handleKeyRelease(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) updateTimelineRate();
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        refreshBrick(brick);
        refreshGameBackground(boardMatrix);

        if (timeLine != null) timeLine.stop();
        timeLine = new Timeline(new KeyFrame(Duration.millis(400), ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))));
        timeLine.setCycleCount(Timeline.INDEFINITE);

        updateTimelineRate();

        if (gameBoard != null) gameBoard.requestFocus();
    }

    private javafx.scene.paint.Paint getFillColor(int brickType) {
        return ColorStrategy.getColorForBrickType(brickType);
    }

    private void refreshBrick(ViewData brick) {
        // Always store the current brick data
        if (brick != null) {
            currentBrickData = brick;
        }

        // Don't show brick if game hasn't started (menu is visible)
        if (!gameStarted) {
            return;
        }

        if (!isPause.get() && brickPanel != null && brick != null) {
            // Clear both panels
            brickPanel.getChildren().clear();
            if (ghostPanel != null) {
                ghostPanel.getChildren().clear();
            }
            
            int[][] data = brick.getBrickData();
            int offsetX = brick.getXPosition();
            int offsetY = brick.getYPosition();
            
            // Draw ghost piece first (behind the actual brick)
            if (ghostPanel != null && eventListener instanceof GameController) {
                GameController gameController = (GameController) eventListener;
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
                                        Rectangle ghostRect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                                        
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
                                        ghostPanel.add(ghostRect, cellX, cellY);
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
                        Rectangle rect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                        rect.setFill(getFillColor(data[j][i]));
                        rect.setArcHeight(5);
                        rect.setArcWidth(5);
                        brickPanel.add(rect, offsetX + i, offsetY + j);
                    }
                }
            }
        }
    }

    public void refreshGameBackground(int[][] board) {
        if (displayMatrix != null) {
            for (int i = 0; i < Math.min(BOARD_HEIGHT, board.length); i++) {
                for (int j = 0; j < Math.min(BOARD_WIDTH, board[i].length); j++) {
                    if (displayMatrix[i][j] != null) displayMatrix[i][j].setFill(getFillColor(board[i][j]));
                }
            }
        }
    }

    private void moveDown(MoveEvent event) {
        if (!gameStarted || isPause.get() || isGameOver.get()) {
            return;
        }
        if (eventListener != null) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData != null) {
                if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                    showNotification("+" + downData.getClearRow().getScoreBonus());
                    // Play line clear sound
                    if (lineClearSound != null) {
                        lineClearSound.stop();
                        lineClearSound.seek(Duration.ZERO);
                        lineClearSound.play();
                    }
                }
                refreshBrick(downData.getViewData());
            }
        }
        if (gameBoard != null) gameBoard.requestFocus();
    }

    private void showNotification(String message) {
        if (groupNotification != null) {
            NotificationPanel panel = new NotificationPanel(message);
            groupNotification.getChildren().add(panel);
            panel.showScore(groupNotification.getChildren());
        }
    }

    public void setEventListener(InputEventListener listener) { this.eventListener = listener; }

    public void bindScore(IntegerProperty score) {
        if (scoreLabel != null && score != null) {
            // Unbind first to avoid binding conflicts
            scoreLabel.textProperty().unbind();
            scoreLabel.textProperty().bind(score.asString("Score: %d"));
        }
    }

    public void bindLevel(IntegerProperty level) {
        if (levelLabel != null && level != null) {
            levelLabel.textProperty().bind(level.asString("Level: %d"));
            level.addListener((obs, oldVal, newVal) -> { currentLevel = newVal.intValue(); updateTimelineRate(); });
            currentLevel = level.get();
            updateTimelineRate();
        }
    }

    public void bindLines(IntegerProperty lines) {
        if (linesLabel != null && lines != null) linesLabel.textProperty().bind(lines.asString("Lines: %d"));
    }

    public void gameOver() {
        if (timeLine != null) timeLine.stop();
        
        // Stop game music and play game over sound
        if (gameMusic != null) {
            gameMusic.stop();
        }
        if (gameOverSound != null) {
            gameOverSound.stop();
            gameOverSound.seek(Duration.ZERO);
            gameOverSound.play();
        }
        
        // Hide bottom panel when game is over
        if (bottomPanel != null) {
            bottomPanel.setVisible(false);
        }
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
                // Hide bottom panel when returning to main menu
                if (bottomPanel != null) bottomPanel.setVisible(false);
                isGameOver.set(false);
                gameStarted = false;
                if (timeLine != null) timeLine.stop();
                
                // Stop game over sound and play main menu music
                if (gameOverSound != null) {
                    gameOverSound.stop();
                }
                playMainMenuMusic();
            });
            
            gameOverPanel.setVisible(true);
        }
        isGameOver.set(true);
    }

    public void newGame(ActionEvent actionEvent) {
        if (timeLine != null) timeLine.stop();
        if (gameOverPanel != null) gameOverPanel.setVisible(false);
        
        // Stop game over sound and start game music
        if (gameOverSound != null) {
            gameOverSound.stop();
        }
        if (mainMenuMusic != null) {
            mainMenuMusic.stop();
        }
        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.seek(Duration.ZERO);
            gameMusic.play();
        }
        
        if (eventListener != null) eventListener.createNewGame();
        if (gameBoard != null) gameBoard.requestFocus();
        if (timeLine != null) timeLine.play();
        isPause.set(false);
        isGameOver.set(false);
        gameStarted = true;
        // Show bottom panel when starting a new game
        if (bottomPanel != null) {
            bottomPanel.setVisible(true);
        }
        if (pauseButton != null) {
            pauseButton.setText("Pause");
        }
        if (mainMenuPanel != null) {
            mainMenuPanel.setVisible(false);
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
        if (mainMenuMusic != null) {
            mainMenuMusic.stop();
        }
        
        // Play countdown sound
        if (countdownSound != null) {
            countdownSound.stop();
            countdownSound.seek(Duration.ZERO);
            countdownSound.play();
        }
        
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
        if (mainMenuMusic != null) {
            mainMenuMusic.stop();
        }
        if (gameMusic != null) {
            gameMusic.play();
        }
        
        // Ensure score binding is active
        if (eventListener instanceof GameController) {
            GameController gameController = (GameController) eventListener;
            bindScore(gameController.getScoreProperty());
        }
        
        // Make brick panel and ghost panel visible
        if (brickPanel != null) {
            brickPanel.setVisible(true);
        }
        if (ghostPanel != null && settingsPanel != null) {
            // Check if ghost piece checkbox is selected
            javafx.scene.control.CheckBox ghostCheckBox = settingsPanel.getGhostPieceCheckBox();
            boolean showGhost = ghostCheckBox != null && ghostCheckBox.isSelected();
            ghostPanel.setVisible(showGhost);
        }
        
        // Show bottom panel when game starts
        if (bottomPanel != null) {
            bottomPanel.setVisible(true);
        }
        
        // Refresh the brick display with stored brick data
        // The brick data was stored when initGameView was called
        if (currentBrickData != null) {
            refreshBrick(currentBrickData);
        }
        
        if (timeLine != null) {
            timeLine.play();
        }
        if (gameBoard != null) {
            gameBoard.requestFocus();
        }
    }

    public void quitGame() {
        javafx.application.Platform.exit();
    }

    public void pauseGame(ActionEvent actionEvent) {
        if (!isPause.get()) {
            if (timeLine != null) timeLine.pause();
            // Pause game music
            if (gameMusic != null) {
                gameMusic.pause();
            }
            isPause.set(true);
            if (pauseButton != null) {
                pauseButton.setText("Resume");
            }
        } else {
            if (timeLine != null) { timeLine.play(); updateTimelineRate(); }
            // Resume game music
            if (gameMusic != null) {
                gameMusic.play();
            }
            isPause.set(false);
            if (pauseButton != null) {
                pauseButton.setText("Pause");
            }
        }
        if (gameBoard != null) gameBoard.requestFocus();
    }

    public void updateNextBricks(List<Brick> nextBricks) {
        if (nextBrickPanes == null) return;
        for (int i = 0; i < nextBrickPanes.size(); i++) {
            GridPane pane = nextBrickPanes.get(i);
            if (pane == null) continue;
            pane.getChildren().clear();
            if (i < nextBricks.size()) {
                int[][] shape = nextBricks.get(i).getShapeMatrix().get(0);
                for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                        if (shape[r][c] != 0) {
                            Rectangle rect = new Rectangle(BRICK_SIZE - 10, BRICK_SIZE - 10);
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
        if (holdBrickRectangles == null) return;
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (holdBrickRectangles[i][j] != null) holdBrickRectangles[i][j].setFill(Color.TRANSPARENT);
        if (heldBrick != null) {
            int[][] shape = heldBrick.getShapeMatrix().get(0);
            for (int i = 0; i < shape.length; i++)
                for (int j = 0; j < shape[i].length; j++)
                    if (shape[i][j] != 0 && holdBrickRectangles[i][j] != null)
                        holdBrickRectangles[i][j].setFill(getFillColor(shape[i][j]));
        }
    }

    private void updateTimelineRate() {
        if (timeLine != null) {
            double rate = 1.0 + (Math.max(1, currentLevel) - 1) * 0.25;
            timeLine.setRate(rate);
        }
    }

    
}
