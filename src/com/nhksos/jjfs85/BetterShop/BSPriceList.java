package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.CheckInput;
import com.jascotty2.MySQL.PriceList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import org.bukkit.material.MaterialData;
import org.bukkit.util.config.*;

public class BSPriceList {

    final Map<Double, Double> BuyMap = new HashMap<Double, Double>();
    final Map<Double, Double> SellMap = new HashMap<Double, Double>();
    final Map<Double, String> NameMap = new HashMap<Double, String>();
    Set<Double> ItemMap = new TreeSet<Double>();
    private String fileName = "PriceList.yml";
    private boolean isLoaded = false;
    private boolean isMySQL = false;
    BSMySQL MySQLPriceList = new BSMySQL();

    public static <T extends Comparable<? super T>> List<T> asSortedList(
            Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }

    public BSPriceList() {
        // load the pricelist.
        if (BetterShop.config.useMySQL()) {
            //isMySQL = true;
            loadMySQL();
        } else {
            loadFile();
        }
    }

    public boolean HasAccess() {
        return isLoaded;
    }

    public String pricelistName() {
        return isMySQL
                ? ((BetterShop.config.sql_hostName.compareToIgnoreCase("localhost") == 0 ? ""
                : BetterShop.config.sql_hostName) + "/"
                + BetterShop.config.sql_database + "/"
                + BetterShop.config.sql_tableName) : fileName;
    }

    public boolean reload() {
        if (isMySQL) {
            return loadMySQL();
        } else {
            return loadFile();
        }
    }

    public final boolean loadMySQL() {
        isLoaded = false;
        isMySQL = true;

        if (MySQLPriceList.connect()) {

            BuyMap.clear();
            SellMap.clear();
            NameMap.clear();

            ItemMap.clear();

            if (MySQLPriceList.IsConnected()) {
                // todo: add option to cache database.. not what i want, so i'm not adding here
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
                BetterShop.Log(Level.INFO, "MySQL database " + pricelistName() + " loaded.");
            } else {
                return false;
            }
        } else {
            // here reload cache if enabled (future)
            /*
            BuyMap.clear();
            SellMap.clear();
            NameMap.clear();
            
            ItemMap.clear();
            keys.clear();
             */
        }
        return isLoaded = true;
    }

    public final boolean loadFile() {
        isLoaded = false;
        isMySQL = false;
        File PLfile = new File(BSConfig.pluginFolder, fileName);
        if (!PLfile.exists()) {
            try {
                BetterShop.Log(Level.INFO, "Creating " + PLfile.getAbsolutePath());
                PLfile.createNewFile();
                return isLoaded = true;
            } catch (IOException ex) {
                BetterShop.Log(Level.SEVERE, "Error creating " + PLfile.getAbsolutePath(), ex);
                return false;
            }
        }
        BetterShop.Log(Level.INFO, "Loading " + fileName);

        Configuration PriceList = new Configuration(PLfile);
        PriceList.load();
        BuyMap.clear();
        SellMap.clear();
        NameMap.clear();
        ItemMap.clear();

        LinkedList<String> keys = new LinkedList<String>();

        if (PriceList.getNode("prices") != null) {
            keys.addAll(PriceList.getKeys("prices"));
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
                        name = PriceList.getString("prices.item" + String.valueOf(id) + ".name", name);
                    }
                    if ((buy != -1.003) && (sell != -1.003)) {
                        double d = id + (sub * .01);
                        BuyMap.put(d, buy);
                        SellMap.put(d, sell);
                        NameMap.put(d, name);
                    }
                }
            }
            ItemMap.addAll(NameMap.keySet());
        }
        BetterShop.Log(Level.INFO, fileName + " loaded.");
        return isLoaded = true;
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

    public boolean remove(String s) {
        try {
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
        } catch (Exception ex) {
            //logger.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public boolean remove(double i) { // throws Exception
        try {
            MaterialData matdat = itemDb.get(i);
            if (isMySQL) {
                MySQLPriceList.RemoveItem(itemDb.getName(matdat));
            } else {
                if (NameMap.containsKey(matdat.getItemTypeId() + (double) matdat.getData() / 100)) {
                    BuyMap.remove(matdat.getItemTypeId() + (double) matdat.getData() / 100);
                    SellMap.remove(matdat.getItemTypeId() + (double) matdat.getData() / 100);
                    NameMap.remove(matdat.getItemTypeId() + (double) matdat.getData() / 100);
                }
                save();
            }
        } catch (Exception ex) {
            //logger.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    private void save() {
        if (isMySQL) {
            MySQLPriceList.commit();
        } else {
            BufferedWriter output = null;
            try {
                output = new BufferedWriter(new FileWriter(BSConfig.pluginFolder));
                ItemMap.clear();
                ItemMap.addAll(NameMap.keySet());
                // FileWriter always assumes default encoding is OK!
                output.write("prices:\n");
                Iterator<Double> ItemIt = ItemMap.iterator();
                while (ItemIt.hasNext()) {
                    double key = ItemIt.next();
                    int item = (int) Math.floor(key);
                    int sub = (int) ((key - item) * 100);
                    output.write(String.format("  item%01dsub%01d:%n", item, sub));
                    output.write("    name: " + NameMap.get(key).toLowerCase() + "\n");
                    output.write("    buy: " + BuyMap.get(key) + "\n");
                    output.write("    sell: " + SellMap.get(key) + "\n");
                }
            } catch (Exception e) {
                BetterShop.Log(Level.SEVERE, "Cannot write to " + BSConfig.pluginFolder.getName(), e);
            } finally {
                if (output != null) {
                    try {
                        output.flush();
                        output.close();
                    } catch (IOException e) {
                        BetterShop.Log(Level.SEVERE, "Error closing " + BSConfig.pluginFolder.getName(), e);
                    }
                }
            }
        }
    }

    public LinkedList<String> GetShopListPage(int pageNum, boolean isPlayer) {
        LinkedList<String> ret = new LinkedList<String>();
        if (isMySQL) {
            LinkedList<PriceList> tableDat = MySQLPriceList.GetFullList();

            int pages = (int) Math.ceil((double) tableDat.size() / BetterShop.config.pagesize);
            String listhead = BetterShop.config.getString("listhead").replace("<page>", String.valueOf(pageNum)).replace("<pages>", String.valueOf(pages));
            if (pageNum > pages) {
                ret.add("There is no page " + pageNum + ".");
            } else {
                ret.add(String.format(listhead, pageNum, pages));
                String listStr = BetterShop.config.getString("listing").replace("<item>", "%1$s").replace("<buyprice>", "%2$s").replace("<sellprice>", "%3$s");
                //                .replace("<tab>", "\t");
                for (int i = BetterShop.config.pagesize * (pageNum - 1), n = 0; n < BetterShop.config.pagesize && i < tableDat.size(); ++i, ++n) {
                    ret.add(String.format(listStr, tableDat.get(i).name,
                            tableDat.get(i).buy <= 0 ? "No" : String.format("%5s", String.format("%01.2f", tableDat.get(i).buy)),
                            tableDat.get(i).sell <= 0 ? "No" : String.format("%5s", String.format("%01.2f", tableDat.get(i).sell))));
                }

            }
        } else {
            int pages = (int) Math.ceil((double) NameMap.size() / BetterShop.config.pagesize);
            String listhead = BetterShop.config.getString("listhead").replace(
                    "<page>", String.valueOf(pageNum)).replace("<pages>",
                    String.valueOf(pages));
            if (pageNum > pages) {
                ret.add("There is no page " + pageNum + ".");
            } else {
                ret.add(String.format(listhead, pageNum, pages));
                int linenum = 0;
                Iterator<Double> iter = ItemMap.iterator();
                for (; (linenum < (pageNum - 1) * BetterShop.config.pagesize) && iter.hasNext(); ++linenum) {
                    iter.next();
                }
                while ((linenum < pageNum * BetterShop.config.pagesize) && (iter.hasNext())) {
                    Double i = iter.next();
                    try {
                        String sellStr = (getSellPrice(i) <= 0) ? "No"
                                : String.format("%01.2f", getSellPrice(i));
                        String buyStr = (getBuyPrice(i) < 0) ? "No"
                                : String.format("%01.2f", getBuyPrice(i));
                        ret.add(String.format(
                                BetterShop.config.getString("listing").replace(
                                "<item>", "%1$s").replace("<buyprice>",
                                "%2$s").replace("<sellprice>", "%3$s"), //.replace("<tab>", "     "),
                                itemDb.getName(i),
                                buyStr, sellStr));
                    } catch (Exception e) {
                        //e.printStackTrace();
                        BetterShop.Log("Error while loading pricelist", e);
                    }
                    ++linenum;
                }
            }
        }
        if (isPlayer) {
            // format spaces
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
        }
        ret.add(BetterShop.config.getString("listtail"));
        return ret;
    }
}
