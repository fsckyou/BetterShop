/**
 * Programmer: Jacob Scott
 * Program Name: ItemStockEntry
 * Description:
 * Date: Mar 22, 2011
 */
package com.jascotty2.Item;

import java.util.Date;
import org.bukkit.inventory.ItemStack;

/**
 * @author jacob
 */
public class ItemStockEntry {

    public int itemNum, itemSub;
    public long amount;
    public String name;
    protected Date tempCacheDate = null; // when last updated

    public ItemStockEntry() {
        name = "";
    } // end default constructor

    public ItemStockEntry(int id, int sub, String itemName, long amt) {
        itemNum = id;
        itemSub = sub;
        name = itemName;
        amount = amt;
    }

    public ItemStockEntry(Item toAdd, long amt) {
        itemNum = toAdd.itemId;
        itemSub = toAdd.itemData;
        name = toAdd.name;
        amount = amt;
    }

    public ItemStockEntry(ItemStockEntry toCopy) {
        Set(toCopy);
    }

    public ItemStockEntry(Item toCopy) {
        Set(toCopy);
    }

    public ItemStockEntry(ItemStack toCopy) {
        Set(toCopy);
    }

    public final void Set(ItemStockEntry toCopy) {
        itemNum = toCopy.itemNum;
        itemSub = toCopy.itemSub;
        name = toCopy.name;
        amount = toCopy.amount;
        tempCacheDate = new Date();
    }

    public final void Set(Item toCopy) {
        itemNum = toCopy.itemId;
        itemSub = toCopy.itemData;
        name = toCopy.name;
        amount = 0;
        tempCacheDate = new Date();
    }

    public final void Set(ItemStack toCopy) {
        itemNum = toCopy.getTypeId();
        Item copy = Item.findItem(toCopy);
        if (copy != null) {
            itemSub = copy.itemData;
            name = copy.name;
        } else {
            itemSub = toCopy.getDurability();
            name = toCopy.toString();
        }
        amount = toCopy.getAmount();
        tempCacheDate = new Date();
    }

    public void SetAmount(long amt) {
        amount = amt;
        tempCacheDate = new Date();
    }

    public void AddAmount(long amt) {
        if (amt >= 0 || amount + amt > 0) {
            amount += amt;
            tempCacheDate = new Date();
        }
    }

    public void RemoveAmount(long amt) {
        amount -= amt;
        tempCacheDate = new Date();
    }

    public long getTime() {
        return tempCacheDate == null ? 0 : tempCacheDate.getTime();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ItemStockEntry) {
            return equals((ItemStockEntry) o);
        } else if (o instanceof Item) {
            return equals((Item) o);
        }
        return false;
    }

    public boolean equals(Item i) {
        if (i == null) {
            return false;
        }
        return (i.IsTool() || itemSub == i.itemData) && itemNum == i.itemId;
    }

    public boolean equals(ItemStockEntry i) {
        if (i == null) {
            return false;
        }
        Item t = Item.findItem(i);
        return (t.IsTool() || itemSub == i.itemSub) && itemNum == i.itemNum;
    }

    public boolean equals(ItemStack i) {
        if (i == null || itemNum != i.getTypeId()) {
            return false;
        }
        if (itemSub == i.getDurability()) {
            return true;
        }
        Item t = Item.findItem(i);
        return t != null && t.IsTool();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + this.itemNum;
        hash = 11 * hash + this.itemSub;
        hash = 11 * hash + (int) (this.amount ^ (this.amount >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return String.format("%d:%d (%s) %d", itemNum, itemSub, name, amount);
    }
} // end class ItemStockEntry

