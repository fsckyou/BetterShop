package com.bukkit.jjfs85.BetterShop;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class BetterShopPlayerListener extends PlayerListener {
	public static BetterShop plugin;

	public BetterShopPlayerListener(BetterShop instance) {
		plugin = instance;
	}

	@Override
	public void onPlayerCommand(PlayerChatEvent event) {
		int i = 0, a = 0, b = 0, s = 0;
		// Make the message a string.
		String[] split = event.getMessage().split(" ");
		// Get the player that talked.
		Player player = event.getPlayer();
		// Accept shop command, parse arguments, check values
		if (split[0].equalsIgnoreCase("/shop")) {
			if (split.length > 1) {
				if (split[1].equalsIgnoreCase("list")) {
					if (split.length == 3) {
						try {
							// A 3rd param? Is it a page number?
							i = this.stringToInt(split[2]);
						} catch (Exception e) {
							// Pass page 1
							i = 1;
						}
						plugin.list(player, i);
					} else {
						plugin.list(player, 1);
					}
				} else {
					// Now we expect the 3rd param to be an item.
					if (split.length > 2) {
						try {
							// First try the items.db
							itemDb.get(split[2]);
						} catch (Exception doh) {
							try {
								// That didn't work. Is it a number?
								i = stringToInt(split[2]);
							} catch (Exception doh2) {
								// Nope. It's fucked.
								player
										.sendMessage("§c[§7SHOP§c] I don't know what §f"
												+ split[2]
												+ "§c is. Maybe try using the ID #.");
							}
						}
					}
					if (split[1].equalsIgnoreCase("buy")) {
						if (split.length == 4) {
							try {
								// The 4th param is an amount
								a = this.stringToInt(split[3]);
							} catch (Exception e) {
								plugin.help(player);
							}
							plugin.buy(player, i, a);
						} else
							plugin.help(player);
					}
					if (split[1].equalsIgnoreCase("sell")) {
						if (split.length == 4) {
							try {
								// The 4th param is an amount
								a = this.stringToInt(split[3]);
							} catch (Exception e) {
								plugin.help(player);
							}
							plugin.sell(player, i, a);
						} else
							plugin.help(player);
					}
					if (split[1].equalsIgnoreCase("add")) {
						if (split.length == 5) {
							try {
								// The 4th param is an amount
								b = this.stringToInt(split[3]);
							} catch (Exception e) {
								plugin.help(player);
							}
							try {
								// The 5th param is an amount
								s = this.stringToInt(split[4]);
							} catch (Exception e) {
								plugin.help(player);
							}
							plugin.add(player, i, b, s);
						} else
							plugin.help(player);
					}
					if (split[1].equalsIgnoreCase("remove")) {
						if (split.length == 4)
							plugin.remove(player, i);
						else
							plugin.help(player);
					}
					if (split[1].equalsIgnoreCase("update")) {
						if (split.length == 5)
							plugin.update(player, i, b, s);
						else
							plugin.help(player);
					}
				}
			}
			plugin.help(player);
			event.setCancelled(true);
		}
	}

	private int stringToInt(String s) throws Exception {
		int i;
		try {
			i = Integer.parseInt(s);
			if ((i < 1) || (i > 2258))
				throw new Exception();
		} catch (NumberFormatException nfe) {

			throw new Exception();
		}
		return i;
	}
}
