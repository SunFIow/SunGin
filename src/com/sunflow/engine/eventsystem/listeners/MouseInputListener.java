package com.sunflow.engine.eventsystem.listeners;

import com.sunflow.engine.eventsystem.events.MouseInputEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MousePressedEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MouseReleasedEvent;

public interface MouseInputListener extends EventListener {
	void onMouseInput(MouseInputEvent event);

	void onMousePressed(MousePressedEvent event);

	void onMouseReleased(MouseReleasedEvent event);
}
