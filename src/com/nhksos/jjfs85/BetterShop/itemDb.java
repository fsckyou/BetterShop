/** This code was literally yoinked from the Essentials plugin source
 * All credit for this work goes to Zenexer, ementalo, Eris,
 * and/or Eggroll. Hope you guys don't mind that I'm using it, but hey,
 * why reinvent the wheel, right?
 * 
 * Ok, now I've had to edit it to add subtype support for my plugin. ~jjfs85
 */

package com.nhksos.jjfs85.BetterShop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.material.MaterialData;

public class itemDb {
	private final static Logger logger = Logger.getLogger("Minecraft");
	private static Map<String, Integer> map = new HashMap<String, Integer>();
	private static Map<String, Byte> submap = new HashMap<String, Byte>();

	public static void load(File folder, String fname) throws IOException {
		folder.mkdirs();
		File file = new File(folder, fname);
		if (!file.exists()) {
			file.createNewFile();
			InputStream res = itemDb.class.getResourceAsStream("/items.db");
			FileWriter tx = new FileWriter(file);
			try {
				for (int i = 0; (i = res.read()) > 0;)
					tx.write(i);
			} finally {
				tx.flush();
				tx.close();
				res.close();
			}
		}

		BufferedReader rx = new BufferedReader(new FileReader(file));
		try {
			map.clear();
			for (int i = 0; rx.ready(); i++) {
				try {
					String line = rx.readLine().trim().toLowerCase();
					if (line.startsWith("#"))
						continue;
					String[] parts = line.split("[^a-z0-9]");
					if (parts.length < 2)
						continue;
					int numeric = Integer.parseInt(parts[1]);
					map.put(parts[0], numeric);
					if (parts.length == 3) {
						numeric = Integer.parseInt(parts[2]);
						submap.put(parts[0], ((Integer) numeric).byteValue());
					}
				} catch (Exception ex) {
					logger.warning("Error parsing " + fname + " on line " + i);
				}
			}
		} finally {
			rx.close();
		}
	}

	public static MaterialData get(double d) throws Exception {
		int i = (int) Math.floor(d);
		int b = (int) (d - i) * 100;
		return get(String.format("%d:%d", i, b));
	}

	public static MaterialData get(String s) throws Exception {
		MaterialData retval = new MaterialData(0);
		String[] split = s.split(":");
		int id = 0;
		byte data = 0;
		try {
			id = Integer.parseInt(split[0]);
			try {
				data = Byte.parseByte(split[1], 10);
			} catch (Exception e1) {
				data = 0;
			}
			if (map.containsValue(id))
				retval = new MaterialData(id, data);
			else
				throw new Exception("No data allowed");
		} catch (Exception e2) {
			if (map.containsKey(s)) {
				// if it's a valid name
				id = map.get(s);
				retval = new MaterialData(id);
				if (submap.containsKey(s)) {
					retval.setData(submap.get(s));
				}
			} else
				throw new Exception("Unknown material");
		}
		return retval;
	}

	public static String getName(double d) throws Exception {
		int i = (int) Math.floor(d);
		byte b = (byte)Math.round((d-i)*100);
		return getName(i, b);
	}

	public static String getName(int i, byte d) throws Exception {
		File file = new File("plugins/BetterShop", "items.db");
		BufferedReader items = new BufferedReader(new FileReader(file));
		String line = items.readLine();
		while (line != null) {
			if (line.charAt(0) == '#') {
				line = items.readLine();
				continue;
			}
			if (Integer.parseInt(line.split("[^a-z0-9]")[1]) == i) {
				while ((line != null) && (d != 0)) {
					if (line.split("[^a-z0-9]").length == 3) {
						if (Byte.parseByte(line.split("[^a-z0-9]")[2], 10) == d) {
							return line.split("[^a-z0-9]")[0];
						} else {
							line = items.readLine();
						}
					} else {
						line = items.readLine();
					}
				}
				return line.split("[^a-z0-9]")[0];
			}
			line = items.readLine();
		}
		throw new Exception(String.format("EOF: no item # %d-%d", i, d));
	}
}