/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: commands & methods for selling
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.jascotty2.bettershop.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import me.jascotty2.bettershop.BSEcon;
import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import me.jascotty2.bettershop.shop.Shop;
import me.jascotty2.bettershop.utils.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.lib.bukkit.commands.Command;
import me.jascotty2.lib.bukkit.commands.WrappedCommandException;
import me.jascotty2.lib.bukkit.inventory.ItemStackManip;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.shop.UserTransaction;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.util.ArrayManip;
import me.jascotty2.lib.util.Str;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author jacob
 */
public class SellCommands {

	static Map<String, String> usersellHistory = new HashMap<String, String>();

	@Command(commands = {"shopsell", "ssell", "sell"},
	aliases = {"sell", "s"},
	desc = "Sell an item for the price in the shop",
	usage = "<item> [amount]",
	min = 1,
	max = 2,
	permissions = {"BetterShop.user.sell"})
	public static void sell(CommandSender player, String[] s) throws WrappedCommandException {
		if (BSutils.anonymousCheck(player)) {
			return;
		} // "sell all", "sell all [item]" moved to own method ("sell [item] all" kept here)
		else if (s.length >= 1 && s[0].equalsIgnoreCase("all")) {
			String[] newArgs = new String[s.length - 1];
			System.arraycopy(s, 1, newArgs, 0, newArgs.length);
			sellall(player, newArgs);
			return;
		} else if (s.length >= 2 && s[s.length - 1].equalsIgnoreCase("all")) {
			String[] newArgs = new String[s.length - 1];
			System.arraycopy(s, 0, newArgs, 0, newArgs.length);
			sellall(player, newArgs);
			return;
		} else if (s.length == 2 && CheckInput.IsInt(s[0]) && !CheckInput.IsInt(s[1])) {
			// "sell ## item"
			// swap two indicies
			String t = s[0];
			s[0] = s[1];
			s[1] = t;
		} else if (s.length == 0 || s.length > 2) {
			return;
		}// initial check complete: set as last action
		usersellHistory.put(((Player) player).getDisplayName(), "shopsell " + Str.concatStr(s, " "));
		// expected syntax: item [amount]

		JItem toSell = JItemDB.findItem(s[0]);
		if (toSell == null) {
			BSutils.sendMessage(player, BetterShop.getConfig().getString("unkitem").
					replace("<item>", s[0]));
			return;
		} else if (toSell.ID() == 0) {
			BSutils.sendMessage(player, toSell.coloredName() + " Cannot be Sold");//, toSell.coloredName());
			return;
		} else if (toSell.isKit()) {
			BSutils.sendMessage(player, "Kits cannot be sold");
			return;
		} else if (toSell.isEntity()) {
			BSutils.sendMessage(player, "Entities cannot be sold");
			return;
		}
		if (!CheckInput.IsInt(s[1])) {
			BSutils.sendMessage(player, s[1] + " is definitely not a number.");
		}
		int amtSell = CheckInput.GetInt(s[1], 0);

		if (amtSell <= 0) {
			BSutils.sendMessage(player, BetterShop.getConfig().getString("nicetry"));
			return;
		}
		try {
			sellItems((Player) player, false, toSell, amtSell);
		} catch (Exception ex) {
			throw new WrappedCommandException(ex);
		}
	}

	@Command(commands = {"shopsellstack", "sellstack", "ssellstack", "sells", "ssells"},
	aliases = {"sellstack"},
	desc = "Sell a stack of an item to the shop",
	usage = "<item> [amount]",
	min = 1,
	permissions = {"BetterShop.user.sell"})
	public static boolean sellstack(CommandSender player, String[] s) throws WrappedCommandException {
		if (BSutils.anonymousCheck(player)) {
			return true;
		}
		if (s.length == 2 && CheckInput.IsInt(s[1])) {

			JItem toSell = JItemDB.findItem(s[0]);
			if (toSell == null) {
				BSutils.sendMessage(player, BetterShop.getConfig().getString("unkitem").
						replace("<item>", s[0]));
				return true;
			} else if (!BetterShop.getConfig().allowbuyillegal && !toSell.IsLegal()
					&& !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false)) {
				BSutils.sendMessage(player, BetterShop.getConfig().getString("illegalbuy").
						replace("<item>", toSell.coloredName()));
				return true;
			}
			// sell max. stackable
			sell(player, new String[]{toSell.IdDatStr(),
						String.valueOf((BetterShop.getConfig().usemaxstack ? toSell.getMaxStackSize() : 64) * CheckInput.GetInt(s[1], 1))});
		} else {
			for (String is : s) {
				JItem toSell = JItemDB.findItem(is);
				if (toSell == null) {
					BSutils.sendMessage(player, BetterShop.getConfig().getString("unkitem").
							replace("<item>", is));
					return true;
				} else if (!BetterShop.getConfig().allowbuyillegal && !toSell.IsLegal()
						&& !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false)) {
					BSutils.sendMessage(player, BetterShop.getConfig().getString("illegalbuy").
							replace("<item>", toSell.coloredName()));
					return true;
				}
				// sell max. stackable
				sell(player, new String[]{toSell.IdDatStr(), String.valueOf(
							BetterShop.getConfig().usemaxstack ? toSell.getMaxStackSize() : 64)});
			}
		}// overwrite history that selll wrote
		usersellHistory.put(((Player) player).getDisplayName(), "shopsellstack " + Str.concatStr(s));
		return true;
	}

	@Command(commands = {"shopsellall", "sellall"},
	aliases = {"sellall", "sall"},
	desc = "Sell a stack of an item to the shop",
	usage = "[item] [amount]",
	permissions = {"BetterShop.user.sell"})
	public static void sellall(CommandSender player, String[] s) throws WrappedCommandException {
		if (BSutils.anonymousCheck(player)) {
			return;
		}
		JItem toSell[] = null;
		boolean onlyInv = false;
		if (s != null) {
			if (s.length > 0) {
				// expected syntax: [inv] [item [item [item [...]]]]
				int st = 0;
				if (s[0].equalsIgnoreCase("inv")) {
					onlyInv = true;
					st = 1;
				}
				toSell = new JItem[s.length - st];
				for (int i = st; i < s.length; ++i) {
					toSell[i - st] = JItemDB.findItem(s[i]);
					if (toSell[i - st] == null) {
						JItem cts[] = JItemDB.getItemsByCategory(s[i]);
						if (cts != null && cts.length > 0) {
							toSell = ArrayManip.arrayConcat(toSell, cts);
							//--i;
						} else {
							BSutils.sendMessage(player, String.format(
									BetterShop.getConfig().getString("unkitem").
									replace("<item>", "%1$s"), s[i]));
							toSell[i - st] = null;
						}
					} else if (toSell[i - st].ID() == 0) {
						BSutils.sendMessage(player, toSell[i - st].coloredName() + " Cannot be Sold"); // toSell[i - st].coloredName()
						toSell[i - st] = null;
					} else if (toSell[i - st].isKit()) {
						BSutils.sendMessage(player, "Kits cannot be sold");
						toSell[i - st] = null;
					} else if (toSell[i - st].isEntity()) {
						BSutils.sendMessage(player, "Entities cannot be sold");
						toSell[i - st] = null;
					}
				}
			} // "[All Sellable]"
		}// initial check complete: set as last action
		usersellHistory.put(((Player) player).getDisplayName(), "shopsellall " + Str.concatStr(s));
		try {
			// now sell the items
			sellItems((Player) player, onlyInv, getCanSell((Player) player, onlyInv, toSell));
		} catch (Exception ex) {
			throw new WrappedCommandException(ex);
		}
	}

	@Command(commands = {"shopsellagain", "ssellagain", "sellagain", "ssa"},
	aliases = {"sellagain", "s!", "sell!"},
	desc = "Repeat the last selling action the player did",
	usage = "",
	permissions = {"BetterShop.user.sell"})
	public static void sellagain(CommandSender sender, String[] s) {
		if (BSutils.anonymousCheck(sender)) {
			return;
		}
		String action = usersellHistory.get(((Player) sender).getDisplayName());
		if (action == null) {
			BSutils.sendMessage(sender, "You have no recent sell history");
			return;
		}
		((Player) sender).performCommand(action);
	}

	public static List<ItemStack> getCanSell(Player player, boolean onlyInv, JItem[] toSell) throws SQLException, Exception {
		List<ItemStack> items = ItemStackManip.itemStackSummary(
				player.getInventory().getContents(), onlyInv ? 9 : 0);
		Shop shop = BetterShop.getShop(player);

		ArrayList<String> notwant = new ArrayList<String>();
		if (toSell.length == 0) {
			// null is a wildcard for all
			toSell = null;
		} else if (toSell != null) {
			// remove unwanted items
			for (int i = 0; i < toSell.length; ++i) {
				if (toSell[i] != null
						&& (!shop.pricelist.isForSale(toSell[i])
						|| (toSell[i].IsTool() && !BetterShop.getConfig().buybacktools))) {
					notwant.add(toSell[i].coloredName());
					toSell[i] = null;
				}
			}
		}

		// remove unsellable items
		if (toSell == null) {
			for (int i = 0; i < items.size(); ++i) {
				if (!shop.pricelist.isForSale(items.get(i))) {
					items.remove(i--);
				} else if (!BetterShop.getConfig().buybacktools
						&& items.get(i).getDurability() > 0) {
					// if not buying used tools, check if that is what this is
					JItem t = JItemDB.GetItem(items.get(i));
					if (t.IsTool()) {
						items.remove(i--);
					}
				}
			}
		} else {
			for (int i = 0; i < items.size(); ++i) {
				// if not trying to sell, or item is not for sale, remove
				if (!contains(toSell, items.get(i))
						|| !shop.pricelist.isForSale(items.get(i))) {
					items.remove(i--);
				}
			}
		}

		// now do a scan for max. sellable
		boolean overstock = false;
		if (shop.config.useStock() && shop.config.noOverStock) {
			for (int i = 0; i < items.size(); ++i) {
				// check if avaliable stock
				JItem it = JItemDB.GetItem(items.get(i));
				long free = shop.stock.freeStockRemaining(it);
				if (free == 0) {
					BSutils.sendMessage(player, BetterShop.getConfig().getString("maxstock").
							replace("<item>", it.coloredName()));
					items.remove(i--);
					overstock = true;
				} else if (free > 0 && items.get(i).getAmount() > free) {
					BSutils.sendMessage(player, BetterShop.getConfig().getString("highstock").
							replace("<item>", it.coloredName()).
							replace("<amt>", String.valueOf(free)));
					items.get(i).setAmount((int) free);
				}
			}
		}

		if (items.isEmpty() && !overstock) {
			BSutils.sendMessage(player, "You Don't have any "
					+ (toSell == null ? "Sellable Items" :
						(toSell.length == 1 ? toSell[0].coloredName() : "of those items")));
		}
		if (notwant.size() > 0) {
			BSutils.sendMessage(player, BetterShop.getConfig().getString("donotwant").
					replace("<item>", notwant.size() > 1
					? "(" + Str.concatStr(notwant.toArray(new String[0]), ", ") + ")"
					: notwant.get(0)));
		}
		return items;
	}

	public static double sellItems(Player player, boolean onlyInv, JItem item, int amt) throws SQLException, Exception {
		List<ItemStack> sellable = getCanSell((Player) player, onlyInv, new JItem[]{item});
		// should be at least one item
		if (!sellable.isEmpty()) {
			if (amt > 0) {
				int amtHas = 0;
				for (ItemStack i : sellable) {
					amtHas += i.getAmount();
					if (i.getAmount() > amt) {
						i.setAmount(amt);
						amt = 0;
					} else {
						amt -= i.getAmount();
					}
				}
				if (amt > 0) {
					BSutils.sendMessage(player,
							BetterShop.getConfig().getString("donthave").
							replace("<hasamt>", String.valueOf(amtHas)).
							replace("<amt>", String.valueOf(amt + amtHas)).
							replace("<item>", item.coloredName()));
				}
			}
			return sellItems(player, onlyInv, sellable);
		}
		return 0;
	}

	public static double sellItems(Player player, boolean onlyInv, List<ItemStack> sellable) throws SQLException, Exception {

		if (sellable == null) {
			sellable = getCanSell((Player) player, onlyInv, null);
		}
		if (sellable.isEmpty()) {
			return 0;
		}

		Shop shop = BetterShop.getShop(player);

		// list of transactions made
		List<UserTransaction> transactions = new LinkedList<UserTransaction>();

		ItemStack[] inv = player.getInventory().getContents();

		double credit = 0; // total to pay player
		int amtSold = 0; //total items sold

		// name of item(s) sold
		String itemN = "";

		for (ItemStack it : sellable) {
			int amtLeft = it.getAmount();
			JItem selling = JItemDB.GetItem(it);
			double price = 0;
			if (selling == null) {
				BetterShopLogger.Log(Level.SEVERE, "Unexpected unknown inventory item: " + it, false);
				continue;
			}

			for (int i = (onlyInv ? 9 : 0); i < inv.length && amtLeft > 0; ++i) {
				if (selling.equals(inv[i])) {
					int amt = inv[i].getAmount();
					if (amtLeft < amt) {
						inv[i].setAmount(amt - amtLeft);
						amt = amtLeft;
					} else {
						inv[i] = null;
					}

					price += selling.IsTool() ? (shop.pricelist.itemSellPrice(player, selling, amt)
							* (1 - ((double) inv[i].getDurability() / selling.MaxDamage())))
							: shop.pricelist.itemSellPrice(player, selling, amt);

					amtSold += amt;
					amtLeft -= amt;
				}
			}
			if (amtLeft > 0) {
				BetterShopLogger.Severe("Not all Items Sold: " + amtLeft + " " + selling.Name() + " left..", false);
			}
			int numSold = it.getAmount() - amtLeft;
			itemN += selling.coloredName() + ", ";
			if (shop.config.useStock()) {
				shop.stock.changeItemAmount(selling, numSold);
			}
			transactions.add(new UserTransaction(selling, true, numSold,
					numSold > 0 ? price / numSold : price, player.getDisplayName()));
			credit += price;
		}

		player.getInventory().setContents(inv);
		BSEcon.credit(player, credit);

		if (itemN.length() > 1) {
			itemN = itemN.substring(0, itemN.length() - 2);
			if (itemN.contains(",")) {
				itemN = "(" + itemN + ")";
			}
		}

		BSutils.sendFormttedMessage(player, "sellmsg", itemN, amtSold, credit);

		// last step: log transactions
		if (BetterShop.getConfig().logUserTransactions) {
			try {
				for (UserTransaction t : transactions) {
					shop.transactions.addRecord(t);
				}
			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
			}
		}

		return credit;
	}

	private static boolean contains(JItem[] items, ItemStack it) {
		if (items != null) {
			for (JItem i : items) {
				if (i != null && i.equals(it)) {
					return true;
				}
			}
		}
		return false;
	}
} // end class SellCommands

