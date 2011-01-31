/** This code was literally yoinked from the Essentials plugin source
 * All credit for this work goes to Zenexer, ementalo, Eris,
 * and/or Eggroll. Hope you guys don't mind that I'm using it, but hey,
 * why reinvent the wheel, right?
 */

package com.bukkit.jjfs85.BetterShop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

public class itemDb {
	private final static Logger logger = Logger.getLogger("Minecraft");
	private static Map<String, Integer> map = new HashMap<String, Integer>();

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
				} catch (Exception ex) {
					logger.warning("Error parsing " + fname + " on line " + i);
				}
			}
		} finally {
			rx.close();
		}
	}

	public static int get(String id) throws Exception {
		int retval = getUnsafe(id);
		if (map.containsValue(retval))
			return retval;
		throw new Exception("Unknown item numeric: " + retval);
	}

	private static int getUnsafe(String id) throws Exception {
		try {
			return Integer.parseInt(id);
		} catch (NumberFormatException ex) {
			if (map.containsKey(id))
				return map.get(id);
			throw new Exception("Unknown item name: " + id);
		}
	}
}