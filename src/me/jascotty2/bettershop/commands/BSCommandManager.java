/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: handles commands for bettershop
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

import java.util.logging.Logger;
import me.jascotty2.bettershop.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.lib.bukkit.commands.CommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author jacob
 */
public class BSCommandManager extends CommandManager {

	//final Class<?>[] functionParameter = new Class<?>[]{CommandSender.class, String[].class};
	final Class<?>[] commandClasses = new Class<?>[]{
		ShopCommand.class, AdminCommands.class,
		BuyCommands.class, SellCommands.class,
		ListCommands.class, HelpCommands.class};

	public BSCommandManager() {
		for(Class<?> c : commandClasses){
			registerCommandClass(c);
		}
	}

	@Override
	protected Logger getLogger() {
		return BetterShopLogger.getLogger();
	}

	@Override
	public boolean hasPermission(CommandSender player, String perm) {
		if(perm.toUpperCase().equals("OP")){
			return player.isOp();
		} else if(!perm.contains(".")
				&& player instanceof Player
				&& perm.equalsIgnoreCase(((Player)player).getName())){
			return true;
		}
		return BSPermissions.hasPermission(player, perm, false);
	}
} // end class BSCommandManager

