package com.sunflow.engine.eventsystem.listeners;

import com.sunflow.engine.eventsystem.events.ScrollEvent;

public interface ScrollListener extends SEventListener {
	void onScrolled(ScrollEvent event);
}
