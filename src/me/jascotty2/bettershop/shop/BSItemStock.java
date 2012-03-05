/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: provides options for items to have stock
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

package me.jascotty2.bettershop.shop;

import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.shop.ItemStock;
import me.jascotty2.lib.bukkit.item.ItemStockEntry;
import me.jascotty2.lib.mysql.MySQLItemStock;
import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import me.jascotty2.bettershop.BSConfig;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.lib.bukkit.item.JItemDB;
import org.bukkit.inventory.ItemStack;

public class BSItemStock extends ItemStock {

	protected Date lastStock;
	final Shop shop;
	private BSPriceList pricelist;

	public BSItemStock(Shop shop) {
		super();
		this.shop = shop;
		this.pricelist = shop.pricelist;
	} // end default constructor

	public final boolean load() {
		lastStock = new Date();
		useCache = BetterShop.getSettings().useDBCache;
		dbCacheTTL = BetterShop.getSettings().priceListLifespan;
		if (BetterShop.getSettings().useMySQL()) {
			databaseType = DBType.MYSQL;
			try {
				MySQLstockList = new MySQLItemStock(
						pricelist.getMySQLconnection(),
						pricelist.config.stockTablename);
				if (MySQLstockList != null && MySQLstockList.isConnected()) {
					return checkMissingStock();
				}
			} catch (SQLException ex) {
				BetterShopLogger.Log(Level.SEVERE, "Failed to connect to MySQL database connection...", ex);
			}
		} else {
			databaseType = DBType.FLATFILE;
			try {
				//System.out.println("attempting FlatFile: " + BSConfig.pluginFolder.getPath() + File.separatorChar + BetterShop.getConfig().tableName + ".csv");
				if (loadFile(new File(BSConfig.pluginFolder.getPath() + File.separatorChar
						+ pricelist.config.stockTablename + ".csv"))) {
					BetterShopLogger.Log(Level.INFO, pricelist.config.stockTablename + ".csv loaded.");
					return checkMissingStock();
				}
			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
			}
			BetterShopLogger.Log(Level.SEVERE, "Failed to load pricelist database " 
					+ pricelist.config.stockTablename + ".csv", false);
		}
		return false;
	}

	/**
	 * checks for items not in stocklist & gives then the default amount
	 * @return false if there was an error while adding new items
	 */
	public boolean checkMissingStock() {
		try {
			JItem[] prices = pricelist.getItems();
			for (JItem i : prices) {
				if (stockList.indexOf(i) == -1) {
					setItemAmount(i, pricelist.config.startStock);
				}
			}
			return true;
		} catch (Exception ex) {
			BetterShopLogger.Log(Level.SEVERE, ex);
		}
		return false;
	}

	public void restock(boolean forced) {
		if (!forced) {
			checkStockRestock();
		} else {
			try {
				lastStock = new Date();
				JItem[] prices = pricelist.getItems();
				for (JItem i : prices) {
					setItemAmount(i, pricelist.config.startStock);
				}
			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
			}
		}
	}

	public void checkStockRestock() {
		if (pricelist.config.useStock() && pricelist.config.restock > 0
				&& ((new Date()).getTime() - lastStock.getTime()) / 1000 > pricelist.config.restock) {
			restock(true);
		}
	}

	public long freeStockRemaining(ItemStack check) {
		JItem c = JItemDB.GetItem(check);
		return c != null ? freeStockRemaining(c.ID(), (byte) c.Data()) : -1;
	}

	public long freeStockRemaining(JItem check) {
		return check != null ? freeStockRemaining(check.ID(), (byte) check.Data()) : -1;
	}

	public long freeStockRemaining(int id, byte dat) {
		if (BetterShop.getSettings().useItemStock) {
			try {
				long st = getItemAmount(id, dat);
				if (st < 0) {
					return -1;
				} else if (pricelist.config.noOverStock) {
					return pricelist.config.maxStock - st;
				} else {
					return Long.MAX_VALUE - st;
				}
			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
			}
		}
		return -1;
	}

	@Override
	public void changeItemAmount(JItem it, long delta) throws SQLException, Exception {
		if (it == null || !pricelist.config.useStock() || getItemAmount(it) < 0) {
			return;
		}
		ItemStockEntry itp = getItemEntry(it);
		if (itp != null) {
			delta += itp.amount;
			if (delta >= pricelist.config.maxStock) {
				setItemAmount(it, pricelist.config.maxStock);
			} else if (delta <= 0) {
				setItemAmount(it, 0);
			} else {
				//System.out.println("new amount: " + delta);
				setItemAmount(it, delta);//itp.amount + delta);
			}
		}
	}
} // end class BSItemStock

