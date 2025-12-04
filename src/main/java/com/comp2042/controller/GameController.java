package com.comp2042.controller;

import com.comp2042.model.DownData;
import com.comp2042.event.EventSource;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.model.Board;
import com.comp2042.model.ClearRow;
import com.comp2042.model.SimpleBoard;
import com.comp2042.model.ViewData;

/**
 * Controls the game logic and coordinates between the game board model and the GUI view.
 * This class serves as the primary controller for game operations, handling player input
 * events, managing game state transitions, calculating scores, and coordinating view updates.
 * It implements InputEventListener to process move events from both user input and automatic
 * game progression (timeline-driven movement). The controller manages brick movement (left,
 * right, down, rotate, hard drop, hold), line clearing, score calculation, garbage sending
 * in multiplayer mode, and game over detection. It supports both single player (playerNumber = 0)
 * and multiplayer modes (playerNumber = 1 or 2), delegating view updates to the appropriate
 * GUI controller methods based on the player number.
 */
public class GameController implements InputEventListener {

    private final Board board;
    private final GuiController viewGuiController;
    private final int playerNumber; // 1 for player 1, 2 for player 2, 0 for single player

    /**
     * Creates a new GameController for single player mode.
     * 
     * @param c The GUI controller to coordinate with for view updates
     */
    public GameController(GuiController c) {
        this(c, 0); // Default to single player (0)
    }

    /**
     * Creates a new GameController for the specified player.
     * Initializes the game board, creates the first brick, sets up event listeners,
     * and binds score, level, and lines properties to the GUI.
     * 
     * @param c The GUI controller to coordinate with for view updates
     * @param playerNumber The player number (0 for single player, 1 or 2 for multiplayer)
     */
    public GameController(GuiController c, int playerNumber) {
        this.viewGuiController = c;
        this.playerNumber = playerNumber;
        this.board = new SimpleBoard(20, 10);
        board.createNewBrick();
        viewGuiController.setEventListener(this, playerNumber);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData(), playerNumber);
        viewGuiController.bindScore(board.getScore().scoreProperty(), playerNumber);

        // Cast to SimpleBoard for the additional properties
        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            viewGuiController.bindLevel(simpleBoard.levelProperty(), playerNumber);
            viewGuiController.bindLines(simpleBoard.linesProperty(), playerNumber);
            // Update next bricks display when game starts
            if (playerNumber > 0) {
                viewGuiController.updateMultiplayerNextBricks(simpleBoard.getNextBricks(), playerNumber);
            } else if (playerNumber == 0) {
                viewGuiController.updateSinglePlayerNextBricks(simpleBoard.getNextBricks());
            }
        }
    }

    /**
     * Handles the automatic down move event, moving the current brick down by one position.
     * If the brick cannot move down, it merges to the background, clears completed rows,
     * awards score bonuses, sends garbage to opponents in multiplayer mode (if applicable),
     * and creates a new brick. If the brick can still move, awards soft drop points for
     * user-initiated moves. Updates the game background and next/hold brick displays.
     * 
     * @param event The move event containing event type and source information
     * @return DownData containing information about cleared rows and updated view data
     */
    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;
        if (!canMove) {
            board.mergeBrickToBackground();
            // Check if only garbage rows will be cleared before clearing
            boolean onlyGarbage = false;
            if (board instanceof SimpleBoard) {
                SimpleBoard simpleBoard = (SimpleBoard) board;
                onlyGarbage = simpleBoard.willClearOnlyGarbage();
            }
            clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                board.getScore().add(clearRow.getScoreBonus());
                
                // Send garbage to opponent in multiplayer mode
                // Only send garbage if regular blocks were cleared, not if only garbage rows were cleared
                if (playerNumber > 0 && !onlyGarbage) {
                    int garbageToSend = calculateGarbageToSend(clearRow.getLinesRemoved());
                    if (garbageToSend > 0) {
                        viewGuiController.sendGarbageToOpponent(playerNumber, garbageToSend);
                    }
                }
            }
            if (board.createNewBrick()) {
                viewGuiController.gameOver(playerNumber);
            }
            // Refresh game background
            if (playerNumber > 0) {
                viewGuiController.refreshMultiplayerGameBackground(board.getBoardMatrix(), playerNumber);
            } else if (playerNumber == 0) {
                viewGuiController.refreshSinglePlayerGameBackground(board.getBoardMatrix());
            }

            // Update next bricks and hold after creating new brick
            if (board instanceof SimpleBoard) {
                SimpleBoard simpleBoard = (SimpleBoard) board;
                if (playerNumber > 0) {
                    viewGuiController.updateMultiplayerNextBricks(simpleBoard.getNextBricks(), playerNumber);
                    viewGuiController.updateMultiplayerHoldBrick(simpleBoard.getHeldBrick(), playerNumber);
                } else if (playerNumber == 0) {
                    viewGuiController.updateSinglePlayerNextBricks(simpleBoard.getNextBricks());
                    viewGuiController.updateSinglePlayerHoldBrick(simpleBoard.getHeldBrick());
                }
            }
        } else {
            if (event.getEventSource() == EventSource.USER) {
                // Soft drop: 1 point per cell moved
                if (board instanceof SimpleBoard) {
                    SimpleBoard simpleBoard = (SimpleBoard) board;
                    int cellCount = simpleBoard.getCurrentBrickCellCount();
                    board.getScore().add(cellCount);
                } else {
                    // Fallback: add 1 point if not SimpleBoard
                    board.getScore().add(1);
                }
            }
        }
        return new DownData(clearRow, board.getViewData());
    }

    /**
     * Handles the left move event, moving the current brick one position to the left.
     * 
     * @param event The move event containing event type and source information
     * @return ViewData containing the updated brick position and state
     */
    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    /**
     * Handles the right move event, moving the current brick one position to the right.
     * 
     * @param event The move event containing event type and source information
     * @return ViewData containing the updated brick position and state
     */
    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    /**
     * Handles the rotate event, rotating the current brick 90 degrees counter-clockwise.
     * 
     * @param event The move event containing event type and source information
     * @return ViewData containing the updated brick position and rotation state
     */
    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

    /**
     * Handles the hard drop event, instantly dropping the current brick to the bottom.
     * Awards points based on the number of cells dropped (2 points per cell).
     * Merges the brick, clears completed rows, and creates a new brick.
     * In multiplayer mode, sends garbage to the opponent when lines are cleared.
     * 
     * @param event The move event containing event type and source information
     * @return DownData containing information about cleared rows and updated view data
     */
    @Override
    public DownData onHardDropEvent(MoveEvent event) {
        ClearRow clearRow = null;
        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            // Hard drop: 2 points per cell dropped
            int cellsDropped = simpleBoard.hardDrop();
            int hardDropScore = cellsDropped * 2;
            board.getScore().add(hardDropScore);
            
            board.mergeBrickToBackground();
            // Check if only garbage rows will be cleared before clearing
            boolean onlyGarbage = false;
            if (board instanceof SimpleBoard) {
                SimpleBoard simpleBoard2 = (SimpleBoard) board;
                onlyGarbage = simpleBoard2.willClearOnlyGarbage();
            }
            clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                board.getScore().add(clearRow.getScoreBonus());
                
                // Send garbage to opponent in multiplayer mode
                // Only send garbage if regular blocks were cleared, not if only garbage rows were cleared
                if (playerNumber > 0 && !onlyGarbage) {
                    int garbageToSend = calculateGarbageToSend(clearRow.getLinesRemoved());
                    if (garbageToSend > 0) {
                        viewGuiController.sendGarbageToOpponent(playerNumber, garbageToSend);
                    }
                }
            }
            if (board.createNewBrick()) {
                viewGuiController.gameOver(playerNumber);
            }
            // Refresh game background
            if (playerNumber > 0) {
                viewGuiController.refreshMultiplayerGameBackground(board.getBoardMatrix(), playerNumber);
                viewGuiController.updateMultiplayerNextBricks(simpleBoard.getNextBricks(), playerNumber);
                viewGuiController.updateMultiplayerHoldBrick(simpleBoard.getHeldBrick(), playerNumber);
            } else if (playerNumber == 0) {
                viewGuiController.refreshSinglePlayerGameBackground(board.getBoardMatrix());
                viewGuiController.updateSinglePlayerNextBricks(simpleBoard.getNextBricks());
                viewGuiController.updateSinglePlayerHoldBrick(simpleBoard.getHeldBrick());
            }
        }
        return new DownData(clearRow, board.getViewData());
    }

    /**
     * Handles the hold event, storing the current brick and swapping it with the previously held brick.
     * If no brick is currently held, stores the current brick and creates a new one.
     * 
     * @param event The move event containing event type and source information
     * @return ViewData containing the updated brick state after holding
     */
    @Override
    public ViewData onHoldEvent(MoveEvent event) {
            if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            simpleBoard.holdBrick();
            if (playerNumber > 0) {
                viewGuiController.updateMultiplayerNextBricks(simpleBoard.getNextBricks(), playerNumber);
                viewGuiController.updateMultiplayerHoldBrick(simpleBoard.getHeldBrick(), playerNumber);
            } else if (playerNumber == 0) {
                viewGuiController.updateSinglePlayerNextBricks(simpleBoard.getNextBricks());
                viewGuiController.updateSinglePlayerHoldBrick(simpleBoard.getHeldBrick());
            }
        }
        return board.getViewData();
    }

    /**
     * Resets the game board and starts a new game session.
     * Clears the board, resets the score, re-binds the score property to the GUI,
     * refreshes the game background, and updates next bricks and hold brick displays.
     * Creates a new initial brick to begin the game.
     */
    @Override
    public void createNewGame() {
        board.newGame();
        // Re-bind score to ensure it updates after reset
        viewGuiController.bindScore(board.getScore().scoreProperty(), playerNumber);
        
        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            // Re-bind level and lines to ensure they update after reset
            viewGuiController.bindLevel(simpleBoard.levelProperty(), playerNumber);
            viewGuiController.bindLines(simpleBoard.linesProperty(), playerNumber);
            
            if (playerNumber > 0) {
                viewGuiController.updateMultiplayerNextBricks(simpleBoard.getNextBricks(), playerNumber);
                viewGuiController.updateMultiplayerHoldBrick(simpleBoard.getHeldBrick(), playerNumber);
            } else if (playerNumber == 0) {
                viewGuiController.updateSinglePlayerNextBricks(simpleBoard.getNextBricks());
                viewGuiController.updateSinglePlayerHoldBrick(simpleBoard.getHeldBrick());
            }
        }
        
        // Refresh game background
        if (playerNumber > 0) {
            viewGuiController.refreshMultiplayerGameBackground(board.getBoardMatrix(), playerNumber);
        } else if (playerNumber == 0) {
            viewGuiController.refreshSinglePlayerGameBackground(board.getBoardMatrix());
        }
    }

    /**
     * Gets the score property for binding to the GUI.
     * 
     * @return The IntegerProperty representing the current score
     */
    public javafx.beans.property.IntegerProperty getScoreProperty() {
        return board.getScore().scoreProperty();
    }
    
    /**
     * Gets the game board instance for ghost position calculation and other operations.
     * 
     * @return The Board instance representing the game state
     */
    public Board getBoard() {
        return board;
    }
    
    /**
     * Gets the player number for this game controller.
     * 
     * @return The player number (0 for single player, 1 or 2 for multiplayer)
     */
    public int getPlayerNumber() {
        return playerNumber;
    }
    
    /**
     * Calculates how many garbage lines to send to the opponent based on lines cleared.
     * This implements the standard Tetris garbage sending rules for multiplayer mode.
     * 
     * @param linesCleared The number of lines cleared in a single move
     * @return The number of garbage lines to send (0 for single, 1 for double, 2 for triple, 4 for tetris)
     */
    private int calculateGarbageToSend(int linesCleared) {
        switch (linesCleared) {
            case 1:
                return 0; // Single line clear doesn't send garbage
            case 2:
                return 1; // Double sends 1 garbage line
            case 3:
                return 2; // Triple sends 2 garbage lines
            case 4:
                return 4; // Tetris sends 4 garbage lines
            default:
                return 0;
        }
    }
    
    /**
     * Gets the board instance as a SimpleBoard for garbage processing and other operations.
     * 
     * @return The SimpleBoard instance if the board is a SimpleBoard, null otherwise
     */
    public SimpleBoard getSimpleBoard() {
        return board instanceof SimpleBoard ? (SimpleBoard) board : null;
    }
}

