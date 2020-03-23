package com.sunflow.util;

import java.util.logging.Level;

import com.sunflow.logging.Log;

public interface LogUtils {
	public static final LogUtils instance = new LogUtils() {};

	default void log(Level level, Object logMessage) { Log.log(level, logMessage); }

	default void log(Level level, String logMessage, Object... params) { Log.log(level, logMessage, params); }

	default void log(Level level, Object logMessage, Throwable throwable) { Log.log(level, logMessage, throwable); }

	default void debug(Object logMessage) { Log.debug(logMessage); }

	default void info(Object logMessage) { Log.info(logMessage); }

	default void warn(Object logMessage) { Log.warn(logMessage); }

	default void error(Object logMessage) { Log.error(logMessage); }

	default void fatal(Object logMessage) { Log.fatal(logMessage); }

	default void debug(String logMessage, Object... params) { Log.debug(logMessage, params); }

	default void info(String logMessage, Object... params) { Log.info(logMessage, params); }

	default void warn(String logMessage, Object... params) { Log.warn(logMessage, params); }

	default void error(String logMessage, Object... params) { Log.error(logMessage, params); }

	default void fatal(String logMessage, Object... params) { Log.fatal(logMessage, params); }

	default void debug(Object... params) { Log.debug(params); }

	default void info(Object... params) { Log.info(params); }

	default void warn(Object... params) { Log.warn(params); }

	default void error(Object... params) { Log.error(params); }

	default void fatal(Object... params) { Log.fatal(params); }

	default void debug(String logMessage, Throwable e) { Log.debug(logMessage, e); }

	default void info(String logMessage, Throwable e) { Log.info(logMessage, e); }

	default void warn(String logMessage, Throwable e) { Log.warn(logMessage, e); }

	default void error(String logMessage, Throwable e) { Log.error(logMessage, e); }

	default void fatal(String logMessage, Throwable e) { Log.fatal(logMessage, e); }
}
