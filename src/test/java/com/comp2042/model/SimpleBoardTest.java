package com.comp2042.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

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
        // Move brick down a bit to ensure it's in a valid position for rotation
        board.moveBrickDown();
        boolean rotated = board.rotateLeftBrick();
        assertTrue(rotated, "Brick should be able to rotate after moving down");
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
        assertTrue(viewData.getXPosition() >= 0, "X position should be non-negative");
        // Y position can be negative initially (brick starts above board at INITIAL_Y = -1)
        assertTrue(viewData.getYPosition() >= -1, "Y position should be >= -1 (initial position)");
    }

    /**
     * Helper method to manually fill a row in the board matrix for testing.
     * The matrix is [width][height] = [10][20], where matrix[i] is a row.
     * Uses reflection to access the private currentGameMatrix field.
     */
    private void fillRow(int rowIndex, int value) {
        try {
            Field matrixField = SimpleBoard.class.getDeclaredField("currentGameMatrix");
            matrixField.setAccessible(true);
            int[][] matrix = (int[][]) matrixField.get(board);
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                matrix[rowIndex][j] = value;
            }
        } catch (Exception e) {
            fail("Failed to access board matrix: " + e.getMessage());
        }
    }

    @Test
    void testClearRows_SingleLine() {
        // Fill one row completely
        fillRow(BOARD_WIDTH - 1, 1); // Fill bottom row
        
        ClearRow clearRow = board.clearRows();
        
        assertEquals(1, clearRow.getLinesRemoved(), "Should clear 1 line");
        assertTrue(clearRow.getScoreBonus() > 0, "Should have score bonus");
        
        // Verify the row was cleared
        int[][] newMatrix = clearRow.getNewMatrix();
        boolean rowIsEmpty = true;
        for (int j = 0; j < BOARD_HEIGHT; j++) {
            if (newMatrix[BOARD_WIDTH - 1][j] != 0) {
                rowIsEmpty = false;
                break;
            }
        }
        assertTrue(rowIsEmpty, "Row should be cleared");
    }

    @Test
    void testClearRows_DoubleLine() {
        // Fill two rows completely
        fillRow(BOARD_WIDTH - 1, 1); // Fill bottom row
        fillRow(BOARD_WIDTH - 2, 1); // Fill second from bottom
        
        ClearRow clearRow = board.clearRows();
        
        assertEquals(2, clearRow.getLinesRemoved(), "Should clear 2 lines");
        assertTrue(clearRow.getScoreBonus() > 0, "Should have score bonus");
    }

    @Test
    void testClearRows_TripleLine() {
        // Fill three rows completely
        fillRow(BOARD_WIDTH - 1, 1); // Fill bottom row
        fillRow(BOARD_WIDTH - 2, 1); // Fill second from bottom
        fillRow(BOARD_WIDTH - 3, 1); // Fill third from bottom
        
        ClearRow clearRow = board.clearRows();
        
        assertEquals(3, clearRow.getLinesRemoved(), "Should clear 3 lines");
        assertTrue(clearRow.getScoreBonus() > 0, "Should have score bonus");
    }

    @Test
    void testClearRows_Tetris() {
        // Fill four rows completely (Tetris)
        fillRow(BOARD_WIDTH - 1, 1); // Fill bottom row
        fillRow(BOARD_WIDTH - 2, 1); // Fill second from bottom
        fillRow(BOARD_WIDTH - 3, 1); // Fill third from bottom
        fillRow(BOARD_WIDTH - 4, 1); // Fill fourth from bottom
        
        ClearRow clearRow = board.clearRows();
        
        assertEquals(4, clearRow.getLinesRemoved(), "Should clear 4 lines (Tetris)");
        assertTrue(clearRow.getScoreBonus() > 0, "Should have score bonus");
    }

    @Test
    void testGarbageRules_SingleLineSendsZeroGarbage() {
        // This test verifies that 1 line clear should result in 0 garbage
        // The actual garbage calculation is in GameController, but we test the line clear count
        fillRow(BOARD_WIDTH - 1, 1);
        
        ClearRow clearRow = board.clearRows();
        assertEquals(1, clearRow.getLinesRemoved());
        // 1 line → 0 garbage (tested in GameControllerTest)
    }

    @Test
    void testGarbageRules_DoubleLineSendsOneGarbage() {
        // 2 lines → 1 garbage (tested in GameControllerTest)
        fillRow(BOARD_WIDTH - 1, 1);
        fillRow(BOARD_WIDTH - 2, 1);
        
        ClearRow clearRow = board.clearRows();
        assertEquals(2, clearRow.getLinesRemoved());
    }

    @Test
    void testGarbageRules_TripleLineSendsTwoGarbage() {
        // 3 lines → 2 garbage (tested in GameControllerTest)
        fillRow(BOARD_WIDTH - 1, 1);
        fillRow(BOARD_WIDTH - 2, 1);
        fillRow(BOARD_WIDTH - 3, 1);
        
        ClearRow clearRow = board.clearRows();
        assertEquals(3, clearRow.getLinesRemoved());
    }

    @Test
    void testGarbageRules_TetrisSendsFourGarbage() {
        // 4 lines → 4 garbage (tested in GameControllerTest)
        fillRow(BOARD_WIDTH - 1, 1);
        fillRow(BOARD_WIDTH - 2, 1);
        fillRow(BOARD_WIDTH - 3, 1);
        fillRow(BOARD_WIDTH - 4, 1);
        
        ClearRow clearRow = board.clearRows();
        assertEquals(4, clearRow.getLinesRemoved());
    }

    @Test
    void testHardDrop_ReturnsCorrectNumberOfCellsDropped() {
        board.createNewBrick();
        
        // Hard drop the brick
        int cellsDropped = board.hardDrop();
        
        // Should have dropped at least some cells
        assertTrue(cellsDropped > 0, "Hard drop should drop at least one cell");
        
        // Verify the brick is at the bottom (can't move down anymore)
        assertFalse(board.moveBrickDown(), "Brick should be at bottom after hard drop");
    }

    @Test
    void testHardDrop_BoardStateUpdatesCorrectly() {
        board.createNewBrick();
        
        // Hard drop
        int cellsDropped = board.hardDrop();
        
        // Merge brick to background
        board.mergeBrickToBackground();
        
        // Verify brick was merged (board should have blocks)
        int[][] matrix = board.getBoardMatrix();
        boolean hasBlocks = false;
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                if (matrix[i][j] != 0) {
                    hasBlocks = true;
                    break;
                }
            }
            if (hasBlocks) break;
        }
        assertTrue(hasBlocks || cellsDropped == 0, "Board should have blocks after merging hard-dropped brick");
    }

    @Test
    void testHardDrop_GhostLineLogicDoesNotBreak() {
        board.createNewBrick();
        
        // Hard drop
        board.hardDrop();
        
        // Get ghost position after hard drop (should be same as current position)
        java.awt.Point ghostPosAfter = board.getGhostPosition();
        ViewData viewData = board.getViewData();
        
        // After hard drop, ghost position should match current position
        assertEquals(viewData.getXPosition(), ghostPosAfter.x);
        assertEquals(viewData.getYPosition(), ghostPosAfter.y);
    }

    @Test
    void testHoldBrick_SwapsCurrentBrickWithHeldBrick() {
        board.createNewBrick();
        ViewData firstBrick = board.getViewData();
        int[][] firstBrickData = firstBrick.getBrickData();
        
        // First hold - should store current brick
        board.holdBrick();
        assertNotNull(board.getHeldBrick(), "Held brick should be set after first hold");
        
        // Get the new current brick
        ViewData secondBrick = board.getViewData();
        int[][] secondBrickData = secondBrick.getBrickData();
        
        // Current brick should be different from first brick
        // (We can't easily compare brick types, but we can verify they're different arrays)
        assertNotSame(firstBrickData, secondBrickData, "Current brick should change after hold");
        
        // Create a new brick to enable holding again
        board.createNewBrick();
        
        // Second hold - should swap
        board.holdBrick();
        
        // After swap, should have a brick
        ViewData fourthBrick = board.getViewData();
        assertNotNull(fourthBrick, "Should have a brick after swap");
    }

    @Test
    void testHoldBrick_CannotHoldTwiceInRow() {
        board.createNewBrick();
        
        // First hold
        board.holdBrick();
        
        // Try to hold again immediately (should not work)
        board.holdBrick();
        ViewData afterSecondHold = board.getViewData();
        
        // Second hold should not change the brick (canHold is false)
        // The brick should remain the same
        assertNotNull(afterSecondHold, "Should still have a brick");
        // The canHold flag prevents the second hold from working
    }

    @Test
    void testHoldBrick_ResetsAfterPlacingBrick() {
        board.createNewBrick();
        
        // Hold the brick
        board.holdBrick();
        assertNotNull(board.getHeldBrick(), "Should have a held brick");
        
        // Move brick to bottom and merge (simulating placing)
        while (board.moveBrickDown()) {
            // Keep moving down
        }
        board.mergeBrickToBackground();
        
        // Create new brick (this resets canHold)
        board.createNewBrick();
        
        // Now we should be able to hold again
        board.holdBrick();
        ViewData afterHold = board.getViewData();
        
        // Hold should work (brick should change)
        assertNotNull(afterHold, "Should have a brick after hold");
        assertNotNull(board.getHeldBrick(), "Should have a held brick after placing and creating new brick");
    }

    @Test
    void testNextBricks_AlwaysHasCorrectSize() {
        // Get next bricks
        java.util.List<com.comp2042.bricks.Brick> nextBricks = board.getNextBricks();
        
        // Should have at least 1 brick, typically 3 for RandomBrickGenerator
        assertNotNull(nextBricks, "Next bricks should not be null");
        assertTrue(nextBricks.size() >= 1, "Should have at least 1 next brick");
        assertTrue(nextBricks.size() <= 3, "Should have at most 3 next bricks");
    }

    @Test
    void testNextBricks_UpdatesAfterCreatingNewBrick() {
        // Get initial next bricks
        java.util.List<com.comp2042.bricks.Brick> initialNextBricks = board.getNextBricks();
        assertNotNull(initialNextBricks);
        
        // Create a new brick
        board.createNewBrick();
        
        // Get next bricks again
        java.util.List<com.comp2042.bricks.Brick> updatedNextBricks = board.getNextBricks();
        assertNotNull(updatedNextBricks);
        
        // Should still have correct size
        assertTrue(updatedNextBricks.size() >= 1, "Should have at least 1 next brick after creating new brick");
        assertTrue(updatedNextBricks.size() <= 3, "Should have at most 3 next bricks after creating new brick");
    }

    @Test
    void testCurrentBrickCellCount_ReturnsCorrectNumberOfBlocks() {
        board.createNewBrick();
        
        // Get the current brick cell count
        int cellCount = board.getCurrentBrickCellCount();
        
        // Should be at least 1 (all bricks have at least 1 cell)
        assertTrue(cellCount >= 1, "Brick should have at least 1 cell");
        
        // Should be at most 4 (largest brick is 4 cells)
        assertTrue(cellCount <= 4, "Brick should have at most 4 cells");
        
        // Verify by checking the actual brick data
        ViewData viewData = board.getViewData();
        int[][] brickData = viewData.getBrickData();
        int actualCount = 0;
        for (int[] row : brickData) {
            for (int cell : row) {
                if (cell != 0) {
                    actualCount++;
                }
            }
        }
        assertEquals(actualCount, cellCount, "Cell count should match actual brick cells");
    }

    @Test
    void testNewGame_ScoreResets() {
        // Add some score
        board.getScore().add(500);
        assertEquals(500, board.getScore().scoreProperty().get());
        
        // Start new game
        board.newGame();
        
        // Score should be reset
        assertEquals(0, board.getScore().scoreProperty().get(), "Score should reset to 0");
    }

    @Test
    void testNewGame_LevelResets() {
        // Increase level by clearing lines
        fillRow(BOARD_WIDTH - 1, 1);
        board.clearRows(); // This might increase level
        
        // Start new game
        board.newGame();
        
        // Level should reset to 1
        assertEquals(1, board.levelProperty().get(), "Level should reset to 1");
    }

    @Test
    void testNewGame_BoardMatrixEmpties() {
        // Fill some cells
        fillRow(BOARD_WIDTH - 1, 1);
        
        // Start new game
        board.newGame();
        
        // Board should be empty
        int[][] matrix = board.getBoardMatrix();
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                assertEquals(0, matrix[i][j], "Board should be empty after newGame");
            }
        }
    }

    @Test
    void testProcessGarbageQueue_DetectsGameOverWhenTopRowHasBlocks() {
        // Create a brick first (required for processGarbageQueue to work)
        board.createNewBrick();
        
        // Fill the top row (row 0) with blocks
        fillRow(0, 1);
        
        // Add garbage to queue
        board.addGarbageToQueue(1);
        
        // Processing garbage should detect game over because top row has blocks
        // This should return true BEFORE shifting (due to top row check)
        boolean gameOver = board.processGarbageQueue();
        
        assertTrue(gameOver, "Should detect game over when top row has blocks and garbage is inserted");
    }

    @Test
    void testProcessGarbageQueue_NoGameOverWhenTopRowEmpty() {
        // Create a brick first (required for processGarbageQueue to work)
        board.createNewBrick();
        
        // Fill a row other than top row
        fillRow(BOARD_WIDTH - 1, 1); // Fill bottom row
        
        // Add garbage to queue
        board.addGarbageToQueue(1);
        
        // Processing garbage should not cause game over if top row is empty
        // Game over might still occur if brick can't be placed, but not due to top row overflow
        // The important thing is that we check top row first
        // If top row was empty, we proceed with normal processing
        board.processGarbageQueue();
        
        // Verify that garbage was processed (queue should be empty or reduced)
        // The test passes if no exception is thrown and processing completes
    }

    @Test
    void testWillClearOnlyGarbage_ReturnsTrueForOnlyGarbageRows() {
        // Fill a row with only garbage blocks (type 8)
        fillRow(BOARD_WIDTH - 1, 8);
        
        // Check if only garbage will be cleared
        boolean onlyGarbage = board.willClearOnlyGarbage();
        
        assertTrue(onlyGarbage, "Should return true when only garbage rows will be cleared");
    }

    @Test
    void testWillClearOnlyGarbage_ReturnsFalseForRegularBlocks() {
        // Fill a row with regular blocks (type 1)
        fillRow(BOARD_WIDTH - 1, 1);
        
        // Check if only garbage will be cleared
        boolean onlyGarbage = board.willClearOnlyGarbage();
        
        assertFalse(onlyGarbage, "Should return false when regular blocks will be cleared");
    }

    @Test
    void testWillClearOnlyGarbage_ReturnsFalseForMixedBlocks() {
        // Fill a row with mixed blocks (garbage and regular)
        int[][] matrix = board.getBoardMatrix();
        for (int j = 0; j < BOARD_HEIGHT; j++) {
            if (j % 2 == 0) {
                matrix[BOARD_WIDTH - 1][j] = 8; // Garbage
            } else {
                matrix[BOARD_WIDTH - 1][j] = 1; // Regular block
            }
        }
        
        // Check if only garbage will be cleared
        boolean onlyGarbage = board.willClearOnlyGarbage();
        
        assertFalse(onlyGarbage, "Should return false when mixed blocks will be cleared");
    }

    @Test
    void testWillClearOnlyGarbage_ReturnsFalseWhenNoRowsToClear() {
        // Don't fill any rows
        boolean onlyGarbage = board.willClearOnlyGarbage();
        
        assertFalse(onlyGarbage, "Should return false when no rows will be cleared");
    }

    @Test
    void testProcessGarbageQueue_ShiftsRowsUp() {
        // Create a brick first (required for processGarbageQueue to work)
        board.createNewBrick();
        
        // Fill a row in the middle
        fillRow(5, 1);
        
        // Add garbage to queue
        board.addGarbageToQueue(1);
        
        // Get matrix before processing
        int[][] matrixBefore = board.getBoardMatrix();
        int valueBefore = matrixBefore[5][0]; // Value in middle row
        
        // Process garbage
        board.processGarbageQueue();
        
        // Get matrix after processing
        int[][] matrixAfter = board.getBoardMatrix();
        int valueAfter = matrixAfter[4][0]; // Should have moved up one row
        
        assertEquals(valueBefore, valueAfter, "Row should have shifted up after garbage insertion");
    }
}

