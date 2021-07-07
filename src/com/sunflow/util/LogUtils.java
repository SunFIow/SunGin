package com.sunflow.util;

import java.util.logging.Level;

import com.sunflow.logging.LogManager;

public interface LogUtils {
	public static final LogUtils instance = new LogUtils() {};

	default void log(Level level, Object logMessage) { LogManager.log(level, logMessage); }

	default void log(Level level, String logMessage, Object... params) { LogManager.log(level, logMessage, params); }

	default void log(Level level, Object logMessage, Throwable throwable) { LogManager.log(level, logMessage, throwable); }

	default void debug(Object logMessage) { LogManager.debug(logMessage); }

	default void info(Object logMessage) { LogManager.info(logMessage); }

	default void warn(Object logMessage) { LogManager.warn(logMessage); }

	default void error(Object logMessage) { LogManager.error(logMessage); }

	default void fatal(Object logMessage) { LogManager.fatal(logMessage); }

	default void debug(String logMessage, Object... params) { LogManager.debug(logMessage, params); }

	default void info(String logMessage, Object... params) { LogManager.info(logMessage, params); }

	default void warn(String logMessage, Object... params) { LogManager.warn(logMessage, params); }

	default void error(String logMessage, Object... params) { LogManager.error(logMessage, params); }

	default void fatal(String logMessage, Object... params) { LogManager.fatal(logMessage, params); }

	default void debug(Object... params) { LogManager.debug(params); }

	default void info(Object... params) { LogManager.info(params); }

	default void warn(Object... params) { LogManager.warn(params); }

	default void error(Object... params) { LogManager.error(params); }

	default void fatal(Object... params) { LogManager.fatal(params); }

	default void debug(String logMessage, Throwable e) { LogManager.debug(logMessage, e); }

	default void info(String logMessage, Throwable e) { LogManager.info(logMessage, e); }

	default void warn(String logMessage, Throwable e) { LogManager.warn(logMessage, e); }

	default void error(String logMessage, Throwable e) { LogManager.error(logMessage, e); }

	default void fatal(String logMessage, Throwable e) { LogManager.fatal(logMessage, e); }
}
