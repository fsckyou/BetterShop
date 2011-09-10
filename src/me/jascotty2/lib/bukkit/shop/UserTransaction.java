/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: an individual record of a user's transaction
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

import me.jascotty2.lib.bukkit.item.ItemStockEntry;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import java.util.Date;
import org.bukkit.inventory.ItemStack;

public class UserTransaction {

    public int itemNum, itemSub, amount;
    public String name, user;
    public boolean sold;
    public double price;
    public long time;

    public UserTransaction() {
        name = user = "null";
        time = (new Date()).getTime() / 1000;
    } // end default constructor

    public UserTransaction(int num, int sub, String name, boolean isSold, int amount, double price, String username) {
        itemNum = num;
        itemSub = sub;
        this.name = name;
        sold = isSold;
        user = username;
        this.amount = amount;
        this.price = price;
        time = (new Date()).getTime() / 1000;
    }

    public UserTransaction(JItem item, boolean isSold, int amount, double price, String username) {
        itemNum = item.ID();
        itemSub = item.Data();
        name = item.Name();
        sold = isSold;
        user = username;
        this.amount = amount;
        this.price = price;
        time = (new Date()).getTime() / 1000;
    }

    public UserTransaction(JItem item, boolean isSold) {
        itemNum = item.ID();
        itemSub = item.Data();
        name = item.Name();
        sold = isSold;
        user = "";
        time = (new Date()).getTime() / 1000;
    }

    public UserTransaction(ItemStack item, boolean isSold, int amount, double price, String username) {
        itemNum = item.getTypeId();
        itemSub = item.getDurability();
        name = JItemDB.GetItemName(item);
        sold = isSold;
        user = username;
        this.amount = amount;
        this.price = price;
        time = (new Date()).getTime() / 1000;
    }

    public UserTransaction(ItemStockEntry item, boolean isSold, double price, String username) {
        itemNum = item.itemNum;
        itemSub = item.itemSub;
        this.name = item.name;
        sold = isSold;
        user = username;
        this.amount = (int) item.amount;
        this.price = price;
        time = (new Date()).getTime() / 1000;
    }

    public UserTransaction(long time, String username, int num, int sub, String itemname, int amt, boolean isSold, double unitprice) {
        this.time = time;
        itemNum = num;
        itemSub = sub;
        name = itemname;
        sold = isSold;
        user = username;
        amount = amt;
        price = unitprice;
    }

    public UserTransaction(long time, String username, JItem item, int amt, boolean isSold, double unitprice) {
        this.time = time;
        itemNum = item.ID();
        itemSub = item.Data();
        name = item.Name();
        sold = isSold;
        user = username;
        amount = amt;
        price = unitprice;
    }

    public JItem GetItem() {
        return JItemDB.GetItem(itemNum, (byte) itemSub);
    }

    @Override
    public String toString() {
        return String.format("'%s', %d, %d, '%s', %d, %d", user, itemNum, itemSub, name, amount, sold ? 1 : 0);
    }

    // only checks item, user, & if sold, not amount
    public boolean equals(UserTransaction t) {
        return itemNum == t.itemNum && itemSub == t.itemSub && sold == t.sold
                && (user == null || user.equalsIgnoreCase(t.user));
    }

    public boolean equals(TotalTransaction t) {
        return itemNum == t.itemNum && itemSub == t.itemSub;
    }

    public boolean equals(JItem it) {
        return itemNum == it.ID() && itemSub == it.Data();
    }

    public boolean equals(ItemStack it) {
        return itemNum == it.getTypeId() && itemSub == it.getDurability();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UserTransaction) {
            return equals((UserTransaction) o);
        }else if (o instanceof TotalTransaction){
            return equals((TotalTransaction) o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.itemNum;
        hash = 47 * hash + this.itemSub;
        hash = 47 * hash + (this.sold ? 1 : 0);
        return hash;
    }
} // end class UserTransaction

