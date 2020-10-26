package com.sunflow.engine.eventsystem.adapters;

import com.sunflow.engine.eventsystem.events.KeyInputEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyPressedEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyReleasedEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyRepeatedEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MousePressedEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MouseReleasedEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseDraggedEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseMovedEvent;
import com.sunflow.engine.eventsystem.events.MouseOnScreenEvent;
import com.sunflow.engine.eventsystem.events.MouseOnScreenEvent.MouseEnteredEvent;
import com.sunflow.engine.eventsystem.events.MouseOnScreenEvent.MouseExitedEvent;
import com.sunflow.engine.eventsystem.events.ScrollEvent;
import com.sunflow.engine.eventsystem.listeners.GameEventListener;

public class GameEventAdapter implements GameEventListener {

	@Override
	public void onKeyInput(KeyInputEvent event) {}

	@Override
	public void onKeyPressed(KeyPressedEvent event) {}

	@Override
	public void onKeyReleased(KeyReleasedEvent event) {}

	@Override
	public void onKeyRepeated(KeyRepeatedEvent event) {}

	@Override
	public void onMouseInput(MouseInputEvent event) {}

	@Override
	public void onMousePressed(MousePressedEvent event) {}

	@Override
	public void onMouseReleased(MouseReleasedEvent event) {}

	@Override
	public void onMouseMotion(MouseMotionEvent event) {}

	@Override
	public void onMouseMoved(MouseMovedEvent event) {}

	@Override
	public void onMouseDragged(MouseDraggedEvent event) {}

	@Override
	public void onMouseOnScreen(MouseOnScreenEvent event) {}

	@Override
	public void onMouseEntered(MouseEnteredEvent event) {}

	@Override
	public void onMouseExited(MouseExitedEvent event) {}

	@Override
	public void onScrolled(ScrollEvent event) {}

}
