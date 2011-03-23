/**
 * Programmer: Jacob Scott
 * Email: jascottytechie at gmail.com
 * Program Name: MySQLItemStock
 * Description: class for working with a MySQL server
 * Date: Mar 8, 2011
 */
package com.jascotty2.MySQL;

import com.jascotty2.Item.Item;
import com.jascotty2.Item.ItemStockEntry;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.LinkedList;

public class MySQLItemStock {

    // local copy of current connection info
    private String sql_database = "minecraft", sql_tableName = "ItemStock";
    // DB connection
    public MySQL MySQLdatabase = new MySQL();
    
    public MySQLItemStock(String database, String tableName, String username, String password, String hostName, String portNum) throws SQLException, Exception {

        MySQLdatabase.connect(database, username, password, hostName, portNum);
        sql_database = database;
        sql_tableName = tableName;
        // now check & create table
        if(!MySQLdatabase.tableExists(tableName)){
            createItemStockTable(tableName);
        }
    } // end default constructor
    public MySQLItemStock(MySQL database, String tableName) throws SQLException {
        MySQLdatabase = database;
        sql_tableName = tableName;
        // now check & create table
        if(!MySQLdatabase.tableExists(tableName)){
            createItemStockTable(tableName);
        }
    }
    public MySQL getMySQLconnection(){
        return MySQLdatabase;
    }
    
    public final boolean connect() throws SQLException, Exception {
        if (MySQLdatabase == null) {
            return false;
        }
        return MySQLdatabase.connect();
    }

    public final boolean connect(String database, String tableName, String username, String password, String hostName, String portNum) throws SQLException, Exception {
        try {
            MySQLdatabase.connect(database, username, password, hostName, portNum);
            sql_database = database;
            sql_tableName = tableName;
        } catch (SQLException ex) {
            throw new SQLException("Error connecting to MySQL database", ex);
        } catch (Exception e) {
            throw new Exception("Failed to start database connection", e);
        }
        // now check if table is there
        boolean exst = false;
        try {
            exst = MySQLdatabase.tableExists(tableName);
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

    public void disconnect() {
        MySQLdatabase.disconnect();
    }

    // manually force database to save
    public void commit() throws SQLException {
        if (MySQLdatabase.IsConnected()) {
            try {
                MySQLdatabase.commit();
            } catch (SQLException ex) {
                throw new SQLException("failed to run COMMIT on database", ex);
            }
        }
    }

    public ItemStockEntry GetItem(String name) throws SQLException, Exception {
        if (MySQLdatabase.IsConnected()) {
            try {
                //BetterShop.Log(Level.INFO, String.format("SELECT * FROM %s WHERE NAME='%s';", BetterShop.config.sql_tableName, name));
                ResultSet table = MySQLdatabase.GetQuery(String.format(
                        "SELECT * FROM %s WHERE NAME='%s';", sql_tableName, name));
                if (table.first()) {
                    ItemStockEntry ret = new ItemStockEntry();
                    ret.itemNum=table.getInt(1);
                    ret.itemSub=table.getByte(2);
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

    public ItemStockEntry GetItem(Item item) throws SQLException, Exception {
        if (MySQLdatabase.IsConnected()) {
            try {
                //BetterShop.Log(Level.INFO, String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", BetterShop.config.sql_tableName, (int)Math.floor(item), (int) Math.round((item - Math.floor(item)) * 100.)));
                ResultSet table = MySQLdatabase.GetQuery(
                        String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", sql_tableName,
                        item.ID(), (int) item.Data()));
                if (table.first()) {
                    ItemStockEntry ret = new ItemStockEntry();
                    ret.itemNum=table.getInt(1);
                    ret.itemSub=table.getByte(2);
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

    public boolean SetAmount(String itemName, long amt) throws SQLException {
        if (ItemExists(itemName)) {
            try {
                MySQLdatabase.RunUpdate(
                        String.format("UPDATE %s SET AMT='%d' WHERE NAME='%s';", 
                        sql_tableName, amt, itemName));
                /* or:
                String.format("UPDATE %s SET BUY=%f1.2, SELL=%f1.2 WHERE ID='%d' AND SUB='%d';",
                buy, sell, itemInfo.getItemTypeId(), itemInfo.getData())).executeUpdate();
                 */
                return true;
            } catch (SQLException ex) {
                throw new SQLException("Error executing UPDATE on " + sql_tableName, ex);
            }
        } else {
            Item toAdd = Item.findItem(itemName);
            if (toAdd != null) {
                try {
                    MySQLdatabase.RunUpdate(
                            String.format("INSERT INTO %s VALUES(%d, %d, '%s', '%d');", sql_tableName,
                            toAdd.ID(), toAdd.Data(), toAdd.name, amt));
                    return true;
                } catch (SQLException ex) {
                    throw new SQLException("Error executing INSERT on " + sql_tableName, ex);
                }
            }
        }
        return false;
    }

    public void SetAmount(Item item, long amt) throws SQLException { //
        if (ItemExists(item)) {
            try {
                //logger.log(Level.INFO, String.format("UPDATE %s SET BUY=%1.2f, SELL=%1.2f WHERE ID='%d' AND SUB='%d';", sql_tableName, buy, sell, item.itemId, (int) item.itemData));
                MySQLdatabase.RunUpdate(
                        String.format("UPDATE %s SET AMT='%d WHERE ID='%d' AND SUB='%d';", sql_tableName,
                        amt, item.ID(), (int) item.Data()));
                //return true;
            } catch (SQLException ex) {
                throw new SQLException("Error executing UPDATE on " + sql_tableName, ex);
            }
        } else {
            //Item toAdd = Item.findItem(item);
            // assuming item is valid
            try {
                //logger.log(Level.INFO, String.format("INSERT INTO %s VALUES(%d, %d, '%s', %1.2f, %1.2f);", sql_tableName, item.itemId, (int)item.itemData, item.name, buy, sell)
                MySQLdatabase.RunUpdate(
                        String.format("INSERT INTO %s VALUES(%d, %d, '%s', '%d');", sql_tableName,
                        item.ID(), (int) item.Data(), item.name, amt));
                //return true;
            } catch (SQLException ex) {
                throw new SQLException("Error executing INSERT on " + sql_tableName, ex);
            }
        }
    }

    public boolean ItemExists(String item) throws SQLException {
        if (MySQLdatabase.IsConnected()) {
            try {
                //logger.log(Level.INFO, String.format("SELECT * FROM %s WHERE NAME='%s';", sql_tableName, item));
                ResultSet table = MySQLdatabase.GetQuery(
                        String.format("SELECT * FROM %s WHERE NAME='%s';", sql_tableName, item));
                return table.first();
            } catch (SQLException ex) {
                throw new SQLException("Error executing SELECT on " + sql_tableName, ex);
            }
        }
        return false;
    }

    public boolean ItemExists(Item item) throws SQLException {
        if (MySQLdatabase.IsConnected()) {
            try {
                //BetterShop.Log(Level.INFO, String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", sql_tableName, (int) Math.floor(item), (int) Math.round((item - Math.floor(item))* 100)));
                ResultSet table = MySQLdatabase.GetQuery(
                        String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", sql_tableName,
                        item.ID(), (int) item.Data()));
                //BetterShop.Log(Level.INFO, table.first()?"true":"false");
                return table.first();
            } catch (SQLException ex) {
                throw new SQLException("Error executing SELECT on " + sql_tableName, ex);
            }
        }
        return false;
    }

    public boolean RemoveItem(String item) throws SQLException {
        if (MySQLdatabase.IsConnected()) {
            try {
                MySQLdatabase.RunUpdate(String.format(
                        "DELETE FROM %s WHERE NAME='%s';", sql_tableName, item));
            } catch (SQLException ex) {
                throw new SQLException("Error executing DELETE on " + sql_tableName, ex);
            }
        }
        return false;
    }
    public boolean RemoveItem(Item item) throws SQLException {
        if (MySQLdatabase.IsConnected()) {
            try {
                MySQLdatabase.RunUpdate(String.format(
                        "DELETE FROM %s WHERE ID='%d' AND SUB='%d';", sql_tableName, item.ID(), (int) item.Data()));
            } catch (SQLException ex) {
                throw new SQLException("Error executing DELETE on " + sql_tableName, ex);
            }
        }
        return false;
    }

    public LinkedList<ItemStockEntry> GetFullList() throws SQLException, Exception {
        LinkedList<ItemStockEntry> tableDat = new LinkedList<ItemStockEntry>();
        if (MySQLdatabase.IsConnected()) {
            try {
                // Statement to use to issue SQL queries
                //Statement st = DBconnection.createStatement();
                //ResultSet table = st.executeQuery("SELECT * FROM " + sql_tableName + ";");
                //PreparedStatement st=
                //ResultSet table = DBconnection.prepareStatement("SELECT * FROM " + sql_tableName + ";").executeQuery();
                ResultSet table = MySQLdatabase.GetQuery(
                        "SELECT * FROM " + sql_tableName + "  ORDER BY ID, SUB;");
                //BetterShop.Log(Level.INFO, "Table selected: ");
                for (table.beforeFirst(); table.next();) {
                    //BetterShop.Log(Level.INFO, table.getString(3));//
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

    public boolean IsConnected() {
        return MySQLdatabase.IsConnected();
    }

    private boolean createItemStockTable(String tableName) throws SQLException {
        if (!MySQLdatabase.IsConnected() || tableName.contains(" ")) {
            return false;
        }
        try {
            MySQLdatabase.RunUpdate("CREATE TABLE " + tableName
                    + "(ID    INTEGER  NOT NULL,"
                    + "SUB   TINYINT  NOT NULL,"
                    + "NAME  VARCHAR(25) NOT NULL,"
                    + "AMT   BIGINT NOT NULL,"
                    + "PRIMARY KEY (ID, SUB));");
        } catch (SQLException e) {
            throw new SQLException("Error while creating table", e);
        }
        return true;
    }
    

    public String GetDatabaseName(){
        return sql_database;
    }
    public String GetTableName(){
        return sql_tableName;
    }
    public String GetUserName(){
        return MySQLdatabase.GetUserName();
    }
    public String GetHostName(){
        return MySQLdatabase.GetHostName();
    }
    public String GetPortNum(){
        return MySQLdatabase.GetPortNum();
    }
} // end class MySQLPriceList

