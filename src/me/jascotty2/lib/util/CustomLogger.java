/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: ( TODO )
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

package me.jascotty2.lib.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomLogger extends Logger {

	protected String name;
	protected final static String logFormat = "[%s] %s";

    public CustomLogger() {
		super(null, null);
		//_logger = Logger.getAnonymousLogger();
    }
    public CustomLogger(String name) {
		super(null, null);
		this.name=name;
		//_logger = Logger.getAnonymousLogger();
    }
    public CustomLogger(String name, String loggerName) {
		super(loggerName, null);
		this.name=name;
		//_logger = loggerOut;
    }
	
	@Override
	public synchronized void log(Level level, String message, Object param) {
		super.log(level, message == null ? null : String.format(logFormat, name, message), param);
		//_logger.log(level, message == null ? null : String.format(logFormat, message), param);
		//Log(level, message, param, true);
	}

	@Override
	public synchronized void log(Level level, String message, Object[] params) {
		super.log(level, message == null ? null : String.format(logFormat, name, message), params);
		//_logger.log(level, message == null ? null : String.format(logFormat, message), params);
		//Log(level, message, params, true);
	}

	@Override
	public synchronized void log(Level level, String message, Throwable thrown) {
		super.log(level, message == null ? null : String.format(logFormat, name, message), thrown);
		//_logger.log(level, message == null ? null : String.format(logFormat, message), thrown);
		//Log(level, message, thrown, true);
	}

	@Override
	public synchronized void severe(String msg) {
		//Log(Level.SEVERE, msg, null, true);
		log(Level.SEVERE, msg, (Object) null);
	}

	@Override
	public synchronized void warning(String msg) {
		//Log(Level.WARNING, msg, null, true);
		log(Level.WARNING, msg, (Object) null);
	}

	@Override
	public synchronized void info(String msg) {
		//Log(Level.INFO, msg, null, true);
		log(Level.INFO, msg, (Object) null);
	}

	@Override
	public synchronized void fine(String msg) {
		//Log(Level.FINE, msg, null, true);
		log(Level.FINE, msg, (Object) null);
	}

	@Override
	public synchronized void finer(String msg) {
		//Log(Level.FINER, msg, null, true);
		log(Level.FINER, msg, (Object) null);
	}

	@Override
	public synchronized void finest(String msg) {
		//Log(Level.FINEST, msg, null, true);
		log(Level.FINEST, msg, (Object) null);
	}

} // end class CutomLogger
