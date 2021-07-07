package com.sunflow.logging;

import java.util.logging.Level;

class SunLevel extends Level {

	/**
	 * ALL indicates that all messages should be logged.
	 * This level is initialized to <CODE>Integer.MIN_VALUE</CODE>.
	 */
	public static final Level ALL = new SunLevel("ALL", Integer.MIN_VALUE);

	/**
	 * DEBUG is a message level for debug messages.
	 * <p>
	 * DEBUG messages are intended to provide a variety of
	 * information, to assist in debugging problems.
	 * For example, DEBUG message might include ...
	 * This level is initialized to <CODE>600</CODE>.
	 */
	public static final Level DEBUG = new SunLevel("DEBUG", 600);

	/**
	 * INFO is a message level for informational messages.
	 * <p>
	 * Typically INFO messages will be written to the console
	 * or its equivalent. So the INFO level should only be
	 * used for reasonably significant messages that will
	 * make sense to end users and system administrators.
	 * This level is initialized to <CODE>800</CODE>.
	 */
	public static final Level INFO = new SunLevel("INFO", 800);

	/**
	 * WARNING is a message level indicating a potential problem.
	 * <p>
	 * In general WARNING messages should describe events that will
	 * be of interest to end users or system managers, or which
	 * indicate potential problems.
	 * This level is initialized to <CODE>900</CODE>.
	 */
	public static final Level WARNING = new SunLevel("WARNING", 900);

	/**
	 * ERROR is a message level indicating a serious failure.
	 * <p>
	 * In general ERROR messages should describe events that are
	 * of considerable importance and which will prevent normal
	 * program execution. They should be reasonably intelligible
	 * to end users and to system administrators.
	 * This level is initialized to <CODE>1000</CODE>.
	 */
	public static final Level ERROR = new SunLevel("ERROR", 1000);

	/**
	 * FATAL is a message level indicating a fatal failure.
	 * <p>
	 * In general FATAL messages should describe events that are
	 * of extrene importance and which will prevent program execution.
	 * They should be reasonably intelligible
	 * to end users and to system administrators.
	 * This level is initialized to <CODE>1100</CODE>.
	 */
	public static final Level FATAL = new SunLevel("FATAL", 1100);

	/**
	 * OFF is a special level that can be used to turn off logging.
	 * This level is initialized to <CODE>Integer.MAX_VALUE</CODE>.
	 */
	public static final Level OFF = new SunLevel("OFF", Integer.MAX_VALUE);

	protected SunLevel(String name, int value) { super(name, value); }

	protected SunLevel(String name, int value, String resourceBundleName) { super(name, value, resourceBundleName); }
}