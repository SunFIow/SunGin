package com.sunflow.interfaces;

import com.sunflow.engine.eventsystem.listeners.SEventListener;

public interface FrameLoopListener extends SEventListener {
	void preDraw();

	void postDraw();
}
