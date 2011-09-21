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

import java.sql.SQLException;
import me.jascotty2.bettershop.BSEcon;
import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import me.jascotty2.bettershop.utils.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopLogger;

import me.jascotty2.lib.bukkit.item.CreatureItem;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.item.JItems;
import me.jascotty2.lib.bukkit.shop.UserTransaction;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.util.Str;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import me.jascotty2.bettershop.shop.Shop;
import me.jascotty2.lib.bukkit.commands.Command;
import me.jascotty2.lib.bukkit.commands.WrappedCommandException;
import me.jascotty2.lib.bukkit.inventory.ItemStackManip;
import org.bukkit.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
					BSutils.sendMessage(player, BetterShop.getConfig().getString("nicetry"));
					return;
				}
			} else if (s[1].equalsIgnoreCase("all") || s[1].equalsIgnoreCase("a")) {
				amt = -1;
			} else {
				BSutils.sendMessage(player, ChatColor.RED + s[1] + " Is Not a Valid Number");
				return;
			}
		}

		JItem toBuy = JItemDB.findItem(s[0]);
		if (toBuy == null) {
			BSutils.sendMessage(player, BetterShop.getConfig().getString("unkitem").replace("<item>", s[0]));
			return;
		} else if (toBuy.ID() <= 0) {
			BSutils.sendMessage(player,
						BetterShop.getConfig().getString("notforsale").
						replace("<item>", toBuy.coloredName()));
			return;
		} else if (!BetterShop.getConfig().allowbuyillegal && !toBuy.IsLegal() && !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false)) {
			BSutils.sendMessage(player, BetterShop.getConfig().getString("illegalbuy").
					replace("<item>", toBuy.coloredName()));
			return;
		}
		// initial check complete: set as last action
		userbuyHistory.put(((Player) player).getDisplayName(), "shopbuy " + Str.concatStr(s, " "));

		try {
			if (amt < 0) {
				buyAllItem((Player) player, toBuy);
			} else {
				buyItem((Player) player, toBuy, amt);
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
		try {
			for (int i = 0; i < s.length; ++i) {
				buy(player, new String[]{s[i], "all"});
			}
		} catch (Exception ex) {
			throw new WrappedCommandException(ex);
		}
	}

	@Command(commands = {"shopbuystack", "sbuystack", "sbuys", "buys"},
	aliases = {"buystack", "bs"},
	desc = "Buy an item from the shop",
	usage = "<item> [amount]",
	permissions = {"BetterShop.user.buy"})
	public static boolean buystack(CommandSender player, String[] s) throws WrappedCommandException {
		if (!BSPermissions.hasPermission(player, BetterShopPermission.USER_BUY, true)) {
			return true;
		} else if (s.length == 0) {
			BSutils.sendMessage(player, "What?");
			return false;
		} else if (BSutils.anonymousCheck(player)) {
			return true;
		}
		try {
			if (s.length == 2 && CheckInput.IsInt(s[1])) {
				JItem toBuy = JItemDB.findItem(s[0]);
				if (toBuy == null) {
					BSutils.sendMessage(player, BetterShop.getConfig().getString("unkitem").
							replace("<item>", s[0]));
					return true;
				} else if (!BetterShop.getConfig().allowbuyillegal && !toBuy.IsLegal() && !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false)) {
					BSutils.sendMessage(player, BetterShop.getConfig().getString("illegalbuy").
							replace("<item>", toBuy.coloredName()));
					return true;
				}
				// buy max. stackable
				buy(player, new String[]{toBuy.IdDatStr(), String.valueOf((BetterShop.getConfig().usemaxstack ? toBuy.getMaxStackSize() : 64) * CheckInput.GetInt(s[1], 1))});
			} else {
				for (String is : s) {
					JItem toBuy = JItemDB.findItem(is);
					if (toBuy == null) {
						BSutils.sendMessage(player, BetterShop.getConfig().getString("unkitem").
								replace("<item>", is));
						return true;
					} else if (!BetterShop.getConfig().allowbuyillegal && !toBuy.IsLegal() && !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false)) {
						BSutils.sendMessage(player, BetterShop.getConfig().getString("illegalbuy").
								replace("<item>", toBuy.coloredName()));
						return true;
					}
					// buy max. stackable
					buy(player, new String[]{toBuy.IdDatStr(), String.valueOf(BetterShop.getConfig().usemaxstack ? toBuy.getMaxStackSize() : 64)});
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

	public static void buyAllItem(Player player, JItem toBuy) {
		if (player == null || toBuy == null) {
			return;
		}
//		int canBuy = BetterShop.getPricelist(player).getAmountCanBuy(player, toBuy);
//
//		if(canBuy <= 0){
//			if(BetterShop.getConfig().useItemStock 
//					&& BetterShop.getStock(player).freeStockRemaining(toBuy) == 0) {
//				BSutils.sendMessage(player, BetterShop.getConfig().getString("outofstock").
//						replace("<item>", toBuy.coloredName()));
//			} else {
//				if(BSutils.amtCanHold(player, toBuy) > 0){
//					double unitPrice = BetterShop.getPricelist(player).itemBuyPrice(player, toBuy, 1);
//					BSutils.sendMessage(player,
//					BetterShop.getConfig().getString("insuffunds").
//					replace("<item>", toBuy.coloredName()).
//					replace("<amt>", "any").
//					replace("<total>", String.valueOf(unitPrice)).
//					replace("<curr>", BetterShop.getConfig().currency()).
//					replace("<priceper>", String.valueOf(unitPrice)).
//					replace("<totcur>", BSEcon.format(unitPrice)));
//				}
//			}
//		} else {
//			// display confirmation if stock low?
////			if(BetterShop.getConfig().useItemStock
////					&& BetterShop.getStock(player).freeStockRemaining(toBuy) == canBuy) {
////				BSutils.sendMessage(player, BetterShop.getConfig().getString("lowstock").
////						replace("<item>", toBuy.coloredName()).
////						replace("<amt>", String.valueOf(canBuy)));
////			}
//			_buyItem(player, toBuy, canBuy);
//		}
		_buyItem(player, toBuy, BSutils.amtCanHold(player, toBuy));
	}

	public static void buyItem(Player player, JItem toBuy, int amt) throws SQLException, Exception {
		if (toBuy == null || player == null || amt <= 0) {
			return;
		}
		int canHold = BSutils.amtCanHold(player, toBuy);
		if (amt > canHold) {
			if (!BetterShop.getPricelist(player.getLocation()).isForSale(toBuy)) {
				BSutils.sendMessage(player,
						BetterShop.getConfig().getString("notforsale").
						replace("<item>", toBuy.coloredName()));
			} else {
				BSutils.sendMessage(player, BetterShop.getConfig().getString("outofroom").
						replace("<item>", toBuy.coloredName()).
						replace("<amt>", String.valueOf(amt)).
						replace("<priceper>", String.format("%01.2f",
						BetterShop.getPricelist(player.getLocation()).itemBuyPrice(player, toBuy, 1))).
						replace("<leftover>", String.valueOf(amt - canHold)).
						replace("<curr>", BetterShop.getConfig().currency()).
						replace("<free>", String.valueOf(canHold)));
			}
			if (canHold == 0) {
				return;
			}
			amt = canHold;
		}
		_buyItem(player, toBuy, amt);
	}

	private static void _buyItem(Player player, JItem toBuy, int amt) {
		Shop shop = BetterShop.getShop(player.getLocation());
		double price = shop.pricelist.itemBuyPrice(player, toBuy, amt);
		if (price < 0) {
			if (price != Double.NEGATIVE_INFINITY) {
				BSutils.sendMessage(player,
						BetterShop.getConfig().getString("notforsale").
						replace("<item>", toBuy.coloredName()));
			}
			return;
		}
		// now check if there are items avaliable for purchase
		long avail = -1;
		if (BetterShop.getConfig().useItemStock) {
			try {
				avail = shop.stock.getItemAmount(toBuy);
			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
				avail = -1;
			}
			if (avail == 0) {
				BSutils.sendMessage(player, BetterShop.getConfig().getString("outofstock").
						replace("<item>", toBuy.coloredName()));
				return;
			} else if (avail >= 0 && amt > avail) {
				BSutils.sendMessage(player, BetterShop.getConfig().getString("lowstock").
						replace("<item>", toBuy.coloredName()).
						replace("<amt>", String.valueOf(avail)));
				amt = (int) avail;
			}
		}
		PlayerInventory inv = player.getInventory();

		if (price == 0 || BSEcon.debit(player, price)) {
			if (toBuy.isEntity()) {
				CreatureItem c = CreatureItem.getCreature(toBuy.ID());
				if (c != null) {
					for (int i = 0; i < amt; ++i) {
						c.spawnNewWithOwner(player);
					}
				}
			} else {
				if (toBuy.equals(JItems.MAP)) {
					//TODO: either make a new map or copy a map.....
				}
				inv.setContents(ItemStackManip.add(player.getInventory().getContents(),
						toBuy, amt, !BetterShop.getConfig().usemaxstack));
			}

			BSutils.sendFormttedMessage(player, "buymsg", toBuy.coloredName(), amt, price);

			try {
				if (BetterShop.getConfig().useItemStock) {
					shop.stock.changeItemAmount(toBuy, -amt);
				}
				if (BetterShop.getConfig().logUserTransactions) {
					shop.transactions.addRecord(new UserTransaction(
							toBuy, false, amt, price / amt, player.getDisplayName()));
				}

			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
			}
		} else {
			BSutils.sendMessage(player,
					BetterShop.getConfig().getString("insuffunds").
					replace("<item>", toBuy.coloredName()).
					replace("<amt>", String.valueOf(amt)).
					replace("<total>", String.valueOf(price)).
					replace("<curr>", BetterShop.getConfig().currency()).
					replace("<priceper>", String.valueOf(price / amt)).
					replace("<totcur>", BSEcon.format(price)));
		}
	}
} // end class BuyCommands

