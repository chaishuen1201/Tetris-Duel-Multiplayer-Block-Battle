package com.comp2042.controller.input;

import com.comp2042.event.EventSource;
import com.comp2042.event.EventType;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.model.DownData;
import com.comp2042.model.ViewData;
import com.comp2042.util.KeyBindingsManager;
import com.comp2042.view.SettingsPanel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

/**
 * Handles all keyboard input for the game.
 * Separates input handling logic from GUI controller.
 */
public class InputHandler {
    
    private static final double SOFT_DROP_RATE = 2.0;
    
    private final KeyBindingsManager keyBindingsManager;
    
    // Callbacks for single player actions
    private SinglePlayerCallbacks singlePlayerCallbacks;
    
    // Callbacks for multiplayer actions
    private MultiplayerCallbacks multiplayerCallbacks;
    
    // State flags for hard drop processing
    private boolean isHardDropProcessing1 = false;
    private boolean isHardDropProcessing2 = false;
    
    // Settings panel reference for rebinding
    private SettingsPanel settingsPanel;
    
    /**
     * Creates an InputHandler with required dependencies.
     * Uses constructor injection to follow Dependency Inversion Principle.
     * 
     * @param keyBindingsManager The key bindings manager for resolving key mappings
     */
    public InputHandler(KeyBindingsManager keyBindingsManager) {
        if (keyBindingsManager == null) {
            throw new IllegalArgumentException("KeyBindingsManager cannot be null");
        }
        this.keyBindingsManager = keyBindingsManager;
    }
    
    public void setSettingsPanel(SettingsPanel settingsPanel) {
        this.settingsPanel = settingsPanel;
    }
    
    public void setSinglePlayerCallbacks(SinglePlayerCallbacks callbacks) {
        this.singlePlayerCallbacks = callbacks;
    }
    
    public void setMultiplayerCallbacks(MultiplayerCallbacks callbacks) {
        this.multiplayerCallbacks = callbacks;
    }
    
    /**
     * Handles key press events for both single and multiplayer modes.
     */
    public void handleKeyPress(KeyEvent keyEvent, boolean isMultiplayerMode, boolean gameStarted) {
        // Check if settings panel is visible and rebinding is active
        if (settingsPanel != null && settingsPanel.isVisible() && settingsPanel.isRebinding()) {
            settingsPanel.handleRebindingKey(keyEvent.getCode());
            keyEvent.consume();
            return;
        }
        
        KeyCode keyCode = keyEvent.getCode();
        
        // Handle pause key (only when game is playing)
        KeyCode pauseKey = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.SINGLE, KeyBindingsManager.Action.PAUSE);
        if (pauseKey != null && keyCode == pauseKey) {
            if (gameStarted && singlePlayerCallbacks != null && !singlePlayerCallbacks.isGameOver()) {
                if (isMultiplayerMode) {
                    // For multiplayer, also check that neither player has lost
                    if (multiplayerCallbacks != null && !multiplayerCallbacks.isGameOver1() && !multiplayerCallbacks.isGameOver2()) {
                        multiplayerCallbacks.pauseGame();
                    }
                } else {
                    singlePlayerCallbacks.pauseGame();
                }
            }
            keyEvent.consume();
            return;
        }
        
        // Handle multiplayer mode
        if (isMultiplayerMode) {
            if (!gameStarted) {
                // Handle ready state - use fixed default keys (SPACE for player 1, ENTER for player 2)
                if (multiplayerCallbacks != null) {
                    if (keyCode == KeyCode.SPACE && !multiplayerCallbacks.isPlayer1Ready()) {
                        multiplayerCallbacks.setPlayer1Ready(true);
                        multiplayerCallbacks.updateReadyLabels();
                        multiplayerCallbacks.checkBothReady();
                        keyEvent.consume();
                        return;
                    } else if (keyCode == KeyCode.ENTER && !multiplayerCallbacks.isPlayer2Ready()) {
                        multiplayerCallbacks.setPlayer2Ready(true);
                        multiplayerCallbacks.updateReadyLabels();
                        multiplayerCallbacks.checkBothReady();
                        keyEvent.consume();
                        return;
                    }
                }
                return;
            }
            handleMultiplayerKeyPress(keyEvent);
            return;
        }
        
        // Single player mode
        if (!gameStarted) {
            // If game hasn't started, Enter key can start it (or NEW_GAME key if set)
            KeyCode newGameKey = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.SINGLE, KeyBindingsManager.Action.NEW_GAME);
            if (newGameKey == null) newGameKey = KeyCode.ENTER;
            if (keyCode == newGameKey && singlePlayerCallbacks != null) {
                singlePlayerCallbacks.startGame();
                keyEvent.consume();
            }
            return;
        }
        
        if (singlePlayerCallbacks != null && !singlePlayerCallbacks.isPaused() && !singlePlayerCallbacks.isGameOver()) {
            // Single player controls using KeyBindingsManager
            KeyCode leftKey = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.SINGLE, KeyBindingsManager.Action.LEFT);
            KeyCode rightKey = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.SINGLE, KeyBindingsManager.Action.RIGHT);
            KeyCode rotateKey = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.SINGLE, KeyBindingsManager.Action.ROTATE);
            KeyCode softDropKey = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.SINGLE, KeyBindingsManager.Action.SOFT_DROP);
            KeyCode hardDropKey = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.SINGLE, KeyBindingsManager.Action.HARD_DROP);
            KeyCode holdKey = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.SINGLE, KeyBindingsManager.Action.HOLD);
            
            InputEventListener eventListener = singlePlayerCallbacks.getEventListener();
            Timeline timeline = singlePlayerCallbacks.getTimeline();
            
            if (leftKey != null && keyCode == leftKey && eventListener != null) {
                ViewData viewData = eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER));
                singlePlayerCallbacks.refreshBrick(viewData);
            } else if (rightKey != null && keyCode == rightKey && eventListener != null) {
                ViewData viewData = eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER));
                singlePlayerCallbacks.refreshBrick(viewData);
            } else if (rotateKey != null && keyCode == rotateKey && eventListener != null) {
                ViewData viewData = eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER));
                singlePlayerCallbacks.refreshBrick(viewData);
            } else if (softDropKey != null && keyCode == softDropKey) {
                if (timeline != null) timeline.setRate(SOFT_DROP_RATE);
                singlePlayerCallbacks.moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
            } else if (hardDropKey != null && keyCode == hardDropKey) {
                if (eventListener != null) {
                    // Hard drop can also clear lines
                    DownData downData = eventListener.onHardDropEvent(new MoveEvent(EventType.HARD_DROP, EventSource.USER));
                    if (downData != null) {
                        if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                            singlePlayerCallbacks.showNotification("+" + downData.getClearRow().getScoreBonus());
                            singlePlayerCallbacks.playLineClear();
                        }
                        singlePlayerCallbacks.refreshBrick(downData.getViewData());
                    }
                }
            } else if (holdKey != null && keyCode == holdKey && eventListener != null) {
                ViewData viewData = eventListener.onHoldEvent(new MoveEvent(EventType.HOLD, EventSource.USER));
                singlePlayerCallbacks.refreshBrick(viewData);
            }
        }
        
        KeyCode newGameKey = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.SINGLE, KeyBindingsManager.Action.NEW_GAME);
        if (newGameKey != null && keyCode == newGameKey && singlePlayerCallbacks != null) {
            singlePlayerCallbacks.newGame();
        }
        keyEvent.consume();
    }
    
    /**
     * Handles key press events for multiplayer mode.
     */
    private void handleMultiplayerKeyPress(KeyEvent keyEvent) {
        if (multiplayerCallbacks == null || !multiplayerCallbacks.isMultiplayerMode() || !multiplayerCallbacks.isGameStarted()) {
            return;
        }
        
        KeyCode keyCode = keyEvent.getCode();
        
        if (multiplayerCallbacks.isPaused()) {
            // Only allow pause/unpause in pause state
            KeyCode pauseKey = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.SINGLE, KeyBindingsManager.Action.PAUSE);
            if (pauseKey != null && keyCode == pauseKey) {
                multiplayerCallbacks.pauseGame();
                keyEvent.consume();
            }
            return;
        }
        
        boolean consumed = false;
        
        // Player 1 controls using KeyBindingsManager
        if (!multiplayerCallbacks.isGameOver1() && multiplayerCallbacks.getEventListener1() != null) {
            KeyCode leftKey1 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER1, KeyBindingsManager.Action.LEFT);
            KeyCode rightKey1 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER1, KeyBindingsManager.Action.RIGHT);
            KeyCode rotateKey1 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER1, KeyBindingsManager.Action.ROTATE);
            KeyCode softDropKey1 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER1, KeyBindingsManager.Action.SOFT_DROP);
            KeyCode hardDropKey1 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER1, KeyBindingsManager.Action.HARD_DROP);
            KeyCode holdKey1 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER1, KeyBindingsManager.Action.HOLD);
            
            InputEventListener eventListener1 = multiplayerCallbacks.getEventListener1();
            Timeline timeline1 = multiplayerCallbacks.getTimeline1();
            
            if (leftKey1 != null && keyCode == leftKey1) {
                ViewData viewData = eventListener1.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER));
                multiplayerCallbacks.refreshBrick(viewData, 1);
                consumed = true;
            } else if (rightKey1 != null && keyCode == rightKey1) {
                ViewData viewData = eventListener1.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER));
                multiplayerCallbacks.refreshBrick(viewData, 1);
                consumed = true;
            } else if (rotateKey1 != null && keyCode == rotateKey1) {
                ViewData viewData = eventListener1.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER));
                multiplayerCallbacks.refreshBrick(viewData, 1);
                consumed = true;
            } else if (softDropKey1 != null && keyCode == softDropKey1) {
                if (timeline1 != null) timeline1.setRate(SOFT_DROP_RATE);
                multiplayerCallbacks.moveDown(new MoveEvent(EventType.DOWN, EventSource.USER), 1);
                consumed = true;
            } else if (hardDropKey1 != null && keyCode == hardDropKey1) {
                // Prevent multiple hard drops from being processed simultaneously
                if (!isHardDropProcessing1) {
                    isHardDropProcessing1 = true;
                    consumed = true;
                    keyEvent.consume(); // Consume immediately to prevent duplicate processing
                    
                    DownData downData = eventListener1.onHardDropEvent(new MoveEvent(EventType.HARD_DROP, EventSource.USER));
                    if (downData != null) {
                        if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                            multiplayerCallbacks.playLineClear();
                        }
                        multiplayerCallbacks.refreshBrick(downData.getViewData(), 1);
                    }
                    // Reset flag after a delay to allow next hard drop (but prevent rapid-fire)
                    Timeline resetTimeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                        isHardDropProcessing1 = false;
                    }));
                    resetTimeline.setCycleCount(1);
                    resetTimeline.play();
                } else {
                    // If already processing, just consume the event
                    consumed = true;
                    keyEvent.consume();
                }
            } else if (holdKey1 != null && keyCode == holdKey1) {
                ViewData viewData = eventListener1.onHoldEvent(new MoveEvent(EventType.HOLD, EventSource.USER));
                multiplayerCallbacks.refreshBrick(viewData, 1);
                consumed = true;
            }
        }
        
        // Player 2 controls using KeyBindingsManager
        if (!multiplayerCallbacks.isGameOver2() && multiplayerCallbacks.getEventListener2() != null) {
            KeyCode leftKey2 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER2, KeyBindingsManager.Action.LEFT);
            KeyCode rightKey2 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER2, KeyBindingsManager.Action.RIGHT);
            KeyCode rotateKey2 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER2, KeyBindingsManager.Action.ROTATE);
            KeyCode softDropKey2 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER2, KeyBindingsManager.Action.SOFT_DROP);
            KeyCode hardDropKey2 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER2, KeyBindingsManager.Action.HARD_DROP);
            KeyCode holdKey2 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER2, KeyBindingsManager.Action.HOLD);
            
            InputEventListener eventListener2 = multiplayerCallbacks.getEventListener2();
            Timeline timeline2 = multiplayerCallbacks.getTimeline2();
            
            if (leftKey2 != null && keyCode == leftKey2) {
                ViewData viewData = eventListener2.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER));
                multiplayerCallbacks.refreshBrick(viewData, 2);
                consumed = true;
            } else if (rightKey2 != null && keyCode == rightKey2) {
                ViewData viewData = eventListener2.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER));
                multiplayerCallbacks.refreshBrick(viewData, 2);
                consumed = true;
            } else if (rotateKey2 != null && keyCode == rotateKey2) {
                ViewData viewData = eventListener2.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER));
                multiplayerCallbacks.refreshBrick(viewData, 2);
                consumed = true;
            } else if (softDropKey2 != null && keyCode == softDropKey2) {
                if (timeline2 != null) {
                    timeline2.setRate(SOFT_DROP_RATE);
                }
                multiplayerCallbacks.moveDown(new MoveEvent(EventType.DOWN, EventSource.USER), 2);
                consumed = true;
            } else if (hardDropKey2 != null && keyCode == hardDropKey2) {
                // Prevent multiple hard drops from being processed simultaneously
                if (!isHardDropProcessing2) {
                    isHardDropProcessing2 = true;
                    consumed = true;
                    keyEvent.consume(); // Consume immediately to prevent duplicate processing
                    
                    DownData downData = eventListener2.onHardDropEvent(new MoveEvent(EventType.HARD_DROP, EventSource.USER));
                    if (downData != null) {
                        if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                            multiplayerCallbacks.playLineClear();
                        }
                        multiplayerCallbacks.refreshBrick(downData.getViewData(), 2);
                    }
                    // Reset flag after a delay to allow next hard drop (but prevent rapid-fire)
                    Timeline resetTimeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                        isHardDropProcessing2 = false;
                    }));
                    resetTimeline.setCycleCount(1);
                    resetTimeline.play();
                } else {
                    // If already processing, just consume the event
                    consumed = true;
                    keyEvent.consume();
                }
            } else if (holdKey2 != null && keyCode == holdKey2) {
                ViewData viewData = eventListener2.onHoldEvent(new MoveEvent(EventType.HOLD, EventSource.USER));
                multiplayerCallbacks.refreshBrick(viewData, 2);
                consumed = true;
            }
        }
        
        // Global controls
        KeyCode pauseKey = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.SINGLE, KeyBindingsManager.Action.PAUSE);
        if (pauseKey != null && keyCode == pauseKey) {
            multiplayerCallbacks.pauseGame();
            consumed = true;
        }
        
        if (consumed) {
            keyEvent.consume();
        }
    }
    
    /**
     * Handles key release events for soft drop key release.
     */
    public void handleKeyRelease(KeyEvent keyEvent, boolean isMultiplayerMode) {
        KeyCode keyCode = keyEvent.getCode();
        
        if (isMultiplayerMode) {
            if (multiplayerCallbacks != null) {
                // Player 1: soft drop key release
                KeyCode softDropKey1 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER1, KeyBindingsManager.Action.SOFT_DROP);
                if (softDropKey1 != null && keyCode == softDropKey1) {
                    multiplayerCallbacks.updateTimelineRate(1);
                }
                // Player 2: soft drop key release
                KeyCode softDropKey2 = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.PLAYER2, KeyBindingsManager.Action.SOFT_DROP);
                if (softDropKey2 != null && keyCode == softDropKey2) {
                    multiplayerCallbacks.updateTimelineRate(2);
                }
            }
        } else {
            if (singlePlayerCallbacks != null) {
                KeyCode softDropKey = keyBindingsManager.getKeyBinding(KeyBindingsManager.PlayerMode.SINGLE, KeyBindingsManager.Action.SOFT_DROP);
                if (softDropKey != null && keyCode == softDropKey) {
                    singlePlayerCallbacks.updateTimelineRate();
                }
            }
        }
    }
    
    /**
     * Callback interface for single player actions.
     */
    public interface SinglePlayerCallbacks {
        InputEventListener getEventListener();
        Timeline getTimeline();
        boolean isPaused();
        boolean isGameOver();
        boolean isGameStarted();
        void refreshBrick(ViewData viewData);
        void moveDown(MoveEvent event);
        void pauseGame();
        void startGame();
        void newGame();
        void showNotification(String message);
        void playLineClear();
        void updateTimelineRate();
    }
    
    /**
     * Callback interface for multiplayer actions.
     */
    public interface MultiplayerCallbacks {
        boolean isMultiplayerMode();
        boolean isGameStarted();
        boolean isPaused();
        boolean isGameOver1();
        boolean isGameOver2();
        boolean isPlayer1Ready();
        boolean isPlayer2Ready();
        void setPlayer1Ready(boolean ready);
        void setPlayer2Ready(boolean ready);
        InputEventListener getEventListener1();
        InputEventListener getEventListener2();
        Timeline getTimeline1();
        Timeline getTimeline2();
        void refreshBrick(ViewData viewData, int playerNumber);
        void moveDown(MoveEvent event, int playerNumber);
        void pauseGame();
        void playLineClear();
        void updateTimelineRate(int playerNumber);
        void updateReadyLabels();
        void checkBothReady();
    }
}

