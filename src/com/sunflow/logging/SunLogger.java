package com.sunflow.logging;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SunLogger {
	public static final Level ALL = Level.ALL;
	public static final Level DEBUG = SunLevel.DEBUG;
	public static final Level INFO = SunLevel.INFO;
	public static final Level WARN = SunLevel.WARN;
	public static final Level ERROR = SunLevel.ERROR;
	public static final Level FATAL = SunLevel.FATAL;
	public static final Level OFF = Level.OFF;

	final Logger logger;

	public SunLogger(Logger logger) { this.logger = logger; }

	public void log(Level level, String logMessage) { logger.log(level, logMessage); }

	public void log(Level level, String logMessage, Object... params) { logger.log(level, logMessage, params); }

	public void log(Level level, String logMessage, Throwable throwable) { logger.log(level, logMessage, throwable); }

	public void log(Level level, Supplier<String> msgSupplier) { logger.log(level, msgSupplier); }

	public void log(Level level, Throwable throwable, Supplier<String> msgSupplier) { logger.log(level, throwable, msgSupplier); }

	public void debug(String logMessage) { log(DEBUG, logMessage); }

	public void info(String logMessage) { log(INFO, logMessage); }

	public void warn(String logMessage) { log(WARN, logMessage); }

	public void error(String logMessage) { log(ERROR, logMessage); }

	public void fatal(String logMessage) { log(FATAL, logMessage); }

	public void debug(String logMessage, Object... params) { log(DEBUG, logMessage, params); }

	public void info(String logMessage, Object... params) { log(INFO, logMessage, params); }

	public void warn(String logMessage, Object... params) { log(WARN, logMessage, params); }

	public void error(String logMessage, Object... params) { log(ERROR, logMessage, params); }

	public void fatal(String logMessage, Object... params) { log(FATAL, logMessage, params); }

	public void log(Level level, Object logMessage) { logger.log(level, logMessage.toString()); }

	public void log(Level level, Object logMessage, Throwable throwable) { logger.log(level, logMessage.toString(), throwable); }

	public void debug(Object logMessage) { log(DEBUG, "{0}", logMessage); }

	public void info(Object logMessage) { log(INFO, "{0}", logMessage); }

	public void warn(Object logMessage) { log(WARN, "{0}", logMessage); }

	public void error(Object logMessage) { log(ERROR, "{0}", logMessage); }

	public void fatal(Object logMessage) { log(FATAL, "{0}", logMessage); }

}
