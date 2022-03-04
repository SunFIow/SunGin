package com.sunflow.engine.eventsystem.events;

public class WindowMoveEvent extends Event {
	private int xpos, ypos;

	public WindowMoveEvent(EventType type, long window, int xpos, int ypos) {
		super(type, window);
		this.xpos = xpos;
		this.ypos = ypos;
	}

	public int getX() { return xpos; }

	public void setX(int width) { this.xpos = width; }

	public int getY() { return ypos; }

	public void setY(int height) { this.ypos = height; }

	@Override
	public String getName() { return "Window Move"; }
}
