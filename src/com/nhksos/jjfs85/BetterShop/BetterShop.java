package com.nhksos.jjfs85.BetterShop;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.Messaging;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * BetterShop for Bukkit
 * 
 * @author jjfs85
 */
public class BetterShop extends JavaPlugin {
	private final static Logger logger = Logger.getLogger("Minecraft");
	private static final String name = "BetterShop";
	static PermissionHandler Permissions = null;
	private final static File pluginFolder = new File("plugins", name);
	static BSConfig configfile;
	private BSCommand bscommand;

	public BetterShop(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) throws IOException {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);

		// NOTE: Event registration should be done in onEnable not here as all
		// events are unregistered when a plugin is disabled
	}

	public void onEnable() {

		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info("Loading " + pdfFile.getName() + " version "
				+ pdfFile.getVersion() + "...");

		try {
			configfile = new BSConfig(pluginFolder, "config.yml");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ready command handlers
		try {
			bscommand = new BSCommand();
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning("cannot load PriceList.yml");
			this.setEnabled(false);
		}

		// ready items.db
		try {
			itemDb.load(pluginFolder, "items.db");
		} catch (IOException e) {
			logger.warning("cannot load items.db");
			e.printStackTrace();
			this.setEnabled(false);
		}

		// setup the permissions
		setupPermissions();

		// Just output some info so we can check
		// all is well
		logger.info(pdfFile.getName() + " version " + pdfFile.getVersion()
				+ " is enabled!");
	}

	public void onDisable() {

		// NOTE: All registered events are automatically unregistered when a
		// plugin is disabled

		// EXAMPLE: Custom code, here we just output some info so we can check
		// all is well
		logger.info("BetterShop now unloaded");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		String[] trimmedArgs = args;
		String commandName = command.getName().toLowerCase();

		try {
			logger.info(((Player) sender).getName() + " used command "
					+ command.getName());
		} catch (Exception e) {
		}

		if (commandName.equals("shoplist")) {
			return bscommand.list(sender, trimmedArgs);
		} else if (commandName.equals("shophelp")) {
			return bscommand.help(sender);
		} else if (commandName.equals("shopbuy")) {
			return bscommand.buy(sender, trimmedArgs);
		} else if (commandName.equals("shopsell")) {
			return bscommand.sell(sender, trimmedArgs);
		} else if (commandName.equals("shopadd")) {
			return bscommand.add(sender, trimmedArgs);
		} else if (commandName.equals("shopremove")) {
			return bscommand.remove(sender, trimmedArgs);
		} else if (commandName.equals("shopload")) {
			return bscommand.load(sender);
		} else if (commandName.equals("shopcheck")) {
			return bscommand.check(sender, trimmedArgs);
		}
		return false;
	}

	void setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin(
				"Permissions");

		if (BetterShop.Permissions == null) {
			if (test != null) {
				BetterShop.Permissions = ((Permissions) test).getHandler();
			} else {
				logger.info(Messaging.bracketize(name)
						+ " Permission system not enabled. Disabling plugin.");
				this.getServer().getPluginManager().disablePlugin(this);
			}
		}
	}
}
