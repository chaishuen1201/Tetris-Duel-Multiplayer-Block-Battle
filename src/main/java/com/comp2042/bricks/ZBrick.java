package com.comp2042.bricks;

import com.comp2042.util.MatrixOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Z-shaped brick (tetromino).
 * Represents the Z-shaped piece with 2 rotation states (symmetric).
 */
public final class ZBrick implements Brick {

    private final List<int[][]> brickMatrix = new ArrayList<>();

    /**
     * Constructs a Z-shaped brick and initializes its rotation states.
     */
    public ZBrick() {
        brickMatrix.add(new int[][]{
                {0, 0, 0, 0},
                {7, 7, 0, 0},
                {0, 7, 7, 0},
                {0, 0, 0, 0}
        });
        brickMatrix.add(new int[][]{
                {0, 7, 0, 0},
                {7, 7, 0, 0},
                {7, 0, 0, 0},
                {0, 0, 0, 0}
        });
    }

    @Override
    public List<int[][]> getShapeMatrix() {
        return MatrixOperations.deepCopyList(brickMatrix);
    }
}
