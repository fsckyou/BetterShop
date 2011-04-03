/**
 * Programmer: Jacob Scott
 * Program Name: BSLog
 * Description: log of transactions
 * Date: Mar 11, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.Shop.TotalTransaction;
import com.jascotty2.Shop.TransactionLog;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class BSTransactionLog extends TransactionLog{

    public BSTransactionLog() {
        //load();
    } // end default constructor

    public boolean load() {
        transactions.clear();
        totalTransactions.clear();
        logUserTransactions = BetterShop.config.logUserTransactions;
        logTotalTransactions  = BetterShop.config.logTotalTransactions;
        transLogTablename = BetterShop.config.transLogTablename;
        recordTablename = BetterShop.config.recordTablename;
        userTansactionLifespan = BetterShop.config.userTansactionLifespan;
        
        if (BetterShop.config.useMySQL()) {
            // use same connection pricelist is using (pricelist MUST be initialized.. does not check)
            MySQLconnection = BetterShop.pricelist.getMySQLconnection();
            try {
                if (BetterShop.config.logUserTransactions) {
                    if (!MySQLconnection.tableExists(BetterShop.config.transLogTablename)) {
                        BetterShop.config.logUserTransactions = createTransactionLogTable();
                    } else {
                        try {
                            truncateRecords();
                        } catch (Exception ex) {
                            BetterShop.Log(Level.SEVERE, ex);
                        }
                    }
                }
                if (BetterShop.config.logTotalTransactions) {
                    if (!MySQLconnection.tableExists(BetterShop.config.recordTablename)) {
                        BetterShop.config.logTotalTransactions = createTransactionRecordTable();
                    } else {
                        //load into memory
                        //for(Result)
                        ResultSet tb = MySQLconnection.GetTable(BetterShop.config.recordTablename);
                        for (tb.beforeFirst(); tb.next();) {
                            totalTransactions.add(new TotalTransaction(
                                    tb.getLong("LAST"), tb.getInt("ID"), tb.getInt("SUB"),
                                    tb.getString("NAME"), tb.getLong("SOLD"), tb.getLong("BOUGHT")));
                        }
                    }
                }
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, "Error retrieving table list", ex);
                BetterShop.config.logUserTransactions = false;
                return isLoaded = false;
            }
        }else{
            MySQLconnection = null;
            flatFile = new File(BSConfig.pluginFolder.getAbsolutePath() + File.separatorChar + BetterShop.config.transLogTablename + ".csv");
            totalsFlatFile = new File(BSConfig.pluginFolder.getAbsolutePath() + File.separatorChar + BetterShop.config.recordTablename + ".csv");
        }
        try {
            updateCache();
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }

        return isLoaded = true;
    }

    public boolean isOpened() {
        return isLoaded && (BetterShop.config.useMySQL()
                ? MySQLconnection != null && MySQLconnection.IsConnected() : flatFile != null);
    }

    public String databaseName() {
        return BetterShop.config.useMySQL()
                ? (MySQLconnection != null ? MySQLconnection.GetDatabaseName() : "null")
                : (flatFile != null ? flatFile.getName() : "null");
    }

} // end class BSLog

