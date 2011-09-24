/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: gives methods to upload error reports to an ftp server
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

package me.jascotty2.lib.bukkit;

import me.jascotty2.lib.util.Str;
import me.jascotty2.lib.net.FTP;
import me.jascotty2.lib.util.Rand;
import java.util.Date;
import org.bukkit.plugin.Plugin;

/**
 * @author jacob
 */
public class FTPErrorReporter {

	// max length of custom message or ip string
	public static final int MAX_MSG_LEN = 90;
	static FTP uploader = null;

	public FTPErrorReporter(String username,
			String password,
			String hostname,
			int maxfilesize) {

		uploader = new FTP(
				username,
				password,
				hostname,
				maxfilesize);
	}

	public String SendNewText(String txt) throws Exception {

		String fn = String.valueOf((int) System.currentTimeMillis() / 1000).replace("-", "") + Rand.randFname(7, 15); // String.valueOf((new Date()).getTime()).substring(3)

		//String fn = Rand.randFname(15, 25);
		if (uploader.SendNewText(fn, txt)) {
			return fn;
		} else {
			return null;
		}
	}

	public boolean SendReport(Plugin plugin, boolean obscureIP,
			String customMessage, String buildDate, String lastAction, String moreInfo,
			String errorMessage, Exception error) throws Exception {
		return SendReport(plugin, obscureIP, customMessage, buildDate,
				lastAction, moreInfo, errorMessage,
				(error == null ? (Throwable) null : error.fillInStackTrace()));
	}

	public boolean SendReport(Plugin plugin, boolean obscureIP,
			String customMessage, String buildDate, String lastAction, String moreInfo,
			String errorMessage, Throwable error) throws Exception {
		if (plugin == null || plugin.getDescription() == null
				|| plugin.getDescription().getName() == null
				|| plugin.getDescription().getVersion() == null
				|| (errorMessage == null && error == null)) {
			return false;
		}
		customMessage = trimMsgStr(customMessage, true);
		if (buildDate != null) {
			buildDate = trimMsgStr(buildDate);
		}
		if (lastAction != null) {
			lastAction = trimMsgStr(lastAction, true);
		}
//        if (moreInfo != null) {
//            moreInfo = trimMsgStr(moreInfo, true);
//        }
		if (errorMessage != null) {
			errorMessage = trimMsgStr(errorMessage, true);
		} else {
			errorMessage = (error.getMessage() != null && error.getMessage().length() > 0 ? error.getMessage() : "") + "\n";
		}

//		String allplugins[] = ServerInfo.installedPlugins(plugin.getServer()),
//				plugins = "";
//		for (int i = 0; i < allplugins.length; ++i) {
//			plugins += allplugins[i];
//			if (i + 1 < allplugins.length) {
//				plugins += ", ";
//			}
//		}

		return SendNewText(
				plugin.getDescription().getName()
				+ "Version: " + plugin.getDescription().getVersion() + (buildDate != null ? "  (" + buildDate + ")" : "")
				+ " Error Report at " + (new Date()).toString() + "\n"
				+ "SUID: " + ServerInfo.serverUID() + "\n"
				+ (obscureIP ? "" : "IP: " + plugin.getServer().getPort() + ";" + ServerInfo.serverIPs(MAX_MSG_LEN) + "\n")
				+ (customMessage != null && !customMessage.isEmpty() ? customMessage + "\n" : "")
				+ "Machine: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "\n"
				+ "Bukkit: " + ServerInfo.getBukkitVersion(true) + "\n" //plugin.getServer().getVersion() + " (" + ServerInfo.getBukkitRunTimeSpan() + ")\n"
				//ServerInfo.getBukkitVersion(true) + "\n"
//				+ "Plugins (" + allplugins.length + ") " + plugins + "\n"
				+ (lastAction != null ? "Last action: " + lastAction + "\n" : "")
				+ (moreInfo != null && !moreInfo.isEmpty() ? moreInfo + "\n" : "")
				+ "Message: " + errorMessage
				+ (error != null ? ((error.getLocalizedMessage() != null && error.getLocalizedMessage().length() > 0
				&& (error.getMessage() == null || !error.getMessage().equals(error.getLocalizedMessage())) ? error.getLocalizedMessage() + "\n" : "")
				+ Str.getStackStr(error) + "\n") : "")) != null;
	}

	private static String trimMsgStr(String msg, boolean newLine) {
		if (msg == null) {
			return "";
		} else {
			if (msg.length() > MAX_MSG_LEN) {
				msg = msg.substring(0, MAX_MSG_LEN);
			}
			if (newLine) {
				return msg.trim() + "\n";
			}
			return msg.trim();
		}
	}

	private static String trimMsgStr(String msg) {
		if (msg == null) {
			return "";
		} else {
			if (msg.length() > MAX_MSG_LEN) {
				msg = msg.substring(0, MAX_MSG_LEN);
			}
			return msg.trim();
		}
	}
} // end class FTPErrorReporter

