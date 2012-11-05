/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: commands & methods related to plugin helps
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

import me.jascotty2.bettershop.BSConfig;
import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import me.jascotty2.bettershop.BSPermissions;
import me.jascotty2.lib.bukkit.commands.Command;
import me.jascotty2.lib.util.Str;
//import me.taylorkelly.help.Help;
import me.taylorkelly.help.Help;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * @author jacob
 */
public class HelpCommands {

	public static boolean helpPluginEnabled = false; // no reason to check twice :)

	@Command(
	commands = {"shophelp", "shelp"},
	aliases = {"help", "?"},
	desc = "Lists available commands",
	permissions = {"BetterShop.user.help"})
	public static boolean help(CommandSender player, String[] s) {
		if(s.length > 0 && s[0].toLowerCase().contains("help")){
			String[] newArgs = new String[s.length - 1];
			System.arraycopy(s, 1, newArgs, 0, newArgs.length);
			s = newArgs;
		}
		if (s.length > 0) {
			// extra command help
			
		}
		
		if (s.length > 0) {
			// more help
			if (Str.isIn(s[0], "shop")) {
				BSutils.sendMessage(player, "/shop   command alias to other commands");
				BSutils.sendMessage(player, "      ");
				if (BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_BACKUP, false)) {
					BSutils.sendMessage(player, "/shop backup   to backup current pricelist");
				}
				if (BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_INFO, false)) {
					BSutils.sendMessage(player, "/shop ver[sion]   show the currently installed version");
					BSutils.sendMessage(player, "        also shows if this is the most current avaliable");
				}
			} else if (Str.isIn(s[0], "shopcheck, scheck, sc")) {
				BSutils.sendMessage(player, "/shopcheck <item>  Check prices for an item");
				BSutils.sendMessage(player, " aliases: scheck, sc");
				BSutils.sendMessage(player, "-- will also run a name match search");
			} else if (Str.isIn(s[0], "shoplist, slist, sl")) {
				BSutils.sendMessage(player, "/shoplist [page]   Lists prices for the shop");
				BSutils.sendMessage(player, " aliases: slist, sl");
			} else if (Str.isIn(s[0], "shopitems, sitems")) {
				BSutils.sendMessage(player, "/shopitems  show listing of items in shop, without prices");
				BSutils.sendMessage(player, " aliases: sitems");
				BSutils.sendMessage(player, "-- coming soon: pages");
			} else if (Str.isIn(s[0], "shopbuy, sbuy, buy")) {
				BSutils.sendMessage(player, "/shopbuy <item> [amount] Buy an item for the price in the shop");
				BSutils.sendMessage(player, " aliases: sbuy, buy");
				BSutils.sendMessage(player, "-- \"all\" is a valid amount: will buy all you can hold/afford");
			} else if (Str.isIn(s[0], "shopbuyall, sbuyall, buyall")) {
				BSutils.sendMessage(player, "/shopbuyall <item>  buy all you can hold/afford");
				BSutils.sendMessage(player, " aliases: sbuyall, buyall");
			} else if (Str.isIn(s[0], "shopbuystack, buystack, sbuystack, sbuys, buys")) {
				BSutils.sendMessage(player, "/shopbuystack <item> [amount] buy items in stacks");
				BSutils.sendMessage(player, " aliases: buystack, sbuystack, sbuys, buys");
				BSutils.sendMessage(player, "-- can list multiple items, or give how many stacks to buy");
			} else if (Str.isIn(s[0], "shopsell, ssell, sell")) {
				BSutils.sendMessage(player, "/shopsell <item> [amount]");
				BSutils.sendMessage(player, " aliases: ssell, sell");
				BSutils.sendMessage(player, "-- \"all\" is a valid amount: will sell all you have");
			} else if (Str.isIn(s[0], "shopsellall, sellall, sell all")) {
				BSutils.sendMessage(player, "/shopsellall [inv] [item [item [...]]] ");
				BSutils.sendMessage(player, "-- Sell all of item from your inventory");
				BSutils.sendMessage(player, " aliases: sellall, sell all");
				BSutils.sendMessage(player, "-- inv will only sell from your inventory, not the lower 9");
				BSutils.sendMessage(player, "-- multiple items can be listed, or none for all sellable");
			} else if (Str.isIn(s[0], "shopadd, sadd")) {
				BSutils.sendMessage(player, "/shopadd <item> <buyprice> [sellprice]");
				BSutils.sendMessage(player, "--  Add an item to or update an item in the price list");
				BSutils.sendMessage(player, " aliases: sadd");
				BSutils.sendMessage(player, "-- price of -1 disables that action");
				BSutils.sendMessage(player, "-- if no sellprice is given, item will not be sellable");
			} else if (Str.isIn(s[0], "shopremove, sremove")) {
				BSutils.sendMessage(player, "/shopremove <item>  Remove an item from the price list");
				BSutils.sendMessage(player, " aliases: sremove");
			} else if (Str.isIn(s[0], "shopload, sload, shop load, shop reload")) {
				BSutils.sendMessage(player, "/shopload   reload prices from pricelist database");
				BSutils.sendMessage(player, " aliases: sload, shop [re]load");
			} else if (Str.isIn(s[0], "shophelp, shelp")) {
				BSutils.sendMessage(player, "/shophelp [command] Lists available commands");
				BSutils.sendMessage(player, " aliases: shelp");
				BSutils.sendMessage(player, "-- providing a command shows specific help for that command");
			} else if (Str.isIn(s[0], "shoplistkits, shopkits, skits")) {
				BSutils.sendMessage(player, "/shoplistkits [page] Lists available kits");
				BSutils.sendMessage(player, " aliases: shopkits, skits");
				BSutils.sendMessage(player, "-- toadd: show what each kit contains");
			} else if (Str.isIn(s[0], "shopbuyagain, sbuyagain, buyagain, sba")) {
				BSutils.sendMessage(player, "/shopbuyagain  repeat last purchase");
				BSutils.sendMessage(player, " aliases: sbuyagain, buyagain, sba");
			} else if (Str.isIn(s[0], "shopsellagain, ssellagain, sellagain, ssa")) {
				BSutils.sendMessage(player, "/shopsellagain  repeat last sale");
				BSutils.sendMessage(player, " aliases: ssellagain, sellagain, ssa");
			} /*else if (s[0].equalsIgnoreCase("")) {
			BSutils.sendMessage(player, "/");
			BSutils.sendMessage(player, " aliases: ");
			BSutils.sendMessage(player, "-- ");
			} */ else {
				BSutils.sendMessage(player, "Unknown Help Topic");
			}
			return true;
		}
		BSutils.sendMessage(player, "--------- Better Shop Usage --------");
		if (BSPermissions.hasPermission(player, BetterShopPermission.USER_LIST, false)) {
			BSutils.sendMessage(player, "/shoplist [page] - List shop prices");
			BSutils.sendMessage(player, "/shopitems - show listing of items in shop, without prices");
			BSutils.sendMessage(player, "/shopkits [page] - show listing of kits in shop");
		}
		if (BSPermissions.hasPermission(player, BetterShopPermission.USER_BUY, false)) {
			BSutils.sendMessage(player, "/shopbuy <item> [amount] - Buy items");
			BSutils.sendMessage(player, "/shopbuyall <item> - Buy all that you can hold/afford");
			BSutils.sendMessage(player, "/shopbuystack <item> [amount] - Buy stacks of items");
			BSutils.sendMessage(player, "/shopbuyagain - repeat last purchase action");
		}
		if (BSPermissions.hasPermission(player, BetterShopPermission.USER_SELL, false)) {
			BSutils.sendMessage(player, "/shopsell <item> [amount] - Sell items ");
			BSutils.sendMessage(player, "/shopsellall <item> - Sell all of your items");
			BSutils.sendMessage(player, "/shopsellstack <item> [amount] - Sell stacks of items");
			BSutils.sendMessage(player, "/shopsellagain - Repeat last sell action");
		}
		if (BSPermissions.hasPermission(player, BetterShopPermission.USER_CHECK, false)) {
			BSutils.sendMessage(player, "/shopcheck <item> [amount] - Check prices of an item");
		}
		BSutils.sendMessage(player, "/shophelp [command] - show help on commands");
		if (BSPermissions.hasPermission(player, BetterShopPermission.ADMIN, false)) {
			BSutils.sendMessage(player, "**-------- Admin commands --------**");
			if (BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ADD, false)) {
				BSutils.sendMessage(player, "/shopadd <item> <$buy> [$sell] - Add/Update an item");
			}
			if (BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_REMOVE, false)) {
				BSutils.sendMessage(player, "/shopremove <item> - Remove an item from the shop");
			}
			if (BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_LOAD, false)) {
				BSutils.sendMessage(player, "/shopload - Reload the Configuration & PriceList DB");
			}
			if (BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_RESTOCK, false)){
				BSutils.sendMessage(player, "/shop restock - restock items to starting values");
			}
			if (BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_BACKUP, false)){
				BSutils.sendMessage(player, "/shop backup [file] - Backup the pricelist to a csv file");
			}
			if (BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_BACKUP, false)){
				BSutils.sendMessage(player, "/shop restore <file> - restore a pricelist from a csv file");
			}
			if (BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_BACKUP, false)){
				BSutils.sendMessage(player, "/shop import - import items to pricelist from a csv file");
			}
			if (BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_INFO, false)){
				BSutils.sendMessage(player, "/shop version - check the current version & if there are updates avaliable");
			}
			if(player.isOp()){
				BSutils.sendMessage(player, "/shop update - Download & Install an Update");
			}
		}
		BSutils.sendMessage(player, "----------------------------------");
		return true;
	}

	public static void registerHelp(Plugin p) {
		if (!helpPluginEnabled && p != null && p instanceof Help) {
			Help helpPlugin = (Help)p;
			Plugin plugin = BetterShop.getPlugin();
			BSConfig config = BetterShop.getSettings();
			helpPlugin.registerCommand("shoplist [page]",
					"List shop prices", plugin, !config.hideHelp,
					"BetterShop.user.list");
			helpPlugin.registerCommand("shopitems",
					"compact listing of items in shop", plugin,
					"BetterShop.user.list");
			helpPlugin.registerCommand("shopkits [page]",
					"show listing of kits in shop", plugin,
					"BetterShop.user.list");
			helpPlugin.registerCommand("shopbuy [item] <amount>",
					"Buy items from the shop", plugin, !config.hideHelp,
					"BetterShop.user.buy");
			helpPlugin.registerCommand("shopbuyall [item]",
					"Buy all that you can hold/afford", plugin,
					"BetterShop.user.buy");
			helpPlugin.registerCommand("shopbuystack [item] <amount>",
					"Buy stacks of items", plugin, "BetterShop.user.buy");
			helpPlugin.registerCommand("shopbuyagain",
					"repeat last purchase action", plugin,
					"BetterShop.user.buy");
			helpPlugin.registerCommand("shopsell [item] <amount>",
					"Sell items to the shop", plugin, !config.hideHelp,
					"BetterShop.user.sell");
			helpPlugin.registerCommand("shopsellstack [item] <amount>",
					"Sell stacks of items", plugin, "BetterShop.user.sell");
			helpPlugin.registerCommand("shopsellall <inv> <item..>",
					"Sell all of your items", plugin, "BetterShop.user.sell");
			helpPlugin.registerCommand("shopsellagain",
					"Repeat last sell action", plugin,
					"BetterShop.user.sell");
			helpPlugin.registerCommand("shopcheck [item]",
					"Check prices of item[s]", plugin, !config.hideHelp,
					"BetterShop.user.check");
			helpPlugin.registerCommand("shophelp [command]",
					"show help on commands", plugin, !config.hideHelp,
					"BetterShop.user.help");
			helpPlugin.registerCommand("shopadd [item] [$buy] <$sell>",
					"Add/Update an item", plugin, !config.hideHelp,
					"BetterShop.admin.add");
			helpPlugin.registerCommand("shopremove [item]",
					"Remove an item from the shop", plugin, !config.hideHelp,
					"BetterShop.admin.remove");
			helpPlugin.registerCommand("shopload",
					"Reload the Configuration & PriceList DB", plugin,
					!config.hideHelp, "BetterShop.admin.load");

			helpPlugin.registerCommand("shop restock",
					"manually restock (if enabled)", plugin,
					"BetterShop.admin.restock");
			helpPlugin.registerCommand("shop ver[sion]",
					"Show Version # and if is current", plugin,
					"BetterShop.admin.info");
			helpPlugin.registerCommand("shop backup",
					"backup current pricelist", plugin,
					"BetterShop.admin.backup");
			helpPlugin.registerCommand("shop import [file]",
					"import a file into the pricelist", plugin,
					"BetterShop.admin.backup");
			helpPlugin.registerCommand("shop restore [file]",
					"restore pricelist from backup", plugin,
					"BetterShop.admin.backup");
			helpPlugin.registerCommand("shop update",
					"manually update bettershop to newest version", plugin,
					"OP");
		} // else Log("HelpCommands not yet found.");
		helpPluginEnabled = p != null;
	}
	
} // end class HelpCommands

