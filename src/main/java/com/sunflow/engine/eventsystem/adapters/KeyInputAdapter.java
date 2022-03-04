package com.sunflow.engine.eventsystem.adapters;

import com.sunflow.engine.eventsystem.events.KeyInputEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyPressedEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyReleasedEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyRepeatedEvent;
import com.sunflow.engine.eventsystem.listeners.KeyInputListener;

public class KeyInputAdapter implements KeyInputListener {
	@Override
	public void onKeyInput(KeyInputEvent event) {}

	@Override
	public void onKeyPressed(KeyPressedEvent event) {}

	@Override
	public void onKeyReleased(KeyReleasedEvent event) {}

	@Override
	public void onKeyRepeated(KeyRepeatedEvent event) {}
}
