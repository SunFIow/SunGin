package com.sunflow.engine.eventsystem.events;

public class MouseMotionEvent extends Event {
	private double mouseX, mouseY;
	private double oldX, oldY;

	public MouseMotionEvent(EventType type, long window, double mouseX, double mouseY, double oldX, double oldY) {
		super(type, window);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.oldX = oldX;
		this.oldY = oldY;
	}

	public double getMouseX() { return mouseX; }

	public void setMouseX(double mouseX) { this.mouseX = mouseX; }

	public double getMouseY() { return mouseY; }

	public void setMouseY(double mouseY) { this.mouseY = mouseY; }

	public double getOldX() { return oldX; }

	public void setOldX(double oldX) { this.oldX = oldX; }

	public double getOldY() { return oldY; }

	public void setOldY(double oldY) { this.oldY = oldY; }

	public double getDeltaX() { return mouseX - oldX; }

	public double getDeltaY() { return mouseY - oldY; }

	@Override
	public String getName() { return "Mouse Motion"; }

	static public class MouseMovedEvent extends MouseMotionEvent {
		public MouseMovedEvent(long window, double mouseX, double mouseY, double oldX, double oldY) {
			super(EventType.MOUSE_MOVE, window, mouseX, mouseY, oldX, oldY);
		}

		@Override
		public String getName() { return "Mouse Moved"; }
	}

	static public class MouseDraggedEvent extends MouseMotionEvent {

		private int button;
		private int mods;

		public MouseDraggedEvent(long window, double mouseX, double mouseY, double oldX, double oldY, int button, int mods) {
			super(EventType.MOUSE_DRAG, window, mouseX, mouseY, oldX, oldY);
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