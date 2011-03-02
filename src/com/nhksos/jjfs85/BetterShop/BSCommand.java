package com.nhksos.jjfs85.BetterShop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

import com.nijiko.coelho.iConomy.iConomy;

public class BSCommand {

	private final static BSPriceList PriceList = new BSPriceList();
	private final static HashMap<Integer, ItemStack> leftover = new HashMap<Integer, ItemStack>();
	private final static String commandPrefix = "";
	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger("Minecraft");
	private static final String name = "BetterShop";
	private final static File pluginFolder = new File("plugins", name);
	public static String currency = "";

	public BSCommand() throws Exception {
		// load the pricelist.
		PriceList.load(pluginFolder, "PriceList.yml");
	}

	public boolean add(CommandSender player, String[] s) 
	{
		if (s.length != 3) {
			return false;
		}
		MaterialData mat = new MaterialData(0);
		try {
			mat = itemDb.get(s[0]);
		} catch (Exception e1) {
			BSutils.sendMessage(player, String.format(BetterShop.configfile
					.getString("unkitem").replace("<item>", "%s"), s[0]));
			return false;
		}
		if (!BSutils.hasPermission(player, "BetterShop.admin.add", true)) {
			return true;
		}
		try {
			PriceList.setPrice(s[0], s[1], s[2]);
		} catch (Exception e) {
			BSutils.sendMessage(player, BetterShop.configfile
					.getString("paramerror"));
			e.printStackTrace();
			return false;
		}
		try {
			BSutils.sendMessage(player, String.format(BetterShop.configfile
					.getString("addmsg").replace("<item>", "%1$s").replace(
							"<buyprice>", "%2$01.2f").replace("<sellprice>",
							"%3$01.2f").replace("<curr>", "%4$s"), itemDb
					.getName(mat.getItemTypeId(), mat.getData()), PriceList
					.getBuyPrice(s[0]), PriceList.getSellPrice(s[0]),
					currency));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean buy(CommandSender player, String[] s) {
		MaterialData item = new MaterialData(0);
		double price = 0;
		int amtleft = 0;
		int amtbought = 1;
		double cost = 0;
		if (!BSutils.hasPermission(player, "BetterShop.user.buy", true)) {
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
				BSutils.sendMessage(player, String.format(BetterShop.configfile
						.getString("unkitem").replace("<item>", "%s"), s[0]));
				return true;
			}
			try {
				price = PriceList.getBuyPrice(item.getItemTypeId());
				if (price < 0)
					new Exception();
			} catch (Exception e1) {
				try {
					BSutils.sendMessage(player, String.format(
							BetterShop.configfile.getString("notforsale")
									.replace("<item>", "%s"), itemDb.getName(
									item.getItemTypeId(), item.getData())));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
			if (s.length == 2)
				try {
					amtbought = Integer.parseInt(s[1]);
				} catch (Exception e) {
					return false;
				}
			if (amtbought <= 0) {
				BSutils.sendMessage(player, BetterShop.configfile
						.getString("nicetry"));
				return true;
			}
			cost = amtbought * price;
			try {
				if (BSutils.debit(player, cost)) {
					BSutils.sendMessage(player, String.format(
							BetterShop.configfile.getString("buymsg").replace(
									"<item>", "%1$s").replace("<amt>", "%2$d")
									.replace("<priceper>", "%3$01.2f").replace(
											"<total>", "%4$01.2f").replace(
											"<curr>", "%5$s"), itemDb.getName(
									item.getItemTypeId(), item.getData()),
							amtbought, price, cost, currency));
					leftover.clear();
					ItemStack itemS = new ItemStack(item.getItemTypeId(),
							amtbought, (short) 0, item.getData());
					leftover.putAll(((Player) player).getInventory().addItem(
							itemS));
					amtleft = (leftover.size() == 0) ? 0 : (leftover.get(0))
							.getAmount();
					if (amtleft > 0) {
						cost = amtleft * price;
						BSutils.sendMessage(player, String.format(
								BetterShop.configfile.getString("outofroom")
										.replace("<item>", "%1$s").replace(
												"<leftover>", "%2$d").replace(
												"<refund>", "%3$01.2f")
										.replace("<curr>", "%5$s").replace(
												"<priceper>", "%4$d"), itemDb
										.getName(item.getItemTypeId(), item
												.getData()), amtleft, cost,
								price, currency));
						BSutils.credit(player, cost);
					}
					return true;
				} else {
					BSutils.sendMessage(player, String.format(
							BetterShop.configfile.getString("insuffunds")
									.replace("<item>", "%1$s").replace("<amt>",
											"%2$d").replace("<total>",
											"%3$01.2f").replace("<curr>",
											"%5$s").replace("<priceper>",
											"%4$01.2f"), itemDb.getName(item
									.getItemTypeId(), item.getData()),
							amtbought, cost, price, currency));
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	public boolean check(CommandSender player, String[] s) {
		MaterialData mat;
		double i = 0;
		if (!BSutils.hasPermission(player, "BetterShop.user.check", true)) {
			return true;
		}
		if (s.length != 1) {
			return false;
		}
		try {
			mat = itemDb.get(s[0]);
		} catch (Exception e) {
			BSutils.sendMessage(player, String.format(BetterShop.configfile
					.getString("unkitem").replace("<item>", "%s"), s[0]));
			return true;
		}
		try {
			i = mat.getItemTypeId() + ((double) mat.getData() / 100);
			BSutils.sendMessage(player, String.format(BetterShop.configfile
					.getString("pricecheck").replace("<buyprice>", "%1$01.2f")
					.replace("<sellprice>", "%2$01.2f").replace("<item>",
							"%3$s").replace("<curr>", "%4$s"), PriceList
					.getBuyPrice(i), PriceList.getSellPrice(i), itemDb
					.getName(i), currency));
		} catch (Exception e) {
			try {
				BSutils.sendMessage(player, String.format(BetterShop.configfile
						.getString("nolisting").replace("<item>", "%s"), itemDb
						.getName(mat.getItemTypeId(), mat.getData())));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return true;
		}
		return true;
	}

	public boolean help(CommandSender player) {
		if (!BSutils.hasPermission(player, "BetterShop.user.help", true)) {
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
		if (BSutils.hasPermission(player, "BetterShop.admin", false)) {
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

		if (!BSutils.hasPermission(player, "BetterShop.user.list", true)) {
			return true;
		}
		try {
			page = (s.length == 0) ? 1 : Integer.parseInt(s[0]);
		} catch (Exception e) {
			BSutils.sendMessage(player, "That's not a page number.");
			return false;
		}
		int pages = (int) Math.ceil((double) PriceList.NameMap.size()
				/ pagesize);
		String listhead = BetterShop.configfile.getString("listhead").replace(
				"<page>", String.valueOf(page)).replace("<pages>",
				String.valueOf(pages));
		if ((s.length != 0) && (s.length != 1)) {
			return false;
		} else if (page > pages) {
			BSutils.sendMessage(player, "There is no page " + page + ".");
			return true;
		} else {
			int linenum = 1;
			Iterator<Double> iter = PriceList.ItemMap.iterator();
			BSutils.sendMessage(player, String.format(listhead, page, pages));
			while ((linenum < (page - 1) * pagesize) && iter.hasNext()) {
				iter.next();
				linenum++;
			}
			while ((linenum < page * pagesize) && (iter.hasNext())) {
				Double i = iter.next();
				try {
					BSutils.sendMessage(player, String.format(
							"[%1$s] Buy: %2$01.2f Sell: %3$01.2f", itemDb
									.getName(i), PriceList.getBuyPrice(i),
							PriceList.getSellPrice(i)));
				} catch (Exception e) {
					e.printStackTrace();
				}
				linenum++;
			}
			BSutils.sendMessage(player, BetterShop.configfile
					.getString("listtail"));
			return true;
		}
	}

	public boolean load(CommandSender player) {
		if (!BSutils.hasPermission(player, "BetterShop.admin.load", true)) {
			return true;
		}
		try {
			PriceList.load(pluginFolder, "PriceList.yml");
		} catch (IOException e) {
			e.printStackTrace();
			BSutils.sendMessage(player, "Pricelist load error. See console.");
			return true;
		}
		BSutils.sendMessage(player, "PriceList.yml loaded.");
		BetterShop.configfile.load();
		BSutils.sendMessage(player, "Config.yml loaded.");
		return true;
	}

	public boolean remove(CommandSender player, String[] s) {
		if ((!BSutils.hasPermission(player, "BetterShop.admin.remove", true))) {
			return true;
		}
		if (s.length != 1) {
			return false;
		} else {
			try {
				PriceList.remove(s[0]);
				BSutils.sendMessage(player, String
						.format(BetterShop.configfile.getString("removemsg")
								.replace("<item>", "%1$s"), s[0]));
			} catch (Exception e) {
				BSutils.sendMessage(player, String.format(BetterShop.configfile
						.getString("unkitem").replace("<item>", "%s"), s[0]));
			}
			return true;
		}
	}

	public boolean sell(CommandSender player, String[] s) {
		int amtSold = 1;
		double price = 0;
		MaterialData item = new MaterialData(0);
		if (!BSutils.hasPermission(player, "BetterShop.user.sell", true)) {
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
				BSutils.sendMessage(player, String.format(BetterShop.configfile
						.getString("unkitem").replace("<item>", "%s"), s[0]));
				return false;
			}
			String itemname = null;
			try {
				itemname = itemDb.getName(item.getItemTypeId(), item.getData());
			} catch (Exception e2) {
				e2.printStackTrace();
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
				BSutils.sendMessage(player, BetterShop.configfile
						.getString("nicetry"));
				return true;
			}
			
			try {
				price = PriceList.getSellPrice(itemname);
				if ((price * amtSold) < 1)
					throw new Exception();
			} catch (Exception e1) {
				BSutils.sendMessage(player, String.format(BetterShop.configfile
						.getString("donotwant").replace("<item>", "%1$s"),
						itemname));
				return true;
			}
			
			itemsToSell.setAmount(amtSold);
			PlayerInventory inv = ((Player) player).getInventory();
			int amtHas = amtSold;
			leftover.clear();
			leftover.putAll(inv.removeItem(itemsToSell));
			if (leftover.size() > 0) {
				amtHas = amtSold - leftover.get(0).getAmount();
				BSutils.sendMessage(player, String.format(BetterShop.configfile
						.getString("donthave").replace("<hasamt>", "%1$d")
						.replace("<amt>", "%2$d"), amtHas, amtSold));
			}
			double total = amtHas * price;
			try {
				BSutils.credit(player, total);
				BSutils.sendMessage(player, String.format(BetterShop.configfile
						.getString("sellmsg").replace("<item>", "%1$s")
						.replace("<amt>", "%2$d").replace("<priceper>",
								"%3$01.2f").replace("<total>", "%4$01.2f")
						.replace("<curr>", "%5$s"), itemDb.getName(item
						.getItemTypeId(), item.getData()), amtHas, price,
						total, currency));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
	}

}
