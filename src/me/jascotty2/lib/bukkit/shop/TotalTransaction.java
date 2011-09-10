/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: for logging total shop item transactions
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

import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import java.util.Date;
import org.bukkit.inventory.ItemStack;

/**
 * @author jacob
 */
public class TotalTransaction {

    public int itemNum, itemSub;
    public String name;
    public long sold, bought;
    public long time;

    public TotalTransaction() {
        name = "null";
        time = (new Date()).getTime() / 1000;
    } // end default constructor

    public TotalTransaction(UserTransaction init) {
        time = (new Date()).getTime() / 1000;

        itemNum = init.itemNum;
        itemSub = init.itemSub;
        name = init.name;
        if (init.sold) {
            sold = init.amount;
        } else {
            bought = init.amount;
        }
    }

    public TotalTransaction(JItem item, long numSold, long numBought) {
        time = (new Date()).getTime() / 1000;
        itemNum = item.ID();
        itemSub = item.Data();
        name = item.Name();
        sold = numSold;
        bought = numBought;
    }

    public TotalTransaction(long time, int num, int sub, String itemname, long numSold, long numBought) {
        this.time = time;
        itemNum = num;
        itemSub = sub;
        name = itemname;
        sold = numSold;
        bought = numBought;
    }

    public TotalTransaction(long time, JItem item, long numSold, long numBought) {
        this.time = time;
        itemNum = item.ID();
        itemSub = item.Data();
        name = item.Name();
        sold = numSold;
        bought = numBought;
    }

    public JItem GetItem() {
        return JItemDB.GetItem(itemNum, (byte) itemSub);
    }

    @Override
    public String toString() {
        return String.format("%d:%d, '%s', +%d, -%d, %d", itemNum, itemSub, name, bought, sold, time);
    }

    // only checks item, user, & if sold, not amount
    public boolean equals(UserTransaction t) {
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
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.itemNum;
        hash = 53 * hash + this.itemSub;
        hash = 53 * hash + (int) (this.sold ^ (this.sold >>> 32));
        hash = 53 * hash + (int) (this.bought ^ (this.bought >>> 32));
        return hash;
    }
} // end class TotalTransaction

