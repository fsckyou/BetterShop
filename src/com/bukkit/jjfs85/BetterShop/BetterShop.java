package com.bukkit.jjfs85.BetterShop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.Messaging;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.bukkit.iConomy.iConomy;

/**
 * BetterShop for Bukkit
 * 
 * @author jjfs85
 */
public class BetterShop extends JavaPlugin {
	private final static Logger logger = Logger.getLogger("Minecraft");
	private final static String commandPrefix = "";
	private final static String messagePrefix = "§c[§7SHOP§c] ";
	private static final String name = "BetterShop";
	private final HashMap<Integer, ItemStack> leftover = new HashMap<Integer, ItemStack>();
	private final BSPriceList PriceList = new BSPriceList();
	private static PermissionHandler Permissions = null;
	private final static File pluginFolder = new File("plugins", name);

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

		// Load pricelist.yml
		try {
			PriceList.load(pluginFolder, "PriceList.yml");
		} catch (IOException e) {
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

	public void setupPermissions() {
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
			return list(sender, trimmedArgs);
		} else if (commandName.equals("shophelp")) {
			return help(sender);
		} else if (commandName.equals("shopbuy")) {
			return buy(sender, trimmedArgs);
		} else if (commandName.equals("shopsell")) {
			return sell(sender, trimmedArgs);
		} else if (commandName.equals("shopadd")) {
			return add(sender, trimmedArgs);
		} else if (commandName.equals("shopremove")) {
			return remove(sender, trimmedArgs);
		} else if (commandName.equals("shopload")) {
			return load(sender);
		} else if (commandName.equals("shopcheck")) {
			return check(sender, trimmedArgs);
		}
		return false;
	}

	public boolean check(CommandSender player, String[] s) {
		MaterialData mat;
		if (!hasPermission(player, "BetterShop.user.check")) {
			sendMessage(player, "OI! You don't have permission to do that!");
			return true;
		}
		if (s.length != 1) {
			return false;
		}
		try {
			mat = itemDb.get(s[0]);
		} catch (Exception e) {
			sendMessage(player, "What is " + s[0] + "?");
			return true;
		}
		try {
			sendMessage(player, String.format("[%s] Buy: %d Sell: %d", itemDb
					.getName(mat.getItemTypeId(), mat.getData()), PriceList
					.getBuyPrice(mat.getItemTypeId()), PriceList
					.getSellPrice(mat.getItemTypeId())));
		} catch (Exception e) {
			sendMessage(player, "That's not on the market.");
			return true;
		}
		return true;
	}

	public boolean list(CommandSender player, String[] s) {
		int pagesize = 9;
		int page = 0;
		if (!hasPermission(player, "BetterShop.user.list")) {
			sendMessage(player, "OI! You don't have permission to do that!");
			return true;
		}
		try {
			page = (s.length == 0) ? 1 : Integer.parseInt(s[0]);
		} catch (Exception e) {
			sendMessage(player, "That's not a page number, idiot.");
			return false;
		}
		int pages = (int) Math.ceil((double) PriceList.NameMap.size()
				/ pagesize);
		if ((s.length != 0) && (s.length != 1)) {
			return false;
		} else if (page > pages) {
			sendMessage(player, "There is no page " + page + ".");
			return true;
		} else {
			int linenum = 1;
			int i = 1;
			sendMessage(player, String.format(
					"---- Price-list Page: %2d of %2d ----", page, pages));
			while ((linenum < page * pagesize) && (i < 2280)) {
				try {
					itemDb.get(i, (byte) 0);
				} catch (Exception e1) {
					i++;
					continue;
				}
				if (PriceList.isForSale(i)) {
					if (linenum > (page - 1) * pagesize) {
						try {
							sendMessage(player, "["
									+ itemDb.getName(i, (byte) 0) + "] Buy: "
									+ PriceList.getBuyPrice(i) + " Sell: "
									+ PriceList.getSellPrice(i));
						} catch (Exception e) {
							e.printStackTrace();
							logger.warning("Pricelist error!");
						}
					}
					linenum++;
				}
				i++;
			}
			sendMessage(player, "-----------------------------");
			return true;
		}
	}

	public boolean buy(CommandSender player, String[] s) {
		MaterialData item = new MaterialData(0);
		int price = 0;
		int amtleft = 0;
		int amtbought = 1;
		int cost = 0;
		if (!hasPermission(player, "BetterShop.user.buy")) {
			sendMessage(player, "OI! You don't have permission to do that!");
			return true;
		}
		if ((s.length > 2) || (s.length == 0)) {
			sendMessage(player, "What?");
			return false;
		} else if (anonymousCheck(player)) {
			return true;
		} else {
			try {
				item = itemDb.get(s[0]);
			} catch (Exception e2) {
				sendMessage(player, "What is " + s[0] + "?");
			}
			try {
				price = PriceList.getBuyPrice(item.getItemTypeId());
			} catch (Exception e1) {
				sendMessage(player, "That's not for sale!");
				return true;
			}
			if (s.length == 2)
				try {
					amtbought = Integer.parseInt(s[1]);
				} catch (Exception e) {
					return false;
				}
			if (amtbought < 0) {
				sendMessage(player, "...Nice try.");
				return true;
			}
			cost = amtbought * price;
			try {
				if (debit(player, cost)) {
					sendMessage(player, "For "
							+ amtbought
							+ " "
							+ itemDb.getName(item.getItemTypeId(), item
									.getData()) + "? " + cost + " "
							+ iConomy.currency + ". Thank you, come again!");
					leftover.clear();
					ItemStack itemS = new ItemStack(item.getItemTypeId(),
							amtbought, (short) 0, item.getData());
					leftover.putAll(((Player) player).getInventory().addItem(
							itemS));
					amtleft = (leftover.size() == 0) ? 0 : (leftover.get(0))
							.getAmount();
					if (amtleft > 0) {
						cost = amtleft * price;
						sendMessage(player,
								"You didn't have enough space for all that.");
						sendMessage(player, "I'm refundng " + cost + " "
								+ iConomy.currency
								+ " for what you couldn't hold.");
						credit(player, cost);
					}
					return true;
				} else {
					sendMessage(player, "Not enough money.");
					return true;
				}
			} catch (Exception e) {
				return false;
			}
		}
	}

	public boolean sell(CommandSender player, String[] s) {
		int amtSold = 1;
		MaterialData item = new MaterialData(0);
		if (!hasPermission(player, "BetterShop.user.sell")) {
			sendMessage(player, "OI! You don't have permission to do that!");
			return true;
		}
		if ((s.length > 2) || (s.length == 0)) {
			return false;
		} else if (anonymousCheck(player)) {
			return true;
		} else {
			try {
				item = itemDb.get(s[0]);
			} catch (Exception e1) {
				sendMessage(player, "What's " + s[0] + "?");
				return false;
			}
			ItemStack itemsToSell = item.toItemStack();
			if (s.length == 2)
				try {
					amtSold = Integer.parseInt(s[1]);
				} catch (Exception e) {
					sendMessage(player, s[1] + " is definitely not a number.");
				}
			if (amtSold < 0) {
				sendMessage(player,
						"Why would you want to buy at the selling price!?");
				return true;
			}
			itemsToSell.setAmount(amtSold);
			PlayerInventory inv = ((Player) player).getInventory();
			leftover.clear();
			leftover.putAll(inv.removeItem(itemsToSell));
			if (leftover.size() > 0) {
				amtSold = amtSold - leftover.get(0).getAmount();
				sendMessage(player, "You only had " + amtSold + ".");
			}
			try {
				credit(player, amtSold
						* PriceList.getSellPrice(item.getItemTypeId()));
				sendMessage(player, "You sold " + amtSold + " "
						+ itemDb.getName(item.getItemTypeId(), item.getData())
						+ " at " + PriceList.getSellPrice(item.getItemTypeId())
						+ " each for a total of " + amtSold
						* PriceList.getSellPrice(item.getItemTypeId()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
	}

	public boolean add(CommandSender player, String[] s) {
		if (s.length != 3) {
			return false;
		}
		MaterialData mat = new MaterialData(0);
		try {
			mat = itemDb.get(s[0]);
		} catch (Exception e1) {
			sendMessage(player, "What's " + s[0] + "?");
			return false;
		}
		if (!hasPermission(player, "BetterShop.admin.add")) {
			sendMessage(player, "OI! You don't have permission to do that!");
			return true;
		}
		try {
			PriceList.setPrice(s[0], s[1], s[2]);
		} catch (Exception e) {
			sendMessage(player, "Something wasn't right there.");
			return false;
		}
		try {
			sendMessage(player, String.format(
					"[%s] added at the prices:    Buy: %d Sell: %d", itemDb
							.getName(mat.getItemTypeId(), mat.getData()),
					PriceList.getBuyPrice(mat.getItemTypeId()), PriceList
							.getSellPrice(mat.getItemTypeId())));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean remove(CommandSender player, String[] s) {
		if ((!hasPermission(player, "BetterShop.admin.remove"))) {
			sendMessage(player, "OI! You don't have permission to do that!");
			return true;
		}
		if (s.length != 1) {
			return false;
		} else {
			try {
				PriceList.remove(s[0]);
				sendMessage(player, s[0] + " has been removed from the shop.");
			} catch (Exception e) {
				sendMessage(player, "You done goofed. Check the name.");
			}
			return true;
		}
	}

	public boolean load(CommandSender player) {
		if (!hasPermission(player, "BetterShop.admin.load")) {
			sendMessage(player, "OI! You don't have permission to do that!");
			return true;
		}
		try {
			PriceList.load(pluginFolder, "PriceList.yml");
		} catch (IOException e) {
			e.printStackTrace();
			sendMessage(player, "Pricelist load error. See console.");
			return true;
		}
		sendMessage(player, "PriceList loaded.");
		return true;
	}

	public boolean help(CommandSender player) {
		if (!hasPermission(player, "BetterShop.user.help")) {
			sendMessage(player, "OI! You don't have permission to do that!");
			return true;
		}
		sendMessage(player, "--------- Better Shop Usage --------");
		sendMessage(player, "/" + commandPrefix
				+ "shoplist <page> - List shop prices");
		sendMessage(player, "/" + commandPrefix
				+ "shopbuy [item] <amount> - Buy items");
		sendMessage(player, "/" + commandPrefix
				+ "shopsell [item] <amount> - Sell items");
		sendMessage(player, "/" + commandPrefix
				+ "shopcheck [item] - Check prices of item");
		if (BetterShop.hasPermission(player, "BetterShop.admin")) {
			sendMessage(player, "**-------- Admin commands --------**");
			sendMessage(player, "/" + commandPrefix
					+ "shopadd [item] [$buy] [$sell] - Add an item to the shop");
			sendMessage(player, "/" + commandPrefix
					+ "shopremove [item] - Remove an item from the shop");
			sendMessage(player, "/" + commandPrefix
					+ "shopload - Reload the PriceList.yml file");
			sendMessage(player, "----------------------------------");
		}
		return true;
	}

	private static boolean hasPermission(CommandSender player, String string) {
		try {
			if (BetterShop.Permissions.has((Player) player, string)) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	private boolean anonymousCheck(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sendMessage(sender,
					"Cannot execute that command, I don't know who you are!");
			return true;
		} else {
			return false;
		}
	}

	private final static void sendMessage(CommandSender player, String s) {
		player.sendMessage(messagePrefix + s);
	}

	private final boolean debit(CommandSender player, int amount)
			throws Exception {
		int balance = 0;
		Object db = BetterShop.class.getClassLoader().loadClass(
				"com.nijikokun.bukkit.iConomy.iConomy").getField("db")
				.get(null);
		balance = (Integer) BetterShop.class.getClassLoader().loadClass(
				"com.nijikokun.bukkit.iConomy.Database").getMethod(
				"get_balance", String.class).invoke(db,
				((Player) player).getName());
		if (balance < amount)
			return false;
		BetterShop.class.getClassLoader().loadClass(
				"com.nijikokun.bukkit.iConomy.Database").getMethod(
				"set_balance", String.class, Integer.TYPE).invoke(db,
				((Player) player).getName(), balance - amount);
		return true;
	}

	private final boolean credit(CommandSender player, int amount)
			throws Exception {
		int balance = 0;
		Object db = BetterShop.class.getClassLoader().loadClass(
				"com.nijikokun.bukkit.iConomy.iConomy").getField("db")
				.get(null);
		balance = (Integer) BetterShop.class.getClassLoader().loadClass(
				"com.nijikokun.bukkit.iConomy.Database").getMethod(
				"get_balance", String.class).invoke(db,
				((Player) player).getName());
		BetterShop.class.getClassLoader().loadClass(
				"com.nijikokun.bukkit.iConomy.Database").getMethod(
				"set_balance", String.class, Integer.TYPE).invoke(db,
				((Player) player).getName(), balance + amount);
		return true;
	}
}
