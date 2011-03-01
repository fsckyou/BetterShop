package com.nhksos.jjfs85.BetterShop;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Bank;
import com.nijikokun.bukkit.Permissions.Permissions;

/*
 * BetterShop for Bukkit
 * 
 * @author jjfs85
 */
public class BetterShop extends JavaPlugin {
	private final static Logger logger = Logger.getLogger("Minecraft");
	private static final String name = "BetterShop";
	static Permissions Permissions = null;
	static iConomy iConomy = null;
	private final static File pluginFolder = new File("plugins", name);
	static BSConfig configfile;
	private BSCommand bscommand;
	static Bank iBank;

	private final Listener Listener = new Listener();

	private class Listener extends ServerListener {

		public Listener() {
		}

		@SuppressWarnings("static-access")
		@Override
		public void onPluginEnabled(PluginEvent event) {
			if (event.getPlugin().getDescription().getName().equals("iConomy")) {
				BetterShop.iConomy = (iConomy) event.getPlugin();
				logger.info("[BetterShop] Attached to iConomy.");
				iBank = iConomy.getBank();
			}
			if (event.getPlugin().getDescription().getName().equals(
					"Permissions")) {
				BetterShop.Permissions = (Permissions) event.getPlugin();
				logger
						.info("[BetterShop] Attached to Permissions or something close enough to it");
			}
		}
	}

	private void registerEvents() {
		this.getServer().getPluginManager().registerEvent(
				Event.Type.PLUGIN_ENABLE, Listener, Priority.Monitor, this);
	}

	@SuppressWarnings("static-access")
	private void hookDepends() {
		if (this.getServer().getPluginManager().isPluginEnabled("iConomy")) {
			BetterShop.iConomy = (iConomy) this.getServer().getPluginManager()
					.getPlugin("iConomy");
			logger.info("[BetterShop] Attached to iConomy.");
			iBank = iConomy.getBank();
		} else {
			logger.warning("[BetterShop] WARNING: iConomy not yet found...");
		}
		if (this.getServer().getPluginManager().isPluginEnabled("Permissions")) {
			BetterShop.Permissions = (Permissions) this.getServer()
					.getPluginManager().getPlugin("Permissions");
			logger.info("[BetterShop] Attached to Permissions.");
		} else {
			logger
					.warning("[BetterShop] WARNING: Permissions not yet found...");
		}
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
		hookDepends();
		registerEvents();

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
		if ((BetterShop.iConomy == null) || (BetterShop.Permissions == null)) {
			BSutils.sendMessage(sender,
					" BetterShop is missing a dependency. Check the console.");
			logger
					.severe("[BetterShop] Missing: "
							+ ((BetterShop.iConomy == null) ? "iConomy"
									: "Permissions"));
			return true;
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
}
