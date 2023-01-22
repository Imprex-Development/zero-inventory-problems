package net.imprex.zip.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ZIPLogger {

	private static Logger logger = Logger.getLogger("bukkit.zeroinventoryproblems");

	private static final String LOG_PREFIX = "[ZeroInventoryProblems] ";
	private static final String LOG_DEBUG_PREFIX = "[ZeroInventoryProblems/Debug] ";

	private static boolean verbose = false;

	public static void setVerbose(boolean verbose) {
		ZIPLogger.verbose = verbose;
	}

	public static void debug(String message) {
		if (ZIPLogger.verbose) {
			ZIPLogger.logger.log(Level.FINE, LOG_DEBUG_PREFIX + message);
		}
	}

	public static void info(String message) {
		ZIPLogger.logger.log(Level.INFO, LOG_PREFIX + message);
	}

	public static void warn(String message) {
		ZIPLogger.logger.log(Level.WARNING, LOG_PREFIX + message);
	}

	public static void error(String message, Throwable throwable) {
		ZIPLogger.logger.log(Level.SEVERE, LOG_PREFIX + message, throwable);
	}
}