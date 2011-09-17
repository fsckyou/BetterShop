/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: methods for maintaining a log of transactions
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
package me.jascotty2.bettershop.shop;

import me.jascotty2.lib.bukkit.shop.TotalTransaction;
import me.jascotty2.lib.bukkit.shop.TransactionLog;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import me.jascotty2.bettershop.BSConfig;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.utils.BetterShopLogger;

public class BSTransactionLog extends TransactionLog {

	final Shop shop;
	private BSPriceList pricelist;

	public BSTransactionLog(Shop shop) {
		super();
		this.shop = shop;
		this.pricelist = shop.pricelist;
	} // end default constructor

    public final boolean load() {
        transactions.clear();
        totalTransactions.clear();
        logUserTransactions = BetterShop.getConfig().logUserTransactions;
        logTotalTransactions = BetterShop.getConfig().logTotalTransactions;
        transLogTablename = pricelist.config.transLogTablename;
        recordTablename = pricelist.config.recordTablename;
        userTansactionLifespan = BetterShop.getConfig().userTansactionLifespan;

        if (BetterShop.getConfig().useMySQL()) {
            // use same connection pricelist is using (pricelist MUST be initialized.. does not check)
            MySQLconnection = pricelist.getMySQLconnection();
            if (MySQLconnection == null) {
                return isLoaded = logTotalTransactions = logUserTransactions = false;
            } else {
                try {
                    if (logUserTransactions) {
                        if (!MySQLconnection.tableExists(transLogTablename)) {
                            logUserTransactions = createTransactionLogTable();
                        } else {
                            tableCheck();
                            try {
                                truncateRecords();
                            } catch (Exception ex) {
                                BetterShopLogger.Log(Level.SEVERE, ex);
                            }
                        }
                    }
                    if (logTotalTransactions) {
                        if (!MySQLconnection.tableExists(recordTablename)) {
                            logTotalTransactions = createTransactionRecordTable();
                        } else {
                            //load into memory
                            //for(Result)
                            ResultSet tb = MySQLconnection.getTable(recordTablename);
                            for (tb.beforeFirst(); tb.next();) {
                                totalTransactions.add(new TotalTransaction(
                                        tb.getLong("LAST"), tb.getInt("ID"), tb.getInt("SUB"),
                                        tb.getString("NAME"), tb.getLong("SOLD"), tb.getLong("BOUGHT")));
                            }
                        }
                    }
                } catch (SQLException ex) {
                    BetterShopLogger.Log(Level.SEVERE, "Error retrieving table list", ex);
                    return isLoaded = logTotalTransactions = logUserTransactions = false;
                }
            }
        } else {
            MySQLconnection = null;
            flatFile = new File(BSConfig.pluginFolder.getAbsolutePath() 
					+ File.separatorChar + pricelist.config.transLogTablename + ".csv");
            totalsFlatFile = new File(BSConfig.pluginFolder.getAbsolutePath() 
					+ File.separatorChar + pricelist.config.recordTablename + ".csv");
        }
        try {
            updateCache();
        } catch (Exception ex) {
            BetterShopLogger.Log(Level.SEVERE, ex);
        }

        return isLoaded = true;
    }

    public boolean isOpened() {
        return isLoaded && (BetterShop.getConfig().useMySQL()
                ? MySQLconnection != null && MySQLconnection.isConnected() : flatFile != null);
    }

    public String databaseName() {
        return BetterShop.getConfig().useMySQL()
                ? (MySQLconnection != null ? MySQLconnection.getDatabaseName() : "null")
                : (flatFile != null ? flatFile.getName() : "null");
    }

    public void tableCheck() {
        if (BetterShop.getConfig().useMySQL()
                && MySQLconnection != null && MySQLconnection.isConnected()) {
            try {
                //Version 1.6.1.1+  ALTER TABLE BetterShopMarketActivity ADD COLUMN PRICE DECIMAL(11,2);

                if (logUserTransactions
                        && !MySQLconnection.columnExists(transLogTablename, "PRICE")) {
                    MySQLconnection.runUpdate("ALTER TABLE " + transLogTablename + " ADD COLUMN PRICE DECIMAL(11,2);");
                    BetterShopLogger.Log(transLogTablename + " updated");
                }
            } catch (SQLException ex) {
                BetterShopLogger.Log(Level.SEVERE, "Error while upgrading MySQL Table", ex);
            }
        }
    }
} // end class BSLog

