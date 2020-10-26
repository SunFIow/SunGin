package com.sunflow.engine.eventsystem.adapters;

import com.sunflow.engine.eventsystem.events.MouseInputEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MousePressedEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MouseReleasedEvent;
import com.sunflow.engine.eventsystem.listeners.MouseInputListener;

public class MouseInputAdapter implements MouseInputListener {
	@Override
	public void onMouseInput(MouseInputEvent event) {}

	@Override
	public void onMousePressed(MousePressedEvent event) {}

	@Override
	public void onMouseReleased(MouseReleasedEvent event) {}
}
