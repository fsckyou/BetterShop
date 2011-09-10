/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: and entry to define an item's stock amount
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

    public ItemStockEntry(JItem toAdd, long amt) {
        Set(toAdd);
        amount = amt;
    }

    public ItemStockEntry(ItemStockEntry toCopy) {
        Set(toCopy);
    }

    public ItemStockEntry(JItem toCopy) {
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

    public final void Set(JItem toCopy) {
        if (toCopy != null) {
            if (toCopy.item != null) {
                itemNum = toCopy.item.ID();
                itemSub = toCopy.item.Data();
                name = toCopy.item.getName();
            } else {
                itemNum = toCopy.itemId;
                itemSub = toCopy.itemDat;
                name = toCopy.name;
            }
        } else {
            itemNum = -1;
            itemSub = 0;
            name = "null";
        }
        amount = 0;
        tempCacheDate = new Date();
    }

    public final void Set(ItemStack toCopy) {
        itemNum = toCopy.getTypeId();
        JItem copy = JItemDB.findItem(toCopy);
        if (copy != null) {
            if (copy.item != null) {
                itemSub = copy.item.Data();
                name = copy.item.getName();
            } else {
                itemSub = copy.itemDat;
                name = copy.name;
            }
        } else {
            itemSub = toCopy.getDurability();
            name = toCopy.getData().getItemType().name();
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
        } else if (o instanceof JItem) {
            return equals((JItem) o);
        }
        return false;
    }

    public boolean equals(JItem i) {
        if (i == null || i.item == null || itemNum != i.ID()) {
            return false;
        }
        return itemSub == i.Data() || i.IsTool();
    }

    public boolean equals(ItemStockEntry i) {
        if (i == null || itemNum != i.itemNum) {
            return false;
        }
        if (itemSub == i.itemSub) {
            return true;
        }
        JItem t = JItemDB.GetItem(i.itemNum, (byte) i.itemSub);
        return t != null && t.IsTool();
    }

    public boolean equals(ItemStack i) {
        if (i == null || itemNum != i.getTypeId()) {
            return false;
        }
        if (itemSub == i.getDurability()) {
            return true;
        }
        JItem t = JItemDB.findItem(i);
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

