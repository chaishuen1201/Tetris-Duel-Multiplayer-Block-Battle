package com.comp2042.util;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.model.NextShapeInfo;

public final class BrickRotator {

    private Brick brick;
    private int currentShape = 0;

    public NextShapeInfo getNextShape() {
        int nextShape = (currentShape + 1) % brick.getShapeMatrix().size();
        return new NextShapeInfo(brick.getShapeMatrix().get(nextShape), nextShape);
    }

    public int[][] getCurrentShape() {
        return brick.getShapeMatrix().get(currentShape);
    }

    public void setCurrentShape(int currentShape) {
        this.currentShape = currentShape;
    }

    public void setBrick(Brick brick) {
        this.brick = brick;
        currentShape = 0;
    }
}

