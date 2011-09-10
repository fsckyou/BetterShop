/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: structure to hold table data
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

public class PriceListItem extends JItem {

    public double buy, sell;
    protected Date tempCacheDate = null; // when last updated

    public PriceListItem() {
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
        super(num, sub);
        if (super.name == null) {
            super.name = name;
        }
        buy = buyPrice;
        sell = sellPrice;
        tempCacheDate = new Date();
    }

    public PriceListItem(JItem copy, double buyPrice, double sellPrice) {
        super(copy);
        buy = buyPrice;
        sell = sellPrice;
        tempCacheDate = new Date();
    }

    public PriceListItem(JItem toSave) {
        super(toSave);
        if (toSave != null) {
            tempCacheDate = new Date();
        }
    }

    public PriceListItem(PriceListItem copy) {
        super(copy);
        if (copy != null) {
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

    public void SetFromItem(JItem i) {
        SetItem(i);
        tempCacheDate = new Date();
    }

    public static PriceListItem fromItem(JItem i) {
        if (i == null) {
            return null;
        }
        return new PriceListItem(i);
    }

    public long getTime() {
        return tempCacheDate.getTime();
    }

    public void Set(PriceListItem copy) {
        SetItem(copy);
        if (copy != null) {
            this.buy = copy.buy;
            this.sell = copy.sell;
        }
        tempCacheDate = new Date();
    }

    public boolean equals(PriceListItem i) {
        return i != null && item != null && item.equals(i.item);
    }
    /*

    @Override
    public boolean equals(Object o) {
    if (o instanceof PriceListItem || o instanceof JItem) {
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

