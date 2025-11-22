package com.comp2042.model;

import javafx.beans.property.IntegerProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreTest {

    private Score score;

    @BeforeEach
    void setUp() {
        score = new Score();
    }

    @Test
    void testInitialScore() {
        IntegerProperty scoreProperty = score.scoreProperty();
        assertEquals(0, scoreProperty.get());
    }

    @Test
    void testAdd() {
        score.add(10);
        assertEquals(10, score.scoreProperty().get());

        score.add(5);
        assertEquals(15, score.scoreProperty().get());
    }

    @Test
    void testReset() {
        score.add(100);
        assertEquals(100, score.scoreProperty().get());

        score.reset();
        assertEquals(0, score.scoreProperty().get());
    }

    @Test
    void testScoreProperty() {
        IntegerProperty property = score.scoreProperty();
        assertNotNull(property);

        // Test that property is observable
        score.add(50);
        assertEquals(50, property.get());
    }
}
