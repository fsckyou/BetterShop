/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: static class for users/groups and purchase discounts
 * .. this is not integrated into a MySQL database.. might add if requested
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

import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.bukkit.item.JItemDB;
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
                                d.addItem(JItemDB.findItem(fields[i]));
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

