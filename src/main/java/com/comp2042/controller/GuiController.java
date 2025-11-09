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
import com.comp2042.view.NotificationPanel;
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
    private static final int GAME_BOARD_WIDTH = GAME_PANEL_WIDTH + 18;
    private static final int GAME_BOARD_HEIGHT = GAME_PANEL_HEIGHT + 18;
    private static final double SOFT_DROP_RATE = 12.0;

    @FXML private BorderPane gameBoard;
    @FXML private StackPane gameStack;
    @FXML private GridPane gamePanel;
    @FXML private Group groupNotification;
    @FXML private GridPane brickPanel;
    @FXML private GameOverPanel gameOverPanel;
    @FXML private VBox nextBricksPanel;
    @FXML private GridPane holdBrickPanel;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label linesLabel;
    @FXML private Button pauseButton;

    private Rectangle[][] displayMatrix;
    private InputEventListener eventListener;
    private Timeline timeLine;
    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);
    private int currentLevel = 1;

    private Rectangle[][] holdBrickRectangles;
    private List<GridPane> nextBrickPanes = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
        } catch (Exception e) {
            System.out.println("Font not found, using default font");
        }

        initializeGamePanel();
        initializeHoldPanel();
        initializeNextBricksPanel();
        initializeInfoPanel();

        if (gameOverPanel != null) gameOverPanel.setVisible(false);

        if (gameBoard != null) {
            gameBoard.setFocusTraversable(true);
            gameBoard.requestFocus();
            gameBoard.setOnKeyPressed(this::handleKeyPress);
            gameBoard.setOnKeyReleased(this::handleKeyRelease);
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
        if (scoreLabel != null) scoreLabel.setText("0");
        if (levelLabel != null) levelLabel.setText("1");
        if (linesLabel != null) linesLabel.setText("0");
    }

    private void handleKeyPress(KeyEvent keyEvent) {
        if (!isPause.get() && !isGameOver.get()) {
            switch (keyEvent.getCode()) {
                case LEFT, A -> { if (eventListener != null) refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER))); }
                case RIGHT, D -> { if (eventListener != null) refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER))); }
                case UP, W -> { if (eventListener != null) refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER))); }
                case DOWN, S -> { if (timeLine != null) timeLine.setRate(SOFT_DROP_RATE); moveDown(new MoveEvent(EventType.DOWN, EventSource.USER)); }
                case SPACE -> { if (eventListener != null) refreshBrick(eventListener.onHardDropEvent(new MoveEvent(EventType.HARD_DROP, EventSource.USER))); }
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
        timeLine.play();
        updateTimelineRate();

        if (gameBoard != null) gameBoard.requestFocus();
    }

    private javafx.scene.paint.Paint getFillColor(int brickType) {
        return ColorStrategy.getColorForBrickType(brickType);
    }

    private void refreshBrick(ViewData brick) {
        if (!isPause.get() && brickPanel != null && brick != null) {
            brickPanel.getChildren().clear();
            int[][] data = brick.getBrickData();
            int offsetX = brick.getXPosition();
            int offsetY = brick.getYPosition();
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
        if (!isPause.get() && eventListener != null) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData != null) {
                if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0)
                    showNotification("+" + downData.getClearRow().getScoreBonus());
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
        if (scoreLabel != null && score != null) scoreLabel.textProperty().bind(score.asString("Score: %d"));
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
        if (gameOverPanel != null) gameOverPanel.setVisible(true);
        isGameOver.set(true);
    }

    public void newGame(ActionEvent actionEvent) {
        if (timeLine != null) timeLine.stop();
        if (gameOverPanel != null) gameOverPanel.setVisible(false);
        if (eventListener != null) eventListener.createNewGame();
        if (gameBoard != null) gameBoard.requestFocus();
        if (timeLine != null) timeLine.play();
        isPause.set(false);
        isGameOver.set(false);
        if (pauseButton != null) {
            pauseButton.setText("Pause");
        }
    }

    public void pauseGame(ActionEvent actionEvent) {
        if (!isPause.get()) {
            if (timeLine != null) timeLine.pause();
            isPause.set(true);
            if (pauseButton != null) {
                pauseButton.setText("Resume");
            }
        } else {
            if (timeLine != null) { timeLine.play(); updateTimelineRate(); }
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
