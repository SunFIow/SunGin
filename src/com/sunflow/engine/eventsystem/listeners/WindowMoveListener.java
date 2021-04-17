package com.sunflow.engine.eventsystem.listeners;

import com.sunflow.engine.eventsystem.events.WindowMoveEvent;

public interface WindowMoveListener extends SEventListener {
	void onMoved(WindowMoveEvent event);
}
