/**
 * Copyright (C) 2012 Jacob Scott <jascottytechie@gmail.com>
 * Description: Structure to hold central transaction details
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
package me.jascotty2.bettershop;
import me.jascotty2.lib.bukkit.item.JItem;

public class PlayerTransation {
	/**
	 * player initiating the transaction
	 */
	public String playername;
	/**
	 * what direction (false == buying the item)
	 */
	public boolean is_selling;
	/**
	 * how many items trying to buy/sell
	 */
	public int amount;
	/**
	 * if the item(s) is known
	 */
	public JItem items[];
	/**
	 * if the item is being looked up, what the search term is
	 */
	public String itemSearch;
	/**
	 * if this is a custom request, can define the specific buy/sell price
	 */
	public float customPrice = -1;
}
