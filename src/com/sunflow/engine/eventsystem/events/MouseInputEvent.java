package com.sunflow.engine.eventsystem.events;

public class MouseInputEvent extends Event {

	private int button;
	private int action;
	private int mods;

	public MouseInputEvent(EventType type, long window, int button, int action, int mods) {
		super(type, window);
	}

	public int getButton() { return button; }

	public void setButton(int button) { this.button = button; }

	public int getAction() { return action; }

	public void setAction(int action) { this.action = action; }

	public int getMods() { return mods; }

	public void setMods(int mods) { this.mods = mods; }

	@Override
	public String getName() { return "Mouse Input"; }

	static public class MousePressedEvent extends MouseInputEvent {
		public MousePressedEvent(long window, int button, int mods) {
			super(EventType.MOUSE_PRESS, window, button, PRESS, mods);
		}

		@Override
		public String getName() { return "Mouse Pressed"; }
	}

	static public class MouseReleasedEvent extends MouseInputEvent {
		public MouseReleasedEvent(long window, int button, int mods) {
			super(EventType.MOUSE_RELEASE, window, button, RELEASE, mods);
		}

		@Override
		public String getName() { return "Mouse Released"; }
	}
}
