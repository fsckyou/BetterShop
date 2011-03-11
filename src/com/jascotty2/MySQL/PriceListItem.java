/**
 * Programmer: Jacob Scott
 * Program Name: PriceListItem
 * Description: structure to hold table data
 * Date: Mar 11, 2011
 */

package com.jascotty2.MySQL;

public class PriceListItem {

    public int itemNum, itemSub;
    public double buy, sell;
    public String name;
    
    public PriceListItem(){
        itemNum=0;
        name="null";
        buy=sell=-1;
    } // end default constructor

    public PriceListItem(int num, int sub, String name, double buyPrice, double sellPrice){
        itemNum=num;
        itemSub=sub;
        this.name=name;
        buy=buyPrice;
        sell=sellPrice;
    }
} // end class PriceListRow
