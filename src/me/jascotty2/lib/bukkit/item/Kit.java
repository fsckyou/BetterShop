/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: provides a class for kits (collection of items)
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
import org.bukkit.inventory.ItemStack;

public class Kit extends JItem {

    private LinkedList<KitItem> kititems = new LinkedList<KitItem>();

    public class KitItem extends JItem {

        public int itemAmount;

        public KitItem() {
        }

        public KitItem(JItem i, int amt) {
            // make sure is a legal (existing) item
            //JItem n = JItem.findItem(i);
            if (i != null && i.item != null) {
                SetItem(i);
                itemAmount = amt;
            } else {
                itemAmount = 0;
            }
        }

        public JItem toItem() {
            return new JItem(this);
        }

        public boolean iequals(ItemStack i) {
            return item != null && item.equals(i);
        }

        @Override
        public String toString() {
            return String.format("%s (%d:%d) @%d", item == null ? name : item.getName(), item.ID(), item.Data(), itemAmount);
        }
    }

    public Kit() {
    } // end default constructor

    public Kit(JItem copy) {
        AddItem(copy);
    }
    
    public Kit(JItem copy, int numUsed) {
        AddItem(copy, numUsed);
    }

    public Kit(String kitStr) {
        SetItems(fromStr(kitStr));
    }

    public static Kit fromStr(String kitStr) {
        if (kitStr == null) {
            return null;
        }
        Kit ret = new Kit();
        String all[] = kitStr.replace(" ", "").split(",");
        for (String i : all) {
            if (i.contains("@")) {
                if (i.length() > i.indexOf("@")) {
                    ret.AddItem(JItemDB.findItem(i.substring(0, i.indexOf("@"))),
                            CheckInput.GetInt(i.substring(i.indexOf("@") + 1), 1));
                } else {
                    ret.AddItem(JItemDB.findItem(i.substring(0, i.indexOf("@"))));
                }
            } else {
                ret.AddItem(JItemDB.findItem(i));
            }
        }
        if (ret.numItems() == 0) {
            return null;
        }
        return ret;
    }

    public final void AddItem(JItem toAdd) {
        if (toAdd == null) {
            return;
        }
        KitItem t = new KitItem(toAdd, 1);
        if (t.itemAmount > 0) {
            kititems.add(t);
        }
    }

    public final void AddItem(JItem toAdd, int itemAmount) {
        if (itemAmount <= 0 || toAdd == null) {
            return;
        }
        KitItem t = new KitItem(toAdd, itemAmount);
        if (t.itemAmount > 0) {
            kititems.add(t);
        }
    }

    /**
     * adds a new item without checking if it's a valid item
     * @param toAdd
     */
    public final void AddNewItem(JItem toAdd) {
        if (toAdd == null) {
            return;
        }
        KitItem t = new KitItem();
        t.itemAmount = 1;
        t.SetItem(toAdd);
        kititems.add(t);
    }

    /**
     * adds a new item without checking if it's a valid item
     * @param toAdd
     * @param itemAmount
     */
    public final void AddNewItem(JItem toAdd, int itemAmount) {
        if (itemAmount <= 0 || toAdd == null) {
            return;
        }
        KitItem t = new KitItem();
        t.itemAmount = itemAmount;
        t.SetItem(toAdd);
        kititems.add(t);
    }

    public final void SetItems(Kit copy) {
        kititems.clear();
        if (copy != null) {
            kititems.addAll(copy.kititems);
        }
    }

    public int numItems() {
        return kititems.size();
    }

    public int totalItems() {
        int n = 0;
        for (KitItem k : kititems) {
            n += k.itemAmount;
        }
        return n;
    }

    public JItem getItem(int index) {
        if (index >= 0 && index < kititems.size()) {
            return kititems.get(index).toItem();
        }
        return null;
    }

    public int getItemCount(int index) {
        if (index >= 0 && index < kititems.size()) {
            return kititems.get(index).itemAmount;
        }
        return 0;
    }

    public int getItemCount(JItem i) {
        for (KitItem k : kititems) {
            if (k.equals(i)) {
                return k.itemAmount;
            }
        }
        return 0;
    }

    public JItem[] getItems() {
        return kititems.toArray(new JItem[0]);
    }

    public KitItem getKitItem(int i) {
        if (i >= 0 && i < kititems.size()) {
            return kititems.get(i);
        }
        return null;
    }

    public KitItem[] getKitItems() {
        return kititems.toArray(new KitItem[0]);
    }

    @Override
    public String toString() {
        String kitid = name + ": ";
        for (int i = 0; i < kititems.size(); ++i) {
            kitid += kititems.get(i).coloredName() + "@" + kititems.get(i).itemAmount;
            if (i + 1 < kititems.size()) {
                kitid += " + ";
            }
        }
        return kitid;
    }
} // end class Kit

