package com.sunflow.engine.eventsystem.events;

public class MouseEvent extends Event {
	private double mouseX, mouseY;
	private double oldX, oldY;

	public MouseEvent(EventType type, long window,
			double mouseX, double mouseY, double oldX, double oldY) {
		super(type, window);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.oldX = oldX;
		this.oldY = oldY;
	}

	public double getMouseX() { return mouseX; }

	public void setMouseX(double mouseX) { this.mouseX = mouseX; }

	public double getMouseY() { return mouseY; }

	public void setMouseY(double mouseY) { this.mouseY = mouseY; }

	public double getOldX() { return oldX; }

	public void setOldX(double oldX) { this.oldX = oldX; }

	public double getOldY() { return oldY; }

	public void setOldY(double oldY) { this.oldY = oldY; }

	public double getDeltaX() { return mouseX - oldX; }

	public double getDeltaY() { return mouseY - oldY; }

	@Override
	public String getName() { return "Mouse"; }
}