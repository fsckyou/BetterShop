/**
 * Programmer: Jacob Scott
 * Email: jascottytechie at gmail.com
 * Program Name: BSMySQL
 * Description: class for working with a MySQL server
 * Date: Mar 8, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.MySQL.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.LinkedList;
import java.util.logging.Level;
import org.bukkit.material.MaterialData;

public class BSMySQL {

    // local copy of current connection info
    private String sql_database = "minecraft";
    // DB connection
    public static MySQL MySQLdatabase = new MySQL();

    public BSMySQL() {
        connect();
    }

    public BSMySQL(String database, String tableName, String username, String password, String hostName, String portNum) {
        try {
            MySQLdatabase.connect(database, username, password, hostName, portNum);
            sql_database = database;
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, "Error connecting to MySQL database", ex);
        }
    } // end default constructor

    public final boolean connect() {
        try {
            MySQLdatabase.connect(BetterShop.config.sql_database,
                    BetterShop.config.sql_username,
                    BetterShop.config.sql_password,
                    BetterShop.config.sql_hostName,
                    BetterShop.config.sql_portNum);
            sql_database = BetterShop.config.sql_database;
        } catch (SQLException ex) {
            BetterShop.Log(Level.SEVERE, "Error connecting to MySQL database", ex);
        } catch (Exception e) {
            BetterShop.Log(Level.SEVERE, "Failed to start database connection", e);
            return false;
        }
        // now check if table is there
        try {
            if (!MySQLdatabase.tableExists(BetterShop.config.sql_tableName)) {
                // table does not exist, so create it
                createPricelistTable(BetterShop.config.sql_tableName);
            }
            if (!MySQLdatabase.tableExists(BetterShop.config.sql_tableName)) {
                createPricelistTable(BetterShop.config.sql_tableName);
            }
        } catch (SQLException ex) {
            BetterShop.Log(Level.SEVERE, "Error while retrieving table list", ex);
            return false;
        } catch (Exception e) {
            BetterShop.Log(Level.SEVERE, "unexpected database error", e);
            return false;
        }
        return true;
    }

    public void disconnect() {
        MySQLdatabase.disconnect();
    }

    // manually force database to save
    public void commit() {
        if (MySQLdatabase.IsConnected()) {
            try {
                MySQLdatabase.commit();
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "failed to commit database", ex);
            }
        }
    }

    public PriceListItem GetItem(String name) {
        PriceListItem ret = new PriceListItem();
        if (MySQLdatabase.IsConnected()) {
            try {
                //BetterShop.Log(Level.INFO, String.format("SELECT * FROM %s WHERE NAME='%s';", sql_tableName, name));
                ResultSet table = MySQLdatabase.GetQuery(String.format(
                        "SELECT * FROM %s WHERE NAME='%s';", BetterShop.config.sql_tableName, name));
                if (table.first()) {
                    ret.itemNum = table.getInt(1);
                    ret.itemSub = table.getInt(2);
                    ret.name = table.getString(3);
                    ret.buy = table.getDouble(4);
                    ret.sell = table.getDouble(5);
                }
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error executing SELECT on " + BetterShop.config.sql_tableName, ex);
                disconnect();
            }
        } else {
            BetterShop.Log(Level.WARNING, "Is Not connected to database: not checking for item");
        }
        return ret;
    }

    public PriceListItem GetItem(double item) {
        PriceListItem ret = new PriceListItem();
        if (MySQLdatabase.IsConnected()) {
            try {
                //BetterShop.Log(Level.INFO, String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", sql_tableName, (int)Math.floor(item), (int) Math.round((item - Math.floor(item)) * 100.)));
                ResultSet table = MySQLdatabase.GetQuery(
                        String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", BetterShop.config.sql_tableName,
                        (int) Math.floor(item), (int) Math.round((item - Math.floor(item)) * 100.)));
                if (table.first()) {
                    ret.itemNum = table.getInt(1);
                    ret.itemSub = table.getInt(2);
                    ret.name = table.getString(3);
                    ret.buy = table.getDouble(4);
                    ret.sell = table.getDouble(5);
                }
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error executing SELECT on " + BetterShop.config.sql_tableName, ex);
                disconnect();
            }
        } else {
            BetterShop.Log(Level.WARNING, "Is Not connected to database: not checking for item");
        }
        return ret;
    }

    // don't normally like to throw exceptions, but that was easiest to add here :(
    public boolean SetPrice(String item, double buy, double sell) { //  throws Exception
        if (ItemExists(item)) {
            try {
                MySQLdatabase.RunUpdate(
                        String.format("UPDATE %s SET BUY=%1.2f, SELL=%1.2f WHERE NAME='%s';", BetterShop.config.sql_tableName,
                        buy, sell, item));
                /* or:
                String.format("UPDATE %s SET BUY=%f1.2, SELL=%f1.2 WHERE ID='%d' AND SUB='%d';",
                buy, sell, itemInfo.getItemTypeId(), itemInfo.getData())).executeUpdate();
                 */
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error executing UPDATE on " + BetterShop.config.sql_tableName, ex);
            }
        } else {
            MaterialData itemInfo;
            try {
                itemInfo = itemDb.get(item);
            } catch (Exception ex) {
                //BetterShop.Log(Level.SEVERE, "Error looking up \"item\"", ex);
                return false;
            }
            try {
                MySQLdatabase.RunUpdate(
                        String.format("INSERT INTO %s VALUES(%d, %d, '%s', %1.2f, %1.2f);", BetterShop.config.sql_tableName,
                        itemInfo.getItemTypeId(), itemInfo.getData(), item, buy, sell));
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error executing INSERT on " + BetterShop.config.sql_tableName, ex);
            }
        }
        return true;
    }

    public boolean SetPrice(double item, double buy, double sell) { // throws Exception
        if (ItemExists(item)) {
            try {
                //BetterShop.Log(Level.INFO, String.format("UPDATE %s SET BUY=%1.2f, SELL=%1.2f WHERE ID='%d' AND SUB='%d';", sql_tableName, buy, sell, (int) Math.floor(item), (int) Math.round((item - Math.floor(item))* 100.) ));
                MySQLdatabase.RunUpdate(
                        String.format("UPDATE %s SET BUY=%1.2f, SELL=%1.2f WHERE ID='%d' AND SUB='%d';", BetterShop.config.sql_tableName,
                        buy, sell, (int) Math.floor(item), (int) Math.round((item - Math.floor(item)) * 100.)));
                /* or:
                String.format("UPDATE %s SET BUY=%f1.2, SELL=%f1.2 WHERE ID='%d' AND SUB='%d';",
                buy, sell, itemInfo.getItemTypeId(), itemInfo.getData())).executeUpdate();
                 */
                return true;
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error executing UPDATE on " + BetterShop.config.sql_tableName, ex);
            }
        } else {
            try {
                //BetterShop.Log(Level.INFO, String.format("INSERT INTO %s VALUES(%d, %f, '%s', %1.2f, %1.2f);", sql_tableName, (int) Math.floor(item), Math.round((item - Math.floor(item))*100)  , itemDb.getName(item), buy, sell));
                MySQLdatabase.RunUpdate(
                        String.format("INSERT INTO %s VALUES(%d, %d, '%s', %1.2f, %1.2f);", BetterShop.config.sql_tableName,
                        (int) Math.floor(item), (int) Math.round((item - Math.floor(item)) * 100.), itemDb.getName(item), buy, sell));
                return true;
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error executing INSERT on " + BetterShop.config.sql_tableName, ex);
            } catch (Exception ex) {
                //BetterShop.Log(Level.SEVERE, "Error looking up item " + item, ex);
            }
        }
        return false;
    }

    public boolean ItemExists(String item) {
        if (MySQLdatabase.IsConnected()) {
            try {
                //BetterShop.Log(Level.INFO, String.format("SELECT * FROM %s WHERE NAME='%s';", sql_tableName, item));
                ResultSet table = MySQLdatabase.GetQuery(
                        String.format("SELECT * FROM %s WHERE NAME='%s';", BetterShop.config.sql_tableName, item));
                return table.first();
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error executing SELECT on " + BetterShop.config.sql_tableName, ex);
                disconnect();
            }
        }
        return false;
    }

    public boolean ItemExists(double item) {
        if (MySQLdatabase.IsConnected()) {
            try {
                //BetterShop.Log(Level.INFO, String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", sql_tableName, (int) Math.floor(item), (int) Math.round((item - Math.floor(item))* 100)));
                ResultSet table = MySQLdatabase.GetQuery(
                        String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", BetterShop.config.sql_tableName,
                        (int) Math.floor(item), (int) Math.round((item - Math.floor(item)) * 100.)));
                //BetterShop.Log(Level.INFO, table.first()?"true":"false");
                return table.first();
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error executing SELECT on " + BetterShop.config.sql_tableName, ex);
                disconnect();
            }
        }
        return false;
    }

    public boolean RemoveItem(String item) {
        if (MySQLdatabase.IsConnected()) {
            try {
                MySQLdatabase.RunUpdate(String.format(
                        "DELETE FROM %s WHERE NAME='%s';", BetterShop.config.sql_tableName, item));
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error executing DELETE on " + BetterShop.config.sql_tableName, ex);
                disconnect();
            }
        }
        return false;
    }

    public LinkedList<PriceListItem> GetFullList() {
        LinkedList<PriceListItem> tableDat = new LinkedList<PriceListItem>();
        if (MySQLdatabase.IsConnected()) {
            try {
                // Statement to use to issue SQL queries
                //Statement st = DBconnection.createStatement();
                //ResultSet table = st.executeQuery("SELECT * FROM " + sql_tableName + ";");
                //PreparedStatement st=
                //ResultSet table = DBconnection.prepareStatement("SELECT * FROM " + sql_tableName + ";").executeQuery();
                ResultSet table = MySQLdatabase.GetQuery(
                        "SELECT * FROM " + BetterShop.config.sql_tableName + "  ORDER BY ID, SUB;");
                //BetterShop.Log(Level.INFO, "Table selected: ");
                for (table.beforeFirst(); table.next();) {
                    //BetterShop.Log(Level.INFO, table.getString(3));//
                    tableDat.add(new PriceListItem(table.getInt(1), table.getInt(2), table.getString(3),
                            table.getDouble(4), table.getDouble(5)));
                }
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error executing SELECT on " + BetterShop.config.sql_tableName, ex);
            }
        } else {
            BetterShop.Log(Level.INFO, "Error: DB not connected");
        }
        return tableDat;
    }

    public boolean IsConnected() {
        return MySQLdatabase.IsConnected();
    }

    protected boolean createPricelistTable(String tableName) {
        if (!MySQLdatabase.IsConnected() || tableName.contains(" ")) {
            return false;
        }
        try {
            MySQLdatabase.RunUpdate("CREATE TABLE " + sql_database + "." + tableName
                + "(ID    INTEGER  NOT NULL,"
                + "SUB   INTEGER  NOT NULL,"
                + "NAME  VARCHAR(25) NOT NULL,"
                + "BUY   DECIMAL(6,2),"
                + "SELL  DECIMAL(6,2),"
                + "PRIMARY KEY (ID, SUB));");
        } catch (SQLException e) {
            BetterShop.Log(Level.SEVERE, "Error while creating table", e);
            return false;
        }
        return true;
    }
} // end class BSMySQL

