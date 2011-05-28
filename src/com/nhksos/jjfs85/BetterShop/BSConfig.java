package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.CheckInput;
import com.jascotty2.Item.Item;
import com.jascotty2.Item.ItemDB;
import com.jascotty2.MinecraftChatStr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

public class BSConfig {

    // chat messages
    private final HashMap<String, String> stringMap = new HashMap<String, String>();
    // files used by plugin
    public final static String configname = "config.yml";
    public final static File pluginFolder = new File("plugins", BetterShop.name);
    public final static File configfile = new File(pluginFolder, configname);
    public final static File signDBFile = new File(pluginFolder, "signs.dat");
    ///// plugin settings
    public boolean checkUpdates = true,
            sendErrorReports = true,
            unMaskErrorID = false,
            autoUpdate = false;
    public boolean sendLogOnError = true, sendAllLog = false;
    public boolean hideHelp = false;
    public static final int MAX_CUSTMSG_LEN = 90;
    ////// shop settings
    public String defaultCurrency = "Coin", pluralCurrency = "Coins";
    public int pagesize = 9;
    public boolean publicmarket = false;
    public String defColor = "white",
            BOSBank = "";
    protected String customErrorMessage = "";
    ///// item buying behavior
    public boolean allowbuyillegal = true, //if someone without BetterShop.admin.illegal can buy illegal items
            usemaxstack = true, //whether maxstack should be honored
            buybacktools = true, //used tools can be bought back?
            buybackenabled = true, //shop buys items from users?
            signShopEnabled = true,
            signItemColor = false, //color the names of items on signs?
            signItemColorBWswap = false,// swap black & white item colors?
            signDestroyProtection = true,
            tntSignDestroyProtection = false;
    public int maxEntityPurchase = 3; // max can purchase at a time
    // sign settings
    public String activeSignColor = "blue"; // automatically changed to \u00A7 format
    public long signDBsaveWait = 30000; // don't save immediately, wait (30s)
    public static long signInteractWait = 1000; // wait before another action allowed
    // global or region shops
    public CommShopMode commandShopMode = CommShopMode.GLOBAL;
    ////// database information
    public String tableName = "BetterShop";
    public String sql_username = "root", sql_password = "root", sql_database = "minecraft", sql_hostName = "localhost", sql_portNum = "3306";
    private DBType databaseType = DBType.FLATFILE;
    /// database caching (MySQL only)
    public boolean useDBCache = true;
    //public BigInteger priceListLifespan = new BigInteger("0"); // 0 = never update
    public long priceListLifespan = 0; // 0 = never update
    public int tempCacheTTL = 10; //how long before tempCache is considered outdated (seconds)
    ////// logging settings
    public boolean logUserTransactions = false, logTotalTransactions = false;
    //public BigInteger userTansactionLifespan = new BigInteger("172800"); // 2 days.. i'd like to use unsigned long, but java doesn't have it..
    public long userTansactionLifespan = 172800;
    public String transLogTablename = "BetterShopMarketActivity", recordTablename = "BetterShopTransactionTotals";
    // if pricelist is to have a custom sort order, what should be at the top
    public ArrayList<String> sortOrder = new ArrayList<String>();
    // dynamic pricing options
    public boolean useDynamicPricing = false;
    //if a price is not set on a craftable item, can still sell if the materials to make are for sale
    public boolean sellcraftables = true;
    // if sellcraftables, how much % more a crafted item costs than the materials
    public double sellcraftableMarkup = .05;
    // if sellcraftables, if colored wool should sell for less than sellcraftableMarkup
    //   (otherwise, a player could buy dye, color a sheep, get 1-3 colored wool, make profit)
    public boolean woolsellweight = true;
    // stock items
    public boolean useItemStock = false;
    // table/file name to use
    public String stockTablename = "BetterShopItemStock";
    //  how much an added item has to start with
    public long startStock = 200;
    // max stock to carry (stock is increased with sales)
    public long maxStock = 500;
    // deny sales if stock is full?
    public boolean noOverStock = true;
    // restock interval.. automatic, and stock will be reset to startStock value
    public long restock = 21600; //6h

    public enum DBType {

        MYSQL, SQLITE, FLATFILE
    }

    public enum CommShopMode {

        GLOBAL, REGIONS, BOTH, NONE
    }

    public BSConfig() {
        create();
        load();
    }

    public final boolean load() {
        // in case load errors out, ensure strings are present

        stringMap.clear();
        // default strings
        // # general messages
        stringMap.put("prefix", "&fSHOP: &2");
        stringMap.put("permdeny", "OI! You don't have permission to do that!");
        stringMap.put("unkitem", "What is &f<item>&2?");
        stringMap.put("nicetry", "...Nice try!");
        // # shopadd messages
        stringMap.put("paramerror", "Oops... something wasn't right there.");
        stringMap.put("addmsg", "&f[<item>&f]&2 added to the shop. Buy: &f<buyprice>&2 Sell: &f<sellprice>");
        stringMap.put("chgmsg", "&f[<item>&f]&2 updated. Buy: &f<buyprice>&2 Sell: &f<sellprice>");
        // # shopremove messages
        stringMap.put("removemsg", "&f[<item>&f]&2 removed from the shop");
        // # shopcheck messages
        stringMap.put("pricecheck", "Price check! &f[<item>&f]&2 Buy: &f<buyprice> &2Sell: &f<sellprice>" + (useItemStock ? " (stock: <avail>)" : ""));
        stringMap.put("multipricecheck", "Price check! <amt> &f[<item>&f]&2 Buy: &f<buyprice> &2Sell: &f<sellprice>");
        stringMap.put("multipricechecksell", "Price check! <amt> &f[<item>&f]&2 Sell: &f<sellcur>");
        stringMap.put("multipricecheckbuy", "Price check! <amt> &f[<item>&f]&2 Buy: &f<buycur>");
        stringMap.put("nolisting", "&f[<item>&f] &2cannot be bought or sold.");
        // # shoplist messages
        stringMap.put("listhead", "-------- Price-List Page: &f<page> &2of &f<pages> &2--------");
        stringMap.put("listing", "&f[<item>&f]&2 <l> Buy: &f<buyprice>&2  Sell: &f<sellprice>" + (useItemStock ? " <l>(stock: <avail>)" : ""));
        stringMap.put("listtail", "-----------------------------------------");
        // # shopbuy messages
        stringMap.put("buymsg", "Buying &f<amt> &2<item>&2 at &f<priceper> &2<curr> each... &f<total> &2<curr> total!");
        stringMap.put("publicbuymsg", "<player> bought &f<amt> &2<item>&2 for &f<totcur>");
        stringMap.put("outofroom", "You tried to buy &f<leftover>&2 too many.. you can only hold <free> <item>&2 more");
        stringMap.put("insuffunds", "&4You don't have enough <curr>! &f<amt> &2<item> at &f<priceper> &2<curr> each = &c<total>");
        stringMap.put("notforsale", "&f[<item>&f] &2cannot be bought");
        stringMap.put("illegalbuy", "&4you don't have permissions to buy &f[<item>&f]");
        // # shopsell messages
        stringMap.put("donthave", "You only have <hasamt>, not <amt>");
        stringMap.put("donotwant", "&f[<item>&f] &2has no value to me. No thanks.");
        stringMap.put("sellmsg", "Selling &f<amt> &2<item>&2 at &f<priceper> &2<curr> each... &f<total> &2<curr> total!");
        stringMap.put("publicsellmsg", "<player> sold &f<amt> &2<item>&2 for &f<totcur>");
        // # stock messages
        stringMap.put("outofstock", "This item is currently out of stock");
        stringMap.put("lowstock", "Only <amt> avaliable for purchase");
        stringMap.put("maxstock", "This item is currently at max stock");
        stringMap.put("highstock", "Only <amt> can be sold back");

        try {
            Configuration config = new Configuration(configfile);
            config.load();
            ConfigurationNode n;

            // check for completedness
            try {
                HashMap<String, String[]> allKeys = new HashMap<String, String[]>();

                allKeys.put("shop", new String[]{
                            "ItemsPerPage",
                            "publicmarket",
                            "allowbuyillegal",
                            "usemaxstack",
                            "buybacktools",
                            "buybackenabled",
                            "maxEntityPurchase",
                            "signShops",
                            "activeSignColor",
                            "signItemColor",
                            "signItemColorBWswap",
                            "signDestroyProtection",
                            "tntSignDestroyProtection",
                            "commandShop",
                            "customsort",
                            "defaultItemColor",
                            "tablename",
                            "hideHelp",
                            "BOSBank",
                            "currencyName"});
                allKeys.put("errors", new String[]{
                            "CheckForUpdates",
                            "AutoUpdate",
                            "AutoErrorReporting",
                            "UnMaskErrorID",
                            "CustomErrorMessage",
                            "sendLogOnError",
                            "sendAllLog"});
                allKeys.put("MySQL", new String[]{
                            "useMySQL",
                            "database",
                            "username",
                            "password",
                            "Hostname",
                            "Port",
                            "cache",
                            "cacheUpdate",
                            "tempCacheTTL"});
                allKeys.put("transactionLog", new String[]{
                            "enabled",
                            "logtablename",
                            "userTansactionLifespan"});
                allKeys.put("totalsTransactionLog", new String[]{
                            "enabled",
                            "logtablename"});
                allKeys.put("dynamicMarket", new String[]{
                            "dynamicPricing",
                            "sellcraftables",
                            "sellcraftableMarkup",
                            "woolsellweight"});
                allKeys.put("itemStock", new String[]{
                            "enabled",
                            "tablename",
                            "startStock",
                            "maxStock",
                            "noOverStock",
                            "restock"});
                allKeys.put("strings", new String[]{
                            "prefix",
                            "permdeny",
                            "unkitem",
                            "nicetry",
                            "paramerror",
                            "addmsg",
                            "chgmsg",
                            "removemsg",
                            "pricecheck",
                            "multipricecheck",
                            "multipricechecksell",
                            "multipricecheckbuy",
                            "nolisting",
                            "listhead",
                            "listing",
                            "listtail",
                            "buymsg",
                            "publicbuymsg",
                            "outofroom",
                            "insuffunds",
                            "notforsale",
                            "illegalbuy",
                            "donthave",
                            "donotwant",
                            "sellmsg",
                            "publicsellmsg",
                            "outofstock",
                            "lowstock",
                            "maxstock",
                            "highstock"});
                String allowNull[] = new String[]{"shop.customsort", "shop.BOSBank", "shop.currencyName"};

                String missing = "", unused = "";
                for (String k : allKeys.keySet()) {
                    if (k == null) {
                        //k = "";
                        continue;
                    } else if (config.getKeys(k) == null) {
                        if (missing.length() > 0) {
                            missing += ", " + k + ".*";
                        } else {
                            missing += k + ".*";
                        }
                        continue;
                    }
                    String key = "";
                    if (k.length() > 0) {
                        key = k + ".";
                        for (String val : config.getKeys(k)) {
                            if (indexOf(allKeys.get(k), val) < 0) {
                                if (unused.length() > 0) {
                                    unused += ", " + key + val;
                                } else {
                                    unused += key + val;
                                }

                            }
                        }
                    }
                    for (String val : allKeys.get(k)) {
                        if (config.getProperty(key + val) == null && indexOf(allowNull, key + val) < 0) {
                            //missing.add(key+val);
                            if (missing.length() > 0) {
                                missing += ", " + key + val;
                            } else {
                                missing += key + val;
                            }
                        }
                    }
                }
                if (unused.length() > 0) {
                    BetterShop.Log("Notice: Unused Configuration Nodes: \n" + unused);
                }
                if (missing.length() > 0) {
                    BetterShop.Log(Level.WARNING, "Missing Configuration Nodes: \n" + missing);
                }
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, "Unexpected Error during config integrety check", ex, false);
                // this shouldn't be happening: send error report
                if (config.getBoolean("AutoErrorReporting", sendErrorReports)) {
                    BetterShop.sendErrorReport("Unexpected Error during config integrety check", ex);
                }
            }

            // supporting these older nodes for now
            boolean usingOldMySQLsetting = false;
            if (configHasNode(config, new String[]{"CheckForUpdates", "AutoUpdate",
                        "AutoErrorReporting", "UnMaskErrorID", "CustomErrorMessage",
                        "ItemsPerPage", "publicmarket", "allowbuyillegal", "usemaxstack",
                        "buybacktools", "buybackenabled", "maxEntityPurchase",
                        "tablename", "useMySQL", "useMySQLPricelist", "defaultItemColor",
                        "sendLogOnError", "sendAllLog", "hideHelp", "customsort"})) {
                BetterShop.Log(Level.WARNING, "Using Deprecated Configuration Nodes: Update To New Format Soon!");

                checkUpdates = config.getBoolean("CheckForUpdates", checkUpdates);
                autoUpdate = config.getBoolean("AutoUpdate", autoUpdate);
                sendErrorReports = config.getBoolean("AutoErrorReporting", sendErrorReports);
                unMaskErrorID = config.getBoolean("UnMaskErrorID", unMaskErrorID);
                customErrorMessage = config.getString("CustomErrorMessage", customErrorMessage).trim();
                if (customErrorMessage.length() > MAX_CUSTMSG_LEN) {
                    BetterShop.Log("Notice: CustomErrorMessage is too long. (will be truncated)");
                    customErrorMessage = customErrorMessage.substring(0, MAX_CUSTMSG_LEN);
                }

                pagesize = config.getInt("ItemsPerPage", pagesize);
                publicmarket = config.getBoolean("publicmarket", publicmarket);

                allowbuyillegal = config.getBoolean("allowbuyillegal", allowbuyillegal);
                usemaxstack = config.getBoolean("usemaxstack", usemaxstack);
                buybacktools = config.getBoolean("buybacktools", buybacktools);
                buybackenabled = config.getBoolean("buybackenabled", buybackenabled);
                maxEntityPurchase = config.getInt("maxEntityPurchase", maxEntityPurchase);

                tableName = config.getString("tablename", tableName);

                if (config.getProperty("useMySQL") != null || config.getProperty("useMySQLPricelist") != null) {
                    usingOldMySQLsetting = true;
                    databaseType = config.getBoolean("useMySQL", config.getBoolean("useMySQLPricelist", false)) ? DBType.MYSQL : DBType.FLATFILE;
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
                            }
                            useDBCache = n.getBoolean("cache", useDBCache);
                            lifespan = n.getString("cacheUpdate");
                            if (lifespan != null) {
                                try {
                                    priceListLifespan = CheckInput.GetBigInt_TimeSpanInSec(lifespan, 'h').longValue();
                                } catch (Exception ex) {
                                    BetterShop.Log(Level.WARNING, "cacheUpdate has an illegal value");
                                    BetterShop.Log(Level.WARNING, ex);
                                }
                            }
                        } else {
                            BetterShop.Log(Level.WARNING, "MySQL section in " + configname + " is missing");
                        }
                    }
                }

                defColor = config.getString("defaultItemColor", defColor);
                ItemDB.setDefaultColor(defColor);

                sendLogOnError = config.getBoolean("sendLogOnError", sendLogOnError);
                sendAllLog = config.getBoolean("sendAllLog", sendAllLog);

                hideHelp = config.getBoolean("hideHelp", hideHelp);
                BOSBank = config.getString("BOSBank", "");

                String customsort = config.getString("customsort");
                if (customsort != null) {
                    // parse for items && add to custom sort arraylist
                    String items[] = customsort.split(",");
                    for (String i : items) {
                        Item toAdd = Item.findItem(i.trim());
                        if (toAdd != null) {
                            sortOrder.add(toAdd.IdDatStr());
                        } else {
                            BetterShop.Log("Invalid Item in customsort configuration: " + i);
                        }
                    }
                }
            } // end depricated settings block

            if ((n = config.getNode("errors")) != null) {
                checkUpdates = n.getBoolean("CheckForUpdates", checkUpdates);
                autoUpdate = n.getBoolean("AutoUpdate", autoUpdate);
                sendErrorReports = n.getBoolean("AutoErrorReporting", sendErrorReports);
                unMaskErrorID = n.getBoolean("UnMaskErrorID", unMaskErrorID);
                customErrorMessage = n.getString("CustomErrorMessage", customErrorMessage).trim();
                if (customErrorMessage.length() > MAX_CUSTMSG_LEN) {
                    BetterShop.Log("Notice: CustomErrorMessage is too long. (will be truncated)");
                    customErrorMessage = customErrorMessage.substring(0, MAX_CUSTMSG_LEN);
                }
                sendLogOnError = n.getBoolean("sendLogOnError", sendLogOnError);
                sendAllLog = n.getBoolean("sendAllLog", sendAllLog);
            }

            if ((n = config.getNode("shop")) != null) {
                pagesize = n.getInt("ItemsPerPage", pagesize);
                publicmarket = n.getBoolean("publicmarket", publicmarket);

                allowbuyillegal = n.getBoolean("allowbuyillegal", allowbuyillegal);
                usemaxstack = n.getBoolean("usemaxstack", usemaxstack);
                buybacktools = n.getBoolean("buybacktools", buybacktools);
                buybackenabled = n.getBoolean("buybackenabled", buybackenabled);
                maxEntityPurchase = n.getInt("maxEntityPurchase", maxEntityPurchase);

                signShopEnabled = n.getBoolean("signShops", signShopEnabled);
                activeSignColor = n.getString("activeSignColor", activeSignColor);
                signDestroyProtection = n.getBoolean("signDestroyProtection", signDestroyProtection);
                tntSignDestroyProtection = n.getBoolean("tntSignDestroyProtection", tntSignDestroyProtection);

                signItemColor = n.getBoolean("signItemColor", signItemColor);
                signItemColorBWswap = n.getBoolean("signItemColorBWswap", signItemColorBWswap);

                String cShopMode = n.getString("commandShop");
                if (cShopMode != null) {
                    if (cShopMode.equalsIgnoreCase("disabled") || cShopMode.equalsIgnoreCase("none")) {
                        commandShopMode = CommShopMode.NONE;
                    }/*else if(cShopMode.equalsIgnoreCase("regions")){
                    commandShopMode = CommShopMode.REGIONS;
                    }else if(cShopMode.equalsIgnoreCase("both")){
                    commandShopMode = CommShopMode.BOTH;
                    }*/ else {
                        commandShopMode = CommShopMode.GLOBAL;
                    }
                }

                tableName = n.getString("tablename", tableName);

                defColor = n.getString("defaultItemColor", defColor);
                ItemDB.setDefaultColor(defColor);

                //ItemCurrency.loadFromString(n.getString("currencyItems", "diamond>20, goldbar>5, ironbar>1, redstone>.5"));
                defaultCurrency = n.getString("currencyName", "Coin");
                pluralCurrency = n.getString("currencyNamePlural", "Coins");

                String customsort = n.getString("customsort");
                if (customsort != null) {
                    // parse for items && add to custom sort arraylist
                    String items[] = customsort.split(",");
                    for (String i : items) {
                        Item toAdd = Item.findItem(i.trim());
                        if (toAdd != null) {
                            sortOrder.add(toAdd.IdDatStr());
                        } else {
                            BetterShop.Log("Invalid Item in customsort configuration: " + i);
                        }
                    }
                }
                hideHelp = n.getBoolean("hideHelp", hideHelp);

            }
            activeSignColor = MinecraftChatStr.getChatColor(activeSignColor);

            if (!usingOldMySQLsetting && (n = config.getNode("MySQL")) != null) {
                databaseType = n.getBoolean("useMySQL", config.getBoolean("useMySQLPricelist", false)) ? DBType.MYSQL : DBType.FLATFILE;
                if (databaseType == DBType.MYSQL) {
                    sql_username = n.getString("username", sql_username);
                    sql_password = n.getString("password", sql_password);
                    sql_database = n.getString("database", sql_database).replace(" ", "_");
                    sql_hostName = n.getString("Hostname", sql_hostName);
                    sql_portNum = n.getString("Port", sql_portNum);
                    String lifespan = n.getString("tempCacheTTL");
                    if (lifespan != null) {
                        tempCacheTTL = CheckInput.GetInt(lifespan, tempCacheTTL);
                    }
                    useDBCache = n.getBoolean("cache", useDBCache);
                    lifespan = n.getString("cacheUpdate");
                    if (lifespan != null) {
                        try {
                            priceListLifespan = CheckInput.GetBigInt_TimeSpanInSec(lifespan, 'h').longValue();
                        } catch (Exception ex) {
                            BetterShop.Log(Level.WARNING, "cacheUpdate has an illegal value");
                            BetterShop.Log(Level.WARNING, ex);
                        }
                    }
                }
            }

            if ((n = config.getNode("transactionLog")) != null) {
                logUserTransactions = n.getBoolean("enabled", logUserTransactions);
                transLogTablename = n.getString("logtablename", transLogTablename).replace(" ", "_");
                String lifespan = n.getString("userTansactionLifespan");
                if (lifespan != null) {
                    try {
                        userTansactionLifespan = CheckInput.GetBigInt_TimeSpanInSec(lifespan).longValue();
                    } catch (Exception ex) {
                        BetterShop.Log(Level.WARNING, "userTansactionLifespan has an illegal value", ex);
                    }
                }
            }

            if ((n = config.getNode("totalsTransactionLog")) != null) {
                logTotalTransactions = n.getBoolean("enabled", logTotalTransactions);
                recordTablename = n.getString("logtablename", recordTablename).replace(" ", "_");
            }

            if ((n = config.getNode("dynamicMarket")) != null) {
                useDynamicPricing = n.getBoolean("dynamicPricing", n.getBoolean("enabled", useDynamicPricing));
                sellcraftables = n.getBoolean("sellcraftables", sellcraftables);
                sellcraftableMarkup = n.getDouble("sellcraftableMarkup", sellcraftableMarkup / 100) * 100;
                woolsellweight = n.getBoolean("woolsellweight", woolsellweight);
            }

            if ((n = config.getNode("itemStock")) != null) {
                useItemStock = n.getBoolean("enabled", useItemStock);
                stockTablename = n.getString("stockTablename", stockTablename);
                noOverStock = n.getBoolean("noOverStock", noOverStock);
                String num = n.getString("startStock");
                if (num != null) {
                    startStock = CheckInput.GetLong(num, startStock);//CheckInput.GetBigInt(num, startStock).longValue();
                }
                num = n.getString("maxStock");
                if (num != null) {
                    maxStock = CheckInput.GetLong(num, maxStock);//CheckInput.GetBigInt(num, maxStock).longValue();
                }
                num = n.getString("restock");
                if (num != null) {
                    try {
                        restock = CheckInput.GetBigInt_TimeSpanInSec(num, 'h').longValue();
                    } catch (Exception ex) {
                        BetterShop.Log(Level.WARNING, "restock has an illegal value", ex);
                    }

                }
            }
            if ((n = config.getNode("strings")) != null) {
                for (String k : config.getKeys("strings")) {
                    if (stringMap.containsKey(k)) {
                        stringMap.put(k, n.getString(k, stringMap.get(k)));
                    }
                }
            } else {
                BetterShop.Log(Level.SEVERE, String.format("strings section missing from configuration file %s", configname));
            }
            for (String k : stringMap.keySet()) {
                stringMap.put(k, stringMap.get(k).replace("&", "\u00A7").replace("%", "%%"));
            }

        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, "Error parsing configuration file", ex, false);
            return false;
        }
        return true;
    }

    public boolean useMySQL() {
        return databaseType == DBType.MYSQL;
    }

    public boolean useFlatfile() {
        return databaseType == DBType.FLATFILE;
    }

    public boolean useGlobalCommandShop() {
        return commandShopMode == CommShopMode.GLOBAL || commandShopMode == CommShopMode.BOTH;
    }

    public boolean useRegionCommandShop() {
        return commandShopMode == CommShopMode.REGIONS || commandShopMode == CommShopMode.BOTH;
    }

    public String getString(String key) {
        String ret = stringMap.get(key);
        if (ret == null) {
            BetterShop.Log(Level.WARNING, String.format("%s missing from configuration file", key));
            ret = "(\"" + key + "\" is Missing)";
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
                BetterShop.Log(configname + " not found. Creating new file.");
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

    public void setCurrency() {
        try {
            if (BetterShop.iConomy != null) {
                String t = BetterShop.iConomy.format(1.);
                defaultCurrency = t.substring(t.indexOf(" ") + 1);
                t = BetterShop.iConomy.format(2.);
                pluralCurrency = t.substring(t.indexOf(" ") + 1);
            } else if (BetterShop.legacyIConomy != null) {
                String t = com.nijiko.coelho.iConomy.iConomy.getBank().format(1.);
                defaultCurrency = t.substring(t.indexOf(" ") + 1);
                t = com.nijiko.coelho.iConomy.iConomy.getBank().format(2.);
                pluralCurrency = t.substring(t.indexOf(" ") + 1);
            } else if(BetterShop.economy != null){
                defaultCurrency = BetterShop.economy.getMoneyName();
                pluralCurrency = BetterShop.economy.getMoneyNamePlural();
            }
        } catch (Exception e) {
            BetterShop.Log(Level.SEVERE, "Error Extracting Currency Name", e, false);
        }
    }

    public String currency() {
        return defaultCurrency;
    }

    public String currency(boolean plural) {
        return plural ? pluralCurrency : defaultCurrency;
    }

    String b(boolean b) {
        return b ? "1" : "0";
    }

    public String condensedSettings() {
        return b(checkUpdates) + ","
                + pagesize + "," + b(publicmarket) + "," + b(allowbuyillegal) + ","
                + b(usemaxstack) + "," + b(buybacktools) + "," + b(buybackenabled) + ","
                + b(hideHelp) + "," + b(sendLogOnError) + "," + b(sendAllLog) + ","
                + sortOrder.size() + ",'" + tableName + "'," + databaseType + ","
                + tempCacheTTL + "," + useDBCache + "," + priceListLifespan + ","
                + logUserTransactions + "," + userTansactionLifespan
                + ",'" + transLogTablename + "',"
                + logTotalTransactions + ",'" + recordTablename + "',"
                + useItemStock + ",'" + stockTablename + "'," + b(noOverStock) + ","
                + startStock + "," + maxStock + "," + restock + ","
                + useDynamicPricing + "," + sellcraftables + "',"
                + String.format("%2.3f", sellcraftableMarkup) + ","
                + b(woolsellweight);
    }

    public static int indexOf(String array[], String search) {
        if (array != null && array.length > 0) {
            for (int i = array.length - 1; i >= 0; --i) {
                if (array[i].equals(search)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int indexOfIgnoreCase(String array[], String search) {
        for (int i = array.length - 1; i >= 0; --i) {
            if (array[i].equalsIgnoreCase(search)) {
                return i;
            }
        }
        return -1;
    }

    static boolean configHasNode(Configuration config, String[] nodes) {
        for (String n : nodes) {
            if (config.getProperty(n) != null) {
                return true;
            }
        }
        return false;
    }
}
