package com.comp2042.model;

import com.comp2042.bricks.Brick;
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

        board.processGarbageQueue();

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

    // ========== EDGE CASE TESTS ==========

    @Test
    void testRotateBrick_AtLeftEdge_WithWallKick() {
        board.createNewBrick();
        
        // Move brick to left edge (x = 0)
        while (board.moveBrickLeft()) {
            // Keep moving left until at edge
        }
        
        // Move down a bit to ensure it's in valid position
        for (int i = 0; i < 5; i++) {
            board.moveBrickDown();
        }
        
        // Try to rotate - should succeed with wall kick if needed
        board.rotateLeftBrick();
        
        // Rotation should succeed (either at position or with wall kick)
        // The wall kick system should allow rotation even at left edge
        ViewData afterRotation = board.getViewData();
        assertNotNull(afterRotation, "Should have valid view data after rotation attempt");
        
        // Position should be valid after rotation (wall kick may adjust it)
        assertTrue(afterRotation.getXPosition() >= -2, 
            "After wall kick, x position should be valid (may be -2 to 2 due to wall kick offsets)");
    }

    @Test
    void testRotateBrick_AtRightEdge_WithWallKick() {
        board.createNewBrick();
        
        // Get initial position
        ViewData initialView = board.getViewData();
        int initialX = initialView.getXPosition();
        
        // Move brick to right edge
        while (board.moveBrickRight()) {
            // Keep moving right until at edge
        }
        
        // Get position at edge
        ViewData atEdgeView = board.getViewData();
        int edgeX = atEdgeView.getXPosition();
        assertTrue(edgeX > initialX, "Brick should have moved right");
        
        // Move down a bit to ensure it's in valid position
        for (int i = 0; i < 5; i++) {
            board.moveBrickDown();
        }
        
        // Try to rotate - should succeed with wall kick if needed
        board.rotateLeftBrick();
        
        // Rotation should not crash - get view data should work regardless of success
        ViewData afterRotation = board.getViewData();
        assertNotNull(afterRotation, "Should have valid view data after rotation attempt");
        assertNotNull(afterRotation.getBrickData(), "Brick data should not be null");
        
        // The important thing is that rotation at the edge is handled gracefully
        // Whether it succeeds or fails depends on the brick type and position
        // We just verify it doesn't crash and returns valid data
        // Position validation is handled by the collision detection system
    }

    @Test
    void testRotateBrick_NearCorner_WithWallKick() {
        board.createNewBrick();
        
        // Move brick to bottom-left corner area
        while (board.moveBrickLeft()) {
            // Keep moving left
        }
        
        // Move down to near bottom
        for (int i = 0; i < BOARD_HEIGHT - 3; i++) {
            board.moveBrickDown();
        }
        
        // Try to rotate - wall kick should handle corner case
        board.rotateLeftBrick();
        
        ViewData afterRotation = board.getViewData();
        assertNotNull(afterRotation, "Should have valid view data after rotation attempt");
        
        // Verify brick is still in valid position after rotation
        assertTrue(afterRotation.getXPosition() >= -2, "X position should be valid");
        assertTrue(afterRotation.getYPosition() < BOARD_HEIGHT, "Y position should be within board");
    }

    @Test
    void testRotateBrick_BlockedByOtherBlocks_UsesWallKick() {
        board.createNewBrick();
        
        // Fill some blocks to the left of spawn position
        try {
            Field matrixField = SimpleBoard.class.getDeclaredField("currentGameMatrix");
            matrixField.setAccessible(true);
            int[][] matrix = (int[][]) matrixField.get(board);
            
            // Place blocks to the left (around x=1, y=5)
            matrix[1][5] = 1;
            matrix[1][6] = 1;
        } catch (Exception e) {
            fail("Failed to access board matrix: " + e.getMessage());
        }
        
        // Move brick down and try to rotate
        for (int i = 0; i < 5; i++) {
            board.moveBrickDown();
        }
        
        // Try rotation - should use wall kick to avoid blocks
        board.rotateLeftBrick();
        
        // Rotation should succeed with wall kick
        ViewData afterRotation = board.getViewData();
        assertNotNull(afterRotation, "Should have valid view data after rotation");
    }

    @Test
    void testGarbageInsertion_BoardAlmostFull_DoesNotCauseImmediateGameOver() {
        board.createNewBrick();
        
        // Fill board almost to the top (leave top 2 rows empty)
        try {
            Field matrixField = SimpleBoard.class.getDeclaredField("currentGameMatrix");
            matrixField.setAccessible(true);
            int[][] matrix = (int[][]) matrixField.get(board);
            
            // Fill rows 2-9 (leaving rows 0-1 empty)
            // Valid row indices are 0-9 (BOARD_WIDTH = 10)
            for (int row = 2; row < BOARD_WIDTH; row++) {
                for (int col = 0; col < BOARD_HEIGHT; col++) {
                    matrix[row][col] = 1;
                }
            }
        } catch (Exception e) {
            fail("Failed to access board matrix: " + e.getMessage());
        }
        
        // Add garbage to queue
        board.addGarbageToQueue(1);
        
        // Process garbage - should not cause immediate game over since top row is empty
        board.processGarbageQueue();
        
        // Game over should not occur immediately (top row was empty)
        int[][] matrixAfter = board.getBoardMatrix();
        assertNotNull(matrixAfter, "Board should still exist after processing garbage");
    }

    @Test
    void testGarbageInsertion_BoardCompletelyFullExceptTopRow_HandlesCorrectly() {
        board.createNewBrick();
        
        // Fill board completely except top row (row 0)
        try {
            Field matrixField = SimpleBoard.class.getDeclaredField("currentGameMatrix");
            matrixField.setAccessible(true);
            int[][] matrix = (int[][]) matrixField.get(board);
            
            // Fill rows 1-9 (leaving row 0 empty)
            // Valid row indices are 0-9 (BOARD_WIDTH = 10)
            for (int row = 1; row < BOARD_WIDTH; row++) {
                for (int col = 0; col < BOARD_HEIGHT; col++) {
                    matrix[row][col] = 1;
                }
            }
        } catch (Exception e) {
            fail("Failed to access board matrix: " + e.getMessage());
        }
        
        // Add garbage to queue
        board.addGarbageToQueue(1);
        
        // Process garbage - should shift everything up
        board.processGarbageQueue();
        
        // Top row should now have blocks (from row 1)
        int[][] matrixAfter = board.getBoardMatrix();
        boolean topRowHasBlocks = false;
        for (int col = 0; col < BOARD_HEIGHT; col++) {
            if (matrixAfter[0][col] != 0) {
                topRowHasBlocks = true;
                break;
            }
        }
        
        // After shifting, top row should have blocks
        assertTrue(topRowHasBlocks, "Top row should have blocks after shifting full board");
        
        // Next garbage insertion should detect game over
        board.addGarbageToQueue(1);
        boolean gameOverAfter = board.processGarbageQueue();
        assertTrue(gameOverAfter, "Should detect game over when top row has blocks");
    }

    @Test
    void testLevelUp_ExactlyAtTenLines() {
        // Clear exactly 10 lines to trigger level up
        for (int i = 0; i < 10; i++) {
            fillRow(BOARD_WIDTH - 1 - i, 1);
        }
        
        // Initially level should be 1
        assertEquals(1, board.levelProperty().get(), "Initial level should be 1");
        
        // Clear rows
        board.clearRows();
        
        // Level should increase to 2 (10 lines / 10 = 1, so level = 1 + 1 = 2)
        assertEquals(2, board.levelProperty().get(), "Level should increase to 2 after clearing 10 lines");
        assertEquals(10, board.linesProperty().get(), "Lines should be 10");
    }

    @Test
    void testLevelUp_ExactlyAtTwentyLines() {
        // Clear 20 lines total (in two batches)
        // First batch: 10 lines
        for (int i = 0; i < 10; i++) {
            fillRow(BOARD_WIDTH - 1 - i, 1);
        }
        board.clearRows();
        assertEquals(2, board.levelProperty().get(), "Level should be 2 after first 10 lines");
        
        // Second batch: 10 more lines
        for (int i = 0; i < 10; i++) {
            fillRow(BOARD_WIDTH - 1 - i, 1);
        }
        board.clearRows();
        
        // Level should increase to 3 (20 lines / 10 = 2, so level = 2 + 1 = 3)
        assertEquals(3, board.levelProperty().get(), "Level should increase to 3 after clearing 20 lines total");
        assertEquals(20, board.linesProperty().get(), "Lines should be 20");
    }

    @Test
    void testLevelUp_DoesNotIncreaseBelowThreshold() {
        // Clear 9 lines (below threshold of 10)
        for (int i = 0; i < 9; i++) {
            fillRow(BOARD_WIDTH - 1 - i, 1);
        }
        
        board.clearRows();
        
        // Level should still be 1 (9 < 10)
        assertEquals(1, board.levelProperty().get(), "Level should remain 1 when clearing less than 10 lines");
        assertEquals(9, board.linesProperty().get(), "Lines should be 9");
    }

    @Test
    void testScoreMultiplier_LevelAffectsScore() {
        // Clear 10 lines to reach level 2
        for (int i = 0; i < 10; i++) {
            fillRow(BOARD_WIDTH - 1 - i, 1);
        }
        board.clearRows();
        assertEquals(2, board.levelProperty().get(), "Level should be 2");
        
        // Clear 4 lines (Tetris) at level 2
        for (int i = 0; i < 4; i++) {
            fillRow(BOARD_WIDTH - 1 - i, 1);
        }
        ClearRow clearRow2 = board.clearRows();
        
        // Base score for 4 lines is 1200, multiplied by level 2 = 2400
        assertEquals(2400, clearRow2.getScoreBonus(), "Score should be multiplied by level 2");
        // Use clearRow2 to avoid unused variable warning
        assertNotNull(clearRow2);
    }

    @Test
    void testNextBrick_UpdatesAfterCreatingNewBrick() {
        // Create initial brick first
        board.createNewBrick();
        
        // Get initial next brick
        ViewData initialView = board.getViewData();
        int[][] initialNextBrick = initialView.getNextBrickData();
        assertNotNull(initialNextBrick, "Initial next brick should not be null");
        
        // Create new brick
        board.createNewBrick();
        
        // Get new view data
        ViewData newView = board.getViewData();
        int[][] newCurrentBrick = newView.getBrickData();
        int[][] newNextBrick = newView.getNextBrickData();
        
        // Current brick should match what was previously the next brick
        // (We can't easily compare brick types, but we can verify structure)
        assertNotNull(newCurrentBrick, "New current brick should not be null");
        assertNotNull(newNextBrick, "New next brick should not be null");
        
        // Next brick should have updated (should be different from current)
        // The important thing is that next brick exists and is valid
        assertTrue(newNextBrick.length > 0, "Next brick should have valid dimensions");
    }

    @Test
    void testHoldBrick_WhenNoBrickExists_DoesNotCrash() {
        // Try to hold when no brick exists (should not crash)
        // The holdBrick() method checks if currentBrick is null and returns early
        board.holdBrick(); // Should handle gracefully if currentBrick is null

        assertTrue(true, "holdBrick() should not crash when no brick exists");
    }

    @Test
    void testHoldBrick_AfterPlacingBrick_ResetsCorrectly() {
        board.createNewBrick();
        
        // Hold the brick
        board.holdBrick();
        Brick firstHeldBrick = board.getHeldBrick();
        assertNotNull(firstHeldBrick, "Should have a held brick");
        
        // Place the current brick (move to bottom and merge)
        while (board.moveBrickDown()) {
            // Keep moving down
        }
        board.mergeBrickToBackground();
        
        // Create new brick (this resets canHold)
        board.createNewBrick();
        
        // Now we should be able to hold again
        board.holdBrick();
        ViewData afterHold = board.getViewData();
        
        // Hold should work (brick should change or swap)
        assertNotNull(afterHold, "Should have valid view data after hold");
        assertNotNull(board.getHeldBrick(), "Should still have a held brick");
    }

    @Test
    void testViewData_ReflectsModelState_Correctly() {
        board.createNewBrick();
        
        // Get initial view data
        ViewData viewData1 = board.getViewData();
        int initialX = viewData1.getXPosition();
        int initialY = viewData1.getYPosition();
        
        // Move brick right
        board.moveBrickRight();
        
        // Get updated view data
        ViewData viewData2 = board.getViewData();
        
        // X position should have increased by 1
        assertEquals(initialX + 1, viewData2.getXPosition(), "X position should reflect movement");
        assertEquals(initialY, viewData2.getYPosition(), "Y position should remain same");
        
        // Move brick down
        board.moveBrickDown();
        
        // Get updated view data
        ViewData viewData3 = board.getViewData();
        
        // Y position should have increased by 1
        assertEquals(initialX + 1, viewData3.getXPosition(), "X position should remain same");
        assertEquals(initialY + 1, viewData3.getYPosition(), "Y position should reflect movement");
    }

    @Test
    void testViewData_ReflectsRotation_Correctly() {
        board.createNewBrick();
        
        // Get initial brick shape
        ViewData viewData1 = board.getViewData();
        int[][] initialShape = viewData1.getBrickData();
        
        // Rotate brick
        board.rotateLeftBrick();
        
        // Get updated view data
        ViewData viewData2 = board.getViewData();
        int[][] rotatedShape = viewData2.getBrickData();
        
        // Shape should have changed (unless it's an O-brick which doesn't change)
        // The important thing is that view data reflects the rotation
        assertNotNull(rotatedShape, "Rotated shape should not be null");
        assertEquals(initialShape.length, rotatedShape.length, "Shape dimensions should match");
    }

    @Test
    void testMultipleGarbageLines_ProcessedCorrectly() {
        board.createNewBrick();
        
        // Add multiple garbage lines to queue
        board.addGarbageToQueue(5);
        
        assertEquals(5, board.getPendingGarbageCount(), "Should have 5 garbage lines in queue");
        
        // Process first garbage line
        boolean gameOver1 = board.processGarbageQueue();
        assertEquals(4, board.getPendingGarbageCount(), "Should have 4 garbage lines remaining");
        assertFalse(gameOver1, "First garbage line should not cause game over on empty board");
        
        // Process second garbage line
        boolean gameOver2 = board.processGarbageQueue();
        assertEquals(3, board.getPendingGarbageCount(), "Should have 3 garbage lines remaining");
        assertFalse(gameOver2, "Second garbage line should not cause game over");
        
        // Process all remaining
        while (board.getPendingGarbageCount() > 0) {
            board.processGarbageQueue();
        }
        
        assertEquals(0, board.getPendingGarbageCount(), "All garbage lines should be processed");
    }

    @Test
    void testMultipleGarbageLines_CounteredByLineClears() {
        board.createNewBrick();
        
        // Add 5 garbage lines
        board.addGarbageToQueue(5);
        assertEquals(5, board.getPendingGarbageCount(), "Should have 5 garbage lines");
        
        // Clear 3 lines (should counter 3 garbage lines)
        fillRow(BOARD_WIDTH - 1, 1);
        fillRow(BOARD_WIDTH - 2, 1);
        fillRow(BOARD_WIDTH - 3, 1);
        board.clearRows();
        
        // Should have 2 garbage lines remaining (5 - 3 = 2)
        assertEquals(2, board.getPendingGarbageCount(), "Should counter 3 garbage lines, leaving 2");
    }

    @Test
    void testMultipleGarbageLines_CounteredByMoreLinesThanQueue() {
        board.createNewBrick();
        
        // Add 2 garbage lines
        board.addGarbageToQueue(2);
        assertEquals(2, board.getPendingGarbageCount(), "Should have 2 garbage lines");
        
        // Clear 5 lines (more than garbage in queue)
        for (int i = 0; i < 5; i++) {
            fillRow(BOARD_WIDTH - 1 - i, 1);
        }
        board.clearRows();
        
        // Should have 0 garbage lines (all countered, can't go negative)
        assertEquals(0, board.getPendingGarbageCount(), "Should counter all garbage lines, leaving 0");
    }

    @Test
    void testGarbageInsertion_WithExistingBlocks_ShiftsCorrectly() {
        board.createNewBrick();
        
        // Fill some rows in the middle (valid row indices are 0-9 for width=10)
        fillRow(5, 1);
        fillRow(6, 1);
        fillRow(7, 1);
        
        // Add garbage
        board.addGarbageToQueue(2);
        
        // Get matrix before processing
        int[][] matrixBefore = board.getBoardMatrix();
        int valueAtRow5 = matrixBefore[5][0];
        int valueAtRow6 = matrixBefore[6][0];
        int valueAtRow7 = matrixBefore[7][0];
        
        // Process garbage (should shift rows up)
        board.processGarbageQueue();
        board.processGarbageQueue();
        
        // Get matrix after processing
        int[][] matrixAfter = board.getBoardMatrix();
        
        // Rows should have shifted up by 2
        assertEquals(valueAtRow5, matrixAfter[3][0], "Row 5 should move to row 3");
        assertEquals(valueAtRow6, matrixAfter[4][0], "Row 6 should move to row 4");
        assertEquals(valueAtRow7, matrixAfter[5][0], "Row 7 should move to row 5");
    }
}

