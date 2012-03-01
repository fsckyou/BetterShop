/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: adds & removes registered plugins
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

import me.jascotty2.bettershop.commands.HelpCommands;
import me.jascotty2.bettershop.spout.SpoutKeyListener;
import me.jascotty2.bettershop.spout.SpoutPopupListener;
import me.jascotty2.bettershop.utils.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopErrorTracker;
import me.jascotty2.bettershop.utils.BetterShopLogger;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.jascotty2.minecraftim.MinecraftIM;
import com.nijikokun.bukkit.Permissions.Permissions;
import me.taylorkelly.help.Help;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.getspout.spoutapi.Spout;

/**
 * @author jacob
 */
class BSPluginListener implements Listener {

	BetterShop shop;

	public BSPluginListener(BetterShop plugin) {
		shop = plugin;
		PluginManager pm = plugin.getServer().getPluginManager();
		checkPermissions(pm.getPlugin("Permissions"));
		checkMIM(pm.getPlugin("MinecraftIM"));
		checkSpout(pm.getPlugin("Spout"));
		checkHelp(pm.getPlugin("Help"));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginEnable(PluginEnableEvent event) {
		if (event.getPlugin().isEnabled()) { // double-checking if enabled
			String pName = event.getPlugin().getDescription().getName();
			if (pName.equals("Help")) {
				checkHelp(event.getPlugin());
			} else if (pName.equals("MinecraftIM")) {
				checkMIM(event.getPlugin());
			} else if (pName.equals("Permissions")) {
				checkPermissions(event.getPlugin());
			} else if (pName.equals("Spout")) {
				checkSpout(event.getPlugin());
			} else {
				BetterShop.economy.onPluginEnable(event);
			}
		}
	}

	public final void checkMIM(Plugin p) {
		if (BetterShopErrorTracker.messenger == null && p instanceof MinecraftIM) {
			BetterShopErrorTracker.messenger = (MinecraftIM) p;
			BetterShopLogger.Info("linked to MinecraftIM");
		}
	}

	public final void checkHelp(Plugin p) {
		if (!HelpCommands.helpPluginEnabled && p instanceof Help) {
			HelpCommands.registerHelp(p);
			BetterShopLogger.Info("'Help' support enabled.");
		}
	}

	public final void checkPermissions(Plugin p) {
		if (BSPermissions.permissionsPlugin == null && p instanceof Permissions) {
			BSPermissions.permissionsPlugin = (Permissions) p;
			BetterShopLogger.Log("Attached to Permissions.");
		}
	}

	public final void checkSpout(Plugin p) {
		if (BetterShop.keyListener == null && p instanceof Spout) {

			BetterShopLogger.Log("Spout Found! :)");
//				File spFile = new File(test.getClass().getProtectionDomain().getCodeSource().getLocation().getPath().
//				replace("%20", " ").replace("%25", "%"));
			try {
				Plugin bp = BetterShop.getPlugin();
				//bp.super.getClassLoader().loadClass(event.getPlugin().getDescription().getMain());
				//JavaPlugin.class.getClassLoader().loadClass(event.getPlugin().getDescription().getMain());
				((JavaPlugin) bp).getClass().getClassLoader().loadClass(p.getDescription().getMain());
				BetterShop.keyListener = new SpoutKeyListener();
				BetterShop.buttonListener = new SpoutPopupListener();

				// spout listeners
				PluginManager pm = shop.getServer().getPluginManager();
				pm.registerEvents(BetterShop.keyListener,shop);
				pm.registerEvents(BetterShop.buttonListener,shop);

				BetterShop.chestShop.registerSpout(true);

			} catch (ClassNotFoundException ex) {
				BetterShopLogger.Severe("Error loading Spout!", ex);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginDisable(PluginDisableEvent event) {
		if (!event.getPlugin().isEnabled()) {
			String pName = event.getPlugin().getDescription().getName();
			if (pName.equals("MinecraftIM")) {
				BetterShopErrorTracker.messenger = null;
				BetterShopLogger.Info("MinecraftIM link disabled");
			} else if (pName.equals("Help")) {
				HelpCommands.helpPluginEnabled = false;
			} else if (pName.equals("Permissions")) {
				if (event.getPlugin() instanceof Permissions) {
					BSPermissions.permissionsPlugin = (Permissions) event.getPlugin();
					BetterShopLogger.Log("Permissions disabled.");
				}
			} else if (pName.equals("Spout")) {
				BetterShop.keyListener = null;
				BetterShop.buttonListener = null;
				BetterShop.chestShop.registerSpout(false);
				BetterShopLogger.Log("Spout disabled.");
			} else {
				BetterShop.economy.onPluginDisable(event);
			}
		}
	}
} // end class BSPluginListener

