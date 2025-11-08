package com.comp2042.controller;

import com.comp2042.event.EventSource;
import com.comp2042.model.DownData;
import com.comp2042.event.EventType;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.model.ViewData;
import com.comp2042.view.ColorStrategy;
import com.comp2042.view.GameOverPanel;
import com.comp2042.view.GameView;
import com.comp2042.view.NotificationPanel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable, GameView {

    private static final int BRICK_SIZE = 20;
    private static final int BRICK_OFFSET_Y = -42;
    private static final int BOARD_OFFSET = 2;
    private static final int GAME_SPEED_MS = 400;

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane brickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    private Rectangle[][] displayMatrix;
    private InputEventListener eventListener;
    private Rectangle[][] rectangles;
    private Timeline gameTimeline;

    private final BooleanProperty isPaused = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();
        setupKeyHandlers();
        gameOverPanel.setVisible(false);
        setupReflection();
    }

    private void setupKeyHandlers() {
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (!isPaused.get() && !isGameOver.get()) {
                    handleGameKeyPress(keyEvent);
                }
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame();
                }
            }
        });
    }

    private void handleGameKeyPress(KeyEvent keyEvent) {
        KeyCode code = keyEvent.getCode();
        if (code == KeyCode.LEFT || code == KeyCode.A) {
            refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
            keyEvent.consume();
        } else if (code == KeyCode.RIGHT || code == KeyCode.D) {
            refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
            keyEvent.consume();
        } else if (code == KeyCode.UP || code == KeyCode.W) {
            refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
            keyEvent.consume();
        } else if (code == KeyCode.DOWN || code == KeyCode.S) {
            moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
            keyEvent.consume();
        }
    }

    private void setupReflection() {
        final Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);
    }

    @Override
    public void initGameView(int[][] boardMatrix, ViewData brick) {
        initializeDisplayMatrix(boardMatrix);
        initializeBrickRectangles(brick);
        positionBrickPanel(brick);
        startGameTimeline();
    }

    private void initializeDisplayMatrix(int[][] boardMatrix) {
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = BOARD_OFFSET; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - BOARD_OFFSET);
            }
        }
    }

    private void initializeBrickRectangles(ViewData brick) {
        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(ColorStrategy.getColorForBrickType(brick.getBrickData()[i][j]));
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
            }
        }
    }

    private void positionBrickPanel(ViewData brick) {
        brickPanel.setLayoutX(gamePanel.getLayoutX() + brick.getXPosition() * brickPanel.getVgap() + brick.getXPosition() * BRICK_SIZE);
        brickPanel.setLayoutY(BRICK_OFFSET_Y + gamePanel.getLayoutY() + brick.getYPosition() * brickPanel.getHgap() + brick.getYPosition() * BRICK_SIZE);
    }

    private void startGameTimeline() {
        gameTimeline = new Timeline(new KeyFrame(
                Duration.millis(GAME_SPEED_MS),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        gameTimeline.setCycleCount(Timeline.INDEFINITE);
        gameTimeline.play();
    }

    @Override
    public void refreshBrick(ViewData brick) {
        if (!isPaused.get()) {
            updateBrickPosition(brick);
            updateBrickVisuals(brick);
        }
    }

    private void updateBrickPosition(ViewData brick) {
        brickPanel.setLayoutX(gamePanel.getLayoutX() + brick.getXPosition() * brickPanel.getVgap() + brick.getXPosition() * BRICK_SIZE);
        brickPanel.setLayoutY(BRICK_OFFSET_Y + gamePanel.getLayoutY() + brick.getYPosition() * brickPanel.getHgap() + brick.getYPosition() * BRICK_SIZE);
    }

    private void updateBrickVisuals(ViewData brick) {
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                setRectangleData(brick.getBrickData()[i][j], rectangles[i][j]);
            }
        }
    }

    @Override
    public void refreshGameBackground(int[][] board) {
        for (int i = BOARD_OFFSET; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
            }
        }
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        rectangle.setFill(ColorStrategy.getColorForBrickType(color));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    private void moveDown(MoveEvent event) {
        if (!isPaused.get()) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                showScoreNotification(downData.getClearRow().getScoreBonus());
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    private void showScoreNotification(int scoreBonus) {
        NotificationPanel notificationPanel = new NotificationPanel("+" + scoreBonus);
        groupNotification.getChildren().add(notificationPanel);
        notificationPanel.showScore(groupNotification.getChildren());
    }

    @Override
    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public void bindScore(IntegerProperty integerProperty) {
        // Score binding can be implemented here if needed
    }

    @Override
    public void gameOver() {
        gameTimeline.stop();
        gameOverPanel.setVisible(true);
        isGameOver.set(true);
    }

    @Override
    public void newGame() {
        gameTimeline.stop();
        gameOverPanel.setVisible(false);
        if (eventListener != null) {
            eventListener.createNewGame();
        }
        gamePanel.requestFocus();
        gameTimeline.play();
        isPaused.set(false);
        isGameOver.set(false);
    }

    public void pauseGame(ActionEvent actionEvent) {
        gamePanel.requestFocus();
    }
}

