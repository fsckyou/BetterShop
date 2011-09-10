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
import me.jascotty2.lib.bukkit.commands.Command;
import me.jascotty2.lib.bukkit.commands.NestedCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author jacob
 */
public class RegionCommands {

	@Command(
	commands = {},
	aliases = {"region"},
	desc = "General shop region commands")
	@NestedCommand({RegionCommands.Commands.class})
	public static void region(CommandSender sender, String[] args) {
	}

	public static class Commands {

		@Command(
	commands = {},
	aliases = {"define"},
		desc = "Define a Shop Region",
		usage = "<region-name>",
		min = 1,
		permissions = {"BetterShop.admin.region"})
		public static void define(CommandSender sender, String[] args) {
			if (BSutils.anonymousCheck(sender)) {
				return;
			}
			BetterShop.getShopManager().addRegion(args[0], (Player) sender);
		}
	}
} // end class RegionCommands

