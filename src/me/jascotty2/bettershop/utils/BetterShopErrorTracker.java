/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: for automatically sending error reports to a server
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

import com.jascotty2.minecraftim.MinecraftIM;
import java.util.Date;
import me.jascotty2.bettershop.BSConfig;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.lib.bukkit.FTPErrorReporter;

/**
 * @author jacob
 */
public class BetterShopErrorTracker {

	public static MinecraftIM messenger = null;
	// for error reporting
	static Date sentErrors[] = new Date[3];
	static final long minSendWait = 3600; // min time before a send expires (seconds)
	static final FTPErrorReporter errorReporter = new FTPErrorReporter(
			"bettershopftp", // username
			"5Vm-9hr_y4e@8u5", // password
			"nas.boldlygoingnowhere.org", // hostname
			2500); // maxfilesize

	public static boolean canSendNotification() {
		return messenger != null;
	}

	public static void sendNotification(String txt) {
		if (messenger != null) {
			messenger.sendNotify(txt);
		}
	}

	public static void sendErrorReport(String txt, Throwable err) {
		boolean allow = false;
		long now = (new Date()).getTime();
		for (int i = 0; i < sentErrors.length; ++i) {
			if (sentErrors[i] == null
					|| (now - sentErrors[i].getTime()) / 1000 >= minSendWait) {
				sentErrors[i] = new Date();
				allow = true;
				break;
			}
		}
		if (allow) {
			int pcount = -1;
			if (BetterShop.getMainPricelist() != null) {
				try {
					pcount = BetterShop.getMainPricelist().getItems(true).length;
				} catch (Exception ex) {
				}
			}
			try {
				BSConfig config = BetterShop.getSettings();
				errorReporter.SendReport(BetterShop.getPlugin(),
						/* obscureIP */ config != null ? !config.unMaskErrorID : true,
						/* customMessage */ config != null ? (config.getCustomErrorMessage().length() > 0 ? config.getCustomErrorMessage() : null) : null,
						/* buildDate */ BetterShop.lastUpdatedStr,
						/* lastAction */ BetterShop.getLastCommand(),
						/* moreInfo */ (config != null ? config.condensedSettings() : "-")
						+ "," + (pcount >= 0 ? pcount : "-"),
						/* errorMessage */ txt,
						/* error */ err);
			} catch (Exception e) {
				BetterShopLogger.Log("error uploading error report: " + e.getClass().getName() + ": " + e.getMessage());
			}
			//
			// String fname = FTPErrorReporter.SendNewText(
			// "BetterShop Error Report at " + (new Date()).toString() + "\n"
			// + "SUID: " + ServerInfo.serverUID(config != null ?
			// !config.unMaskErrorID : true, BSConfig.MAX_CUSTMSG_LEN) + "\n"
			// + (config != null ? (config.customErrorMessage.length() > 0 ?
			// config.customErrorMessage + "\n" : "") : "")
			// + "Machine: " + System.getProperty("os.name") + " " +
			// System.getProperty("os.arch") /* + "," +
			// System.getProperty("user.dir")*/ + "\n"
			// + "Bukkit: " + ServerInfo.getBukkitVersion(true) + "\n"
			// + "Version: " + pdfFile.getVersion() + "  (" + lastUpdatedStr +
			// ")\n"
			// + "Econ: " + econPlugin() + "\n"
			// + "Permissions: " + (Permissions != null ? "true" : "false") +
			// "\n"
			// + "Last executed command: " + lastCommand + "\n"
			// + (config != null ? config.condensedSettings() : "-") + "," +
			// (pcount >= 0 ? pcount : "-") + "\n"
			// + "Message: " + (txt != null ? txt : err.getMessage() != null &&
			// err.getMessage().length() > 0 ? err.getMessage() : "") + "\n"
			// + (err.getLocalizedMessage() != null &&
			// err.getLocalizedMessage().length() > 0
			// && (err.getMessage() == null ||
			// !err.getMessage().equals(err.getLocalizedMessage())) ?
			// err.getLocalizedMessage() + "\n" : "")
			// + Str.getStackStr(err) + "\n");
			// if (fname != null && fname.length() > 0) {
			// System.out.println("report sent. id: " + fname);
			// } else {
			// System.out.println("Error report unable to send.. is the server online & BetterShop up-to-date?");
			// System.out.println("(if yes, then the error tracker is likely temporarily offline)");
			// }
		} // else { System.out.println("sending too fast.."); }
	}
} // end class BetterShopErrorTracker

