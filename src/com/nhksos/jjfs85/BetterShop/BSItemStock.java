/**
 * Programmer: Jacob Scott
 * Program Name: BSItemStock
 * Description: provides options for items to have stock
 * Date: Mar 18, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.Item.JItem;
import com.jascotty2.Shop.ItemStock;
import com.jascotty2.Item.ItemStockEntry;
import com.jascotty2.MySQL.MySQLItemStock;
import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;

public class BSItemStock extends ItemStock {

	protected Date lastStock;

	public BSItemStock() {
		super();
	} // end default constructor

	public final boolean load() {
		lastStock = new Date();
		useCache = BetterShop.config.useDBCache;
		dbCacheTTL = BetterShop.config.priceListLifespan;
		if (BetterShop.config.useMySQL()) {
			databaseType = DBType.MYSQL;
			try {
				MySQLstockList = new MySQLItemStock(
						BetterShop.pricelist.getMySQLconnection(),
						BetterShop.config.stockTablename);
				if (MySQLstockList != null && MySQLstockList.IsConnected()) {
					return checkMissingStock();
				}
			} catch (SQLException ex) {
				BetterShop.Log(Level.SEVERE, "Failed to connect to MySQL database connection...", ex);
			}
		} else {
			databaseType = DBType.FLATFILE;
			try {
				//System.out.println("attempting FlatFile: " + BSConfig.pluginFolder.getPath() + File.separatorChar + BetterShop.config.tableName + ".csv");
				if (loadFile(new File(BSConfig.pluginFolder.getPath() + File.separatorChar
						+ BetterShop.config.stockTablename + ".csv"))) {
					BetterShop.Log(Level.INFO, BetterShop.config.stockTablename + ".csv loaded.");
					return checkMissingStock();
				}
			} catch (Exception ex) {
				BetterShop.Log(Level.SEVERE, ex);
			}
			BetterShop.Log(Level.SEVERE, "Failed to load pricelist database " + BetterShop.config.stockTablename + ".csv", false);
		}
		return false;
	}

	/**
	 * checks for items not in stocklist & gives then the default amount
	 */
	public boolean checkMissingStock() {
		try {
			JItem[] prices = BetterShop.pricelist.getItems();
			for (JItem i : prices) {
				if (stockList.indexOf(i) == -1) {
					setItemAmount(i, BetterShop.config.startStock);
				}
			}
			return true;
		} catch (Exception ex) {
			BetterShop.Log(Level.SEVERE, ex);
		}
		return false;
	}

	public void Restock(boolean forced) {
		if (!forced) {
			checkStockRestock();
		} else {
			try {
				lastStock = new Date();
				JItem[] prices = BetterShop.pricelist.getItems();
				for (JItem i : prices) {
					setItemAmount(i, BetterShop.config.startStock);
				}
			} catch (Exception ex) {
				BetterShop.Log(Level.SEVERE, ex);
			}
		}
	}

	public void checkStockRestock() {
		if (BetterShop.config.useItemStock && BetterShop.config.restock > 0
				&& ((new Date()).getTime() - lastStock.getTime()) / 1000 > BetterShop.config.restock) {
			Restock(true);
		}
	}

	public long freeStockRemaining(JItem check) {
		return check != null ? freeStockRemaining(check.ID(), check.Data()) : -1;
	}

	public long freeStockRemaining(int id, byte dat) {
		if (BetterShop.config.useItemStock) {
			try {
				long st = BetterShop.stock.getItemAmount(id, dat);
				if (st < 0) {
					return -1;
				} else if (BetterShop.config.noOverStock) {
					return BetterShop.config.maxStock - st;
				} else {
					return Long.MAX_VALUE - st;
				}
			} catch (Exception ex) {
				BetterShop.Log(Level.SEVERE, ex);
			}
		}
		return -1;
	}

	@Override
	public void changeItemAmount(JItem it, long delta) throws SQLException, Exception {
		if (it == null || !BetterShop.config.useItemStock || BetterShop.stock.getItemAmount(it) < 0) {
			return;
		}
		ItemStockEntry itp = getItemEntry(it);
		if (itp != null) {
			delta += itp.amount;
			if (delta >= BetterShop.config.maxStock) {
				setItemAmount(it, BetterShop.config.maxStock);
			} else if (delta <= 0) {
				setItemAmount(it, 0);
			} else {
				//System.out.println("new amount: " + delta);
				setItemAmount(it, delta);//itp.amount + delta);
			}
		}
	}
} // end class BSItemStock

