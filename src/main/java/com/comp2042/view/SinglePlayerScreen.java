package com.comp2042.view;

import com.comp2042.controller.GameConstants;
import com.comp2042.event.InputEventListener;
import com.comp2042.model.ViewData;
import javafx.beans.property.IntegerProperty;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the single player game screen UI components and rendering.
 * This class handles the complete UI setup for single-player Tetris gameplay, including
 * game board initialization, brick display panels, ghost piece rendering, next bricks
 * preview, hold brick display, and score/level/lines label bindings. The class uses
 * dependency injection pattern where UI components are set via setter methods (typically
 * injected from FXML). It initializes game panels with proper grid constraints, creates
 * display matrices for rendering, and manages the state of current brick data. The class
 * delegates actual rendering operations to GameViewRenderer, keeping rendering logic
 * separate from UI component management. It supports reactive property binding for score,
 * level, and lines using JavaFX IntegerProperty.
 */
public class SinglePlayerScreen {
    
    // Constants
    private static final int BRICK_SIZE = GameConstants.BRICK_SIZE;
    private static final int BOARD_WIDTH = GameConstants.BOARD_WIDTH;
    private static final int BOARD_HEIGHT = GameConstants.BOARD_HEIGHT;
    private static final int GAME_PANEL_WIDTH = GameConstants.GAME_PANEL_WIDTH;
    private static final int GAME_PANEL_HEIGHT = GameConstants.GAME_PANEL_HEIGHT;
    
    // UI Components (injected from FXML)
    private BorderPane gameBoard;
    private StackPane gameStack;
    private GridPane gamePanel;
    private Group groupNotification;
    private GridPane brickPanel;
    private GridPane ghostPanel;
    private GameOverPanel gameOverPanel;
    private PausePanel pausePanel;
    private VBox nextBricksPanel;
    private GridPane holdBrickPanel;
    private Label scoreLabel;
    private Label levelLabel;
    private Label linesLabel;
    private Label countdownLabel;
    private Label timerLabel;
    
    // Internal state
    private Rectangle[][] displayMatrix;
    private Rectangle[][] holdBrickRectangles;
    private List<GridPane> nextBrickPanes = new ArrayList<>();
    private ViewData currentBrickData;
    private InputEventListener eventListener;
    
    // Rendering - delegated to GameViewRenderer
    private final GameViewRenderer renderer = new GameViewRenderer();
    
    /**
     * Creates a new SinglePlayerScreen instance.
     * UI components must be set via setter methods (typically injected from FXML)
     * before the screen can be used. The renderer is initialized automatically.
     */
    public SinglePlayerScreen() {
        // Components will be injected via setters
    }
    
    /**
     * Sets the game board BorderPane (typically injected from FXML).
     * 
     * @param gameBoard The BorderPane containing the game board layout
     */
    public void setGameBoard(BorderPane gameBoard) {
        this.gameBoard = gameBoard;
    }
    
    /**
     * Sets the game stack StackPane (typically injected from FXML).
     * 
     * @param gameStack The StackPane for layering game components
     */
    public void setGameStack(StackPane gameStack) {
        this.gameStack = gameStack;
    }
    
    /**
     * Sets the game panel GridPane for the background grid (typically injected from FXML).
     * 
     * @param gamePanel The GridPane representing the game board background
     */
    public void setGamePanel(GridPane gamePanel) {
        this.gamePanel = gamePanel;
    }
    
    /**
     * Sets the notification group for displaying notifications (typically injected from FXML).
     * 
     * @param groupNotification The Group container for notification panels
     */
    public void setGroupNotification(Group groupNotification) {
        this.groupNotification = groupNotification;
    }
    
    /**
     * Sets the brick panel GridPane for rendering the current brick (typically injected from FXML).
     * 
     * @param brickPanel The GridPane for rendering the falling brick
     */
    public void setBrickPanel(GridPane brickPanel) {
        this.brickPanel = brickPanel;
    }
    
    /**
     * Sets the ghost panel GridPane for rendering the ghost piece (typically injected from FXML).
     * 
     * @param ghostPanel The GridPane for rendering the ghost piece preview
     */
    public void setGhostPanel(GridPane ghostPanel) {
        this.ghostPanel = ghostPanel;
    }
    
    /**
     * Sets the game over panel (typically injected from FXML).
     * 
     * @param gameOverPanel The GameOverPanel instance
     */
    public void setGameOverPanel(GameOverPanel gameOverPanel) {
        this.gameOverPanel = gameOverPanel;
    }
    
    /**
     * Sets the pause panel (typically injected from FXML).
     * 
     * @param pausePanel The PausePanel instance
     */
    public void setPausePanel(PausePanel pausePanel) {
        this.pausePanel = pausePanel;
    }
    
    /**
     * Sets the next bricks panel VBox (typically injected from FXML).
     * 
     * @param nextBricksPanel The VBox container for next brick previews
     */
    public void setNextBricksPanel(VBox nextBricksPanel) {
        this.nextBricksPanel = nextBricksPanel;
    }
    
    /**
     * Sets the hold brick panel GridPane (typically injected from FXML).
     * 
     * @param holdBrickPanel The GridPane for displaying the held brick
     */
    public void setHoldBrickPanel(GridPane holdBrickPanel) {
        this.holdBrickPanel = holdBrickPanel;
    }
    
    /**
     * Sets the score label (typically injected from FXML).
     * 
     * @param scoreLabel The Label for displaying the score
     */
    public void setScoreLabel(Label scoreLabel) {
        this.scoreLabel = scoreLabel;
    }
    
    /**
     * Sets the level label (typically injected from FXML).
     * 
     * @param levelLabel The Label for displaying the level
     */
    public void setLevelLabel(Label levelLabel) {
        this.levelLabel = levelLabel;
    }
    
    /**
     * Sets the lines label (typically injected from FXML).
     * 
     * @param linesLabel The Label for displaying the lines cleared
     */
    public void setLinesLabel(Label linesLabel) {
        this.linesLabel = linesLabel;
    }
    
    /**
     * Sets the countdown label (typically injected from FXML).
     * 
     * @param countdownLabel The Label for displaying the countdown
     */
    public void setCountdownLabel(Label countdownLabel) {
        this.countdownLabel = countdownLabel;
    }
    
    /**
     * Sets the timer label (typically injected from FXML).
     * 
     * @param timerLabel The Label for displaying the game timer
     */
    public void setTimerLabel(Label timerLabel) {
        this.timerLabel = timerLabel;
    }
    
    /**
     * Gets the brick panel GridPane.
     * 
     * @return The GridPane for rendering the current falling brick
     */
    public GridPane getBrickPanel() {
        return brickPanel;
    }
    
    /**
     * Gets the ghost panel GridPane.
     * 
     * @return The GridPane for rendering the ghost piece
     */
    public GridPane getGhostPanel() {
        return ghostPanel;
    }
    
    /**
     * Sets the event listener for handling game input events.
     * 
     * @param eventListener The InputEventListener to handle game events
     */
    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }
    
    /**
     * Gets the event listener for handling game input events.
     * 
     * @return The InputEventListener instance, or null if not set
     */
    public InputEventListener getEventListener() {
        return eventListener;
    }
    
    /**
     * Initializes the game panels (game panel, brick panel, and ghost panel).
     * Sets up grid constraints, creates display matrices, and configures panels
     * with proper sizing and transparency. The game panel is initialized with
     * a grid of transparent rectangles with gray borders representing the game
     * board. Brick and ghost panels are configured to be mouse-transparent so
     * they don't interfere with input handling.
     */
    public void initializeGamePanel() {
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

    /**
     * Initializes the hold brick panel.
     * Creates a 4x4 grid of transparent rectangles with gray borders for
     * displaying the held brick. The rectangles are slightly smaller than
     * the standard brick size (BRICK_SIZE - 10) for visual distinction.
     */
    public void initializeHoldPanel() {
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

    /**
     * Initializes the next bricks preview panel.
     * Creates three 4x4 grid panes for displaying the next three upcoming bricks.
     * The panes are initially hidden and will be shown when the game starts.
     * Each pane contains a grid of transparent rectangles for rendering brick shapes.
     */
    public void initializeNextBricksPanel() {
        if (nextBricksPanel != null) {
            nextBrickPanes.clear();
            nextBricksPanel.getChildren().clear();
            for (int i = 0; i < 3; i++) {
                GridPane pane = new GridPane();
                pane.setVgap(1);
                pane.setHgap(1);
                pane.setPrefSize(80, 80);
                // Set max size to prevent expansion when wide pieces (like I-piece) are displayed
                pane.setMaxSize(80, 80);
                pane.setMinSize(80, 80);
                // Hide panes initially - they'll be shown when game starts
                pane.setVisible(false);
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

    
    /**
     * Binds the score property to the score label.
     * Unbinds any existing binding and creates a new binding that formats
     * the score value as "Score: {value}".
     * 
     * @param score The IntegerProperty representing the score
     */
    public void bindScore(IntegerProperty score) {
        if (scoreLabel != null && score != null) {
            scoreLabel.textProperty().unbind();
            scoreLabel.textProperty().bind(score.asString("Score: %d"));
        }
    }

    /**
     * Binds the level property to the level label.
     * Unbinds any existing binding and creates a new binding that formats
     * the level value as "Level: {value}".
     * 
     * @param level The IntegerProperty representing the level
     */
    public void bindLevel(IntegerProperty level) {
        if (levelLabel != null && level != null) {
            levelLabel.textProperty().unbind();
            levelLabel.textProperty().bind(level.asString("Level: %d"));
        }
    }

    /**
     * Binds the lines property to the lines label.
     * Unbinds any existing binding and creates a new binding that formats
     * the lines value as "Lines: {value}".
     * 
     * @param lines The IntegerProperty representing the number of lines cleared
     */
    public void bindLines(IntegerProperty lines) {
        if (linesLabel != null && lines != null) {
            linesLabel.textProperty().unbind();
            linesLabel.textProperty().bind(lines.asString("Lines: %d"));
        }
    }
    
    /**
     * Gets the current brick data.
     * 
     * @return The ViewData containing the current brick information, or null if not set
     */
    public ViewData getCurrentBrickData() {
        return currentBrickData;
    }
    
    /**
     * Sets the current brick data.
     * 
     * @param currentBrickData The ViewData containing the current brick information
     */
    public void setCurrentBrickData(ViewData currentBrickData) {
        this.currentBrickData = currentBrickData;
    }
    
    /**
     * Gets the display matrix for the game board.
     * 
     * @return The 2D array of Rectangle objects representing the game board cells
     */
    public Rectangle[][] getDisplayMatrix() {
        return displayMatrix;
    }
    
    /**
     * Gets the list of next brick preview panes.
     * 
     * @return The list of GridPane objects for displaying upcoming bricks
     */
    public List<GridPane> getNextBrickPanes() {
        return nextBrickPanes;
    }
    
    /**
     * Gets the hold brick rectangles array.
     * 
     * @return The 2D array of Rectangle objects for rendering the held brick
     */
    public Rectangle[][] getHoldBrickRectangles() {
        return holdBrickRectangles;
    }
    
    /**
     * Clears all panels (display matrix, hold brick, next bricks) using the renderer.
     * Resets the game board display, hold brick display, and next bricks display
     * to their empty/transparent state. This is typically called when starting a
     * new game or resetting the display.
     */
    public void clearAllPanels() {
        // Clear display matrix
        renderer.clearDisplayMatrix(displayMatrix);
        
        // Clear hold brick display
        renderer.renderHoldBrick(null, holdBrickRectangles);
        
        // Clear next bricks display
        if (nextBrickPanes != null) {
            int brickSize = BRICK_SIZE - 10;
            renderer.clearNextBrickPanes(nextBrickPanes, brickSize);
        }
    }
}


