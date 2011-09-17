/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: generic methods that i haven't moved to their own classes
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

package me.jascotty2.bettershop;

import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.ItemStockEntry;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.lib.bukkit.MinecraftChatStr;
//import org.bukkit.Server;
//import org.bukkit.plugin.Plugin;
//import org.bukkit.plugin.PluginManager;
//import org.bukkit.permissions.PermissionAttachmentInfo;

public class BSutils {

	public static boolean anonymousCheck(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, "Cannot execute that command, I don't know who you are!");
			return true;
		} else {
			return false;
		}
	}

	public static void sendFormttedMessage(Player player, String key, String item, int amt, double total) {

		BSutils.sendMessage(player, BetterShop.getConfig().getString(key).
				replace("<item>", item).
				replace("<amt>", Integer.toString(amt)).
				replace("<priceper>", String.format("%.2f", total / amt)).
				replace("<total>", String.format("%.2f", total)).
				replace("<curr>", BetterShop.getConfig().currency()).
				replace("<totcur>", BSEcon.format(total)));
		//price
		if (BetterShop.getConfig().publicmarket && BetterShop.getConfig().hasString("public" + key)) {
			BSutils.broadcastMessage(player, BetterShop.getConfig().getString("public" + key).
					replace("<item>", item).
					replace("<amt>", String.valueOf(amt)).
					replace("<priceper>", String.format("%.2f", total / amt)).
					replace("<total>", String.format("%.2f", total)).
					replace("<curr>", BetterShop.getConfig().currency()).
					replace("<totcur>", BSEcon.format(total)).
					replace("<player>", player.getDisplayName()),
					false);
		}
	}

	public static void sendMessage(CommandSender player, String s) {
		if (player != null) {
			if (s.contains("\n")) {
				String lns[] = s.split("\n");
				player.sendMessage(BetterShop.getConfig().getString("prefix") + lns[0]);
				for (int i = 1; i < lns.length; ++i) {
					player.sendMessage(lns[i]);
				}
			} else {
				player.sendMessage(BetterShop.getConfig().getString("prefix") + s);
			}
		}
	}

	public static void sendMessage(CommandSender player, String s, boolean isPublic) {
		if (!isPublic) {
			sendMessage(player, s);
		} else if (player != null) {
			broadcastMessage(player, s);
		}
	}

	public static void broadcastMessage(CommandSender player, String s) {
		if (player != null) {
			if (s.contains("\n")) {
				String lns[] = s.split("\n");
				player.getServer().broadcastMessage(BetterShop.getConfig().getString("prefix") + lns[0]);
				for (int i = 1; i < lns.length; ++i) {
					player.getServer().broadcastMessage(lns[i]);
				}
			} else {
				player.getServer().broadcastMessage(BetterShop.getConfig().getString("prefix") + s);
			}
		}
		BetterShopLogger.Log("(public announcement) " + s.replaceAll("\\\u00A7.", ""));
	}

	public static void broadcastMessage(CommandSender player, String s, boolean includePlayer) {
		if (player != null) {
			if (includePlayer) {
				broadcastMessage(player, s);
			} else {
				String name = player instanceof Player ? ((Player) player).getDisplayName() : "";
				for (Player p : player.getServer().getOnlinePlayers()) {
					if (!p.getDisplayName().equals(name)) {
						//player.getServer().broadcastMessage(BetterShop.getConfig().getString("prefix") + s);
						//p.sendMessage(BetterShop.getConfig().getString("prefix") + s);
						sendMessage(p, s);
					}
				}
				BetterShopLogger.Log("(public announcement) " + MinecraftChatStr.uncoloredStr(s));
			}
		}
	}

	/**
	 * all items in the user's inventory
	 * does not run check for tools, so tools with damage are returned separately
	 * @param player Player to check
	 * @param onlyInv if the lower 9 slots should be checked or not
	 * @return
	 */
	public static ArrayList<ItemStockEntry> getTotalInventory(Player player, boolean onlyInv) {
		if (player == null) {
			return null;
		}
		ArrayList<ItemStockEntry> inv = new ArrayList<ItemStockEntry>();

		ItemStack[] its = player.getInventory().getContents();
		for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
			if (its[i] != null && its[i].getAmount() > 0) {
				ItemStockEntry find = new ItemStockEntry(its[i]);
				int pos = inv.indexOf(find);
				if (pos >= 0) {
					inv.get(pos).AddAmount(its[i].getAmount());
				} else {
					inv.add(find);
				}
			}
		}

		return inv;
	}

	public static ArrayList<ItemStockEntry> getTotalInventory(Player player, boolean onlyInv, JItem toFind) {
		if (toFind == null) {
			return getTotalInventory(player, onlyInv);
		}// else
		return getTotalInventory(player, onlyInv, new JItem[]{toFind});
	}

	public static ArrayList<ItemStockEntry> getTotalInventory(Player player, boolean onlyInv, JItem[] toFind) {
		if (toFind == null || toFind.length == 0) {
			return getTotalInventory(player, onlyInv);
		} else if (player == null) {
			return null;
		}
		ArrayList<ItemStockEntry> inv = new ArrayList<ItemStockEntry>();

		ItemStack[] its = player.getInventory().getContents();
		for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
			if (its[i] != null && its[i].getAmount() > 0) {
				for (JItem it : toFind) {
					if (it != null && its[i] != null && it.equals(its[i])) {
						ItemStockEntry find = new ItemStockEntry(its[i]);
						int pos = inv.indexOf(find);
						if (pos >= 0) {
							inv.get(pos).AddAmount(its[i].getAmount());
						} else {
							inv.add(find);
						}
						break;
					}
				}
			}
		}
		return inv;
	}

	public static ArrayList<ItemStockEntry> getTotalInventory(Player player, boolean onlyInv, List<ItemStockEntry> toFind) {
		if (toFind == null) {
			return getTotalInventory(player, onlyInv);
		} else if (player == null) {
			return null;
		}
		ArrayList<ItemStockEntry> inv = new ArrayList<ItemStockEntry>();
		if (toFind.isEmpty()) {
			return inv;
		}
		ItemStack[] its = player.getInventory().getContents();
		for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
			if (its[i] != null && its[i].getAmount() > 0) {
				for (ItemStockEntry it : toFind) {
					if (it != null && its[i] != null && it.equals(its[i])) {
						int pos = inv.indexOf(it);
						if (pos >= 0) {
							inv.get(pos).AddAmount(its[i].getAmount());
						} else {
							inv.add(new ItemStockEntry(it));
						}
						break;
					}
				}
			}
		}
		return inv;
	}
	/*
	public static ArrayList<ItemStockEntry> getTotalSellableInventory(Player player, boolean onlyInv) {
	ArrayList<ItemStockEntry> inv = new ArrayList<ItemStockEntry>();
	try {
	ItemStack[] its = player.getInventory().getContents();
	for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
	if (its[i].getAmount() > 0 && BetterShop.getPricelist().isForSale(its[i])) {
	ItemStockEntry find = new ItemStockEntry(its[i]);
	int pos = inv.indexOf(find);
	if (pos >= 0) {
	inv.get(pos).AddAmount(its[i].getAmount());
	} else {
	inv.add(find);
	}
	}
	}
	} catch (Exception ex) {
	BetterShopLogger.Log(Level.SEVERE, ex);
	return null;
	}
	return inv;
	}//*/

	public static int amtHas(Player player, JItem toBuy) {
		int amt = 0;
		if (!toBuy.isEntity()) {
			PlayerInventory inv = player.getInventory();
			// don't search armor slots
			for (int i = 0; i <= 35; ++i) {
				ItemStack it = inv.getItem(i);
				if (toBuy.equals(it)) {
					amt += it.getAmount();
				}
			}
		}
		return amt;
	}

	public static int amtCanHold(Player player, JItem toBuy) {
		int canHold = 0, maxStack = BetterShop.getConfig().usemaxstack ? toBuy.getMaxStackSize() : 64;
		if (!toBuy.isEntity()) {
			PlayerInventory inv = player.getInventory();
			// don't search armor slots
			for (int i = 0; i <= 35; ++i) {
				ItemStack it = inv.getItem(i);
				if (it == null || (toBuy.equals(it) && it.getAmount() < maxStack) || it.getAmount() == 0) {
					canHold += maxStack - it.getAmount();
				}
			}
		} else {
			canHold = BetterShop.getConfig().maxEntityPurchase;
		}
		return canHold;
	}

}
