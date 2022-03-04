package com.sunflow.engine.eventsystem.events;

abstract public class Event implements EventConstants {
	private EventType type;
	private long window;

	public Event(EventType type, long window) { this.type = type; this.window = window; }

	public EventType getType() { return type; }

	public void setType(EventType type) { this.type = type; }

	public long getWindow() { return window; }

	public void setWindow(long window) { this.window = window; }

	abstract public String getName();
}
