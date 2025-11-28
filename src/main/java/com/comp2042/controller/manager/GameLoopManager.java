package com.comp2042.controller.manager;

import com.comp2042.controller.GameController;
import com.comp2042.event.EventSource;
import com.comp2042.event.EventType;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.model.DownData;
import com.comp2042.model.SimpleBoard;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Manages game loop timelines and movement logic.
 * Handles single player and multiplayer game loops, garbage processing, and countdown.
 */
public class GameLoopManager {
    
    // Timeline fields
    private Timeline timeLine;
    private Timeline timeLine1;
    private Timeline timeLine2;
    private Timeline garbageProcessTimeline1;
    private Timeline garbageProcessTimeline2;
    private Timeline countdownTimeline;
    
    // Dependencies
    private final GameStateManager gameStateManager;
    
    // Game component references (set via setters)
    private GameController gameController1;
    private GameController gameController2;
    private InputEventListener eventListener;
    private InputEventListener eventListener1;
    private InputEventListener eventListener2;
    private int currentLevel = 1;
    
    // Callbacks for UI updates and game logic
    public interface MoveDownCallbacks {
        void onMultiplayerMoveDown(DownData downData, int playerNumber);
        void onSinglePlayerMoveDown(DownData downData);
        void onRequestFocus();
        void onShowNotification(String message);
        void onPlayLineClear();
    }
    
    public interface GarbageProcessingCallbacks {
        void processGarbageQueue(int playerNumber);
    }
    
    public interface CountdownCallbacks {
        void onCountdownComplete();
        void onCountdownTick(int count);
    }
    
    private MoveDownCallbacks moveDownCallbacks;
    private GarbageProcessingCallbacks garbageProcessingCallbacks;
    private CountdownCallbacks countdownCallbacks;
    
    public GameLoopManager(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }
    
    // Setters for game components
    public void setTimeLine(Timeline timeLine) {
        this.timeLine = timeLine;
    }
    
    public void setTimeLine1(Timeline timeLine1) {
        this.timeLine1 = timeLine1;
    }
    
    public void setTimeLine2(Timeline timeLine2) {
        this.timeLine2 = timeLine2;
    }
    
    public void setGarbageProcessTimeline1(Timeline garbageProcessTimeline1) {
        this.garbageProcessTimeline1 = garbageProcessTimeline1;
    }
    
    public void setGarbageProcessTimeline2(Timeline garbageProcessTimeline2) {
        this.garbageProcessTimeline2 = garbageProcessTimeline2;
    }
    
    public void setGameController1(GameController gameController1) {
        this.gameController1 = gameController1;
    }
    
    public void setGameController2(GameController gameController2) {
        this.gameController2 = gameController2;
    }
    
    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }
    
    public void setEventListener1(InputEventListener eventListener1) {
        this.eventListener1 = eventListener1;
    }
    
    public void setEventListener2(InputEventListener eventListener2) {
        this.eventListener2 = eventListener2;
    }
    
    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }
    
    public void setMoveDownCallbacks(MoveDownCallbacks callbacks) {
        this.moveDownCallbacks = callbacks;
    }
    
    public void setGarbageProcessingCallbacks(GarbageProcessingCallbacks callbacks) {
        this.garbageProcessingCallbacks = callbacks;
    }
    
    public void setCountdownCallbacks(CountdownCallbacks callbacks) {
        this.countdownCallbacks = callbacks;
    }
    
    // Getters for timelines
    public Timeline getTimeLine() {
        return timeLine;
    }
    
    public Timeline getTimeLine1() {
        return timeLine1;
    }
    
    public Timeline getTimeLine2() {
        return timeLine2;
    }
    
    public Timeline getGarbageProcessTimeline1() {
        return garbageProcessTimeline1;
    }
    
    public Timeline getGarbageProcessTimeline2() {
        return garbageProcessTimeline2;
    }
    
    public Timeline getCountdownTimeline() {
        return countdownTimeline;
    }
    
    /**
     * Creates and starts the single player game loop timeline.
     */
    public void createSinglePlayerTimeline() {
        if (timeLine != null) {
            timeLine.stop();
        }
        timeLine = new Timeline(new KeyFrame(Duration.millis(400), ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        updateTimelineRate();
    }
    
    /**
     * Creates and starts the multiplayer game loop timeline for a specific player.
     */
    public void createMultiplayerTimeline(int playerNumber) {
        if (playerNumber == 1) {
            if (timeLine1 != null) {
                timeLine1.stop();
            }
            timeLine1 = new Timeline(new KeyFrame(Duration.millis(400), ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD), 1)));
            timeLine1.setCycleCount(Timeline.INDEFINITE);
            updateTimelineRate(1);
        } else if (playerNumber == 2) {
            if (timeLine2 != null) {
                timeLine2.stop();
            }
            timeLine2 = new Timeline(new KeyFrame(Duration.millis(400), ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD), 2)));
            timeLine2.setCycleCount(Timeline.INDEFINITE);
            updateTimelineRate(2);
        }
    }
    
    /**
     * Handles automatic piece movement down for single player.
     */
    public void moveDown(MoveEvent event) {
        moveDown(event, 0);
    }
    
    /**
     * Handles automatic piece movement down for single player or multiplayer.
     */
    public void moveDown(MoveEvent event, int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0) {
            boolean isGameOver = (playerNumber == 1) ? gameStateManager.isGameOver1() : gameStateManager.isGameOver2();
            InputEventListener listener = (playerNumber == 1) ? eventListener1 : eventListener2;
            
            if (!gameStateManager.isGameStarted() || gameStateManager.isPaused() || isGameOver) {
                return;
            }
            if (listener != null) {
                DownData downData = listener.onDownEvent(event);
                if (downData != null) {
                    if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                        // Play line clear sound
                        if (moveDownCallbacks != null) {
                            moveDownCallbacks.onPlayLineClear();
                        }
                    }
                    if (moveDownCallbacks != null) {
                        moveDownCallbacks.onMultiplayerMoveDown(downData, playerNumber);
                    }
                }
            }
        } else {
            if (!gameStateManager.isGameStarted() || gameStateManager.isPaused() || gameStateManager.isGameOver()) {
                return;
            }
            if (eventListener != null) {
                DownData downData = eventListener.onDownEvent(event);
                if (downData != null) {
                    if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                        if (moveDownCallbacks != null) {
                            moveDownCallbacks.onShowNotification("+" + downData.getClearRow().getScoreBonus());
                            moveDownCallbacks.onPlayLineClear();
                        }
                    }
                    if (moveDownCallbacks != null) {
                        moveDownCallbacks.onSinglePlayerMoveDown(downData);
                    }
                }
            }
            if (moveDownCallbacks != null) {
                moveDownCallbacks.onRequestFocus();
            }
        }
    }
    
    /**
     * Updates the timeline rate based on current level for single player.
     */
    public void updateTimelineRate() {
        updateTimelineRate(0);
    }
    
    /**
     * Updates the timeline rate based on current level for single player or multiplayer.
     */
    public void updateTimelineRate(int playerNumber) {
        if (gameStateManager.isMultiplayerMode() && playerNumber > 0) {
            Timeline timeline = (playerNumber == 1) ? timeLine1 : timeLine2;
            GameController controller = (playerNumber == 1) ? gameController1 : gameController2;
            
            if (timeline != null && controller != null && controller.getBoard() instanceof SimpleBoard) {
                SimpleBoard board = (SimpleBoard) controller.getBoard();
                int level = board.levelProperty().get();
                double rate = 1.0 + (Math.max(1, level) - 1) * 0.25;
                timeline.setRate(rate);
            }
        } else {
            if (timeLine != null) {
                double rate = 1.0 + (Math.max(1, currentLevel) - 1) * 0.25;
                timeLine.setRate(rate);
            }
        }
    }
    
    /**
     * Starts garbage processing timelines for both players in multiplayer mode.
     * Garbage lines appear gradually (one every 2 seconds) to give players time to react.
     */
    public void startGarbageProcessingTimelines() {
        if (!gameStateManager.isMultiplayerMode()) {
            return;
        }
        
        // Stop existing timelines if any
        if (garbageProcessTimeline1 != null) {
            garbageProcessTimeline1.stop();
        }
        if (garbageProcessTimeline2 != null) {
            garbageProcessTimeline2.stop();
        }
        
        // Create timeline for player 1 - process garbage every 2 seconds
        garbageProcessTimeline1 = new Timeline(new KeyFrame(Duration.seconds(2), ae -> {
            if (!gameStateManager.isPaused() && !gameStateManager.isGameOver1()) {
                if (garbageProcessingCallbacks != null) {
                    garbageProcessingCallbacks.processGarbageQueue(1);
                }
            }
        }));
        garbageProcessTimeline1.setCycleCount(Timeline.INDEFINITE);
        
        // Create timeline for player 2 - process garbage every 2 seconds
        garbageProcessTimeline2 = new Timeline(new KeyFrame(Duration.seconds(2), ae -> {
            if (!gameStateManager.isPaused() && !gameStateManager.isGameOver2()) {
                if (garbageProcessingCallbacks != null) {
                    garbageProcessingCallbacks.processGarbageQueue(2);
                }
            }
        }));
        garbageProcessTimeline2.setCycleCount(Timeline.INDEFINITE);
        
        // Start both timelines
        garbageProcessTimeline1.play();
        garbageProcessTimeline2.play();
    }
    
    /**
     * Stops garbage processing timelines.
     */
    public void stopGarbageProcessingTimelines() {
        if (garbageProcessTimeline1 != null) {
            garbageProcessTimeline1.stop();
        }
        if (garbageProcessTimeline2 != null) {
            garbageProcessTimeline2.stop();
        }
    }
    
    /**
     * Creates and starts the countdown timeline.
     * The timeline will call callbacks for each countdown tick and completion.
     */
    public void createCountdownTimeline() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        
        countdownTimeline = new Timeline();
        
        // Countdown from 3 to 1 (each number shows for 1 second)
        for (int i = 3; i >= 1; i--) {
            final int count = i;
            KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(3 - count + 1), 
                e -> {
                    if (countdownCallbacks != null && count > 1) {
                        countdownCallbacks.onCountdownTick(count - 1);
                    }
                }
            );
            countdownTimeline.getKeyFrames().add(keyFrame);
        }
        
        // After countdown, start the game
        KeyFrame startGameFrame = new KeyFrame(
            Duration.seconds(3),
            e -> {
                if (countdownCallbacks != null) {
                    countdownCallbacks.onCountdownComplete();
                }
            }
        );
        countdownTimeline.getKeyFrames().add(startGameFrame);
        
        countdownTimeline.play();
    }
    
    /**
     * Stops the countdown timeline.
     */
    public void stopCountdownTimeline() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
    }
}

