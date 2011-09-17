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
import me.jascotty2.bettershop.utils.BSPermissions;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

/**
 * @author jacob
 */
public class SignRestore extends BlockListener implements Runnable {

	final Plugin plugin;
	final Server server;
	final SignDB signs;
	int taskID = -1;
	final TNTblock tntBlock;

	public SignRestore(Plugin p, SignDB signs) {
		if (p == null || signs == null) {
			throw new IllegalArgumentException("Arguments Cannot be Null");
		}
		plugin = p;
		server = p.getServer();
		this.signs = signs;
		tntBlock = new TNTblock(p, signs);
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

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		if (signs.signExists(event.getBlock().getLocation())) {
			signs.remove(event.getBlock().getLocation());
		}
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (signs.signExists(event.getBlock().getLocation())) {
			if (BetterShop.getConfig().signDestroyProtection
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
}
// end class SignRestore


class TNTblock extends EntityListener {

	final Plugin plugin;
	final Server server;
	final SignDB signs;

	TNTblock(Plugin p, SignDB signs) {
		plugin = p;
		server = p.getServer();
		this.signs = signs;
	}

	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		if (BetterShop.getConfig().signTNTprotection) {
			for (Block b : event.blockList()) {
				if (signs.signExists(b.getLocation())) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}
}