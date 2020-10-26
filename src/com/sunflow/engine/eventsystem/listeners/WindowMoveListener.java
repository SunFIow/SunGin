package com.sunflow.engine.eventsystem.listeners;

import com.sunflow.engine.eventsystem.events.WindowMoveEvent;

public interface WindowMoveListener extends EventListener {
	void onMoved(WindowMoveEvent event);
}
