package com.sunflow.engine;

import java.awt.MouseInfo;

public class Mouse {
	public float x, y;
	public float screenX, screenY;
	public float wheel;

	public float lastX, lastY;
	public float lastScreenX, lastScreenY;
	public float lastWheel;

	protected boolean[] buttons = new boolean[MouseInfo.getNumberOfButtons()];
	protected boolean[] buttonsNew = new boolean[buttons.length];
	protected boolean[] buttonsOld = new boolean[buttons.length];

	public void updatePosition(float x, float y) {
		this.lastX = this.x;
		this.lastY = this.y;
		this.x = x;
		this.y = y;
	}

	public void updateScreenPosition(float screenX, float screenY) {
		this.lastScreenX = this.screenX;
		this.lastScreenY = this.screenY;
		this.screenX = screenX;
		this.screenY = screenY;
	}

	public void updateWheel(float wheel) {
		this.lastWheel = this.wheel;
		this.wheel = wheel;
	}
}
