package com.comp2042.controller;

import com.comp2042.model.DownData;
import com.comp2042.event.EventSource;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.model.Board;
import com.comp2042.model.ClearRow;
import com.comp2042.model.SimpleBoard;
import com.comp2042.model.ViewData;
import com.comp2042.view.GameView;

public class GameController implements InputEventListener {

    private static final int BOARD_WIDTH = 25;
    private static final int BOARD_HEIGHT = 10;

    private final Board board;
    private final GameView gameView;

    public GameController(GameView gameView) {
        this.board = new SimpleBoard(BOARD_WIDTH, BOARD_HEIGHT);
        this.gameView = gameView;
        board.createNewBrick();
        gameView.setEventListener(this);
        gameView.initGameView(board.getBoardMatrix(), board.getViewData());
        gameView.bindScore(board.getScore().scoreProperty());
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
                gameView.gameOver();
            }
            gameView.refreshGameBackground(board.getBoardMatrix());
        } else {
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
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
    public void createNewGame() {
        board.newGame();
        gameView.refreshGameBackground(board.getBoardMatrix());
    }
}

