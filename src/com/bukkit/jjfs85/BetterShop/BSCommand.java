package com.bukkit.jjfs85.BetterShop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

import com.nijikokun.bukkit.iConomy.iConomy;

public class BSCommand {

	private final static BSPriceList PriceList = new BSPriceList();
	private final static HashMap<Integer, ItemStack> leftover = new HashMap<Integer, ItemStack>();
	private final static String commandPrefix = "";
	private final static Logger logger = Logger.getLogger("Minecraft");
	private static final String name = "BetterShop";
	private final static File pluginFolder = new File("plugins", name);

	public BSCommand() throws Exception {
		// load the pricelist.
		PriceList.load(pluginFolder, "PriceList.yml");
	}

	public boolean add(CommandSender player, String[] s) {
		if (s.length != 3) {
			return false;
		}
		MaterialData mat = new MaterialData(0);
		try {
			mat = itemDb.get(s[0]);
		} catch (Exception e1) {
			BSutils.sendMessage(player, "What's " + s[0] + "?");
			return false;
		}
		if (!BSutils.hasPermission(player, "BetterShop.admin.add")) {
			BSutils.sendMessage(player,
					"OI! You don't have permission to do that!");
			return true;
		}
		try {
			PriceList.setPrice(s[0], s[1], s[2]);
		} catch (Exception e) {
			BSutils.sendMessage(player, "Something wasn't right there.");
			return false;
		}
		try {
			BSutils.sendMessage(player, String.format(
					"[%s] added at the prices:    Buy: %d Sell: %d", itemDb
							.getName(mat.getItemTypeId(), mat.getData()),
					PriceList.getBuyPrice(mat.getItemTypeId()), PriceList
							.getSellPrice(mat.getItemTypeId())));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean buy(CommandSender player, String[] s) {
		MaterialData item = new MaterialData(0);
		int price = 0;
		int amtleft = 0;
		int amtbought = 1;
		int cost = 0;
		if (!BSutils.hasPermission(player, "BetterShop.user.buy")) {
			BSutils.sendMessage(player,
					"OI! You don't have permission to do that!");
			return true;
		}
		if ((s.length > 2) || (s.length == 0)) {
			BSutils.sendMessage(player, "What?");
			return false;
		} else if (BSutils.anonymousCheck(player)) {
			return true;
		} else {
			try {
				item = itemDb.get(s[0]);
			} catch (Exception e2) {
				BSutils.sendMessage(player, "What is " + s[0] + "?");
			}
			try {
				price = PriceList.getBuyPrice(item.getItemTypeId());
			} catch (Exception e1) {
				BSutils.sendMessage(player, "That's not for sale!");
				return true;
			}
			if (s.length == 2)
				try {
					amtbought = Integer.parseInt(s[1]);
				} catch (Exception e) {
					return false;
				}
			if (amtbought < 0) {
				BSutils.sendMessage(player, "...Nice try.");
				return true;
			}
			cost = amtbought * price;
			try {
				if (BSutils.debit(player, cost)) {
					BSutils.sendMessage(player, "For "
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
						BSutils.sendMessage(player,
								"You didn't have enough space for all that.");
						BSutils.sendMessage(player, "I'm refundng " + cost
								+ " " + iConomy.currency
								+ " for what you couldn't hold.");
						BSutils.credit(player, cost);
					}
					return true;
				} else {
					BSutils.sendMessage(player, "Not enough money.");
					return true;
				}
			} catch (Exception e) {
				return false;
			}
		}
	}

	public boolean check(CommandSender player, String[] s) {
		MaterialData mat;
		if (!BSutils.hasPermission(player, "BetterShop.user.check")) {
			BSutils.sendMessage(player,
					"OI! You don't have permission to do that!");
			return true;
		}
		if (s.length != 1) {
			return false;
		}
		try {
			mat = itemDb.get(s[0]);
		} catch (Exception e) {
			BSutils.sendMessage(player, "What is " + s[0] + "?");
			return true;
		}
		try {
			BSutils.sendMessage(player, String.format("[%s] Buy: %d Sell: %d",
					itemDb.getName(mat.getItemTypeId(), mat.getData()),
					PriceList.getBuyPrice(mat.getItemTypeId()), PriceList
							.getSellPrice(mat.getItemTypeId())));
		} catch (Exception e) {
			BSutils.sendMessage(player, "That's not on the market.");
			return true;
		}
		return true;
	}

	public boolean help(CommandSender player) {
		if (!BSutils.hasPermission(player, "BetterShop.user.help")) {
			BSutils.sendMessage(player,
					"OI! You don't have permission to do that!");
			return true;
		}
		BSutils.sendMessage(player, "--------- Better Shop Usage --------");
		BSutils.sendMessage(player, "/" + commandPrefix
				+ "shoplist <page> - List shop prices");
		BSutils.sendMessage(player, "/" + commandPrefix
				+ "shopbuy [item] <amount> - Buy items");
		BSutils.sendMessage(player, "/" + commandPrefix
				+ "shopsell [item] <amount> - Sell items");
		BSutils.sendMessage(player, "/" + commandPrefix
				+ "shopcheck [item] - Check prices of item");
		if (BSutils.hasPermission(player, "BetterShop.admin")) {
			BSutils.sendMessage(player, "**-------- Admin commands --------**");
			BSutils
					.sendMessage(
							player,
							"/"
									+ commandPrefix
									+ "shopadd [item] [$buy] [$sell] - Add an item to the shop");
			BSutils.sendMessage(player, "/" + commandPrefix
					+ "shopremove [item] - Remove an item from the shop");
			BSutils.sendMessage(player, "/" + commandPrefix
					+ "shopload - Reload the PriceList.yml file");
			BSutils.sendMessage(player, "----------------------------------");
		}
		return true;
	}

	public boolean list(CommandSender player, String[] s) {
		int pagesize = 9;
		int page = 0;
		if (!BSutils.hasPermission(player, "BetterShop.user.list")) {
			BSutils.sendMessage(player,
					"OI! You don't have permission to do that!");
			return true;
		}
		try {
			page = (s.length == 0) ? 1 : Integer.parseInt(s[0]);
		} catch (Exception e) {
			BSutils.sendMessage(player, "That's not a page number, idiot.");
			return false;
		}
		int pages = (int) Math.ceil((double) PriceList.NameMap.size()
				/ pagesize);
		if ((s.length != 0) && (s.length != 1)) {
			return false;
		} else if (page > pages) {
			BSutils.sendMessage(player, "There is no page " + page + ".");
			return true;
		} else {
			int linenum = 1;
			int i = 1;
			BSutils.sendMessage(player, String.format(
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
							BSutils.sendMessage(player, "["
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
			BSutils.sendMessage(player, "-----------------------------");
			return true;
		}
	}

	public boolean load(CommandSender player) {
		if (!BSutils.hasPermission(player, "BetterShop.admin.load")) {
			BSutils.sendMessage(player,
					"OI! You don't have permission to do that!");
			return true;
		}
		try {
			PriceList.load(pluginFolder, "PriceList.yml");
		} catch (IOException e) {
			e.printStackTrace();
			BSutils.sendMessage(player, "Pricelist load error. See console.");
			return true;
		}
		BSutils.sendMessage(player, "PriceList loaded.");
		return true;
	}

	public boolean remove(CommandSender player, String[] s) {
		if ((!BSutils.hasPermission(player, "BetterShop.admin.remove"))) {
			BSutils.sendMessage(player,
					"OI! You don't have permission to do that!");
			return true;
		}
		if (s.length != 1) {
			return false;
		} else {
			try {
				PriceList.remove(s[0]);
				BSutils.sendMessage(player, s[0]
						+ " has been removed from the shop.");
			} catch (Exception e) {
				BSutils.sendMessage(player, "You done goofed. Check the name.");
			}
			return true;
		}
	}

	public boolean sell(CommandSender player, String[] s) {
		int amtSold = 1;
		int price = 0;
		MaterialData item = new MaterialData(0);
		if (!BSutils.hasPermission(player, "BetterShop.user.sell")) {
			BSutils.sendMessage(player,
					"OI! You don't have permission to do that!");
			return true;
		}
		if ((s.length > 2) || (s.length == 0)) {
			return false;
		} else if (BSutils.anonymousCheck(player)) {
			return true;
		} else {
			try {
				item = itemDb.get(s[0]);
			} catch (Exception e1) {
				BSutils.sendMessage(player, "What's " + s[0] + "?");
				return false;
			}
			try {
				price = PriceList.getSellPrice(item.getItemTypeId());
				if (price < 1)
					throw new Exception();
			} catch (Exception e1) {
				BSutils.sendMessage(player, "We don't want that!");
				return true;
			}
			ItemStack itemsToSell = item.toItemStack();
			if (s.length == 2)
				try {
					amtSold = Integer.parseInt(s[1]);
				} catch (Exception e) {
					BSutils.sendMessage(player, s[1]
							+ " is definitely not a number.");
				}
			if (amtSold < 0) {
				BSutils.sendMessage(player,
						"Why would you want to buy at the selling price!?");
				return true;
			}
			itemsToSell.setAmount(amtSold);
			PlayerInventory inv = ((Player) player).getInventory();
			leftover.clear();
			leftover.putAll(inv.removeItem(itemsToSell));
			if (leftover.size() > 0) {
				amtSold = amtSold - leftover.get(0).getAmount();
				BSutils.sendMessage(player, "You only had " + amtSold + ".");
			}
			try {
				BSutils.credit(player, amtSold * price);
				BSutils.sendMessage(player, "You sold " + amtSold + " "
						+ itemDb.getName(item.getItemTypeId(), item.getData())
						+ " at " + price + " each for a total of " + amtSold
						* price);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
	}

}
