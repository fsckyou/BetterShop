package com.bukkit.jjfs85.BetterShop;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class BetterPlayerListener extends PlayerListener {
	public static BetterShop plugin;

	public BetterPlayerListener(BetterShop instance) {
		plugin = instance;
	}

	@Override
	public void onPlayerCommand(PlayerChatEvent event) {
		@SuppressWarnings("unused")
		int i = 0, a = 0, b = 0, s = 0;
		// Make the message a string.
		String[] split = event.getMessage().split(" ");
		// Get the player that talked.
		Player player = event.getPlayer();
		// Accept shop command, parse arguments, check values
		// TODO EVERYTHING HERE.
		if (split[0].equalsIgnoreCase("/shop")) {
			if (split.length > 2) {
				if ((i = this.stringToInt(split[2])) < 0) {
					player.sendMessage("Invalid Item.");
				}
			}
			if (split[1].equalsIgnoreCase("list")) {
				if (split.length == 3)
					plugin.list(player, i);
				else
					plugin.list(player, 1);
			}
			if (split[1].equalsIgnoreCase("buy")) {
				if (split.length == 4)
					plugin.buy(player, i, a);
				else
					plugin.help(player);
			}
			if (split[1].equalsIgnoreCase("sell")) {
				if (split.length == 4)
					plugin.sell(player, i, a);
				else
					plugin.help(player);
			}
			event.setCancelled(true);
		}
	}

	private int stringToInt(String s) {
		int i;
		try {
			i = Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			return -1;
		}
		return i;
	}
}
