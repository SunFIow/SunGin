package com.sunflow.engine.eventsystem.events;

public class MouseOnScreenEvent extends Event {
	private boolean onScreen;

	public MouseOnScreenEvent(EventType type, long window, boolean onScreen) {
		super(type, window);
		this.onScreen = onScreen;
	}

	public boolean getOnScreen() { return onScreen; }

	public boolean getEntered() { return getOnScreen(); }

	public boolean getExited() { return !getOnScreen(); }

	public void setOnScreen(boolean onScreen) { this.onScreen = onScreen; }

	@Override
	public String getName() { return "Mouse On Screen"; }

	static public class MouseEnteredEvent extends MouseOnScreenEvent {
		public MouseEnteredEvent(long window) { super(EventType.MOUSE_ENTER, window, true); }

		@Override
		public String getName() { return "Mouse Entered"; }
	}

	static public class MouseExitedEvent extends MouseOnScreenEvent {
		public MouseExitedEvent(long window) { super(EventType.MOUSE_EXIT, window, false); }

		@Override
		public String getName() { return "Mouse Exited"; }
	}
}
