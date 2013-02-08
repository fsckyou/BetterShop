/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: for handling permissions checks
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

import com.nijikokun.bukkit.Permissions.Permissions;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.lib.util.Str;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author jacob
 */
public class BSPermissions {

	public static Permissions permissionsPlugin = null;
	public static Permission vaultPerms = null;

	public static boolean hasPermission(CommandSender player, BetterShopPermission node) {
		return hasPermission(player, node.toString(), false);
	}

	public static boolean hasPermission(CommandSender player, BetterShopPermission node, boolean notify) {
		return hasPermission(player, node.toString(), notify);
	}

	public static boolean hasPermission(CommandSender player, String node) {
		return hasPermission(player, node, false);
	}

	public static boolean hasPermission(CommandSender player, String node, boolean notify) {
		if (player == null || player.isOp() || !(player instanceof Player)
				|| node == null || node.length() == 0) { // ops override permission check (double-check is a Player)
			return true;
		}
		if (has((Player) player, node)) {
			return true;
		} else if (notify) {
			//PermDeny(player, node);
			BSutils.sendMessage(player,
					BetterShop.getSettings().getString("permdeny").replace("<perm>", node));
		}
		return false;
	}

	public static boolean has(Player player, String node) {
		try {
			if (vaultPerms != null) {
				return vaultPerms.has(player, node);
			} else if (permissionsPlugin != null) {
				return permissionsPlugin.getHandler().has(player, node);
			}
//			System.out.println("no perm: checking superperm for " + player.getName() + ": " + node);
//			System.out.println(player.hasPermission(node));
//			for(PermissionAttachmentInfo i : player.getEffectivePermissions()){
//				System.out.println(i.getPermission());
//			}
			if (player.hasPermission(node)) {
				return true;
			} else if (!node.contains("*") && Str.count(node, '.') >= 2) {
//				System.out.println("Checking for " + node.substring(0, node.lastIndexOf('.') + 1) + "*  : "
//						+ player.hasPermission(node.substring(0, node.lastIndexOf('.') + 1) + "*"));
				return player.hasPermission(node.substring(0, node.lastIndexOf('.') + 1) + "*");
			}
//			System.out.println("no permission");
			return false;
		} catch (Exception e) {
			BetterShopLogger.Severe(e);
		}
		return node.length() < 16 // if invalid node, assume true
				|| (!node.substring(0, 16).equalsIgnoreCase("BetterShop.admin") // only ops have access to .admin
				&& !node.substring(0, 19).equalsIgnoreCase("BetterShop.discount"));
	}

} // end class BSPermissions
