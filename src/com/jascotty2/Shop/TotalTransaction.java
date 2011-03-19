/**
 * Programmer: Jacob Scott
 * Program Name: TotalTransaction
 * Description:
 * Date: Mar 19, 2011
 */
package com.jascotty2.Shop;

import com.jascotty2.Item.Item;
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

    public TotalTransaction(Item item, long numSold, long numBought) {
        time = (new Date()).getTime() / 1000;
        itemNum = item.ID();
        itemSub = item.Data();
        name = item.name;
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

    public TotalTransaction(long time, Item item, long numSold, long numBought) {
        this.time = time;
        itemNum = item.ID();
        itemSub = item.Data();
        name = item.name;
        sold = numSold;
        bought = numBought;
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
        return String.format("%d:%d, '%s', +%d, -%d, %d", itemNum, itemSub, name, bought, sold, time);
    }

    // only checks item, user, & if sold, not amount
    public boolean equals(UserTransaction t) {
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

