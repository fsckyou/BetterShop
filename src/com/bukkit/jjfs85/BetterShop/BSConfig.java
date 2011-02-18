/**
 * 
 */
package com.bukkit.jjfs85.BetterShop;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.util.config.Configuration;

/**
 * @author jjfs85
 * 
 */
public class BSConfig {
	private final static Logger logger = Logger.getLogger("Minecraft");
	private final HashMap<String, String> stringMap = new HashMap<String, String>();
	private File folder = new File("plugins", "BetterShop");
	private String configname = "config.yml";
	private final File configfile = new File(folder, configname);
	Configuration config;

	public BSConfig(File folder, String configname) {
		this.configname = configname;
		this.folder = folder;
		try {
			create();
		} catch (Exception e) {
			e.printStackTrace();
		}
		load();
		logger.info("Debug - Read " + stringMap.size());
	}

	public void load() {
		List<String> keyList = new LinkedList<String>();
		config = new Configuration(new File(folder, configname));
		config.load();
		keyList.addAll(config.getKeys("strings"));
		String tmpString;

		for (int i = 0; i < keyList.size(); i++) {
			// get the string
			tmpString = config.getString("strings." + keyList.get(i));
			while (tmpString.contains("&")) {
				tmpString = tmpString.replace("&", "\u00A7");
			}
			logger.warning("Debug - Here's " + keyList.get(i) + " " + tmpString);
			// put the string in a HashMap for retrieval later
			stringMap.put(keyList.get(i), tmpString);
		}
	}

	public String getString(String key) {

		return stringMap.get(key);
	}

	private void create() throws Exception {
		folder.mkdirs();
		if (!configfile.exists()) {
			logger.warning("config.yml not found. Creating new file.");
			configfile.createNewFile();
			InputStream res = itemDb.class.getResourceAsStream("/config.yml");
			FileWriter tx = new FileWriter(configfile);
			try {
				for (int i = 0; (i = res.read()) > 0;)
					tx.write(i);
			} finally {
				tx.flush();
				tx.close();
				res.close();
			}
		} else
			logger.info("config.yml found!");
	}

}
