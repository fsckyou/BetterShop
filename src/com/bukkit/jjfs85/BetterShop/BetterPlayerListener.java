package com.bukkit.jjfs85.BetterShop;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class BetterPlayerListener extends PlayerListener {
		public static BetterShop plugin;
		public BetterPlayerListener(BetterShop instance) {
			plugin = instance;
		}
		
		public void onPlayerCommand(PlayerChatEvent event){
            //Make the message a string.
            String[] split = event.getMessage().split(" ");
            //Get the player that talked.
            Player player = event.getPlayer();
            //Accept shop command, parse arguments, check values
            if (split[0].equalsIgnoreCase("/shop")) {
            		if (split[1].equalsIgnoreCase("list"))
            			if (split.length == 3)
            				plugin.list(player, split[2]);
            			else
            				plugin.list(player, 1);
            		if (split[1].equalsIgnoreCase("buy"))
            			if (split.length == 4)
            				plugin.buy(player, split[2], split[3]);
            		if (split[1].equalsIgnoreCase("sell"))
            			if (split.length == 4)
            				plugin.sell(player, split[2], split[3]);
                    event.setCancelled(true);
            }
		}
		private int StringToInt(string s) {
			
		}
}
