package com.sunflow.engine.eventsystem.listeners;

import com.sunflow.engine.eventsystem.events.WindowResizeEvent;

public interface WindowResizeListener extends SEventListener {
	void onResized(WindowResizeEvent event);
}
