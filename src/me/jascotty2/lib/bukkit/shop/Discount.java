/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: holder for discount data
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
import java.util.LinkedList;

public class Discount {

    public boolean isUser; // (if false, is group)
    public LinkedList<String> users = new LinkedList<String>(); // which users/groups have this discount
    public LinkedList<JItem> items; // which items are discounted (null == all)
    public double discount; // discount percentage

    public Discount(JItem discounted, boolean isUser, LinkedList<String> users, double discount) {
        this.isUser = isUser;
        this.discount = discount;
        this.users.clear();
        this.users.addAll(users);
        items = new LinkedList<JItem>();
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
    public void addItem(JItem itm){
        if(itm!=null){
        if(items==null)items = new LinkedList<JItem>();
         items.add(itm);
        }
    }

    public void removeUser(String usr){
         users.remove(usr);
    }
    public void removeItem(JItem itm){
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
