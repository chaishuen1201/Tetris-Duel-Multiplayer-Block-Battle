package com.comp2042.view;

import com.comp2042.event.InputEventListener;
import com.comp2042.model.ViewData;
import javafx.beans.property.IntegerProperty;

public interface GameView {
    void initGameView(int[][] boardMatrix, ViewData brick);
    void refreshGameBackground(int[][] board);
    void refreshBrick(ViewData brick);
    void setEventListener(InputEventListener eventListener);
    void bindScore(IntegerProperty integerProperty);
    void gameOver();
    void newGame();
}

