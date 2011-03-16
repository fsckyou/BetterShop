/**
 * Programmer: Jacob Scott
 * Program Name: CraftRecipe
 * Description: used to store the items used to craft another
 * Date: Mar 12, 2011
 */
package com.jascotty2.Item;

import com.jascotty2.CheckInput;
import java.util.LinkedList;

public class CraftRecipe { //  extends Kit

    protected LinkedList<Kit> items = new LinkedList<Kit>();
    public int resultAmount;
    //public Item resultItem;

    public CraftRecipe() {
    } // end default constructor

    public CraftRecipe(String recipeStr) {
        SetRecipe(fromStr(recipeStr));
    }

    public void AddItem(Item toAdd) {
        if (toAdd != null) {
            items.add(new Kit(toAdd));
        }
    }

    public void AddItem(Item toAdd, int numUsed) {
        if (toAdd != null && numUsed > 0) {
            items.add(new Kit(toAdd, numUsed));
        }
    }

    /**
     * adds without checking if is a valid item
     * @param toAdd
     */
    public void AddNewItem(Item toAdd) {
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
    public void AddNewItem(Item toAdd, int numUsed) {
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
            if (i.contains("@")) {
                if (i.length() > i.indexOf("@")) {
                    ret.AddItem(new Item(i.substring(0, i.indexOf("@"))),
                            CheckInput.GetInt(i.substring(i.indexOf("@") + 1), 1));
                } else {
                    ret.AddItem(new Item(i.substring(0, i.indexOf("@"))));
                }
            } else {
                ret.AddItem(new Item(i));
            }
        }
        if (ret.totalItems() == 0) {
            return null;
        }
        return ret;
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

    public Item getItem(int index) {
        if (index >= 0 && index < items.size()) {
            return (Item) items.get(index);
        }
        return null;
    }

    public int getItemCount(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index).totalItems();
        }
        return 0;
    }

    public int getItemCount(Item i) {
        for (Kit k : items) {
            if (((Item) k).equals(i)) {
                return k.totalItems();
            }
        }
        return 0;
    }

    public Item[] getItems() {
        return items.toArray(new Item[0]);
    }

    public Kit[] getKits() {
        return items.toArray(new Kit[0]);
    }

    public Kit.KitItem[] getKitItems() {
        Kit.KitItem[] ret = new Kit.KitItem[numItems()];
        for (int i = 0, n=0; i < items.size(); ++i) {
            for(int j=0; j<items.get(i).numItems(); ++j, ++n){
                ret[n] = items.get(i).getKitItem(j);
            }
        }
        return ret;
    }
} // end class CraftRecipe

