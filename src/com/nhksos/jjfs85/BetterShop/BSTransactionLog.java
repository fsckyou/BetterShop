/**
 * Programmer: Jacob Scott
 * Program Name: BSLog
 * Description: log of transactions
 * Date: Mar 11, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.MySQL.UserTransaction;
import java.sql.SQLException;
import java.util.logging.Level;

//todo: add logging for flatfile format
public class BSTransactionLog {

    public BSTransactionLog() {
        load();
    } // end default constructor

    public final boolean load() {
        if (BetterShop.config.useMySQL()) {
            try {
                if (BetterShop.config.logUserTransactions) {
                    if (!BSMySQL.MySQLdatabase.tableExists(BetterShop.config.transLogTablename)) {
                        BetterShop.config.logUserTransactions = createTransactionLogTable();
                    }
                }
                if (BetterShop.config.logTotalTransactions) {
                    if (!BSMySQL.MySQLdatabase.tableExists(BetterShop.config.recordTablename)) {
                        BetterShop.config.logTotalTransactions = createTransactionRecordTable();
                    }
                }
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error retrieving table list", ex);
                BetterShop.config.logUserTransactions = false;
                return false;
            }
            return true;
        }
        return false;
    }

    public void addRecord(UserTransaction rec) {
        if (BetterShop.config.logUserTransactions) {
            truncateRecords();
            if (BetterShop.config.useMySQL()) {
                try {
                    BSMySQL.MySQLdatabase.RunUpdate("INSERT INTO " + BetterShop.config.sql_database + "." + BetterShop.config.transLogTablename
                            + String.format(" VALUES(UNIX_TIMESTAMP(), '%s', %d, %d, '%s', %d, %d);", rec.user, rec.itemNum, rec.itemSub, rec.name, rec.amount, rec.sold ? 1 : 0));
                } catch (SQLException e) {
                    BetterShop.Log(Level.SEVERE, "Error inserting transaction data", e);
                }
            }
        }
        if (BetterShop.config.logTotalTransactions) {
            if (BetterShop.config.useMySQL()) {// && BSMySQL.MySQLdatabase.IsConnected()
                try {
                    if (BSMySQL.MySQLdatabase.GetQuery(
                            String.format("SELECT * FROM %s.%s WHERE ID='%d' AND SUB='%d';",
                            BetterShop.config.sql_database, BetterShop.config.recordTablename,
                            rec.itemNum, rec.itemSub)).first()) {
                        // exists: update
                        if (rec.sold) {
                            BSMySQL.MySQLdatabase.RunUpdate(
                                    String.format("UPDATE %s.%s SET SOLD = SOLD + %d, LAST=UNIX_TIMESTAMP() WHERE ID='%d' AND SUB='%d';",
                                    BetterShop.config.sql_database, BetterShop.config.recordTablename,
                                    rec.amount, rec.itemNum, rec.itemSub));
                        } else {
                            BSMySQL.MySQLdatabase.RunUpdate(
                                    String.format("UPDATE %s.%s SET BOUGHT = BOUGHT + %d, LAST=UNIX_TIMESTAMP()  WHERE ID='%d' AND SUB='%d';",
                                    BetterShop.config.sql_database, BetterShop.config.recordTablename,
                                    rec.amount, rec.itemNum, rec.itemSub));
                        }
                    } else {
                        BSMySQL.MySQLdatabase.RunUpdate(
                                String.format("INSERT INTO %s.%s VALUES(%d, %d, '%s', %d, %d, UNIX_TIMESTAMP());",
                                BetterShop.config.sql_database, BetterShop.config.recordTablename,
                                rec.itemNum, rec.itemSub, rec.name, rec.sold ? rec.amount : 0, rec.sold ? 0 : rec.amount));
                    }
                } catch (SQLException ex) {
                    BetterShop.Log(Level.SEVERE, "Error running MySQL Query/Update/Insert while updating transaction totals", ex);
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
                BSMySQL.MySQLdatabase.RunUpdate("DELETE FROM " + BetterShop.config.sql_database + "." + BetterShop.config.transLogTablename
                        + " WHERE UNIX_TIMESTAMP() - DATE > " + BetterShop.config.userTansactionLifespan + ";");
            } catch (SQLException e) {
                BetterShop.Log(Level.SEVERE, "Error while removing old records", e);
            }
        }
    }

    protected final boolean createTransactionLogTable() {
        if (!BSMySQL.MySQLdatabase.IsConnected()) {
            return false;
        }
        try {
            BSMySQL.MySQLdatabase.RunUpdate("CREATE TABLE " + BetterShop.config.sql_database + "." + BetterShop.config.transLogTablename
                    + "(DATE  INTEGER UNSIGNED   NOT NULL," // DEFAULT UNIX_TIMESTAMP()
                    + "USER  VARCHAR(50) NOT NULL,"
                    + "ID    INTEGER  NOT NULL,"
                    + "SUB   INTEGER  NOT NULL,"
                    + "NAME  VARCHAR(25) NOT NULL,"
                    + "AMT   INTEGER   NOT NULL,"
                    + "SOLD  BIT NOT NULL,"
                    + "PRIMARY KEY (DATE, USER));");
        } catch (SQLException e) {
            BetterShop.Log(Level.SEVERE, "Error while creating transaction log table", e);
            return false;
        }
        return true;
    }

    protected final boolean createTransactionRecordTable() {
        if (!BSMySQL.MySQLdatabase.IsConnected()) {
            return false;
        }
        try {
            BSMySQL.MySQLdatabase.RunUpdate("CREATE TABLE " + BetterShop.config.sql_database + "." + BetterShop.config.recordTablename
                    + "(ID    INTEGER  NOT NULL,"
                    + "SUB    INTEGER  NOT NULL,"
                    + "NAME   VARCHAR(25) NOT NULL,"
                    + "SOLD   INTEGER   NOT NULL,"
                    + "BOUGHT INTEGER   NOT NULL,"
                    + "LAST   INTEGER UNSIGNED   NOT NULL,"
                    + "PRIMARY KEY (ID, SUB));");
        } catch (SQLException e) {
            BetterShop.Log(Level.SEVERE, "Error while creating transaction log table", e);
            return false;
        }
        return true;
    }
} // end class BSLog

