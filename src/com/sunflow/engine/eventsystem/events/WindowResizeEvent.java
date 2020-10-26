package com.sunflow.engine.eventsystem.events;

public class WindowResizeEvent extends Event {
	private int width, height;

	public WindowResizeEvent(EventType type, long window, int width, int height) {
		super(type, window);
		this.width = width;
		this.height = height;
	}

	public int getWidth() { return width; }

	public void setWidth(int width) { this.width = width; }

	public int getHeight() { return height; }

	public void setHeight(int height) { this.height = height; }

	@Override
	public String getName() { return "Window Resize"; }
}
