package com.sunflow.engine;

import java.awt.MouseInfo;

public class Mouse {
	protected float newX, newY;
	protected float newScreenX, newScreenY;
	protected float newWheelX, newWheelY;

	protected float x, y;
	protected float screenX, screenY;
	protected float wheelX, wheelY;

	protected float lastX, lastY;
	protected float lastScreenX, lastScreenY;
	protected float lastWheelX, lastWheelY;

	protected int button;
	protected boolean mousePressed;
	protected boolean[] newButtons = new boolean[MouseInfo.getNumberOfButtons()];
	protected boolean[] buttons = new boolean[newButtons.length];
	protected boolean[] lastButtons = new boolean[newButtons.length];

	public void update() {
		lastX = x;
		lastY = y;
		x = newX;
		y = newY;

		lastScreenX = screenX;
		lastScreenY = screenY;
		screenX = newScreenX;
		screenY = newScreenY;

		lastWheelX = wheelX;
		lastWheelY = wheelY;
		wheelX = newWheelX;
		wheelY = newWheelY;
		this.newWheelX = 0;
		this.newWheelY = 0;

		// for (int i = 0; i < buttonsNew.length; i++) buttonsOld[i] = buttons[i];
		// for (int i = 0; i < buttonsNew.length; i++) buttons[i] = buttonsNew[i];
		System.arraycopy(buttons, 0, lastButtons, 0, newButtons.length);
		System.arraycopy(newButtons, 0, buttons, 0, newButtons.length);
	}

	public void updatePosition(float x, float y) {
		this.newX = x;
		this.newY = y;
	}

	public void updateScreenPosition(float screenX, float screenY) {
		this.newScreenX = screenX;
		this.newScreenY = screenY;
	}

	public void updateWheel(float wheelX, float wheelY) {
		this.newWheelX = wheelX;
		this.newWheelY = wheelY;
	}

	public void updateButton(int button, boolean newState, int buttonLeft) {
		this.button = button;
		this.newButtons[button] = newState;
		if (button == buttonLeft) mousePressed = newState;
	}

	public float x() { return x; }

	public float screenX() { return screenX; }

	public float screenY() { return screenY; }

	public float y() { return y; }

	public float lastX() { return lastX; }

	public float lastY() { return lastY; }

	public int button() { return button; }

	public boolean pressed() { return mousePressed; }

	// public int buttons() { return buttons; }

	public boolean isButtonDown(int button) {
		if (button < 0 || button > newButtons.length) return false;
		return buttons[button];
	}

	public boolean isButtonPressed(int button) {
		if (button < 0 || button > newButtons.length) return false;
		return !lastButtons[button] && buttons[button];
	}

	public boolean isButtonHeld(int button) {
		if (button < 0 || button > newButtons.length) return false;
		return lastButtons[button] && buttons[button];
	}

	public boolean isButtonReleased(int button) {
		if (button < 0 || button > newButtons.length) return false;
		return lastButtons[button] && !buttons[button];
	}

	public float mouseWheel() { return mouseWheelX(); }

	public float mouseWheelX() { return wheelX; }

	public float mouseWheelY() { return wheelY; }

}
