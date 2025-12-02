package com.comp2042.model;

/**
 * Immutable data class containing information returned from a down move event.
 * Combines information about row clearing (if any) with updated view data
 * for rendering the current game state.
 */
public final class DownData {
    private final ClearRow clearRow;
    private final ViewData viewData;

    /**
     * Creates a new DownData instance with the specified clear row and view data.
     * 
     * @param clearRow Information about cleared rows (can be null if no rows were cleared)
     * @param viewData Updated view data for rendering the current game state
     */
    public DownData(ClearRow clearRow, ViewData viewData) {
        this.clearRow = clearRow;
        this.viewData = viewData;
    }

    /**
     * Gets the clear row information.
     * 
     * @return ClearRow containing information about cleared rows, or null if no rows were cleared
     */
    public ClearRow getClearRow() {
        return clearRow;
    }

    /**
     * Gets the updated view data.
     * 
     * @return ViewData containing the current brick position and state
     */
    public ViewData getViewData() {
        return viewData;
    }
}

