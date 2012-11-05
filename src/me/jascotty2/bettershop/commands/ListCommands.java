/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: commands & methods for viewing items & info in the shop
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

import java.util.List;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.BSEcon;
import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import me.jascotty2.bettershop.shop.Shop;
import me.jascotty2.bettershop.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.lib.bukkit.MinecraftChatStr;
import me.jascotty2.lib.bukkit.commands.Command;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.item.PriceListItem;
import me.jascotty2.lib.io.CheckInput;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author jacob
 */
public class ListCommands {

	@Command(commands = {"shoplist", "slist", "sl"},
	aliases = {"list", "l", "ls"},
	desc = "Lists prices for the shop",
	permissions = {"BetterShop.user.list"})
	public static boolean list(CommandSender player, String[] s) {
		int pagenum = 1;
		if ((s.length > 1)) {
			return false;
		} else if (s.length == 1) {
			if (s[0].equalsIgnoreCase("full") || s[0].equalsIgnoreCase("all")) {
				pagenum = -1;
			} else if (s[0].equalsIgnoreCase("item") || s[0].equalsIgnoreCase("items")) {
				return listitems(player, null);
			} else if (s[0].equalsIgnoreCase("kits")) {
				return listkits(player, null);
			} else if (!CheckInput.IsInt(s[0])) {
				BSutils.sendMessage(player, "That's not a page number.");
				return false;
			} else {
				pagenum = CheckInput.GetInt(s[0], 1);
			}
		}

		Shop shop = BetterShop.getShop(player);
		if (shop == null) {
			BSutils.sendMessage(player, ChatColor.RED + "Pricelist Error: Notify Server Admin");
		} else {
			for (String line : shop.pricelist.GetShopListPage(pagenum, player, shop.stock)) {
				BSutils.sendMessage(player,
						line.replace("<curr>", BetterShop.getSettings().currency()));
			}
		}

		return true;
	}

	@Command(commands = {"shoplistitems", "shopitems", "sitems", "slistitems"},
	aliases = {"items", "i", "li", "listitems"},
	desc = "Lists items registered in the shop",
	permissions = {"BetterShop.user.list"})
	public static boolean listitems(CommandSender player, String[] s) {
		if (s != null && s.length > 1) {
			return false;
		}
		try {
			List<String> items = BetterShop.getPricelist(player).getItemList(
					BetterShop.getSettings().allowbuyillegal || BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false));
			StringBuilder output = new StringBuilder("\u00A72");
			if (items != null && items.size() > 0) {
				for (int i = 0; i < items.size(); ++i) {
					output.append(items.get(i));
					if (i + 1 < items.size()) {
						output.append("\u00A72, ");
					}
				}
			}
			BSutils.sendMessage(player, output.toString());
			return true;
		} catch (Exception ex) {
			BetterShopLogger.Severe(ex);
		}

		BSutils.sendMessage(player, ChatColor.RED + "An Error Occurred while looking up an item.. attemping to reload..");
		if (AdminCommands.load(null, null)) {
			// ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
			BSutils.sendMessage(player, "Success! Please try again.. ");
		} else {
			BSutils.sendMessage(player, ChatColor.RED + "Failed! Please let an OP know of this error");
		}
		return true;
	}

	@Command(
	commands = {"shoplistkits", "shopkits", "skits", "slistits"},
	aliases = {"kits", "k", "listkits"},
	desc = "Lists kits in the shop",
	permissions = {"BetterShop.user.list"})
	public static boolean listkits(CommandSender player, String[] s) {
		try {
			BSutils.sendMessage(player, "Kit listing:");
			String kitNames = "";
			for (JItem i : BetterShop.getPricelist(player).getItems(BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL))) {
				if (i.isKit()) {
					if (kitNames.length() > 0) {
						kitNames += ", ";
					}
					kitNames += i.coloredName();
				}
			}
			BSutils.sendMessage(player, kitNames);
			return true;
		} catch (Exception ex) {
			BetterShopLogger.Severe(ex);
			BSutils.sendMessage(player, "Error looking up an item.. Attempting DB reload..");
			if (AdminCommands.load(null, null)) {
				// ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
				BSutils.sendMessage(player, "Success! Please try again.. ");
			} else {
				BSutils.sendMessage(player, ChatColor.RED + "Failed! Please let an OP know of this error");
			}
			return true;
		}
	}

	@Command(
	commands = {"shoplistalias", "shopalias", "salias", "sa"},
	aliases = {"alias", "a", "listalias", "aliases", "listaliases"},
	desc = "Show the accepted aliases for an item",
	permissions = {"BetterShop.user.help"})
	public static boolean listAlias(CommandSender player, String[] s) {
		if (s.length != 1) {
			return false;
		}
		JItem it = JItemDB.findItem(s[0]);
		if (it == null) {
			BSutils.sendMessage(player, BetterShop.getSettings().getString("unkitem").
					replace("<item>", s[0]));
		} else {
			StringBuilder aliases = new StringBuilder();
			for (String a : it.Aliases()) {
				aliases.append(a).append(", ");
			}
			if (aliases.length() > 1) {//.indexOf(",") != -1) {
				aliases.delete(aliases.length() - 2, aliases.length());
			}

			BSutils.sendMessage(player,
					MinecraftChatStr.strWordWrap(
					BetterShop.getSettings().getString("listalias").
					replace("<item>", it.coloredName()).replace("<alias>", aliases.toString())));
		}
		return true;
	}

	@Command(
	commands = {"shopcheck", "scheck", "sc"},
	aliases = {"check", "c", "price", "lookup"},
	desc = "Check prices for an item",
	permissions = {"BetterShop.user.check"})
	public static boolean check(CommandSender player, String[] s) {
		if (s == null || s.length == 0 || s.length > 2) {
			return false;
		} else if (s.length > 1 && !CheckInput.IsInt(s[1]) && !s[1].equalsIgnoreCase("all")) {
			BSutils.sendMessage(player, "Invalid amount");
			return true;
		}

		boolean canBuyIllegal = BetterShop.getSettings().allowbuyillegal
				|| BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false);

		Shop shop = BetterShop.getShop(player);

		try {
			if (s[0].equalsIgnoreCase("all")) {
				List<ItemStack> sellable = SellCommands.getCanSell((Player) player, false, null, -1);
				if (!sellable.isEmpty()) {
					PriceListItem price = new PriceListItem();
					price.buy = price.sell = 0;
					String name = "("; // price.name = "(";
					int numCheck = 0;
					for (ItemStack ite : sellable) {
						numCheck += ite.getAmount();
						JItem it = JItemDB.findItem(ite);
						PriceListItem tprice = shop.pricelist.getItemPrice(it);
						if (tprice != null) {
							price.buy += tprice.buy > 0 ? tprice.buy : 0;
							price.sell += tprice.sell > 0 ? tprice.sell : 0;
							if (name.length() > 1) {
								name += ", " + it.coloredName();
							} else {
								name += it.coloredName();
							}
						}
					}
					name += ")";
					BSutils.sendMessage(player, String.format(
							BetterShop.getSettings().getString(numCheck == 1 ? "pricecheck" : "multipricecheck").
							replace("<buyprice>", "%1$s").
							replace("<sellprice>", "%2$s").
							replace("<item>", "%3$s").
							replace("<curr>", "%4$s").
							replace("<buycur>", "%5$s").
							replace("<sellcur>", "%6$s").
							replace("<avail>", "%7$s").
							replace("<amt>", "%8$s"),
							(price.IsLegal() || canBuyIllegal) && price.buy >= 0 ? price.buy : "No",
							price.sell >= 0 ? price.sell : "No",
							name,
							BetterShop.getSettings().currency(),
							(price.IsLegal() || canBuyIllegal) && price.buy >= 0
							? BSEcon.format(price.buy) : "No",
							price.sell >= 0 ? BSEcon.format(price.sell) : "No",
							"?",
							numCheck));
				} else {
					BSutils.sendMessage(player, "no sellabel items in your inventory");
				}
				return true;
			} else {
				JItem lookup[] = JItemDB.findItems(s[0]);
				if (lookup == null || lookup.length == 0 || lookup[0] == null) {
					lookup = JItemDB.getItemsByCategory(s[0]);
					if (lookup == null || lookup.length == 0 || lookup[0] == null) {
						BSutils.sendMessage(player, BetterShop.getSettings().getString("unkitem").
								replace("<item>", s[0]));
						return true;
					}
				}

				int inStore = 0,
						numCheck = s.length > 1 && !s[1].equalsIgnoreCase("all") ? CheckInput.GetInt(s[1], 1) : 1;
				for (JItem i : lookup) {
					PriceListItem price = shop.pricelist.getItemPrice(i);
					if (price != null) {
						++inStore;
						BSutils.sendMessage(player, String.format(
								BetterShop.getSettings().getString(numCheck == 1 ? "pricecheck" : "multipricecheck").
								replace("<buyprice>", "%1$s").
								replace("<sellprice>", "%2$s").
								replace("<item>", "%3$s").
								replace("<curr>", "%4$s").
								replace("<buycur>", "%5$s").
								replace("<sellcur>", "%6$s").
								replace("<avail>", "%7$s").
								replace("<amt>", "%8$s"),
								(price.IsLegal() || canBuyIllegal) && price.buy >= 0 ? numCheck * price.buy : "No",
								price.sell >= 0 ? numCheck * price.sell : "No",
								i.coloredName(),
								BetterShop.getSettings().currency(),
								(price.IsLegal() || canBuyIllegal) && price.buy >= 0
								? BSEcon.format(numCheck * price.buy) : "No",
								price.sell >= 0 ? BSEcon.format(numCheck * price.sell) : "No",
								!shop.config.useStock() || shop.stock.getItemAmount(i) < 0 ? "INF" : shop.stock.getItemAmount(i),
								numCheck));

					} else if (lookup.length <= 5) { // only show nolisting if result page is 5 or less lines
						BSutils.sendMessage(player,
								String.format(BetterShop.getSettings().getString("nolisting").
								replace("<item>", "%s"), i.coloredName()));
					}
				}
				if (lookup.length > 5 && inStore == 0) {
					BSutils.sendMessage(player, String.format("No Sellable items found under \"%s\"", s[0]));
				}
				return true;

			}
		} catch (Exception ex) {
			BetterShopLogger.Severe(ex);
		}
		BSutils.sendMessage(player, ChatColor.RED + "An Error Occurred while looking up an item.. attemping to reload..");
		if (AdminCommands.load(null, null)) {
			// ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
			BSutils.sendMessage(player, "Success! Please try again.. ");
		} else {
			BSutils.sendMessage(player, ChatColor.RED + "Failed! Please let an OP know of this error");
		}
		return true;
	}
} // end class ListCommands

