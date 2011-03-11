/**
 * Programmer: Jacob Scott
 * Program Name: BSLog
 * Description: log of transactions
 * Date: Mar 11, 2011
 */

package com.nhksos.jjfs85.BetterShop;

import java.sql.SQLException;
import java.util.logging.Level;

public class BSLog {

    public BSLog() {

    } // end default constructor

    protected boolean createTransactionLogTable() {
        if (!BSMySQL.MySQLdatabase.IsConnected()) {
            return false;
        }
        try {
            BSMySQL.MySQLdatabase.RunUpdate("CREATE TABLE " + BetterShop.config.sql_database + "." + BetterShop.config.transLogTablename
                + "(ID    INTEGER  NOT NULL,"
                + "SUB   INTEGER  NOT NULL,"
                + "NAME  VARCHAR(25) NOT NULL,"
                + "BUY   DECIMAL(6,2),"
                + "SELL  DECIMAL(6,2),"
                + "PRIMARY KEY (ID, SUB));");
        } catch (SQLException e) {
            BetterShop.Log(Level.SEVERE, "Error while creating transaction log table", e);
            return false;
        }
        return true;
    }
} // end class BSLog
