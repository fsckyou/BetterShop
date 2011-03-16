/**
 * Programmer: Jacob Scott
 * Program Name: Discount
 * Description: holder for discount data
 * Date: Mar 13, 2011
 */
package com.jascotty2;

import com.jascotty2.Item.Item;
import java.util.LinkedList;

public class Discount {

    public boolean isUser; // (if false, is group)
    public LinkedList<String> users = new LinkedList<String>(); // which users/groups have this discount
    public LinkedList<Item> items; // which items are discounted (null == all)
    public double discount; // discount percentage

    public Discount(Item discounted, boolean isUser, LinkedList<String> users, double discount) {
        this.isUser = isUser;
        this.discount = discount;
        this.users.clear();
        this.users.addAll(users);
        items = new LinkedList<Item>();
        items.add(discounted);
    }

    public Discount(boolean isUser, String user, double discount) {
        this.isUser = isUser;
        this.discount = discount;
        this.users.clear();
        this.users.add(user);
    }

    public Discount() {
    }

    public void addUser(String usr){
        if(usr!=null && usr.length()>0){
            users.add(usr);
        }
    }
    public void addItem(Item itm){
        if(itm!=null){
        if(items==null)items = new LinkedList<Item>();
         items.add(itm);
        }
    }

    public void removeUser(String usr){
         users.remove(usr);
    }
    public void removeItem(Item itm){
        if(items!=null){
            items.remove(itm);
            if(items.size()==0) items=null;
        }
    }
    
    public String getUserListStr() {
        String ret = "";
        for (int i = 0; i < users.size(); ++i) {
            ret += users.get(i);
            if (i + 1 < users.size()) {
                ret += ",";
            }
        }
        return ret;
    }

    public String getItemListStr() {
        String ret = "";
        if (items != null) {
            for (int i = 0; i < items.size(); ++i) {
                ret += items.get(i).IdDatStr();
                if (i + 1 < items.size()) {
                    ret += ",";
                }
            }
        }
        return ret;
    }
}
