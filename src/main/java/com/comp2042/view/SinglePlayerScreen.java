package com.comp2042.view;

import com.comp2042.controller.GameConstants;
import com.comp2042.controller.GameController;
import com.comp2042.event.InputEventListener;
import com.comp2042.logic.bricks.Brick;
import com.comp2042.model.SimpleBoard;
import com.comp2042.model.ViewData;
import javafx.beans.property.IntegerProperty;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

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
    
    public SinglePlayerScreen() {
        // Components will be injected via setters
    }
    
    // Setters for FXML-injected components
    public void setGameBoard(BorderPane gameBoard) {
        this.gameBoard = gameBoard;
    }
    
    public void setGameStack(StackPane gameStack) {
        this.gameStack = gameStack;
    }
    
    public void setGamePanel(GridPane gamePanel) {
        this.gamePanel = gamePanel;
    }
    
    public void setGroupNotification(Group groupNotification) {
        this.groupNotification = groupNotification;
    }
    
    public void setBrickPanel(GridPane brickPanel) {
        this.brickPanel = brickPanel;
    }
    
    public void setGhostPanel(GridPane ghostPanel) {
        this.ghostPanel = ghostPanel;
    }
    
    public void setGameOverPanel(GameOverPanel gameOverPanel) {
        this.gameOverPanel = gameOverPanel;
    }
    
    public void setPausePanel(PausePanel pausePanel) {
        this.pausePanel = pausePanel;
    }
    
    public void setNextBricksPanel(VBox nextBricksPanel) {
        this.nextBricksPanel = nextBricksPanel;
    }
    
    public void setHoldBrickPanel(GridPane holdBrickPanel) {
        this.holdBrickPanel = holdBrickPanel;
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
    
    public void setCountdownLabel(Label countdownLabel) {
        this.countdownLabel = countdownLabel;
    }
    
    public void setTimerLabel(Label timerLabel) {
        this.timerLabel = timerLabel;
    }
    
    // Getters for components that GuiController needs
    public BorderPane getGameBoard() {
        return gameBoard;
    }
    
    public StackPane getGameStack() {
        return gameStack;
    }
    
    public GridPane getGamePanel() {
        return gamePanel;
    }
    
    public Group getGroupNotification() {
        return groupNotification;
    }
    
    public GridPane getBrickPanel() {
        return brickPanel;
    }
    
    public GridPane getGhostPanel() {
        return ghostPanel;
    }
    
    public GameOverPanel getGameOverPanel() {
        return gameOverPanel;
    }
    
    public PausePanel getPausePanel() {
        return pausePanel;
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
    
    public Label getCountdownLabel() {
        return countdownLabel;
    }
    
    public Label getTimerLabel() {
        return timerLabel;
    }
    
    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }
    
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

    public void initializeNextBricksPanel() {
        if (nextBricksPanel != null) {
            nextBrickPanes.clear();
            nextBricksPanel.getChildren().clear();
            for (int i = 0; i < 3; i++) {
                GridPane pane = new GridPane();
                pane.setVgap(1);
                pane.setHgap(1);
                pane.setPrefSize(80, 80);
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

    public void initializeInfoPanel() {
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
    
    public void refreshBrick(ViewData brick, boolean gameStarted) {
        // Always store the current brick data
        if (brick != null) {
            currentBrickData = brick;
        }

        // Don't show brick if game hasn't started (menu is visible)
        if (!gameStarted) {
            return;
        }

        GridPane currentBrickPanel = brickPanel;
        GridPane currentGhostPanel = ghostPanel;
        InputEventListener currentEventListener = eventListener;
        int scaledBrickSize = BRICK_SIZE;

        if (currentBrickPanel != null && brick != null) {
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
                                        javafx.scene.paint.Paint brickColor = ColorStrategy.getColorForBrickType(data[j][i]);
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
                        rect.setFill(ColorStrategy.getColorForBrickType(data[j][i]));
                        rect.setArcHeight(5);
                        rect.setArcWidth(5);
                        currentBrickPanel.add(rect, offsetX + i, offsetY + j);
                    }
                }
            }
        }
    }
    
    public void refreshGameBackground(int[][] board) {
        if (displayMatrix != null) {
            for (int i = 0; i < Math.min(BOARD_HEIGHT, board.length); i++) {
                for (int j = 0; j < Math.min(BOARD_WIDTH, board[i].length); j++) {
                    if (displayMatrix[i][j] != null) {
                        displayMatrix[i][j].setFill(ColorStrategy.getColorForBrickType(board[i][j]));
                    }
                }
            }
        }
    }
    
    public void updateNextBricks(List<Brick> nextBricks, boolean gameStarted) {
        if (nextBrickPanes == null || nextBrickPanes.isEmpty()) return;
        int brickSize = BRICK_SIZE - 10;
        int maxBricks = nextBrickPanes.size();
        
        for (int i = 0; i < Math.min(maxBricks, nextBrickPanes.size()); i++) {
            GridPane pane = nextBrickPanes.get(i);
            if (pane == null) continue;
            pane.getChildren().clear();
            // Only make pane visible when game has started (after countdown)
            // Don't show bricks during main menu or countdown
            if (gameStarted) {
                pane.setVisible(true);
            }
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
                        holdBrickRectangles[i][j].setFill(ColorStrategy.getColorForBrickType(shape[i][j]));
        }
    }
    
    public void bindScore(IntegerProperty score) {
        if (scoreLabel != null && score != null) {
            scoreLabel.textProperty().unbind();
            scoreLabel.textProperty().bind(score.asString("Score: %d"));
        }
    }

    public void bindLevel(IntegerProperty level) {
        if (levelLabel != null && level != null) {
            levelLabel.textProperty().unbind();
            levelLabel.textProperty().bind(level.asString("Level: %d"));
        }
    }

    public void bindLines(IntegerProperty lines) {
        if (linesLabel != null && lines != null) {
            linesLabel.textProperty().unbind();
            linesLabel.textProperty().bind(lines.asString("Lines: %d"));
        }
    }
    
    public ViewData getCurrentBrickData() {
        return currentBrickData;
    }
    
    public void setCurrentBrickData(ViewData currentBrickData) {
        this.currentBrickData = currentBrickData;
    }
    
    public Rectangle[][] getDisplayMatrix() {
        return displayMatrix;
    }
    
    public List<GridPane> getNextBrickPanes() {
        return nextBrickPanes;
    }
    
    public Rectangle[][] getHoldBrickRectangles() {
        return holdBrickRectangles;
    }
}

