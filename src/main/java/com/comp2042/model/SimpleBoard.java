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

    private static final int INITIAL_X = 4;
    private static final int INITIAL_Y = 10;
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
        boolean conflict = MatrixOperations.intersect(currentMatrix, nextShape.getShape(), 
                (int) currentOffset.getX(), (int) currentOffset.getY());
        if (conflict) {
            return false;
        } else {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }
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
        List<Brick> nextBricks = new ArrayList<>();
        // Get the next brick (peek without removing)
        Brick next = brickGenerator.getNextBrick();
        if (next != null) {
            nextBricks.add(next);
        }
        // For now, just return the next brick. In a full implementation,
        // you might want to peek ahead at more bricks
        return nextBricks;
    }

    public Brick getHeldBrick() {
        return heldBrick;
    }

    public void hardDrop() {
        while (moveBrickDown()) {
            // Keep moving down until it can't move anymore
        }
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

