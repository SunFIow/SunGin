package com.sunflow.logging;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sunflow.game.GameBase;

public class LogManager {

	private static SunLogger sunLogger = createSunLogger();

	private static SunLogger createSunLogger() {
		Logger logger = Logger.getLogger("com.sunflow.logging.Log");
		logger.setUseParentHandlers(false);
		logger.setLevel(SunLevel.ALL);

		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(new SunFormatter());
		ch.setLevel(SunLevel.ALL);
		logger.addHandler(ch);

		Path dir = Paths.get("logs");
		if (Files.notExists(dir)) try {
			Files.createDirectory(dir);
		} catch (IOException e) {
			logger.log(SunLevel.ERROR, "Logger file Directory could't be created", e);
		}

		try {
			Path path = dir.resolve("debug.log");
			FileHandler fh = new FileHandler(path.toString());
			fh.setFormatter(new SunFormatter());
			fh.setLevel(SunLevel.ALL);
			logger.addHandler(fh);
		} catch (SecurityException | IOException e) {
			logger.log(SunLevel.ERROR, "Debug file logger not working.", e);
		}
		try {
			Path path = dir.resolve("latest.log");
			FileHandler fh = new FileHandler(path.toString());
			fh.setFormatter(new SunFormatter());
			fh.setLevel(SunLevel.INFO);
			logger.addHandler(fh);
		} catch (SecurityException | IOException e) {
			logger.log(SunLevel.ERROR, "Latest file logger not working.", e);
		}

//		Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.log(ERROR, "Uncaught Exception", e));
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> GameBase.UncaughtException(e));

		return new SunLogger(logger);
	}

	public static SunLogger getLogger(String logName) {
		Logger logger = Logger.getLogger(logName);
		logger.setUseParentHandlers(false);
		logger.setLevel(SunLevel.ALL);
		sunLogger.forEachHandler(logger::addHandler);
		return new SunLogger(logger);
	}

	public static SunLogger getLogger() {
		return getLogger(toLoggerName(getCallerClass(3)));
	}

	private static String toLoggerName(Class<?> cls) {
		final String canonicalName = cls.getCanonicalName();
		return canonicalName != null ? canonicalName : cls.getName();

	}

	private static Class<?> getCallerClass(final int depth) {
		if (depth < 0) {
			throw new IndexOutOfBoundsException(Integer.toString(depth));
		}
		// note that we need to add 1 to the depth value to compensate for this method, but not for the Method.invoke
		// since Reflection.getCallerClass ignores the call to Method.invoke()
		try {
			return (Class<?>) GET_CALLER_CLASS.invoke(null, depth + 1 + JDK_7u25_OFFSET);
		} catch (final Exception e) {
			// theoretically this could happen if the caller class were native code
			// TODO: return Object.class
			return null;
		}
	}

	// Checkstyle Suppress: the lower-case 'u' ticks off CheckStyle...
	// CHECKSTYLE:OFF
	private static final int JDK_7u25_OFFSET;
	// CHECKSTYLE:OFF

	private static final Method GET_CALLER_CLASS;

	static {
		Method getCallerClass;
		int java7u25CompensationOffset = 0;
		try {
			final Class<?> sunReflectionClass = Class.forName("sun.reflect.Reflection");
			getCallerClass = sunReflectionClass.getDeclaredMethod("getCallerClass", int.class);
			Object o = getCallerClass.invoke(null, 0);
			getCallerClass.invoke(null, 0);
			if (o == null || o != sunReflectionClass) {
				getCallerClass = null;
				java7u25CompensationOffset = -1;
			} else {
				o = getCallerClass.invoke(null, 1);
				if (o == sunReflectionClass) {
					System.out.println("WARNING: Java 1.7.0_25 is in use which has a broken implementation of Reflection.getCallerClass(). " +
							" Please consider upgrading to Java 1.7.0_40 or later.");
					java7u25CompensationOffset = 1;
				}
			}
		} catch (final Exception | LinkageError e) {
			System.out.println("WARNING: sun.reflect.Reflection.getCallerClass is not supported. This will impact performance.");
			getCallerClass = null;
			java7u25CompensationOffset = -1;
		}

		GET_CALLER_CLASS = getCallerClass;
		JDK_7u25_OFFSET = java7u25CompensationOffset;
	}

	public static void log(Level level, Object logMessage) { sunLogger.log(level, logMessage); }

	public static void log(Level level, String logMessage, Object... params) { sunLogger.log(level, logMessage, params); }

	public static void log(Level level, Object logMessage, Throwable throwable) { sunLogger.log(level, logMessage, throwable); }

	public static void debug(Object logMessage) { sunLogger.log(SunLevel.DEBUG, logMessage); }

	public static void info(Object logMessage) { sunLogger.log(SunLevel.INFO, logMessage); }

	public static void warn(Object logMessage) { sunLogger.log(SunLevel.WARNING, logMessage); }

	public static void error(Object logMessage) { sunLogger.log(SunLevel.ERROR, logMessage); }

	public static void fatal(Object logMessage) { sunLogger.log(SunLevel.FATAL, logMessage); }

	public static void debug(String logMessage, Object... params) { sunLogger.log(SunLevel.DEBUG, logMessage, params); }

	public static void info(String logMessage, Object... params) { sunLogger.log(SunLevel.INFO, logMessage, params); }

	public static void warn(String logMessage, Object... params) { sunLogger.log(SunLevel.WARNING, logMessage, params); }

	public static void error(String logMessage, Object... params) { sunLogger.log(SunLevel.ERROR, logMessage, params); }

	public static void fatal(String logMessage, Object... params) { sunLogger.log(SunLevel.FATAL, logMessage, params); }

	public static void debug(Object... params) { sunLogger.log(SunLevel.DEBUG, getObjectArrayLogMessage(params), params); }

	public static void info(Object... params) { sunLogger.log(SunLevel.INFO, getObjectArrayLogMessage(params), params); }

	public static void warn(Object... params) { sunLogger.log(SunLevel.WARNING, getObjectArrayLogMessage(params), params); }

	public static void error(Object... params) { sunLogger.log(SunLevel.ERROR, getObjectArrayLogMessage(params), params); }

	public static void fatal(Object... params) { sunLogger.log(SunLevel.FATAL, getObjectArrayLogMessage(params), params); }

	private static String getObjectArrayLogMessage(Object... params) {
		int num = params.length;
		StringBuilder builder = new StringBuilder();
		String part1 = "{";
		String part2 = "} ";
		for (int i = 0; i < num; i++) {
			builder.append(part1);
			builder.append(i);
			builder.append(part2);
		}
		return builder.toString();
	}

	public static void debug(String logMessage, Throwable e) { sunLogger.log(SunLevel.DEBUG, logMessage, e); }

	public static void info(String logMessage, Throwable e) { sunLogger.log(SunLevel.INFO, logMessage, e); }

	public static void warn(String logMessage, Throwable e) { sunLogger.log(SunLevel.DEBUG, logMessage, e); }

	public static void error(String logMessage, Throwable e) { sunLogger.log(SunLevel.WARNING, logMessage, e); }

	public static void fatal(String logMessage, Throwable e) { sunLogger.log(SunLevel.FATAL, logMessage, e); }

	/**
	 * Checks whether sunLogger is enabled for the {@link SunLevel#DEBUG DEBUG} Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level DEBUG, {@code false} otherwise.
	 */
	public boolean isDebugEnabled() { return sunLogger.isDebugEnabled(); }

	/**
	 * Checks whether sunLogger is enabled for the {@link SunLevel#INFO INFO} Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level INFO, {@code false} otherwise.
	 */
	public boolean isInfoEnabled() { return sunLogger.isInfoEnabled(); }

	/**
	 * Checks whether sunLogger is enabled for the {@link SunLevel#WARNING WARNING} Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level WARNING, {@code false} otherwise.
	 */
	public boolean isWarningEnabled() { return sunLogger.isWarningEnabled(); }

	/**
	 * Checks whether sunLogger is enabled for the {@link SunLevel#ERROR ERROR} Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level ERROR, {@code false} otherwise.
	 */
	public boolean isErrorEnabled() { return sunLogger.isErrorEnabled(); }

	/**
	 * Checks whether sunLogger is enabled for the {@link SunLevel#FATAL FATAL} Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level FATAL, {@code false} otherwise.
	 */
	public boolean isFatalEnabled() { return sunLogger.isFatalEnabled(); }

	/**
	 * Checks whether sunLogger is enabled for the {@link SunLevel#ALL ALL} Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level ALL, {@code false} otherwise.
	 */
	public boolean isEnabled() { return sunLogger.isEnabled(); }
}
