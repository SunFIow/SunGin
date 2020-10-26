package com.sunflow;

public class Settings {

	public boolean autostart;
	public ScreenType screentype;

	public Settings autostart(boolean autostart) { this.autostart = autostart; return this; }

	public Settings screentype(ScreenType screentype) { this.screentype = screentype; return this; }

	public Settings defaultSettings() {
		autostart = true;
		screentype = ScreenType.JAVA;
		return this;
	}

	public enum ScreenType {
		JAVA, OPENGL
	}

}
