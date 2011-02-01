package com.bukkit.jjfs85.BetterShop;

import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class BetterShopPlayerListener extends PlayerListener {
	public static BetterShop plugin;

	public BetterShopPlayerListener(BetterShop instance) {
		plugin = instance;
	}

	@Override
	public void onPlayerCommand(PlayerChatEvent event) {
		// Make the message a string.
		String[] split = event.getMessage().split(" ");
		// Get the player that talked.
		CommandSender player = event.getPlayer();
		// Accept shop command, parse arguments, check values
		if (split[0].equalsIgnoreCase("/" + BetterShop.commandPrefix + "shop")) {
			if (split.length > 1) {
				if (split[1].equalsIgnoreCase("list")) {
					plugin.list(player, split);
				} else if (split[1].equalsIgnoreCase("buy")) {
					plugin.buy(player, split);
				} else if (split[1].equalsIgnoreCase("sell")) {
					plugin.sell(player, split);
				} else if (split[1].equalsIgnoreCase("add")) {
					plugin.add(player, split);
				} else if (split[1].equalsIgnoreCase("remove")) {
					plugin.remove(player, split);
				} else if (split[1].equalsIgnoreCase("update")) {
					plugin.update(player, split);
				} else if (split[1].equalsIgnoreCase("load")) {
					plugin.load(player);
				} else {
					plugin.help(player);
				}
			} else {
				plugin.help(player);
			}
			event.setCancelled(true);
		}
	}
}
