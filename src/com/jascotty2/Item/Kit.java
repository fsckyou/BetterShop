/**
 * Programmer: Jacob Scott
 * Program Name: Kit
 * Description: provides a class for kits
 * Date: Mar 12, 2011
 */
package com.jascotty2.Item;

import com.jascotty2.CheckInput;
import java.util.LinkedList;

public class Kit extends Item {

    private LinkedList<KitItem> kititems = new LinkedList<KitItem>();
    //private LinkedList<String> itemAliases = new LinkedList<String>();

    public class KitItem extends Item {

        public int itemAmount;

        public KitItem() {
        }

        public KitItem(Item i, int amt) {
            // make sure is a legal (existing) item
            Item n = Item.findItem(i);
            if (n != null) {
                SetItem(n);
                itemAmount = amt;
            } else {
                itemAmount = 0;
            }
        }

        public Item toItem() {
            return new Item(this);
        }

        @Override
        public String toString(){
            return String.format("%s (%d:%d) @%d", name, itemId, itemData, itemAmount);
        }
    }

    public Kit() {
    } // end default constructor

    public Kit(Item copy) {
        AddItem(copy);
    }

    public Kit(Item copy, int numUsed) {
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
                    ret.AddItem(Item.findItem(i.substring(0, i.indexOf("@"))),
                            CheckInput.GetInt(i.substring(i.indexOf("@") + 1), 1));
                } else {
                    ret.AddItem(Item.findItem(i.substring(0, i.indexOf("@"))));
                }
            } else {
                ret.AddItem(Item.findItem(i));
            }
        }
        if (ret.numItems() == 0) {
            return null;
        }
        return ret;
    }

    public final void AddItem(Item toAdd) {
        if (toAdd == null) {
            return;
        }
        KitItem t = new KitItem(toAdd, 1);
        if (t.itemAmount > 0) {
            kititems.add(t);
        }
    }

    public final void AddItem(Item toAdd, int itemAmount) {
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
    public final void AddNewItem(Item toAdd) {
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
    public final void AddNewItem(Item toAdd, int itemAmount) {
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

    public Item getItem(int index) {
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

    public int getItemCount(Item i) {
        for (KitItem k : kititems) {
            if (k.equals(i)) {
                return k.itemAmount;
            }
        }
        return 0;
    }

    public Item[] getItems() {
        return kititems.toArray(new Item[0]);
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
            kitid += kititems.get(i).name + "@" + kititems.get(i).itemAmount;
            if (i + 1 < kititems.size()) {
                kitid += " + ";
            }
        }
        return kitid;
    }
} // end class Kit

