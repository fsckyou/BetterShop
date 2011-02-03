package com.bukkit.jjfs85.BetterShop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.*;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.bukkit.iConomy.iConomy;

/**
 * BetterShop for Bukkit
 * 
 * @author jjfs85
 */
public class BetterShop extends JavaPlugin {
	public final static String commandPrefix = "b";
	public final static String messagePrefix = "§c[§7SHOP§c] ";
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	private final HashMap<Integer, ItemStack> leftover = new HashMap<Integer, ItemStack>();
	private final BetterShopPriceList PriceList = new BetterShopPriceList();

	public BetterShop(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) throws IOException {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);

		// NOTE: Event registration should be done in onEnable not here as all
		// events are unregistered when a plugin is disabled
	}

	public void onEnable() {
		// Just output some info so we can check
		// all is well
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is enabled!");

		// Load pricelist.yml
		try {
			PriceList.load();
		} catch (IOException e) {
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

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		String[] trimmedArgs = args;
		String commandName = command.getName().toLowerCase();

		if (commandName.equals("shoplist")) {
			return list(sender, trimmedArgs);
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
		}
		return false;
	}

	public boolean list(CommandSender player, String[] s) {
		int pagesize = 5;
		if ((s.length != 0) && (s.length != 1)) {
			this.help(player);
			return false;
		} else {
			int p = (s.length == 0) ? 1 : Integer.parseInt(s[1]);
			int j = 1;
			int i = 1;
			while ((j < p * pagesize) && (i < 2280)) {
				if (PriceList.isForSale(i)) {
					if (j > (p - 1) * pagesize) {
						try {
							BetterShop.sendMessage(player, "["
									+ Material.getMaterial(i).name()
									+ "] Buy: " + PriceList.getBuyPrice(i)
									+ " Sell: " + PriceList.getSellPrice(i));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					j++;
				}
				i++;
			}
			return true;
		}
	}

	public boolean buy(CommandSender player, String[] s) {
		int material = new Integer (Material.getMaterial(s[0].toUpperCase()).getId());
		int price;
		int amtleft = 0;
		int amtbought = 0;
		int balance = iConomy.db.get_balance(player.toString());
		int cost = 0;
		try {
			price = PriceList.getBuyPrice(material);
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		if ((s.length > 2) || (s.length == 0)) {
			return false;
		} else if (anonymousCheck(player)) {
			return false;
		} else {
			try {
				if (balance < (Integer.parseInt(s[1]) * price)) {
					leftover.putAll(((Player) player).getInventory().addItem(
							new ItemStack(material, Integer.parseInt(s[1]))));
					amtleft = ((leftover.get(material)) == null) ? 0 : (leftover.get(material)).getAmount();
					amtbought = Integer.parseInt(s[1]) - amtleft;
					cost = amtbought * price;
					
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}

	public boolean sell(CommandSender player, String[] s) {
		BetterShop.sendMessage(player, "Selling is not implemented yet.");
		// TODO Implement sell method
		return false;
	}

	public boolean add(CommandSender player, String[] s) {
		if (s.length != 5) {
			this.help(player);
			return false;
		} else {
			try {
				PriceList.setPrice(s[2], s[3], s[4]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}

	public boolean remove(CommandSender player, String[] s) {
		if (s.length != 3) {
			this.help(player);
			return false;
		} else {
			try {
				PriceList.remove(s[2]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}

	public boolean update(CommandSender player, String[] s) {
		if (s.length != 5) {
			this.help(player);
			return false;
		} else {
			try {
				PriceList.setPrice(s[2], s[3], s[4]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}

	public boolean load(CommandSender player) {
		// TODO Implement shopping list loading
		try {
			PriceList.load();
		} catch (IOException e) {
			e.printStackTrace();
			BetterShop
					.sendMessage(player, "Pricelist load error. See console.");
			return false;
		}
		BetterShop.sendMessage(player, "PriceList loaded.");
		return true;
	}

	public boolean help(CommandSender player) {
		// TODO Implement help method
		BetterShop.sendMessage(player,
				"--------------- Better Shop Usage ---------------");
		BetterShop.sendMessage(player, "/" + commandPrefix + "shoplist <page>");
		BetterShop.sendMessage(player, "/" + commandPrefix
				+ "shopbuy [item] [amount]");
		BetterShop.sendMessage(player, "/" + commandPrefix
				+ "shopsell [item] [amount]");
		if (BetterShop.hasPermission(player, "Admin")) {
			BetterShop.sendMessage(player, "/" + commandPrefix
					+ "shopadd [item] [$buy] [$sell]");
			BetterShop.sendMessage(player, "/" + commandPrefix
					+ "shopremove [item]");
			BetterShop.sendMessage(player, "/" + commandPrefix
					+ "shopupdate [item] [$buy] [$sell]");
			BetterShop.sendMessage(player, "/" + commandPrefix + "shop load");
		}
		return true;
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

	private boolean anonymousCheck(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender
					.sendMessage("Cannot execute that command, I don't know who you are!");
			return true;
		} else {
			return false;
		}
	}

	private final static void sendMessage(CommandSender player, String s) {
		player.sendMessage(messagePrefix + s);
	}
}
