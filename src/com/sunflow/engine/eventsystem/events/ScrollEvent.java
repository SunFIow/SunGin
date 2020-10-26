package com.sunflow.engine.eventsystem.events;

public class ScrollEvent extends Event {
	private double offsetX, offsetY;
	private double lastOffsetX, lastOffsetY;

	public ScrollEvent(long window, double offsetX, double offsetY, double lastOffsetX, double lastOffsetY) {
		super(EventType.SCROLL, window);
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.lastOffsetX = lastOffsetX;
		this.lastOffsetY = lastOffsetY;
	}

	public double getAmountX() {
		return offsetX - lastOffsetX;
	}

	public double getAmountY() {
		return offsetY - lastOffsetY;
	}

	public double getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(double offsetX) {
		this.offsetX = offsetX;
	}

	public double getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(double offsetY) {
		this.offsetY = offsetY;
	}

	public double getLastOffsetX() {
		return lastOffsetX;
	}

	public void setLastOffsetX(double lastOffsetX) {
		this.lastOffsetX = lastOffsetX;
	}

	public double getLastOffsetY() {
		return lastOffsetY;
	}

	public void setLastOffsetY(double lastOffsetY) {
		this.lastOffsetY = lastOffsetY;
	}

	@Override
	public String getName() { return "Scroll Event"; }

}
