package com.comp2042.util;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;
import com.comp2042.model.NextShapeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrickRotatorTest {

    private BrickRotator rotator;
    private Brick brick;

    @BeforeEach
    void setUp() {
        rotator = new BrickRotator();
        BrickGenerator generator = new RandomBrickGenerator();
        brick = generator.getBrick();
        rotator.setBrick(brick);
    }

    @Test
    void testInitialShape() {
        int[][] shape = rotator.getCurrentShape();
        assertNotNull(shape);
        assertTrue(shape.length > 0);
    }

    @Test
    void testGetNextShape() {
        NextShapeInfo nextShape = rotator.getNextShape();
        assertNotNull(nextShape);
        assertNotNull(nextShape.getShape());
        assertTrue(nextShape.getPosition() >= 0);
    }

    @Test
    void testSetCurrentShape() {
        NextShapeInfo nextShape = rotator.getNextShape();
        int position = nextShape.getPosition();
        
        rotator.setCurrentShape(position);
        int[][] currentShape = rotator.getCurrentShape();
        
        assertArrayEquals(nextShape.getShape(), currentShape);
    }

    @Test
    void testSetBrick() {
        BrickGenerator generator = new RandomBrickGenerator();
        Brick newBrick = generator.getBrick();
        rotator.setBrick(newBrick);
        
        int[][] shape = rotator.getCurrentShape();
        assertNotNull(shape);
    }

    @Test
    void testRotationCycle() {
        int[][] initialShape = rotator.getCurrentShape();
        NextShapeInfo nextShape = rotator.getNextShape();
        
        rotator.setCurrentShape(nextShape.getPosition());
        int[][] rotatedShape = rotator.getCurrentShape();
        
        // Shapes should be different after rotation
        assertNotEquals(initialShape.length, rotatedShape.length, 
                "Shape dimensions might change after rotation");
    }
}

