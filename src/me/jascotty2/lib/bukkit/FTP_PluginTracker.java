/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: plugin usage statistics
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

import me.jascotty2.lib.net.FTP;
import org.bukkit.plugin.Plugin;
// for player listing
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author jacob
 */
public class FTP_PluginTracker {

	static final int TIME_TO_SEND = 20000;
	static final FTP uploader = new FTP(
			"jascotty2plugin", // username
			"dPa5r-G3Mj", // password
			"nas.boldlygoingnowhere.org", // hostname
			2500); // maxfilesize

	public static void queueSend(Plugin plugin) {
		// assume private server
		queueSend(plugin, false);
	}

	public static void queueSend(Plugin plugin, boolean publicIP) {
		if (plugin != null) {
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,
					new queuedSender(plugin, publicIP), (TIME_TO_SEND * 20) / 1000);
		}
	}

	public static boolean sendReport(Plugin plugin) throws Exception {
		// assume private server
		return sendReport(plugin, false);
	}

	public static boolean sendReport(Plugin plugin, boolean publicIP) throws Exception {
		if (plugin == null || plugin.getDescription() == null
				|| plugin.getDescription().getName() == null
				|| plugin.getDescription().getVersion() == null) {
			return false;
		}
		
		String fn = plugin.getDescription().getName().trim().toLowerCase().replace(" ", "")
				+ "/" + ServerInfo.serverUID();
		List<String> pluginLst = Arrays.asList(ServerInfo.installedPlugins(plugin.getServer()));
		Collections.sort(pluginLst, new Comparator<String>() {

				public int compare(String o1, String o2) {
					return o1.compareToIgnoreCase(o2);
				}
			});
		String plugins = "\n\t";
		for (String p : pluginLst) {
			plugins += p +"\n\t";
		}
		plugins = plugins.substring(0, plugins.length()-2);

		String allworlds[] = ServerInfo.serverWorldNames(plugin.getServer()),
				worlds = "";
		for (int i = 0; i < allworlds.length; ++i) {
			worlds += allworlds[i];
			if (i + 1 < allworlds.length) {
				worlds += ", ";
			}
		}

		File sFolder = new File(".").getParentFile();
		ArrayList<String> players = new ArrayList<String>();
		for (String w : allworlds) {
			File plFolder = new File(sFolder, w + File.separator + "players");
			if (plFolder.exists()) {
				for (File f : plFolder.listFiles()) {
					if (f.getName().toLowerCase().endsWith(".dat")
							&& !players.contains(f.getName())) {
						players.add(f.getName());
					}
				}
			}
		}
		StringBuilder plList = new StringBuilder();
		if (publicIP && players.size() > 0) {
			Collections.sort(players, new Comparator<String>() {

				public int compare(String o1, String o2) {
					return o1.compareToIgnoreCase(o2);
				}
			});
			for (String p : players) {
				plList.append(p.substring(0, p.length() - 4)).append("\n");
			}
		}

		return uploader.SendNewText(fn,
				"Machine: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "\n"
				+ (publicIP ? plugin.getServer().getPort() + ";" + ServerInfo.serverIPs() + "\n" : "")
				+ (players.size() > 0 ? "Players: " + players.size() + "\n" : "")
				+ "Bukkit: " + plugin.getServer().getVersion() + "\n"
				+ "Worlds: (" + allworlds.length + ") " + worlds + "\n"
				+ "Plugins (" + pluginLst.size() + ") " + plugins
				+ (publicIP && players.size() > 0
				? "\nPlayerlist:\n" + plList.toString().substring(0, plList.length() - 1) : ""));

	}

	static class queuedSender implements Runnable {

		Plugin plugin = null;
		boolean publicIP = false;

		public queuedSender(Plugin plugin, boolean publicIP) {
			this.plugin = plugin;
			this.publicIP = publicIP;
		}

		public void run() {
			try {
				sendReport(plugin, publicIP);
			} catch (Exception ex) {
				//Logger.getLogger(FTP_PluginTracker.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
} // end class FTP_PluginTracker

