package com.sunflow.engine.eventsystem.listeners;

import com.sunflow.engine.eventsystem.events.KeyInputEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyPressedEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyReleasedEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyRepeatedEvent;

public interface KeyInputListener extends SEventListener {
	static int id = 1;

	void onKeyInput(KeyInputEvent event);

	void onKeyPressed(KeyPressedEvent event);

	void onKeyReleased(KeyReleasedEvent event);

	void onKeyRepeated(KeyRepeatedEvent event);
}
