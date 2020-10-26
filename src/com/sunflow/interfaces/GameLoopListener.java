package com.sunflow.interfaces;

import com.sunflow.engine.eventsystem.listeners.EventListener;

public interface GameLoopListener extends EventListener {
	void update();
}
