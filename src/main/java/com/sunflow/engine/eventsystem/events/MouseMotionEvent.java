package com.sunflow.engine.eventsystem.events;

public class MouseMotionEvent extends MouseEvent {

	public MouseMotionEvent(EventType type, long window,
			double mouseX, double mouseY, double oldX, double oldY) {
		super(type, window, mouseX, mouseY, oldX, oldY);
	}

	@Override
	public String getName() { return "Mouse Motion"; }

	static public class MouseMovedEvent extends MouseMotionEvent {
		public MouseMovedEvent(long window,
				double mouseX, double mouseY, double oldX, double oldY) {
			super(EventType.MOUSE_MOVE,
					window, mouseX, mouseY, oldX, oldY);
		}

		@Override
		public String getName() { return "Mouse Moved"; }
	}

	static public class MouseDraggedEvent extends MouseMotionEvent {
		private int button;
		private int mods;

		public MouseDraggedEvent(long window,
				double mouseX, double mouseY, double oldX, double oldY,
				int button, int mods) {
			super(EventType.MOUSE_DRAG,
					window, mouseX, mouseY, oldX, oldY);
			this.button = button;
			this.mods = mods;
		}

		public int getButton() { return button; }

		public void setButton(int button) { this.button = button; }

		public int getMods() { return mods; }

		public void setMods(int mods) { this.mods = mods; }

		@Override
		public String getName() { return "Mouse Dragged"; }
	}
}