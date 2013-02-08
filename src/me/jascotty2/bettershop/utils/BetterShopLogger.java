/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: plugin logging
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.jascotty2.bettershop.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.jascotty2.bettershop.BSConfig;
import me.jascotty2.bettershop.BetterShop;

/**
 * @author jacob
 */
public class BetterShopLogger extends Logger {

	private static Logger _logger = Logger.getLogger("Minecraft");
	protected final static Logger logger = new BetterShopLogger();
	protected static String logFormat = "[BetterShop] %s";
	public FileWriter commlog_fstream = null;
	public BufferedWriter commlog_out = null;

	BetterShopLogger() {
		//super("Minecraft", null);
		super(null, null);
	}

	public static void init(Logger customLogger) {
		if (customLogger != null) {
			_logger = customLogger;
		}
	}
	
	public static void setUsePrefix(boolean b) {
		logFormat = b ? "[BetterShop] %s" : "%s";
	}
	/*
	public class BetterShopLogger extends CustomLogger {
	
	protected final static Logger logger = new BetterShopLogger();
	public FileWriter commlog_fstream = null;
	public BufferedWriter commlog_out = null;
	
	BetterShopLogger() {
	super("BetterShop", "Minecraft");
	}*/

	@Override
	public synchronized void log(Level level, String message, Object param) {
		//super.log(level, message == null ? null : String.format(logFormat, message), param);
		//_logger.log(level, message == null ? null : String.format(logFormat, message), param);
		Log(level, message, param);
	}

	@Override
	public synchronized void log(Level level, String message, Object[] params) {
		//super.log(level, message == null ? null : String.format(logFormat, message), params);
		//_logger.log(level, message == null ? null : String.format(logFormat, message), params);
		Log(level, message, params);
	}

	@Override
	public synchronized void log(Level level, String message, Throwable thrown) {
		//super.log(level, message == null ? null : String.format(logFormat, message), thrown);
		//_logger.log(level, message == null ? null : String.format(logFormat, message), thrown);
		Log(level, message, thrown);
	}

	@Override
	public synchronized void severe(String msg) {
		Log(Level.SEVERE, msg, null);
	}

	@Override
	public synchronized void warning(String msg) {
		Log(Level.WARNING, msg, null);
	}

	@Override
	public synchronized void info(String msg) {
		Log(Level.INFO, msg, null);
	}

	@Override
	public synchronized void config(String msg) {
		_logger.config(msg);
	}

	@Override
	public synchronized void fine(String msg) {
		Log(Level.FINE, msg, null);
	}

	@Override
	public synchronized void finer(String msg) {
		Log(Level.FINER, msg, null);
	}

	@Override
	public synchronized void finest(String msg) {
		Log(Level.FINEST, msg, null);
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void Fine(String msg) {
		Log(Level.FINE, msg, null);
	}

	public static void Info(String msg) {
		Log(Level.INFO, msg, (Throwable) null);
	}

	public static void Warning(String msg) {
		Log(Level.WARNING, msg, null);
	}

	public static void Warning(Exception err) {
		Log(Level.WARNING, null, err);
	}

	public static void Warning(String msg, Throwable err) {
		Log(Level.WARNING, msg, err);
	}


	public static void Severe(String msg) {
		Log(Level.SEVERE, msg, null);
	}

	public static void Severe(Exception err) {
		Log(Level.SEVERE, null, err);
	}

	public static void Severe(String msg, Throwable err) {
		Log(Level.SEVERE, msg, err);
	}

	public static void Log(String msg) {
		Log(Level.INFO, msg, null);
	}

	public static void Log(String msg, Throwable err) {
		Log(Level.INFO, msg, err);
	}

	public static void Log(Level loglevel, String msg) {
		Log(loglevel, msg, null);
	}

	public static void Log(Level loglevel, Exception err) {
		Log(loglevel, null, err);
	}

	public static void Log(Level loglevel, Throwable err) {
		Log(loglevel, null, err);
	}

	public static void Log(Level loglevel, String msg, Object params) {
		if (params != null && params instanceof Throwable) {
			Throwable err = (Throwable) params;
			if (msg == null) {
				_logger.log(loglevel, String.format(logFormat,
						err == null ? "? unknown exception ?" : err.getMessage()), err);
			} else {
				_logger.log(loglevel, String.format(logFormat, msg), err);
			}
		} else if (msg == null) {
			_logger.log(loglevel, String.format(logFormat), params);
		} else {
			_logger.log(loglevel, String.format(logFormat, msg), params);
		}
	}

	public static void Log(Level loglevel, String msg, Object[] params) {
		if (msg == null) {
			_logger.log(loglevel, String.format(logFormat), params);
		} else {
			_logger.log(loglevel, String.format(logFormat, msg), params);
		}
	}

	public static void LogCommand(String playername, String command) {
		((BetterShopLogger) logger).logCommand(playername, command);
	}

	public static void CloseCommandLog() {
		((BetterShopLogger) logger).closeCommandLog();
	}

	public void logCommand(String playername, String command) {
		if (commlog_fstream == null) {
			try {
				commlog_fstream = new FileWriter(new File(BSConfig.pluginFolder,
						BetterShop.getSettings().commandFilename), true);
				commlog_out = new BufferedWriter(commlog_fstream);
			} catch (IOException ex) {
				Log(Level.SEVERE, "Failed to open logfile for writing", ex);
				commlog_fstream = null;
				commlog_out = null;
				return;
			}
		}
		try {
			commlog_out.write(commandLogStr(playername, command));
			commlog_out.newLine();
		} catch (IOException ex) {
			BetterShopLogger.Log(Level.SEVERE, "Failed to write to logfile", ex);
		}
	}

	public void closeCommandLog() {
		if (commlog_fstream != null) {
			try {
				commlog_out.flush();
				commlog_out.close();
				commlog_fstream.flush();
				commlog_fstream.close();
			} catch (IOException ex) {
				// BetterShopLogger.Log(Level.SEVERE, "Failed to write to logfile", ex, false);
			}
			commlog_out = null;
			commlog_fstream = null;
		}
	}

	static String commandLogStr(String playername, String command) {
		String time[] = (new java.text.SimpleDateFormat("kk:hh:mm:ss:a:z:Z:yyyy:MM:ww:DD:dd:EEE")).format(new java.util.Date()).split(":");
		return BetterShop.getSettings().getString("logformat").
				replace("<H>", time[0]).
				replace("<h>", time[1]).
				replace("<m>", time[2]).
				replace("<s>", time[3]).
				replace("<a>", time[4]).
				replace("<z>", time[5]).
				replace("<Z>", time[6]).
				replace("<y>", time[7]).
				replace("<M>", time[8]).
				replace("<w>", time[9]).
				replace("<D>", time[10]).
				replace("<d>", time[11]).
				replace("<E>", time[12]).
				replace("<t>", "\t").
				replace("<e>", String.valueOf((int) (System.currentTimeMillis() / 1000))).
				replace("<u>", playername).replace("<user>", playername).
				replace("<c>", command).replace("<command>", command);
	}
} // end class BetterShopLogger

