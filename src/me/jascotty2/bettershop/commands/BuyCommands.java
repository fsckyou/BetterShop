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

import me.jascotty2.lib.bukkit.item.CreatureItem;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.item.JItems;
import me.jascotty2.lib.bukkit.item.Kit;
import me.jascotty2.lib.bukkit.item.Kit.KitItem;
import me.jascotty2.lib.bukkit.shop.UserTransaction;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.util.Str;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import me.jascotty2.bettershop.shop.Shop;
import me.jascotty2.lib.bukkit.commands.Command;
import me.jascotty2.lib.bukkit.inventory.ItemStackManip;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author jacob
 */
public class BuyCommands {

	static Map<String, String> userbuyHistory = new HashMap<String, String>();

	@Command(
	commands = {"shopbuy", "sbuy", "buy"},
	aliases = {"buy", "b", "sb"},
	desc = "Buy an item from the shop",
	usage = "<item> [amount]",
	permissions = {"BetterShop.user.buy"})
	public static boolean buy(CommandSender player, String[] s) {
		if (BSutils.anonymousCheck(player)) {
			return true;
		} else if ((s.length > 2) || (s.length == 0)) {
			BSutils.sendMessage(player, "What?");
			return false;
		} else if (s.length == 2 && (s[0].equalsIgnoreCase("all")
				|| (CheckInput.IsInt(s[0]) && !CheckInput.IsInt(s[1])))) {
			// swap two indicies
			String t = s[0];
			s[0] = s[1];
			s[1] = t;
		}
		JItem toBuy = JItemDB.findItem(s[0]);
		if (toBuy == null) {
			BSutils.sendMessage(player, String.format(BetterShop.getConfig().getString("unkitem").replace("<item>", "%1$s"), s[0]));
			return true;
		} else if (toBuy.ID() <= 0) {
			BSutils.sendMessage(player, toBuy.coloredName() + " Cannot be Bought");//, toBuy.coloredName());
			return true;
		} else if (!BetterShop.getConfig().allowbuyillegal && !toBuy.IsLegal() && !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false)) {
			BSutils.sendMessage(player, BetterShop.getConfig().getString("illegalbuy").
					replace("<item>", toBuy.coloredName()));
			return true;
		}/* else if (toBuy.isKit()) {
		return buyKit(player, s);
		}*/

		// initial check complete: set as last action
		userbuyHistory.put(((Player) player).getDisplayName(), "shopbuy " + Str.concatStr(s, " "));

		//UserTransaction bought ;
		if (s.length == 2) {
			if (s[1].equalsIgnoreCase("all")) {
				buyAllItem((Player) player, toBuy);
			} else if (!CheckInput.IsInt(s[1])) {
				BSutils.sendMessage(player, s[1] + " is definitely not a number.");
				return true;
			} else {
				buyItem((Player) player, toBuy, CheckInput.GetInt(s[1], -1));
			}
		} else {
			buyItem((Player) player, toBuy, 1);
		}
		return true;
	}

	@Command(
	commands = {"shopbuyall", "sbuyall", "buyall"},
	aliases = {"buyall", "ba", "ball"},
	desc = "Buy all that you can of an item from the shop",
	usage = "<item> [amount]",
	permissions = {"BetterShop.user.buy"})
	public static boolean buyall(CommandSender player, String[] s) {
		String newArgs[] = new String[s.length + 1];
		System.arraycopy(s, 0, newArgs, 0, s.length);
		newArgs[newArgs.length - 1] = "all";
		return buy(player, newArgs);
	}

	@Command(
	commands = {"shopbuystack", "sbuystack", "sbuys", "buys"},
	aliases = {"buystack", "bs"},
	desc = "Buy an item from the shop",
	usage = "<item> [amount]",
	permissions = {"BetterShop.user.buy"})
	public static boolean buystack(CommandSender player, String[] s) {
		if (!BSPermissions.hasPermission(player, BetterShopPermission.USER_BUY, true)) {
			return true;
		} else if (s.length == 0) {
			BSutils.sendMessage(player, "What?");
			return false;
		} else if (BSutils.anonymousCheck(player)) {
			return true;
		}
		if (s.length == 2 && CheckInput.IsInt(s[1])) {

			JItem toBuy = JItemDB.findItem(s[0]);
			if (toBuy == null) {
				BSutils.sendMessage(player, String.format(BetterShop.getConfig().getString("unkitem").
						replace("<item>", "%1$s"), s[0]));
				return true;
			} else if (!BetterShop.getConfig().allowbuyillegal && !toBuy.IsLegal() && !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false)) {
				BSutils.sendMessage(player, String.format(BetterShop.getConfig().getString("illegalbuy").
						replace("<item>", "%1$s"), toBuy.coloredName()));
				return true;
			}
			// buy max. stackable
			buy(player, new String[]{toBuy.IdDatStr(), String.valueOf((BetterShop.getConfig().usemaxstack ? toBuy.getMaxStackSize() : 64) * CheckInput.GetInt(s[1], 1))});
		} else {
			for (String is : s) {
				JItem toBuy = JItemDB.findItem(is);
				if (toBuy == null) {
					BSutils.sendMessage(player, String.format(BetterShop.getConfig().getString("unkitem").
							replace("<item>", "%1$s"), is));
					return true;
				} else if (!BetterShop.getConfig().allowbuyillegal && !toBuy.IsLegal() && !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false)) {
					BSutils.sendMessage(player, String.format(BetterShop.getConfig().getString("illegalbuy").
							replace("<item>", "%1$s"), toBuy.coloredName()));
					return true;
				}
				// buy max. stackable
				buy(player, new String[]{toBuy.IdDatStr(), String.valueOf(BetterShop.getConfig().usemaxstack ? toBuy.getMaxStackSize() : 64)});
			}
		}// overwrite history that buy wrote
		userbuyHistory.put(((Player) player).getDisplayName(), "shopbuystack " + Str.concatStr(s, " "));
		return true;
	}

	@Command(
	commands = {"shopbuyagain", "sbuyagain", "buyagain", "sba"},
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

	public static UserTransaction buyAllItem(Player player, JItem toBuy) {
		if (toBuy == null || player == null) {
			return null;
		}
		double price = BetterShop.getPricelist(player.getLocation()).itemBuyPrice(player, toBuy, 1);
		if (price < 0) {
			if (price != Double.NEGATIVE_INFINITY) {
				BSutils.sendMessage(player,
						BetterShop.getConfig().getString("notforsale").
						replace("<item>", toBuy.coloredName()));
			}
			return null;
		}
		int canHold = BSutils.amtCanHold(player, toBuy),
				maxStack = BetterShop.getConfig().usemaxstack ? toBuy.getMaxStackSize() : 64;

		double bal = BSEcon.getBalance(player);
		if (bal < price * canHold) {
			canHold = (int) (bal / price);
		}

		return _buyItem(player, toBuy, canHold, maxStack, price);
	}

	public static UserTransaction buyItem(Player player, JItem toBuy, int amt) {
		if (toBuy == null || player == null || amt <= 0) {
			return null;
		} else if (amt <= 0) {
			BSutils.sendMessage(player, BetterShop.getConfig().getString("nicetry"));
			return null;
		}
		double price = BetterShop.getPricelist(player.getLocation()).itemBuyPrice(player, toBuy, 1);
		if (price < 0) {
			if (price != Double.NEGATIVE_INFINITY) {
				BSutils.sendMessage(player,
						BetterShop.getConfig().getString("notforsale").
						replace("<item>", toBuy.coloredName()));
			}
			return null;
		}
		if (toBuy.isKit()) {
			return _buyKit(player, JItemDB.getKit(toBuy), amt, price);
		}
		int canHold = 0, maxStack = BetterShop.getConfig().usemaxstack ? toBuy.getMaxStackSize() : 64;
		PlayerInventory inv = player.getInventory();
		if (!toBuy.isEntity()) {
			// don't search armor slots
			for (int i = 0; i <= 35; ++i) {
				ItemStack it = inv.getItem(i);
				if (it.getAmount() <= 0 || (toBuy.equals(it) && it.getAmount() < maxStack)) {
					canHold += maxStack - it.getAmount();
				}
			}
		} else {
			canHold = BetterShop.getConfig().maxEntityPurchase;
		}
		if (amt > canHold) {
			BSutils.sendMessage(player, BetterShop.getConfig().getString("outofroom").
					replace("<item>", toBuy.coloredName()).
					replace("<amt>", String.valueOf(amt)).
					replace("<priceper>", String.format("%01.2f", price)).
					replace("<leftover>", String.valueOf(amt - canHold)).
					replace("<curr>", BetterShop.getConfig().currency()).
					replace("<free>", String.valueOf(canHold)));
			if (canHold == 0) {
				return null;
			}
			amt = canHold;
		}

		return _buyItem(player, toBuy, amt, maxStack, price);
	}

	private static UserTransaction _buyItem(Player player, JItem toBuy, int amt, int maxStack, double unitPrice) {
		UserTransaction ret = null; // new UserTransaction(toBuy, false);
		PlayerInventory inv = player.getInventory();
		Shop shop = BetterShop.getShop(player.getLocation());
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
				return null;
			} else if (avail >= 0 && amt > avail) {
				BSutils.sendMessage(player, BetterShop.getConfig().getString("lowstock").
						replace("<item>", toBuy.coloredName()).
						replace("<amt>", String.valueOf(avail)));
				amt = (int) avail;
			}
		}
		double cost = amt * unitPrice;

		if (cost == 0 || BSEcon.debit(player, cost)) {
			if (!toBuy.isEntity()) {
				if (toBuy.equals(JItems.MAP)) {
					//TODO: either make a new map or copy a map.....
				}
				//if (maxStack == 64) { //((Player) player).getInventory().addItem(toBuy.toItemStack(amtbought));
				//    inv.addItem(toBuy.toItemStack(amtbought));
				//} else {
				int amtLeft = amt;
				for (int i = 0; i <= 35; ++i) {
					ItemStack it = inv.getItem(i);
					if (it.getAmount() <= 0) {
						inv.setItem(i, toBuy.toItemStack((maxStack < amtLeft ? maxStack : amtLeft)));
						amtLeft -= maxStack;
					} else if (toBuy.equals(it) && it.getAmount() < maxStack) {
						int itAmt = it.getAmount();
						inv.setItem(i, toBuy.toItemStack(amtLeft + itAmt > maxStack ? maxStack : amtLeft + itAmt));
						amtLeft -= maxStack - itAmt;
					}
					if (amtLeft <= 0) {
						break;
					}
				}
				//}
				// drop in front of player?
				//World w = player.getServer().getWorld(""); w.dropItem(player.getServer().getPlayer("").getLocation(), leftover.values());//.dropItem(
			} else {
				CreatureItem c = CreatureItem.getCreature(toBuy.ID());
				if (c != null) {
					for (int i = 0; i < amt; ++i) {
						c.spawnNewWithOwner(player);
					}
				}
			}

			BSutils.sendFormttedMessage(player, "buymsg", toBuy.coloredName(), amt, cost);
			// replace("<priceper>", unitPrice

			try {
				if (BetterShop.getConfig().useItemStock) {
					shop.stock.changeItemAmount(toBuy, -amt);
				}

				ret = new UserTransaction(
						toBuy, false, amt, unitPrice, player.getDisplayName());
				shop.transactions.addRecord(ret);

			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
			}
		} else {
			BSutils.sendMessage(player,
					BetterShop.getConfig().getString("insuffunds").
					replace("<item>", toBuy.coloredName()).
					replace("<amt>", String.valueOf(amt)).
					replace("<total>", String.valueOf(unitPrice * amt)).
					replace("<curr>", BetterShop.getConfig().currency()).
					replace("<priceper>", String.valueOf(unitPrice)).
					replace("<totcur>", BSEcon.format(unitPrice * amt)));
		}
		return ret;
	}

	private static UserTransaction _buyKit(Player player, Kit toBuy, int amt, double unitPrice) {
		UserTransaction ret = null;
		PlayerInventory inv = player.getInventory();
		KitItem items[] = toBuy.getKitItems();

		int maxBuy = ItemStackManip.amountCanHold(inv.getContents(), toBuy, !BetterShop.getConfig().usemaxstack);

		if (amt > maxBuy) {
			BSutils.sendMessage(player, String.format(BetterShop.getConfig().getString("outofroom").
					replace("<item>", "%1$s").
					replace("<amt>", "%2$d").
					replace("<priceper>", "%3$01.2f").
					replace("<leftover>", "%4$d").
					replace("<curr>", "%5$s").
					replace("<free>", "%6$d"), toBuy.coloredName(),
					amt, unitPrice, amt - maxBuy, BetterShop.getConfig().currency(), maxBuy));
			if (maxBuy == 0) {
				return null;
			}
			amt = maxBuy;
		}

		// now check if there are items avaliable for purchase
		long avail = -1;
		if (BetterShop.getConfig().useItemStock) {
			try {
				avail = BetterShop.getStock(player.getLocation()).getItemAmount(toBuy.ID());
			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
			}
			/*if (avail == -1) {
			BSutils.sendMessage(player, ChatColor.RED + "Failed to lookup an item stock listing");
			return true;
			} else */ if (avail == 0) {
				BSutils.sendMessage(player, BetterShop.getConfig().getString("outofstock").
						replace("<item>", toBuy.coloredName()));
				return null;
			} else if (amt > avail) {
				BSutils.sendMessage(player, String.format(BetterShop.getConfig().getString("lowstock").
						replace("<item>", toBuy.coloredName()).
						replace("<amt>", String.valueOf(avail))));
				amt = (int) avail;
			}
		}

		double cost = amt * unitPrice;
		if (cost == 0 || BSEcon.debit(player, cost)) {
			try {
				
				inv.setContents(ItemStackManip.add(player.getInventory().getContents(), toBuy, amt));

				if (BetterShop.getConfig().useItemStock) {
					BetterShop.getStock(player.getLocation()).changeItemAmount(toBuy.ID(), toBuy.Name(), -amt);
				}

				BSutils.sendFormttedMessage(player, "buymsg", toBuy.coloredName(), amt, cost);

				ret = new UserTransaction(toBuy.ID(), 0, toBuy.Name(), false, amt, unitPrice, player.getDisplayName());
				BetterShop.getTransactions(player.getLocation()).addRecord(ret);
			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
			}
		} else {
			BSutils.sendMessage(player, String.format(
					BetterShop.getConfig().getString("insuffunds").
					replace("<item>", "%1$s").
					replace("<amt>", "%2$d").
					replace("<total>", "%3$01.2f").
					replace("<curr>", "%5$s").
					replace("<priceper>", "%4$01.2f").
					replace("<totcur>", "%6$s"), toBuy.coloredName(),
					amt, cost, unitPrice, BetterShop.getConfig().currency(),
					BSEcon.format(unitPrice)));
		}
		return ret;
	}
} // end class BuyCommands

