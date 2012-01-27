/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: commands & methods for buying
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

import me.jascotty2.bettershop.BSEcon;
import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import me.jascotty2.bettershop.utils.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.bettershop.shop.Shop;

import me.jascotty2.lib.bukkit.item.CreatureItem;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.item.JItems;
import me.jascotty2.lib.bukkit.shop.UserTransaction;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.util.Str;
import me.jascotty2.lib.bukkit.commands.Command;
import me.jascotty2.lib.bukkit.commands.WrappedCommandException;
import me.jascotty2.lib.bukkit.inventory.ItemStackManip;
import me.jascotty2.lib.bukkit.item.ItemStockEntry;
import me.jascotty2.lib.bukkit.item.Kit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author jacob
 */
public class BuyCommands {

	static Map<String, String> userbuyHistory = new HashMap<String, String>();

	@Command(commands = {"shopbuy", "sbuy", "buy"},
	aliases = {"buy", "b", "sb"},
	desc = "Buy an item from the shop",
	usage = "<item> [amount]",
	min = 1,
	max = 2,
	permissions = {"BetterShop.user.buy"})
	public static void buy(CommandSender player, String[] s) throws WrappedCommandException {
		if (BSutils.anonymousCheck(player)) {
			return;
		}
		if (s.length == 2 && (s[0].equalsIgnoreCase("all") || s[0].equalsIgnoreCase("a")
				|| (CheckInput.IsInt(s[0]) && !CheckInput.IsInt(s[1])
				&& !(s[1].equalsIgnoreCase("all") || s[1].equalsIgnoreCase("a"))))) {
			// swap two indicies
			String t = s[0];
			s[0] = s[1];
			s[1] = t;
		}

		int amt = 1;
		if (s.length == 2) {
			if (CheckInput.IsInt(s[1])) {
				if ((amt = CheckInput.GetInt(s[1], -1)) <= 0) {
					BSutils.sendMessage(player, BetterShop.getSettings().getString("nicetry"));
					return;
				}
			} else if (s[1].equalsIgnoreCase("all") || s[1].equalsIgnoreCase("a")) {
				amt = -1;
			} else {
				BSutils.sendMessage(player, ChatColor.RED + s[1] + " Is Not a Valid Number");
				return;
			}
		}

		JItem toBuy = JItemDB.isCategory(s[0]) ? null : JItemDB.findItem(s[0]), buyCat[] = null;
		if (toBuy == null) {
			buyCat = JItemDB.getItemsByCategory(s[0]);
			if (buyCat == null || buyCat.length == 0) {
				BSutils.sendMessage(player, BetterShop.getSettings().getString("unkitem").replace("<item>", s[0]));
				return;
			}
		} else if (toBuy.ID() <= 0) {
			BSutils.sendMessage(player,
					BetterShop.getSettings().getString("notforsale").
					replace("<item>", toBuy.coloredName()));
			return;
		} else if (!BetterShop.getSettings().allowbuyillegal && !toBuy.IsLegal() && !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false)) {
			BSutils.sendMessage(player, BetterShop.getSettings().getString("illegalbuy").
					replace("<item>", toBuy.coloredName()));
			return;
		}
		// initial check complete: set as last action
		userbuyHistory.put(((Player) player).getDisplayName(), "shopbuy " + Str.concatStr(s, " "));

		try {
			if (toBuy != null) {
				buyItem((Player) player, toBuy, amt, -1);
			} else {
				// buying by category
				buyItem((Player) player, buyCat, amt, -1);
			}
		} catch (Exception ex) {
			throw new WrappedCommandException(ex);
		}
	}

	@Command(commands = {"shopbuyall", "sbuyall", "buyall"},
	aliases = {"buyall", "ba", "ball"},
	desc = "Buy all that you can of an item from the shop",
	usage = "<item> [item ... ]",
	min = 1,
	permissions = {"BetterShop.user.buy"})
	public static void buyall(CommandSender player, String[] s) throws WrappedCommandException {
		for (int i = 0; i < s.length; ++i) {
			buy(player, new String[]{s[i], "all"});
		}
	}

	@Command(commands = {"shopbuystack", "sbuystack", "sbuys", "buys"},
	aliases = {"buystack", "bs"},
	desc = "Buy an item from the shop",
	usage = "<item> [amount]",
	min = 1,
	permissions = {"BetterShop.user.buy"})
	public static boolean buystack(CommandSender player, String[] s) throws WrappedCommandException {
		if (BSutils.anonymousCheck(player)) {
			return true;
		}
		try {
			int amt = 1, l = s.length - 1;
			if (s.length >= 2 && CheckInput.IsInt(s[l])) {
				amt = CheckInput.GetInt(s[l], 1);
				--l;
			}
			if (s[0].contains(",")) {
				String[] its = s[0].split(",");
				String[] newArgs = new String[s.length - 1 + its.length];
				System.arraycopy(its, 0, newArgs, 0, its.length);
				System.arraycopy(s, 1, newArgs, its.length - 1, s.length - 1);
				s = newArgs;
				l += its.length - 1;
			}
			ArrayList<JItem[]> toBuy = new ArrayList<JItem[]>();
			//Kit toBuy = new Kit();
			boolean mx = BetterShop.getSettings().usemaxstack;
			for (int i = 0; i <= l; ++i) {
				JItem j = JItemDB.isCategory(s[i]) ? null : JItemDB.findItem(s[i]), buyCat[] = null;
				if (j == null) {
					buyCat = JItemDB.getItemsByCategory(s[i]);
					if (buyCat == null || buyCat.length == 0) {
						BSutils.sendMessage(player, BetterShop.getSettings().getString("unkitem").replace("<item>", s[i]));
						continue;
					} else if (j.ID() <= 0) {
						BSutils.sendMessage(player,
								BetterShop.getSettings().getString("notforsale").
								replace("<item>", j.coloredName()));
						continue;
					} else if (!BetterShop.getSettings().allowbuyillegal && !j.IsLegal() && !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false)) {
						BSutils.sendMessage(player, BetterShop.getSettings().getString("illegalbuy").
								replace("<item>", j.coloredName()));
						continue;
					}
				}
				if (j != null) {
					toBuy.add(new JItem[]{j});
					//toBuy.AddItem(j, mx ? j.MaxStackSize() : 64);
				} else {
					toBuy.add(buyCat);
//					for(JItem c : buyCat){
//						toBuy.AddItem(c, mx ? c.MaxStackSize() : 64);
//					}
				}
			}
			if (toBuy.size() > 0) { //.numItems() > 0){
				//buyItem((Player) player, toBuy, 1, -1);
				for (JItem[] it : toBuy) {
					buyItem((Player) player, it, amt * (mx && it.length == 1 ? it[0].MaxStackSize() : 64), -1);
				}
			}
		} catch (Exception ex) {
			throw new WrappedCommandException(ex);
		}
		// overwrite history that buy wrote
		userbuyHistory.put(((Player) player).getDisplayName(), "shopbuystack " + Str.concatStr(s, " "));
		return true;
	}

	@Command(commands = {"shopbuyagain", "sbuyagain", "buyagain", "sba"},
	aliases = {"buyagain", "b!", "buy!"},
	desc = "Repeat the last purchase action the player did",
	usage = "",
	permissions = {"BetterShop.user.buy"})
	public static void buyagain(CommandSender sender, String[] s) {
		if (BSutils.anonymousCheck(sender)) {
			return;
		}
		String action = userbuyHistory.get(((Player) sender).getDisplayName());
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

	public static List<ItemStockEntry> getCanBuy(Player player, JItem[] toBuy) throws SQLException, Exception {
		return getCanBuy(player, toBuy, -1);
	}

	public static List<ItemStockEntry> getCanBuy(Player player, JItem toBuy[], double customPrice) throws SQLException, Exception {
		ArrayList<ItemStockEntry> purchase = new ArrayList<ItemStockEntry>();
		Shop shop = BetterShop.getShop(player);
		int newSize = 0;
		long maxAvail[] = new long[toBuy.length];
		for (int i = 0; i < toBuy.length; ++i) {
			if (toBuy[i] != null && !shop.pricelist.canBuy(toBuy[i])) {
				toBuy[i] = null;
			} else if (toBuy[i] != null) {
				maxAvail[i] = shop.config.useStock() ? shop.stock.freeStockRemaining(toBuy[i]) : -1;
//				System.out.println("can buy " + maxAvail[i] + " of " + toBuy[i].Name());
				++newSize;
			}
		}

		ItemStack start[] = ItemStackManip.copy(player.getInventory().getContents()),
				result[] = ItemStackManip.copy(start);
//		for(int i = 0; i<result.length; ++i){
//			System.out.println(i + " " + (result[i] == null ? "null" :
//				JItemDB.GetItemName(result[i]) + "  x " + result[i].getAmount()));
//		}
		// treat as a kit
		ItemStackManip.add(result, new Kit(toBuy), 64 * 36, !BetterShop.getSettings().usemaxstack);

//		for(int i = 0; i<result.length; ++i){
//			System.out.println(i + " " + (result[i] == null ? "null" :
//				JItemDB.GetItemName(result[i]) + "  x " + result[i].getAmount()));
//		}

		List<ItemStack> diff = ItemStackManip.itemStackDifferences(start, result);

//		System.out.println("after adding: " + diff.size() + " new items");
//		for(ItemStack i : diff) {
//			System.out.println(JItemDB.GetItemName(i) + ":" + i.getDurability() + "x" + i.getAmount());
//		}

		if (diff.isEmpty()/* || diff.size() > newSize*/) {
			return purchase;
		}
		int minbuy = diff.get(0).getAmount(), maxbuy = 0;
		for (int i = 0; i < toBuy.length; ++i) {
			if (toBuy[i] != null) {
				int in = ItemStackManip.indexOf(diff, toBuy[i]);
				if (in < 0) {
					--newSize;
					toBuy[i] = null;
				} else {
					int a = diff.get(in).getAmount();
					if (maxAvail[i] >= 0 && a > maxAvail[i]) {
						a = (int) maxAvail[i];
					}
					if (maxbuy < a) {
						maxbuy = diff.get(in).getAmount();
					}
					if (minbuy > diff.get(in).getAmount()) {
						minbuy = diff.get(in).getAmount();
					}
				}
			}
		}

		double cash = BSEcon.getBalance(player);
		double baseprice = 0;

		for (int i = 0; i < toBuy.length; ++i) {
			if (toBuy[i] != null) {
				baseprice += customPrice >= 0 ? customPrice : shop.pricelist.getBuyPrice(toBuy[i]);
//				System.out.println(toBuy[i].Name() + ": " + shop.pricelist.getBuyPrice(toBuy[i]));
			}
		}
//		System.out.println("baseprice: " + baseprice + "/" + cash);
		if (baseprice > cash) {
			return purchase;
		}
		baseprice /= newSize;

		//start at estimated max
		int amt = (int) (cash / baseprice);
		if (amt > minbuy) {
			amt = minbuy;
		}
//		System.out.println("trying " + amt + " each");
		if (customPrice < 0) {
			// now loop until can't afford
			/** (this is in anticipation of scaled prices) **/
			// first check if base amt is too much
			double price;
			do {
				price = 0;

				for (int i = 0; i < toBuy.length; ++i) {
					if (toBuy[i] != null) {
						price += shop.pricelist.itemBuyPrice(player, toBuy[i],
								amt > maxAvail[i] && maxAvail[i] > 0 ? (int) maxAvail[i] : amt);
					}
				}
			} while (price > cash && --amt > 0);

			while (price + baseprice < cash) {
//				System.out.println("trying " + amt + " each");
				++amt;

				price = 0;

				for (int i = 0; i < toBuy.length; ++i) {
					if (toBuy[i] != null) {
						price += shop.pricelist.itemBuyPrice(player, toBuy[i],
								amt > maxAvail[i] && maxAvail[i] > 0 ? (int) maxAvail[i] : amt);
					}
				}
				if (price > cash) {
					--amt;
					break;
				} else if (amt >= maxbuy) { // check if at max can buy
					amt = maxbuy;
					break;
				}
			}
//			System.out.println("final: " + amt + " each: " + price);
		}

//		System.out.println("after removing can't afford:");
		if (amt > 0) {
			for (int i = 0; i < toBuy.length; ++i) {
				if (toBuy[i] != null && maxAvail[i] != 0) {
					purchase.add(new ItemStockEntry(toBuy[i],
							amt > maxAvail[i] && maxAvail[i] > 0 ? (int) maxAvail[i] : amt));
//					System.out.println(toBuy[i].Name() + " x " + purchase.get(purchase.size()-1).amount + " (of " + maxAvail[i] + ")");
				}
			}
		}

		return purchase;
	}

//	public static void buyAllItem(Player player, JItem toBuy) {
//		if (player == null || toBuy == null) {
//			return;
//		}
//		_buyItem(player, new JItem[]{toBuy}, BSutils.amtCanHold(player, toBuy), -1);
//	}
//
//	public static void buyAllItem(Player player, JItem toBuy, double customPrice){
//		if (player == null || toBuy == null) {
//			return;
//		}
//		_buyItem(player, new JItem[]{toBuy}, BSutils.amtCanHold(player, toBuy), customPrice);
//	}
//
//	public static void buyAllItem(Player player, JItem[] toBuy, double customPrice) throws SQLException, Exception {
//		if (player == null || toBuy == null || toBuy.length == 0) {
//			return;
//		}
//		int amt;
//		if(toBuy.length == 1){
//			amt = BSutils.amtCanHold(player, toBuy[0]);
//		} else {
//			List<ItemStockEntry> open = getCanBuy(player, toBuy, -1);
//			amt = 0;
//			for(ItemStockEntry i : open){
//				if(i.amount > amt){
//					amt = (int) i.amount;
//				}
//			}
//		}
//		_buyItem(player, toBuy, amt, customPrice);
//	}
	public static void buyItem(Player player, JItem toBuy, int amt) throws SQLException, Exception {
		buyItem(player, new JItem[]{toBuy}, amt, -1);
	}

	public static void buyItem(Player player, JItem toBuy, int amt, double customPrice) throws SQLException, Exception {
		buyItem(player, new JItem[]{toBuy}, amt, customPrice);
	}

	public static void buyItem(Player player, JItem[] toBuy, int amt, double customPrice) throws SQLException, Exception {
		if (toBuy == null || toBuy.length == 0 || toBuy[0] == null || player == null || amt == 0) {
			return;
		}
		Shop shop = BetterShop.getShop(player);
		if (toBuy.length == 1) {
			if (customPrice <= 0 && !shop.pricelist.canBuy(toBuy[0])) {
				BSutils.sendMessage(player,
						BetterShop.getSettings().getString("notforsale").
						replace("<item>", toBuy[0].coloredName()));
				return;
			}
			// check if there are items avaliable for purchase
			long avail = -1;
			if (BetterShop.getSettings().useItemStock) {
				try {
					avail = shop.stock.getItemAmount(toBuy[0]);
				} catch (Exception ex) {
					BetterShopLogger.Log(Level.SEVERE, ex);
					avail = -1;
				}
				if (avail == 0) {
					BSutils.sendMessage(player, BetterShop.getSettings().getString("outofstock").
							replace("<item>", toBuy[0].coloredName()));
					return;
				} else if (avail >= 0 && amt > avail) {
					BSutils.sendMessage(player, BetterShop.getSettings().getString("lowstock").
							replace("<item>", toBuy[0].coloredName()).
							replace("<amt>", String.valueOf(avail)));
					amt = (int) avail;
				}
			}
			int canHold = shop.pricelist.getAmountCanBuy(player, toBuy[0], customPrice);
			if (amt < 0) {
				amt = canHold;
			} else if (canHold >= 0 && amt > canHold) {
				BSutils.sendMessage(player, BetterShop.getSettings().getString("outofroom").
						replace("<item>", toBuy[0].coloredName()).
						replace("<amt>", String.valueOf(amt)).
						replace("<priceper>", String.format("%01.2f",
						shop.pricelist.itemBuyPrice(player, toBuy[0], 1))).
						replace("<leftover>", String.valueOf(amt - canHold)).
						replace("<curr>", BetterShop.getSettings().currency()).
						replace("<free>", String.valueOf(canHold)));
				if (canHold <= 0) {
					return;
				}
				amt = canHold;
			}
			_buyItem(player, toBuy, amt, customPrice);
		} else {
			List<ItemStockEntry> canBuy = new ArrayList<ItemStockEntry>();

			String notWant = "", noStock = "";
			for (JItem i : toBuy) {
				if (BetterShop.getPricelist(player.getLocation()).canBuy(i)) {
					int buyamt = amt;
					// check if heve enough to buy
					if (BetterShop.getSettings().useItemStock) {
						long avail = -1;
						try {
							avail = shop.stock.getItemAmount(i);
						} catch (Exception ex) {
							BetterShopLogger.Log(Level.SEVERE, ex);
							avail = -1;
						}
						if (avail == 0) {
							noStock += i.coloredName() + ", ";
							continue;
						} else if (avail >= 0 && amt > avail) {
							buyamt = (int) avail;
						}
					}
					canBuy.add(new ItemStockEntry(i, buyamt));
				} else if (i != null) {
					notWant += i.coloredName() + ", ";
				}
			}
			
			if (!noStock.isEmpty()) {
				noStock = noStock.substring(0, noStock.length() - 2);
				if (noStock.contains(",")) {
					noStock = "(" + noStock + ")";
				}
				BSutils.sendMessage(player, BetterShop.getSettings().getString("outofstock").
						replace("<item>", noStock));
			}

			if (canBuy.isEmpty()) {
				if (!notWant.isEmpty()) {
					notWant = notWant.substring(0, notWant.length() - 2);
					if (notWant.contains(",")) {
						notWant = "(" + notWant + ")";
					}
					BSutils.sendMessage(player,
							BetterShop.getSettings().getString("notforsale").
							replace("<item>", notWant));
				}
				return;
			}

			// else, find max. can hold
			ItemStack start[] = ItemStackManip.copy(player.getInventory().getContents()),
					result[] = ItemStackManip.copy(start);
			List<ItemStack> add = new ArrayList<ItemStack>();
			for (ItemStockEntry u : canBuy) {
				add.add(new ItemStack(u.itemNum, (int) u.amount));
			}
			ItemStackManip.add(result, add, !BetterShop.getSettings().usemaxstack);
			List<ItemStack> diff = ItemStackManip.itemStackDifferences(start, result);

			String over = "";
			int overamt = 0, overleft = 0, overbuy = 0;
			double buyprice = 0;
			for (ItemStockEntry i : canBuy) { //ItemStack i : diff) {
				int j = -1;
				for (int n = 0; n < diff.size(); ++n) {
					if (i.equals(diff.get(n))) {
						j = n;
						break;
					}
				}
				if (j < 0) {
					// not in diff: can't be added
					over += JItemDB.GetItemColoredName(i.itemNum, i.itemSub) + ", ";
					overamt += i.amount;
					buyprice += BetterShop.getPricelist(player.getLocation()).itemBuyPrice(player, toBuy[0], 1) * i.amount;
					i.amount = 0;
				} else {
					if (i.amount > diff.get(j).getAmount()) {
						over += JItemDB.GetItemColoredName(i.itemNum, i.itemSub) + ", ";
						overamt += diff.get(j).getAmount();
						overleft += canBuy.get(j).amount - diff.get(j).getAmount();
						overbuy += (int) diff.get(j).getAmount();
						buyprice += BetterShop.getPricelist(player.getLocation()).itemBuyPrice(player, toBuy[0], 1) * (int) diff.get(j).getAmount();
						canBuy.get(j).amount = diff.get(j).getAmount();
					}
				}
			}
			if (overamt > 0) {
				over = over.substring(0, over.length() - 2);
				if (over.contains(",")) {
					over = "(" + over + ")";
				}
				BSutils.sendMessage(player, BetterShop.getSettings().getString("outofroom").
						replace("<item>", over).
						replace("<amt>", String.valueOf(overamt)).
						replace("<priceper>", String.format("%01.2f", buyprice / overbuy)).
						replace("<leftover>", String.valueOf(overleft)).
						replace("<curr>", BetterShop.getSettings().currency()).
						replace("<free>", String.valueOf(overbuy)));

				if (overbuy <= 0) {
					return;
				}
			}

			buyItem(player, canBuy, customPrice);
		}

	}

	/*
	 * assumes that the amounts given are correct, and will try to purchase until out of room or money
	 */
	public static void buyItem(Player player, List<ItemStockEntry> buy, double customPrice) throws SQLException, Exception {
		Shop shop = BetterShop.getShop(player.getLocation());
		PlayerInventory inv = player.getInventory();
		double price = 0;
		String itemN = "";
		int amtBought = 0;
		for (ItemStockEntry ite : buy) {
			if (ite == null || ite.itemNum <= 0) {
				continue;
			}
			JItem it = JItemDB.GetItem(ite.itemNum, (byte) ite.itemSub);
			if (it == null) {
				continue;
			}
			long maxAmt = shop.config.useStock() ? shop.stock.getItemAmount(it) : -1;
			int buyAmt = maxAmt > 0 && ite.amount > maxAmt ? (int) maxAmt : (int) ite.amount;
			double itemCost = customPrice >= 0 ? customPrice * buyAmt
					: shop.pricelist.itemBuyPrice(player, it, buyAmt);
			amtBought += buyAmt;
			price += itemCost;
			// buy
			if (BSEcon.debit(player, itemCost)) {
				itemN += it.coloredName() + ", ";
				if (it.isEntity()) {
					CreatureItem c = CreatureItem.getCreature(it.ID());
					if (c != null) {
						for (int i = 0; i < buyAmt; ++i) {
							c.spawnNewWithOwner(player);
						}
					}
				} else {
					if (it.equals(JItems.MAP)) {
						//TODO: either make a new map or copy a map.....
					}
					if (!ItemStackManip.canHold(player.getInventory().getContents(), it, buyAmt, !BetterShop.getSettings().usemaxstack)) {
						BSutils.sendMessage(player, ChatColor.RED + "Program Error: tried to buy more than can hold: aborting.");
						System.out.println(ChatColor.RED + "Program Error: tried to buy more than can hold: " + it + " x" + buyAmt);
						BSEcon.credit(player, itemCost);
						break;
					}
					inv.setContents(ItemStackManip.add(player.getInventory().getContents(),
							it, buyAmt, !BetterShop.getSettings().usemaxstack));
				}

				try {
					if (BetterShop.getSettings().useItemStock) {
						shop.stock.changeItemAmount(it, -buyAmt);
					}
					if (BetterShop.getSettings().logUserTransactions) {
						shop.transactions.addRecord(new UserTransaction(
								it, false, buyAmt, price / buyAmt, player.getDisplayName()));
					}

				} catch (Exception ex) {
					BetterShopLogger.Log(Level.SEVERE, ex);
				}
			} else {
				BSutils.sendMessage(player,
						BetterShop.getSettings().getString("insuffunds").
						replace("<item>", it.coloredName()).
						replace("<amt>", String.valueOf(buyAmt)).
						replace("<total>", String.valueOf(price)).
						replace("<curr>", BetterShop.getSettings().currency()).
						replace("<priceper>", String.valueOf(price / buyAmt)).
						replace("<totcur>", BSEcon.format(price)));
				break;
			}
		}

		if (itemN.length() > 0) {
			itemN = itemN.substring(0, itemN.length() - 2);
			if (itemN.contains(",")) {
				itemN = "(" + itemN + ")";
			}
		}

		BSutils.sendFormttedMessage(player, "buymsg", itemN, amtBought, price);
	}

	/**
	 * assumes items can be bought, and have the correct amount(s)
	 */
	private static void _buyItem(Player player, JItem[] toBuy, int amt, double customPrice) throws SQLException, Exception {
		Shop shop = BetterShop.getShop(player.getLocation());
		PlayerInventory inv = player.getInventory();
		double price = 0;// = customPrice >= 0 ? customPrice * amt : shop.pricelist.itemBuyPrice(player, toBuy, amt);
		String itemN = "";
		int amtBought = 0;
		for (JItem it : toBuy) {
			if (it == null) {
				continue;
			}
			long maxAmt = shop.config.useStock() ? shop.stock.getItemAmount(it) : -1;
			int buyAmt = maxAmt > 0 && amt > maxAmt ? (int) maxAmt : amt;
			amtBought += buyAmt;
			double itemCost = customPrice >= 0 ? customPrice * buyAmt
					: shop.pricelist.itemBuyPrice(player, it, buyAmt);
			price += itemCost;
			if (itemCost < 0) {
				throw new Exception("Invalid Price encountered: " + itemCost + " for " + amt + " " + it.Name());
			}
			if (BSEcon.debit(player, itemCost)) {
				itemN += it.coloredName() + ", ";
				if (it.isEntity()) {
					CreatureItem c = CreatureItem.getCreature(it.ID());
					if (c != null) {
						for (int i = 0; i < buyAmt; ++i) {
							c.spawnNewWithOwner(player);
						}
					}
				} else {
					if (it.equals(JItems.MAP)) {
						//TODO: either make a new map or copy a map.....
					}
					inv.setContents(ItemStackManip.add(player.getInventory().getContents(),
							it, buyAmt, !BetterShop.getSettings().usemaxstack));
				}

				try {
					if (BetterShop.getSettings().useItemStock) {
						shop.stock.changeItemAmount(it, -buyAmt);
					}
					if (BetterShop.getSettings().logUserTransactions) {
						shop.transactions.addRecord(new UserTransaction(
								it, false, buyAmt, price / buyAmt, player.getDisplayName()));
					}

				} catch (Exception ex) {
					BetterShopLogger.Log(Level.SEVERE, ex);
				}
			} else {
				BSutils.sendMessage(player,
						BetterShop.getSettings().getString("insuffunds").
						replace("<item>", it.coloredName()).
						replace("<amt>", String.valueOf(amt)).
						replace("<total>", String.valueOf(price)).
						replace("<curr>", BetterShop.getSettings().currency()).
						replace("<priceper>", String.valueOf(price / amt)).
						replace("<totcur>", BSEcon.format(price)));
				break;
			}
		}

		if (itemN.length() > 0) {
			itemN = itemN.substring(0, itemN.length() - 2);
			if (itemN.contains(",")) {
				itemN = "(" + itemN + ")";
			}
		}

		BSutils.sendFormttedMessage(player, "buymsg", itemN, amtBought, price);
	}
} // end class BuyCommands

