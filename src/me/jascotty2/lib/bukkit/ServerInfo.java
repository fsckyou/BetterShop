/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: methods for obtaining more info about a bukkit server
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

/**
 * @author jacob
 */
public class ServerInfo {

	public static String suid = null, sip = null, sipM = null;
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
				try{
					String javaPath = System.getProperty("java.home");
					suid += javaPath;
					File java = new File(javaPath);
					suid += String.valueOf(java.lastModified());
				}catch(Exception e){
				}
				suid = SUIDmd5Str(suid);
			} catch (Exception ex) {
				try {
					//Logger.getAnonymousLogger().log(Level.WARNING, ex.getMessage(), ex);
					suid = SUIDmd5Str(serverIPs());
				} catch (Exception ex1) {
					suid = shorten(serverIPs().replace(":", "").replace(".", ""), 16);
				}
			}
		}

		return suid;
	}

	private static String shorten(String s, int len) {
		return len > 0 && s.length() > len ? s.substring(0, len) : s;
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

	public static String serverIPs() {
		return serverIPs(false);
	}

	public static String serverIPs(int maxLen) {
		return shorten(serverIPs(false), maxLen);
	}

	public static String serverIPs(boolean mask, int maxLen) {
		return shorten(serverIPs(mask), maxLen);
	}

	public static String serverIPs(boolean mask) {
		if (sip == null) {
			sip = "";
			try {
				// Obtain the InetAddress of the computer on which this program is running
				InetAddress localaddr = InetAddress.getLocalHost();
				sip = localaddr.getHostName();
				try {
					URL autoIP = new URL("http://automation.whatismyip.com/n09230945.asp");
					BufferedReader in = new BufferedReader(new InputStreamReader(autoIP.openStream()));
					sip += ":" + (in.readLine()).trim();

				} catch (Exception e) {
					sip += ":ukpip";
				}
				for (InetAddress i : InetAddress.getAllByName(localaddr.getHostName())) {
					if (!i.isLoopbackAddress()) {
						sip += ":" + i.getHostAddress();
					}
				}
			} catch (Exception ex) {
				sip += ":ukh";
			}
		}
		if (mask && sipM == null) {
			try {
				sipM = SUIDmd5Str(sip);
			} catch (Exception ex) {
				//Logger.getLogger(ServerInfo.class.getName()).log(Level.SEVERE, null, ex);
				sipM = shorten(sip.replace(":", "").replace(".", ""), 16);
			}
		}
		return mask ? sipM : sip;
	}

	public static String getBukkitVersion() {
		return getBukkitVersion(false);
	}

	/** reads the server log for Bukkit Version
	 * @param includeStart if true, will append a newline a & the last start timestamp
	 * @return
	 */
	public static String getBukkitVersion(boolean includeStart) {
		File slog = new File("server.log");
		if (slog.exists() && slog.canRead()) {
			FileReader fstream = null;
			try {
				String ver = "";
				fstream = new FileReader(slog.getAbsolutePath());
				BufferedReader in = new BufferedReader(fstream);

				String line = "";
				while ((line = in.readLine()) != null) {
					if (line.contains("This server is running Craftbukkit version git-Bukkit-")) {
						ver = line;
					}
				}
				if (ver.length() > 0) {
					return !includeStart ? ver.substring(ver.indexOf("git-Bukkit-"))
							: ver.substring(ver.indexOf("git-Bukkit-")) + "\nStartTime: " + ver.substring(0, 19)
							+ "  (" + serverRunTimeSpan(ver.substring(0, 19)) + ")";
				} else {
					return "?";
				}
			} catch (Exception ex) {
				Logger.getAnonymousLogger().log(Level.SEVERE, ex.getMessage(), ex);
			} finally {
				try {
					fstream.close();
				} catch (IOException ex) {
				}
			}

		}
		return "?";
	}

	public static String getBukkitRunTimeSpan() {
		String b = getBukkitVersion(true);
		if (b.contains("(")) {
			b = b.substring(b.lastIndexOf('(') + 1);
			b = b.substring(0, b.length() - 1);
			return b;
		}
		return "?";
	}

	public static String serverRunTimeSpan(String startTime) {
		Date uploadDate = null;
		try {
			// 2011-04-01 21:35:22
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
			uploadDate = formatter.parse(startTime.trim());
		} catch (ParseException ex) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Error parsing log start date", ex);
			return ex.getMessage();
		}
		long sec = ((new Date()).getTime() - uploadDate.getTime()) / 1000;
		int mon = (int) (sec / 2592000);
		sec -= mon * 2592000;

		int day = (int) (sec / 86400);
		sec -= day * 86400;

		int hr = (int) (sec / 3600);
		sec -= hr * 3600;

		int min = (int) (sec / 60);
		sec = sec % 60;

		String timeSpan = "";
		if (mon > 0) {
			timeSpan += mon + " Months, ";
		}
		if (day > 0) {
			timeSpan += day + " Days, ";
		}
		if (hr > 0) {
			timeSpan += hr + " Hours, ";
		}
		if (min > 0) {
			timeSpan += min + " Minutes, ";
		}
		return timeSpan + sec + " Sec";
	}

	public static String[] installedPlugins(Server sv) {
		ArrayList<String> enabled = new ArrayList<String>();
		for (Plugin p : sv.getPluginManager().getPlugins()) {
			if (p.isEnabled()) {
				enabled.add((p.getDescription().getName() != null ? p.getDescription().getName() : p.toString())
						+ (p.getDescription().getVersion() != null ? " v" + p.getDescription().getVersion() : ""));
			}
		}
		return enabled.toArray(new String[0]);
	}

	public static String installedPluginsStr(Server sv) {
		String allplugins[] = ServerInfo.installedPlugins(sv),
				plugins = "";
		for (int i = 0; i < allplugins.length; ++i) {
			plugins += allplugins[i];
			if (i + 1 < allplugins.length) {
				plugins += ", ";
			}
		}
		return plugins;
	}

	public static String[] serverWorldNames(Server sv) {
		ArrayList<String> enabled = new ArrayList<String>();
		for (World w : sv.getWorlds()) {
			enabled.add(w.getName());// + (w.getPVP() ? " (pvp)" : " (no pvp)"));
		}
		return enabled.toArray(new String[0]);
	}

	public static String serverWorldNamesStr(Server sv) {
		String allworlds[] = ServerInfo.serverWorldNames(sv),
				worlds = "";
		for (int i = 0; i < allworlds.length; ++i) {
			worlds += allworlds[i];
			if (i + 1 < allworlds.length) {
				worlds += ", ";
			}
		}
		return worlds;
	}
} // end class ServerInfo

