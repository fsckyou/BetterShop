/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: BetterShop plugin configuration settings
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
package me.jascotty2.bettershop;

import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.MinecraftChatStr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import me.jascotty2.bettershop.enums.CommandShopMode;
import me.jascotty2.bettershop.enums.DBType;
import me.jascotty2.bettershop.enums.DiscountMethod;
import me.jascotty2.bettershop.enums.EconMethod;
import me.jascotty2.bettershop.enums.SpoutCategoryMethod;
import me.jascotty2.bettershop.shop.ShopConfig;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import org.bukkit.ChatColor;

import me.jascotty2.lib.bukkit.config.Configuration;
import me.jascotty2.lib.bukkit.config.ConfigurationNode;

public class BSConfig {

// <editor-fold defaultstate="collapsed" desc="Settings Variables">
	// chat messages
	private final HashMap<String, String> stringMap = new HashMap<String, String>();
	// files used by plugin
	public final static String configname = "config.yml";
	public final static File pluginFolder = new File("plugins", "BetterShop");
	public final static File configfile = new File(pluginFolder, configname);
	public final static File itemDBFile = new File(pluginFolder, "itemsdb.yml");
	public final static File signDBFile = new File(pluginFolder, "signs.dat");
	public final static File chestDBFile = new File(pluginFolder, "chests.dat");
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
	public final ShopConfig mainShopConfig = new ShopConfig();
	public EconMethod econ = EconMethod.AUTO;
	///// item buying behavior
	public boolean allowbuyillegal = true, //if someone without BetterShop.admin.illegal can buy illegal items
			usemaxstack = true, //whether maxstack should be honored
			buybacktools = true, //used tools can be bought back?
			buybackenabled = true; //shop buys items from users?
	public int maxEntityPurchase = 3; // max can purchase at a time
	// sign settings
	public boolean signShopEnabled = true,
			signDestroyProtection = true,
			signWEprotection = false,
			signTNTprotection = true,
			tntSignDestroyProtection = false,
			signItemColor = false, //color the names of items on signs?
			signItemColorBWswap = false;// swap black & white item colors?;;
	public String activeSignColor = "blue"; // automatically changed to \u00A7 format
	public static long signInteractWait = 1000; // wait before another action allowed
	// chest shop
	public String chestShopText = "BetterShop Chest Shop";
	public boolean chestShopEnabled = true,
			chestDestroyProtection = true,
			chestTNTprotection = true,
			chestSellBar = false;
	public String chestText = "BetterShop Chest Shop <e>",
			chestEditText = "(Editing)";
	// global or region shops
	public CommandShopMode commandShopMode = CommandShopMode.GLOBAL;
	////// database information
	private DBType databaseType = DBType.FLATFILE;
	/// database caching (MySQL only)
	public boolean useDBCache = true;
	//public BigInteger priceListLifespan = new BigInteger("0"); // 0 = never update
	public long priceListLifespan = 0; // 0 = never update
	public int tempCacheTTL = 10; //how long before tempCache is considered outdated (seconds)
	////// logging settings
	public boolean logUserTransactions = false,
			logTotalTransactions = false,
			logCommands = false;
	//public BigInteger userTansactionLifespan = new BigInteger("172800"); // 2 days.. i'd like to use unsigned long, but java doesn't have it..
	public long userTansactionLifespan = 172800;
	public String commandFilename = "Commands.log";
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
	// spout-related
	public boolean spoutEnabled = true;
	private String spoutKey = "B";
	public boolean largeSpoutMenu = true,
			spoutUsePages = false,
			spoutCatCustomSort = true,
			spoutUseScroll = false;
	public SpoutCategoryMethod spoutCategories = SpoutCategoryMethod.NONE;
	// discount permissions groups
	HashMap<String, Double> groups = new HashMap<String, Double>();
	public DiscountMethod discountSellingMethod = DiscountMethod.LOWER;
// </editor-fold>

	public BSConfig() {
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
		stringMap.put("logformat", "<M>/<d> <H>:<m>:<s> <user> <t> > <c>");
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
		stringMap.put("listalias", "&f[<item>&f]&2 is also known as: &b<alias>");
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

		stringMap.put("regionShopDisabled", "You cannot access the store from here!");

		try {
			Configuration config = new Configuration(configfile);
			config.load();
			ConfigurationNode n;

			// check for completedness
			try {
				HashMap<String, String[]> allKeys = new HashMap<String, String[]>();
				allKeys.put("shop", new String[]{
							"ItemsPerPage", "publicmarket",
							"logcommands", "commandLogFile", "logformat",
							"allowbuyillegal", "usemaxstack",
							"buybacktools", "buybackenabled", "maxEntityPurchase",
							"signShops",
							"activeSignColor", "signItemColor",
							"signItemColorBWswap",
							"signDestroyProtection",
							"weSignDestroyProtection",
							"tntSignDestroyProtection",
							"chestShops", 
							"chestDestroyProtection",
							"tntChestDestroyProtection",
							"chestText", "chestEditText", "chestSellBar",
							"sellDiscountMethod",
							"commandShop",
							"customsort",
							"defaultItemColor",
							"tablename",
							"hideHelp",
							"BOSBank",
							"currencyName",
							"economy"});
				allKeys.put("errors", new String[]{
							"CheckForUpdates",
							"AutoUpdate",
							"AutoErrorReporting",
							"UnMaskErrorID",
							"CustomErrorMessage",
							"sendLogOnError",
							"sendAllLog"});
				allKeys.put("spout", new String[]{
							"enabled",
							"key",
							"largeMenu",
							"usePages",
							"categories",
							"useSort"});
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
				allKeys.put("strings", stringMap.keySet().toArray(new String[0]));
				String allowNull[] = new String[]{
					"shop.customsort", "shop.BOSBank", "shop.currencyName", "shop.sellDiscountMethod", 
					"strings.listtail", "strings.logformat"};

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
					BetterShopLogger.Log("Notice: Unused Configuration Nodes: \n" + unused);
				}
				if (missing.length() > 0) {
					BetterShopLogger.Log("Missing Configuration Nodes: \n" + missing);
				}
			} catch (Exception ex) {
				// this shouldn't be happening: send error report
				BetterShopLogger.Log(Level.SEVERE, "Unexpected Error during config integrety check", ex, true);
			}

			// supporting these older nodes for now
			boolean usingOldMySQLsetting = false;
			if (configHasNode(config, new String[]{"CheckForUpdates", "AutoUpdate",
						"AutoErrorReporting", "UnMaskErrorID", "CustomErrorMessage",
						"ItemsPerPage", "publicmarket", "allowbuyillegal", "usemaxstack",
						"buybacktools", "buybackenabled", "maxEntityPurchase",
						"tablename", "useMySQL", "useMySQLPricelist", "defaultItemColor",
						"sendLogOnError", "sendAllLog", "hideHelp", "customsort"})) {
				BetterShopLogger.Log(Level.WARNING, "Using Deprecated Configuration Nodes: Update To New Format Soon!");

				checkUpdates = config.getBoolean("CheckForUpdates", checkUpdates);
				autoUpdate = config.getBoolean("AutoUpdate", autoUpdate);
				sendErrorReports = config.getBoolean("AutoErrorReporting", sendErrorReports);
				unMaskErrorID = config.getBoolean("UnMaskErrorID", unMaskErrorID);
				customErrorMessage = config.getString("CustomErrorMessage", customErrorMessage).trim();
				if (customErrorMessage.length() > MAX_CUSTMSG_LEN) {
					BetterShopLogger.Log("Notice: CustomErrorMessage is too long. (will be truncated)");
					customErrorMessage = customErrorMessage.substring(0, MAX_CUSTMSG_LEN);
				}

				pagesize = config.getInt("ItemsPerPage", pagesize);
				publicmarket = config.getBoolean("publicmarket", publicmarket);

				allowbuyillegal = config.getBoolean("allowbuyillegal", allowbuyillegal);
				usemaxstack = config.getBoolean("usemaxstack", usemaxstack);
				buybacktools = config.getBoolean("buybacktools", buybacktools);
				buybackenabled = config.getBoolean("buybackenabled", buybackenabled);
				maxEntityPurchase = config.getInt("maxEntityPurchase", maxEntityPurchase);

				mainShopConfig.tableName = config.getString("tablename", mainShopConfig.tableName);

				if (config.getProperty("useMySQL") != null || config.getProperty("useMySQLPricelist") != null) {
					usingOldMySQLsetting = true;
					databaseType = config.getBoolean("useMySQL", config.getBoolean("useMySQLPricelist", false)) ? DBType.MYSQL : DBType.FLATFILE;
					if (databaseType == DBType.MYSQL) {
						n = config.getNode("MySQL");
						if (n != null) {
							mainShopConfig.sql_username = n.getString("username", mainShopConfig.sql_username);
							mainShopConfig.sql_password = n.getString("password", mainShopConfig.sql_password);
							mainShopConfig.sql_database = n.getString("database", mainShopConfig.sql_database).replace(" ", "_");
							mainShopConfig.sql_hostName = n.getString("Hostname", mainShopConfig.sql_hostName);
							mainShopConfig.sql_portNum = n.getString("Port", mainShopConfig.sql_portNum);
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
									BetterShopLogger.Log(Level.WARNING, "cacheUpdate has an illegal value");
									BetterShopLogger.Log(Level.WARNING, ex);
								}
							}
						} else {
							BetterShopLogger.Log(Level.WARNING, "MySQL section in " + configname + " is missing");
						}
					}
				}

				defColor = config.getString("defaultItemColor", defColor);
				JItemDB.setDefaultColor(defColor);

				sendLogOnError = config.getBoolean("sendLogOnError", sendLogOnError);
				sendAllLog = config.getBoolean("sendAllLog", sendAllLog);

				hideHelp = config.getBoolean("hideHelp", hideHelp);
				BOSBank = config.getString("BOSBank", "");

				String customsort = config.getString("customsort");
				if (customsort != null) {
					// parse for items && add to custom sort arraylist
					String items[] = customsort.split(",");
					for (String i : items) {
						JItem toAdd = JItemDB.findItem(i.trim());
						if (toAdd != null) {
							sortOrder.add(toAdd.IdDatStr());
						} else {
							BetterShopLogger.Log("Invalid Item in customsort configuration: " + i);
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
					BetterShopLogger.Log("Notice: CustomErrorMessage is too long. (will be truncated)");
					customErrorMessage = customErrorMessage.substring(0, MAX_CUSTMSG_LEN);
				}
				sendLogOnError = n.getBoolean("sendLogOnError", sendLogOnError);
				sendAllLog = n.getBoolean("sendAllLog", sendAllLog);
			}

			if ((n = config.getNode("shop")) != null) {
				pagesize = n.getInt("ItemsPerPage", pagesize);
				publicmarket = n.getBoolean("publicmarket", publicmarket);

				logCommands = n.getBoolean("logcommands", logCommands);
				if (n.getString("commandLogFile") != null) {
					commandFilename = n.getString("commandLogFile");
				}
				stringMap.put("logformat", n.getString("logformat", stringMap.get("logformat")));

				allowbuyillegal = n.getBoolean("allowbuyillegal", allowbuyillegal);
				usemaxstack = n.getBoolean("usemaxstack", usemaxstack);
				buybacktools = n.getBoolean("buybacktools", buybacktools);
				buybackenabled = n.getBoolean("buybackenabled", buybackenabled);
				maxEntityPurchase = n.getInt("maxEntityPurchase", maxEntityPurchase);

				signShopEnabled = n.getBoolean("signShops", signShopEnabled);
				activeSignColor = n.getString("activeSignColor", activeSignColor);
				signDestroyProtection = n.getBoolean("signDestroyProtection", signDestroyProtection);
				signWEprotection = n.getBoolean("weSignDestroyProtection", signWEprotection);
				signTNTprotection = n.getBoolean("tntSignDestroyProtection", signTNTprotection);

				chestShopEnabled = n.getBoolean("chestShops", chestShopEnabled);
				chestSellBar = n.getBoolean("chestSellBar", chestSellBar);
				chestDestroyProtection = n.getBoolean("chestDestroyProtection", chestDestroyProtection);
				chestTNTprotection = n.getBoolean("tntChestDestroyProtection", chestTNTprotection);

				chestText = n.getString("chestText", chestText);
				chestEditText = n.getString("chestEditText", chestEditText);

				signItemColor = n.getBoolean("signItemColor", signItemColor);
				signItemColorBWswap = n.getBoolean("signItemColorBWswap", signItemColorBWswap);

				String sd = n.getString("sellDiscountMethod");
				if(sd != null) {
					if(sd.equalsIgnoreCase("None")) {
						discountSellingMethod = DiscountMethod.NONE;
					} else if(sd.equalsIgnoreCase("Higher")) {
						discountSellingMethod = DiscountMethod.HIGHER;
					} else {
						if(!sd.equalsIgnoreCase("Lower")) {
							BetterShopLogger.Warning("Invalid setting in shop.sellDiscountMethod" + sd);
						}
						discountSellingMethod = DiscountMethod.LOWER;
					}
				}

				String cShopMode = n.getString("commandShop");
				if (cShopMode != null) {
					if (cShopMode.equalsIgnoreCase("disabled")
							|| cShopMode.equalsIgnoreCase("none")) {
						commandShopMode = CommandShopMode.NONE;
					} else if (cShopMode.equalsIgnoreCase("regions")
							|| cShopMode.equalsIgnoreCase("region")) {
						commandShopMode = CommandShopMode.REGIONS;
					} else if (cShopMode.equalsIgnoreCase("both")) {
						commandShopMode = CommandShopMode.BOTH;
					} else {
						if (!cShopMode.equalsIgnoreCase("global")) {
							BetterShopLogger.Warning("Invalid setting in shop.commandShop: " + cShopMode);
						}
						commandShopMode = CommandShopMode.GLOBAL;
					}
				}

				mainShopConfig.tableName = n.getString("tablename", mainShopConfig.tableName);

				defColor = n.getString("defaultItemColor", defColor);
				JItemDB.setDefaultColor(defColor);

				//ItemCurrency.loadFromString(n.getString("currencyItems", "diamond>20, goldbar>5, ironbar>1, redstone>.5"));
				defaultCurrency = n.getString("currencyName", "Coin");
				pluralCurrency = n.getString("currencyNamePlural", "Coins");

				String customsort = n.getString("customsort");
				if (customsort != null) {
					// parse for items && add to custom sort arraylist
					String items[] = customsort.split(",");
					for (String i : items) {
						JItem toAdd = JItemDB.findItem(i.trim());
						if (toAdd != null) {
							sortOrder.add(toAdd.IdDatStr());
						} else {
							BetterShopLogger.Log("Invalid Item in customsort configuration: " + i);
						}
					}
				}
				hideHelp = n.getBoolean("hideHelp", hideHelp);
				
				String t = n.getString("economy");
				if(t != null) {
					if(t.equalsIgnoreCase("exp")) {
						econ = EconMethod.EXP;
					} else if(t.equalsIgnoreCase("total")) {
						econ = EconMethod.TOTAL;
					}
//					else if(t.equalsIgnoreCase("bultin")) {
//						econ = EconMethod.BULTIN;
//					} 
					else {
						econ = EconMethod.AUTO;
						if(!t.equalsIgnoreCase("auto")) {
							BetterShopLogger.Log("shop.economy has an invalid value: " + t + " (defaulting to auto)");
						}
					}
				}
			}
			if (activeSignColor.equalsIgnoreCase("blue")) {
				// "blue" looks like light purple on a sign
				activeSignColor = "dark blue";
			}
			activeSignColor = MinecraftChatStr.getChatColorStr(activeSignColor, ChatColor.BLUE);

			if ((n = config.getNode("spout")) != null) {
				spoutEnabled = n.getBoolean("enabled", spoutEnabled);
				largeSpoutMenu = n.getBoolean("largeMenu", largeSpoutMenu);
				spoutUsePages = n.getBoolean("usePages", spoutUsePages);
				setSpoutKey(n.getString("key", spoutKey));
				String c = n.getString("categories");
				if (c != null) {
					if (c.equalsIgnoreCase("cycle")) {
						spoutCategories = SpoutCategoryMethod.CYCLE;
					} else if (c.equalsIgnoreCase("tab") || c.equalsIgnoreCase("tabbed")) {
						spoutCategories = SpoutCategoryMethod.TABBED;
					} else {
						spoutCategories = SpoutCategoryMethod.NONE;
					}
				}
				spoutCatCustomSort = n.getBoolean("useSort", spoutCatCustomSort);
			}

			// groups
			if ((n = config.getNode("discountGroups")) != null) {
				groups.clear();
				for (String g : n.getKeys()) {
					if (!CheckInput.IsDouble(n.getString(g))) {
						BetterShopLogger.Log(Level.WARNING, "Invalid discount set for " + g);
					} else {
						double d = CheckInput.GetDouble(n.getString(g), 0) / 100;
						if (d > 1) {
							d = 1;
						}
						groups.put(g, d);
					}
				}
			}

			if (!usingOldMySQLsetting && (n = config.getNode("MySQL")) != null) {
				databaseType = n.getBoolean("useMySQL", config.getBoolean("useMySQLPricelist", false)) ? DBType.MYSQL : DBType.FLATFILE;
				if (databaseType == DBType.MYSQL) {
					mainShopConfig.sql_username = n.getString("username", mainShopConfig.sql_username);
					mainShopConfig.sql_password = n.getString("password", mainShopConfig.sql_password);
					mainShopConfig.sql_database = n.getString("database", mainShopConfig.sql_database).replace(" ", "_");
					mainShopConfig.sql_hostName = n.getString("Hostname", mainShopConfig.sql_hostName);
					mainShopConfig.sql_portNum = n.getString("Port", mainShopConfig.sql_portNum);
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
							BetterShopLogger.Log(Level.WARNING, "cacheUpdate has an illegal value");
							BetterShopLogger.Log(Level.WARNING, ex);
						}
					}
				}
			}

			if ((n = config.getNode("transactionLog")) != null) {
				logUserTransactions = n.getBoolean("enabled", logUserTransactions);
				mainShopConfig.transLogTablename = n.getString("logtablename", mainShopConfig.transLogTablename).replace(" ", "_");
				String lifespan = n.getString("userTansactionLifespan");
				if (lifespan != null) {
					try {
						userTansactionLifespan = CheckInput.GetBigInt_TimeSpanInSec(lifespan).longValue();
					} catch (Exception ex) {
						BetterShopLogger.Log(Level.WARNING, "userTansactionLifespan has an illegal value", ex);
					}
				}
			}

			if ((n = config.getNode("totalsTransactionLog")) != null) {
				logTotalTransactions = n.getBoolean("enabled", logTotalTransactions);
				mainShopConfig.recordTablename = n.getString("logtablename", mainShopConfig.recordTablename).replace(" ", "_");
			}

			if ((n = config.getNode("dynamicMarket")) != null) {
				useDynamicPricing = n.getBoolean("dynamicPricing", n.getBoolean("enabled", useDynamicPricing));
				sellcraftables = n.getBoolean("sellcraftables", sellcraftables);
				sellcraftableMarkup = n.getDouble("sellcraftableMarkup", sellcraftableMarkup / 100) * 100;
				woolsellweight = n.getBoolean("woolsellweight", woolsellweight);
			}

			if ((n = config.getNode("itemStock")) != null) {
				useItemStock = n.getBoolean("enabled", useItemStock);
				mainShopConfig.stockTablename = n.getString("stockTablename", mainShopConfig.stockTablename);
				mainShopConfig.noOverStock = n.getBoolean("noOverStock", mainShopConfig.noOverStock);
				String num = n.getString("startStock");
				if (num != null) {
					mainShopConfig.startStock = CheckInput.GetLong(num, mainShopConfig.startStock);//CheckInput.GetBigInt(num, startStock).longValue();
				}
				num = n.getString("maxStock");
				if (num != null) {
					mainShopConfig.maxStock = CheckInput.GetLong(num, mainShopConfig.maxStock);//CheckInput.GetBigInt(num, maxStock).longValue();
				}
				num = n.getString("restock");
				if (num != null) {
					try {
						mainShopConfig.restock = CheckInput.GetBigInt_TimeSpanInSec(num, 'h').longValue();
					} catch (Exception ex) {
						BetterShopLogger.Log(Level.WARNING, "restock has an illegal value", ex);
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
				BetterShopLogger.Log(Level.SEVERE, String.format("strings section missing from configuration file %s", configname), false);
			}
			for (String k : stringMap.keySet()) {
				stringMap.put(k, stringMap.get(k).
						replace("&&", "\t\n"). // so a '&' can still be used
						replace("&", "\u00A7").
						replace("\t\n", "&"). // replace the &
						replace("%", "%%"));
			}

		} catch (Exception ex) {
			BetterShopLogger.Log(Level.SEVERE, "Error parsing configuration file", ex, false);
			return false;
		}
		return true;
	}

	public void extractDefaults() {
		pluginFolder.mkdirs();


		if (!configfile.exists()) {
			BetterShopLogger.Log(configname + " not found. Creating new file.");
			extractFile(
					configfile);


		}
		if (!itemDBFile.exists()) {
			extractFile(itemDBFile);


		}
	}

	private void extractFile(File dest) {
		extractFile(dest, dest.getName());


	}

	private void extractFile(File dest, String fname) {
		try {
			dest.createNewFile();
			InputStream res = BetterShop.class.getResourceAsStream("/" + fname);
			FileWriter tx = new FileWriter(dest);



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
			BetterShopLogger.Log(Level.SEVERE, "Failed creating new file (" + fname + ")", ex, false);


		}
	}

	public void setCurrency() {
		try {
//            if (BetterShop.iConomy != null) {
//                String t = BetterShop.iConomy.format(1.);
//                defaultCurrency = t.substring(t.indexOf(" ") + 1);
//                t = BetterShop.iConomy.format(2.);
//                pluralCurrency = t.substring(t.indexOf(" ") + 1);
//            } else if (BetterShop.legacyIConomy != null) {
//                String t = com.nijiko.coelho.iConomy.iConomy.getBank().format(1.);
//                defaultCurrency = t.substring(t.indexOf(" ") + 1);
//                t = com.nijiko.coelho.iConomy.iConomy.getBank().format(2.);
//                pluralCurrency = t.substring(t.indexOf(" ") + 1);
//            } else if (BetterShop.economy != null) {
//                defaultCurrency = BetterShop.economy.getMoneyName();
//                pluralCurrency = BetterShop.economy.getMoneyNamePlural();
//            } else if (BetterShop.essentials != null) {
//                File conf = new File(BetterShop.essentials.getDataFolder(), "config.yml");
//                if (conf.exists()) {
//                    Configuration config = new Configuration(conf);
//                    config.load();
//                    defaultCurrency = config.getString("currency-name", defaultCurrency);
//                    pluralCurrency = config.getString("currency-name-plural", pluralCurrency);
//                }
//            }
			String eco = BSEcon.economyMethod.getName();


			if (eco.equalsIgnoreCase("iConomy")
					|| eco.equalsIgnoreCase("BOSEconomy")) {
				String t = BSEcon.format(1.);
				defaultCurrency = t.substring(t.indexOf(' ') + 1);
				t = BSEcon.format(2.);
				pluralCurrency = t.substring(t.indexOf(' ') + 1);


			} else if (eco.equalsIgnoreCase("Essentials")) {
				File conf = new File("Essentials", "config.yml");


				if (conf.exists()) {
					Configuration config = new Configuration(conf);
					config.load();
					defaultCurrency = config.getString("currency-name", defaultCurrency);
					pluralCurrency = config.getString("currency-name-plural", pluralCurrency);


				}
			}
		} catch (Exception e) {
			BetterShopLogger.Log(Level.SEVERE, "Error Extracting Currency Name", e, false);


		}
	}

	public boolean useMySQL() {
		return databaseType == DBType.MYSQL;


	}

	public boolean useFlatfile() {
		return databaseType == DBType.FLATFILE;


	}

	public boolean useGlobalCommandShop() {
		return commandShopMode == CommandShopMode.GLOBAL || commandShopMode == CommandShopMode.BOTH;


	}

	public boolean useRegionCommandShop() {
		return commandShopMode == CommandShopMode.REGIONS || commandShopMode == CommandShopMode.BOTH;


	}

	public boolean useCommandShop() {
		return commandShopMode != CommandShopMode.NONE;


	}

	public boolean useCommandShopGlobal() {
		return commandShopMode == CommandShopMode.BOTH;


	}

	public String getActiveSignColor() {
		return activeSignColor;


	}

	public String getCustomErrorMessage() {
		return customErrorMessage;


	}

	public String getSpoutKey() {
		return spoutKey;


	}

	public void setSpoutKey(String spoutKey) {
		if (!this.spoutKey.equals(spoutKey)) {
			this.spoutKey = spoutKey;


			if (BetterShop.keyListener != null) {
				BetterShop.keyListener.reloadKey();


			}
		}
	}

	public String getString(String key) {
		String ret = stringMap.get(key);


		if (ret == null) {
			BetterShopLogger.Log(Level.WARNING, String.format("%s missing from configuration file", key));
			ret = "(\"" + key + "\" is Missing)";


		}
		return ret;


	}

	public boolean hasString(String key) {
		String ret = stringMap.get(key);


		if (ret == null) {
			BetterShopLogger.Log(Level.WARNING, String.format("%s missing from configuration file", key));


			return false;


		}
		return true;


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
				+ sortOrder.size() + ",'" + mainShopConfig.tableName + "'," + databaseType + ","
				+ tempCacheTTL + "," + useDBCache + "," + priceListLifespan + ","
				+ logUserTransactions + "," + userTansactionLifespan
				+ ",'" + mainShopConfig.transLogTablename + "',"
				+ logTotalTransactions + ",'" + mainShopConfig.recordTablename + "',"
				+ useItemStock + ",'" + mainShopConfig.stockTablename + "'," + b(mainShopConfig.noOverStock) + ","
				+ mainShopConfig.startStock + "," + mainShopConfig.maxStock + "," + mainShopConfig.restock + ","
				+ useDynamicPricing + "," + sellcraftables + ","
				+ String.format("%2.3f", sellcraftableMarkup) + ","
				+ b(woolsellweight) + ","
				+ spoutEnabled + "," + spoutKey + ","
				+ largeSpoutMenu + "," + spoutUsePages + ","
				+ spoutCatCustomSort + "," + spoutCategories.name();


	}

	public static int indexOf(String array[], String search) {
		if (array != null && array.length > 0) {
			for (int i = array.length - 1; i
					>= 0;
					--i) {
				if (array[i].equals(search)) {
					return i;


				}
			}
		}
		return -1;


	}

	public static int indexOfIgnoreCase(String array[], String search) {
		for (int i = array.length - 1; i
				>= 0;
				--i) {
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
