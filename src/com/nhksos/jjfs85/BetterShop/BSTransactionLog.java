/**
 * Programmer: Jacob Scott
 * Program Name: BSLog
 * Description: log of transactions
 * Date: Mar 11, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.MySQL.MySQL;
import com.jascotty2.MySQL.UserTransaction;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

//todo: add logging for flatfile format
public class BSTransactionLog {

    MySQL MySQLconnection = null;
    boolean isLoaded = false;
    // file, if flatfile
    File flatFile = null;
    // cache of records
    ArrayList<UserTransaction> priceList = new ArrayList<UserTransaction>();

    public BSTransactionLog() {
        //load();
    } // end default constructor

    public final boolean load() {
        if (BetterShop.config.useMySQL()) {
            // use same connection pricelist is using (pricelist MUST be initialized.. does not check)
            MySQLconnection = BetterShop.pricelist.getMySQLconnection();
            try {
                if (BetterShop.config.logUserTransactions) {
                    if (!MySQLconnection.tableExists(BetterShop.config.transLogTablename)) {
                        BetterShop.config.logUserTransactions = createTransactionLogTable();
                    }else truncateRecords();
                }
                if (BetterShop.config.logTotalTransactions) {
                    if (!MySQLconnection.tableExists(BetterShop.config.recordTablename)) {
                        BetterShop.config.logTotalTransactions = createTransactionRecordTable();
                    }
                }
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error retrieving table list", ex);
                BetterShop.config.logUserTransactions = false;
                return isLoaded = false;
            }
            return isLoaded = true;
        }else{
            flatFile = new File(BSConfig.pluginFolder.getAbsolutePath() + File.separatorChar + BetterShop.config.transLogTablename);
            
        }
        return isLoaded = false;
    }

    public boolean isOpened() {
        return isLoaded && (BetterShop.config.useMySQL()
                ? MySQLconnection != null && MySQLconnection.IsConnected() : flatFile != null);
    }
    
    public String databaseName(){
        return BetterShop.config.useMySQL() ? 
            (MySQLconnection != null ? MySQLconnection.GetDatabaseName() : "null") :
            (flatFile != null ? flatFile.getName() : "null");
    }

    public void addRecord(UserTransaction rec) {
        //System.out.println("add record: " + BetterShop.config.logUserTransactions + "  " + BetterShop.config.logTotalTransactions);
        if (BetterShop.config.logUserTransactions) {
            truncateRecords();
            if (BetterShop.config.useMySQL()) {
                try {
                    MySQLconnection.RunUpdate("INSERT INTO " + BetterShop.config.sql_database + "." + BetterShop.config.transLogTablename
                            + String.format(" VALUES(UNIX_TIMESTAMP(), '%s', %d, %d, '%s', %d, %d);", rec.user, rec.itemNum, rec.itemSub, rec.name, rec.amount, rec.sold ? 1 : 0));
                } catch (SQLException e) {
                    BetterShop.Log(Level.SEVERE, "Error inserting transaction data");
                    BetterShop.Log(Level.SEVERE, e);
                }
            }
        }
        if (BetterShop.config.logTotalTransactions) {
            if (BetterShop.config.useMySQL()) {// && MySQLconnection.IsConnected()
                try {
                    if (MySQLconnection.GetQuery(
                            String.format("SELECT * FROM %s.%s WHERE ID='%d' AND SUB='%d';",
                            BetterShop.config.sql_database, BetterShop.config.recordTablename,
                            rec.itemNum, rec.itemSub)).first()) {
                        // exists: update
                        if (rec.sold) {
                            MySQLconnection.RunUpdate(
                                    String.format("UPDATE %s.%s SET SOLD = SOLD + %d, LAST=UNIX_TIMESTAMP() WHERE ID='%d' AND SUB='%d';",
                                    BetterShop.config.sql_database, BetterShop.config.recordTablename,
                                    rec.amount, rec.itemNum, rec.itemSub));
                        } else {
                            MySQLconnection.RunUpdate(
                                    String.format("UPDATE %s.%s SET BOUGHT = BOUGHT + %d, LAST=UNIX_TIMESTAMP()  WHERE ID='%d' AND SUB='%d';",
                                    BetterShop.config.sql_database, BetterShop.config.recordTablename,
                                    rec.amount, rec.itemNum, rec.itemSub));
                        }
                    } else {
                        MySQLconnection.RunUpdate(
                                String.format("INSERT INTO %s.%s VALUES(%d, %d, '%s', %d, %d, UNIX_TIMESTAMP());",
                                BetterShop.config.sql_database, BetterShop.config.recordTablename,
                                rec.itemNum, rec.itemSub, rec.name, rec.sold ? rec.amount : 0, rec.sold ? 0 : rec.amount));
                    }
                } catch (SQLException ex) {
                    BetterShop.Log(Level.SEVERE, "Error running MySQL Query/Update/Insert while updating transaction totals");
                    BetterShop.Log(Level.SEVERE, ex);
                }
            }
        }
    }

    /**
     * removes records older than specified interval
     */
    public void truncateRecords() {
        if (BetterShop.config.useMySQL()) {
            try {
                //BetterShop.Log("DELETE FROM " + BetterShop.config.sql_database + "." + BetterShop.config.transLogTablename + " WHERE UNIX_TIMESTAMP() - DATE > " + BetterShop.config.userTansactionLifespan + ";");
                MySQLconnection.RunUpdate("DELETE FROM " + BetterShop.config.sql_database + "." + BetterShop.config.transLogTablename
                        + " WHERE UNIX_TIMESTAMP() - DATE > " + BetterShop.config.userTansactionLifespan + ";");
            } catch (SQLException e) {
                BetterShop.Log(Level.SEVERE, "Error while removing old records");
                BetterShop.Log(Level.SEVERE, e);
            }
        }
    }

    protected final boolean createTransactionLogTable() {
        if (!MySQLconnection.IsConnected()) {
            return false;
        }
        try {
            MySQLconnection.RunUpdate("CREATE TABLE " + BetterShop.config.sql_database + "." + BetterShop.config.transLogTablename
                    + "(DATE  INTEGER UNSIGNED   NOT NULL," // DEFAULT UNIX_TIMESTAMP()
                    + "USER  VARCHAR(50) NOT NULL,"
                    + "ID    INTEGER  NOT NULL,"
                    + "SUB   INTEGER  NOT NULL,"
                    + "NAME  VARCHAR(25) NOT NULL,"
                    + "AMT   INTEGER   NOT NULL,"
                    + "SOLD  BIT NOT NULL,"
                    + "PRIMARY KEY (DATE, USER, ID));");
        } catch (SQLException e) {
            BetterShop.Log(Level.SEVERE, "Error while creating transaction log table");
            BetterShop.Log(Level.SEVERE, e);
            return false;
        }
        return true;
    }

    protected final boolean createTransactionRecordTable() {
        if (!MySQLconnection.IsConnected()) {
            return false;
        }
        try {
            MySQLconnection.RunUpdate("CREATE TABLE " + BetterShop.config.sql_database + "." + BetterShop.config.recordTablename
                    + "(ID    INTEGER  NOT NULL,"
                    + "SUB    INTEGER  NOT NULL,"
                    + "NAME   VARCHAR(25) NOT NULL,"
                    + "SOLD   INTEGER   NOT NULL,"
                    + "BOUGHT INTEGER   NOT NULL,"
                    + "LAST   INTEGER UNSIGNED   NOT NULL,"
                    + "PRIMARY KEY (ID, SUB));");
        } catch (SQLException e) {
            BetterShop.Log(Level.SEVERE, "Error while creating transaction log table");
            BetterShop.Log(Level.SEVERE, e);
            return false;
        }
        return true;
    }
} // end class BSLog

