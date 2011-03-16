/**
 * Programmer: Jacob Scott
 * Program Name: ItemDB
 * Description: searchable database for items
 * Date: Mar 8, 2011
 */
package com.jascotty2.Item;

import com.jascotty2.CheckInput;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

public class ItemDB extends Item {

    private final static Logger logger = Logger.getLogger("Minecraft");
    // kits are also saved in Item.items for compatibility (convenience) with id's starting at 5000
    private static Map<Integer, Kit> kits = new HashMap<Integer, Kit>();
    private static boolean dbLoaded = false;

    public static boolean load() {
        return load(new File("."), "itemsdb.yml");
    }

    public static boolean load(File folder) {
        return load(folder, "itemsdb.yml");
    }

    public static boolean load(File folder, String fname) {
        try {
            folder.mkdirs();
            File file = new File(folder, fname);
            if (!file.exists()) {
                file.createNewFile();
                InputStream res = ItemDB.class.getResourceAsStream("/itemsdb.yml");
                FileWriter tx = new FileWriter(file);
                try {
                    for (int i = 0; (i = res.read()) > 0;) {
                        tx.write(i);
                    }
                } finally {
                    tx.flush();
                    tx.close();
                    res.close();
                }
            }

            Configuration itemdb = new Configuration(file);
            itemdb.load();
            
            ConfigurationNode n = itemdb.getNode("items");
            if (n == null) {
                logger.log(Level.SEVERE, "\'items\' not found in itemsdb.yml");
            } else {
                items.clear();
                // will run through items twice: 1st load items, then the craft recipes
                for (String k : itemdb.getKeys("items")) {
                    if (k.length() >= 5 && k.substring(0, 4).equalsIgnoreCase("item")) {
                        n = itemdb.getNode("items." + k);
                        if (n != null) {
                            Item item = new Item();
                            item.name = n.getString("name", "null").toLowerCase();
                            item.SetColor(n.getString("color"));
                            item.SetMaxStack(n.getInt("maxstack", 64));
                            item.isLegal = n.getBoolean("legal", true);
                            String itemidd = "0";
                            if (k.indexOf("sub") > 0) {
                                itemidd = k.substring(4, k.indexOf("sub")) + ":" + k.substring(k.indexOf("sub") + 3);
                                // will also have a sub-value alias (ex: wool:green)
                                String a = n.getString("sub");
                                if (a != null) {
                                    String all[] = a.split(",");
                                    for (String i : all) {
                                        item.AddSubAlias(i.trim().toLowerCase());
                                    }
                                }
                            } else {
                                itemidd = k.substring(4) + ":0";
                            }
                            // now add aliases
                            String a = n.getString("aliases");
                            if (a != null) {
                                String all[] = a.split(",");
                                for (String i : all) {
                                    item.AddAlias(i.trim().toLowerCase());
                                }
                            }
                            item.setIDD(itemidd);
                            items.put(itemidd, item);
                            //System.out.println("Added: " + item);
                        }
                    }

                }
                // now load craft recipes
                for (String k : itemdb.getKeys("items")) {
                    if (k.length() >= 5 && k.substring(0, 4).equalsIgnoreCase("item")) {
                        n = itemdb.getNode("items." + k);
                        if (n != null) {
                            Item item = Item.findItem(n.getString("name"));
                            String a = n.getString("craft");
                            if (a != null && item != null) {
                                String all[] = a.split(",");
                                for (String i : all) {
                                    CraftRecipe toAdd = CraftRecipe.fromStr(i.trim());
                                    if (toAdd != null) {
                                        item.AddRecipe(toAdd);
                                    } else {
                                        logger.log(Level.WARNING, String.format("%s has an invalid item or craft syntax error: %s", item.toString(), i));
                                    }
                                }
                            }
                        }
                    }
                }

                n = itemdb.getNode("kits");
                if (n == null) {
                    logger.log(Level.WARNING, "\'kits\' not found in itemsdb.yml");
                } else {
                    kits.clear();
                    for (String k : itemdb.getKeys("kits")) { // for( : itemdb.getNodeList("", null)){
                        if (k.length() >= 4 && k.substring(0, 3).equalsIgnoreCase("kit")) {
                            n = itemdb.getNode("kits." + k);
                            if (n != null) {
                                String a = n.getString("items");
                                if (a != null) {
                                    Kit kit = Kit.fromStr(a);
                                    if (kit == null) {
                                        logger.log(Level.WARNING, String.format("kit %s has an invalid item syntax", k));
                                    } else if (n.getString("name", "").length() == 0) {
                                        logger.log(Level.WARNING, String.format("kit %s has no name", k));
                                    } else {
                                        // kits are numbered starting at 5000
                                        kit.itemId = 4999 + CheckInput.GetInt(k.substring(3), 0);
                                        kit.SetColor(n.getString("color"));
                                        if (kit.itemId == 4999) {
                                            logger.log(Level.WARNING, String.format("%s is an invalid kit number. (Must start at 1)", k));
                                            continue;  //next kit node
                                        }
                                        kit.name = n.getString("name", "null").toLowerCase();
                                        // now add aliases (if any)
                                        a = n.getString("aliases");
                                        if (a != null) {
                                            String all[] = a.split(",");
                                            for (String i : all) {
                                                kit.AddAlias(i.trim().toLowerCase());
                                            }
                                        }
                                        // add to list
                                        kits.put(kit.itemId, kit);
                                        // add to item list
                                        items.put(kit.IdDatStr(), kit);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //logger.log(Level.INFO, "Items loaded: " + items.size() + " + " + kits.size() + " kits");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading itemsdb.yml", ex);
            return dbLoaded = false;
        }
        return dbLoaded = true;
    }

    public boolean isLoaded() {
        return dbLoaded;
    }

    public static Item GetItem(String search) {
        if (!dbLoaded) {
            load();
        }
        return findItem(search);
    }

    public static String GetItemIdDat(String search) {
        if (!dbLoaded) {
            load();
        }
        Item searchi = findItem(search);
        return searchi == null ? "" : searchi.IdDatStr(); // searchi.toString(); // 
    }

    public static String GetItemName(ItemStack search){
        if (!dbLoaded) {
            load();
        }
        Item searchi = findItem(search);
        return searchi == null ? "" : searchi.name;
    }

    public static boolean isItem(String search) {
        Item temp = Item.findItem(search);
        return temp != null && temp.itemId < 5000;
    }

    public static Kit getKit(String search) {
        /*if (kits.containsKey(search)) {
            return kits.get(search);
        }*/
        for (Kit k : kits.values()) {
            if (k.equals(search)) {
                // debugging: output items in kit
                /*
                System.out.println("kit " + search + " found: ");
                for (Kit.KitItem i : k.getKitItems()) {
                System.out.println(i + "( " + i.itemAmount + " )");
                }//*/
                return k;
            }
        }
        return null;
    }

    public static Kit getKit(Item search) {
        /*if (!search.isKit()) {
            return null;
        }*/
        return kits.get(search.itemId);
    }

    public static boolean isKit(String search) {
        return getKit(search) != null;
    }
}
