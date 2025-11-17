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
            }
            if (board.createNewBrick()) {
                viewGuiController.gameOver(playerNumber);
            }
            viewGuiController.refreshGameBackground(board.getBoardMatrix(), playerNumber);

            // Update next bricks and hold after creating new brick
            if (board instanceof SimpleBoard) {
                SimpleBoard simpleBoard = (SimpleBoard) board;
                viewGuiController.updateNextBricks(simpleBoard.getNextBricks(), playerNumber);
                viewGuiController.updateHoldBrick(simpleBoard.getHeldBrick(), playerNumber);
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
            }
            if (board.createNewBrick()) {
                viewGuiController.gameOver(playerNumber);
            }
            viewGuiController.refreshGameBackground(board.getBoardMatrix(), playerNumber);
            viewGuiController.updateNextBricks(simpleBoard.getNextBricks(), playerNumber);
            viewGuiController.updateHoldBrick(simpleBoard.getHeldBrick(), playerNumber);
        }
        return new DownData(clearRow, board.getViewData());
    }

    @Override
    public ViewData onHoldEvent(MoveEvent event) {
        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            simpleBoard.holdBrick();
            viewGuiController.updateHoldBrick(simpleBoard.getHeldBrick(), playerNumber);
        }
        return board.getViewData();
    }

    @Override
    public void createNewGame() {
        board.newGame();
        // Re-bind score to ensure it updates after reset
        viewGuiController.bindScore(board.getScore().scoreProperty(), playerNumber);
        viewGuiController.refreshGameBackground(board.getBoardMatrix(), playerNumber);

        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            viewGuiController.updateNextBricks(simpleBoard.getNextBricks(), playerNumber);
            viewGuiController.updateHoldBrick(simpleBoard.getHeldBrick(), playerNumber);
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
}

