/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: Global Shop System for Minecraft
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

import me.jascotty2.bettershop.shop.Shop;
import me.jascotty2.bettershop.shop.BSTransactionLog;
import me.jascotty2.bettershop.shop.BSItemStock;
import me.jascotty2.bettershop.shop.BSPriceList;
import me.jascotty2.bettershop.spout.SpoutKeyListener;
import me.jascotty2.bettershop.spout.SpoutPopupListener;
import me.jascotty2.bettershop.utils.BetterShopErrorTracker;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.bettershop.commands.BSCommandManager;
import me.jascotty2.bettershop.commands.HelpCommands;
import me.jascotty2.bettershop.regionshops.RegionShopManager;
import me.jascotty2.bettershop.signshop.BSSignShop;
import me.jascotty2.bettershop.chestshop.BSChestShop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Server;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Collection;
import java.util.logging.Level;

import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.item.CreatureItem.EntityListen;
import me.jascotty2.lib.bukkit.commands.CommandException;
import me.jascotty2.lib.bukkit.commands.CommandUsageException;
import me.jascotty2.lib.bukkit.commands.MissingNestedCommandException;
import me.jascotty2.lib.bukkit.commands.WrappedCommandException;
import me.jascotty2.lib.util.ArrayManip;
import me.jascotty2.lib.util.Str;

/**
 * BetterShop for Bukkit
 */
public class BetterShop extends JavaPlugin {

	public final static String lastUpdatedStr = "09/04/11 21:50 -0500"; // "MM/dd/yy HH:mm Z"
	public final static int lastUpdated_gracetime = 20; // how many minutes off before out of date
	protected static Plugin bettershopPlugin = null;
	protected final static BSConfig config = new BSConfig();
	protected final static RegionShopManager shopManager = new RegionShopManager();
	protected static BSSignShop signShop = null;
	protected static BSChestShop chestShop = null;
	protected static BSEcon economy;
	private BSPluginListener pListener = null;
	// for animal/monster purchases
	public final EntityListen entityListener = new EntityListen();
	private final static BSCommandManager commandManager = new BSCommandManager();
	protected static String lastCommand = "";
	// for spout-related classes
	protected static SpoutKeyListener keyListener = null;
	protected static SpoutPopupListener buttonListener = null;

	public void onEnable() {
		bettershopPlugin = this;
		PluginDescriptionFile pdfFile = this.getDescription();
		BetterShopLogger.Info(String.format("Loading %s version %s ...",
				pdfFile.getName(), pdfFile.getVersion()));

		config.extractDefaults();
		// ready items db (needed for pricelist, sorting in config, item lookup,
		// ...)
		try {
			JItemDB.load(BSConfig.itemDBFile);
			// Log("Itemsdb loaded");
		} catch (Exception e) {
			BetterShopLogger.Severe("cannot load items db: closing plugin", e, false);
			this.setEnabled(false);
			return;
		}

		config.load();
		if (config.checkUpdates) {
			BetterShopLogger.Log("Checking for updates...");
			if (config.autoUpdate) {
				if (!Updater.IsUpToDate(true)) {
					BetterShopLogger.Log("Downloading & Installing Update");
					ServerReload sreload = new ServerReload(getServer());
					if (Updater.DownloadUpdate()) {
						BetterShopLogger.Log("Update Downloaded: Restarting Server..");
						// this.setEnabled(false);
						// this.getServer().dispatchCommand((CommandSender) new
						// CommanderSenderImpl(this), "stop");
						// this.getServer().dispatchCommand(new
						// AdminCommandSender(this), "stop");

						try {
							// (new ServerReload(getServer())).start(500);
							sreload.start(500);
						} catch (Exception e) { // just in case...
							this.getServer().reload();
						}
						return;
					}
				}
			} else {
				Updater.Check();
			}
		}
		economy = new BSEcon(this);
		if (shopManager.load() > 0) {
			BetterShopLogger.Severe("Error while enabling Shop");
		}
		signShop = new BSSignShop(this);

		if (config.signShopEnabled && !signShop.load()) {
			BetterShopLogger.Severe("cannot load sign shop database", false);
		}
		if (config.signShopEnabled && config.signWEprotection) {
			signShop.startProtecting();
		}

		signShop.registerEvents();


		chestShop = new BSChestShop(this);

		if (config.chestShopEnabled && !chestShop.load()) {
			BetterShopLogger.Severe("cannot load chest shop database", false);
		}

		chestShop.registerEvents();


		// for monster purchasing
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener,
				Event.Priority.Normal, this);

		// monitor plugins - if any are enabled/disabled by a plugin manager
		pListener = new BSPluginListener(this);
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, pListener,
				Event.Priority.Monitor, this);

		BetterShopLogger.Info(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is enabled!");

		//sendErrorReport("Test Error", new Exception());
		// usage stats tracking :)
		if (config.sendErrorReports) { // setting to allow privacy-minded people some privacy..
			me.jascotty2.lib.bukkit.FTP_PluginTracker.queueSend(this, config.unMaskErrorID);
		}
	}

	public void onDisable() {
		// NOTE: All registered events are automatically unregistered when a plugin is disabled
		try {
			lastCommand = "(disabling)";

			shopManager.closeAll();

			if (signShop != null) {
				signShop.save();
				signShop.stopProtecting();
			}
			signShop = null;

			if (chestShop != null) {
				chestShop.save();
				chestShop.closeAllChests();
			}
			chestShop = null;

			keyListener = null;
			buttonListener = null;

			BetterShopLogger.CloseCommandLog();
			BetterShopErrorTracker.messenger = null;

			BetterShopLogger.Fine("disabled");

		} catch (Throwable t) {
			BetterShopLogger.Severe("error disabling..", t, false);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		String commandName = command.getName().toLowerCase(), argStr = Str.concatStr(args, " ");
		if (!commandManager.hasCommand(commandName)) {
			// No command found!
			return false;
		}
		lastCommand = (sender instanceof Player ? "player:" : "console:") + commandName + " " + argStr;

		if (sender instanceof Player
				&& (!shopManager.locationHasShop(((Player) sender).getLocation())
				|| (Str.isIn(commandName,
				new String[]{"shopbuy", "shopbuyall", "shopbuystack",
					"shopsell", "shopsellall", "shopsellstack", /*
				 * "shoplist",
				 * "shopitems",
				 * "shopcheck",
				 * "shoplistkits",
				 * "shopadd",
				 * "shopremove"
				 */})
				&& !commandShopEnabled(((Player) sender).getLocation())))) {
			BSutils.sendMessage(sender, config.getString("regionShopDisabled"));
			return true;
		}

		if (config.logCommands) {
			BetterShopLogger.LogCommand(
					sender instanceof Player ? ((Player) sender).getName() : "(console)",
					commandLabel + " " + argStr);
		}

		if (!BSEcon.active()
				&& (commandName.contains("buy")
				|| commandName.contains("sell")
				|| argStr.contains("buy") || argStr.contains("sell"))) {
			BSutils.sendMessage(sender, ChatColor.RED
					+ "BetterShop is missing a dependency. Check the console.");
			BetterShopLogger.Severe("Missing: iConomy or BOSEconomy", false);
			return true;
		}

		checkRestock();

		try {
			commandManager.execute(sender, commandName, args);
			return true;
		} catch (MissingNestedCommandException e) {
			//BSutils.sendMessage(sender, ChatColor.RED + e.getMessage());
			BSutils.sendMessage(sender, ChatColor.RED + e.getUsage());
		} catch (CommandUsageException e) {
			BSutils.sendMessage(sender, ChatColor.RED + e.getMessage());
			BSutils.sendMessage(sender, ChatColor.RED + e.getUsage());
		} catch (WrappedCommandException e) {
			BetterShopLogger.Severe("Unexpected Error executing a command", e.getCause());
			BSutils.sendMessage(sender, ChatColor.RED + "Problem Executing Command!");
		} catch (CommandException e) {
			BetterShopLogger.Warning(e);
			BSutils.sendMessage(sender, ChatColor.RED + /*"Problem Executing Command!"*/ e.getMessage());
		} catch (Exception e) {
			BetterShopLogger.Severe("Unexpected Error executing a command", e, false);
			BSutils.sendMessage(sender, ChatColor.RED + "Unexpected Error executing command");
			return true;
		} catch (Throwable t) {
			BetterShopLogger.Severe("Unexpected Error in BetterShop", t);
			BSutils.sendMessage(sender, ChatColor.RED + "Unexpected Error executing command");
			return true;
		}
		//TODO: help can get help from any command.
		//HelpCommands.help(sender, ArrayManip.arrayConcat(new String[]{command.getName()}, args));
		return true;
	}

	public static void setLastCommand(String lastCommand) {
		StackTraceElement stm[] = (new Exception()).getStackTrace();
		if (stm[1].getClass().getPackage().getName().startsWith(BetterShop.class.getPackage().getName())) {
			BetterShop.lastCommand = lastCommand;
		} else {
			BetterShopLogger.Warning("Stranger Attempted to modify last command");
			System.out.println(stm[1].getClass().getPackage().getName());
			BetterShopLogger.Log(Level.INFO, new Exception());
		}
	}

	public static Plugin getPlugin() {
		return bettershopPlugin;
	}

	public static BSConfig getConfig() {
		return config;
	}

	public static String getLastCommand() {
		return lastCommand;
	}

	public static BSPriceList getMainPricelist() {
		return shopManager.getShop((String) null).pricelist;
	}

	public static BSPriceList getPricelist(Location l) {
		return shopManager.getShop(l).pricelist;
	}

	public static BSPriceList getPricelist(CommandSender player) {
		return getShop(player).pricelist;
	}

	public static BSSignShop getSignShop() {
		return signShop;
	}

	public static BSChestShop getChestShop() {
		return chestShop;
	}

	public static BSItemStock getStock(Location l) {
		return shopManager.getShop(l).stock;
	}

	public static BSItemStock getStock(CommandSender player) {
		return getShop(player).stock;
	}

	public static void checkRestock() {
		shopManager.checkRestock();
	}

	public static void restock() {
		shopManager.restock();
	}

	public static BSTransactionLog getTransactions(Location l) {
		return shopManager.getShop(l).transactions;
	}

	public static BSTransactionLog getTransactions(CommandSender player) {
		return getShop(player).transactions;
	}

	public static Shop getMainShop() {
		return shopManager.getShop((String) null);
	}

	public static Shop getShop(Location l) {
		return shopManager.getShop(l);
	}

	public static Shop getShop(String s) {
		return shopManager.getShop(s);
	}

	public static Shop getShop(CommandSender player) {
		return shopManager.getShop(player instanceof Player ? ((Player) player).getLocation() : null);
	}

	public static Collection<Shop> getShops() {
		return shopManager.getShops();
	}

	public static RegionShopManager getShopManager() {
		return shopManager;
	}

	public static boolean commandShopEnabled(Location loc) {
		return shopManager.isCommandShopEnabled(loc);
	}

	public static boolean spoutEnabled(){
		return buttonListener != null;
	}

	public static int reload(CommandSender sender) {
		int errors = 0;
		if (config.signShopEnabled && signShop.saveDelayActive()) {
			if (signShop.save()) {
				BSutils.sendMessage(sender, BetterShop.getSignShop().numSigns() + " signs saved.");
			} else {
				BSutils.sendMessage(sender, ChatColor.RED + "shop signs db save error");
				++errors;
			}
		}

		if (config.signShopEnabled && config.tntSignDestroyProtection) {
			signShop.stopProtecting();
		}
		if (JItemDB.load(BSConfig.itemDBFile)) {
			BSutils.sendMessage(sender, JItemDB.size() + " items loaded.");
		} else {
			BetterShopLogger.Warning("Cannot Load Items db!");
			BSutils.sendMessage(sender, ChatColor.RED + "Item Database Load Error.");
			++errors;
		}

		if (!BetterShop.getConfig().load()) {
			BSutils.sendMessage(sender, ChatColor.RED + "Config loading error.");
			++errors;
		} else {
			BSutils.sendMessage(sender, "Config.yml loaded.");
		}

		shopManager.closeAll();
		errors += shopManager.load();

		if (config.signShopEnabled && signShop.load()) {
			BSutils.sendMessage(sender, BetterShop.getSignShop().numSigns() + " signs loaded.");
		} else {
			BSutils.sendMessage(sender, ChatColor.RED + "shop signs db load error");
			++errors;
		}
		if (BetterShop.getConfig().signShopEnabled && BetterShop.getConfig().tntSignDestroyProtection) {
			BetterShop.getSignShop().startProtecting();
		}
		return errors;
	}

	public static class ServerReload extends TimerTask {

		Server reload = null;

		public ServerReload(Server s) {
			reload = s;
		}

		public void start(long wait) {
			(new Timer()).schedule(this, wait);
		}

		@Override
		public void run() {
			if (reload != null) {
				reload.reload();
			}
		}
	}
}
