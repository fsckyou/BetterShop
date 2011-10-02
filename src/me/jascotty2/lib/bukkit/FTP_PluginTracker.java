/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: plugin usage statistics
 * mostly a standalone class, only requires me.jascotty2.lib.net.FTP
 * (and bukkit)
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
import org.bukkit.Server;
// plugin sorting
import java.util.Collections;
import java.util.Comparator;
import java.util.Arrays;
import java.util.List;
// for player listing
import org.bukkit.World;
import java.io.File;
import java.util.ArrayList;
// for suid
import java.util.Enumeration;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
	static int sendTaskID = -1;

	public static void queueSend(Plugin plugin) {
		queueSend(plugin, 3);
	}

	public static void queueSend(Plugin plugin, int numTries) {
		if (plugin != null && sendTaskID < 0) {
			sendTaskID = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,
					new queuedSender(plugin, numTries), (TIME_TO_SEND * 20) / 1000);
		}
	}

	public static boolean sendReport(Plugin plugin) throws Exception {
		return plugin == null ? false : sendReport(plugin.getServer());
	}

	public static boolean sendReport(Server server) throws Exception {
		if (server == null) {
			return false;
		}

		String fn = "data/" + Info.serverUID();
		List<String> pluginLst = Arrays.asList(installedPlugins(server));
		Collections.sort(pluginLst, new Comparator<String>() {

			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		StringBuilder plugins = new StringBuilder("\n\t");
		for (String p : pluginLst) {
			plugins.append(p).append("\n\t");
		}
		String allplugins = plugins.substring(0, plugins.length() - 2);

		int numWorlds = server.getWorlds().size();

		File serverFolder = new File(".").getParentFile();

		ArrayList<String> players = new ArrayList<String>();
		for (World w : server.getWorlds()) {
			if (w == null) {
				continue;
			}
			File plFolder = new File(serverFolder, w.getName() + File.separator + "players");
			if (plFolder.exists()) {
				for (File f : plFolder.listFiles()) {
					if (f.getName().toLowerCase().endsWith(".dat")
							&& !players.contains(f.getName())) {
						players.add(f.getName());
					}
				}
			}
		}

		return uploader.SendNewText(fn,
				"Machine: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "\n"
				+ (players.size() > 0 ? "Players: " + players.size() + "\n" : "")
				+ "Bukkit: " + server.getVersion() + "\n"
				+ "Worlds: " + numWorlds + "\n"
				+ "Plugins (" + pluginLst.size() + ") " + allplugins);

	}

	static class queuedSender implements Runnable {

		Plugin plugin = null;
		int tryNum = 0;

		public queuedSender(Plugin plugin, int numTries) {
			this.plugin = plugin;
			tryNum = numTries;
		}

		public void run() {
			try {
				if (sendReport(plugin)) {
					return;
				}
			} catch (Exception ex) {
				//Logger.getLogger(FTP_PluginTracker.class.getName()).log(Level.SEVERE, null, ex);
			}
			if (--tryNum > 0) {
				sendTaskID = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,
						new queuedSender(plugin, tryNum), (TIME_TO_SEND * 20) / 1000);
			}
		}
	}

	static String[] installedPlugins(Server sv) {
		ArrayList<String> enabled = new ArrayList<String>();
		for (Plugin p : sv.getPluginManager().getPlugins()) {
			if (p.isEnabled()) {
				enabled.add((p.getDescription().getName() != null ? p.getDescription().getName() : p.toString())
						+ (p.getDescription().getVersion() != null ? " v" + p.getDescription().getVersion() : ""));
			}
		}
		return enabled.toArray(new String[0]);
	}

	static class Info {

		public static String suid = null;
		private static final String suidSysProps[] = new String[]{
			"os.name", "os.arch", "java.class.version", "user.dir"};

		public static String serverUID() {
			if (suid == null) {
				try {
					suid = "";
					for (String p : suidSysProps) {
						suid += System.getProperty(p);
					}
					Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
					while (nets.hasMoreElements()) {
						NetworkInterface nic = nets.nextElement();
						suid += nic.getDisplayName();
						suid += Arrays.toString(nic.getHardwareAddress());
					}
//				File f = new File(ServerInfo.class.getProtectionDomain().
//						getCodeSource().getLocation().getPath().replace("%20", " ").replace("%25", "%"));
//				suid += f.getParentFile().getAbsolutePath();
					// Get hostname
					try {
						suid += InetAddress.getLocalHost().getHostName();
					} catch (/*UnknownHost*/Exception e) {
					}
					try {
						String javaPath = System.getProperty("java.home");
						suid += javaPath;
						File java = new File(javaPath);
						suid += String.valueOf(java.lastModified());
					} catch (Exception e) {
					}
					suid = SUIDmd5Str(suid);
				} catch (Exception ex) {
					return "0000000000000000";
				}
			}

			return suid;
		}

		public static String SUIDmd5Str(String txt) throws NoSuchAlgorithmException {
			if (txt == null) {
				txt = "null";
			}
			byte hash[] = MessageDigest.getInstance("MD5").digest(txt.getBytes());
			String ret = "";
			char chars[] = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's',
				'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm',
				'~', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
				'-', '+', //',', '.', '=', ':', ';', '/', '?', '!', '@', '#', '$', '%', '^', '&', '*',
				'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S',
				'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M'};
			for (byte b : hash) {
				ret += chars[((int) b + 255) % chars.length];
			}
			return ret;
		}
	}
} // end class FTP_PluginTracker

