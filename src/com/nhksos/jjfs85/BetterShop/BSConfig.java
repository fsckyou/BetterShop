/**
 * 
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.CheckInput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

/**
 * @author jjfs85
 * 
 */
public class BSConfig {

    private final HashMap<String, String> stringMap = new HashMap<String, String>();
    public final static String configname = "config.yml";
    public final static File pluginFolder = new File("plugins", BetterShop.name);
    public final static File configfile = new File(pluginFolder, configname);
    public String currency = "Coin";
    private boolean config_useMySQL = false;
    public String sql_username = "root", sql_password = "root", sql_database = "minecraft", sql_tableName = "BetterShop", sql_hostName = "localhost", sql_portNum = "3306";
    public boolean logUserTransactions = false, logTotalTransactions = false;
    public BigInteger userTansactionLifespan = new BigInteger("2880"); // 2 days.. i'd like to use unsigned long, but java doesn't have it..
    public String transLogTablename = "BetterShopMarketActivity", recordTablename = "BetterShopTransactionTotals";
    public int pagesize = 9;

    public BSConfig() {
        create();
        load();
    }

    public final boolean load() {
        Configuration config = new Configuration(new File(pluginFolder, configname));
        config.load();
        ConfigurationNode n;
        pagesize = config.getInt("ItemsPerPage", pagesize);
        config_useMySQL = config.getBoolean("useMySQLPricelist", false);
        if (config_useMySQL) {
            n = config.getNode("MySQL");
            if (n != null) {
                sql_username = n.getString("username", sql_username);
                sql_password = n.getString("password", sql_password);
                sql_database = n.getString("database", sql_database).replace(" ", "_");
                sql_tableName = n.getString("tablename", sql_tableName);
                sql_hostName = n.getString("Hostname", sql_hostName);
                sql_portNum = n.getString("Port", sql_portNum);
            } else {
                BetterShop.Log(Level.WARNING, "MySQL section in " + configname + " is missing");
            }
        }
        n = config.getNode("transactionLog");
        if (n != null) {
            logUserTransactions = n.getBoolean("enabled", logUserTransactions);
            transLogTablename = n.getString("logtablename", transLogTablename).replace(" ", "_");
            String lifespan = n.getString("userTansactionLifespan");
            if (lifespan != null) {
                int charPos = 0;
                for (; charPos < lifespan.length() - 1; ++charPos) {
                    if (!Character.isDigit(lifespan.charAt(charPos))) {
                        break;
                    }
                }
                boolean good = false;
                if (charPos > 0) {
                    // double-check value
                    if (CheckInput.IsInt(lifespan.substring(0, charPos))) {
                        good = true; // assume good until proven otherwise :)
                        userTansactionLifespan = new BigInteger(lifespan.substring(0, charPos));
                        if (charPos == lifespan.length() - 1 || lifespan.charAt(charPos) == 'h') {
                            // it's annoying that this class doesn't accept a long..
                            userTansactionLifespan.multiply(new BigInteger("60"));
                        } else if (lifespan.charAt(charPos) == 'd') {
                            userTansactionLifespan.multiply(new BigInteger("1440"));
                        } else if (lifespan.charAt(charPos) == 'w') {
                            userTansactionLifespan.multiply(new BigInteger("10080"));
                        } else if (lifespan.charAt(charPos) == 'M') {
                            // using 1m = 30 days
                            userTansactionLifespan.multiply(new BigInteger("302400"));
                        } else if (lifespan.charAt(charPos) == 'm') {
                            // already number that need
                        } else {
                            good = false;
                        }
                    }
                }
                if (!good) {
                    BetterShop.Log(Level.WARNING, "userTansactionLifespan has an illegal value");
                }
            }
        }

        n = config.getNode("totalsTransactionLog");
        if (n != null) {
            logTotalTransactions = n.getBoolean("enabled", logTotalTransactions);
            recordTablename = n.getString("logtablename", recordTablename).replace(" ", "_");
        }

        n = config.getNode("strings");
        if (n != null) {
            for (Object k : config.getKeys("strings").toArray()) {
                if (k instanceof String) {
                    String tmpString = n.getString(k.toString());
                    // double-check that recieved a value
                    if (tmpString != null) {
                        tmpString = tmpString.replace("&", "\u00A7");
                        // put the string in a HashMap for retrieval later
                        stringMap.put(k.toString(), tmpString);
                    }
                }
            }
        } else {
            BetterShop.Log(Level.SEVERE, String.format("strings section missing from configuration file %s", configname));
        }
        return true;
    }

    public boolean useMySQL() {
        return config_useMySQL;//(getString("useMySQL").equalsIgnoreCase("true"));
    }

    public String getString(String key) {
        String ret = stringMap.get(key);
        if (ret == null) {
            BetterShop.Log(Level.WARNING, String.format("%s missing from configuration file", key));
            ret = "(Error: Message is Missing)";
        }
        return ret;
    }

    private void create() {
        pluginFolder.mkdirs();
        if (!configfile.exists()) {
            try {
                BetterShop.Log(Level.WARNING, configname + " not found. Creating new file.");
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
                BetterShop.Log(Level.SEVERE, "Failed creating new config file ", ex);
            }
        }
        //else logger.log(Level.INFO, configname + " found!");
    }
}
