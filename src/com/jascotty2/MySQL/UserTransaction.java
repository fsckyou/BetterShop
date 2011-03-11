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
    public String name, user;
    public boolean sold;
    
    public UserTransaction() {
        name=user="null";
    } // end default constructor

    
    public UserTransaction(int num, int sub, String name, boolean isSold, int amount, String username) {
        itemNum=num;
        itemSub=sub;
        this.name=name;
        sold=isSold;
        user=username;
        this.amount=amount;
    }

    @Override
    public String toString(){
        return String.format("'%s', %d, %d, '%s', %d, %d", user, itemNum, itemSub, name, amount, sold ? 1 : 0);
    }
} // end class UserTransaction
