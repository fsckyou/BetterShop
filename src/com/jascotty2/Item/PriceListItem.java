/**
 * Programmer: Jacob Scott
 * Program Name: PriceListItem
 * Description: structure to hold table data
 * Date: Mar 11, 2011
 */
package com.jascotty2.Item;

import java.util.Date;

public class PriceListItem extends Item {

    public double buy, sell;
    protected Date tempCacheDate = null; // when last updated

    public PriceListItem() {
        itemId = 0;
        name = "null";
        buy = sell = -1;
    } // end default constructor

    /**
     * does not make a check if the item is valid
     * @param num item id
     * @param sub sub-data
     * @param name name of the item
     * @param buyPrice
     * @param sellPrice
     */
    public PriceListItem(int num, byte sub, String name, double buyPrice, double sellPrice) {
        itemId = num;
        itemData = sub;
        this.name = name;
        buy = buyPrice;
        sell = sellPrice;
        tempCacheDate = new Date();
    }

    public PriceListItem(Item copy, double buyPrice, double sellPrice) {
        SetItem(copy);
        buy = buyPrice;
        sell = sellPrice;
        tempCacheDate = new Date();
    }

    public PriceListItem(Item toSave) {
        if (toSave != null) {
            SetItem(toSave);
            tempCacheDate = new Date();
        }
    }

    public PriceListItem(PriceListItem copy) {
        if (copy != null) {
            SetItem(copy);
            buy = copy.buy;
            sell = copy.sell;
            tempCacheDate = new Date();
        }
    }

    public double BuyPrice() {
        return buy;
    }

    public double SellPrice() {
        return sell;
    }

    public Date LastUpdated() {
        return tempCacheDate;
    }

    public void SetBuyPrice(double newPrice) {
        buy = newPrice;
        tempCacheDate = new Date();
    }

    public void SetSellPrice(double newPrice) {
        sell = newPrice;
        tempCacheDate = new Date();
    }

    public void SetPrice(double buy, double sell) {
        this.buy = buy;
        this.sell = sell;
        tempCacheDate = new Date();
    }

    public void SetFromItem(Item i) {
        SetItem(i);
        tempCacheDate = new Date();
    }

    public static PriceListItem fromItem(Item i) {
        if (i == null) {
            return null;
        }
        return new PriceListItem(i);
    }

    public long getTime() {
        return tempCacheDate.getTime();
    }

    public void Set(PriceListItem copy) {
        if (copy == null) {
            itemId = -1;
            return;
        }
        SetItem(copy);
        this.buy = copy.buy;
        this.sell = copy.sell;
        tempCacheDate = new Date();
    }


    public boolean equals(PriceListItem i) {
        return i.ID() == itemId && i.Data() == itemData;
    }
    
    /*

    @Override
    public boolean equals(Object o) {
        if (o instanceof PriceListItem || o instanceof Item) {
            return equals((PriceListItem) o);
        }
        return false;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.buy) ^ (Double.doubleToLongBits(this.buy) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.sell) ^ (Double.doubleToLongBits(this.sell) >>> 32));
        return hash;
    }*/
} // end class PriceListRow

