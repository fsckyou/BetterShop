/**
 * Programmer: Jacob Scott
 * Program Name: UserTransaction
 * Description:
 * Date: Mar 11, 2011
 */
package com.jascotty2.Shop;

import com.jascotty2.Item.Item;
import com.jascotty2.Item.ItemDB;
import com.jascotty2.Item.ItemStockEntry;
import java.util.Date;
import org.bukkit.inventory.ItemStack;

/**
 * @author jacob
 */
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

    public UserTransaction(Item item, boolean isSold, int amount, double price, String username) {
        itemNum = item.ID();
        itemSub = item.Data();
        name = item.name;
        sold = isSold;
        user = username;
        this.amount = amount;
        this.price = price;
        time = (new Date()).getTime() / 1000;
    }

    public UserTransaction(Item item, boolean isSold) {
        itemNum = item.ID();
        itemSub = item.Data();
        name = item.name;
        sold = isSold;
        user = "";
        time = (new Date()).getTime() / 1000;
    }

    public UserTransaction(ItemStack item, boolean isSold, int amount, double price, String username) {
        itemNum = item.getTypeId();
        itemSub = item.getDurability();
        name = ItemDB.GetItemName(item);
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

    public UserTransaction(long time, String username, Item item, int amt, boolean isSold, double unitprice) {
        this.time = time;
        itemNum = item.ID();
        itemSub = item.Data();
        name = item.name;
        sold = isSold;
        user = username;
        amount = amt;
        price = unitprice;
    }

    public Item GetItem() {
        Item ret = Item.findItem(itemNum, (byte) itemSub);
        if (ret == null) {
            return new Item(itemNum, (byte) itemSub, name);
        }
        return ret;
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

    public boolean equals(Item it) {
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

