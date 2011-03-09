/**
 * Programmer: Jacob Scott
 * Email: jascottytechie at gmail.com
 * Program Name: BSMySQL
 * Description: class for working with a MySQL server
 * Date: Mar 8, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import java.io.File;
//import java.sql.*;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Connection;
//import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.material.MaterialData;

public class BSMySQL {

    private final static Logger logger = Logger.getLogger("Minecraft");
    private String sql_username, sql_password, sql_database = "minecraft", sql_tableName = "BetterShop", sql_hostName = "localhost", sql_portNum = "3306";
    private Connection DBconnection = null;

    public BSMySQL(String database, String tableName, String username, String password, String hostName, String portNum) {

        sql_database = database;
        sql_tableName = tableName;
        sql_username = username;
        sql_password = password;
        sql_hostName = hostName;
        sql_portNum = portNum;

        // connect to database (for now, jeaves connection open)
        connect();

    } // end default constructor

    public final boolean connect() {

        // double-check that mysql-bin.jar exists

        // file used by iConomy
        // 4 can try, name shared with iConomy mysql.. i don't check for all possibilities, though)
        String names[] = new String[]{"lib/mysql.jar",
            "lib/mysql-connector-java-bin.jar",
            "lib/mysql-connector-java-5.1.14-bin.jar",
            "lib/mysql-connector-java-5.1.15-bin.jar"};
        File f = null;
        for (int i = 0; i < names.length - 1; ++i) {
            f = new File(names[i]);
            if (f.exists()) {
                break;
            }
        }
        if (f == null || !f.exists()) {
            // todo: download jar to lib folder
            logger.log(Level.SEVERE, "Failed to load dependency: download mysql-connector-java-bin.jar into /lib");

        }

        // connect to database
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            DBconnection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s?create=true", sql_hostName, sql_portNum, sql_database),
                    sql_username, sql_password);
            // or append "user=%s&password=%s", sql_username, sql_password);
            // create=true: create database if not already exist

            // now check if table is there
            DatabaseMetaData dbm = DBconnection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, sql_tableName, null);
            if (!tables.next()) {
                // table does not exist
                createTable(sql_tableName);
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to connect to database", ex);
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start database connection", e);
            return false;
        }
        return true;
    }

    public void disconnect() {
        try {
            if (DBconnection != null && !DBconnection.isClosed()) {
                DBconnection.close();
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error handling diconnect", ex);
        }
        DBconnection = null;
    }

    // manually force database to save
    public void commit() {
        if (IsConnected()) {
            try {
                DBconnection.createStatement().executeUpdate("COMMIT;");
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "failed to commit database", ex);
            }
        }
    }

    public BS_SQL_Data GetItem(String name) {
        BS_SQL_Data ret = new BS_SQL_Data();
        if (IsConnected()) {
            try {
                //logger.log(Level.INFO, String.format("SELECT * FROM %s WHERE NAME='%s';", sql_tableName, name));
                ResultSet table = DBconnection.createStatement().executeQuery(
                        String.format("SELECT * FROM %s WHERE NAME='%s';", sql_tableName, name));
                if (table.first()) {
                    ret.itemNum = table.getInt(1);
                    ret.itemSub = table.getInt(2);
                    ret.name = table.getString(3);
                    ret.buy = table.getDouble(4);
                    ret.sell = table.getDouble(5);
                }
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error executing SELECT on " + sql_tableName, ex);
                disconnect();
            }
        } else {
            logger.log(Level.WARNING, "Is Not connected to database: not checking for item");
        }
        return ret;
    }

    public BS_SQL_Data GetItem(double item) {
        BS_SQL_Data ret = new BS_SQL_Data();
        if (IsConnected()) {
            try {
                //logger.log(Level.INFO, String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", sql_tableName, (int)Math.floor(item), (int) Math.round((item - Math.floor(item)) * 100.)));
                ResultSet table = DBconnection.createStatement().executeQuery(
                        String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", sql_tableName, 
                        (int) Math.floor(item), (int) Math.round((item - Math.floor(item)) * 100.)));
                if (table.first()) {
                    ret.itemNum = table.getInt(1);
                    ret.itemSub = table.getInt(2);
                    ret.name = table.getString(3);
                    ret.buy = table.getDouble(4);
                    ret.sell = table.getDouble(5);
                }
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error executing SELECT on " + sql_tableName, ex);
                disconnect();
            }
        } else {
            logger.log(Level.WARNING, "Is Not connected to database: not checking for item");
        }
        return ret;
    }

    // don't normally like to throw exceptions, but that was easiest to add here :(
    public boolean SetPrice(String item, double buy, double sell) { //  throws Exception
        if (ItemExists(item)) {
            try {
                DBconnection.prepareStatement(
                        String.format("UPDATE %s SET BUY=%1.2f, SELL=%1.2f WHERE NAME='%s';", sql_tableName,
                        buy, sell, item)).executeUpdate();
                /* or:
                String.format("UPDATE %s SET BUY=%f1.2, SELL=%f1.2 WHERE ID='%d' AND SUB='%d';",
                buy, sell, itemInfo.getItemTypeId(), itemInfo.getData())).executeUpdate();
                 */
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error executing UPDATE on " + sql_tableName, ex);
            }
        } else {
            MaterialData itemInfo;
            try {
                itemInfo = itemDb.get(item);
            } catch (Exception ex) {
                //logger.log(Level.SEVERE, "Error looking up \"item\"", ex);
                return false;
            }
            try {
                DBconnection.prepareStatement(
                        String.format("INSERT INTO %s VALUES(%d, %d, '%s', %1.2f, %1.2f);", sql_tableName,
                        itemInfo.getItemTypeId(), itemInfo.getData(), item, buy, sell)).executeUpdate();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error executing INSERT on " + sql_tableName, ex);
            }
        }
        return true;
    }

    public boolean SetPrice(double item, double buy, double sell) { // throws Exception
        if (ItemExists(item)) {
            try {
                //logger.log(Level.INFO, String.format("UPDATE %s SET BUY=%1.2f, SELL=%1.2f WHERE ID='%d' AND SUB='%d';", sql_tableName, buy, sell, (int) Math.floor(item), (int) Math.round((item - Math.floor(item))* 100.) ));
                DBconnection.prepareStatement(
                        String.format("UPDATE %s SET BUY=%1.2f, SELL=%1.2f WHERE ID='%d' AND SUB='%d';", sql_tableName,
                        buy, sell, (int) Math.floor(item), (int) Math.round((item - Math.floor(item)) * 100.))).executeUpdate();
                /* or:
                String.format("UPDATE %s SET BUY=%f1.2, SELL=%f1.2 WHERE ID='%d' AND SUB='%d';",
                buy, sell, itemInfo.getItemTypeId(), itemInfo.getData())).executeUpdate();
                 */
                return true;
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error executing UPDATE on " + sql_tableName, ex);
            }
        } else {
            try {
                //logger.log(Level.INFO, String.format("INSERT INTO %s VALUES(%d, %f, '%s', %1.2f, %1.2f);", sql_tableName, (int) Math.floor(item), Math.round((item - Math.floor(item))*100)  , itemDb.getName(item), buy, sell));
                DBconnection.prepareStatement(
                        String.format("INSERT INTO %s VALUES(%d, %d, '%s', %1.2f, %1.2f);", sql_tableName,
                        (int) Math.floor(item), (int) Math.round((item - Math.floor(item))* 100.) , itemDb.getName(item), buy, sell)).executeUpdate();
                return true;
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error executing INSERT on " + sql_tableName, ex);
            } catch(Exception ex) {
                //logger.log(Level.SEVERE, "Error looking up item " + item, ex);
            }
        }
        return false;
    }

    public boolean ItemExists(String item) {
        if (IsConnected()) {
            try {
                //logger.log(Level.INFO, String.format("SELECT * FROM %s WHERE NAME='%s';", sql_tableName, item));
                ResultSet table = DBconnection.createStatement().executeQuery(
                        String.format("SELECT * FROM %s WHERE NAME='%s';", sql_tableName, item));
                return table.first();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error executing SELECT on " + sql_tableName, ex);
                disconnect();
            }
        }
        return false;
    }

    public boolean ItemExists(double item) {
        if (IsConnected()) {
            try {
                //logger.log(Level.INFO, String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", sql_tableName, Math.floor(item), (int) (item - Math.floor(item)) * 100.));
                ResultSet table = DBconnection.createStatement().executeQuery(
                        String.format("SELECT * FROM %s WHERE ID='%d' AND SUB='%d';", sql_tableName, 
                        (int) Math.floor(item), (int) Math.round((item - Math.floor(item))* 100.) ));
                return table.first();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error executing SELECT on " + sql_tableName, ex);
                disconnect();
            }
        }
        return false;
    }

    public boolean RemoveItem(String item) {
        if (IsConnected()) {
            try {
                DBconnection.createStatement().executeUpdate(String.format(
                        "DELETE FROM %s WHERE NAME='%s';", sql_tableName, item));
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error executing DELETE on " + sql_tableName, ex);
                disconnect();
            }
        }
        return false;
    }

    public LinkedList<BS_SQL_Data> GetFullList() {
        LinkedList<BS_SQL_Data> tableDat = new LinkedList<BS_SQL_Data>();
        if (IsConnected()) {
            try {
                // Statement to use to issue SQL queries
                //Statement st = DBconnection.createStatement();
                //ResultSet table = st.executeQuery("SELECT * FROM " + sql_tableName + ";");
                //PreparedStatement st=
                //ResultSet table = DBconnection.prepareStatement("SELECT * FROM " + sql_tableName + ";").executeQuery();
                ResultSet table = DBconnection.createStatement().executeQuery(
                        "SELECT * FROM " + sql_tableName + "  ORDER BY ID, SUB;");
                //logger.log(Level.INFO, "Table selected: ");
                for (table.beforeFirst(); table.next();) {
                    //logger.log(Level.INFO, table.getString(3));//
                    tableDat.add(new BS_SQL_Data(table.getInt(1), table.getInt(2), table.getString(3),
                            table.getDouble(4), table.getDouble(5)));
                }
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error executing SELECT on " + sql_tableName, ex);
            }
        } else {
            logger.log(Level.INFO, "Error: DB not connected");
        }
        return tableDat;
    }

    public boolean IsConnected() {
        try {
            return DBconnection != null && !DBconnection.isClosed();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Uxexpected error while checking db connection", ex);
            return false;
        }
    }

    protected boolean createTable(String tableName) {
        if (!IsConnected() || tableName.contains(" ")) {
            return false;
        }

        String createComm = "CREATE TABLE " + sql_database + "." + tableName
                + "(ID    INTEGER  NOT NULL,"
                + "SUB   INTEGER  NOT NULL,"
                + "NAME  VARCHAR(25) NOT NULL,"
                + "BUY   DECIMAL(6,2),"
                + "SELL  DECIMAL(6,2),"
                + "PRIMARY KEY (ID, SUB));";
        Statement stmt = null;
        try {
            stmt = DBconnection.createStatement();
            stmt.executeUpdate(createComm);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error while creating table", e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Uxexpected error while closing SQL Statement", ex);
            }
        }
        return true;
    }
} // end class BSMySQL

