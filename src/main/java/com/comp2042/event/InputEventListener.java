package com.comp2042.event;

import com.comp2042.model.DownData;
import com.comp2042.model.ViewData;

public interface InputEventListener {

    DownData onDownEvent(MoveEvent event);

    ViewData onLeftEvent(MoveEvent event);

    ViewData onRightEvent(MoveEvent event);

    ViewData onRotateEvent(MoveEvent event);

    DownData onHardDropEvent(MoveEvent event);

    ViewData onHoldEvent(MoveEvent event);

    void createNewGame();
}

