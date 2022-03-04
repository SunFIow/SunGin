package com.sunflow.interfaces;

import com.sunflow.engine.eventsystem.listeners.SEventListener;

public interface FrameLoopListener extends SEventListener {
	default void preDraw() {}

	default void postDraw() {}
}
