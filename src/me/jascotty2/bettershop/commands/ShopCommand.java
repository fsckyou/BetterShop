/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: intermediary command for others
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
import me.jascotty2.bettershop.spout.SpoutPopupDisplay;
import me.jascotty2.lib.bukkit.commands.Command;
import me.jascotty2.lib.bukkit.commands.NestedCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * @author jacob
 */
public class ShopCommand {

	@Command(commands = {"shop", "bettershop", "bshop"},
	desc = "General shop commands")
	@NestedCommand({HelpCommands.class, AdminCommands.class, ListCommands.class,
		BuyCommands.class, SellCommands.class,
		RegionCommands.class, ChestCommands.class, Commands.class})
	public static void shop(CommandSender player, String[] s) {
	}

	public static class Commands {

		@Command(commands = {},
		aliases = {"gui", "g", "menu"},
		desc = "Open the Spout GUI, if possible",
		usage = "",
		permissions = {"BetterShop.user.spout"})
		public static void openGui(CommandSender player, String[] s) {
			if (BSutils.anonymousCheck(player)) {
				return;
			}
			if (!BetterShop.spoutEnabled()) {
				BSutils.sendMessage(player, ChatColor.RED + "Spout is not installed");
				return;
			}
			SpoutPlayer sp = (SpoutPlayer) player;
			if(!sp.isSpoutCraftEnabled()){
				BSutils.sendMessage(player, ChatColor.RED + "You don't have SpoutCraft!");
			} else {
				SpoutPopupDisplay.popup(sp);
			}
		}
	}
} // end class ShopCommand

