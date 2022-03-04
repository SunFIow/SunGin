package com.sunflow.logging;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SunLogger {
	public static final Level OFF = SunLevel.OFF;
	public static final Level DEBUG = SunLevel.DEBUG;
	public static final Level INFO = SunLevel.INFO;
	public static final Level WARNING = SunLevel.WARNING;
	public static final Level ERROR = SunLevel.ERROR;
	public static final Level FATAL = SunLevel.FATAL;
	public static final Level ALL = SunLevel.ALL;

	private final Logger logger;

	public void forEachHandler(Consumer<? super Handler> action) {
		Arrays.asList(logger.getHandlers()).forEach(action);
	}

	public SunLogger(Logger logger) { this.logger = logger; }

	public void log(Level level, String logMessage) { logger.log(level, logMessage); }

	public void log(Level level, String logMessage, Object... params) { logger.log(level, logMessage, params); }

	public void log(Level level, String logMessage, Throwable throwable) { logger.log(level, logMessage, throwable); }

	public void log(Level level, Supplier<String> msgSupplier) { logger.log(level, msgSupplier); }

	public void log(Level level, Throwable throwable, Supplier<String> msgSupplier) { logger.log(level, throwable, msgSupplier); }

	public void debug(String logMessage) { log(SunLevel.DEBUG, logMessage); }

	public void info(String logMessage) { log(SunLevel.INFO, logMessage); }

	public void warn(String logMessage) { log(SunLevel.WARNING, logMessage); }

	public void error(String logMessage) { log(SunLevel.ERROR, logMessage); }

	public void fatal(String logMessage) { log(SunLevel.FATAL, logMessage); }

	public void debug(String logMessage, Object... params) { log(SunLevel.DEBUG, logMessage, params); }

	public void info(String logMessage, Object... params) { log(SunLevel.INFO, logMessage, params); }

	public void warn(String logMessage, Object... params) { log(SunLevel.WARNING, logMessage, params); }

	public void error(String logMessage, Object... params) { log(SunLevel.ERROR, logMessage, params); }

	public void fatal(String logMessage, Object... params) { log(SunLevel.FATAL, logMessage, params); }

	public void log(Level level, Object logMessage) { logger.log(level, logMessage.toString()); }

	public void log(Level level, Object logMessage, Throwable throwable) { logger.log(level, logMessage.toString(), throwable); }

	public void debug(Object logMessage) { log(SunLevel.DEBUG, "{0}", logMessage); }

	public void info(Object logMessage) { log(SunLevel.INFO, "{0}", logMessage); }

	public void warn(Object logMessage) { log(SunLevel.WARNING, "{0}", logMessage); }

	public void error(Object logMessage) { log(SunLevel.ERROR, "{0}", logMessage); }

	public void fatal(Object logMessage) { log(SunLevel.FATAL, "{0}", logMessage); }

	/**
	 * Checks whether sunLogger is enabled for the {@link SunLevel#DEBUG DEBUG} Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level DEBUG, {@code false} otherwise.
	 */
	public boolean isDebugEnabled() { return logger.isLoggable(SunLevel.DEBUG); }

	/**
	 * Checks whether sunLogger is enabled for the {@link SunLevel#INFO INFO} Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level DEBUG, {@code false} otherwise.
	 */
	public boolean isInfoEnabled() { return logger.isLoggable(SunLevel.INFO); }

	/**
	 * Checks whether sunLogger is enabled for the {@link SunLevel#WARNING WARNING} Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level DEBUG, {@code false} otherwise.
	 */
	public boolean isWarningEnabled() { return logger.isLoggable(SunLevel.WARNING); }

	/**
	 * Checks whether sunLogger is enabled for the {@link SunLevel#ERROR ERROR} Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level DEBUG, {@code false} otherwise.
	 */
	public boolean isErrorEnabled() { return logger.isLoggable(SunLevel.ERROR); }

	/**
	 * Checks whether sunLogger is enabled for the {@link SunLevel#FATAL FATAL} Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level DEBUG, {@code false} otherwise.
	 */
	public boolean isFatalEnabled() { return logger.isLoggable(SunLevel.FATAL); }

	/**
	 * Checks whether sunLogger is enabled for the {@link SunLevel#ALL ALL} Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level DEBUG, {@code false} otherwise.
	 */
	public boolean isEnabled() { return logger.isLoggable(SunLevel.ALL); }
}
