package com.sunflow.engine.eventsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.sunflow.engine.eventsystem.events.Event;
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
import com.sunflow.engine.eventsystem.listeners.KeyInputListener;
import com.sunflow.engine.eventsystem.listeners.MouseInputListener;
import com.sunflow.engine.eventsystem.listeners.MouseMotionListener;
import com.sunflow.engine.eventsystem.listeners.MouseOnScreenListener;
import com.sunflow.engine.eventsystem.listeners.SEventListener;
import com.sunflow.engine.eventsystem.listeners.ScrollListener;
import com.sunflow.engine.eventsystem.listeners.WindowMoveListener;
import com.sunflow.engine.eventsystem.listeners.WindowResizeListener;

public class EventManager {
	static private Map<Long, RawCallbacks> mRawCallbacks = new HashMap<>();

	static private List<Event> events = new LinkedList<>();

	static private List<KeyInputListener> keyInputListeners = new ArrayList<>();
	static private List<MouseInputListener> mouseInputListeners = new ArrayList<>();
	static private List<MouseMotionListener> mouseMotionListeners = new ArrayList<>();
	static private List<MouseOnScreenListener> mouseOnScreenListeners = new ArrayList<>();
	static private List<ScrollListener> scrollListeners = new ArrayList<>();
	static private List<WindowResizeListener> windowResizeListeners = new ArrayList<>();
	static private List<WindowMoveListener> windowMoveListeners = new ArrayList<>();

	static public void setupRawCallbacks(long windowID) {
		RawCallbacks rawCallbacks = new RawCallbacks(windowID);
		mRawCallbacks.put(windowID, rawCallbacks);
	}

	static public void destroyRawCallbacks(long windowID) {
		RawCallbacks rawCallbacks = mRawCallbacks.remove(windowID);
		if (rawCallbacks != null) rawCallbacks.destroy();
	}

	static public void pollEvents() {
		events.clear();
		GLFW.glfwPollEvents();
	}

	static public void addEvent(Event event) { if (event != null) events.add(event); }

	static public List<Event> getAllEvents() { return events; }

	static public boolean addEventListener(SEventListener listener) {
		boolean added = false;
		if (listener instanceof KeyInputListener) {
			keyInputListeners.add((KeyInputListener) listener);
			added = true;
		}
		if (listener instanceof MouseInputListener) {
			mouseInputListeners.add((MouseInputListener) listener);
			added = true;
		}
		if (listener instanceof MouseMotionListener) {
			mouseMotionListeners.add((MouseMotionListener) listener);
			added = true;
		}
		if (listener instanceof MouseOnScreenListener) {
			mouseOnScreenListeners.add((MouseOnScreenListener) listener);
			added = true;
		}
		if (listener instanceof ScrollListener) {
			scrollListeners.add((ScrollListener) listener);
			added = true;
		}
		if (listener instanceof WindowResizeListener) {
			windowResizeListeners.add((WindowResizeListener) listener);
			added = true;
		}
		if (listener instanceof WindowMoveListener) {
			windowMoveListeners.add((WindowMoveListener) listener);
			added = true;
		}
		return added;
	}

	static public void addKeyInputListener(KeyInputListener listener) { keyInputListeners.add(listener); }

	static public void addMouseInputListener(MouseInputListener listener) { mouseInputListeners.add(listener); }

	static public void addMouseMotionListener(MouseMotionListener listener) { mouseMotionListeners.add(listener); }

	static public void addMouseOnScreenListener(MouseOnScreenListener listener) { mouseOnScreenListeners.add(listener); }

	static public void addScrollListener(ScrollListener listener) { scrollListeners.add(listener); }

	static public void addWindowResizeListener(WindowResizeListener listener) { windowResizeListeners.add(listener); }

	static public void addWindowMoveListener(WindowMoveListener listener) { windowMoveListeners.add(listener); }

	static public boolean removeEventListener(SEventListener listener) {
		boolean removed = false;
		if (listener instanceof KeyInputListener) {
			keyInputListeners.remove(listener);
			removed = true;
		}
		if (listener instanceof MouseInputListener) {
			mouseInputListeners.remove(listener);
			removed = true;
		}
		if (listener instanceof MouseMotionListener) {
			mouseMotionListeners.remove(listener);
			removed = true;
		}
		if (listener instanceof MouseOnScreenListener) {
			mouseOnScreenListeners.remove(listener);
			removed = true;
		}
		if (listener instanceof ScrollListener) {
			scrollListeners.remove(listener);
			removed = true;
		}
		if (listener instanceof WindowResizeListener) {
			windowResizeListeners.remove(listener);
			removed = true;
		}
		if (listener instanceof WindowMoveListener) {
			windowMoveListeners.remove(listener);
			removed = true;
		}
		return removed;
	}

	static public void removeKeyInputListener(KeyInputListener listener) { keyInputListeners.remove(listener); }

	static public void removeMouseInputListener(MouseInputListener listener) { mouseInputListeners.remove(listener); }

	static public void removeMouseMotionListener(MouseMotionListener listener) { mouseMotionListeners.remove(listener); }

	static public void removeMouseOnScreenListener(MouseOnScreenListener listener) { mouseOnScreenListeners.remove(listener); }

	static public void removeScrollListener(ScrollListener listener) { scrollListeners.remove(listener); }

	static public void removeWindowResizeListener(WindowResizeListener listener) { windowResizeListeners.remove(listener); }

	static public void removeWindowMoveListener(WindowMoveListener listener) { windowMoveListeners.remove(listener); }

	static public void processMessages(double delta) {
		List<Event> events = EventManager.getAllEvents();

		for (Event event : events) switch (event.getType()) {
			case KEY_INPUT:
				keyInputListeners.forEach(l -> l.onKeyInput((KeyInputEvent) event));
				break;
			case KEY_PRESS:
				keyInputListeners.forEach(l -> l.onKeyInput((KeyInputEvent) event));
				keyInputListeners.forEach(l -> l.onKeyPressed((KeyPressedEvent) event));
				break;
			case KEY_RELEASE:
				keyInputListeners.forEach(l -> l.onKeyInput((KeyInputEvent) event));
				keyInputListeners.forEach(l -> l.onKeyReleased((KeyReleasedEvent) event));
				break;
			case KEY_REPEAT:
				keyInputListeners.forEach(l -> l.onKeyInput((KeyInputEvent) event));
				keyInputListeners.forEach(l -> l.onKeyRepeated((KeyRepeatedEvent) event));
				break;

			case MOUSE_INPUT:
				mouseInputListeners.forEach(l -> l.onMouseInput((MouseInputEvent) event));
				break;
			case MOUSE_PRESS:
				mouseInputListeners.forEach(l -> l.onMouseInput((MouseInputEvent) event));
				mouseInputListeners.forEach(l -> l.onMousePressed((MousePressedEvent) event));
				break;
			case MOUSE_RELEASE:
				mouseInputListeners.forEach(l -> l.onMouseInput((MouseInputEvent) event));
				mouseInputListeners.forEach(l -> l.onMouseReleased((MouseReleasedEvent) event));
				break;

			case MOUSE_MOTION:
				mouseMotionListeners.forEach(l -> l.onMouseMotion((MouseMotionEvent) event));
				break;
			case MOUSE_MOVE:
				mouseMotionListeners.forEach(l -> l.onMouseMotion((MouseMotionEvent) event));
				mouseMotionListeners.forEach(l -> l.onMouseMoved((MouseMovedEvent) event));
				break;
			case MOUSE_DRAG:
				mouseMotionListeners.forEach(l -> l.onMouseMotion((MouseMotionEvent) event));
				mouseMotionListeners.forEach(l -> l.onMouseDragged((MouseDraggedEvent) event));
				break;

			case MOUSE_ONSCREEN:
				mouseOnScreenListeners.forEach(l -> l.onMouseOnScreen((MouseOnScreenEvent) event));
				break;
			case MOUSE_ENTER:
				mouseOnScreenListeners.forEach(l -> l.onMouseOnScreen((MouseOnScreenEvent) event));
				mouseOnScreenListeners.forEach(l -> l.onMouseEntered((MouseEnteredEvent) event));
				break;
			case MOUSE_EXIT:
				mouseOnScreenListeners.forEach(l -> l.onMouseOnScreen((MouseOnScreenEvent) event));
				mouseOnScreenListeners.forEach(l -> l.onMouseExited((MouseExitedEvent) event));
				break;

			case SCROLL:
				scrollListeners.forEach(l -> l.onScrolled((ScrollEvent) event));
				break;

			case WINDOW_RESIZE:
				windowResizeListeners.forEach(l -> l.onResized((WindowResizeEvent) event));
				break;
			case WINDOW_MOVE:
				windowMoveListeners.forEach(l -> l.onMoved((WindowMoveEvent) event));
				break;

			default:
				System.err.println(event.getName() + " not supported !");
		}
	}
}
