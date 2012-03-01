/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: ( TODO )
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
package me.jascotty2.bettershop.chestshop;

import org.getspout.spoutapi.event.inventory.InventoryCloseEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;

public class ChestShopInventoryListenerSpout implements Listener {

	final BSChestShop callback;

	public ChestShopInventoryListenerSpout(BSChestShop callback) {
		this.callback = callback;
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if ((event.getPlayer() != null)) {
			callback.chestClose(event.getPlayer());
		}
	}
} // end class ChestShopPlayerListener

