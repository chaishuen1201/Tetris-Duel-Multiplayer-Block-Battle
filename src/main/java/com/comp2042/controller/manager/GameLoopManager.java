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
 * Manages game loop timelines and automatic piece movement logic for both single player
 * and multiplayer modes. This class coordinates JavaFX Timeline instances that control
 * the automatic downward movement of game pieces, handles level-based speed adjustments,
 * manages garbage block processing in multiplayer mode, and orchestrates the countdown
 * sequence before game start. It integrates with game controllers, event listeners, and
 * callback interfaces to execute game logic and UI updates at appropriate intervals.
 * The manager ensures proper timing, state checking (pause, game over), and resource
 * management for all timeline operations.
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
    /**
     * Callback interface for move down operations in the game loop.
     * Defines methods that are called when pieces automatically move down during gameplay.
     */
    public interface MoveDownCallbacks {
        /**
         * Called when a piece moves down in multiplayer mode.
         * 
         * @param downData The data containing view updates and line clear information
         * @param playerNumber The player number (1 or 2) whose piece moved down
         */
        void onMultiplayerMoveDown(DownData downData, int playerNumber);
        
        /**
         * Called when a piece moves down in single player mode.
         * 
         * @param downData The data containing view updates and line clear information
         */
        void onSinglePlayerMoveDown(DownData downData);
        
        /**
         * Called to request focus on the game window.
         */
        void onRequestFocus();
        
        /**
         * Called to show a notification message to the player.
         * 
         * @param message The message to display
         */
        void onShowNotification(String message);
        
        /**
         * Called to play the line clear sound effect.
         */
        void onPlayLineClear();
    }
    
    /**
     * Callback interface for garbage processing operations.
     * Defines methods that are called when garbage blocks need to be processed
     * in multiplayer mode.
     */
    public interface GarbageProcessingCallbacks {
        /**
         * Called to process the garbage queue for a specific player.
         * 
         * @param playerNumber The player number (1 or 2) whose garbage queue should be processed
         */
        void processGarbageQueue(int playerNumber);
    }
    
    /**
     * Callback interface for countdown operations.
     * Defines methods that are called during the countdown sequence before game start.
     */
    public interface CountdownCallbacks {
        /**
         * Called when the countdown sequence completes and the game should start.
         */
        void onCountdownComplete();
        
        /**
         * Called for each countdown tick to update the displayed countdown number.
         * 
         * @param count The current countdown number (2, 1, or 0)
         */
        void onCountdownTick(int count);
    }
    
    private MoveDownCallbacks moveDownCallbacks;
    private GarbageProcessingCallbacks garbageProcessingCallbacks;
    private CountdownCallbacks countdownCallbacks;
    
    /**
     * Creates a new GameLoopManager with the specified game state manager.
     * 
     * @param gameStateManager The game state manager for checking game state
     */
    public GameLoopManager(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }
    
    // Setters for game components
    /**
     * Sets the single player timeline.
     * 
     * @param timeLine The Timeline for single player game loop
     */
    public void setTimeLine(Timeline timeLine) {
        this.timeLine = timeLine;
    }
    
    /**
     * Sets the player 1 timeline for multiplayer mode.
     * 
     * @param timeLine1 The Timeline for player 1's game loop
     */
    public void setTimeLine1(Timeline timeLine1) {
        this.timeLine1 = timeLine1;
    }
    
    /**
     * Sets the player 2 timeline for multiplayer mode.
     * 
     * @param timeLine2 The Timeline for player 2's game loop
     */
    public void setTimeLine2(Timeline timeLine2) {
        this.timeLine2 = timeLine2;
    }
    
    /**
     * Sets the garbage processing timeline for player 1.
     * 
     * @param garbageProcessTimeline1 The Timeline for processing player 1's garbage queue
     */
    public void setGarbageProcessTimeline1(Timeline garbageProcessTimeline1) {
        this.garbageProcessTimeline1 = garbageProcessTimeline1;
    }
    
    /**
     * Sets the garbage processing timeline for player 2.
     * 
     * @param garbageProcessTimeline2 The Timeline for processing player 2's garbage queue
     */
    public void setGarbageProcessTimeline2(Timeline garbageProcessTimeline2) {
        this.garbageProcessTimeline2 = garbageProcessTimeline2;
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
     * Sets the event listener for single player mode.
     * 
     * @param eventListener The InputEventListener for single player
     */
    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }
    
    /**
     * Sets the event listener for player 1.
     * 
     * @param eventListener1 The InputEventListener for player 1
     */
    public void setEventListener1(InputEventListener eventListener1) {
        this.eventListener1 = eventListener1;
    }
    
    /**
     * Sets the event listener for player 2.
     * 
     * @param eventListener2 The InputEventListener for player 2
     */
    public void setEventListener2(InputEventListener eventListener2) {
        this.eventListener2 = eventListener2;
    }
    
    /**
     * Sets the current game level.
     * 
     * @param currentLevel The current level (affects game speed)
     */
    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }
    
    /**
     * Sets the callbacks for move down operations.
     * 
     * @param callbacks The MoveDownCallbacks implementation
     */
    public void setMoveDownCallbacks(MoveDownCallbacks callbacks) {
        this.moveDownCallbacks = callbacks;
    }
    
    /**
     * Sets the callbacks for garbage processing operations.
     * 
     * @param callbacks The GarbageProcessingCallbacks implementation
     */
    public void setGarbageProcessingCallbacks(GarbageProcessingCallbacks callbacks) {
        this.garbageProcessingCallbacks = callbacks;
    }
    
    /**
     * Sets the callbacks for countdown operations.
     * 
     * @param callbacks The CountdownCallbacks implementation
     */
    public void setCountdownCallbacks(CountdownCallbacks callbacks) {
        this.countdownCallbacks = callbacks;
    }
    
    // Getters for timelines
    /**
     * Gets the single player timeline.
     * 
     * @return The Timeline for single player game loop
     */
    public Timeline getTimeLine() {
        return timeLine;
    }
    
    /**
     * Gets the player 1 timeline for multiplayer mode.
     * 
     * @return The Timeline for player 1's game loop
     */
    public Timeline getTimeLine1() {
        return timeLine1;
    }
    
    /**
     * Gets the player 2 timeline for multiplayer mode.
     * 
     * @return The Timeline for player 2's game loop
     */
    public Timeline getTimeLine2() {
        return timeLine2;
    }
    
    /**
     * Gets the garbage processing timeline for player 1.
     * 
     * @return The Timeline for processing player 1's garbage queue
     */
    public Timeline getGarbageProcessTimeline1() {
        return garbageProcessTimeline1;
    }
    
    /**
     * Gets the garbage processing timeline for player 2.
     * 
     * @return The Timeline for processing player 2's garbage queue
     */
    public Timeline getGarbageProcessTimeline2() {
        return garbageProcessTimeline2;
    }
    
    /**
     * Gets the countdown timeline.
     * 
     * @return The Timeline for countdown sequence
     */
    public Timeline getCountdownTimeline() {
        return countdownTimeline;
    }
    
    /**
     * Creates and starts the single player game loop timeline.
     * Stops any existing timeline, creates a new Timeline that triggers automatic
     * piece movement every 400 milliseconds, sets it to loop indefinitely, and
     * updates the timeline rate based on the current level. The timeline will
     * continue running until explicitly stopped.
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
     * 
     * @param playerNumber The player number (1 or 2) for which to create the timeline
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
     * Handles automatic piece movement down for single player mode.
     * Delegates to the main moveDown method with player number 0 (single player).
     * 
     * @param event The move event containing event type and source information
     */
    public void moveDown(MoveEvent event) {
        moveDown(event, 0);
    }
    
    /**
     * Handles automatic piece movement down for single player or multiplayer mode.
     * Checks game state (started, paused, game over) before processing. If conditions
     * are met, triggers the appropriate event listener to move the piece down, processes
     * line clear events if lines were removed, and invokes callbacks to update the UI
     * and play sound effects. For single player, also requests focus on the game window.
     * 
     * @param event The move event containing event type and source information
     * @param playerNumber The player number (0 for single player, 1 or 2 for multiplayer)
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
     * Updates the timeline rate based on current level for single player mode.
     * Delegates to the main updateTimelineRate method with player number 0.
     * The rate increases by 25% for each level above 1, making the game faster
     * as the player progresses.
     */
    public void updateTimelineRate() {
        updateTimelineRate(0);
    }
    
    /**
     * Updates the timeline rate based on current level for single player or multiplayer mode.
     * Calculates the speed multiplier based on the current level (rate = 1.0 + (level - 1) * 0.25),
     * so each level increases speed by 25%. For multiplayer, retrieves the level from the
     * player's board. For single player, uses the stored currentLevel value.
     * 
     * @param playerNumber The player number (0 for single player, 1 or 2 for multiplayer)
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
     * Stops garbage processing timelines for both players in multiplayer mode.
     * Stops both player 1 and player 2 garbage processing timelines if they are running.
     * Does nothing if the timelines are not initialized or already stopped.
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
     * Creates and starts the countdown timeline for the pre-game countdown sequence.
     * Stops any existing countdown timeline, then creates a new Timeline that displays
     * countdown numbers (3, 2, 1) over 3 seconds. Each number is shown for 1 second,
     * with callbacks triggered for each tick. After 3 seconds, the completion callback
     * is invoked to start the game. The timeline plays automatically when created.
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
     * Stops the countdown timeline if it is currently running.
     * Does nothing if the countdown timeline is not initialized or already stopped.
     */
    public void stopCountdownTimeline() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
    }
}

