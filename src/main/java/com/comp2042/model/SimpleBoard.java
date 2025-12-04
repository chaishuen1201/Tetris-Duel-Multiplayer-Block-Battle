package com.comp2042.model;

import com.comp2042.bricks.Brick;
import com.comp2042.logic.BrickGenerator;
import com.comp2042.logic.RandomBrickGenerator;
import com.comp2042.util.BrickRotator;
import com.comp2042.util.MatrixOperations;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Board interface representing a Tetris game board.
 * This class manages the complete game state including the game board matrix (2D array),
 * current active brick, held brick, score tracking, level progression, lines cleared count,
 * and garbage queue for multiplayer mode. It handles all brick operations including movement
 * (down, left, right), rotation with wall kick support, hard drop, hold functionality,
 * row clearing with score calculation, level progression based on lines cleared, and
 * garbage block processing. The board uses JavaFX properties for reactive UI binding
 * (level and lines), implements collision detection for all movements, and manages
 * garbage queue operations for multiplayer attack mechanics.
 */
public class SimpleBoard implements Board {

    private static final int INITIAL_X = 3;
    private static final int INITIAL_Y = -1;
    private static final int LINES_PER_LEVEL = 10;

    private final int width;
    private final int height;
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private int[][] currentGameMatrix;
    private Point currentOffset;
    private final Score score;
    private final IntegerProperty level = new SimpleIntegerProperty(1);
    private final IntegerProperty lines = new SimpleIntegerProperty(0);
    private Brick heldBrick;
    private boolean canHold = true;
    private Brick currentBrick;
    private GarbageQueue garbageQueue;

    /**
     * Creates a new SimpleBoard with the specified dimensions.
     * Initializes the game board matrix, brick generator, brick rotator, score tracker,
     * and garbage queue. The board starts empty with level 1 and 0 lines cleared.
     * 
     * @param width The width of the game board in cells (typically 10)
     * @param height The height of the game board in cells (typically 20)
     */
    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.currentGameMatrix = new int[width][height];
        this.brickGenerator = new RandomBrickGenerator();
        this.brickRotator = new BrickRotator();
        this.score = new Score();
        this.garbageQueue = new GarbageQueue(height); // Use height (columns) not width (rows)
    }

    /**
     * Moves the current brick down by one position.
     * Checks for collisions before moving. If a collision would occur, the brick
     * cannot move and false is returned.
     * 
     * @return true if the brick was successfully moved down, false if blocked by collision
     */
    @Override
    public boolean moveBrickDown() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point newPosition = new Point(currentOffset);
        newPosition.translate(0, 1);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), 
                (int) newPosition.getX(), (int) newPosition.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = newPosition;
            return true;
        }
    }

    /**
     * Moves the current brick one position to the left.
     * Checks for collisions before moving. If a collision would occur, the brick
     * cannot move and false is returned.
     * 
     * @return true if the brick was successfully moved left, false if blocked by collision
     */
    @Override
    public boolean moveBrickLeft() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point newPosition = new Point(currentOffset);
        newPosition.translate(-1, 0);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), 
                (int) newPosition.getX(), (int) newPosition.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = newPosition;
            return true;
        }
    }

    /**
     * Moves the current brick one position to the right.
     * Checks for collisions before moving. If a collision would occur, the brick
     * cannot move and false is returned.
     * 
     * @return true if the brick was successfully moved right, false if blocked by collision
     */
    @Override
    public boolean moveBrickRight() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point newPosition = new Point(currentOffset);
        newPosition.translate(1, 0);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), 
                (int) newPosition.getX(), (int) newPosition.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = newPosition;
            return true;
        }
    }

    /**
     * Rotates the current brick 90 degrees counter-clockwise.
     * Implements wall kick functionality, trying multiple offset positions if the
     * rotation at the current position would cause a collision. Attempts standard
     * wall kick offsets (left 1, right 1, left 2, right 2, up 1, and combinations)
     * to find a valid rotation position.
     * 
     * @return true if the brick was successfully rotated (at current position or with wall kick),
     *         false if no valid rotation position could be found
     */
    @Override
    public boolean rotateLeftBrick() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        NextShapeInfo nextShape = brickRotator.getNextShape();
        int[][] rotatedShape = nextShape.getShape();
        
        // Try rotation at current position first
        if (!MatrixOperations.intersect(currentMatrix, rotatedShape, 
                (int) currentOffset.getX(), (int) currentOffset.getY())) {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }
        
        // Wall kick: try different offset positions to find a valid rotation
        // Standard wall kick offsets: left 1, right 1, left 2, right 2, up 1
        int[][] wallKickOffsets = {
            {-1, 0},  // Try left 1
            {1, 0},   // Try right 1
            {-2, 0},  // Try left 2
            {2, 0},   // Try right 2
            {0, -1},  // Try up 1
            {-1, -1}, // Try left 1, up 1
            {1, -1}   // Try right 1, up 1
        };
        
        for (int[] offset : wallKickOffsets) {
            int newX = (int) currentOffset.getX() + offset[0];
            int newY = (int) currentOffset.getY() + offset[1];
            
            if (!MatrixOperations.intersect(currentMatrix, rotatedShape, newX, newY)) {
                // Valid rotation found with wall kick
                currentOffset = new Point(newX, newY);
                brickRotator.setCurrentShape(nextShape.getPosition());
                return true;
            }
        }
        
        // No valid rotation position found
        return false;
    }

    /**
     * Creates a new brick and places it at the initial spawn position.
     * First clears any completely filled rows (safety check), then generates a new
     * brick from the brick generator, resets the brick rotator, sets the initial
     * offset position, and resets the hold ability. Returns true if the new brick
     * cannot be placed at the spawn position (indicating game over).
     * 
     * @return true if the new brick cannot be placed (game over condition), false otherwise
     */
    @Override
    public boolean createNewBrick() {
        // First, clear any completely filled rows (including rubbish rows) before checking game over
        // This ensures that completely filled rubbish rows don't cause premature game over
        // Note: We clear rows here but don't update score/level - that's handled by clearRows() when called
        // This is a safety check to ensure rows are cleared before game over check
        ClearRow clearRow = MatrixOperations.checkRemoving(currentGameMatrix);
        if (clearRow.getLinesRemoved() > 0) {
            currentGameMatrix = clearRow.getNewMatrix();
            // Don't update score/level here - that's handled by clearRows() to avoid double-counting
            // This is just to ensure completely filled rows (including rubbish) are cleared before game over check
        }
        
        currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);
        currentOffset = new Point(INITIAL_X, INITIAL_Y);
        canHold = true; // Reset hold ability when new brick is created
        
        // Only return true (game over) if the brick intersects with blocks at the top
        // This means the game should only end when blocks reach the top, not when rubbish rows are full
        return MatrixOperations.intersect(currentGameMatrix, brickRotator.getCurrentShape(), 
                (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    /**
     * Gets the current game board matrix.
     * Returns the 2D array representing the game board state, where 0 represents
     * empty cells and non-zero values represent filled cells (with different values
     * for different block types, including 8 for garbage blocks).
     * 
     * @return The 2D integer array representing the game board matrix [width][height]
     */
    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    /**
     * Gets the current view data for rendering the game state.
     * Creates a ViewData object containing the current brick shape, position, and
     * the next brick preview shape.
     * 
     * @return ViewData containing the current brick shape, position coordinates, and next brick shape
     */
    @Override
    public ViewData getViewData() {
        return new ViewData(brickRotator.getCurrentShape(), 
                (int) currentOffset.getX(), 
                (int) currentOffset.getY(), 
                brickGenerator.getNextBrick().getShapeMatrix().get(0));
    }

    /**
     * Merges the current brick into the game board background.
     * Permanently places the current brick at its current position into the board matrix,
     * making it part of the static board state. This is called when a brick can no longer
     * move down.
     */
    @Override
    public void mergeBrickToBackground() {
        currentGameMatrix = MatrixOperations.merge(currentGameMatrix, brickRotator.getCurrentShape(), 
                (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    /**
     * Checks if the rows that will be cleared contain only garbage blocks (type 8).
     * This is used to determine if garbage should be sent to opponent.
     * @return true if rows will be cleared AND all of them contain only garbage blocks, false otherwise
     */
    public boolean willClearOnlyGarbage() {
        boolean foundRowToClear = false;
        // Check which rows will be cleared
        for (int i = 0; i < currentGameMatrix.length; i++) {
            boolean rowToClear = true;
            for (int j = 0; j < currentGameMatrix[i].length; j++) {
                if (currentGameMatrix[i][j] == 0) {
                    rowToClear = false;
                    break;
                }
            }
            if (rowToClear) {
                foundRowToClear = true;
                // This row will be cleared - check if it contains only garbage blocks (type 8)
                for (int j = 0; j < currentGameMatrix[i].length; j++) {
                    int cellValue = currentGameMatrix[i][j];
                    // If any cell is not garbage (type 8) and not empty (0), it's a regular block
                    if (cellValue != 0 && cellValue != 8) {
                        return false; // Found a regular block, not only garbage
                    }
                }
            }
        }
        // If we found rows to clear and all of them contain only garbage, return true
        // If no rows will be cleared, return false (so garbage will be sent if regular blocks are cleared)
        return foundRowToClear;
    }

    /**
     * Clears completed rows from the game board.
     * Removes all completely filled rows, shifts remaining rows down, updates the lines
     * cleared count, updates the level based on total lines cleared (1 level per 10 lines),
     * counters garbage from the queue based on lines cleared, and calculates score bonus
     * multiplied by the current level. Returns a ClearRow object with information about
     * the cleared rows and level-multiplied score bonus.
     * 
     * @return ClearRow containing the number of lines removed, the new board matrix,
     *         and the level-multiplied score bonus
     */
    @Override
    public ClearRow clearRows() {
        ClearRow clearRow = MatrixOperations.checkRemoving(currentGameMatrix);
        currentGameMatrix = clearRow.getNewMatrix();
        if (clearRow.getLinesRemoved() > 0) {
            
            // Counter incoming garbage in THIS player's queue only
            // This only affects this board's own garbageQueue, not the opponent's
            counterGarbage(clearRow.getLinesRemoved());
            
            int newLines = lines.get() + clearRow.getLinesRemoved();
            lines.set(newLines);
            // Update level based on lines cleared
            int newLevel = (newLines / LINES_PER_LEVEL) + 1;
            if (newLevel > level.get()) {
                level.set(newLevel);
            }
            // Multiply base score by current level (level is updated above)
            int levelMultipliedScore = clearRow.getScoreBonus() * level.get();
            // Return new ClearRow with level-multiplied score
            return new ClearRow(clearRow.getLinesRemoved(), clearRow.getNewMatrix(), levelMultipliedScore);
        }
        return clearRow;
    }

    /**
     * Gets the Score object for this board.
     * 
     * @return The Score instance tracking the player's score
     */
    @Override
    public Score getScore() {
        return score;
    }

    /**
     * Resets the board to start a new game.
     * Clears the game board matrix, resets the score, sets level to 1, sets lines to 0,
     * clears the held brick, resets hold ability, clears the garbage queue, and creates
     * a new initial brick.
     */
    @Override
    public void newGame() {
        currentGameMatrix = new int[width][height];
        score.reset();
        level.set(1);
        lines.set(0);
        heldBrick = null;
        canHold = true;
        if (garbageQueue != null) {
            garbageQueue.clear();
        }
        createNewBrick();
    }

    /**
     * Gets the level property for reactive UI binding.
     * The level increases by 1 for every 10 lines cleared.
     * 
     * @return The IntegerProperty representing the current level
     */
    public IntegerProperty levelProperty() {
        return level;
    }

    /**
     * Gets the lines property for reactive UI binding.
     * Tracks the total number of lines cleared in the current game.
     * 
     * @return The IntegerProperty representing the total lines cleared
     */
    public IntegerProperty linesProperty() {
        return lines;
    }

    /**
     * Gets the list of next bricks for preview display.
     * Returns the next 3 bricks if using RandomBrickGenerator, otherwise returns
     * a list containing just the next brick.
     * 
     * @return A list of Brick objects representing upcoming pieces for preview
     */
    public List<Brick> getNextBricks() {
        // Get the next 3 bricks for display
        if (brickGenerator instanceof RandomBrickGenerator) {
            return ((RandomBrickGenerator) brickGenerator).getNextBricks(3);
        }
        // Fallback: if not RandomBrickGenerator, return just the next brick
        List<Brick> nextBricks = new ArrayList<>();
        Brick next = brickGenerator.getNextBrick();
        if (next != null) {
            nextBricks.add(next);
        }
        return nextBricks;
    }

    /**
     * Gets the currently held brick.
     * 
     * @return The Brick object that is currently held, or null if no brick is held
     */
    public Brick getHeldBrick() {
        return heldBrick;
    }

    /**
     * Performs a hard drop, instantly moving the brick to the bottom of the board.
     * Repeatedly moves the brick down until it can no longer move, then returns
     * the number of cells dropped. This count is used for calculating hard drop
     * bonus points (typically 2 points per cell).
     * 
     * @return The number of cells the brick was dropped
     */
    public int hardDrop() {
        int cellsDropped = 0;
        while (moveBrickDown()) {
            cellsDropped++;
        }
        return cellsDropped;
    }
    
    /**
     * Helper method to count non-zero cells in a brick shape matrix.
     * Used for calculating soft drop points based on the number of cells in the brick.
     * 
     * @param shape The 2D array representing the brick shape
     * @return The number of non-zero cells in the shape
     */
    private int countCellsInShape(int[][] shape) {
        int count = 0;
        for (int[] row : shape) {
            for (int cell : row) {
                if (cell != 0) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Gets the number of cells in the current brick shape.
     * Used for calculating soft drop points (1 point per cell moved).
     * 
     * @return The number of non-zero cells in the current brick shape
     */
    public int getCurrentBrickCellCount() {
        return countCellsInShape(brickRotator.getCurrentShape());
    }
    
    /**
     * Calculates the ghost position (where the brick will land if dropped).
     * Simulates dropping the brick down until it hits a collision, returning the
     * position where it would land. This is used to display a ghost piece preview
     * showing where the current brick will land.
     * 
     * @return The Point representing the position where the brick would land,
     *         or the current position if the brick is null
     */
    public Point getGhostPosition() {
        if (currentBrick == null) {
            return new Point(currentOffset);
        }
        
        int[][] shape = brickRotator.getCurrentShape();
        Point ghostPos = new Point(currentOffset);
        
        // Simulate dropping the brick down until it hits something
        while (true) {
            Point testPos = new Point(ghostPos);
            testPos.translate(0, 1);
            
            // Check if moving down would cause a collision
            if (MatrixOperations.intersect(currentGameMatrix, shape, 
                    (int) testPos.getX(), (int) testPos.getY())) {
                // Found the landing position
                break;
            }
            
            ghostPos = testPos;
        }
        
        return ghostPos;
    }

    /**
     * Holds the current brick and swaps with the previously held brick.
     * If no brick is currently held, stores the current brick and creates a new one.
     * If a brick is already held, swaps the current brick with the held brick.
     * The hold ability is disabled after use and resets when a new brick is created.
     * This prevents holding the same brick multiple times in a row.
     */
    public void holdBrick() {
        if (!canHold || currentBrick == null) {
            return;
        }

        if (heldBrick == null) {
            // First hold - just store current and get new brick
            heldBrick = currentBrick;
            currentBrick = brickGenerator.getBrick();
            brickRotator.setBrick(currentBrick);
            currentOffset = new Point(INITIAL_X, INITIAL_Y);
        } else {
            // Swap held brick with current brick
            Brick temp = heldBrick;
            heldBrick = currentBrick;
            currentBrick = temp;
            brickRotator.setBrick(currentBrick);
            currentOffset = new Point(INITIAL_X, INITIAL_Y);
        }
        canHold = false;
    }
    
    /**
     * Adds garbage lines to the queue for later processing.
     * Garbage lines are added to the queue and processed gradually (one every 2 seconds)
     * to give players time to react. This is used in multiplayer mode when opponents
     * send garbage attacks.
     * 
     * @param numLines The number of garbage lines to add to the queue
     */
    public void addGarbageToQueue(int numLines) {
        if (garbageQueue != null && numLines > 0) {
            garbageQueue.addGarbage(numLines);
        }
    }
    
    /**
     * Processes pending garbage from the queue and adds it to the bottom of the board.
     * 
     * The matrix is structured as [width][height] = [20][10].
     * Looking at checkRemoving and how the matrix is accessed:
     * - matrix.length = 20 (width)
     * - matrix[i].length = 10 (height)
     * - matrix[i] represents a row (i from 0 to 19)
     * - matrix[i][j] represents column j in row i (j from 0 to 9)
     * So we have 20 rows and 10 columns, which matches BOARD_HEIGHT=20 and BOARD_WIDTH=10.
     * The bottom row is at index width-1 = 19.
     * 
     * @return true if garbage was successfully added to the board, false if the queue was empty or processing failed
     */
    public boolean processGarbageQueue() {
        if (garbageQueue == null || garbageQueue.isEmpty()) {
            return false;
        }
        
        int[] garbageLine = garbageQueue.pollGarbageLine();
        if (garbageLine == null) {
            return false;
        }
        
        // Check if the top row (row 0) contains any blocks before shifting
        // If it does, shifting will push blocks off the board, causing game over
        boolean topRowHasBlocks = false;
        for (int col = 0; col < height; col++) {
            if (currentGameMatrix[0][col] != 0) {
                topRowHasBlocks = true;
                break;
            }
        }
        
        // If top row has blocks, shifting will push them off the board = game over
        if (topRowHasBlocks) {
            return true; // Indicates game over due to blocks being pushed off
        }
        
        // The matrix is [width][height] = [20][10]
        // matrix[i] is a row (i from 0 to 19), matrix[i][j] is column j in row i (j from 0 to 9)
        // We need to shift all rows up: move row i+1 to row i
        for (int row = 0; row < width - 1; row++) {
            System.arraycopy(currentGameMatrix[row + 1], 0, currentGameMatrix[row], 0, height);
        }
        
        // Add the garbage line at the bottom row (width - 1 = 19, which is the last row)
        System.arraycopy(garbageLine, 0, currentGameMatrix[width - 1], 0, height);
        
        // Check if game over (if new brick can't be placed)
        if (MatrixOperations.intersect(currentGameMatrix, brickRotator.getCurrentShape(), 
                (int) currentOffset.getX(), (int) currentOffset.getY())) {
            return true; // Indicates potential game over
        }
        
        return false;
    }
    
    /**
     * Counters incoming garbage by removing lines from THIS player's queue when THIS player clears lines.
     * This method only affects the garbage queue of the board instance it's called on.
     * Each player has their own isolated garbageQueue, so this will never affect the opponent's queue.
     * 
     * @param linesCleared Number of lines cleared by THIS player
     * @return Number of garbage lines that were countered from THIS player's queue
     */
    public int counterGarbage(int linesCleared) {
        if (garbageQueue == null || linesCleared <= 0) {
            return 0;
        }
        
        // Each line cleared counters one garbage line from THIS player's own queue
        // This garbageQueue is instance-specific and cannot affect other players' queues
        return garbageQueue.removeGarbage(linesCleared);
    }
    
    /**
     * Gets the number of pending garbage lines in the queue.
     * 
     * @return The number of garbage lines waiting to be processed, or 0 if the queue is empty or null
     */
    public int getPendingGarbageCount() {
        return garbageQueue != null ? garbageQueue.size() : 0;
    }
    
    /**
     * Gets the garbage queue instance for this board.
     * Each board has its own isolated garbage queue that cannot affect other players.
     * 
     * @return The GarbageQueue instance for this board, or null if not initialized
     */
    public GarbageQueue getGarbageQueue() {
        return garbageQueue;
    }
}

