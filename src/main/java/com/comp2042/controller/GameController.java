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

    public GameController(GuiController c) {
        this.viewGuiController = c;
        this.board = new SimpleBoard(20, 10);
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());

        // Cast to SimpleBoard for the additional properties
        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            viewGuiController.bindLevel(simpleBoard.levelProperty());
            viewGuiController.bindLines(simpleBoard.linesProperty());
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
                viewGuiController.gameOver();
            }
            viewGuiController.refreshGameBackground(board.getBoardMatrix());

            // Update next bricks and hold after creating new brick
            if (board instanceof SimpleBoard) {
                SimpleBoard simpleBoard = (SimpleBoard) board;
                viewGuiController.updateNextBricks(simpleBoard.getNextBricks());
                viewGuiController.updateHoldBrick(simpleBoard.getHeldBrick());
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
    public ViewData onHardDropEvent(MoveEvent event) {
        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            // Hard drop: 2 points per cell dropped
            int cellsDropped = simpleBoard.hardDrop();
            int hardDropScore = cellsDropped * 2;
            board.getScore().add(hardDropScore);

            board.mergeBrickToBackground();
            ClearRow clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                board.getScore().add(clearRow.getScoreBonus());
            }
            if (board.createNewBrick()) {
                viewGuiController.gameOver();
            }
            viewGuiController.refreshGameBackground(board.getBoardMatrix());
            viewGuiController.updateNextBricks(simpleBoard.getNextBricks());
            viewGuiController.updateHoldBrick(simpleBoard.getHeldBrick());
        }
        return board.getViewData();
    }

    @Override
    public ViewData onHoldEvent(MoveEvent event) {
        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            simpleBoard.holdBrick();
            viewGuiController.updateHoldBrick(simpleBoard.getHeldBrick());
        }
        return board.getViewData();
    }

    @Override
    public void createNewGame() {
        board.newGame();
        // Re-bind score to ensure it updates after reset
        viewGuiController.bindScore(board.getScore().scoreProperty());
        viewGuiController.refreshGameBackground(board.getBoardMatrix());

        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            viewGuiController.updateNextBricks(simpleBoard.getNextBricks());
            viewGuiController.updateHoldBrick(simpleBoard.getHeldBrick());
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
}

