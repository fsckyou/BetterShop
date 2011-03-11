/**
 * Programmer: Jacob Scott
 * Program Name: UserTransaction
 * Description:
 * Date: Mar 11, 2011
 */

package com.jascotty2.MySQL;

/**
 * @author jacob
 */
public class UserTransaction {

    public int itemNum, itemSub, amount;
    public String name;
    public boolean sold;
    
    public UserTransaction() {
        name="null";
    } // end default constructor

    
    public UserTransaction(int num, int sub, String name, boolean isSold) {
        itemNum=num;
        itemSub=sub;
        this.name=name;
        sold=isSold;
    }
} // end class UserTransaction
