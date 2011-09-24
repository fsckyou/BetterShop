/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: for keeping a rolling history of shop transactions
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
package me.jascotty2.lib.bukkit.shop;

import me.jascotty2.lib.io.FileIO;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.mysql.MySQL;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.jascotty2.lib.util.Str;

/**
 * @author jacob
 */
public class TransactionLog {

    protected MySQL MySQLconnection = null;
    protected boolean isLoaded = false;
    // file, if flatfile
    protected File flatFile = null, totalsFlatFile = null;
    // cache of records
    protected ArrayList<UserTransaction> transactions = new ArrayList<UserTransaction>();
    protected ArrayList<TotalTransaction> totalTransactions = new ArrayList<TotalTransaction>();
    protected ArrayList<TotalTransaction> recentTotalTransactions = new ArrayList<TotalTransaction>();
    public boolean logUserTransactions = true, logTotalTransactions = false;
    public String transLogTablename = "UserTransactionLog",
            recordTablename = "TotalTransactionLog";
    public long userTansactionLifespan = 172800;
    protected final static Logger logger = Logger.getLogger("Minecraft");

    public TransactionLog() {
    } // end default constructor

    public void addRecord(UserTransaction rec) throws SQLException, IOException, Exception {

        //System.out.println("add record: " + BetterShop.getConfig().logUserTransactions + "  " + BetterShop.getConfig().logTotalTransactions);
        if (logUserTransactions) {
            truncateRecords(false);
            transactions.add(rec);

            //System.out.println("adding " + rec + "  (" + transactions.size());
            if (MySQLconnection != null) {
                try {
                    MySQLconnection.runUpdate("INSERT INTO `" + transLogTablename
                            + String.format(Locale.US, "` VALUES(UNIX_TIMESTAMP(), '%s', %d, %d, '%s', %d, %d, %.2f);",
                            Str.strTrim(rec.user, 50), rec.itemNum, rec.itemSub, Str.strTrim(rec.name, 25), rec.amount, rec.sold ? 1 : 0, rec.price));
                } catch (SQLException e) {
                    throw new SQLException("Error inserting transaction data", e);
                }
            } else {
                // append to transaction list & save
                // if going to update Total Transactions as well, postpone save
                if (!logTotalTransactions) {
                    save();
                }
            }
        }
        if (logTotalTransactions) {

            boolean exst = false;
            for (TotalTransaction t : totalTransactions) {
                if (t.itemNum == rec.itemNum && t.itemSub == rec.itemSub) {
                    exst = true;
                    if (rec.sold) {
                        t.sold += rec.amount;
                    } else {
                        t.bought += rec.amount;
                    }
                    break;
                }
            }
            if (!exst) {
                totalTransactions.add(new TotalTransaction(rec));
            }

            if (MySQLconnection != null) {// && MySQLconnection.isConnected()
                try {
                    if (MySQLconnection.getQuery(
                            String.format("SELECT * FROM `%s` WHERE ID='%d' AND SUB='%d';",
                            recordTablename, rec.itemNum, rec.itemSub)).first()) {
                        // exists: update
                        if (rec.sold) {
                            MySQLconnection.runUpdate(
                                    String.format("UPDATE `%s` SET SOLD = SOLD + %d, LAST=UNIX_TIMESTAMP() WHERE ID='%d' AND SUB='%d';",
                                    recordTablename, rec.amount, rec.itemNum, rec.itemSub));
                        } else {
                            MySQLconnection.runUpdate(
                                    String.format("UPDATE `%s` SET BOUGHT = BOUGHT + %d, LAST=UNIX_TIMESTAMP()  WHERE ID='%d' AND SUB='%d';",
                                    recordTablename, rec.amount, rec.itemNum, rec.itemSub));
                        }
                    } else {
                        MySQLconnection.runUpdate(
                                String.format("INSERT INTO `%s` VALUES(%d, %d, '%s', %d, %d, UNIX_TIMESTAMP());",
                                recordTablename, rec.itemNum, rec.itemSub, Str.strTrim(rec.name, 25),
                                rec.sold ? rec.amount : 0, rec.sold ? 0 : rec.amount));
                    }
                } catch (SQLException ex) {
                    throw new SQLException("Error running MySQL Query/Update/Insert while updating transaction totals", ex);
                }
            } else {
                save();
            }
        }
    }

    /**
     * removes records older than specified interval
     * @throws SQLException
     * @throws IOException
     * @throws Exception 
     */
    public void truncateRecords() throws SQLException, IOException, Exception {
        truncateRecords(true);
    }

    /**
     *
     * @param saveFile if using flatfile, whether should save on completion
     * @throws SQLException
     * @throws IOException
     * @throws Exception 
     */
    public void truncateRecords(boolean saveFile) throws SQLException, IOException, Exception {
        if (MySQLconnection != null) {
            try {
                //BetterShopLogger.Log("DELETE FROM " + BetterShop.getConfig().sql_database + "." + BetterShop.getConfig().transLogTablename + " WHERE UNIX_TIMESTAMP() - DATE > " + BetterShop.getConfig().userTansactionLifespan + ";");
                MySQLconnection.runUpdate("DELETE FROM `" + transLogTablename
                        + "` WHERE UNIX_TIMESTAMP() - DATE > " + userTansactionLifespan + ";");
            } catch (SQLException e) {
                throw new SQLException("Error while removing old records", e);
            }
            updateCache();
        } else {
            // loop through cache & remove old entries
            long curTime = (new Date()).getTime();
            for (int i = transactions.size() - 1; i > 0; --i) {
                if (transactions.get(i).time - curTime > userTansactionLifespan) {
                    transactions.remove(i);
                }
            }
            if (saveFile) {
                save();
            }
        }

        if (logUserTransactions) {
            recentTotalTransactions.clear();
            // now update user totals
            // (for MySQL, could use SELECT MAX(DATE) 'DATE', SOLD, SUM(AMT) 'AMT', NAME, ID, SUB, AVG(PRICE) 'PRICE' FROM $mysql_table GROUP BY ID, SUB, SOLD ORDER BY ID, SUB, SOLD
            for (UserTransaction t : transactions) {
                int pos = recentTotalTransactions.indexOf(t);
                if (pos >= 0) {
                    if (t.sold) {
                        recentTotalTransactions.get(pos).sold += t.amount;
                    } else {
                        recentTotalTransactions.get(pos).bought += t.amount;
                    }
                } else {
                    recentTotalTransactions.add(new TotalTransaction(t));
                }
            }
        }
    }

    protected void updateCache() throws SQLException, IOException, Exception {
        if (MySQLconnection != null) {
            if (logUserTransactions) {
                transactions.clear();
                recentTotalTransactions.clear();
                if (MySQLconnection.isConnected()) {
                    try {
                        ResultSet table = MySQLconnection.getQuery(
                                "SELECT * FROM `" + transLogTablename + "` ORDER BY DATE ASC;");

                        for (table.beforeFirst(); table.next();) {
                            transactions.add(new UserTransaction(table.getInt(1), table.getString(2),
                                    table.getInt(3), table.getInt(4), table.getString(5),
                                    table.getInt(6), table.getByte(7) != 0, table.getDouble(8)));
                        }
                    } catch (SQLException ex) {
                        throw new SQLException("Error executing SELECT on " + transLogTablename, ex);
                    }
                } else {
                    throw new Exception("Error: MySQL DB not connected");
                }
            }
            // totals shouldn't need to be updated
            if (logTotalTransactions) {
                totalTransactions.clear();
                ResultSet tb = MySQLconnection.getTable(recordTablename);
                for (tb.beforeFirst(); tb.next();) {
                    totalTransactions.add(new TotalTransaction(
                            tb.getLong("LAST"), tb.getInt("ID"), tb.getInt("SUB"),
                            tb.getString("NAME"), tb.getLong("SOLD"), tb.getLong("BOUGHT")));
                }
            }
        } else {
            if (logUserTransactions) {
                truncateRecords();
                transactions.clear();
                recentTotalTransactions.clear();
                if (flatFile != null && flatFile.exists()) {
                    try {
                        List<String[]> actFile = FileIO.loadCSVFile(flatFile);
                        for (int n = 0; n < actFile.size(); ++n) {//String[] line : actFile) {
                            String[] line = actFile.get(n);
                            if (line.length >= 8) {
                                JItem plItem = JItemDB.findItem(line[2] + ":" + (line[3].equals(" ") ? "0" : line[3]));
                                if (plItem != null) {
                                    //priceList.add(new PriceListItem(plItem, fields[2].length() == 0 ? -1 : CheckInput.GetDouble(fields[2], -1), fields[3].length() == 0 ? -1 : CheckInput.GetDouble(fields[3], -1)));
                                    transactions.add(new UserTransaction(CheckInput.GetInt(line[0], 0), line[1], plItem,
                                            CheckInput.GetInt(line[5], 0), CheckInput.GetInt(line[6], 0) != 0, CheckInput.GetDouble(line[7], 0)));
                                } else if (n > 0) { // first line is expected invalid: is title
                                    logger.log(Level.WARNING, String.format("Invalid item on line %d in %s", (n + 1), flatFile.getName()));
                                }
                            } else {
                                logger.log(Level.WARNING, String.format("unexpected transaction line at %d in %s", (n + 1), flatFile.getName()));
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        logger.log(Level.SEVERE, "Unexpected Error: File not found: " + flatFile.getName(), ex);
                    } catch (IOException ex) {
                        throw new IOException("Error opening " + flatFile.getName() + " for reading", ex);
                    }
                    //System.out.println("loaded " + transactions.size());
                    // if also loading totals, postpone save
                    if (!logTotalTransactions) {
                        save();
                    }
                }
            }
            if (logTotalTransactions) {
                if (totalsFlatFile != null && totalsFlatFile.exists()) {
                    totalTransactions.clear();
                    try {
                        List<String[]> actFile = FileIO.loadCSVFile(totalsFlatFile);
                        for (int n = 0; n < actFile.size(); ++n) {//String[] line : actFile) {
                            String[] line = actFile.get(n);
                            if (line.length >= 6) {
                                JItem plItem = JItemDB.findItem(line[1] + ":" + (line[2].equals(" ") ? "0" : line[2]));
                                if (plItem != null) {
                                    //priceList.add(new PriceListItem(plItem, fields[2].length() == 0 ? -1 : CheckInput.GetDouble(fields[2], -1), fields[3].length() == 0 ? -1 : CheckInput.GetDouble(fields[3], -1)));
                                    totalTransactions.add(new TotalTransaction(CheckInput.GetLong(line[0], 0), plItem,
                                            CheckInput.GetLong(line[4], 0), CheckInput.GetLong(line[5], 0)));
                                } else if (n > 0) { // first line is expected invalid: is title
                                    logger.log(Level.WARNING, String.format("Invalid item on line %d in %s", (n + 1), totalsFlatFile.getName()));
                                }
                            } else {
                                logger.log(Level.WARNING, String.format("unexpected pricelist line at %d in %s", (n + 1), totalsFlatFile.getName()));
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        logger.log(Level.SEVERE, "Unexpected Error: File not found: " + totalsFlatFile.getName(), ex);
                    } catch (IOException ex) {
                        throw new IOException("Error opening " + totalsFlatFile.getName() + " for reading", ex);
                    }
                    //System.out.println("loaded " + totalTransactions.size());
                } else {
                    save();
                }
            }
        }
    }

    protected final boolean createTransactionLogTable() throws SQLException {
        if (!MySQLconnection.isConnected()) {
            return false;
        }
        try {
            MySQLconnection.runUpdate("CREATE TABLE `" + transLogTablename
                    + "`(DATE  INTEGER UNSIGNED   NOT NULL," // DEFAULT UNIX_TIMESTAMP()
                    + "USER  VARCHAR(50) NOT NULL,"
                    + "ID    INTEGER  NOT NULL,"
                    + "SUB   INTEGER  NOT NULL,"
                    + "NAME  VARCHAR(25) NOT NULL,"
                    + "AMT   INTEGER   NOT NULL,"
                    + "SOLD  TINYINT NOT NULL,"
                    + "PRICE DECIMAL(11,2),"
                    + "PRIMARY KEY (DATE, USER, ID, SUB));");
        } catch (SQLException e) {
            throw new SQLException("Error while creating transaction log table", e);
        }
        return true;
    }

    protected final boolean createTransactionRecordTable() throws SQLException {
        if (!MySQLconnection.isConnected()) {
            return false;
        }
        try {
            MySQLconnection.runUpdate("CREATE TABLE " + recordTablename
                    + "(ID    INTEGER  NOT NULL,"
                    + "SUB    INTEGER  NOT NULL,"
                    + "NAME   VARCHAR(25) NOT NULL,"
                    + "SOLD   INTEGER   NOT NULL,"
                    + "BOUGHT INTEGER   NOT NULL,"
                    + "LAST   INTEGER UNSIGNED   NOT NULL,"
                    + "PRIMARY KEY (ID, SUB));");
        } catch (SQLException e) {
            throw new SQLException("Error while creating transaction log table", e);
        }
        return true;
    }

    public boolean save() throws SQLException, IOException, Exception {
        if (MySQLconnection != null) {
            if (MySQLconnection.isConnected()) {
                try {
                    MySQLconnection.commit();
                    return true;
                } catch (SQLException ex) {
                    throw new SQLException("Error executing COMMIT", ex);
                }
            }
        } else {
            if (logUserTransactions) {
                if (flatFile != null){// && !flatFile.isDirectory()) {
                    ArrayList<String> lines = new ArrayList<String>();
                    lines.add("date,user,id,sub,name,amt,sold,price");
                    for (UserTransaction i : transactions) {
                        lines.add(i.time + "," + i.user + ","
                                + i.itemNum + "," + i.itemSub + "," + i.name + ","
                                + i.amount + "," + (i.sold ? "1" : "0") + "," + i.price);
                    }
                    try {
                        if (!FileIO.saveFile(flatFile, lines)) {
                            throw new Exception("Error writing to " + flatFile.getName());
                        }
                    } catch (IOException ex) {
                        // todo: wait until try totalsFlatFile before throwing?
                        throw new IOException("Error opening " + flatFile.getName() + " for writing", ex);
                    }

                } else {
                    logger.log(Level.SEVERE, "Error saving activity log: undefined");
                }
            }

            if (logTotalTransactions) {
                if (totalsFlatFile != null) {
                    ArrayList<String> lines = new ArrayList<String>();
                    lines.add("date,id,sub,name,sold,bought");
                    for (TotalTransaction i : totalTransactions) {
                        lines.add(i.time + ","
                                + i.itemNum + "," + i.itemSub + "," + i.name + ","
                                + i.sold + "," + i.bought);
                    }
                    try {
                        if (!FileIO.saveFile(totalsFlatFile, lines)) {
                            throw new Exception("Error writing to " + totalsFlatFile.getName());
                        }
                    } catch (IOException ex) {
                        throw new IOException("Error opening " + totalsFlatFile.getName() + " for writing", ex);
                    }

                } else {
                    logger.log(Level.SEVERE, "Error saving totals log: undefined");
                }
            }
        }
        return false;
    }

    public TotalTransaction recentItemTotals(JItem search) {
        int pos = recentTotalTransactions.indexOf(search);

        return pos >= 0 ? recentTotalTransactions.get(pos) : null;
    }
} // end class TransactionLog

