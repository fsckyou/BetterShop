/**
/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: used to store the items used to craft another
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
package me.jascotty2.lib.bukkit.item;

import me.jascotty2.lib.io.CheckInput;
import java.util.LinkedList;

public class CraftRecipe { //  extends Kit

    protected LinkedList<Kit> items = new LinkedList<Kit>();
    public int resultAmount;
    //public JItem resultItem;

    public CraftRecipe() {
    } // end default constructor

    public CraftRecipe(String recipeStr) {
        SetRecipe(fromStr(recipeStr));
    }

    public void AddItem(JItem toAdd) {
        if (toAdd != null) {
            items.add(new Kit(toAdd));
        }
    }

    public void AddItem(JItem toAdd, int numUsed) {
        if (toAdd != null && numUsed > 0) {
            items.add(new Kit(toAdd, numUsed));
        }
    }

    public void AddItem(JItems toAdd) {
        if (toAdd != null) {
            items.add(new Kit(new JItem(toAdd)));
        }
    }

    public void AddItem(JItems toAdd, int numUsed) {
        if (toAdd != null && numUsed > 0) {
            items.add(new Kit(new JItem(toAdd), numUsed));
        }
    }

    public void AddItem(int id) {
        if (id > 0) {
            JItem i = new JItem();
            i.itemId = id;
            items.add(new Kit(i));
        }
    }

    public void AddItem(int id, byte dat) {
        if (id > 0) {
            JItem i = new JItem();
            i.itemId = id;
            i.itemDat = dat;
            items.add(new Kit(i));
        }
    }
    public void AddItem(int id, int numUsed) {
        if (id > 0 && numUsed > 0) {
            JItem i = new JItem();
            i.itemId = id;
            items.add(new Kit(i, numUsed));
        }
    }

    public void AddItem(int id, byte dat, int numUsed) {
        if (id > 0 && numUsed > 0) {
            JItem i = new JItem();
            i.itemId = id;
            i.itemDat = dat;
            items.add(new Kit(i, numUsed));
        }
    }

    /**
     * adds without checking if is a valid item
     * @param toAdd
     */
    public void AddNewItem(JItem toAdd) {
        if (toAdd != null) {
            Kit nk = new Kit();
            nk.AddNewItem(toAdd);
            items.add(nk);
        }
    }

    /**
     * adds without checking if is a valid item
     * @param toAdd
     * @param numUsed
     */
    public void AddNewItem(JItem toAdd, int numUsed) {
        if (toAdd != null && numUsed > 0) {
            Kit nk = new Kit();
            nk.AddNewItem(toAdd, numUsed);
            items.add(nk);
        }
    }

    public final void SetRecipe(CraftRecipe copy) {
        items.clear();
        if (copy != null) {
            items.addAll(copy.items);
        }
    }

    public final void SetRecipe(String craftStr) {
        items.clear();
        if (craftStr != null) {
            CraftRecipe n = fromStr(craftStr);
            if (n != null) {
                items.addAll(n.items);
            }
        }
    }

    public static CraftRecipe fromStr(String craftStr) {
        if (craftStr == null) {
            return null;
        }
        // ex: 4@8+263=8
        CraftRecipe ret = new CraftRecipe();

        // get result amount
        if (craftStr.contains("=")) {
            if (craftStr.split("=").length > 2 || craftStr.length() == craftStr.indexOf("=")) {
                return null;
            }
            ret.resultAmount = CheckInput.GetInt(craftStr.substring(craftStr.indexOf("=") + 1), 0);
            craftStr = craftStr.substring(0, craftStr.indexOf("="));
        } else {
            ret.resultAmount = 1;
        }
        // extract all items
        for (String i : craftStr.split("\\+")) {
            JItem ni = null;
            String it = i;
            if (i.contains("@")) {
                if (i.length() > i.indexOf("@")) {
                    it = i.substring(0, i.indexOf("@"));
                    ni = JItemDB.findItem(it);
                    ret.AddItem(ni,
                            CheckInput.GetInt(i.substring(i.indexOf("@") + 1), 0));
                } else {
                    it = i.substring(0, i.indexOf("@"));
                    ni = JItemDB.findItem(it);
                    ret.AddItem(ni);
                }
            } else {
                ni = JItemDB.findItem(i);
                ret.AddItem(ni);
            }
            if (ni == null) {
                System.out.println("null item: " + it);
            }
        }
        return ret.totalItems() == 0 ? null : ret;
    }

    // from kit class
    public int numItems() {
        return items.size();
    }

    public int totalItems() {
        int n = 0;
        for (Kit k : items) {
            n += k.totalItems();
        }
        return n;
    }

    public JItem getItem(int index) {
        if (index >= 0 && index < items.size()) {
            Kit k = items.get(index);
            return new JItem(k.ID(), k.Name());
        }
        return null;
    }

    public int getItemCount(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index).totalItems();
        }
        return 0;
    }

    public int getItemCount(JItem i) {
        for (Kit k : items) {
            if (k.ID() == i.ID()) { //if (((JItem) k).equals(i)) {
                return k.totalItems();
            }
        }
        return 0;
    }

    public JItem[] getItems() {
        return items.toArray(new JItem[0]);
    }

    public Kit[] getKits() {
        return items.toArray(new Kit[0]);
    }

    public Kit.KitItem[] getKitItems() {
        Kit.KitItem[] ret = new Kit.KitItem[numItems()];
        for (int i = 0, n = 0; i < items.size(); ++i) {
            for (int j = 0; j < items.get(i).numItems(); ++j, ++n) {
                ret[n] = items.get(i).getKitItem(j);
            }
        }
        return ret;
    }
} // end class CraftRecipe

