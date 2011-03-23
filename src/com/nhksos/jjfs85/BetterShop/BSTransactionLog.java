/**
 * Programmer: Jacob Scott
 * Program Name: BSLog
 * Description: log of transactions
 * Date: Mar 11, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.CSV;
import com.jascotty2.CheckInput;
import com.jascotty2.Item.Item;
import com.jascotty2.MySQL.MySQL;
import com.jascotty2.Shop.TotalTransaction;
import com.jascotty2.Shop.UserTransaction;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

public class BSTransactionLog {

    protected MySQL MySQLconnection = null;
    protected boolean isLoaded = false;
    // file, if flatfile
    protected File flatFile = null, totalsFlatFile = null;
    // cache of records
    protected ArrayList<UserTransaction> transactions = new ArrayList<UserTransaction>();
    protected ArrayList<TotalTransaction> totalTransactions = new ArrayList<TotalTransaction>();

    public BSTransactionLog() {
        //load();
    } // end default constructor

    public final boolean load() {
        transactions.clear();
        totalTransactions.clear();
        if (BetterShop.config.useMySQL()) {
            // use same connection pricelist is using (pricelist MUST be initialized.. does not check)
            MySQLconnection = BetterShop.pricelist.getMySQLconnection();
            try {
                if (BetterShop.config.logUserTransactions) {
                    if (!MySQLconnection.tableExists(BetterShop.config.transLogTablename)) {
                        BetterShop.config.logUserTransactions = createTransactionLogTable();
                    } else {
                        truncateRecords();
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
            updateCache();
        } else {
            if (BetterShop.config.logUserTransactions) {
                flatFile = new File(BSConfig.pluginFolder.getAbsolutePath() + File.separatorChar + BetterShop.config.transLogTablename + ".csv");
                if (flatFile.exists()) {
                    try {
                        ArrayList<String[]> actFile = CSV.loadFile(flatFile);
                        for (int n = 0; n < actFile.size(); ++n) {//String[] line : actFile) {
                            String[] line = actFile.get(n);
                            if (line.length >=8) {
                                Item plItem = Item.findItem(line[2] + ":" + (line[3].equals(" ") ? "0" : line[3]));
                                if (plItem != null) {
                                    //priceList.add(new PriceListItem(plItem, fields[2].length() == 0 ? -1 : CheckInput.GetDouble(fields[2], -1), fields[3].length() == 0 ? -1 : CheckInput.GetDouble(fields[3], -1)));
                                    transactions.add(new UserTransaction(CheckInput.GetInt(line[0], 0), line[1], plItem,
                                            CheckInput.GetInt(line[5], 0), CheckInput.GetInt(line[6], 0) != 0, CheckInput.GetDouble(line[7], 0)));
                                } else if (n > 0) { // first line is expected invalid: is title
                                    BetterShop.Log(Level.WARNING, String.format("Invalid item on line %d in %s", (n + 1), flatFile.getName()));
                                }
                            } else {
                                BetterShop.Log(Level.WARNING, String.format("unexpected pricelist line at %d in %s", (n + 1), flatFile.getName()));
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        BetterShop.Log(Level.SEVERE, "Unexpected Error: File not found: " + flatFile.getName(), ex);
                    } catch (IOException ex) {
                        BetterShop.Log(Level.SEVERE, "Error opening " + flatFile.getName() + " for reading", ex);
                    }
                    //System.out.println("loaded " + transactions.size());
                } else {
                    // if also loading totals, postpone save
                    if(!BetterShop.config.logTotalTransactions)
                    save();
                }
            }
            if (BetterShop.config.logTotalTransactions) {
                totalsFlatFile = new File(BSConfig.pluginFolder.getAbsolutePath() + File.separatorChar + BetterShop.config.recordTablename + ".csv");
                if (totalsFlatFile.exists()) {
                    try {
                        ArrayList<String[]> actFile = CSV.loadFile(totalsFlatFile);
                        for (int n = 0; n < actFile.size(); ++n) {//String[] line : actFile) {
                            String[] line = actFile.get(n);
                            if (line.length >=6) {
                                Item plItem = Item.findItem(line[1] + ":" + (line[2].equals(" ") ? "0" : line[2]));
                                if (plItem != null) {
                                    //priceList.add(new PriceListItem(plItem, fields[2].length() == 0 ? -1 : CheckInput.GetDouble(fields[2], -1), fields[3].length() == 0 ? -1 : CheckInput.GetDouble(fields[3], -1)));
                                    totalTransactions.add(new TotalTransaction(CheckInput.GetLong(line[0], 0), plItem,
                                            CheckInput.GetLong(line[4], 0), CheckInput.GetLong(line[5], 0)));
                                } else if (n > 0) { // first line is expected invalid: is title
                                    BetterShop.Log(Level.WARNING, String.format("Invalid item on line %d in %s", (n + 1), totalsFlatFile.getName()));
                                }
                            } else {
                                BetterShop.Log(Level.WARNING, String.format("unexpected pricelist line at %d in %s", (n + 1), totalsFlatFile.getName()));
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        BetterShop.Log(Level.SEVERE, "Unexpected Error: File not found: " + totalsFlatFile.getName(), ex);
                    } catch (IOException ex) {
                        BetterShop.Log(Level.SEVERE, "Error opening " + totalsFlatFile.getName() + " for reading", ex);
                    }
                    //System.out.println("loaded " + totalTransactions.size());
                } else {
                    save();
                }
            }
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

    public void addRecord(UserTransaction rec) {
        //System.out.println("add record: " + BetterShop.config.logUserTransactions + "  " + BetterShop.config.logTotalTransactions);
        if (BetterShop.config.logUserTransactions) {
            truncateRecords(false);
            transactions.add(rec);
            //System.out.println("adding " + rec + "  (" + transactions.size());
            if (BetterShop.config.useMySQL()) {
                try {
                    MySQLconnection.RunUpdate("INSERT INTO " + BetterShop.config.sql_database + "." + BetterShop.config.transLogTablename
                            + String.format(" VALUES(UNIX_TIMESTAMP(), '%s', %d, %d, '%s', %d, %d, %.2f);",
                            rec.user, rec.itemNum, rec.itemSub, rec.name, rec.amount, rec.sold ? 1 : 0, rec.price));
                } catch (SQLException e) {
                    BetterShop.Log(Level.SEVERE, "Error inserting transaction data");
                    BetterShop.Log(Level.SEVERE, e);
                }
            } else {
                // append to transaction list & save
                // if going to update Total Transactions as well, postpone save
                if(!BetterShop.config.logTotalTransactions)
                save();
            }
        }
        if (BetterShop.config.logTotalTransactions) {
            
            boolean exst=false;
            for(TotalTransaction t : totalTransactions){
                if(t.itemNum==rec.itemNum && t.itemSub==rec.itemSub){
                    exst=true;
                    if(rec.sold){
                        t.sold+=rec.amount;
                    }else{
                        t.bought+=rec.amount;
                    }
                    break;
                }
            }
            if(!exst){
                totalTransactions.add(new TotalTransaction(rec));
            }

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
            }else{
                save();
            }
        }
    }

    /**
     * removes records older than specified interval
     */
    public void truncateRecords() {
        truncateRecords(true);
    }

    /**
     * 
     * @param saveFile if using flatfile, whether should save on completion
     */
    public void truncateRecords(boolean saveFile) {
        if (BetterShop.config.useMySQL()) {
            try {
                //BetterShop.Log("DELETE FROM " + BetterShop.config.sql_database + "." + BetterShop.config.transLogTablename + " WHERE UNIX_TIMESTAMP() - DATE > " + BetterShop.config.userTansactionLifespan + ";");
                MySQLconnection.RunUpdate("DELETE FROM " + BetterShop.config.sql_database + "." + BetterShop.config.transLogTablename
                        + " WHERE UNIX_TIMESTAMP() - DATE > " + BetterShop.config.userTansactionLifespan + ";");
            } catch (SQLException e) {
                BetterShop.Log(Level.SEVERE, "Error while removing old records");
                BetterShop.Log(Level.SEVERE, e);
            }
            updateCache();
        } else {
            // loop through cache & remove old entries
            long curTime = (new Date()).getTime();
            for (int i = transactions.size() - 1; i > 0; --i) {
                if (transactions.get(i).time - curTime > BetterShop.config.userTansactionLifespan) {
                    transactions.remove(i);
                }
            }
            if (saveFile) {
                save();
            }
        }
    }

    protected void updateCache() {
        if (BetterShop.config.useMySQL()) {
            if (BetterShop.config.logUserTransactions) {
                transactions.clear();
                if (MySQLconnection.IsConnected()) {
                    try {
                        ResultSet table = MySQLconnection.GetQuery(
                                "SELECT * FROM " + BetterShop.config.transLogTablename + "  ORDER BY DATE ASC;");

                        for (table.beforeFirst(); table.next();) {
                            transactions.add(new UserTransaction(table.getInt(1), table.getString(2),
                                    table.getInt(3), table.getInt(4), table.getString(5),
                                    table.getInt(6), table.getByte(7) != 0, table.getDouble(8)));
                        }
                    } catch (SQLException ex) {
                        BetterShop.Log(Level.SEVERE, "Error executing SELECT on " + BetterShop.config.transLogTablename, ex);
                    }
                } else {
                    BetterShop.Log(Level.SEVERE, "Error: MySQL DB not connected");
                }
            }
            // totals shouldn't need to be updated
            //if (BetterShop.config.logTotalTransactions){ }
        } else {
            load();
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
                    + "SOLD  TINYINT NOT NULL,"
                    + "PRICE DECIMAL(11,2),"
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

    public boolean save() {
        if (BetterShop.config.useMySQL()) {
            if (MySQLconnection.IsConnected()) {
                try {
                    MySQLconnection.commit();
                    return true;
                } catch (SQLException ex) {
                    BetterShop.Log(Level.SEVERE, "Error executing COMMIT", ex);
                }
            }
        } else {
            if (BetterShop.config.logUserTransactions) {
                if (flatFile != null && !flatFile.isDirectory()) {
                    ArrayList<String> lines = new ArrayList<String>();
                    lines.add("date,user,id,sub,name,amt,sold,price");
                    for (UserTransaction i : transactions) {
                        lines.add(i.time + "," + i.user + ","
                                + i.itemNum + "," + i.itemSub + "," + i.name + ","
                                + i.amount + "," + (i.sold ? "1" : "0") + "," + i.price);
                    }
                    try {
                        if(!CSV.saveFile(flatFile, lines)){
                            BetterShop.Log(Level.SEVERE, "Error writing to " + flatFile.getName());
                        }
                    } catch (IOException ex) {
                        BetterShop.Log(Level.SEVERE, "Error opening " + flatFile.getName() + " for writing", ex);
                    }

                }else{
                    BetterShop.Log("Error saving activity log: undefined or is directory");
                }
            }

            if (BetterShop.config.logTotalTransactions) {
                if (totalsFlatFile != null && !totalsFlatFile.isDirectory()) {
                    ArrayList<String> lines = new ArrayList<String>();
                    lines.add("date,id,sub,name,sold,bought");
                    for (TotalTransaction i : totalTransactions) {
                        lines.add(i.time + ","
                                + i.itemNum + "," + i.itemSub + "," + i.name + ","
                                + i.sold + "," + i.bought);
                    }
                    try {
                        if(!CSV.saveFile(totalsFlatFile, lines)){
                            BetterShop.Log(Level.SEVERE, "Error writing to " + totalsFlatFile.getName());
                        }
                    } catch (IOException ex) {
                        BetterShop.Log(Level.SEVERE, "Error opening " + totalsFlatFile.getName() + " for writing", ex);
                    }

                }else{
                    BetterShop.Log("Error saving totals log: undefined or is directory");
                }
            }
        }
        return false;
    }
} // end class BSLog

