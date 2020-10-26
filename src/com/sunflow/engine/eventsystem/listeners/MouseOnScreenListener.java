package com.sunflow.engine.eventsystem.listeners;

import com.sunflow.engine.eventsystem.events.MouseOnScreenEvent;
import com.sunflow.engine.eventsystem.events.MouseOnScreenEvent.MouseEnteredEvent;
import com.sunflow.engine.eventsystem.events.MouseOnScreenEvent.MouseExitedEvent;

public interface MouseOnScreenListener extends EventListener {
	void onMouseOnScreen(MouseOnScreenEvent event);

	void onMouseEntered(MouseEnteredEvent event);

	void onMouseExited(MouseExitedEvent event);
}
