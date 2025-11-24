package com.comp2042.model;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;
import com.comp2042.util.BrickRotator;
import com.comp2042.util.MatrixOperations;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

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

    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.currentGameMatrix = new int[width][height];
        this.brickGenerator = new RandomBrickGenerator();
        this.brickRotator = new BrickRotator();
        this.score = new Score();
        this.garbageQueue = new GarbageQueue(height); // Use height (columns) not width (rows)
    }

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

    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    @Override
    public ViewData getViewData() {
        return new ViewData(brickRotator.getCurrentShape(), 
                (int) currentOffset.getX(), 
                (int) currentOffset.getY(), 
                brickGenerator.getNextBrick().getShapeMatrix().get(0));
    }

    @Override
    public void mergeBrickToBackground() {
        currentGameMatrix = MatrixOperations.merge(currentGameMatrix, brickRotator.getCurrentShape(), 
                (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    @Override
    public ClearRow clearRows() {
        ClearRow clearRow = MatrixOperations.checkRemoving(currentGameMatrix);
        currentGameMatrix = clearRow.getNewMatrix();
        if (clearRow.getLinesRemoved() > 0) {
            // IMPORTANT: Only remove garbage from THIS player's own board and queue
            // Each player has their own isolated garbageQueue instance
            
            // Rubbish rows are only cleared by checkRemoving when they become completely filled
            // (all cells non-zero, including filling the empty cell), just like normal rows.
            // They are NOT removed as a reward when other lines are cleared.
            
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

    @Override
    public Score getScore() {
        return score;
    }

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

    public IntegerProperty levelProperty() {
        return level;
    }

    public IntegerProperty linesProperty() {
        return lines;
    }

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

    public Brick getHeldBrick() {
        return heldBrick;
    }

    public int hardDrop() {
        int cellsDropped = 0;
        while (moveBrickDown()) {
            cellsDropped++;
        }
        return cellsDropped;
    }
    
    // Helper method to count non-zero cells in a brick shape
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
    
    // Get the number of cells in the current brick shape
    public int getCurrentBrickCellCount() {
        return countCellsInShape(brickRotator.getCurrentShape());
    }
    
    // Calculate the ghost position (where the brick will land)
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
     * Adds garbage lines to the queue.
     * @param numLines Number of garbage lines to add
     */
    public void addGarbageToQueue(int numLines) {
        if (garbageQueue != null && numLines > 0) {
            garbageQueue.addGarbage(numLines);
        }
    }
    
    /**
     * Processes pending garbage from the queue and adds it to the bottom of the board.
     * Returns true if garbage was added, false otherwise.
     * 
     * The matrix is structured as [width][height] = [20][10].
     * Looking at checkRemoving and how the matrix is accessed:
     * - matrix.length = 20 (width)
     * - matrix[i].length = 10 (height)
     * - matrix[i] represents a row (i from 0 to 19)
     * - matrix[i][j] represents column j in row i (j from 0 to 9)
     * So we have 20 rows and 10 columns, which matches BOARD_HEIGHT=20 and BOARD_WIDTH=10.
     * The bottom row is at index width-1 = 19.
     */
    public boolean processGarbageQueue() {
        if (garbageQueue == null || garbageQueue.isEmpty()) {
            return false;
        }
        
        int[] garbageLine = garbageQueue.pollGarbageLine();
        if (garbageLine == null) {
            return false;
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
     * Gets the number of pending garbage lines.
     */
    public int getPendingGarbageCount() {
        return garbageQueue != null ? garbageQueue.size() : 0;
    }
    
    /**
     * Gets the garbage queue (for external access if needed).
     */
    public GarbageQueue getGarbageQueue() {
        return garbageQueue;
    }
}

