package com.sunflow.interfaces;

import com.sunflow.engine.eventsystem.listeners.SEventListener;

public interface GameLoopListener extends SEventListener {
	void preUpdate();

	void postUpdate();
}
