/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: for checking & maintaining chests & items
 *		(can be used to prevent accidental destruction / deletion of shop chests)
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

import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.utils.BSPermissions;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import me.jascotty2.lib.bukkit.inventory.ChestManip;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EndermanPickupEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

// end class SignRestore
/**
 * @author jacob
 */
public class ChestListener implements Listener /*implements Runnable*/ {

	final Plugin plugin;
	final Server server;
	final ChestDB chestsBD;
	final DamageBlocker blockBreakBlock;

	public ChestListener(Plugin p, ChestDB chestsBD) {
		if (p == null || chestsBD == null) {
			throw new IllegalArgumentException("Arguments Cannot be Null");
		}
		plugin = p;
		server = p.getServer();
		this.chestsBD = chestsBD;
		blockBreakBlock = new DamageBlocker(p, chestsBD);
	}

	public void startProtect() {
		plugin.getServer().getPluginManager().registerEvents(blockBreakBlock, plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!event.isCancelled()) {
			if (chestsBD.savedChestExists(event.getBlock().getLocation())) {
				chestsBD.remove(event.getBlock().getLocation());
			} else if (event.getBlockPlaced().getState() instanceof Chest) {
				Chest other = ChestManip.otherChest(event.getBlock());
				if (other != null && chestsBD.savedChestExists(other.getBlock().getLocation())) {
					chestsBD.setChest(event.getBlock().getLocation());
					BSutils.sendMessage(event.getPlayer(), "Chest Shop Expanded");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.isCancelled() && chestsBD.savedChestExists(event.getBlock().getLocation())) {
			if (BetterShop.getSettings().chestDestroyProtection
					&& !BSPermissions.hasPermission(event.getPlayer(), BetterShopPermission.ADMIN_CHESTS, true)) {
				event.setCancelled(true);
			} else {
				chestsBD.remove(event.getBlock().getLocation());
				BSutils.sendMessage(event.getPlayer(), "Chest Shop Removed");
			}
		}
	}
}

class DamageBlocker implements Listener {

	final Plugin plugin;
	final Server server;
	final ChestDB chestsBD;

	DamageBlocker(Plugin p, ChestDB chestsBD) {
		plugin = p;
		server = p.getServer();
		this.chestsBD = chestsBD;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (!event.isCancelled() && BetterShop.getSettings().chestTNTprotection) {
			for (Block b : event.blockList()) {
				if (chestsBD.has(b.getLocation())) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEndermanPickup(EndermanPickupEvent event) {
		if (!event.isCancelled() && BetterShop.getSettings().signDestroyProtection) {
			if (chestsBD.has(event.getBlock().getLocation())) {
				event.setCancelled(true);
				return;
			}
		}
	}
}
