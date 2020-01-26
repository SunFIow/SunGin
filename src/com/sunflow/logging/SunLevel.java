package com.sunflow.logging;

import java.util.logging.Level;

class SunLevel extends Level {
	public static final Level DEBUG = new SunLevel("DEBUG", 700);
	public static final Level INFO = new SunLevel("INFO", 800);
	public static final Level WARN = new SunLevel("WARN", 900);
	public static final Level ERROR = new SunLevel("ERROR", 1000);
	public static final Level FATAL = new SunLevel("FATAL", 1100);

	protected SunLevel(String name, int value) { super(name, value); }

	protected SunLevel(String name, int value, String resourceBundleName) { super(name, value, resourceBundleName); }
}