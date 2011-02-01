package com.bukkit.jjfs85.BetterShop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Server;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.command.*;
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
	public final static String commandPrefix = "b";
	public final static String messagePrefix = "§c[§7SHOP§c] ";
	private final BetterShopPlayerListener playerListener = new BetterShopPlayerListener(
			this);
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	private final BetterShopPriceList PriceList = new BetterShopPriceList();

	public BetterShop(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) throws IOException {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);

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

		// Load up items.db
		File folder = new File("plugins", pdfFile.getName());
		try {
			itemDb.load(folder, "items.db");
		} catch (IOException e) {
			System.out.println("BetterShop: Items.db error");
		}

		// Load prices
		try {
			PriceList.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onDisable() {

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

	public void setDebugging(final CommandSender player, final boolean value) {
		debugees.put((Player) player, value);
	}

	public void list(CommandSender player, String[] s) {
		BetterShop.sendMessage(player, "some sorta shop list");
		// TODO Implement list method
		try {
			BetterShop.sendMessage(player, "" + PriceList.getBuyPrice(1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void buy(CommandSender player, String[] s) {
		BetterShop.sendMessage(player, "Buying not implemented yet.");
		boolean help = false;
		// TODO Implement buy method
		if (s.length == 4) {
			@SuppressWarnings("unused")
			int i = Integer.parseInt(s[3]);
		} else
			help = true;
		if (help) {
			this.help(player);
		}
	}

	public void sell(CommandSender player, String[] s) {
		BetterShop.sendMessage(player, "some sorta selling things");
		// TODO Implement sell method
	}

	public void add(CommandSender player, String[] s) {
		BetterShop.sendMessage(player, "you're adding something");
		// TODO Implement add method
		if (s.length != 5) {
			this.help(player);
		} else {
				try {
					PriceList.setPrice(s[2], s[3],
							s[4]);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public void remove(CommandSender player, String[] s) {
		BetterShop.sendMessage(player, "Remove not implemented");
		// TODO Implement remove method
	}

	public void update(CommandSender player, String[] s) {
		BetterShop
				.sendMessage(player, "Update not implemented.");
		// TODO Implement update method
	}

	public void load(CommandSender player) {
		// TODO Implement shopping list loading
		try {
			PriceList.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			BetterShop.sendMessage(player, "Pricelist load error. See console.");
		}
		BetterShop.sendMessage(player, "PriceList loaded.");
	}

	public void help(CommandSender player) {
		// TODO Implement help method
		BetterShop.sendMessage(player,
				"--------------- Better Shop Usage ---------------");
		BetterShop
				.sendMessage(player, "/" + commandPrefix + "shop list <page>");
		BetterShop.sendMessage(player, "/" + commandPrefix
				+ "shop buy [item] [amount]");
		BetterShop.sendMessage(player, "/" + commandPrefix
				+ "shop sell [item] [amount]");
		if (BetterShop.hasPermission(player, "Admin")) {
			BetterShop.sendMessage(player, "/" + commandPrefix
					+ "shop add [item] [$buy] [$sell]");
			BetterShop.sendMessage(player, "/" + commandPrefix
					+ "shop remove [item]");
			BetterShop.sendMessage(player, "/" + commandPrefix
					+ "shop update [item] [$buy] [$sell]");
			BetterShop.sendMessage(player, "/" + commandPrefix + "shop load");
		}
	}

	private static boolean hasPermission(CommandSender player, String string) {
		// TODO Implement permission checking using the permissions plugin.
		if (string.equalsIgnoreCase("admin")) {
			if (((HumanEntity) player).getName().equalsIgnoreCase("jjfs85")) {
				return true;
			}
		}
		return false;
	}

	public final static void sendMessage(CommandSender player, String s) {
		player.sendMessage(messagePrefix + s);
	}
}
