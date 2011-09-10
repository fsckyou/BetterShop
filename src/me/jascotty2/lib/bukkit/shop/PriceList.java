/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: tracks items in a database with buy/sell prices
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
package me.jascotty2.lib.bukkit.shop;

import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.mysql.MySQLPriceList;
import me.jascotty2.lib.bukkit.item.*;
import me.jascotty2.lib.bukkit.MinecraftChatStr;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.jascotty2.lib.mysql.MySQL;
import org.bukkit.inventory.ItemStack;

public class PriceList {
	// set this to true to catch & log exceptions instead of throwing them

	public static boolean log_nothrow = false;
	// what to write to if logging
	protected final static Logger logger = Logger.getLogger("Minecraft");
	//how long before tempCache is considered outdated (seconds), used if MySQL & caching disabled
	public int tempCacheTTL = 10;
	// temporary storage for last queried item (primarily for when caching is disabled)
	PriceListItem tempCache = null;
	// use full db caching?
	public boolean useCache = false;
	// if using caching, how long before cache is considered outdated (seconds)
	public long dbCacheTTL = 0;
	// if false, will disconnect from db when now using it (use if have a high dbCacheTTL)
	//public boolean persistentMySQL = true;
	// last time the pricelist was updated
	protected Date lastCacheUpdate = null;
	protected List<PriceListItem> priceList = new ArrayList<PriceListItem>();
	private boolean isLoaded = false;
	private DBType databaseType = DBType.FLATFILE;
	//  current db connection, if using MySQL
	protected MySQLPriceList MySQLpricelist = null;
	// file, if flatfile
	File flatFile = null;
	// max buy/sell price
	public static final int MAX_PRICE = 999999999; // int max=2,147,483,647
	// uses the JItem.IdDatStr() (like "5:0")
	public List<String> sortOrder = new ArrayList<String>();

	/*
	public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	List<T> list = new ArrayList<T>(c);
	java.util.Collections.sort(list);
	return list;
	}//*/
	public static class PriceListItemComparator implements Comparator<PriceListItem> {

		public List<String> sortOrder = new ArrayList<String>();
		boolean descending = true;

		public PriceListItemComparator() {
		}

		public PriceListItemComparator(List<String> sortOrder) {
			this.sortOrder.addAll(sortOrder);
		}

		public int compare(PriceListItem o1, PriceListItem o2) {
			if (sortOrder.size() > 0) {
				int o1i = sortOrder.indexOf(o1.IdDatStr());
				int o2i = sortOrder.indexOf(o2.IdDatStr());
				if (o1i != -1 && o2i != -1) {
					return o2i - o1i;
				} else if (o1i != -1) {
					return -1;
				} else if (o2i != -1) {
					return 1;
				}
			}
			return (o1.ID() * 100 - o2.ID() * 100 + o1.Data() - o2.Data()) * (descending ? 1 : -1);
		}
	}

	public void sort() {
		java.util.Collections.sort(priceList, new PriceListItemComparator(sortOrder));
	}

	public enum DBType {

		MYSQL, FLATFILE //  SQLITE,
	}

	public PriceList() {
	} // end default constructor

	public String pricelistName() {
		return databaseType == DBType.MYSQL
				? (MySQLpricelist == null ? null
				: (MySQLpricelist.getHostName() + ":"
				+ MySQLpricelist.getDatabaseName() + "/"
				+ MySQLpricelist.getTableName())) : flatFile == null ? null : flatFile.getName();
	}

	public MySQL getMySQLconnection() {
		return MySQLpricelist == null ? null : MySQLpricelist;
	}

	public final boolean reloadList() throws SQLException, IOException, Exception {
		if (databaseType == DBType.MYSQL) {
			return reloadMySQL();
		} else {
			return loadFile(flatFile);
		}
	}

	private boolean reloadMySQL() throws SQLException, Exception { //
		if (MySQLpricelist != null)// && MySQLdatabase.IsConnected())  MySQLdatabase.disconnect();
		{
			priceList.clear();
			try {
				MySQLpricelist.connect();
			} catch (Exception ex) {
				if (log_nothrow) {
					logger.log(Level.SEVERE, "Error connecting to MySQL database", ex);
				} else {
					throw new Exception("Error connecting to MySQL database", ex);
				}
			}
			// load pricelist data
			priceList.addAll(MySQLpricelist.getFullList());
		}

		return false;
	}

	public final boolean loadMySQL(String database, String tableName, String username, String password, String hostName, String portNum) throws SQLException, Exception {
		databaseType = DBType.MYSQL;
		try {
			if (MySQLpricelist == null) {
				MySQLpricelist = new MySQLPriceList(database, tableName, username, password, hostName, portNum);
			} else {
				MySQLpricelist.connect(database, tableName, username, password, hostName, portNum);
			}
		} catch (SQLException ex) {
			if (log_nothrow) {
				logger.log(Level.SEVERE, "Error connecting to MySQL database or while retrieving table list", ex);
			} else {
				throw ex; // "Error connecting to MySQL database or while retrieving table list"
			}
		} catch (Exception ex) {
			if (log_nothrow) {
				logger.log(Level.SEVERE, "Failed to start database connection", ex);
			} else {
				throw ex; // "Failed to start database connection"
			}
		}
		return isLoaded = MySQLpricelist.isConnected();
	}

	public boolean loadFile(File toload) throws IOException, Exception {
		if (toload != null) {
			databaseType = DBType.FLATFILE;
			flatFile = toload;
			priceList.clear();
			if (toload.exists()) {
				isLoaded = false;
				FileReader fstream = null;
				try {
					fstream = new FileReader(toload.getAbsolutePath());
					BufferedReader in = new BufferedReader(fstream);
					try {
						int n = 0;
						for (String line = null; (line = in.readLine()) != null && line.length() > 0; ++n) {
							// if was edited in openoffice, will instead have semicolins..
							String fields[] = line.replace(";", ",").replace(",,", ", ,").split(",");

							if (fields.length > 4) {
								JItem plItem = JItemDB.findItem(fields[0] + ":" + (fields[1].equals(" ") ? "0" : fields[1]));
								if (plItem != null) {
									//priceList.add(new PriceListItem(plItem, fields[2].length() == 0 ? -1 : CheckInput.GetDouble(fields[2], -1), fields[3].length() == 0 ? -1 : CheckInput.GetDouble(fields[3], -1)));
									priceList.add(new PriceListItem(plItem,
											fields[2].equals(" ") ? -1 : CheckInput.GetDouble(fields[2], -1),
											fields[3].equals(" ") ? -1 : CheckInput.GetDouble(fields[3], -1)));
								} else if (n > 0) { // first line is expected invalid: is title
									logger.log(Level.WARNING, String.format("Invalid item on line %d in %s (%s)", (n + 1), toload.getName(),
											fields[0] + ":" + (fields[1].equals(" ") ? "0" : fields[1]) + (fields.length <= 4 ? "" : " [" + fields[4] + "]")));
								}
							} else {
								logger.log(Level.WARNING, String.format("unexpected pricelist line at %d in %s", (n + 1), toload.getName()));
							}
						}
					} finally {
						in.close();
					}
				} catch (IOException ex) {
					if (!log_nothrow) {
						throw new IOException("Error opening " + toload.getName() + " for reading", ex);
					}
					logger.log(Level.SEVERE, "Error opening " + toload.getName() + " for reading", ex);
				} finally {
					if (fstream != null) {
						try {
							fstream.close();
						} catch (IOException ex) {
							if (!log_nothrow) {
								throw new IOException("Error closing " + toload.getName(), ex);
							}
							logger.log(Level.SEVERE, "Error closing " + toload.getName(), ex);
						}
					}
				}
				return isLoaded = true && save();
			} else { // !toload.exists()
				// still set as loaded
				isLoaded = true;
				return save();
			}
		}
		return false;
	}

	/**
	 * closes connections & frees up used memory
	 * @throws IOException
	 * @throws SQLException
	 */
	public void close() throws IOException, SQLException {
		// no need to save.. is saved after every edit
		//save();
		if (databaseType == DBType.MYSQL && MySQLpricelist != null) {
			MySQLpricelist.commit();
			MySQLpricelist.disconnect();
			MySQLpricelist = null;
		}
		priceList.clear();
		lastCacheUpdate = null;
		isLoaded = false;
	}

	public boolean isLoaded() {
		if (databaseType == DBType.MYSQL) {
			return (dbCacheTTL == 0 && MySQLpricelist.isConnected()) || isLoaded;
		}
		return isLoaded;
	}

	public boolean save() throws IOException, SQLException {
		sort();
		if (databaseType == DBType.MYSQL) {
			try {
				MySQLpricelist.commit();
				return true;
			} catch (SQLException ex) {
				if (!log_nothrow) {
					throw new SQLException("Error executing COMMIT", ex);
				}
				logger.log(Level.SEVERE, "Error executing COMMIT", ex);
				return false;
			}
		} else {
			try {
				return saveFile(flatFile);
			} catch (IOException ex) {
				if (!log_nothrow) {
					throw new IOException("Error Saving " + (flatFile == null ? "flatfile price database" : flatFile.getName()), ex);
				}
				logger.log(Level.SEVERE, "Error Saving " + (flatFile == null ? "flatfile price database" : flatFile.getName()), ex);
				return false;
			}
		}
	}

	public boolean saveFile(File tosave) throws IOException {
		if (tosave != null && !tosave.isDirectory()) {
			if (!tosave.exists()) {
				File dir = new File(tosave.getAbsolutePath().substring(0, tosave.getAbsolutePath().lastIndexOf(File.separatorChar)));
				dir.mkdirs();
				try {
					if (!tosave.createNewFile()) {
						return false;
					}
				} catch (Exception e) {
					return false;
				}
			}
			if (tosave.canWrite()) {

				FileWriter fstream = null;
				try {
					fstream = new FileWriter(tosave.getAbsolutePath());
					//System.out.println("writing to " + tosave.getAbsolutePath());
					BufferedWriter out = new BufferedWriter(fstream);
					out.write("id,subdata,buyprice,sellprice,name");
					out.newLine();
					for (PriceListItem i : priceList) {
						// names provided for others to easily edit db
						out.write(i.ID() + "," + i.Data() + "," + i.buy + "," + i.sell + "," + i.Name());
						out.newLine();
					}
					out.flush();
					out.close();
				} catch (IOException ex) {
					if (!log_nothrow) {
						throw new IOException("Error opening " + tosave.getName() + " for writing", ex);
					}
					logger.log(Level.SEVERE, "Error opening " + tosave.getName() + " for writing", ex);
				} finally {
					if (fstream != null) {
						try {
							fstream.close();
						} catch (IOException ex) {/*
							if (!log_nothrow) {
							throw new IOException("Error closing " + tosave.getName(), ex);
							}
							logger.log(Level.SEVERE, "Error closing " + tosave.getName(), ex);*/

						}
					}
				}
				return true;
			}
		} else if (tosave == null) {
			throw new IOException("no file to save to");
		} else if (tosave.isDirectory()) {
			throw new IOException("file to save is a directory");
		}
		return false;
	}

	public void updateCache() throws SQLException, Exception {
		updateCache(true);
	}

	public void updateCache(boolean checkFirst) throws SQLException, Exception {
		if (databaseType == DBType.FLATFILE) {
			// (flatfile is cached, manually updated)
			return;
		}
		//System.out.println("use c:" + useCache + "   " + dbCacheTTL);
		if (!checkFirst || (useCache && lastCacheUpdate == null)
				|| (useCache && dbCacheTTL > 0 && lastCacheUpdate != null
				&& ((new Date()).getTime() - lastCacheUpdate.getTime()) / 1000 > dbCacheTTL)) {
			//System.out.println("updating cache (" + (lastCacheUpdate != null?((new Date()).getTime() - lastCacheUpdate.getTime())/100:-1) + "s since last update)");
			// MySQL cache outdated: update
			List<PriceListItem> update = MySQLpricelist.getFullList();
			priceList.clear();
			priceList.addAll(update);
			lastCacheUpdate = new Date();
		}
		//else cache up-to-date, or disabled
	}

	public boolean itemExists(ItemStockEntry i) throws SQLException, Exception {
		return itemExists(JItemDB.GetItem(i.itemNum, (byte) i.itemSub));
	}

	public boolean itemExists(String check) throws SQLException, Exception {
		return itemExists(JItemDB.findItem(check));
	}

	public boolean itemExists(ItemStack check) throws SQLException, Exception {
		return itemExists(JItemDB.findItem(check));
	}

	public boolean itemExists(JItem check) throws SQLException, Exception {
		if (check == null) {
			return false;
		}
		updateCache(true);
		if (databaseType == DBType.MYSQL && dbCacheTTL == 0) {
			tempCache = MySQLpricelist.getItem(check);
		} else { // should be in cache
			int i = priceList.indexOf(new PriceListItem(check));
			if (i < 0) {
				return false;
			}//else
			if (tempCache == null) {
				tempCache = new PriceListItem(priceList.get(i));
			} else {
				tempCache.Set(priceList.get(i));
			}
		}
		return tempCache != null;
	}

	public boolean isForSale(String s) throws SQLException, Exception {
		if (tempCache != null && tempCache.Name().equals(s)) {
			// has been retrieved recently
			return tempCache.sell >= 0;
		}
		//if itemExists, tempCache will contain the item
		return itemExists(s) && tempCache.sell >= 0;//getSellPrice(s) >= 0;
	}

	public boolean isForSale(JItem i) throws SQLException, Exception {
		if (tempCache != null && tempCache.equals(i)) {
			// has been retrieved recently
			return tempCache.sell >= 0;
		}
		//if itemExists, tempCache will contain the item
		return itemExists(i) && tempCache.sell >= 0;//getSellPrice(i) >= 0;
	}

	public boolean isForSale(ItemStack i) throws SQLException, Exception {
		if (tempCache != null && tempCache.equals(i)) {
			// has been retrieved recently
			return tempCache.sell >= 0;
		}
		//if itemExists, tempCache will contain the item
		return itemExists(i) && tempCache.sell >= 0;//getSellPrice(i) >= 0;
	}

	public boolean isForSale(ItemStockEntry i) throws SQLException, Exception {
		if (tempCache != null && tempCache.ID() == i.itemNum && tempCache.Data() == i.itemSub) {
			// has been retrieved recently
			return tempCache.sell >= 0;
		}
		//if itemExists, tempCache will contain the item
		return itemExists(i) && tempCache.sell >= 0;//getSellPrice(i) >= 0;
	}

	public double getSellPrice(ItemStack i) throws SQLException, Exception {
		return getSellPrice(JItemDB.findItem(i));
	}

	public double getSellPrice(String s) throws SQLException, Exception {
		return getSellPrice(JItemDB.findItem(s));
	}

	public double getSellPrice(ItemStockEntry it) throws SQLException, Exception {
		return getSellPrice(JItemDB.GetItem(it.itemNum, (byte) it.itemSub));
	}

	public double getSellPrice(JItem it) throws SQLException, Exception {
		if (it == null) {
			return -1;
		}
		PriceListItem itp = getItemPrice(it);
		return itp != null ? itp.sell : -1;

	}

	public double getSellPrice(int id, byte dat) throws SQLException, Exception {
		PriceListItem itp = getItemPrice(id, dat);
		return itp != null ? itp.sell : -1;
	}

	public double getBuyPrice(String s) throws SQLException, Exception {
		return getBuyPrice(JItemDB.findItem(s));
	}

	public double getBuyPrice(JItem it) throws SQLException, Exception {
		if (it == null) {
			return -1;
		}
		PriceListItem itp = getItemPrice(it);
		return itp != null ? itp.buy : -1;
	}

	public double getBuyPrice(int id, byte dat) throws SQLException, Exception {
		PriceListItem itp = getItemPrice(id, dat);
		return itp != null ? itp.buy : -1;
	}

	public PriceListItem getItemPrice(JItem it) throws SQLException, Exception {
		return it == null ? null : getItemPrice(it.ID(), it.Data());
	}

	public PriceListItem getItemPrice(int id, byte dat) throws SQLException, Exception {
		if (tempCache != null && tempCache.ID() == id && tempCache.Data() == dat
				&& (System.currentTimeMillis() - tempCache.getTime()) / 1000 < tempCacheTTL) {
			// use temp
			return tempCache;
		}
		if (databaseType == DBType.MYSQL && !useCache) {
			tempCache = MySQLpricelist.getItem(id, dat);
		} else {
			updateCache(true);
			PriceListItem f = null;
			for (PriceListItem p : priceList) {
				if (p != null && p.ID() == id && p.Data() == dat) {
					f = p;
					break;
				}
			}
			if (f == null) {
				return null;
			}
			if (tempCache == null) {
				tempCache = new PriceListItem(f);
			} else {
				tempCache.Set(f);
			}
		}
		return tempCache;
	}

	public boolean setPrice(String item, double b, double s) throws SQLException, IOException, Exception {
		return setPrice(JItemDB.findItem(item), b, s);
	}

	public boolean setPrice(JItem it, double b, double s) throws SQLException, IOException, Exception {
		if (it == null) {
			return false;
		}
		tempCache = null;
		if (databaseType == DBType.MYSQL) {
			MySQLpricelist.setPrice(it, b, s);
			updateCache(false);
			return true;
		} else {
			int i = priceList.indexOf(new PriceListItem(it));
			if (i < 0) {
				priceList.add(new PriceListItem(it, b, s));
			} else {
				priceList.get(i).SetPrice(b, s);
			}
			return save();
		}
	}

	public boolean remove(String s) throws SQLException, Exception {
		return remove(JItemDB.findItem(s));
	}

	public boolean remove(JItem it) throws SQLException, Exception {
		tempCache = null;
		if (databaseType == DBType.MYSQL) {
			MySQLpricelist.removeItem(it);
			updateCache(false);
			return true;
		} else {
			if (priceList.remove(new PriceListItem(it))) {
				return save();
			}
		}
		return false;
	}

	public void removeAll() throws IOException, SQLException {
		tempCache = null;
		if (databaseType == DBType.MYSQL) {
			MySQLpricelist.removeAll();
		} else {
			priceList.clear();
			save();
		}
	}

	public List<String> getItemList(boolean showIllegal) throws SQLException, Exception {
		LinkedList<String> items = new LinkedList<String>();

		for (int i = 0; i < priceList.size(); ++i) {
			if ((!showIllegal && !priceList.get(i).IsLegal())
					|| (priceList.get(i).buy < 0 && priceList.get(i).sell < 0)) {
				continue;
			}
			items.add(priceList.get(i).coloredName());
		}
		return items;
	}

	public List<String> getItemList(boolean showIllegal, String itemTail) throws SQLException, Exception {
		LinkedList<String> items = new LinkedList<String>();

		for (int i = 0; i < priceList.size(); ++i) {
			if (!showIllegal && !priceList.get(i).IsLegal()
					|| (priceList.get(i).buy < 0 && priceList.get(i).sell < 0)) {
				continue;
			}
			items.add(priceList.get(i).coloredName() + itemTail);
		}
		return items;
	}

	/**
	 * size of items that shop will buy or sell
	 * @return
	 */
	public int getShopSize() {
		int num = priceList.size();
		for (PriceListItem i : priceList) {
			if (i.buy < 0 && i.sell < 0) {
				--num;
			}
		}
		return num;
	}

	/**
	 * number of items that shop will buy or sell & is legal or can buy illegal
	 * @param showIllegal
	 * @return
	 */
	public int getShopSize(boolean showIllegal) {
		int num = priceList.size();
		for (PriceListItem i : priceList) {
			if ((i.buy < 0 && i.sell < 0)
					|| !(showIllegal || i.IsLegal())) {
				--num;
			}
		}
		return num;
	}

	/**
	 *
	 * @param pageNum zero-indexed page to get
	 * @param pageSize size of an output page
	 * @param showIllegal whether to count illegal items
	 * @return
	 */
	public int getShopPageStart(int pageNum, int pageSize, boolean showIllegal) {
		if (pageSize <= 0) {
			pageSize = 1;
		}
		int num = 0, showNum = 0;
		for (PriceListItem i : priceList) {
			if (!((i.buy < 0 && i.sell < 0)
					|| !(showIllegal || i.IsLegal()))) {
				if (showNum / pageSize == pageNum) {
					break;
				}
				++showNum;
			}
			++num;
		}
		return num;
	}

	/**
	 * returns a page of prices
	 * @param pageNum page to lookup (-1 will print all pages)
	 * @param isPlayer if should use minecraft font spacing
	 * @param pageSize how many on a page
	 * @param listing format to output listing with
	 * @param header page header (<page> of <pages>)
	 * @param footer page footer
	 * @return a list of formatted lines
	 * @throws SQLException if using MySQL database & there was some database connection error
	 * @throws Exception some serious error occurred (details in message)
	 */
	public List<String> getShopListPage(int pageNum, boolean isPlayer, int pageSize, String listing, String header, String footer) throws SQLException, Exception {
		return getShopListPage(pageNum, isPlayer, pageSize, listing, header, footer, false, true);
	}

	/**
	 * returns a page of prices
	 * @param pageNum page to lookup (-1 will print all pages)
	 * @param isPlayer if should use minecraft font spacing
	 * @param pageSize how many on a page
	 * @param listing format to output listing with
	 * @param header page header (<page> of <pages>)
	 * @param footer page footer
	 * @param showIllegal whether illegal items should be included in the listing
	 * @param showDec whether to round to whole numbers or show 2 decimal places
	 * @return a list of formatted lines
	 * @throws SQLException if using MySQL database & there was some database connection error
	 * @throws Exception some serious error occurred (details in message)
	 */
	public List<String> getShopListPage(int pageNum, boolean isPlayer, int pageSize, String listing, String header, String footer, boolean showIllegal, boolean showDec) throws SQLException, Exception {
		return getShopListPage(pageNum, isPlayer, pageSize, listing, header, footer, showIllegal, showDec, null);
	}

	/**
	 * returns a page of prices
	 * @param pageNum page to lookup (-1 will print all pages)
	 * @param isPlayer if should use minecraft font spacing
	 * @param pageSize how many on a page
	 * @param listing format to output listing with
	 * @param header page header (<page> of <pages>)
	 * @param footer page footer
	 * @param showIllegal whether illegal items should be included in the listing
	 * @param showDec whether to round to whole numbers or show 2 decimal places
	 * @param stock what to use for stock, if applicable
	 * @return a list of formatted lines
	 * @throws SQLException if using MySQL database & there was some database connection error
	 * @throws Exception some serious error occurred (details in message)
	 */
	public List<String> getShopListPage(int pageNum, boolean isPlayer,
			int pageSize, String listing, String header, String footer,
			boolean showIllegal, boolean showDec, ItemStock stock) throws SQLException, Exception {
		return getShopListPage(pageNum, isPlayer, pageSize, listing,
				header, footer, showIllegal, showDec, stock, 0);
	}

	/**
	 * returns a page of prices
	 * @param pageNum page to lookup (-1 will print all pages)
	 * @param isPlayer if should use minecraft font spacing
	 * @param pageSize how many on a page
	 * @param listing format to output listing with
	 * @param header page header (<page> of <pages>)
	 * @param footer page footer
	 * @param showIllegal whether illegal items should be included in the listing
	 * @param showDec whether to round to whole numbers or show 2 decimal places
	 * @param stock what to use for stock, if applicable
	 * @param discount percentage to mark the price down
	 * @return a list of formatted lines
	 * @throws SQLException if using MySQL database & there was some database connection error
	 * @throws Exception some serious error occurred (details in message)
	 */
	public List<String> getShopListPage(int pageNum, boolean isPlayer,
			int pageSize, String listing, String header, String footer,
			boolean showIllegal, boolean showDec, ItemStock stock, double discount) throws SQLException, Exception {
		LinkedList<String> ret = new LinkedList<String>();
		if (databaseType == DBType.MYSQL && !useCache) {
			updateCache(false);// manually update
		} else {
			updateCache();
		}

		int pricelistsize = getShopSize(showIllegal);//priceList.size();

		int pages = (int) Math.ceil((double) pricelistsize / pageSize);

		int pageStart;
		if (pageNum <= 0) {
			//pageNum = 1;
			pageStart = 0;
			pageSize = pricelistsize;
		} else {
			pageStart = getShopPageStart(pageNum - 1, pageSize, showIllegal);
		}

		String listhead = header == null || header.length() == 0 ? ""
				: header.replace("<page>", pageNum < 0 ? "(All)" : String.valueOf(pageNum)).
				replace("<pages>", String.valueOf(pages));
		if (pageNum > pages) {
			ret.add("There is no page " + pageNum + ". (" + pages + " pages total)");
		} else {
			if (listhead.length() > 0) {
				ret.add(String.format(listhead, pageNum, pages));
			}
			listing = listing.replace("<item>", "%1$s").replace("<buyprice>", "%2$s").replace("<sellprice>", "%3$s").replace("<avail>", "%4$s");
			///for (int i = pageSize * (pageNum - 1), n = 0; n < pageSize && i < priceList.size(); ++i, ++n) {
			for (int i = pageStart, n = 0; n < pageSize && i < priceList.size(); ++i, ++n) {
				PriceListItem it = priceList.get(i);
				if ((!showIllegal && !it.IsLegal())
						|| (it.buy < 0 && it.sell < 0)) {
					--n;
					continue;
				}
				long st = stock != null ? stock.getItemAmount(it) : -1;
				double buy = it.buy,
						sell = it.sell;
				if (discount != 0) {
					buy -= buy * discount;
					sell -= sell * discount;
				}
				ret.add(String.format(listing, it.coloredName(),
						String.format("%5s", buy < 0 ? " No " : (showDec ? String.format("%01.2f", buy) : String.valueOf((int) Math.round(buy)))),
						String.format("%5s", sell < 0 ? " No " : (showDec ? String.format("%01.2f", sell) : String.valueOf((int) Math.round(sell)))),
						(st < 0 ? "INF" : String.valueOf(st))));
			}
			if (footer != null && footer.length() > 0) {
				ret.add(footer);
			}
		}
		if (ret.size() > 2) {
			// format spaces
			return MinecraftChatStr.alignTags(ret, isPlayer);
		}
		return ret;
	}

	public JItem[] getItems() throws SQLException, Exception {
		return (JItem[]) getPricelistItems();
	}

	public JItem[] getItems(boolean showIllegal) throws SQLException, Exception {
		//for(JItem i :((JItem[])getPricelistItems())) System.out.println(i);
		if (showIllegal) {
			return getPricelistItems();
		}
		//else
		return (JItem[]) getPricelistItems(showIllegal);
	}

	public PriceListItem[] getPricelistItems() throws SQLException, Exception {
		if (databaseType == DBType.MYSQL && !useCache) {
			updateCache(false);// manually update
		} else {
			updateCache(true);
		}
		return priceList.toArray(new PriceListItem[0]);
	}

	public PriceListItem[] getPricelistItems(boolean showIllegal) throws SQLException, Exception {
		if (databaseType == DBType.MYSQL && !useCache) {
			// manually update
			priceList.clear();
			priceList.addAll(MySQLpricelist.getFullList());
		} else {
			updateCache();
		}
		ArrayList<PriceListItem> items = new ArrayList<PriceListItem>();
		for (int i = 0; i < priceList.size(); ++i) {
			if (!showIllegal && !priceList.get(i).IsLegal()) {
				continue;
			}
			items.add(priceList.get(i));
		}
		return items.toArray(new PriceListItem[0]);
	}
} // end class PriceList

