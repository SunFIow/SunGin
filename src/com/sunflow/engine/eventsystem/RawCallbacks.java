package com.sunflow.engine.eventsystem;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import com.sunflow.engine.eventsystem.events.EventConstants;
import com.sunflow.engine.eventsystem.events.EventType;
import com.sunflow.engine.eventsystem.events.KeyInputEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyPressedEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyReleasedEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyRepeatedEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MousePressedEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MouseReleasedEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseDraggedEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseMovedEvent;
import com.sunflow.engine.eventsystem.events.MouseOnScreenEvent;
import com.sunflow.engine.eventsystem.events.MouseOnScreenEvent.MouseEnteredEvent;
import com.sunflow.engine.eventsystem.events.MouseOnScreenEvent.MouseExitedEvent;
import com.sunflow.engine.eventsystem.events.ScrollEvent;
import com.sunflow.engine.eventsystem.events.WindowMoveEvent;
import com.sunflow.engine.eventsystem.events.WindowResizeEvent;

public class RawCallbacks implements EventConstants {

	private GLFWKeyCallback keyInput;
	private GLFWMouseButtonCallback mouseInput;
	private GLFWCursorPosCallback mouseMotion;
	private GLFWScrollCallback scroll;
	private GLFWCursorEnterCallback mouseOnScreen;
	private GLFWWindowSizeCallback windowSize;
	private GLFWWindowPosCallback windowPosition;

	protected RawCallbacks(long windowID) {
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		GLFW.glfwSetKeyCallback(windowID, keyInput = new KeyInputCallback());
		// Setup a mouse callback. It will be called every time a mouse button is pressed, repeated or released.
		GLFW.glfwSetMouseButtonCallback(windowID, mouseInput = new MouseInputCallback());
		// Setup a cursor position callback. It will be called every time the mouse is moved.
		GLFW.glfwSetCursorPosCallback(windowID, mouseMotion = new MouseMotionCallback());
		// Setup a scroll callback. It will be called every time a scrolling device is used, such as a mouse wheel or scrolling area of a touchpad.
		GLFW.glfwSetScrollCallback(windowID, scroll = new ScrollCallback());
		// Setup a cursor enter callback. It will be called every time the cursor enters or leaves the client area of the window.
		GLFW.glfwSetCursorEnterCallback(windowID, mouseOnScreen = new MouseOnScreenCallback());

		GLFW.glfwSetWindowSizeCallback(windowID, windowSize = new WindowResizeCallback());
		GLFW.glfwSetWindowPosCallback(windowID, windowPosition = new WindowMoveCallback());
	}

	public void destroy() {
		keyInput.free();
		mouseInput.free();
		mouseMotion.free();
		scroll.free();
		mouseOnScreen.free();
	}

	public class KeyInputCallback extends GLFWKeyCallback {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			KeyInputEvent event = null;
			if (action == PRESS) event = new KeyPressedEvent(window, key, scancode, mods);
			else if (action == RELEASE) event = new KeyReleasedEvent(window, key, scancode, mods);
			else if (action == REPEAT) event = new KeyRepeatedEvent(window, key, scancode, mods);
			else event = new KeyInputEvent(EventType.KEY_INPUT, window, key, scancode, action, mods);
			EventManager.addEvent(event);
		}
	}

	public class MouseInputCallback extends GLFWMouseButtonCallback {
		@Override
		public void invoke(long window, int button, int action, int mods) {
			MouseInputEvent event = null;
			if (action == PRESS) event = new MousePressedEvent(window, button, mods);
			else if (action == RELEASE) event = new MouseReleasedEvent(window, button, mods);
			else event = new MouseInputEvent(EventType.MOUSE_INPUT, window, button, action, mods);
			EventManager.addEvent(event);

			if (action == PRESS && currentButton == -1) {
				currentButton = button;
				currentMods = mods;
			} else if (action == RELEASE && currentButton == button) {
				currentButton = -1;
				currentMods = -1;
			}
		}
	}

	private int currentButton, currentMods;

	public class MouseMotionCallback extends GLFWCursorPosCallback {
		private double lastX, lastY;

		@Override
		public void invoke(long window, double mouseX, double mouseY) {
			MouseMotionEvent event = null;
			if (currentButton < 0) event = new MouseMovedEvent(window, mouseX, mouseY, lastX, lastY);
			else if (currentButton <= MOUSE_BUTTON_LAST) event = new MouseDraggedEvent(window, mouseX, mouseY, lastX, lastY, currentButton, currentMods);
			else event = new MouseMotionEvent(EventType.MOUSE_MOTION, window, mouseX, mouseY, lastX, lastY);
			lastX = mouseX;
			lastY = mouseY;
			EventManager.addEvent(event);
		}
	}

	public class ScrollCallback extends GLFWScrollCallback {
		private double lastOffsetX, lastOffsetY;

		@Override
		public void invoke(long window, double offsetX, double offsetY) {
			ScrollEvent event = new ScrollEvent(window, offsetX, offsetY, lastOffsetX, lastOffsetY);
			EventManager.addEvent(event);
			lastOffsetX = offsetX;
			lastOffsetY = offsetY;
		}
	}

	public class MouseOnScreenCallback extends GLFWCursorEnterCallback {
		@Override
		public void invoke(long window, boolean onScreen) {
			MouseOnScreenEvent event = null;
			if (onScreen) event = new MouseEnteredEvent(window);
			else event = new MouseExitedEvent(window);
			EventManager.addEvent(event);
		}
	}

	public class WindowResizeCallback extends GLFWWindowSizeCallback {
		@Override
		public void invoke(long window, int width, int height) {
			WindowResizeEvent event = null;
			event = new WindowResizeEvent(EventType.WINDOW_RESIZE, window, width, height);
			EventManager.addEvent(event);
		}
	}

	public class WindowMoveCallback extends GLFWWindowPosCallback {

		@Override
		public void invoke(long window, int xpos, int ypos) {
			WindowMoveEvent event = null;
			event = new WindowMoveEvent(EventType.WINDOW_MOVE, window, xpos, ypos);
			EventManager.addEvent(event);
		}

	}

}