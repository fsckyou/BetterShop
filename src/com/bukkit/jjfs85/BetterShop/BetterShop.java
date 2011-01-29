package com.bukkit.jjfs85.BetterShop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import com.nijikokun.bukkit.iConomy.iConomy;

public class BetterShop extends JavaPlugin{

	
	public BetterShop(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}
/**
 *  BetterShop
 *  Author: jjfs85
 *  Version: 0.01
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
		
	}
	
	public void help(Player player) {
		
	}
}
