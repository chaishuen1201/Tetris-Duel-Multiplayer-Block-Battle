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

    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.currentGameMatrix = new int[width][height];
        this.brickGenerator = new RandomBrickGenerator();
        this.brickRotator = new BrickRotator();
        this.score = new Score();
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
        currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);
        currentOffset = new Point(INITIAL_X, INITIAL_Y);
        canHold = true; // Reset hold ability when new brick is created
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
}

