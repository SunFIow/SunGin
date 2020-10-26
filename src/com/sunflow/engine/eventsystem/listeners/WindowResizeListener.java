package com.sunflow.engine.eventsystem.listeners;

import com.sunflow.engine.eventsystem.events.WindowResizeEvent;

public interface WindowResizeListener extends EventListener {
	void onResized(WindowResizeEvent event);
}
