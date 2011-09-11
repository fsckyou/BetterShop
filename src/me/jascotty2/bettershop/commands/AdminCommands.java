/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: commands & methods for plugin administration
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import me.jascotty2.bettershop.BSConfig;
import me.jascotty2.bettershop.BSEcon;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.BetterShop.ServerReload;
import me.jascotty2.bettershop.Updater;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import me.jascotty2.bettershop.shop.Shop;
import me.jascotty2.bettershop.utils.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.lib.bukkit.commands.Command;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.item.PriceListItem;
import me.jascotty2.lib.bukkit.shop.PriceList;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.util.Str;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author jacob
 */
public class AdminCommands {

	final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss");

	@Command(
	commands = {"shopload"},
	aliases = {"load", "reload"},
	desc = "Reload Shop & Settings",
	permissions = {"BetterShop.admin.load"})
	public static boolean load(CommandSender player, String[] s) {
		if (player != null && !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_LOAD, true)) {
			return true;
		}
		int err = BetterShop.reload(player);
		BSutils.sendMessage(player, "Reload Complete with " + (err == 0 ? "no" : String.valueOf(err)) + " Errors");
		return player != null || err == 0;
	}

	@Command(
	commands = {"shopadd", "sadd"},
	aliases = {"add", "ad"},
	desc = "Add an item to, or update an item in, the price list",
	usage = "[item] [buy-price] [sell-price]",
	min = 2,
	max = 3,
	permissions = {"BetterShop.admin.add"})
	public static boolean add(CommandSender player, String[] s) {
		if (s.length == 2) {
			// append -1 as sell price
			String ns[] = new String[3];
			for (int i = 0; i < 2; ++i) {
				ns[i] = s[i];
			}
			ns[2] = "-1";
			s = ns;
		}
		if (s.length != 3) {
			return false;
		}
		if (!BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ADD, true)) {
			return true;
		}

		JItem toAdd = JItemDB.findItem(s[0]);
		if (toAdd == null) {
			BSutils.sendMessage(player,
					BetterShop.getConfig().getString("unkitem").replace("<item>", s[0]));
			return false;
		}

		if (CheckInput.IsDouble(s[1]) && CheckInput.IsDouble(s[2])) {
			double buy = CheckInput.GetDouble(s[1], -1),
					sel = CheckInput.GetDouble(s[2], -1);
			if (buy > PriceList.MAX_PRICE
					|| sel > PriceList.MAX_PRICE) {
				BSutils.sendMessage(player, "Price set too high. Max = " + BSEcon.format(PriceList.MAX_PRICE));
				return true;
			} else if (toAdd.isKit() && sel >= 0) {
				BSutils.sendMessage(player, "Note: Kits cannot be sold");
				s[2] = "-1";
			} else if (toAdd.isEntity() && sel >= 0) {
				BSutils.sendMessage(player, "Note: Entities cannot be sold");
				s[2] = "-1";
			}
			try {
				Shop shop = BetterShop.getShop(
						player instanceof Player ? ((Player) player).getLocation() : null);
				boolean isChanged = shop.pricelist.itemExists(toAdd);
				if (shop.pricelist.setPrice(toAdd, buy, sel)) {
					PriceListItem nPrice = shop.pricelist.getItemPrice(toAdd);
					double by = nPrice == null ? -2 : nPrice.buy,
							sl = nPrice == null ? -2 : nPrice.sell;

					BSutils.sendMessage(player, BetterShop.getConfig().getString(isChanged ? "chgmsg" : "addmsg").
							replace("<item>", toAdd.coloredName()).
							replace("<buyprice>", String.format("%.2f", by)).
							replace("<sellprice>", String.format("%.2f", sl)).
							replace("<curr>", BetterShop.getConfig().currency()).
							replace("<buycur>", BSEcon.format(by)).
							replace("<sellcur>", BSEcon.format(sl)),
							BetterShop.getConfig().publicmarket);

					if (!isChanged && shop.config.useStock()) {
						shop.stock.setItemAmount(toAdd, shop.config.startStock);
					}
					return true;
				}
			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
			}
			BSutils.sendMessage(player, ChatColor.RED + "An Error Occurred While Adding.");
		} else {
			BSutils.sendMessage(player, BetterShop.getConfig().getString("paramerror"));
			return false;
		}

		return true;
	}

	@Command(
	commands = {"shopremove", "sremove"},
	aliases = {"remove", "rm"},
	desc = "Remove an item from the price list",
	usage = "[item]",
	min = 1,
	max = 1,
	permissions = {"BetterShop.admin.remove"})
	public static boolean remove(CommandSender player, String[] s) {
		if ((!BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_REMOVE, true))) {
			return true;
		} else if (s == null || s.length != 1) {
			return false;
		}
		JItem toRem = JItemDB.findItem(s[0]);
		if (toRem != null) {
			try {
				Shop shop = BetterShop.getShop(
						player instanceof Player ? ((Player) player).getLocation() : null);

				shop.pricelist.remove(toRem);
				shop.stock.remove(toRem);

				BSutils.sendMessage(player, BetterShop.getConfig().getString("removemsg").
						replace("<item>", toRem.coloredName()), BetterShop.getConfig().publicmarket);

				return true;
			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
			}
			BSutils.sendMessage(player, ChatColor.RED + "Error removing item");
		} else {
			BSutils.sendMessage(player,
					BetterShop.getConfig().getString("unkitem").replace("<item>", s[0]));
		}
		return true;

	}

	@Command(
	commands = {},
	aliases = {"restock"},
	desc = "Restock the Shop's Stock of Items",
	usage = "[item [item ...]]",
	permissions = {"BetterShop.admin.restock"})
	public static void restock(CommandSender sender, String[] s) {
		//TODO:
		// if item[s] specified, restock only those
		BetterShop.restock();
		sender.sendMessage("Stock set to initial values");
	}

	@Command(
	commands = {},
	aliases = {"backup"},
	desc = "Backup the pricelist to a csv file",
	usage = "[filename]",
	permissions = {"BetterShop.admin.backup"})
	public static void backupDB(CommandSender sender, String[] s) {
		String fn;

		if (s.length >= 1) {
			fn = Str.concatStr(s, 1, " ");
			if (fn.toLowerCase().endsWith(".csv")) {
				fn = fn.substring(0, fn.length() - 5);
			}
		} else {
			fn = dateFormatter.format(new java.util.Date());
		}

		if (s.length >= 1) {
			Shop sh = BetterShop.getShop(s[0]);
			if (sh != null) {
				if (backupDB(sh, fn)) {
					BSutils.sendMessage(sender, ChatColor.GREEN + "Backup saved as " + fn);
				} else {
					BSutils.sendMessage(sender, ChatColor.RED + "Failed to save backup file " + fn);
				}
				return;
			}
		}

		for (Shop shop : BetterShop.getShops()) {
			String filename;
			if (s.length > 1) {
				filename = fn + "_" + shop.config.tableName;//shop.getName();
			} else {
				filename = shop.config.tableName + "_" + fn;
			}
			if (backupDB(shop, filename)) {
				BSutils.sendMessage(sender, ChatColor.GREEN + "Backup saved as " + filename);
			} else {
				BSutils.sendMessage(sender, ChatColor.RED + "Failed to save backup file " + filename);
			}
		}

	}

	protected static boolean backupDB(Shop shop, String saveAs) {
		if (!saveAs.toLowerCase().endsWith(".csv")) {
			saveAs += ".csv";
		}
		try {
			return shop.pricelist.saveFile(new File(BSConfig.pluginFolder.getPath()
					+ File.separatorChar + saveAs));
		} catch (IOException ex) {
			BetterShopLogger.Severe("Failed to save backup file " + saveAs, ex, false);
		}
		return false;
	}

	@Command(
	commands = {},
	aliases = {"import"},
	desc = "import items to pricelist from a csv file",
	usage = "[filename]",
	permissions = {"BetterShop.admin.backup"})
	public static boolean importDB(CommandSender player, String[] s) {
		String fname = Str.concatStr(s, " ");
		if (fname.length() == 0) {
			BSutils.sendMessage(player, "Need a file to import");
			return true;
		}

		File toImport = new File(BSConfig.pluginFolder.getPath() + File.separatorChar + fname);
		if (!toImport.exists() && !fname.toLowerCase().endsWith(".csv")) {
			fname += ".csv";
			toImport = new File(BSConfig.pluginFolder.getPath() + File.separatorChar + fname);
		}
		if (!toImport.exists() && s.length > 1) {
			// check if the first string is a shop name
			Shop shop = BetterShop.getShop(s[0]);
			if (shop != null) {
				fname = Str.concatStr(s, 1, " ");
				toImport = new File(BSConfig.pluginFolder.getPath() + File.separatorChar + fname);
				if (!toImport.exists() && !fname.toLowerCase().endsWith(".csv")) {
					fname += ".csv";
					toImport = new File(BSConfig.pluginFolder.getPath() + File.separatorChar + fname);
				}
				if (toImport.exists()) {
					if (shop.pricelist.importDB(toImport)) {
						BSutils.sendMessage(player, "Database Imported");
					} else {
						BSutils.sendMessage(player, ChatColor.RED + " An Error Occured while importing database");
					}
				}
			}
		}
		if (!toImport.exists()) {
			BSutils.sendMessage(player, fname + " does not exist");
			return true;
		}
		if (BetterShop.getMainPricelist().importDB(toImport)) {
			BSutils.sendMessage(player, "Database Imported");
		} else {
			BSutils.sendMessage(player, ChatColor.RED + " An Error Occured while importing database");
		}
		return true;
	}

	@Command(
	commands = {},
	aliases = {"restore"},
	desc = "restore a pricelist from a csv file",
	usage = "[filename]",
	permissions = {"BetterShop.admin.backup"})
	public static boolean restoreDB(CommandSender player, String[] s) {
		String fname = Str.concatStr(s, " ");
		if (fname.length() == 0) {
			BSutils.sendMessage(player, "Need a file to import");
			return true;
		}

		File toImport = new File(BSConfig.pluginFolder.getPath() + File.separatorChar + fname);
		if (!toImport.exists() && !fname.toLowerCase().endsWith(".csv")) {
			fname += ".csv";
			toImport = new File(BSConfig.pluginFolder.getPath() + File.separatorChar + fname);
		}
		if (!toImport.exists() && s.length > 1) {
			// check if the first string is a shop name
			Shop shop = BetterShop.getShop(s[0]);
			if (shop != null) {
				fname = Str.concatStr(s, 1, " ");
				toImport = new File(BSConfig.pluginFolder.getPath() + File.separatorChar + fname);
				if (!toImport.exists() && !fname.toLowerCase().endsWith(".csv")) {
					fname += ".csv";
					toImport = new File(BSConfig.pluginFolder.getPath() + File.separatorChar + fname);
				}
				if (toImport.exists()) {
					if (shop.pricelist.importDB(toImport)) {
						BSutils.sendMessage(player, "Database Imported");
					} else {
						BSutils.sendMessage(player, ChatColor.RED + " An Error Occured while importing database");
					}
				}
			}
		}
		if (!toImport.exists()) {
			BSutils.sendMessage(player, fname + " does not exist");
			return true;
		}
		if (BetterShop.getMainPricelist().restoreDB(toImport)) {
			BSutils.sendMessage(player, "Database Imported");
		} else {
			BSutils.sendMessage(player, ChatColor.RED + " An Error Occured while importing database");
		}
		return true;
	}

	@Command(
	commands = {},
	aliases = {"update"},
	desc = "Download & Install an update from git",
	usage = "",
	permissions = {"OP"})
	public static void update(CommandSender sender, String[] s) {
		if (sender.isOp()) {
			BetterShopLogger.Log("Downloading & Installing Update");
			BSutils.sendMessage(sender, "Downloading & Installing Update");
			ServerReload sreload = new ServerReload(BetterShop.getPlugin().getServer());
			if (Updater.DownloadUpdate()) {
				BetterShopLogger.Log("Update Downloaded: Restarting Server..");
				BSutils.sendMessage(sender, "Download Successful.. reloading server");
				// this.setEnabled(false);
				// this.getServer().dispatchCommand((CommandSender)
				// new CommanderSenderImpl(this), "stop");
				// this.getServer().dispatchCommand(new
				// AdminCommandSender(this), "stop");

				// this.getServer().reload();
				sreload.start(500);
			}
		} else {
			BSutils.sendMessage(sender, "Only an OP can update the shop plugin");
		}
	}

	@Command(
	commands = {},
	aliases = {"version", "v", "ver"},
	desc = "Check the current version",
	permissions = {"BetterShop.admin.info", "jascotty2", "jjfs85"})
	public static void version(CommandSender sender, String[] s) {
		// allow admin.info or developers access to plugin status
		// (so if i find a bug i can see if it's current)
		BSutils.sendMessage(sender, "version " + BetterShop.getPlugin().getDescription().getVersion());
		if (Updater.IsUpToDate()) {
			BSutils.sendMessage(sender, "Version is up-to-date");
		} else {
			BSutils.sendMessage(sender, "Newer Version Avaliable");
		}

	}
} // end class AdminCommands

