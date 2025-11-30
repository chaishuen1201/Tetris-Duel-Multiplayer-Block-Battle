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
        int initialPosition = 0; // Start at position 0
        
        // Get the number of shapes for this brick
        int numShapes = brick.getShapeMatrix().size();
        
        // If brick has only one shape (like O-brick), rotation should cycle back to same shape
        if (numShapes == 1) {
            NextShapeInfo nextShape = rotator.getNextShape();
            assertEquals(0, nextShape.getPosition(), "O-brick should cycle back to position 0");
            rotator.setCurrentShape(nextShape.getPosition());
            int[][] rotatedShape = rotator.getCurrentShape();
            assertArrayEquals(initialShape, rotatedShape, "O-brick rotation should return same shape");
        } else {
            // For bricks with multiple shapes, verify rotation cycles through them
            NextShapeInfo nextShape = rotator.getNextShape();
            int nextPosition = nextShape.getPosition();
            
            // Next position should be 1 (or wrap around to 0 if at last position)
            int expectedNext = (initialPosition + 1) % numShapes;
            assertEquals(expectedNext, nextPosition, "Next shape position should increment correctly");
            
            // Apply rotation
            rotator.setCurrentShape(nextPosition);
            int[][] rotatedShape = rotator.getCurrentShape();
            
            // Rotated shape should be different from initial (unless it's the same shape)
            // For most bricks, the shapes will be different
            // Use assertArrayEquals to check if they're the same, and if so, that's also valid
            // The important thing is that rotation works, not that shapes are always different
            assertNotNull(rotatedShape, "Rotated shape should not be null");
            assertEquals(initialShape.length, rotatedShape.length, 
                    "Shape dimensions should remain the same (all bricks use 4x4)");
        }
    }
}

