package com.comp2042.controller;

import com.comp2042.model.DownData;
import com.comp2042.event.EventSource;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.model.Board;
import com.comp2042.model.ClearRow;
import com.comp2042.model.SimpleBoard;
import com.comp2042.model.ViewData;

public class GameController implements InputEventListener {

    private final Board board;
    private final GuiController viewGuiController;
    private final int playerNumber; // 1 for player 1, 2 for player 2, 0 for single player

    public GameController(GuiController c) {
        this(c, 0); // Default to single player (0)
    }

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
            if (playerNumber > 0 && viewGuiController.getMultiplayerScreen() != null) {
                viewGuiController.getMultiplayerScreen().updateNextBricks(simpleBoard.getNextBricks(), playerNumber);
            } else if (playerNumber == 0 && viewGuiController.getSinglePlayerScreen() != null) {
                viewGuiController.getSinglePlayerScreen().updateNextBricks(simpleBoard.getNextBricks(), viewGuiController.getGameStateManager().isGameStarted());
            }
        }
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;
        if (!canMove) {
            board.mergeBrickToBackground();
            clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                board.getScore().add(clearRow.getScoreBonus());
                
                // Send garbage to opponent in multiplayer mode
                if (playerNumber > 0) {
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
            if (playerNumber > 0 && viewGuiController.getMultiplayerScreen() != null) {
                viewGuiController.getMultiplayerScreen().refreshGameBackground(board.getBoardMatrix(), playerNumber);
            } else if (playerNumber == 0 && viewGuiController.getSinglePlayerScreen() != null) {
                viewGuiController.getSinglePlayerScreen().refreshGameBackground(board.getBoardMatrix());
            }

            // Update next bricks and hold after creating new brick
            if (board instanceof SimpleBoard) {
                SimpleBoard simpleBoard = (SimpleBoard) board;
                if (playerNumber > 0 && viewGuiController.getMultiplayerScreen() != null) {
                    viewGuiController.getMultiplayerScreen().updateNextBricks(simpleBoard.getNextBricks(), playerNumber);
                    viewGuiController.getMultiplayerScreen().updateHoldBrick(simpleBoard.getHeldBrick(), playerNumber);
                } else if (playerNumber == 0 && viewGuiController.getSinglePlayerScreen() != null) {
                    viewGuiController.getSinglePlayerScreen().updateNextBricks(simpleBoard.getNextBricks(), viewGuiController.getGameStateManager().isGameStarted());
                    viewGuiController.getSinglePlayerScreen().updateHoldBrick(simpleBoard.getHeldBrick());
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

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

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
            clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                board.getScore().add(clearRow.getScoreBonus());
                
                // Send garbage to opponent in multiplayer mode
                if (playerNumber > 0) {
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
            if (playerNumber > 0 && viewGuiController.getMultiplayerScreen() != null) {
                viewGuiController.getMultiplayerScreen().refreshGameBackground(board.getBoardMatrix(), playerNumber);
                viewGuiController.getMultiplayerScreen().updateNextBricks(simpleBoard.getNextBricks(), playerNumber);
                viewGuiController.getMultiplayerScreen().updateHoldBrick(simpleBoard.getHeldBrick(), playerNumber);
            } else if (playerNumber == 0 && viewGuiController.getSinglePlayerScreen() != null) {
                viewGuiController.getSinglePlayerScreen().refreshGameBackground(board.getBoardMatrix());
                viewGuiController.getSinglePlayerScreen().updateNextBricks(simpleBoard.getNextBricks(), viewGuiController.getGameStateManager().isGameStarted());
                viewGuiController.getSinglePlayerScreen().updateHoldBrick(simpleBoard.getHeldBrick());
            }
        }
        return new DownData(clearRow, board.getViewData());
    }

    @Override
    public ViewData onHoldEvent(MoveEvent event) {
        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            simpleBoard.holdBrick();
            if (playerNumber > 0 && viewGuiController.getMultiplayerScreen() != null) {
                viewGuiController.getMultiplayerScreen().updateHoldBrick(simpleBoard.getHeldBrick(), playerNumber);
            } else if (playerNumber == 0 && viewGuiController.getSinglePlayerScreen() != null) {
                viewGuiController.getSinglePlayerScreen().updateHoldBrick(simpleBoard.getHeldBrick());
            }
        }
        return board.getViewData();
    }

    @Override
    public void createNewGame() {
        board.newGame();
        // Re-bind score to ensure it updates after reset
        viewGuiController.bindScore(board.getScore().scoreProperty(), playerNumber);
        // Refresh game background
        if (playerNumber > 0 && viewGuiController.getMultiplayerScreen() != null) {
            viewGuiController.getMultiplayerScreen().refreshGameBackground(board.getBoardMatrix(), playerNumber);
        } else if (playerNumber == 0 && viewGuiController.getSinglePlayerScreen() != null) {
            viewGuiController.getSinglePlayerScreen().refreshGameBackground(board.getBoardMatrix());
        }

        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            if (playerNumber > 0 && viewGuiController.getMultiplayerScreen() != null) {
                viewGuiController.getMultiplayerScreen().updateNextBricks(simpleBoard.getNextBricks(), playerNumber);
                viewGuiController.getMultiplayerScreen().updateHoldBrick(simpleBoard.getHeldBrick(), playerNumber);
            } else if (playerNumber == 0 && viewGuiController.getSinglePlayerScreen() != null) {
                viewGuiController.getSinglePlayerScreen().updateNextBricks(simpleBoard.getNextBricks(), viewGuiController.getGameStateManager().isGameStarted());
                viewGuiController.getSinglePlayerScreen().updateHoldBrick(simpleBoard.getHeldBrick());
            }
        }
    }

    // Expose score property for re-binding when needed
    public javafx.beans.property.IntegerProperty getScoreProperty() {
        return board.getScore().scoreProperty();
    }
    
    // Expose board for ghost position calculation
    public Board getBoard() {
        return board;
    }
    
    // Get player number
    public int getPlayerNumber() {
        return playerNumber;
    }
    
    /**
     * Calculates how many garbage lines to send based on lines cleared.
     * Rules:
     * - Single (1 line): 0 garbage lines
     * - Double (2 lines): 1 garbage line
     * - Triple (3 lines): 2 garbage lines
     * - Tetris (4 lines): 4 garbage lines
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
     * Gets the board instance (for garbage processing).
     */
    public SimpleBoard getSimpleBoard() {
        return board instanceof SimpleBoard ? (SimpleBoard) board : null;
    }
}

