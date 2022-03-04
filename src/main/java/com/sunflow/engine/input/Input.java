package com.sunflow.engine.input;

import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

public class Input implements InputConstants {

	public static long windowID;

	public static void setWindowID(long windowID) { Input.windowID = windowID; }

	public static final int KEY_FIRST = 32;
	public static final int KEY_LAST = GLFW.GLFW_KEY_LAST;

	public static final int BUTTON_FIRST = 0;
	public static final int BUTTON_LAST = GLFW.GLFW_MOUSE_BUTTON_LAST;

	public static final int PRESS = GLFW.GLFW_PRESS;
	public static final int RELEASE = GLFW.GLFW_RELEASE;

	public static final byte GOT_PRESSED = 1;
	public static final byte STILL_PRESSED = 2;
	public static final byte GOT_RELEASED = 3;
	public static final byte STILL_RELEASED = 0;

	public static byte[] keys = new byte[KEY_LAST];
	public static byte[] buttons = new byte[BUTTON_LAST];

	public static void update() {
		for (int i = KEY_FIRST; i < KEY_LAST; i++) {
			int state = keys[i];
			int action = GLFW.glfwGetKey(windowID, i);
			if (action == PRESS) {
				if (state == GOT_RELEASED || state == STILL_RELEASED) keys[i] = GOT_PRESSED;
				else if (state == GOT_PRESSED) keys[i] = STILL_PRESSED;
			} else {
				if (state == GOT_PRESSED || state == STILL_PRESSED) keys[i] = GOT_RELEASED;
				else if (state == GOT_RELEASED) keys[i] = STILL_RELEASED;
			}
		}

		for (int i = BUTTON_FIRST; i < BUTTON_LAST; i++) {
			int state = buttons[i];
			int action = GLFW.glfwGetMouseButton(windowID, i);
			if (action == PRESS) {
				if (state == GOT_RELEASED || state == STILL_RELEASED) buttons[i] = GOT_PRESSED;
				else if (state == GOT_PRESSED) buttons[i] = STILL_PRESSED;
			} else {
				if (state == GOT_PRESSED || state == STILL_PRESSED) buttons[i] = GOT_RELEASED;
				else if (buttons[i] == GOT_RELEASED) buttons[i] = STILL_RELEASED;
			}
		}
	}

	public static boolean isKeyPressed(int key) {
		return keys[key] == GOT_PRESSED || keys[key] == STILL_PRESSED;
	}

	public static boolean isKeyRepeated(int key) {
		return keys[key] == STILL_PRESSED;
	}

	public static boolean isKeyReleased(int key) {
		return keys[key] == GOT_RELEASED || keys[key] == STILL_RELEASED;
	}

	public static boolean onKeyPressed(int key) {
		return keys[key] == GOT_PRESSED;
	}

	public static boolean onKeyReleased(int key) {
		return keys[key] == GOT_RELEASED;
	}

	public static boolean isMousePressed(int button) {
		return buttons[button] == GOT_PRESSED || buttons[button] == STILL_PRESSED;
	}

	public static boolean isMouseRepeated(int button) {
		return buttons[button] == STILL_PRESSED;
	}

	public static boolean isMouseReleased(int button) {
		return buttons[button] == GOT_RELEASED || buttons[button] == STILL_RELEASED;
	}

	public static boolean onMousePressed(int button) {
		return buttons[button] == GOT_PRESSED;
	}

	public static boolean onMouseReleased(int button) {
		return buttons[button] == GOT_RELEASED;
	}

	public static Vector2d getMousePosition() {
		double[] xpos = new double[1], ypos = new double[1];
		GLFW.glfwGetCursorPos(windowID, xpos, ypos);
		return new Vector2d(xpos[0], ypos[0]);
	}

	public static double getMouseX() {
		double[] xpos = new double[1];
		GLFW.glfwGetCursorPos(windowID, xpos, null);
		return xpos[0];
	}

	public static double getMouseY() {
		double[] ypos = new double[1];
		GLFW.glfwGetCursorPos(windowID, null, ypos);
		return ypos[0];
	}
}
