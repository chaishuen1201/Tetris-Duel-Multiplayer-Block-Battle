package com.comp2042.view;

import com.comp2042.controller.GameConstants;
import com.comp2042.controller.GameController;
import com.comp2042.event.InputEventListener;
import com.comp2042.logic.bricks.Brick;
import com.comp2042.model.SimpleBoard;
import com.comp2042.model.ViewData;
import com.comp2042.view.ColorStrategy;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    private Runnable onAttachKeyboardHandlers;
    private Consumer<Parent> onGetRootBorderPane;
    private Consumer<SettingsPanel> onSetSettingsPanel;
    
    // Pause panel action handler (optional, for direct setup)
    private com.comp2042.controller.PausePanelActionHandler pausePanelActionHandler;
    
    // Game controllers and event listeners (needed for UI updates)
    private GameController gameController1, gameController2;
    private InputEventListener eventListener1, eventListener2;
    private ViewData currentBrickData1, currentBrickData2;
    
    public MultiplayerScreen() {
        initializePanels();
    }
    
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
            Runnable onAttachKeyboardHandlers,
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
        this.onAttachKeyboardHandlers = onAttachKeyboardHandlers;
        this.onGetRootBorderPane = onGetRootBorderPane;
        this.onSetSettingsPanel = onSetSettingsPanel;
        
        // Re-setup pause panel actions now that callbacks are set
        // If pausePanelActionHandler is set, use it; otherwise use callback-based setup
        if (pausePanelActionHandler != null && multiplayerPausePanel != null) {
            pausePanelActionHandler.setupMultiplayerPausePanelActions(multiplayerPausePanel);
        } else {
            setupPausePanelActions();
        }
        setupWinningPanelActions();
    }
    
    public void setGameControllers(GameController gameController1, GameController gameController2) {
        this.gameController1 = gameController1;
        this.gameController2 = gameController2;
    }
    
    public void setEventListeners(InputEventListener eventListener1, InputEventListener eventListener2) {
        this.eventListener1 = eventListener1;
        this.eventListener2 = eventListener2;
    }
    
    public HBox getContainer() {
        return multiplayerContainer;
    }
    
    public StackPane getWrapper() {
        return multiplayerWrapper;
    }
    
    public GridPane getGamePanel(int playerNumber) {
        return (playerNumber == 1) ? gamePanel1 : gamePanel2;
    }
    
    public GridPane getBrickPanel(int playerNumber) {
        return (playerNumber == 1) ? brickPanel1 : brickPanel2;
    }
    
    public GridPane getGhostPanel(int playerNumber) {
        return (playerNumber == 1) ? ghostPanel1 : ghostPanel2;
    }
    
    public Rectangle[][] getDisplayMatrix(int playerNumber) {
        return (playerNumber == 1) ? displayMatrix1 : displayMatrix2;
    }
    
    public Label getScoreLabel(int playerNumber) {
        return (playerNumber == 1) ? scoreLabel1 : scoreLabel2;
    }
    
    public Label getLevelLabel(int playerNumber) {
        return (playerNumber == 1) ? levelLabel1 : levelLabel2;
    }
    
    public GridPane getHoldBrickPanel(int playerNumber) {
        return (playerNumber == 1) ? holdBrickPanel1 : holdBrickPanel2;
    }
    
    public VBox getNextBricksPanel(int playerNumber) {
        return (playerNumber == 1) ? nextBricksPanel1 : nextBricksPanel2;
    }
    
    public Rectangle[][] getHoldBrickRectangles(int playerNumber) {
        return (playerNumber == 1) ? holdBrickRectangles1 : holdBrickRectangles2;
    }
    
    public List<GridPane> getNextBrickPanes(int playerNumber) {
        return (playerNumber == 1) ? nextBrickPanes1 : nextBrickPanes2;
    }
    
    public boolean isPlayer1Ready() {
        return player1Ready;
    }
    
    public boolean isPlayer2Ready() {
        return player2Ready;
    }
    
    public void setPlayer1Ready(boolean ready) {
        player1Ready = ready;
        if (onSetPlayer1Ready != null) {
            onSetPlayer1Ready.accept(ready);
        }
    }
    
    public void setPlayer2Ready(boolean ready) {
        player2Ready = ready;
        if (onSetPlayer2Ready != null) {
            onSetPlayer2Ready.accept(ready);
        }
    }
    
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
    
    public void showReadyPanel() {
        // Reset ready states
        player1Ready = false;
        player2Ready = false;
        
        // Create ready panel if it doesn't exist
        if (readyPanel == null) {
            readyPanel = new BorderPane();
            readyPanel.getStyleClass().add("ready-panel");
            
            VBox mainContainer = new VBox(30);
            mainContainer.getStyleClass().add("ready-container");
            mainContainer.setAlignment(Pos.CENTER);
            
            // Title
            Label titleLabel = new Label("GET READY!");
            titleLabel.getStyleClass().add("ready-title");
            
            // Create two separate boxes for each player
            HBox playersContainer = new HBox(40);
            playersContainer.setAlignment(Pos.CENTER);
            
            // Player 1 box
            VBox player1Box = new VBox(15);
            player1Box.getStyleClass().add("ready-player-box");
            player1Box.setAlignment(Pos.CENTER);
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
            player2Box.setAlignment(Pos.CENTER);
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
            multiplayerReadyOverlay = new StackPane();
            multiplayerReadyOverlay.setAlignment(Pos.CENTER);
            multiplayerReadyOverlay.setMaxWidth(Double.MAX_VALUE);
            multiplayerReadyOverlay.setMaxHeight(Double.MAX_VALUE);
            multiplayerReadyOverlay.setPickOnBounds(true);
            multiplayerReadyOverlay.setMouseTransparent(false);
            
            readyPanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            multiplayerReadyOverlay.getChildren().add(readyPanel);
        }
        
        // Add ready overlay to wrapper if it exists
        if (multiplayerWrapper != null) {
            if (!multiplayerWrapper.getChildren().contains(multiplayerReadyOverlay)) {
                multiplayerWrapper.getChildren().add(multiplayerReadyOverlay);
            }
            multiplayerReadyOverlay.setVisible(true);
            multiplayerReadyOverlay.setManaged(true);
        }
        
        // Update ready labels
        updateReadyLabels();
    }
    
    public void hideReadyPanel() {
        if (multiplayerReadyOverlay != null) {
            multiplayerReadyOverlay.setVisible(false);
            multiplayerReadyOverlay.setManaged(false);
        }
    }
    
    public void updateReadyLabels() {
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
    
    public void checkBothReady() {
        if (player1Ready && player2Ready) {
            hideReadyPanel();
            if (onStartGame != null) {
                onStartGame.accept(() -> {});
            }
        }
    }
    
    public void showPausePanel() {
        if (multiplayerPauseOverlay != null && multiplayerPausePanel != null) {
            multiplayerPauseOverlay.setVisible(true);
            multiplayerPauseOverlay.setManaged(true);
            multiplayerPauseOverlay.setMouseTransparent(false);
        }
    }
    
    public void hidePausePanel() {
        if (multiplayerPauseOverlay != null) {
            multiplayerPauseOverlay.setVisible(false);
            multiplayerPauseOverlay.setManaged(false);
            multiplayerPauseOverlay.setMouseTransparent(true);
        }
    }
    
    public void showWinningPanel(int winnerPlayerNumber, int timeUsed) {
        if (multiplayerWinningOverlay != null && multiplayerWinningPanel != null) {
            multiplayerWinningPanel.setWinner(winnerPlayerNumber);
            multiplayerWinningPanel.setTimeUsed(timeUsed);
            multiplayerWinningOverlay.setVisible(true);
            multiplayerWinningOverlay.setManaged(true);
            multiplayerWinningOverlay.setMouseTransparent(false);
            
            if (multiplayerWrapper != null && multiplayerWrapper.getChildren().contains(multiplayerWinningOverlay)) {
                multiplayerWrapper.getChildren().remove(multiplayerWinningOverlay);
                multiplayerWrapper.getChildren().add(multiplayerWinningOverlay);
            }
            
            if (multiplayerContainer != null) {
                multiplayerContainer.setManaged(true);
            }
            if (multiplayerWrapper != null) {
                multiplayerWrapper.setManaged(true);
            }
        }
    }
    
    public void hideWinningPanel() {
        if (multiplayerWinningOverlay != null) {
            multiplayerWinningOverlay.setVisible(false);
            multiplayerWinningOverlay.setManaged(false);
            multiplayerWinningOverlay.setMouseTransparent(true);
        }
    }
    
    public void showSettingsOverlay(SettingsPanel settingsPanel) {
        if (multiplayerSettingsOverlay != null && settingsPanel != null) {
            if (!multiplayerSettingsOverlay.getChildren().contains(settingsPanel)) {
                multiplayerSettingsOverlay.getChildren().add(settingsPanel);
            }
            multiplayerSettingsOverlay.setVisible(true);
            multiplayerSettingsOverlay.setManaged(true);
            multiplayerSettingsOverlay.setMouseTransparent(false);
            
            if (multiplayerWrapper != null && multiplayerWrapper.getChildren().contains(multiplayerSettingsOverlay)) {
                multiplayerWrapper.getChildren().remove(multiplayerSettingsOverlay);
                multiplayerWrapper.getChildren().add(multiplayerSettingsOverlay);
            }
        }
    }
    
    public void hideSettingsOverlay() {
        if (multiplayerSettingsOverlay != null) {
            multiplayerSettingsOverlay.setVisible(false);
            multiplayerSettingsOverlay.setManaged(false);
            multiplayerSettingsOverlay.setMouseTransparent(true);
        }
    }
    
    public void refreshBrick(ViewData brick, int playerNumber) {
        if (brick != null) {
            if (playerNumber == 1) {
                currentBrickData1 = brick;
            } else if (playerNumber == 2) {
                currentBrickData2 = brick;
            }
        }
        
        GridPane currentBrickPanel = (playerNumber == 1) ? brickPanel1 : brickPanel2;
        GridPane currentGhostPanel = (playerNumber == 1) ? ghostPanel1 : ghostPanel2;
        InputEventListener currentEventListener = (playerNumber == 1) ? eventListener1 : eventListener2;
        double scale = 0.85;
        int scaledBrickSize = (int)(BRICK_SIZE * scale);
        
        if (currentBrickPanel != null && brick != null) {
            currentBrickPanel.getChildren().clear();
            if (currentGhostPanel != null) {
                currentGhostPanel.getChildren().clear();
            }
            
            int[][] data = brick.getBrickData();
            int offsetX = brick.getXPosition();
            int offsetY = brick.getYPosition();
            
            // Draw ghost piece first
            if (currentGhostPanel != null && currentEventListener instanceof GameController) {
                GameController gameController = (GameController) currentEventListener;
                if (gameController.getBoard() instanceof SimpleBoard) {
                    SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                    java.awt.Point ghostPos = simpleBoard.getGhostPosition();
                    
                    int ghostX = (int) ghostPos.getX();
                    int ghostY = (int) ghostPos.getY();
                    
                    if (ghostY > offsetY) {
                        for (int i = 0; i < data.length; i++) {
                            for (int j = 0; j < data[i].length; j++) {
                                if (data[j][i] != 0) {
                                    int cellX = ghostX + i;
                                    int cellY = ghostY + j;
                                    
                                    if (cellX >= 0 && cellY >= 0 && cellX < BOARD_WIDTH && cellY < BOARD_HEIGHT) {
                                        Rectangle ghostRect = new Rectangle(scaledBrickSize, scaledBrickSize);
                                        
                                        javafx.scene.paint.Paint brickColor = ColorStrategy.getColorForBrickType(data[j][i]);
                                        if (brickColor instanceof javafx.scene.paint.Color) {
                                            javafx.scene.paint.Color color = (javafx.scene.paint.Color) brickColor;
                                            javafx.scene.paint.Color ghostColor = new javafx.scene.paint.Color(
                                                color.getRed(),
                                                color.getGreen(),
                                                color.getBlue(),
                                                0.3
                                            );
                                            ghostRect.setFill(ghostColor);
                                            javafx.scene.paint.Color strokeColor = new javafx.scene.paint.Color(
                                                color.getRed() * 0.7,
                                                color.getGreen() * 0.7,
                                                color.getBlue() * 0.7,
                                                0.5
                                            );
                                            ghostRect.setStroke(strokeColor);
                                            ghostRect.setStrokeWidth(1.5);
                                        } else {
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
            
            // Draw actual brick
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    if (data[j][i] != 0) {
                        Rectangle rect = new Rectangle(scaledBrickSize, scaledBrickSize);
                        rect.setFill(ColorStrategy.getColorForBrickType(data[j][i]));
                        rect.setArcHeight(5);
                        rect.setArcWidth(5);
                        currentBrickPanel.add(rect, offsetX + i, offsetY + j);
                    }
                }
            }
        }
    }
    
    public void refreshGameBackground(int[][] board, int playerNumber) {
        Rectangle[][] matrix = (playerNumber == 1) ? displayMatrix1 : displayMatrix2;
        if (matrix != null) {
            for (int i = 0; i < Math.min(BOARD_HEIGHT, board.length); i++) {
                for (int j = 0; j < Math.min(BOARD_WIDTH, board[i].length); j++) {
                    if (matrix[i][j] != null) {
                        matrix[i][j].setFill(ColorStrategy.getColorForBrickType(board[i][j]));
                    }
                }
            }
        }
    }
    
    public void updateNextBricks(List<Brick> nextBricks, int playerNumber) {
        List<GridPane> panes = (playerNumber == 1) ? nextBrickPanes1 : nextBrickPanes2;
        double scale = 0.85;
        int brickSize = (int)((BRICK_SIZE - 10) * scale);
        
        if (panes == null || panes.isEmpty()) return;
        
        // For multiplayer, only show the first brick
        int maxBricks = 1;
        
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
                            rect.setFill(ColorStrategy.getColorForBrickType(shape[r][c]));
                            rect.setArcHeight(5);
                            rect.setArcWidth(5);
                            pane.add(rect, c, r);
                        }
                    }
                }
            }
        }
    }
    
    public void updateHoldBrick(Brick heldBrick, int playerNumber) {
        Rectangle[][] rectangles = (playerNumber == 1) ? holdBrickRectangles1 : holdBrickRectangles2;
        
        if (rectangles == null) return;
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (rectangles[i][j] != null) rectangles[i][j].setFill(Color.TRANSPARENT);
        if (heldBrick != null) {
            int[][] shape = heldBrick.getShapeMatrix().get(0);
            for (int i = 0; i < shape.length; i++)
                for (int j = 0; j < shape[i].length; j++)
                    if (shape[i][j] != 0 && rectangles[i][j] != null)
                        rectangles[i][j].setFill(ColorStrategy.getColorForBrickType(shape[i][j]));
        }
    }
    
    public void bindScore(IntegerProperty score, int playerNumber) {
        Label label = (playerNumber == 1) ? scoreLabel1 : scoreLabel2;
        if (label != null && score != null) {
            label.textProperty().unbind();
            label.textProperty().bind(score.asString("%d"));
        }
    }
    
    public void bindLevel(IntegerProperty level, int playerNumber) {
        Label label = (playerNumber == 1) ? levelLabel1 : levelLabel2;
        if (label != null && level != null) {
            label.textProperty().unbind();
            label.textProperty().bind(level.asString("%d"));
        }
    }
    
    public void clearGamePanels() {
        // Clear displayMatrix1
        if (displayMatrix1 != null) {
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    if (displayMatrix1[i][j] != null) {
                        displayMatrix1[i][j].setFill(Color.TRANSPARENT);
                    }
                }
            }
        }
        
        // Clear displayMatrix2
        if (displayMatrix2 != null) {
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    if (displayMatrix2[i][j] != null) {
                        displayMatrix2[i][j].setFill(Color.TRANSPARENT);
                    }
                }
            }
        }
        
        // Clear brick panels
        double scale = 0.85;
        int brickSize = (int)(BRICK_SIZE * scale);
        
        if (brickPanel1 != null) {
            brickPanel1.getChildren().clear();
            initializeBrickPanel(brickPanel1, brickSize);
        }
        if (brickPanel2 != null) {
            brickPanel2.getChildren().clear();
            initializeBrickPanel(brickPanel2, brickSize);
        }
        
        // Clear ghost panels
        if (ghostPanel1 != null) {
            ghostPanel1.getChildren().clear();
            initializeBrickPanel(ghostPanel1, brickSize);
        }
        if (ghostPanel2 != null) {
            ghostPanel2.getChildren().clear();
            initializeBrickPanel(ghostPanel2, brickSize);
        }
        
        // Clear hold panels
        int holdBrickSize = (int)((BRICK_SIZE - 10) * scale);
        
        if (holdBrickPanel1 != null) {
            holdBrickPanel1.getChildren().clear();
            if (holdBrickRectangles1 == null) {
                holdBrickRectangles1 = new Rectangle[4][4];
            }
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
            if (holdBrickRectangles2 == null) {
                holdBrickRectangles2 = new Rectangle[4][4];
            }
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
        
        // Clear next bricks panels
        int nextBrickSize = (int)(80 * scale);
        
        if (nextBricksPanel1 != null) {
            nextBricksPanel1.getChildren().clear();
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
    
    public void setBrickPanelsVisible(boolean visible, SettingsPanel settingsPanel) {
        if (brickPanel1 != null) {
            brickPanel1.setVisible(visible);
        }
        if (brickPanel2 != null) {
            brickPanel2.setVisible(visible);
        }
        if (ghostPanel1 != null && settingsPanel != null) {
            javafx.scene.control.CheckBox ghostCheckBox = settingsPanel.getGhostPieceCheckBox();
            boolean showGhost = ghostCheckBox != null && ghostCheckBox.isSelected();
            ghostPanel1.setVisible(showGhost && visible);
        }
        if (ghostPanel2 != null && settingsPanel != null) {
            javafx.scene.control.CheckBox ghostCheckBox = settingsPanel.getGhostPieceCheckBox();
            boolean showGhost = ghostCheckBox != null && ghostCheckBox.isSelected();
            ghostPanel2.setVisible(showGhost && visible);
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
        setupWinningPanelActions();
        multiplayerWinningOverlay.getChildren().add(multiplayerWinningPanel);
        multiplayerWinningOverlay.setVisible(false);
        multiplayerWrapper.getChildren().add(multiplayerWinningOverlay);
        
        // Initialize game panels
        initializeMultiplayerPanels();
    }
    
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
        if (multiplayerTimerLabel == null) {
            multiplayerTimerLabel = new Label("00:00");
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
    
    private void setupWinningPanelActions() {
        if (multiplayerWinningPanel == null) return;
        
        multiplayerWinningPanel.setOnRestartAction(() -> {
            if (onRestartGame != null) {
                onRestartGame.accept(() -> {});
            }
        });
        
        multiplayerWinningPanel.setOnMainMenuAction(() -> {
            if (onQuitToMenu != null) {
                onQuitToMenu.accept(() -> {});
            }
        });
    }
    
    public Label getTimerLabel() {
        // Return the timer label so it can be registered with TimerManager
        return multiplayerTimerLabel;
    }
    
    /**
     * Sets the pause panel action handler to use for setting up pause panel actions.
     * If set, this will be used instead of the callback-based setup.
     * @param handler The pause panel action handler
     */
    public void setPausePanelActionHandler(com.comp2042.controller.PausePanelActionHandler handler) {
        this.pausePanelActionHandler = handler;
        if (handler != null && multiplayerPausePanel != null) {
            handler.setupMultiplayerPausePanelActions(multiplayerPausePanel);
        }
    }
    
    public PausePanel getMultiplayerPausePanel() {
        return multiplayerPausePanel;
    }
}

