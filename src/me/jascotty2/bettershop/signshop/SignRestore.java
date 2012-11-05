/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: for checking & maintaining signs
 *		(can be used to prevent accidental destruction / deletion of shop signs)
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
package me.jascotty2.bettershop.signshop;

import java.util.List;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.BSPermissions;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * @author jacob
 */
public class SignRestore implements Listener, Runnable {

	final Plugin plugin;
	final Server server;
	final SignDB signs;
	int taskID = -1;

	public SignRestore(Plugin p, SignDB signs) {
		if (p == null || signs == null) {
			throw new IllegalArgumentException("Arguments Cannot be Null");
		}
		plugin = p;
		server = p.getServer();
		this.signs = signs;
	}

	public void start(long wait) {
		//(new Timer()).scheduleAtFixedRate(this, wait, wait);
		// 20 ticks per second
		taskID = server.getScheduler().scheduleSyncRepeatingTask(plugin, this, 100, (wait * 20) / 1000);
	}

	public void cancel() {
		if (taskID != -1) {
			server.getScheduler().cancelTask(taskID);
			taskID = -1;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!event.isCancelled() && signs.signExists(event.getBlock().getLocation())) {
			signs.remove(event.getBlock().getLocation());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.isCancelled() && signs.signExists(event.getBlock().getLocation())) {
			if (BetterShop.getSettings().signDestroyProtection
					&& !BSPermissions.hasPermission(event.getPlayer(), BetterShopPermission.ADMIN_MAKESIGN, true)) {
				event.setCancelled(true);
			} else {
				signs.remove(event.getBlock().getLocation());
				if (!(event.getBlock().getState() instanceof Sign)) {
					List<Block> list = signs.getShopSigns(event.getBlock());
					for (Block b : list) {
						signs.remove(b.getLocation());
					}
				}
			}
		}
	}

	@Override
	public void run() {
		for (BlockState b : signs.getSignAnchors()) {
			if (b.getBlock().getLocation().getBlock().getTypeId() != b.getTypeId()) {
				b.getBlock().getLocation().getBlock().setTypeIdAndData(b.getTypeId(), b.getRawData(), false);
			}
		}
		for (Sign b : signs.getSavedSigns()) {
			if (b.getBlock().getLocation().getBlock().getTypeId() != b.getTypeId()) {
				b.getBlock().getLocation().getBlock().setTypeIdAndData(b.getTypeId(), b.getRawData(), false);
				Sign dest = (Sign) b.getBlock().getLocation().getBlock().getState();
				for (int i = 0; i < 4; ++i) {
					dest.setLine(i, b.getLine(i));
				}
				dest.update();
			}
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if (!event.isCancelled() && BetterShop.getSettings().signTNTprotection) {
			for (Block b : event.blockList()) {
				if (signs.signExists(b.getLocation())) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityBlockInteract(EntityInteractEvent event) {
		// ought to work for enderman pickup and other break events, but not sure how to test..
		if (!event.isCancelled() && event.getBlock() != null 
				&& !(event.getEntity() instanceof Player)
				&& BetterShop.getSettings().signDestroyProtection) {
			if (signs.signExists(event.getBlock().getLocation()) || signs.isSignAnchor(event.getBlock())) {
				event.setCancelled(true);
				return;
			}
		}
	}
}
// end class SignRestore
