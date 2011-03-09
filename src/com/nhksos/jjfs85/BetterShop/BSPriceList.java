package com.nhksos.jjfs85.BetterShop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.material.MaterialData;
import org.bukkit.util.config.*;

public class BSPriceList {

    private final static Logger logger = Logger.getLogger("Minecraft");
    final Map<Double, Double> BuyMap = new HashMap<Double, Double>();
    final Map<Double, Double> SellMap = new HashMap<Double, Double>();
    final Map<Double, String> NameMap = new HashMap<Double, String>();
    Set<Double> ItemMap = new TreeSet<Double>();
    final LinkedList<String> keys = new LinkedList<String>();
    private File PLfile, PLfolder;
    private String fileName = "PriceList.yml";
    private Configuration PriceList;
    private boolean isLoaded = false;
    private boolean isMySQL = false;
    public String sql_database, sql_tableName, sql_username, sql_password, sql_hostName, sql_portNum;
    BSMySQL MySQLPriceList = null;
    public int pagesize = 9;

    public static <T extends Comparable<? super T>> List<T> asSortedList(
            Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }

    public BSPriceList() {
    }

    public BSPriceList(File pluginFolder, String fileName) {
        load(pluginFolder, fileName);
    }

    public BSPriceList(String database, String tableName, String username, String password, String hostName, String portNum) {
        load(database, tableName, username, password, hostName, portNum);
    }

    public boolean HasAccess() {
        return isLoaded;
    }

    public String pricelistName() {
        return isMySQL ? (sql_hostName.compareToIgnoreCase("localhost") == 0 ? "" : sql_hostName) + "/" + sql_database + "/" + sql_tableName : fileName;
    }

    public void reload() {
        if (isMySQL) {
            load(PLfolder, fileName);
        } else {
            load(sql_database, sql_tableName, sql_username, sql_password, sql_hostName, sql_portNum);
        }
    }

    public boolean load(String database, String tableName, String username, String password, String hostName, String portNum) {
        isLoaded = false;
        isMySQL = true;

        sql_database = database;
        sql_tableName = tableName;
        sql_username = username;
        sql_password = password;
        sql_hostName = hostName;
        sql_portNum = portNum;

        BuyMap.clear();
        SellMap.clear();
        NameMap.clear();

        ItemMap.clear();
        keys.clear();

        // try connecting to database
        // todo: add option to cache database.. not what i want, so i'm not adding here
        MySQLPriceList = new BSMySQL(database, tableName, username, password, hostName, portNum);
        if (MySQLPriceList.IsConnected()) {
            /*
            LinkedList<BS_SQL_Data> tableDat = MySQLPriceList.GetFullList();

            for (int i = 0; i < keys.size(); ++i) {
            BS_SQL_Data row = tableDat.get(i);
            double d = row.itemNum + (row.itemSub * .01);
            BuyMap.put(d, row.buy);
            SellMap.put(d, row.sell);
            NameMap.put(d, row.name);
            }
            ItemMap.addAll(NameMap.keySet());
             */
            logger.log(Level.INFO, "MySQL database " + pricelistName() + " loaded.");
            isLoaded = true;
            return true;
        } else {
            return false;
        }
    }

    public boolean load(File PLpath, String fileName) {
        isLoaded = false;
        isMySQL = false;
        PLfolder = PLpath;
        PLfile = new File(PLpath, fileName);
        if (!PLfile.getParentFile().exists()) {
            logger.log(Level.INFO, "Creating " + PLpath.getAbsolutePath());
            PLpath.mkdirs();
        }
        if (!PLfile.exists()) {
            try {
                logger.log(Level.INFO, "Creating " + PLfile.getAbsolutePath());
                PLfile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(BSPriceList.class.getName()).log(Level.SEVERE, "Error creating " + PLfile.getAbsolutePath(), ex);
                return false;
            }
        }
        logger.log(Level.INFO, "Loading " + fileName);
        PriceList = new Configuration(PLfile);
        PriceList.load();
        BuyMap.clear();
        SellMap.clear();
        NameMap.clear();

        keys.clear();

        try {
            keys.addAll(PriceList.getKeys("prices"));
        } catch (Exception e0) {
            logger.info("Empty PriceList or error reading PriceList");
            return false;
        }
        for (int i = 0; i < keys.size(); ++i) {
            double buy = -1.003;
            double sell = -1.003;
            String name = "Unk";
            String[] split = keys.get(i).split("[^0-9]");
            if (split.length != 0) {
                int id = 0;
                int sub = 0;
                if (split.length == 8) {
                    id = CheckInput.GetInt(split[split.length - 4], 0);
                    sub = CheckInput.GetInt(split[split.length - 1], 0);
                } else {
                    id = CheckInput.GetInt(split[split.length - 1], 0);
                }
                if (keys.contains("item" + String.valueOf(id) + "sub" + String.valueOf(sub))) {
                    buy = PriceList.getDouble("prices.item" + String.valueOf(id)
                            + "sub" + String.valueOf(sub) + ".buy", -1);
                    sell = PriceList.getDouble("prices.item" + String.valueOf(id)
                            + "sub" + String.valueOf(sub) + ".sell", -1);
                    name = PriceList.getString("prices.item" + String.valueOf(id)
                            + "sub" + String.valueOf(sub) + ".name", "Unk");
                } else if (keys.contains("item" + String.valueOf(id))) {
                    buy = PriceList.getDouble("prices.item" + String.valueOf(id) + ".buy", -1);
                    sell = PriceList.getDouble("prices.item" + String.valueOf(id) + ".sell", -1);
                    name = PriceList.getString("prices.item" + String.valueOf(id) + ".name", "Unk");
                }
                if ((buy != -1.003) && (sell != -1.003)) {
                    double d = id + (sub * .01);
                    BuyMap.put(d, buy);
                    SellMap.put(d, sell);
                    NameMap.put(d, name);
                }
            }
        }
        ItemMap.clear();
        ItemMap.addAll(NameMap.keySet());
        logger.log(Level.INFO, fileName + " loaded.");
        isLoaded = true;
        return true;
    }

    public boolean isForSale(String s) { // throws Exception
        if (isMySQL) {
            return MySQLPriceList.ItemExists(s);
        } else {
            double i;
            try {
                i = itemDb.get(s).getItemTypeId() + (double) itemDb.get(s).getData() / 100;
            } catch (Exception ex) {
                //logger.log(Level.SEVERE, null, ex);
                return false;
            }
            return isForSale(i);
        }
    }

    public boolean isForSale(double i) {
        if (isMySQL) {
            return MySQLPriceList.ItemExists(i);
        } else {
            return NameMap.containsKey(i);
        }
    }

    public boolean itemExists(double i) {
        return isForSale(i);
    }

    public double getBuyPrice(String s) { //  throws Exception
        //logger.log(Level.INFO, "check buy: " + isMySQL);
        if (isMySQL) {
            return MySQLPriceList.GetItem(s).buy;
        } else {
            try {
                double i = itemDb.get(s).getItemTypeId() + (double) itemDb.get(s).getData() / 100;
                return getBuyPrice(i);
            } catch (Exception ex) {
                //logger.log(Level.SEVERE, null, ex);
                return -1;
            }
        }
    }

    public double getBuyPrice(double i) { // throws Exception 
        if (isMySQL) {
            return MySQLPriceList.GetItem(i).buy;
        } else {
            if (NameMap.containsKey(i)) {
                return BuyMap.get(i);
            } else {
                return -1; //throw new Exception();
            }
        }
    }

    public double getSellPrice(String s) { // throws Exception
        if (isMySQL) {
            return MySQLPriceList.GetItem(s).sell;
        } else {
            try {
                double i = itemDb.get(s).getItemTypeId() + (double) itemDb.get(s).getData() / 100;
                return getSellPrice(i);
            } catch (Exception ex) {
                //logger.log(Level.SEVERE, null, ex);
                return -1;
            }
        }
    }

    public double getSellPrice(double i) { // throws Exception
        if (isMySQL) {
            return MySQLPriceList.GetItem(i).sell;
        } else {
            if (NameMap.containsKey(i)) {
                return SellMap.get(i);
            } else {
                return -1; // throw new Exception();
            }
        }
    }

    public boolean setPrice(String item, String b, String s) { // throws Exception
        // try to parse... hunt for exception...
        //Double.parseDouble(b);
        //Double.parseDouble(s);
        MaterialData itemInfo;
        try {
            itemInfo = itemDb.get(item);
        } catch (Exception ex) {
            //logger.log(Level.SEVERE, null, ex);
            return false;
        }
        double i = itemInfo.getItemTypeId() + (double) itemInfo.getData() / 100;
        if (isMySQL) {
            /*
            if (CheckInput.IsInt(item.contains(":") ? item.substring(0, item.indexOf(":")) : item)) {
            item = Double.toString(i);
            }
            MySQLPriceList.SetPrice(item, Double.parseDouble(b), Double.parseDouble(s));
            //*/
            //MySQLPriceList.SetPrice(i, Double.parseDouble(b), Double.parseDouble(s));
            //logger.log(Level.INFO, Double.toString(i));
            MySQLPriceList.SetPrice(i, CheckInput.GetDouble(b, -1), CheckInput.GetDouble(s, -1));
        } else {
            try {

                if (NameMap.containsKey(i)) {
                    BuyMap.remove(i);
                    SellMap.remove(i);
                    NameMap.remove(i);
                }
                BuyMap.put(i, CheckInput.GetDouble(b, -1));//Double.parseDouble(b));
                SellMap.put(i, CheckInput.GetDouble(s, -1));//Double.parseDouble(s));
                NameMap.put(i, itemDb.getName(i));
                save();
            } catch (Exception ex) {
                //logger.log(Level.SEVERE, "Error looking up itemname of " + i, ex);
                return false;
            }
        }
        return true;
    }

    public void remove(String s) throws Exception {
        // throws exception if item not known
        MaterialData matdat = itemDb.get(s);
        if (isMySQL) {
            MySQLPriceList.RemoveItem(s);
        } else {
            if (NameMap.containsKey(matdat.getItemTypeId()
                    + (double) matdat.getData() / 100)) {
                BuyMap.remove(matdat.getItemTypeId() + (double) matdat.getData() / 100);
                SellMap.remove(matdat.getItemTypeId() + (double) matdat.getData() / 100);
                NameMap.remove(matdat.getItemTypeId() + (double) matdat.getData() / 100);
            }
            save();
        }
    }

    private void save() {
        if (isMySQL) {
            MySQLPriceList.commit();
        } else {
            BufferedWriter output = null;
            ItemMap.clear();
            ItemMap.addAll(NameMap.keySet());
            try {
                output = new BufferedWriter(new FileWriter(PLfile));
            } catch (IOException e1) {
                logger.log(Level.WARNING, "Cannot write to " + PLfile.getName());
                e1.printStackTrace();
            }
            try {
                // FileWriter always assumes default encoding is OK!
                output.write("prices:");
                output.newLine();
                Iterator<Double> ItemIt = ItemMap.iterator();
                while (ItemIt.hasNext()) {
                    double key = ItemIt.next();
                    int item = (int) Math.floor(key);
                    int sub = (int) ((key - item) * 100);
                    output.write(String.format("  item%01dsub%01d:", item, sub));
                    output.newLine();
                    output.write("    name: " + NameMap.get(key).toLowerCase());
                    output.newLine();
                    output.write("    buy: " + BuyMap.get(key));
                    output.newLine();
                    output.write("    sell: " + SellMap.get(key));
                    output.newLine();
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Cannot write to " + PLfile.getName());
                e.printStackTrace();
            } finally {
                try {
                    output.flush();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public LinkedList<String> GetShopListPage(int pageNum) {
        LinkedList<String> ret = new LinkedList<String>();
        if (isMySQL) {
            LinkedList<BS_SQL_Data> tableDat = MySQLPriceList.GetFullList();

            int pages = (int) Math.ceil((double) tableDat.size() / pagesize);
            String listhead = BetterShop.configfile.getString("listhead").replace("<page>", String.valueOf(pageNum)).replace("<pages>", String.valueOf(pages));
            if (pageNum > pages) {
                ret.add("There is no page " + pageNum + ".");
            } else {
                ret.add(String.format(listhead, pageNum, pages));
                String listStr = BetterShop.configfile.getString("listing").replace("<item>", "%1$s").replace("<buyprice>", "%2$s").replace("<sellprice>", "%3$s");
                //                .replace("<tab>", "\t");
                for (int i = pagesize * (pageNum - 1), n = 0; n < pagesize && i < tableDat.size(); ++i, ++n) {
                    ret.add(String.format(listStr, tableDat.get(i).name,
                            tableDat.get(i).buy <= 0 ? "No" : String.format("%5s", String.format("%01.2f", tableDat.get(i).buy)),
                            tableDat.get(i).sell <= 0 ? "No" : String.format("%5s", String.format("%01.2f", tableDat.get(i).sell))));
                }

            }
        } else {
            // old method, pagenum fixed
            int pages = (int) Math.ceil((double) NameMap.size() / pagesize);
            String listhead = BetterShop.configfile.getString("listhead").replace(
                    "<page>", String.valueOf(pageNum)).replace("<pages>",
                    String.valueOf(pages));
            if (pageNum > pages) {
                ret.add("There is no page " + pageNum + ".");
            } else {
                ret.add(String.format(listhead, pageNum, pages));
                int linenum = 0;
                Iterator<Double> iter = ItemMap.iterator();
                while ((linenum < (pageNum - 1) * pagesize) && iter.hasNext()) {
                    iter.next();
                    ++linenum;
                }
                while ((linenum < pageNum * pagesize) && (iter.hasNext())) {
                    Double i = iter.next();
                    try {
                        String sellStr = (getSellPrice(i) <= 0) ? "No"
                                : String.format("%01.2f", getSellPrice(i));
                        String buyStr = (getBuyPrice(i) < 0) ? "No"
                                : String.format("%01.2f", getBuyPrice(i));
                        ret.add(String.format(
                                BetterShop.configfile.getString("listing").replace(
                                "<item>", "%1$s").replace("<buyprice>",
                                "%2$s").replace("<sellprice>", "%3$s"), //.replace("<tab>", "     "),
                                itemDb.getName(i),
                                buyStr, sellStr));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ++linenum;
                }
            }
        }
        // format spaces
        // todo: dynamic number spacing, depending on largest number
        //      (easier not to do here, but in string formatting above)
        if (ret.size() > 1 && ret.get(1).contains("<tab>")) {
            // go through each line:
            // - max pos of first found, then space all to be that length
            while (ret.get(1).contains("<tab>")) {
                int maxPos = 0;
                // todo? count chars with pixel width.. minecraft chat does not use fixed-width font
                for (int i = 1; i < ret.size(); ++i) {
                    if (ret.get(i).indexOf("<tab>") > maxPos) {
                        maxPos = ret.get(i).indexOf("<tab>");
                    }
                }
                LinkedList<String> newret = new LinkedList<String>();
                for (int i = 0; i < ret.size(); ++i) {
                    String line = ret.get(i);
                    if (line.indexOf("<tab>") != -1) {
                        newret.add(String.format("%" + maxPos + "s %s", line.substring(0, line.indexOf("<tab>")), line.substring(line.indexOf("<tab>") + 5)));
                    } else {
                        newret.add(line);
                    }
                }
                ret = newret;
            }
        }

        ret.add(BetterShop.configfile.getString("listtail"));
        return ret;
    }
}
