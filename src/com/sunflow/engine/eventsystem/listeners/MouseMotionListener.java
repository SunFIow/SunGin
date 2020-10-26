package com.sunflow.engine.eventsystem.listeners;

import com.sunflow.engine.eventsystem.events.MouseMotionEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseDraggedEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseMovedEvent;

public interface MouseMotionListener extends EventListener {
	void onMouseMotion(MouseMotionEvent event);

	void onMouseMoved(MouseMovedEvent event);

	void onMouseDragged(MouseDraggedEvent event);
}
