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
    
    public InputEventListener getEventListener() {
        return eventListener;
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
    
    /**
     * Clears all panels (display matrix, hold brick, next bricks) using the renderer.
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

