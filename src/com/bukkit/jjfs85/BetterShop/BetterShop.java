package com.bukkit.jjfs85.BetterShop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * BetterShop for Bukkit
 * 
 * @author jjfs85
 */
public class BetterShop extends JavaPlugin {
	private final BetterShopPlayerListener playerListener = new BetterShopPlayerListener(
			this);
	@SuppressWarnings("unused")
	private final BetterShopPlayerListener blockListener = new BetterShopPlayerListener(
			this);
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();

	public BetterShop(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) throws IOException {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
		// TODO: Place any custom initialization code here

		// NOTE: Event registration should be done in onEnable not here as all
		// events are unregistered when a plugin is disabled
	}

	public void onEnable() {
		// Register our events
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvent(Event.Type.PLAYER_COMMAND, this.playerListener,
				Event.Priority.Normal, this);

		// EXAMPLE: Custom code, here we just output some info so we can check
		// all is well
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is enabled!");

		// TODO: Place any custom enable code here including the registration of
		// any events

		// Load up items.db
		File folder = new File("plugins", pdfFile.getName());
		try {
			itemDb.load(folder, "items.db");
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	public void onDisable() {
		// TODO: Place any custom disable code here

		// NOTE: All registered events are automatically unregistered when a
		// plugin is disabled

		// EXAMPLE: Custom code, here we just output some info so we can check
		// all is well
		System.out.println("BetterShop now unloaded!");
	}

	public boolean isDebugging(final Player player) {
		if (debugees.containsKey(player)) {
			return debugees.get(player);
		} else {
			return false;
		}
	}

	public void setDebugging(final Player player, final boolean value) {
		debugees.put(player, value);
	}

	public void list(Player player, int page) {
		player.sendMessage("some sorta shop list");
		// TODO Implement list method
	}

	public void buy(Player player, int item, int amt) {
		player.sendMessage("some sorta shop buying stuff");
		// TODO Implement buy method
	}

	public void sell(Player player, int item, int amt) {
		player.sendMessage("some sorta selling things");
		// TODO Implement sell method
	}

	public void add(Player player, int item, int buyamt, int sellamt) {
		player.sendMessage("you're adding something");
		// TODO Implement add method
	}

	public void remove(Player player, int item) {
		player.sendMessage("removin' gaems");
		// TODO Implement remove method
	}

	public void update(Player player, int item, int buyamt, int sellamt) {
		player.sendMessage("you're updating " + item + " in the shop!");
		// TODO Implement update method
	}

	public void help(Player player) {
		player.sendMessage("Help yourself.");
		// TODO Implement help method
	}
}
