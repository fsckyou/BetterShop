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
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.item.PriceListItem;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import me.jascotty2.lib.util.Str;

public class MySQLPriceList extends MySQL {

	// local copy of current connection info
	private String sql_tableName = "PriceList";

	public MySQLPriceList(String database, String tableName,
			String username, String password, String hostName, String portNum) throws SQLException, Exception {
		connect(database, tableName, username, password, hostName, portNum);
	} // end default constructor

	public final boolean connect(String database, String tableName,
			String username, String password, String hostName, String portNum) throws SQLException, Exception {
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
		try {
			if (!tableExists(tableName)) {
				// table does not exist, so create it
				createPricelistTable(tableName);
			}
		} catch (SQLException ex) {
			throw new SQLException("Error while retrieving table list", ex);
		} catch (Exception e) {
			throw new Exception("unexpected database error", e);
		}
		return true;
	}

	public PriceListItem getItem(String name) throws SQLException, Exception {
		if (isConnected()) {
			try {
				//BetterShopLogger.Log(Level.INFO, String.format("SELECT * FROM %s WHERE NAME='%s';", BetterShop.getConfig().sql_tableName, name));
				ResultSet table = getQuery(String.format(
						"SELECT * FROM `%s` WHERE NAME='%s';", sql_tableName, Str.strTrim(name, 25)));
				if (table.first()) {
					return new PriceListItem(
							table.getInt(1), table.getByte(2), table.getString(3),
							table.getDouble(4), table.getDouble(5));
				}
			} catch (SQLException ex) {
				throw new SQLException("Error executing SELECT on " + sql_tableName, ex);
			}
		} else {
			throw new Exception("Is Not connected to database: not checking for item");
		}
		return null;
	}

	public PriceListItem getItem(JItem item) throws SQLException, Exception {
		return item != null ? getItem(item.ID(), (byte) item.Data()) : null;
	}

	public PriceListItem getItem(int id, byte dat) throws SQLException, Exception {
		if (isConnected()) {
			try {
				ResultSet table = getQuery(
						String.format("SELECT * FROM `%s` WHERE ID='%d' AND SUB='%d';", sql_tableName,
						id, (int) dat));
				if (table.first()) {
					return new PriceListItem(
							table.getInt(1), table.getByte(2), table.getString(3),
							table.getDouble(4), table.getDouble(5));
				}
			} catch (SQLException ex) {
				throw new SQLException("Error executing SELECT on " + sql_tableName, ex);
			}
		} else {
			throw new Exception("Is Not connected to database: not checking for item");
		}
		return null;
	}

	public boolean setPrice(String itemName, double buy, double sell) throws SQLException {
		if (itemExists(itemName)) {
			try {
				runUpdate(
						String.format(Locale.US, "UPDATE `%s` SET BUY='%1.2f', SELL='%1.2f' WHERE NAME='%s';", sql_tableName,
						buy, sell, Str.strTrim(itemName, 25)));
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
							String.format(Locale.US, "INSERT INTO `%s` VALUES(%d, %d, '%s', '%1.2f', '%1.2f');", sql_tableName,
							toAdd.ID(), toAdd.Data(), Str.strTrim(toAdd.Name(), 25), buy, sell));
					return true;
				} catch (SQLException ex) {
					throw new SQLException("Error executing INSERT on " + sql_tableName, ex);
				}
			}
		}
		return false;
	}

	public void setPrice(JItem item, double buy, double sell) throws SQLException { //
		if (itemExists(item)) {
			try {
				//java.util.logging.Logger.getAnonymousLogger().info(String.format("UPDATE %s SET BUY=%1.2f, SELL=%1.2f WHERE ID='%d' AND SUB='%d';", sql_tableName, buy, sell, item.ID(), (int) item.Data()));
				runUpdate(
						String.format(Locale.US, "UPDATE `%s` SET BUY='%1.2f', SELL='%1.2f' WHERE ID='%d' AND SUB='%d';", sql_tableName,
						buy, sell, item.ID(), (int) item.Data()));
			} catch (SQLException ex) {
				throw new SQLException("Error executing UPDATE on " + sql_tableName, ex);
			}
		} else {
			// assuming item is valid
			try {
				//java.util.logging.Logger.getAnonymousLogger().info(String.format("INSERT INTO %s VALUES(%d, %d, '%s', '%1.2f', '%1.2f');", sql_tableName, item.ID(), (int) item.Data(), item.Name(), buy, sell));
				runUpdate(
						String.format(Locale.US, "INSERT INTO `%s` VALUES(%d, %d, '%s', '%1.2f', '%1.2f');", sql_tableName,
						item.ID(), (int) item.Data(), Str.strTrim(item.Name(), 25), buy, sell));
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
						String.format("SELECT * FROM `%s` WHERE NAME='%s';", sql_tableName, Str.strTrim(item, 25) ));
				return table.first();
			} catch (SQLException ex) {
				throw new SQLException("Error executing SELECT on " + sql_tableName, ex);
			}
		}
		return false;
	}

	public boolean itemExists(JItem item) throws SQLException {
		if (isConnected()) {
			try {
				//BetterShopLogger.Log(Level.INFO, String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", sql_tableName, (int) Math.floor(item), (int) Math.round((item - Math.floor(item))* 100)));
				ResultSet table = getQuery(
						String.format("SELECT * FROM `%s` WHERE ID='%d' AND SUB='%d';", sql_tableName,
						item.ID(), (int) item.Data()));
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

	public void removeAll() throws SQLException {
		if (isConnected()) {
			try {
				runUpdate(String.format(
						"DELETE FROM `%s`;", sql_tableName));
			} catch (SQLException ex) {
				throw new SQLException("Error executing DELETE on " + sql_tableName, ex);
			}
		}
	}

	public List<PriceListItem> getFullList() throws SQLException, Exception {
		LinkedList<PriceListItem> tableDat = new LinkedList<PriceListItem>();
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
					tableDat.add(new PriceListItem(table.getInt(1), table.getByte(2),
							table.getString(3), table.getDouble(4), table.getDouble(5)));
				}
			} catch (SQLException ex) {
				throw new SQLException("Error executing SELECT on " + sql_tableName, ex);
			}
		} else {
			throw new Exception("Error: MySQL DB not connected");
		}
		return tableDat;
	}

	private boolean createPricelistTable(String tableName) throws SQLException {
		if (!isConnected() || tableName.contains(" ")) {
			return false;
		}
		try {
			runUpdate("CREATE TABLE `" + sql_database + "`.`" + tableName
					+ "`(ID    INTEGER  NOT NULL,"
					+ "SUB   TINYINT  NOT NULL,"
					+ "NAME  VARCHAR(25) NOT NULL,"
					+ "BUY   DECIMAL(11,2),"
					+ "SELL  DECIMAL(11,2),"
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

