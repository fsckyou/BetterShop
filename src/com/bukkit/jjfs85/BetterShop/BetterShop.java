package com.bukkit.jjfs85.BetterShop;

import java.io.File;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterShop extends JavaPlugin {

	public BetterShop(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}

	/**
	 * BetterShop Author: jjfs85 Version: 0.01
	 */

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEnable() {
		// TODO Auto-generated method stub

	}

	public void list(Player player, int page) {
		// TODO Implement list method
	}

	public void buy(Player player, int item, int amt) {
		// TODO Implement buy method
	}

	public void sell(Player player, int item, int amt) {
		// TODO Implement sell method
	}

	public void add(Player player, int item, int amt, int buyamt, int sellamt) {
		// TODO Implement add method
	}

	public void remove(Player player, int item, int amt, int buyamt, int sellamt) {
		// TODO Implement remove method
	}

	public void help(Player player) {
		// TODO Implement help method
	}
}
