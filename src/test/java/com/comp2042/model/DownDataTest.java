package com.comp2042.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DownDataTest {

    @Test
    void testDownDataCreation() {
        ClearRow clearRow = new ClearRow(1, new int[5][5], 50);
        ViewData viewData = new ViewData(new int[2][2], 0, 0, new int[2][2]);

        DownData downData = new DownData(clearRow, viewData);

        assertSame(clearRow, downData.getClearRow());
        assertSame(viewData, downData.getViewData());
    }

    @Test
    void testDownDataWithNullClearRow() {
        ViewData viewData = new ViewData(new int[2][2], 0, 0, new int[2][2]);

        DownData downData = new DownData(null, viewData);

        assertNull(downData.getClearRow());
        assertSame(viewData, downData.getViewData());
    }
}

