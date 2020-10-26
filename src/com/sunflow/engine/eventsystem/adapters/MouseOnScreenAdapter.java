package com.sunflow.engine.eventsystem.adapters;

import com.sunflow.engine.eventsystem.events.MouseOnScreenEvent;
import com.sunflow.engine.eventsystem.events.MouseOnScreenEvent.MouseEnteredEvent;
import com.sunflow.engine.eventsystem.events.MouseOnScreenEvent.MouseExitedEvent;
import com.sunflow.engine.eventsystem.listeners.MouseOnScreenListener;

public class MouseOnScreenAdapter implements MouseOnScreenListener {
	@Override
	public void onMouseOnScreen(MouseOnScreenEvent event) {}

	@Override
	public void onMouseEntered(MouseEnteredEvent event) {}

	@Override
	public void onMouseExited(MouseExitedEvent event) {}
}
