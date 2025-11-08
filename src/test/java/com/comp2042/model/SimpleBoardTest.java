package com.comp2042.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleBoardTest {

    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private SimpleBoard board;

    @BeforeEach
    void setUp() {
        board = new SimpleBoard(BOARD_WIDTH, BOARD_HEIGHT);
    }

    @Test
    void testInitialState() {
        int[][] matrix = board.getBoardMatrix();
        assertEquals(BOARD_WIDTH, matrix.length);
        assertEquals(BOARD_HEIGHT, matrix[0].length);
        
        // Board should be empty initially
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                assertEquals(0, matrix[i][j]);
            }
        }
    }

    @Test
    void testCreateNewBrick() {
        boolean gameOver = board.createNewBrick();
        assertFalse(gameOver, "New game should not be over");
        
        ViewData viewData = board.getViewData();
        assertNotNull(viewData);
        assertNotNull(viewData.getBrickData());
    }

    @Test
    void testMoveBrickDown() {
        board.createNewBrick();
        boolean moved = board.moveBrickDown();
        assertTrue(moved, "Brick should be able to move down initially");
    }

    @Test
    void testMoveBrickLeft() {
        board.createNewBrick();
        boolean moved = board.moveBrickLeft();
        assertTrue(moved, "Brick should be able to move left initially");
    }

    @Test
    void testMoveBrickRight() {
        board.createNewBrick();
        boolean moved = board.moveBrickRight();
        assertTrue(moved, "Brick should be able to move right initially");
    }

    @Test
    void testRotateBrick() {
        board.createNewBrick();
        boolean rotated = board.rotateLeftBrick();
        assertTrue(rotated, "Brick should be able to rotate initially");
    }

    @Test
    void testGetScore() {
        Score score = board.getScore();
        assertNotNull(score);
        assertEquals(0, score.scoreProperty().get());
    }

    @Test
    void testNewGame() {
        // Add some score
        board.getScore().add(100);
        assertEquals(100, board.getScore().scoreProperty().get());
        
        // Create a brick and move it
        board.createNewBrick();
        board.moveBrickDown();
        
        // Start new game
        board.newGame();
        
        // Score should be reset
        assertEquals(0, board.getScore().scoreProperty().get());
        
        // Board should be empty
        int[][] matrix = board.getBoardMatrix();
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                assertEquals(0, matrix[i][j]);
            }
        }
    }

    @Test
    void testGetViewData() {
        board.createNewBrick();
        ViewData viewData = board.getViewData();
        
        assertNotNull(viewData);
        assertNotNull(viewData.getBrickData());
        assertNotNull(viewData.getNextBrickData());
        assertTrue(viewData.getXPosition() >= 0);
        assertTrue(viewData.getYPosition() >= 0);
    }
}

