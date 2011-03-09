/**
 * 
 */
package com.nhksos.jjfs85.BetterShop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

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
    private boolean config_useMySQL = false;
    public String sql_username = "root", sql_password = "root", sql_database = "minecraft", sql_tableName = "BetterShop", sql_hostName = "localhost", sql_portNum = "3306";
    public int pagesize = 9;

    public BSConfig() {
        create();
        load();
    }

    public BSConfig(File folder, String configname) {
        this.configname = configname;
        this.folder = folder;
        create();
        load();
    }

    public final void load() {
        List<String> keyList = new LinkedList<String>();
        config = new Configuration(new File(folder, configname));
        config.load();

        config_useMySQL = config.getBoolean("useMySQLPricelist", false);
        if (config_useMySQL) {
            ConfigurationNode n = config.getNode("MySQL");
            sql_username = n.getString("username", sql_username);
            sql_password = n.getString("password", sql_password);
            sql_database = n.getString("database", sql_database);
            sql_tableName = n.getString("tablename", sql_tableName);
            sql_hostName = n.getString("Hostname", sql_hostName);
            sql_portNum = n.getString("Port", sql_portNum);
        }
        pagesize = config.getInt("ItemsPerPage", pagesize);

        keyList.addAll(config.getKeys("strings"));
        String tmpString;

        for (int i = 0; i < keyList.size(); i++) {
            // get the string
            tmpString = config.getString("strings." + keyList.get(i));
            if (tmpString != null) {
                while (tmpString.contains("&")) {
                    tmpString = tmpString.replace("&", "\u00A7");
                }
                // put the string in a HashMap for retrieval later
                stringMap.put(keyList.get(i), tmpString);
            }
        }
    }

    public boolean useMySQL() {
        return config_useMySQL;//(getString("useMySQL").equalsIgnoreCase("true"));
    }

    public String getString(String key) {
        return stringMap.get(key);
    }

    private void create() {
        folder.mkdirs();
        if (!configfile.exists()) {
            try {
                logger.log(Level.WARNING, configname + " not found. Creating new file.");
                configfile.createNewFile();
                InputStream res = itemDb.class.getResourceAsStream("/config.yml");
                FileWriter tx = new FileWriter(configfile);
                try {
                    for (int i = 0; (i = res.read()) > 0;) {
                        tx.write(i);
                    }
                } finally {
                    tx.flush();
                    tx.close();
                    res.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(BSConfig.class.getName()).log(Level.SEVERE, "Failed creating new config file ", ex);
            }
        }
        //else logger.log(Level.INFO, configname + " found!");
    }
}
