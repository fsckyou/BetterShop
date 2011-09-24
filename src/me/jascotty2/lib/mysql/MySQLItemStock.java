/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: class for working with a MySQL server
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
package me.jascotty2.lib.mysql;

import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.ItemStockEntry;
import me.jascotty2.lib.bukkit.item.JItemDB;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.LinkedList;
import java.util.List;
import me.jascotty2.lib.util.Str;

public class MySQLItemStock extends MySQL {

	// local copy of current connection info
	private String sql_tableName = "ItemStock";

	public MySQLItemStock(String database, String tableName,
			String username, String password, String hostName, String portNum) throws SQLException, Exception {
		connect(database, username, password, hostName, portNum);
		sql_database = database;
		sql_tableName = tableName;
		// now check & create table
		if (isConnected() && !tableExists(tableName)) {
			createItemStockTable(tableName);
		}
	} // end default constructor

	public MySQLItemStock(MySQL database, String tableName) throws SQLException {
		super(database);
		sql_tableName = tableName;
		// now check & create table
		if (isConnected() && !tableExists(tableName)) {
			createItemStockTable(tableName);
		}
	}

	public final boolean connect(String database, String tableName, String username, String password, String hostName, String portNum) throws SQLException, Exception {
		try {
			if (connect(database, username, password, hostName, portNum)) {
				sql_tableName = tableName;
			}
		} catch (SQLException ex) {
			throw new SQLException("Error connecting to MySQL database", ex);
		} catch (Exception e) {
			throw new Exception("Failed to start database connection", e);
		}
		// now check if table is there
		boolean exst = false;
		try {
			exst = tableExists(tableName);
		} catch (SQLException ex) {
			throw new SQLException("Error while retrieving table list", ex);
		} catch (Exception e) {
			throw new Exception("unexpected database error", e);
		}
		if (!exst) {
			// table does not exist, so create it
			createItemStockTable(tableName);
		}
		return true;
	}

	public ItemStockEntry getItem(String name) throws SQLException, Exception {
		if (isConnected()) {
			try {
				//BetterShopLogger.Log(Level.INFO, String.format("SELECT * FROM %s WHERE NAME='%s';", BetterShop.getConfig().sql_tableName, name));
				ResultSet table = getQuery(String.format(
						"SELECT * FROM `%s` WHERE NAME='%s';", sql_tableName, Str.strTrim(name, 25)));
				if (table.first()) {
					ItemStockEntry ret = new ItemStockEntry();
					ret.itemNum = table.getInt(1);
					ret.itemSub = table.getByte(2);
					ret.name = table.getString(3);
					ret.amount = table.getLong(4);
					return ret;
				}
			} catch (SQLException ex) {
				throw new SQLException("Error executing SELECT on " + sql_tableName, ex);
			}
		} else {
			throw new Exception("Is Not connected to database: not checking for item");
		}
		return null;
	}

	public ItemStockEntry getItem(JItem item) throws SQLException, Exception {
		return getItem(item.ID(), (int) item.Data());
	}

	public ItemStockEntry getItem(int id, byte dat) throws SQLException, Exception {
		return getItem(id, (int) dat);
	}

	public ItemStockEntry getItem(int id, int dat) throws SQLException, Exception {
		if (isConnected()) {
			try {
				//BetterShopLogger.Log(Level.INFO, String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", BetterShop.getConfig().sql_tableName, (int)Math.floor(item), (int) Math.round((item - Math.floor(item)) * 100.)));
				ResultSet table = getQuery(
						String.format("SELECT * FROM `%s` WHERE ID='%d' AND SUB='%d';", sql_tableName,
						id, dat));
				if (table.first()) {
					ItemStockEntry ret = new ItemStockEntry();
					ret.itemNum = table.getInt(1);
					ret.itemSub = table.getByte(2);
					ret.name = table.getString(3);
					ret.amount = table.getLong(4);
					return ret;
				}
			} catch (SQLException ex) {
				throw new SQLException("Error executing SELECT on " + sql_tableName, ex);
			}
		} else {
			throw new Exception("Is Not connected to database: not checking for item");
		}
		return null;
	}

	public boolean setAmount(String itemName, long amt) throws SQLException {
		if (itemExists(itemName)) {
			try {
				runUpdate(
						String.format("UPDATE `%s` SET AMT='%d' WHERE NAME='%s';",
						sql_tableName, amt, Str.strTrim(itemName, 25)));
				/* or:
				String.format("UPDATE %s SET BUY=%f1.2, SELL=%f1.2 WHERE ID='%d' AND SUB='%d';",
				buy, sell, itemInfo.getItemTypeId(), itemInfo.getData())).executeUpdate();
				 */
				return true;
			} catch (SQLException ex) {
				throw new SQLException("Error executing UPDATE on " + sql_tableName, ex);
			}
		} else {
			JItem toAdd = JItemDB.findItem(itemName);
			if (toAdd != null) {
				try {
					runUpdate(
							String.format("INSERT INTO `%s` VALUES(%d, %d, '%s', '%d');", sql_tableName,
							toAdd.ID(), toAdd.Data(), Str.strTrim(toAdd.Name(), 25), amt));
					return true;
				} catch (SQLException ex) {
					throw new SQLException("Error executing INSERT on " + sql_tableName, ex);
				}
			}
		}
		return false;
	}

	public void setAmount(JItem item, long amt) throws SQLException { //
		if (item != null) {
			setAmount(item.ID(), item.Data(), item.Name(), amt);
		}
	}

	public void setAmount(int id, byte dat, String name, long amt) throws SQLException { //
		if (itemExists(id, dat)) {
			try {
				//logger.log(Level.INFO, String.format("UPDATE %s SET BUY=%1.2f, SELL=%1.2f WHERE ID='%d' AND SUB='%d';", sql_tableName, buy, sell, item.itemId, (int) item.itemData));
				runUpdate(
						String.format("UPDATE `%s` SET AMT=%d WHERE ID='%d' AND SUB='%d';", sql_tableName,
						amt, id, dat));
				//return true;
			} catch (SQLException ex) {
				throw new SQLException("Error executing UPDATE on " + sql_tableName, ex);
			}
		} else {
			//JItem toAdd = JItem.findItem(item);
			// assuming item is valid
			try {
				//logger.log(Level.INFO, String.format("INSERT INTO %s VALUES(%d, %d, '%s', %1.2f, %1.2f);", sql_tableName, item.itemId, (int)item.itemData, item.name, buy, sell)
				runUpdate(
						String.format("INSERT INTO `%s` VALUES(%d, %d, '%s', %d);", sql_tableName,
						id, dat, Str.strTrim(name, 25), amt));
				//return true;
			} catch (SQLException ex) {
				throw new SQLException("Error executing INSERT on " + sql_tableName, ex);
			}
		}
	}

	public boolean itemExists(String item) throws SQLException {
		if (isConnected()) {
			try {
				//logger.log(Level.INFO, String.format("SELECT * FROM %s WHERE NAME='%s';", sql_tableName, item));
				ResultSet table = getQuery(
						String.format("SELECT * FROM `%s` WHERE NAME='%s';", sql_tableName, Str.strTrim(item, 25)));
				return table.first();
			} catch (SQLException ex) {
				throw new SQLException("Error executing SELECT on " + sql_tableName, ex);
			}
		}
		return false;
	}

	public boolean itemExists(JItem item) throws SQLException {
		return item == null ? false : itemExists(item.ID(), item.Data());
	}

	public boolean itemExists(int id, byte dat) throws SQLException {
		if (isConnected()) {
			try {
				//BetterShopLogger.Log(Level.INFO, String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", sql_tableName, (int) Math.floor(item), (int) Math.round((item - Math.floor(item))* 100)));
				ResultSet table = getQuery(
						String.format("SELECT * FROM `%s` WHERE ID='%d' AND SUB='%d';", sql_tableName,
						id, (int) dat));
				//BetterShopLogger.Log(Level.INFO, table.first()?"true":"false");
				return table.first();
			} catch (SQLException ex) {
				throw new SQLException("Error executing SELECT on " + sql_tableName, ex);
			}
		}
		return false;
	}

	public boolean removeItem(String item) throws SQLException {
		if (isConnected()) {
			try {
				runUpdate(String.format(
						"DELETE FROM `%s` WHERE NAME='%s';", sql_tableName, Str.strTrim(item, 25)));
			} catch (SQLException ex) {
				throw new SQLException("Error executing DELETE on " + sql_tableName, ex);
			}
		}
		return false;
	}

	public boolean removeItem(JItem item) throws SQLException {
		if (isConnected()) {
			try {
				runUpdate(String.format(
						"DELETE FROM `%s` WHERE ID='%d' AND SUB='%d';", sql_tableName, item.ID(), (int) item.Data()));
			} catch (SQLException ex) {
				throw new SQLException("Error executing DELETE on " + sql_tableName, ex);
			}
		}
		return false;
	}

	public boolean clearDB() throws SQLException {
		if (isConnected()) {
			try {
				runUpdate(String.format(
						"DELETE FROM `%s`;", sql_tableName));
			} catch (SQLException ex) {
				throw new SQLException("Error executing DELETE on " + sql_tableName, ex);
			}
		}
		return false;
	}

	public List<ItemStockEntry> getFullList() throws SQLException, Exception {
		LinkedList<ItemStockEntry> tableDat = new LinkedList<ItemStockEntry>();
		if (isConnected()) {
			try {
				// Statement to use to issue SQL queries
				//Statement st = DBconnection.createStatement();
				//ResultSet table = st.executeQuery("SELECT * FROM " + sql_tableName + ";");
				//PreparedStatement st=
				//ResultSet table = DBconnection.prepareStatement("SELECT * FROM " + sql_tableName + ";").executeQuery();
				ResultSet table = getQuery(
						"SELECT * FROM `" + sql_tableName + "`  ORDER BY ID, SUB;");
				//BetterShopLogger.Log(Level.INFO, "Table selected: ");
				for (table.beforeFirst(); table.next();) {
					//BetterShopLogger.Log(Level.INFO, table.getString(3));//
					tableDat.add(new ItemStockEntry(table.getInt(1), table.getByte(2),
							table.getString(3), table.getLong(4)));
				}
			} catch (SQLException ex) {
				throw new SQLException("Error executing SELECT on " + sql_tableName, ex);
			}
		} else {
			throw new Exception("Error: MySQL DB not connected");
		}
		return tableDat;
	}

	private boolean createItemStockTable(String tableName) throws SQLException {
		if (!isConnected() || tableName.contains(" ")) {
			return false;
		}
		try {
			runUpdate("CREATE TABLE `" + tableName
					+ "`(ID    INTEGER  NOT NULL,"
					+ "SUB   TINYINT  NOT NULL,"
					+ "NAME  VARCHAR(25) NOT NULL,"
					+ "AMT   BIGINT NOT NULL,"
					+ "PRIMARY KEY (ID, SUB));");
		} catch (SQLException e) {
			throw new SQLException("Error while creating table", e);
		}
		return true;
	}

	public String getTableName() {
		return sql_tableName;
	}
} // end class MySQLPriceList

