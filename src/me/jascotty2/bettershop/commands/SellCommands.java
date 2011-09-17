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

import java.util.ArrayList;
import java.util.Arrays;
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
import me.jascotty2.lib.bukkit.item.ItemStockEntry;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.shop.UserTransaction;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.util.ArrayManip;
import me.jascotty2.lib.util.Str;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author jacob
 */
public class SellCommands {

	static Map<String, String> usersellHistory = new HashMap<String, String>();

	@Command(
	commands = {"shopsell", "ssell", "sell"},
	aliases = {"sell", "s"},
	desc = "Sell an item for the price in the shop",
	usage = "<item> [amount]",
	permissions = {"BetterShop.user.sell"})
	public static boolean sell(CommandSender player, String[] s) {
		if (BSutils.anonymousCheck(player)) {
			return true;
		} // "sell all", "sell all [item]" moved to own method ("sell [item] all" kept here)
		else if (s.length == 1 && s[0].equalsIgnoreCase("all")) {
			return sellall(player, null);
		} else if (s.length == 2) {
			if (s[0].equalsIgnoreCase("all")) {
				return sellall(player, new String[]{s[1]});
			} else if (s[1].equalsIgnoreCase("all")) {
				return sellall(player, new String[]{s[0]});
			} else if (CheckInput.IsInt(s[0]) && !CheckInput.IsInt(s[1])) {
				// swap two indicies
				String t = s[0];
				s[0] = s[1];
				s[1] = t;
			}
		} else if (s.length == 0 || s.length > 2) {
			return false;
		}// initial check complete: set as last action
		usersellHistory.put(((Player) player).getDisplayName(), "shopsell " + Str.concatStr(s, " "));
		// expected syntax: item [amount]

		JItem toSell = JItemDB.findItem(s[0]);
		if (toSell == null) {
			BSutils.sendMessage(player, BetterShop.getConfig().getString("unkitem").
					replace("<item>", s[0]));
			return false;
		} else if (toSell.ID() == 0) {
			BSutils.sendMessage(player, toSell.coloredName() + " Cannot be Sold");//, toSell.coloredName());
			return true;
		} else if (toSell.isKit()) {
			BSutils.sendMessage(player, "Kits cannot be sold");
			return true;
		} else if (toSell.isEntity()) {
			BSutils.sendMessage(player, "Entities cannot be sold");
			return true;
		}
		Shop shop = BetterShop.getShop(player);
		double price = Double.NEGATIVE_INFINITY;
		try {
			price = shop.pricelist.itemSellPrice((Player) player, toSell, 1);
		} catch (Exception ex) {
			BetterShopLogger.Log(Level.SEVERE, ex);
		}
		if (price < 0) {
			if (price == Double.NEGATIVE_INFINITY) {
				BSutils.sendMessage(player, "Error looking up price.. Attempting DB reload..");
				if (AdminCommands.load(null, null)) {
					// ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
					BSutils.sendMessage(player, "Success! Please try again.. ");
				} else {
					BSutils.sendMessage(player, ChatColor.RED + "Failed! Please let an OP know of this error");
				}
			} else {
				BSutils.sendMessage(player, BetterShop.getConfig().getString("donotwant").
						replace("<item>", toSell.coloredName()));
			}
			return true;
		}

		// go through inventory & find how much user has

		PlayerInventory inv = ((Player) player).getInventory();
		int amtSold = 1, amtHas = 0;

		for (ItemStack i : inv.getContents()) {
			if (toSell.equals(i)) {
				//System.out.println("found: " + i);
				if (!toSell.IsTool() || (toSell.IsTool()
						&& (i.getDurability() == 0 || BetterShop.getConfig().buybacktools))) {
					amtHas += i.getAmount();
				}
			}
		}

		if (amtHas <= 0) {
			BSutils.sendMessage(player, "You Don't have any " + (toSell == null ? "Sellable Items" : toSell.coloredName()));
			return true;
		}
		if (s.length == 2) {
			if (s[1].equalsIgnoreCase("all")) {
				amtSold = amtHas;
			} else if (CheckInput.IsInt(s[1])) {
				amtSold = CheckInput.GetInt(s[1], 1);
				if (amtSold > amtHas) {
					BSutils.sendMessage(player, 
							BetterShop.getConfig().getString("donthave").
							replace("<hasamt>", String.valueOf(amtHas)).
							replace("<amt>", String.valueOf(amtSold)).
							replace("<item>", toSell.coloredName()));
					amtSold = amtHas;
				} else if (amtSold <= 0) {
					BSutils.sendMessage(player, BetterShop.getConfig().getString("nicetry"));
					return true;
				}
			} else {
				BSutils.sendMessage(player, s[1] + " is definitely not a number.");
			}
		} // else  amtSold = 1


		// now check the remaining stock can sell back
		long avail = -1;
		if (shop.config.useStock()) {
			avail = shop.stock.freeStockRemaining(toSell);
			/*if (avail == -1) {
			BSutils.sendMessage(player, ChatColor.RED + "Failed to lookup an item stock listing");
			return true;
			} else */ if (avail == 0 && shop.config.noOverStock) {
				BSutils.sendMessage(player, BetterShop.getConfig().getString("maxstock").
						replace("<item>", toSell.coloredName()));
				return true;
			} else if (avail > 0 && amtSold > avail && shop.config.noOverStock) {
				BSutils.sendMessage(player, BetterShop.getConfig().getString("highstock").
						replace("<item>", toSell.coloredName()).
						replace("<amt>", String.valueOf(avail)));
				amtSold = (int) avail;
			}
		}

		double total = 0;//amtSold * price;

		int itemsLeft = amtSold;

		for (int i = 0; i <= 35; ++i) {
			ItemStack thisSlot = inv.getItem(i);
			//if (toSell.equals(thisSlot)) {
			if (toSell.equals(thisSlot) && (!toSell.IsTool() || (toSell.IsTool()
					&& (thisSlot.getDurability() == 0
					|| (thisSlot.getDurability() > 0 && BetterShop.getConfig().buybacktools))))) {
				int amt = thisSlot.getAmount(), tamt = amt;

				if (itemsLeft >= amt) {
					inv.setItem(i, null);
				} else {
					// remove only whats left to remove
					inv.setItem(i, toSell.toItemStack(amt - itemsLeft));
					amt = itemsLeft;
				}
				if (toSell.IsTool()) {
					//System.out.println("tool with " + thisSlot.getDurability() +"/"+ toSell.MaxDamage());
					total += (price * (1 - ((double) thisSlot.getDurability() / toSell.MaxDamage()))) * amt;
				} else {
					total += price * amt;
				}
				itemsLeft -= tamt;

				if (itemsLeft <= 0) {
					break;
				}
			}
		}

		BSEcon.credit((Player) player, total);

		if (shop.config.useStock()) {
			try {
				shop.stock.changeItemAmount(toSell, amtSold);
			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
			}
		}

		BSutils.sendFormttedMessage((Player) player, "sellmsg", toSell.coloredName(), amtSold, total);

		try {
			shop.transactions.addRecord(new UserTransaction(toSell, true, amtSold, total / amtSold, ((Player) player).getDisplayName()));
		} catch (Exception ex) {
			BetterShopLogger.Log(Level.SEVERE, ex);
		}

		return true;
	}

	@Command(
	commands = {"shopsellstack", "sellstack", "ssellstack", "sells", "ssells"},
	aliases = {"sellstack"},
	desc = "Sell a stack of an item to the shop",
	usage = "<item> [amount]",
	permissions = {"BetterShop.user.sell"})
	public static boolean sellstack(CommandSender player, String[] s) {
		if (BSutils.anonymousCheck(player)) {
			return true;
		} else if (s.length == 0) {
			BSutils.sendMessage(player, "What?");
			return false;
		}
		if (s.length == 2 && CheckInput.IsInt(s[1])) {

			JItem toSell = JItemDB.findItem(s[0]);
			if (toSell == null) {
				BSutils.sendMessage(player, String.format(BetterShop.getConfig().getString("unkitem").
						replace("<item>", "%1$s"), s[0]));
				return true;
			} else if (!BetterShop.getConfig().allowbuyillegal && !toSell.IsLegal()
					&& !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false)) {
				BSutils.sendMessage(player, String.format(BetterShop.getConfig().getString("illegalbuy").
						replace("<item>", "%1$s"), toSell.coloredName()));
				return true;
			}
			// sell max. stackable
			sell(player, new String[]{toSell.IdDatStr(),
						String.valueOf((BetterShop.getConfig().usemaxstack ? toSell.getMaxStackSize() : 64) * CheckInput.GetInt(s[1], 1))});
		} else {
			for (String is : s) {
				JItem toSell = JItemDB.findItem(is);
				if (toSell == null) {
					BSutils.sendMessage(player, String.format(BetterShop.getConfig().getString("unkitem").
							replace("<item>", "%1$s"), is));
					return true;
				} else if (!BetterShop.getConfig().allowbuyillegal && !toSell.IsLegal()
						&& !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false)) {
					BSutils.sendMessage(player, String.format(BetterShop.getConfig().getString("illegalbuy").
							replace("<item>", "%1$s"), toSell.coloredName()));
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

	@Command(
	commands = {"shopsellall", "sellall"},
	aliases = {"sellall", "sall"},
	desc = "Sell a stack of an item to the shop",
	usage = "<item> [amount]",
	permissions = {"BetterShop.user.sell"})
	public static boolean sellall(CommandSender player, String[] s) {
		if (BSutils.anonymousCheck(player)) {
			return true;
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
							return false;
						}
					} else if (toSell[i - st].ID() == 0) {
						BSutils.sendMessage(player, toSell[i - st].coloredName() + " Cannot be Sold"); // toSell[i - st].coloredName()
						return true; //toSell[i - st] = null; //
					} else if (toSell[i - st].isKit()) {
						BSutils.sendMessage(player, "Kits cannot be sold");
						return true;
					} else if (toSell[i - st].isEntity()) {
						BSutils.sendMessage(player, "Entities cannot be sold");
						return true;
					}
				}
			} // "[All Sellable]"
		}// initial check complete: set as last action
		usersellHistory.put(((Player) player).getDisplayName(), "shopsellall " + Str.concatStr(s));

		List<ItemStockEntry> playerInv = getCanSell((Player) player, onlyInv, toSell);
		if (playerInv == null || playerInv.isEmpty()) {
			return true;
		}

		// now scan through & remove the items
		sellItems((Player) player, onlyInv, playerInv);

		return true;
	}

	@Command(
	commands = {"shopsellagain", "ssellagain", "sellagain", "ssa"},
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
			BSutils.sendMessage(sender,
					"You have no recent sell history");
			return;
		}
//		else {
//			// trim command & put into args
//			String cm[] = action.split(" ");
//			String commandName = cm[0];
//			s = new String[cm.length - 1];
//			for (int i = 1; i < cm.length; ++i) {
//				s[i - 1] = cm[i];
//			}
//		}
		((Player) sender).performCommand(action);
	}

	public static List<ItemStockEntry> getCanSell(Player player, boolean onlyInv, JItem[] toSell) {
		List<ItemStockEntry> sellable = new ArrayList<ItemStockEntry>(),
				playerInv = BSutils.getTotalInventory(player, onlyInv, toSell);
		if (toSell != null && toSell.length == 1 && toSell[0] == null) {
			toSell = null;
		}
		Shop shop = BetterShop.getShop(player);
		//ItemStack[] its = player.getInventory().getContents();
		boolean overstock = false;
		ArrayList<String> notwant = new ArrayList<String>();
		try {
			for (int i = 0; i < playerInv.size(); ++i) {
				JItem check = JItemDB.GetItem(playerInv.get(i));
				if (!shop.pricelist.isForSale(check)
						|| (check.IsTool() && !BetterShop.getConfig().buybacktools && playerInv.get(i).itemSub > 0)) {
					if (toSell != null && toSell.length > 0) {
						notwant.add(check.coloredName());
					}
					playerInv.remove(i);
					--i;
				} else {
					if (shop.config.useStock()) {
						// check if avaliable stock
						long free = shop.stock.freeStockRemaining(check);
						if (free == 0 && shop.config.useStock()) {
							BSutils.sendMessage(player, BetterShop.getConfig().getString("maxstock").
									replace("<item>", check.coloredName()));
							playerInv.get(i).amount = 0;
							overstock = true;
						} else if (free > 0 && playerInv.get(i).amount > free && shop.config.noOverStock) {
							BSutils.sendMessage(player, BetterShop.getConfig().getString("highstock").
									replace("<item>", check.coloredName()).
									replace("<amt>", String.valueOf(free)));
							playerInv.get(i).amount = (int) free;
						}
					}
					//amtHas += playerInv.get(i).amount;
					int isel = sellable.indexOf(playerInv.get(i));
					if (isel >= 0) {
						sellable.get(isel).SetAmount(sellable.get(isel).amount + playerInv.get(i).amount);
					} else {
						sellable.add(new ItemStockEntry(playerInv.get(i)));
					}
				}
			}
		} catch (Exception ex) {
			BetterShopLogger.Log(Level.SEVERE, ex);
			BSutils.sendMessage(player, "Error looking up an item");// .. Attempting DB reload..
            /*if (load(null)) {
			// ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
			BSutils.sendMessage(player, "Success! Please try again.. ");
			} else {
			BSutils.sendMessage(player, "\u00A74Failed! Please let an OP know of this error");
			}*/
			return null;
		}
		if (notwant.size() > 0) {
			BSutils.sendMessage(player, String.format(
					BetterShop.getConfig().getString("donotwant").
					replace("<item>", "%1$s"), "(" + Str.concatStr(notwant.toArray(new String[0]), ", ") + ")"));
			if (notwant.size() == toSell.length) {
				return sellable;
			}
		}
		if (sellable.isEmpty() && !overstock) {
			BSutils.sendMessage(player, "You Don't have any "
					+ (toSell == null || toSell.length == 0 || (toSell.length == 1 && toSell[0] == null) ? "Sellable Items"
					: (toSell.length == 1 ? toSell[0].coloredName() : "of those items")));
		}
		return sellable;
	}

	public static double sellItems(Player player, boolean onlyInv, JItem item, int amt) {
		if (item == null || amt < 0) {
			List<ItemStockEntry> playerInv = getCanSell(player, onlyInv, item == null ? null : new JItem[]{item});
			if (playerInv == null || playerInv.isEmpty()) {
				return 0;
			}
			return sellItems(player, onlyInv, playerInv);
		} else {
			return sellItems(player, onlyInv, Arrays.asList(new ItemStockEntry(item, (long) amt)));
		}
	}

	public static double sellItems(Player player, boolean onlyInv, List<ItemStockEntry> items) {
		PlayerInventory inv = player.getInventory();
		ItemStack[] its = inv.getContents();

		Shop shop = BetterShop.getShop(player);

		List<ItemStockEntry> playerInv = BSutils.getTotalInventory(player, onlyInv, items);
		// make list of transactions made (or should make)
		List<UserTransaction> transactions = new LinkedList<UserTransaction>();
		try {
			for (ItemStockEntry ite : playerInv) {
				transactions.add(new UserTransaction(ite, true,
						shop.pricelist.getSellPrice(ite),
						player.getDisplayName()));
			}
		} catch (Exception ex) {
			BetterShopLogger.Log(Level.SEVERE, ex);
		}

		double credit = 0;
		int amtSold = 0;
		try {
			for (ItemStockEntry ite : items) {
				JItem toSell = JItemDB.GetItem(ite);
				if (toSell != null) {
					int amtLeft = (int) ite.amount;
					for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
						JItem it = JItemDB.GetItem(its[i]);
						if (it != null && it.equals(toSell)) {
							if (shop.pricelist.isForSale(it) && (!it.IsTool()
									|| (its[i].getDurability() == 0 || BetterShop.getConfig().buybacktools))) {
								int amt = its[i].getAmount();
								if (amtLeft < amt) {
									inv.setItem(i, it.toItemStack(amt - amtLeft));
									amt = amtLeft;
								} else {
									inv.setItem(i, null);
								}

								credit += it.IsTool() ? (shop.pricelist.itemSellPrice(player, it, amt)
										* (1 - ((double) its[i].getDurability() / it.MaxDamage())))
										: shop.pricelist.itemSellPrice(player, it, amt);

								amtSold += amt;
								amtLeft -= amt;
								if (amtLeft <= 0) {
									if (shop.config.useStock()) {
										shop.stock.changeItemAmount(it, ite.amount);
									}
									break;
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			BetterShopLogger.Log(Level.SEVERE, ex);
			BSutils.sendMessage(player, "Error looking up an item");
		}

		try {
			for (UserTransaction t : transactions) {
				shop.transactions.addRecord(t);
			}
		} catch (Exception ex) {
			BetterShopLogger.Log(Level.SEVERE, ex);
		}
		BSEcon.credit(player, credit);

		// name of item(s) sold
		String itemN = ""; // "(All Sellable)"
		if (items.size() == 1) {
			JItem i = JItemDB.GetItem(items.get(0));
			itemN = i != null ? i.coloredName() : items.get(0).name;
		} else {
			itemN = "(";
			for (ItemStockEntry ite : playerInv) {
				JItem i = JItemDB.GetItem(ite);
				itemN += i == null ? ite.name : i.coloredName() + " ";
			}
			itemN = itemN.trim().replace(" ", ", ") + ")";
		}

		BSutils.sendFormttedMessage(player, "sellmsg", itemN, amtSold, credit);

		return credit;
	}
} // end class SellCommands

