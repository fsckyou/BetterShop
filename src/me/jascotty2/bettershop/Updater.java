/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: checks for & can install bettershop updates from the github download page
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

package me.jascotty2.bettershop;

import me.jascotty2.bettershop.utils.BetterShopLogger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.net.GitJarUpdater;

/**
 * @author jacob
 */
public class Updater extends GitJarUpdater {

	protected final static Updater update = new Updater();
	
	@Override
	public String getDownloadPage(int attempt) {
		switch(attempt){
			case 0:
				return "https://github.com/BetterShop/BetterShop/downloads";
			case 1:
				return "https://github.com/jascotty2/BetterShop/downloads";
		}
		return null;
	}

	@Override
	public String getDownloadLink(int attempt) {
		switch(attempt){
			case 0:
				return "/downloads/BetterShop/BetterShop/BetterShop.jar";
			case 1:
				return "/downloads/jascotty2/BetterShop/BetterShop.jar";
		}
		return null;
	}

	@Override
	public String getDownloadLinkCounter(int attempt) {
		switch(attempt){
			case 0:
				return "https://github.com/downloads/BetterShop/BetterShop/BetterShop.jar";
			case 1:
				return "https://github.com/downloads/jascotty2/BetterShop/BetterShop.jar";
		}
		return null;
	}

	@Override
	public String getDownloadLinkUrl(int attempt) {
		switch(attempt){
			case 0:
				return "http://cloud.github.com/downloads/BetterShop/BetterShop/BetterShop.jar";
			case 1:
				return "http://cloud.github.com/downloads/jascotty2/BetterShop/BetterShop.jar";
		}
		return null;
	}

	public static void Check() {
		IsUpToDate(true);
	}
	
	public static boolean IsUpToDate() {
		return IsUpToDate(false);
	}

	public static boolean IsUpToDate(boolean log) {
		// check last update time
		long t = readUpdatedFile();
		long d = System.currentTimeMillis() - t;
		if (d > 0 && d < BetterShop.lastUpdated_gracetime * 60000) {
			BetterShopLogger.Log("Recently Updated: skipping update check");
			return true;
		}
		try {
			DateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm Z", Locale.US);
			Date pluginDate = formatter.parse(BetterShop.lastUpdatedStr);
			return update.isUpToDate(
					BetterShop.bettershopPlugin.getDescription().getVersion(),
					pluginDate, BetterShop.lastUpdated_gracetime * 60000,
					log ? BetterShopLogger.getLogger() : null);
		} catch (ParseException ex) {
			BetterShopLogger.Severe(ex);
		}
		return true;
	}

	public static boolean DownloadUpdate() {
		try {
			update.downloadUpdate();
			setUpdatedFile();
			return true;
		}/* catch (MalformedURLException ex) {
		Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
		Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
		}*/ catch (Exception ex) {
			BetterShopLogger.Log(Level.SEVERE, "Failed to download update", ex, false);
		}
		return false;
	}

	static long readUpdatedFile() {
		File versionFile = new File(BSConfig.pluginFolder, "lastUpdate");
		long t = 0;
		if (versionFile.exists() && versionFile.canRead()) {
			FileReader fstream = null;
			BufferedReader in = null;
			try {
				fstream = new FileReader(versionFile.getAbsolutePath());
				in = new BufferedReader(fstream);

				t = CheckInput.GetLong(in.readLine(), 0);

			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, "Error reading update save file", ex);
			} finally {
				if (in != null) {
					try {
						in.close();
						fstream.close();
					} catch (IOException ex) {
					}
				}
			}
		} else {
			setUpdatedFile();
		}
		return t;
	}

	static void setUpdatedFile() {
		File versionFile = new File(BSConfig.pluginFolder, "lastUpdate");
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {
			versionFile.createNewFile();
			fstream = new FileWriter(versionFile.getAbsolutePath());
			out = new BufferedWriter(fstream);

			out.write(String.valueOf(System.currentTimeMillis()));

		} catch (Exception ex) {
			BetterShopLogger.Log(Level.SEVERE, "Error saving update save file", ex);
		} finally {
			if (out != null) {
				try {
					out.close();
					fstream.close();
				} catch (IOException ex) {
				}
			}
		}
	}

	/*
	public boolean loadNew() {
	File pluginFile = new File(new File("plugins"), pluginName + ".jar");
	if (pluginFile.isFile()) {
	try {
	Plugin newPlugin = serverPM.loadPlugin(pluginFile);
	if (newPlugin != null) {
	pluginName = newPlugin.getDescription().getName();
	sender.sendMessage("§ePlugin Loaded: §c[" + pluginName + "]");
	serverPM.enablePlugin(newPlugin);
	if (newPlugin.isEnabled()) {
	sender.sendMessage("§ePlugin Enabled: §a[" + pluginName + "]");
	} else {
	sender.sendMessage("§ePlugin §cFAILED§e to Enable:§c[" + pluginName + "]");
	}
	} else {
	sender.sendMessage("§ePlugin §cFAILED§e to Load!");
	}
	} catch (InvalidPluginException ex) {
	sender.sendMessage("§cFile exists but is not a plugin file.");
	} catch (InvalidDescriptionException ex) {
	sender.sendMessage("§cPlugin exists but is invalid.");
	}
	} else {
	sender.sendMessage("§cFile does NOT exist, check name and try again.");
	}
	return false;
	}
	 */
} // end class Updater

