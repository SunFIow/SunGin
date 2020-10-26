package com.sunflow.engine.eventsystem.events;

public class KeyInputEvent extends Event {

	private int keycode;
	private int scancode;
	private int action;
	private int mods;

	public KeyInputEvent(EventType type, long window, int key, int scancode, int action, int mods) {
		super(type, window);
		this.keycode = key;
		this.scancode = scancode;
		this.action = action;
	}

	public char getKeyChar() { return (char) keycode; }

	public int getKeyCode() { return keycode; }

	public void setKeyCode(int keycode) { this.keycode = keycode; }

	public int getScanCode() { return scancode; }

	public void setScanCode(int scancode) { this.scancode = scancode; }

	public int getAction() { return action; }

	public void setAction(int action) { this.action = action; }

	public int getMods() { return mods; }

	public void setMods(int mods) { this.mods = mods; }

	@Override
	public String getName() { return "Key Input"; }

	static public class KeyPressedEvent extends KeyInputEvent {
		public KeyPressedEvent(long window, int key, int scancode, int mods) {
			super(EventType.KEY_PRESS, window, key, PRESS, scancode, mods);
		}

		@Override
		public String getName() { return "Key Pressed"; }
	}

	static public class KeyReleasedEvent extends KeyInputEvent {
		public KeyReleasedEvent(long window, int key, int scancode, int mods) {
			super(EventType.KEY_RELEASE, window, key, RELEASE, scancode, mods);
		}

		@Override
		public String getName() { return "Key Released"; }
	}

	static public class KeyRepeatedEvent extends KeyInputEvent {
		public KeyRepeatedEvent(long window, int key, int scancode, int mods) {
			super(EventType.KEY_REPEAT, window, key, REPEAT, scancode, mods);
		}

		@Override
		public String getName() { return "Key Repeated"; }
	}
}
