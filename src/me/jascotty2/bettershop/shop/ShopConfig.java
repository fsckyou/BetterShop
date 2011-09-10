/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: configuration settings for an individual shop
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

import java.util.List;
import me.jascotty2.bettershop.BetterShop;

/**
 * @author jacob
 */
public class ShopConfig {
	// general

	public String sql_username = "root",
			sql_password = "root",
			sql_database = "minecraft",
			sql_hostName = "localhost",
			sql_portNum = "3306";
	// pricelist
	public String tableName = "BetterShop";
	// transaction logging
	public String transLogTablename = "BetterShopMarketActivity",
			recordTablename = "BetterShopTransactionTotals";
	// stock settings
	public String stockTablename = "BetterShopItemStock";
	//  how much an added item has to start with
	public long startStock = 200;
	// max stock to carry (stock is increased with sales)
	public long maxStock = 500;
	// deny sales if stock is full?
	public boolean noOverStock = true;
	// restock interval.. automatic, and stock will be reset to startStock value
	public long restock = 21600; //6h

	public void set(ShopConfig copy) {
		if (copy == null) {
			set(BetterShop.getConfig().mainShopConfig);
		} else {
			sql_username = copy.sql_username;
			sql_password = copy.sql_password;
			sql_database = copy.sql_database;
			sql_hostName = copy.sql_hostName;
			sql_portNum = copy.sql_portNum;

			tableName = copy.tableName;

			transLogTablename = copy.transLogTablename;
			recordTablename = copy.recordTablename;

			stockTablename = copy.stockTablename;
			startStock = copy.startStock;
			maxStock = copy.maxStock;
			noOverStock = copy.noOverStock;
			restock = copy.restock;
		}
	}

	public List<String> getCustomSort() {
		return BetterShop.getConfig().sortOrder;
	}

	public boolean useDBcaching() {
		return BetterShop.getConfig().useDBCache;
	}

	public int dbCacheTime() {
		return BetterShop.getConfig().tempCacheTTL;
	}

	public long pricelistCacheTime() {
		return BetterShop.getConfig().priceListLifespan;
	}

	public boolean useMySQL() {
		return BetterShop.getConfig().useMySQL();
	}

	public boolean useStock() {
		return BetterShop.getConfig().useItemStock;
	}

	public int getPageSize() {
		return BetterShop.getConfig().pagesize;
	}

	public String getListFormat() {
		return BetterShop.getConfig().getString("listing");
	}

	public String getListHead() {
		return BetterShop.getConfig().getString("listhead");
	}

	public String getListTail() {
		return BetterShop.getConfig().getString("listtail");
	}

	public boolean allowIllegalPurchase() {
		return BetterShop.getConfig().allowbuyillegal;
	}
} // end class ShopConfig

