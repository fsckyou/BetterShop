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

import me.jascotty2.lib.bukkit.commands.Command;
import me.jascotty2.lib.bukkit.commands.NestedCommand;
import org.bukkit.command.CommandSender;

/**
 * @author jacob
 */
public class ShopCommand {

	@Command(
	commands = {"shop", "bettershop", "bshop"},
	desc = "General shop commands")
	@NestedCommand({HelpCommands.class, AdminCommands.class, ListCommands.class,
		BuyCommands.class, SellCommands.class, RegionCommands.class})
	public static void shop(CommandSender player, String[] s) {
	}

} // end class ShopCommand
