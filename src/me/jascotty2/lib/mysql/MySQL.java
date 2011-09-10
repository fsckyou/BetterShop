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

import me.jascotty2.lib.net.InstallDependency;
import java.io.File;
//import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Connection;
//import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.Statement;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQL {

    // local copy of current connection info
    protected String sql_username, sql_password, sql_database, sql_hostName = "localhost", sql_portNum = "3306";
    // DB connection
    protected Connection DBconnection = null;
    private static int checkedDep = 0;

    public MySQL(){
	}

    public MySQL(MySQL copy) {
		DBconnection = copy.DBconnection;
		this.sql_username = copy.sql_username;
		this.sql_password = copy.sql_password;
		this.sql_database = copy.sql_database;
		this.sql_hostName = copy.sql_hostName;
		this.sql_portNum = copy.sql_portNum;
     }

    public MySQL(String database, String username, String password, String hostName, String portNum) throws Exception {
        // connect to database (for now, jeaves connection open)
        connect(database, username, password, hostName, portNum);
    }

    public MySQL(String database, String username, String password, String hostName) throws Exception {
        connect(database, username, password, hostName);
    }

    public MySQL(String database, String username, String password) throws Exception {
        connect(database, username, password);
    }

    public MySQL(String database, String username) throws Exception {
        connect(database, username, "");
    }

    /**
     * Connect to a database
     * @param database MySQL database to use
     * @param username MySQL username to connect as
     * @param password MySQL user password
     * @param hostName host of server
     * @param portNum port to use
     * @return if new connection successful (false if there was no change)
     * @throws Exception
     */
    public final boolean connect(String database, String username, String password, String hostName, String portNum) throws Exception {
        if (!(isConnected() && sql_database.equals(database)
                && sql_username.equals(username)
                && sql_password.equals(password)
                && sql_hostName.equals(hostName)
                && sql_portNum.equals(portNum))) {

            sql_database = database;
            sql_username = username;
            sql_password = password;
            sql_hostName = hostName;
            sql_portNum = portNum;
            return connect();
        }
        return false;
    }

    /**
     * Connect to a database
     * @param database MySQL database to use
     * @param username MySQL username to connect as
     * @param password MySQL user password
     * @param hostName host of server
     * @return if new connection successful (false if there was no change)
     * @throws Exception
     */
    public final boolean connect(String database, String username, String password, String hostName) throws Exception {
        if (!(isConnected() && sql_database.equals(database)
                && sql_username.equals(username)
                && sql_password.equals(password)
                && sql_hostName.equals(hostName))) {

            sql_database = database;
            sql_username = username;
            sql_password = password;
            sql_hostName = hostName;
            sql_portNum = "3306";
            return connect();
        }
        return false;
    }

    /**
     * Connect to a database on localhost
     * @param database MySQL database to use
     * @param username MySQL username to connect as
     * @param password MySQL user password
     * @return if new connection successful (false if there was no change)
     * @throws Exception
     */
    public final boolean connect(String database, String username, String password) throws Exception {
        if (!(isConnected() && sql_database.equals(database)
                && sql_username.equals(username)
                && sql_password.equals(password))) {

            sql_database = database;
            sql_username = username;
            sql_password = password;
            sql_hostName = "localhost";
            sql_portNum = "3306";
            return connect();
        }
        return false;
    }

    /**
     * Connect to a database on localhost (blank password)
     * @param database MySQL database to use
     * @param username MySQL username to connect as
     * @return if new connection successful (false if there was no change)
     * @throws Exception
     */
    public final boolean connect(String database, String username) throws Exception {
        if (!(isConnected() && sql_database.equals(database)
                && sql_username.equals(username))) {

            sql_database = database;
            sql_username = username;
            sql_password = "";
            sql_hostName = "localhost";
            sql_portNum = "3306";
            return connect();
        }
        return false;
    }

    /**
     * Checks if mysql-connector-java-bin.jar exists
     * @return
     */
    public static boolean checkDependency() {
        //int checked = 0;
        if (checkedDep != 0) {
            return checkedDep > 0;
        }
        // file used by iConomy
        // 4 can try, name shared with iConomy mysql.. i don't check for all possibilities, though)
        String names[] = new String[]{"lib/mysql.jar",
            "lib/mysql-connector-java-bin.jar",
            "lib/mysql-connector-java-5.1.14-bin.jar",
            "lib/mysql-connector-java-5.1.15-bin.jar"};
        File f = null;
        for (int i = 0; i < names.length; ++i) {
            f = new File(names[i]);
            if (f.exists()) {
                checkedDep = 1;
                return true;
            }
        }
        // !f.exists()

        // downloads jar to lib folder
        if (!InstallDependency.install("lib" + File.separator + "mysql-connector-java-bin.jar",
                "mysql-connector-java-5.1.15/mysql-connector-java-5.1.15-bin.jar",
                "http://mirror.services.wisc.edu/mysql/Downloads/Connector-J/mysql-connector-java-5.1.15.zip",
                "http://mirror.anigaiku.com/Dependencies/mysql-connector-java-bin.jar"
                )) {

            checkedDep = -1;
            return false;
        }

        checkedDep = 1;
        return true;

    }

    /**
     * Connect/Reconnect using current info
     * @return if can connect & connected
     * @throws Exception
     */
    public final boolean connect() throws Exception {
        if (DBconnection == null) {
            // double-check that mysql-bin.jar exists
            if (!checkDependency()) {
                return false;
            }

            // connect to database
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            if (isConnected()) {
                disconnect();
            }

            DBconnection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s?create=true,autoReconnect=true", sql_hostName, sql_portNum, sql_database),
                    sql_username, sql_password);

            // or append "user=%s&password=%s", sql_username, sql_password);
            // create=true: create database if not already exist
            // autoReconnect=true: should fix errors that occur if the connection times out
        } else {
            if (DBconnection.isClosed()) {
                DBconnection = DriverManager.getConnection(
                        String.format("jdbc:mysql://%s:%s/%s?create=true,autoReconnect=true", sql_hostName, sql_portNum, sql_database),
                        sql_username, sql_password);
            }
        }
        return true;
    }

    /**
     * close the MySQL server connection
     */
    public void disconnect() { //  throws Exception
        try {
            if (DBconnection != null && !DBconnection.isClosed()) {
                DBconnection.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, "Error closing MySQL connection", ex);
        }
        DBconnection = null;
    }

    /**
     * manually force database to save
     */
    public void commit() throws SQLException {
        if (isConnected()) {
            DBconnection.createStatement().executeUpdate("COMMIT;");
        }
    }

    public ResultSet getQuery(String qry) throws SQLException {

        if (isConnected()) {
            try {
                if(!qry.trim().endsWith(";")){
                    qry+=";";
                }
                //BetterShopLogger.Log(Level.INFO, String.format("SELECT * FROM %s WHERE NAME='%s';", sql_tableName, name));
                return DBconnection.createStatement().executeQuery(qry);

            } catch (SQLException ex) {
                // if lost connection & successfully reconnected, try again
                if (isConnected(false) && isConnected(true)) {
                    return DBconnection.createStatement().executeQuery(qry);
                }
                //disconnect();
                throw ex;
            }
        } else {
            return null;
        }
    }

    public int runUpdate(String qry) throws SQLException { //
        if (isConnected()) {
            try {
                if(!qry.trim().endsWith(";")){
                    qry+=";";
                }
                return DBconnection.prepareStatement(qry).executeUpdate();
            } catch (SQLException ex) {
                // if lost connection & successfully reconnected, try again
                if (isConnected(false) && isConnected(true)) {
                    return DBconnection.prepareStatement(qry).executeUpdate();
                }
                //disconnect();
                throw ex;
            }
        } else {
            return -1;
        }
    }

    public ResultSet getTable(String tablename) throws SQLException {
        if (isConnected()) {
            try {
                // Statement to use to issue SQL queries
                //Statement st = DBconnection.createStatement();
                //ResultSet table = st.executeQuery("SELECT * FROM " + sql_tableName + ";");
                //PreparedStatement st=
                //ResultSet table = DBconnection.prepareStatement("SELECT * FROM `" + sql_tableName + "`;").executeQuery();
                return DBconnection.createStatement().executeQuery("SELECT * FROM `" + tablename + "`;");

            } catch (SQLException ex) {
                // if lost connection & successfully reconnected, try again
                if (isConnected(false) && isConnected(true)) {
                    return DBconnection.createStatement().executeQuery("SELECT * FROM `" + tablename + "`;");
                }
                //disconnect();
                throw ex;
            }
        } else {
            return null;
        }
    }

    /**
     * check if is currently connected to a server
     * preforms a pre-check, and if not & connection info exists, will attempt reconnect
     */
    public boolean isConnected() {
        return isConnected(true);
    }

    public boolean isConnected(boolean reconnect) {
        try {
            if (DBconnection != null && DBconnection.isClosed()) {
                try {
                    connect();
                } catch (Exception ex) {
                    // should not reach here, since is only thrown if creating a new connection
                    // (while connecting to the mysql lib)
                }
            }
            return DBconnection != null && !DBconnection.isClosed();
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, "Error checking MySQL connection status", ex);
            DBconnection = null;
            return false;
        }
    }

    /**
     * check if connected & a table exists in this database
     * @param tableName table to look up
     * @throws SQLException
     */
    public boolean tableExists(String tableName) throws SQLException {
        if (isConnected()) {
            //try {
            return DBconnection.getMetaData().getTables(null, null, tableName, null).next();
            //} catch (SQLException ex) {
            //    Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, "Error retrieving table list", ex);
            //}
        }
        return false;
    }

    public boolean columnExists(String tableName, String columnName) throws SQLException {
        if (isConnected()) {
            ResultSet t = DBconnection.getMetaData().getColumns(null, null, tableName, null);//.getTables(null, null, tableName, null);//
            for (;t.next();) {
                //for(int i=1; i<=7; ++i)
                //System.out.println(t.getString(i));
                //System.out.println();
                if(t.getString(4).equals(columnName))
                    return true;
                //try {
                    //t.getRowId(columnName);
                    //t.findColumn(columnName);
                //} catch (SQLException ex) {
                //    Logger.getAnonymousLogger().log(Level.WARNING, ex.getMessage(), ex);
                //    return false;
                //}
            }
        }
        return false;
    }

    public String getUserName() {
        return sql_username;
    }

    public String getDatabaseName() {
        return sql_database;
    }

    public String getHostName() {
        return sql_hostName;
    }

    public String getPortNum() {
        return sql_portNum;
    }
} // end class BSMySQL

