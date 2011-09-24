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
package me.jascotty2.bettershop.commands;

import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.lib.bukkit.commands.Command;
import me.jascotty2.lib.bukkit.commands.NestedCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChestCommands {

	@Command(commands = {},
	aliases = {"chest", "ch"},
	desc = "General shop chest commands")
	@NestedCommand({ChestCommands.Commands.class})
	public static void chest(CommandSender sender, String[] args) {
	}

	public static class Commands {

		@Command(commands = {},
		aliases = {"define", "d", "create", "c"},
		desc = "Define a Chest shop",
		usage = "",
		permissions = {"BetterShop.admin.chests"})
		public static void define(CommandSender player, String[] s) {
			if (BSutils.anonymousCheck(player)) {
				return;
			}
			Player p = (Player) player;
			Block b = p.getTargetBlock(null, 5);
			if (b.getType() == Material.CHEST) {
				if (BetterShop.getChestShop().defineChestShop(b)) {
					BSutils.sendMessage(p, "Chest is now a Shop");
				} else {
					BSutils.sendMessage(p, ChatColor.RED + "Chest is already a Shop");
				}
			} else {
				BSutils.sendMessage(p, ChatColor.RED + "This is not a Chest!");
			}
		}

		@Command(commands = {},
		aliases = {"edit", "e"},
		desc = "Change Items in a Chest Shop",
		usage = "",
		permissions = {"BetterShop.admin.chests"})
		public static void edit(CommandSender player, String[] s) {
			if (BSutils.anonymousCheck(player)) {
				return;
			}
			Player p = (Player) player;
			Block b = p.getTargetBlock(null, 5);
			if (b.getType() == Material.CHEST) {
				if (BetterShop.getChestShop().hasChestShop(b)) {
					BetterShop.getChestShop().openChest(p, (Chest) b.getState(), true);
				} else {
					BSutils.sendMessage(p, ChatColor.RED + "This is not a Chest Shop");
				}
			} else {
				BSutils.sendMessage(p, ChatColor.RED + "This is not a Chest!");
			}
		}

		@Command(commands = {},
		aliases = {"remove", "r", "delete", "del"},
		desc = "Remove a Chest Shop",
		usage = "",
		permissions = {"BetterShop.admin.chests"})
		public static void remove(CommandSender player, String[] s) {
			if (BSutils.anonymousCheck(player)) {
				return;
			}
			Player p = (Player) player;
			Block b = p.getTargetBlock(null, 5);
			if (b.getType() == Material.CHEST) {
				if (BetterShop.getChestShop().removeChestShop(b)) {
					BSutils.sendMessage(p, "Chest is no longer a Shop");
				} else {
					BSutils.sendMessage(p, ChatColor.RED + "This is not a Chest Shop");
				}
			} else {
				BSutils.sendMessage(p, ChatColor.RED + "This is not a Chest!");
			}
		}
	}
} // end class ChestCommands

