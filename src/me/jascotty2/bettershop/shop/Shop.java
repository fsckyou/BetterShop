/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: a generic shop class, including pricelist, stock, and transaction log
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

package me.jascotty2.bettershop.shop;

/**
 * @author jacob
 */
public class Shop {

	public final BSPriceList pricelist;
	public final BSItemStock stock;
	public final BSTransactionLog transactions;
	public final ShopConfig config = new ShopConfig();
	protected String name;

    public Shop() {
		pricelist = new BSPriceList(config);
		stock = new BSItemStock(pricelist);
		transactions = new BSTransactionLog(pricelist);
    } // end default constructor

	public boolean load(String shopName){
		this.name = shopName == null ? "Main" : shopName;
		if(shopName == null){
			config.set(null);
		} else {
			// TODO: expand for possible multiple shops
		}
		if(pricelist.load()){
			boolean l1 = stock.load();
			boolean l2 = transactions.load();
			return l1 && l2;
		}
		return false;
	}

	public String getName() {
		return name;
	}
	
} // end class Shop
