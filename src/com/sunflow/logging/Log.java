package com.sunflow.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
	public static final Level ALL = Level.ALL;
	public static final Level DEBUG = SunLevel.DEBUG;
	public static final Level INFO = SunLevel.INFO;
	public static final Level WARN = SunLevel.WARN;
	public static final Level ERROR = SunLevel.ERROR;
	public static final Level FATAL = SunLevel.FATAL;
	public static final Level OFF = Level.OFF;

	private static SunLogger sunLogger = createSunLogger();

	private static SunLogger createSunLogger() {
		Logger logger = Logger.getLogger("com.sunflow.logging.Log");
		logger.setUseParentHandlers(false);
		logger.setLevel(ALL);

		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(new SunFormatter());
		ch.setLevel(ALL);
		logger.addHandler(ch);

		Path dir = Paths.get("logs");
		if (Files.notExists(dir)) try {
			Files.createDirectory(dir);
		} catch (IOException e) {
			logger.log(ERROR, "Logger file Directory could't be created", e);
		}

		try {
			Path path = dir.resolve("debug.log");
			FileHandler fh = new FileHandler(path.toString());
			fh.setFormatter(new SunFormatter());
			fh.setLevel(ALL);
			logger.addHandler(fh);
		} catch (SecurityException | IOException e) {
			logger.log(ERROR, "Debug file logger not working.", e);
		}
		try {
			Path path = dir.resolve("latest.log");
			FileHandler fh = new FileHandler(path.toString());
			fh.setFormatter(new SunFormatter());
			fh.setLevel(INFO);
			logger.addHandler(fh);
		} catch (SecurityException | IOException e) {
			logger.log(ERROR, "Latest file logger not working.", e);
		}

		Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.log(ERROR, "Uncaught Exception", e));

		return new SunLogger(logger);
	}

	public static SunLogger getLogger(String logName) {
		Logger logger = Logger.getLogger(logName);
		logger.setUseParentHandlers(false);
		logger.setLevel(ALL);
		Arrays.asList(sunLogger.logger.getHandlers()).forEach(logger::addHandler);
		return new SunLogger(logger);
	}

	public static void log(Level level, Object logMessage) { sunLogger.log(level, logMessage); }

	public static void log(Level level, String logMessage, Object... params) { sunLogger.log(level, logMessage, params); }

	public static void log(Level level, Object logMessage, Throwable throwable) { sunLogger.log(level, logMessage, throwable); }

	public static void debug(Object logMessage) { sunLogger.log(DEBUG, logMessage); }

	public static void info(Object logMessage) { sunLogger.log(INFO, logMessage); }

	public static void warn(Object logMessage) { sunLogger.log(WARN, logMessage); }

	public static void error(Object logMessage) { sunLogger.log(ERROR, logMessage); }

	public static void fatal(Object logMessage) { sunLogger.log(FATAL, logMessage); }

	public static void debug(String logMessage, Object... params) { sunLogger.log(DEBUG, logMessage, params); }

	public static void info(String logMessage, Object... params) { sunLogger.log(INFO, logMessage, params); }

	public static void warn(String logMessage, Object... params) { sunLogger.log(WARN, logMessage, params); }

	public static void error(String logMessage, Object... params) { sunLogger.log(ERROR, logMessage, params); }

	public static void fatal(String logMessage, Object... params) { sunLogger.log(FATAL, logMessage, params); }

	public static void debug(Object... params) { sunLogger.log(DEBUG, getObjectArrayLogMessage(params), params); }

	public static void info(Object... params) { sunLogger.log(INFO, getObjectArrayLogMessage(params), params); }

	public static void warn(Object... params) { sunLogger.log(WARN, getObjectArrayLogMessage(params), params); }

	public static void error(Object... params) { sunLogger.log(ERROR, getObjectArrayLogMessage(params), params); }

	public static void fatal(Object... params) { sunLogger.log(FATAL, getObjectArrayLogMessage(params), params); }

	private static String getObjectArrayLogMessage(Object... params) {
		int num = params.length;
		StringBuilder builder = new StringBuilder();
		String part1 = "{";
		String part2 = "}";
		for (int i = 0; i < num; i++) {
			builder.append(part1);
			builder.append(i);
			builder.append(part2);
			builder.append(" ");
		}
		return builder.toString();
	}

	public static void debug(String logMessage, Throwable e) { sunLogger.log(DEBUG, logMessage, e); }

	public static void info(String logMessage, Throwable e) { sunLogger.log(INFO, logMessage, e); }

	public static void warn(String logMessage, Throwable e) { sunLogger.log(DEBUG, logMessage, e); }

	public static void error(String logMessage, Throwable e) { sunLogger.log(WARN, logMessage, e); }

	public static void fatal(String logMessage, Throwable e) { sunLogger.log(FATAL, logMessage, e); }
}
