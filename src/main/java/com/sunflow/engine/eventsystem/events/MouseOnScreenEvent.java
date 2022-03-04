package com.sunflow.engine.eventsystem.events;

public class MouseOnScreenEvent extends MouseEvent {
	private boolean onScreen;

	public MouseOnScreenEvent(EventType type, long window, boolean onScreen,
			double mouseX, double mouseY, double oldX, double oldY) {
		super(type, window,
				mouseX, mouseY, oldX, oldY);
		this.onScreen = onScreen;
	}

	public boolean getOnScreen() { return onScreen; }

	public boolean getEntered() { return getOnScreen(); }

	public boolean getExited() { return !getOnScreen(); }

	public void setOnScreen(boolean onScreen) { this.onScreen = onScreen; }

	@Override
	public String getName() { return "Mouse On Screen"; }

	static public class MouseEnteredEvent extends MouseOnScreenEvent {
		public MouseEnteredEvent(long window, double mouseX, double mouseY, double oldX, double oldY) {
			super(EventType.MOUSE_ENTER, window, true,
					mouseX, mouseY, oldX, oldY);
		}

		@Override
		public String getName() { return "Mouse Entered"; }
	}

	static public class MouseExitedEvent extends MouseOnScreenEvent {
		public MouseExitedEvent(long window, double mouseX, double mouseY, double oldX, double oldY) {
			super(EventType.MOUSE_EXIT, window, false,
					mouseX, mouseY, oldX, oldY);
		}

		@Override
		public String getName() { return "Mouse Exited"; }
	}
}
