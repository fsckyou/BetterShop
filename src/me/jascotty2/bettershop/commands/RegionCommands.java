/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: commands & methods related to shop regions
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
import me.jascotty2.bettershop.regionshops.RegionShopManager;
import me.jascotty2.lib.bukkit.MinecraftChatStr;
import me.jascotty2.lib.bukkit.commands.Command;
import me.jascotty2.lib.bukkit.commands.NestedCommand;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.util.Str;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author jacob
 */
public class RegionCommands {

	@Command(commands = {},
	aliases = {"region", "r"},
	desc = "General shop region commands")
	@NestedCommand({RegionCommands.Commands.class})
	public static void region(CommandSender sender, String[] args) {
	}

	public static class Commands {

		@Command(commands = {},
		aliases = {"define", "d"},
		desc = "Define a Shop Region",
		usage = "<region-name>",
		min = 1,
		permissions = {"BetterShop.admin.region"})
		public static void define(CommandSender sender, String[] args) {
			if (BSutils.anonymousCheck(sender)) {
				return;
			}
			if (!BetterShop.getShopManager().addRegion(args[0], (Player) sender)) {
				BSutils.sendMessage(sender, ChatColor.RED + "Could not save shop region");
			}
		}

		@Command(commands = {},
		aliases = {"remove", "r"},
		desc = "Remove a Shop Region",
		usage = "<region-name>",
		min = 1,
		permissions = {"BetterShop.admin.region"})
		public static void remove(CommandSender sender, String[] args) {
			BetterShop.getShopManager().removeRegion(sender, (args[0]));
		}

		@Command(commands = {},
		aliases = {"list", "ls", "l"},
		desc = "List Shop Regions",
		usage = "[page]",
		max = 1,
		permissions = {"BetterShop.admin.region"})
		public static void list(CommandSender sender, String[] args) {
			RegionShopManager s = BetterShop.getShopManager();
			int max = s.numRegions(),
					page = args.length > 0 ? CheckInput.GetInt(args[0], 0) - 1 : 0,
					pagesize = BetterShop.getConfig().pagesize,
					pages = (int) Math.ceil((double)max / pagesize);
			if (page < 0 || page > pages) {
				BSutils.sendMessage(sender, args[0] + " is not a valid page number (there are " + pages + " pages)");
				return;
			}
			if (sender instanceof Player) {
				BSutils.sendMessage(sender, MinecraftChatStr.padCenter(
						" Shop Regions  page " + (page+1) + "/" + pages + " ", '-', MinecraftChatStr.chatwidth - MinecraftChatStr.getStringWidth(BetterShop.getConfig().getString("prefix"))));
			} else {
				BSutils.sendMessage(sender, Str.padCenter(
						" Shop Regions  page " + (page+1) + "/" + pages + " ",
						80 - MinecraftChatStr.getStringWidth(BetterShop.getConfig().getString("prefix")), '-'));
			}
			for (String l : BetterShop.getShopManager().getRegionList(
					(sender instanceof Player ? ((Player) sender).getWorld() : null),
					page, pagesize)) {
				BSutils.sendMessage(sender, l);
			}
		}
	}
} // end class RegionCommands

