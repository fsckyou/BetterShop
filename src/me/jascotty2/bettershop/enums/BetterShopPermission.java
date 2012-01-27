/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: permissions nodes used in the plugin
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
package me.jascotty2.bettershop.enums;

public enum BetterShopPermission {

	/**
	 * generic user permissions
	 */
	USER("BetterShop.user"),
	/**
	 * look through shop listing of prices
	 */
	USER_LIST("BetterShop.user.list"),
	/**
	 * check the price of item(s)
	 */
	USER_CHECK("BetterShop.user.check"),
	/**
	 * view ingame help menu
	 */
	USER_HELP("BetterShop.user.help"),
	/**
	 * buy items from the shop
	 */
	USER_BUY("BetterShop.user.buy"),
	/**
	 * sell items to the shop
	 */
	USER_SELL("BetterShop.user.sell"),
	/**
	 * allow a user to use the spout gui menu
	 */
	USER_SPOUT("BetterShop.user.spout"),
	/**
	 * allow a user to use the a chest shop
	 */
	USER_CHEST("BetterShop.user.chest"),
	/**
	 * generic admin permissions
	 */
	ADMIN("BetterShop.admin"),
	/**
	 * add/edit items to/in the shop
	 */
	ADMIN_ADD("BetterShop.admin.add"),
	/**
	 * remove items from the shop
	 */
	ADMIN_REMOVE("BetterShop.admin.remove"),
	/**
	 * reload configuration & pricelist
	 */
	ADMIN_LOAD("BetterShop.admin.load"),
	/**
	 * show shop stats
	 */
	ADMIN_INFO("BetterShop.admin.info"),
	/**
	 * gives the ability to purchase 'illegal' items
	 */
	ADMIN_ILLEGAL("BetterShop.admin.illegal"),
	/**
	 * backing up and restoring the pricelist
	 */
	ADMIN_BACKUP("BetterShop.admin.backup"),
	/**
	 * manually restock (if item stock is enabled)
	 */
	ADMIN_RESTOCK("BetterShop.admin.restock"),
	/**
	 * ability to add/remove shop signs
	 */
	ADMIN_MAKESIGN("BetterShop.admin.makesign"),
	/**
	 * ability to add/remove shop regions
	 */
	ADMIN_REGION("BetterShop.admin.region"),
	/**
	 * ability to add/remove shop chests
	 */
	ADMIN_CHESTS("BetterShop.admin.chests");
	public final String permissionNode;

	BetterShopPermission(String per) {
		permissionNode = per;
	}

	@Override
	final public String toString() {
		return permissionNode;
	}
}
