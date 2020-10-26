package com.sunflow.engine.eventsystem.adapters;

import com.sunflow.engine.eventsystem.events.MouseMotionEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseDraggedEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseMovedEvent;
import com.sunflow.engine.eventsystem.listeners.MouseMotionListener;

public class MouseMotionAdapter implements MouseMotionListener {
	@Override
	public void onMouseMotion(MouseMotionEvent event) {}

	@Override
	public void onMouseMoved(MouseMovedEvent event) {}

	@Override
	public void onMouseDragged(MouseDraggedEvent event) {}
}
