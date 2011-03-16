/**
 * Programmer: Jacob Scott
 * Program Name: ShopDiscount
 * Description: static class for users/groups and purchase discounts
 * .. this is not integrated into a MySQL database.. might add if requested
 * Date: Mar 12, 2011
 */
package com.jascotty2;

import com.jascotty2.Item.Item;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShopDiscount {

    protected static final Logger logger = Logger.getLogger("Minecraft");
    protected static LinkedList<Discount> discounts = new LinkedList<Discount>();
    protected static boolean isLoaded = false;
    public static String discountFilename = "discounts.csv";

    public static boolean loadFile(String filename) {
        discountFilename = filename;
        return loadFile();
    }

    public static boolean loadFile() {
        if ((new File(discountFilename)).exists()) {
            FileReader fstream = null;
            discounts.clear();
            try {
                fstream = new FileReader(discountFilename);
                BufferedReader in = new BufferedReader(fstream);
                try {
                    int n = 0;
                    in.readLine();// first line is expected invalid: is title
                    for (String line = null; (line = in.readLine()) != null && line.length() > 0 && line.split(",").length > 3; ++n) {
                        String fields[] = line.split(",");
                        Discount d = new Discount();
                        d.isUser = fields[0].equalsIgnoreCase("u");
                        d.discount = CheckInput.GetDouble(fields[1], 0);
                        int i = 2;
                        for (; i < fields.length; ++i) {
                            if (fields[i].length() > 0) {
                                if (fields[i].equalsIgnoreCase("!")) {
                                    break;
                                }
                                // add as allowed user
                                d.addUser(fields[i]);
                            }
                        }
                        for (; i < fields.length; ++i) {
                            if (fields[i].length() > 0) // add as allowed item
                            {
                                d.addItem(Item.findItem(fields[i]));
                            }
                        }
                    }
                } finally {
                    in.close();
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Error opening " + discountFilename + " for reading", ex);
            } finally {
                try {
                    fstream.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Error closing " + discountFilename, ex);
                }
            }
        }
        return isLoaded = true;
    }

    public boolean saveFile() {
        File tosave = new File(discountFilename);
        if (tosave != null && !tosave.isDirectory()) {
            if (!tosave.exists() || tosave.canWrite()) {
                FileWriter fstream = null;
                try {
                    fstream = new FileWriter(tosave.getAbsolutePath());
                    BufferedWriter out = new BufferedWriter(fstream);
                    out.write("type,discount,users/groups,items,! denotes start of item list");
                    out.newLine();
                    for (Discount i : discounts) {
                        out.write((i.isUser ? "u," : "g,") + i.discount + "," + i.getUserListStr() + ",!," + i.getItemListStr());
                        out.newLine();
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Error opening " + discountFilename + " for writing", ex);
                } finally {
                    try {
                        fstream.close();
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, "Error closing " + discountFilename, ex);
                    }
                }
            }
        }
        return false;
    }
} // end class ShopDiscount

