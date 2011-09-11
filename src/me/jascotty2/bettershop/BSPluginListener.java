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
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.java.JavaPlugin;

import com.jascotty2.minecraftim.MinecraftIM;
import com.nijikokun.bukkit.Permissions.Permissions;
import me.taylorkelly.help.Help;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.getspout.spout.Spout;

/**
 * @author jacob
 */
class BSPluginListener extends ServerListener {

	BetterShop shop;

	public BSPluginListener(BetterShop plugin) {
		shop = plugin;
	}

	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		if (event.getPlugin().isEnabled()) { // double-checking enabled
			String pName = event.getPlugin().getDescription().getName();
			if (pName.equals("Help")) {
				if (event.getPlugin() instanceof Help) {
					HelpCommands.registerHelp(event.getPlugin());
					BetterShopLogger.Info("'Help' support enabled.");
				}
			} else if (pName.equals("MinecraftIM")) {
				if (event.getPlugin() instanceof MinecraftIM) {
					BetterShopErrorTracker.messenger = (MinecraftIM) event.getPlugin();
					BetterShopLogger.Info("linked to MinecraftIM");
				}
			} else if (pName.equals("Permissions")) {
				if (event.getPlugin() instanceof Permissions) {
					BSPermissions.permissionsPlugin = (Permissions) event.getPlugin();
					BetterShopLogger.Log("Attached to Permissions.");
				}
			} else if (pName.equals("Spout")) {
				if (event.getPlugin() instanceof Spout) {

					BetterShopLogger.Log("Spout Found! :)");
//				File spFile = new File(test.getClass().getProtectionDomain().getCodeSource().getLocation().getPath().
//				replace("%20", " ").replace("%25", "%"));
					try {
						Plugin bp = BetterShop.getPlugin();
						//bp.super.getClassLoader().loadClass(event.getPlugin().getDescription().getMain());
						//JavaPlugin.class.getClassLoader().loadClass(event.getPlugin().getDescription().getMain());
						((JavaPlugin) bp).getClass().getClassLoader().loadClass(event.getPlugin().getDescription().getMain());
						BetterShop.keyListener = new SpoutKeyListener();
						BetterShop.buttonListener = new SpoutPopupListener();

						// spout listeners
						PluginManager pm = shop.getServer().getPluginManager();
						pm.registerEvent(Event.Type.CUSTOM_EVENT, BetterShop.keyListener,
								Event.Priority.Normal, shop);
						pm.registerEvent(Event.Type.CUSTOM_EVENT, BetterShop.buttonListener,
								Event.Priority.Normal, shop);

					} catch (ClassNotFoundException ex) {
						BetterShopLogger.Severe("Error loading Spout!", ex);
					}
				}
			} else {
				BetterShop.economy.onPluginEnable(event);
			}
		}
	}

	@Override
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
					BetterShopLogger.Log("Attached to Permissions.");
				}
			} else if (pName.equals("Spout")) {
				BetterShopLogger.Log("Spout Found! :)");
//				File spFile = new File(test.getClass().getProtectionDomain().getCodeSource().getLocation().getPath().
//				replace("%20", " ").replace("%25", "%"));
				try {
					//Plugin bp = BetterShop.getPlugin();
					//bp.super.getClassLoader().loadClass(test.getDescription().getMain());
					JavaPlugin.class.getClassLoader().loadClass(event.getPlugin().getDescription().getMain());
					BetterShop.keyListener = new SpoutKeyListener();
					BetterShop.buttonListener = new SpoutPopupListener();
				} catch (ClassNotFoundException ex) {
					BetterShopLogger.Severe("Error loading Spout!", ex);
				}
			} else {
				BetterShop.economy.onPluginDisable(event);
			}
		}
	}
} // end class BSPluginListener

