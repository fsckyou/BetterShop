/**
 * 
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.CheckInput;
import com.jascotty2.Item.Item;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

/**
 * @author jjfs85
 * 
 */
public class BSConfig {
    
    // chat messages
    private final HashMap<String, String> stringMap = new HashMap<String, String>();
    public String currency = "Coin";
    public int pagesize = 9;
    public boolean publicmarket = false;
    // files used by plugin
    public final static String configname = "config.yml";
    public final static File pluginFolder = new File("plugins", BetterShop.name);
    public final static File configfile = new File(pluginFolder, configname);
    // database information
    public String tableName = "BetterShop";
    public String sql_username = "root", sql_password = "root", sql_database = "minecraft", sql_hostName = "localhost", sql_portNum = "3306";
    private DBType databaseType = DBType.FLATFILE;
    // database caching (MySQL only)
    public BigInteger priceListLifespan = new BigInteger("0"); // 0 = never update
    public int tempCacheTTL = 10; //how long before tempCache is considered outdated (seconds)
    // logging settings
    public boolean logUserTransactions = false, logTotalTransactions = false;
    public BigInteger userTansactionLifespan = new BigInteger("172800"); // 2 days.. i'd like to use unsigned long, but java doesn't have it..
    public String transLogTablename = "BetterShopMarketActivity", recordTablename = "BetterShopTransactionTotals";
    // if pricelist is to have a custom sort order, what should be at the top
    public ArrayList<String> sortOrder = new ArrayList<String>();
    public enum DBType {

        MYSQL, SQLITE, FLATFILE
    }

    public BSConfig() {
        create();
        load();
    }

    public final boolean load() {
        Configuration config = new Configuration(configfile);
        config.load();
        ConfigurationNode n;
        pagesize = config.getInt("ItemsPerPage", pagesize);
        publicmarket = config.getBoolean("publicmarket", publicmarket);
        tableName = config.getString("tablename", tableName);
        databaseType = config.getBoolean("useMySQLPricelist", false) ? DBType.MYSQL : DBType.FLATFILE;
        if (databaseType == DBType.MYSQL) {
            n = config.getNode("MySQL");
            if (n != null) {
                sql_username = n.getString("username", sql_username);
                sql_password = n.getString("password", sql_password);
                sql_database = n.getString("database", sql_database).replace(" ", "_");
                sql_hostName = n.getString("Hostname", sql_hostName);
                sql_portNum = n.getString("Port", sql_portNum);
                String lifespan = n.getString("tempCacheTTL");
                if (lifespan != null) {
                    tempCacheTTL = CheckInput.GetInt(lifespan, tempCacheTTL);
                    /*try {
                        tempCacheTTL = CheckInput.GetBigInt_TimeSpanInSec(lifespan).intValue();
                    } catch (Exception ex) {
                        BetterShop.Log(Level.WARNING, "tempCacheTTL has an illegal value", ex);
                        //BetterShop.Log(Level.WARNING, ex);
                    }//*/
                }
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
                try {
                    userTansactionLifespan = CheckInput.GetBigInt_TimeSpanInSec(lifespan);
                } catch (Exception ex) {
                    BetterShop.Log(Level.WARNING, "userTansactionLifespan has an illegal value", ex);
                }
            }
        } else {
            BetterShop.Log(Level.WARNING, "transactionLog section in config not found");
        }

        n = config.getNode("totalsTransactionLog");
        if (n != null) {
            logTotalTransactions = n.getBoolean("enabled", logTotalTransactions);
            recordTablename = n.getString("logtablename", recordTablename).replace(" ", "_");
        } else {
            BetterShop.Log(Level.WARNING, "totalsTransactionLog section in config not found");
        }

        n = config.getNode("strings");
        if (n != null) {
            stringMap.clear();
            for (Object k : config.getKeys("strings").toArray()) {
                if (k instanceof String) {
                    String tmpString = n.getString(k.toString());
                    // double-check that recieved a value
                    if (tmpString != null) {
                        tmpString = tmpString.replace("&", "\u00A7").replace("%", "%%"); // tmpString.replaceAll("&.", "\u00A7").replace("%", "%%");// 
                        // put the string in a HashMap for retrieval later
                        stringMap.put(k.toString(), tmpString);
                    }
                }
            }
        } else {
            BetterShop.Log(Level.SEVERE, String.format("strings section missing from configuration file %s", configname));
        }
        String customsort = config.getString("customsort");
        if(customsort!=null){
            // parse for items && add to custom sort arraylist
            String items[] = customsort.split(",");
            for(String i : items){
                Item toAdd = Item.findItem(i.trim());
                if(toAdd!=null){
                    sortOrder.add(toAdd.IdDatStr());
                }else{
                    BetterShop.Log("Invalid Item in customsort configuration: " + i);
                }
            }
        }
        
        return true;
    }

    public boolean useMySQL() {
        return databaseType == DBType.MYSQL;
    }

    public boolean useFlatfile() {
        return databaseType == DBType.FLATFILE;
    }

    public String getString(String key) {
        String ret = stringMap.get(key);
        if (ret == null) {
            BetterShop.Log(Level.WARNING, String.format("%s missing from configuration file", key));
            ret = "(Error: Message is Missing)";
        }
        return ret;
    }
    
    public boolean hasString(String key) {
        String ret = stringMap.get(key);
        if (ret == null) {
            BetterShop.Log(Level.WARNING, String.format("%s missing from configuration file", key));
            return false;
        }
        return true;
    }

    private void create() {
        pluginFolder.mkdirs();
        if (!configfile.exists()) {
            try {
                BetterShop.Log(Level.WARNING, configname + " not found. Creating new file.");
                configfile.createNewFile();
                InputStream res = BetterShop.class.getResourceAsStream("/config.yml");
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
