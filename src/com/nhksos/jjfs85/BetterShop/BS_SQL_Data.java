/**
 * Programmer: Jacob Scott
 * Program Name: BS_SQL_Data
 * Description: structure to hold table data
 * Date: Mar 8, 2011
 */

package com.nhksos.jjfs85.BetterShop;


public class BS_SQL_Data {
    public int itemNum, itemSub;
    double buy, sell;
    String name;
    
    public BS_SQL_Data(){
        itemNum=0;
        name="null";
        buy=sell=-1;
    }

    public BS_SQL_Data(int num, int sub, String name, double buyPrice, double sellPrice){
        itemNum=num;
        itemSub=sub;
        this.name=name;
        buy=buyPrice;
        sell=sellPrice;
    }
} // end class BS_SQL_Data
