package com.sunflow.interfaces;

import com.sunflow.engine.eventsystem.listeners.EventListener;

public interface FrameLoopListener extends EventListener {
	void update();
}
