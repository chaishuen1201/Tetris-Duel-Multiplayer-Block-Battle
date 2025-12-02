package com.comp2042.bricks;

import com.comp2042.util.MatrixOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the O-shaped brick (tetromino).
 * Represents the square piece with only 1 rotation state (symmetric).
 */
public final class OBrick implements Brick {

    private final List<int[][]> brickMatrix = new ArrayList<>();

    /**
     * Constructs an O-shaped brick and initializes its rotation states.
     */
    public OBrick() {
        brickMatrix.add(new int[][]{
                {0, 0, 0, 0},
                {0, 4, 4, 0},
                {0, 4, 4, 0},
                {0, 0, 0, 0}
        });
    }

    @Override
    public List<int[][]> getShapeMatrix() {
        return MatrixOperations.deepCopyList(brickMatrix);
    }

}
