/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: for storing info about a shop sign
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
package me.jascotty2.bettershop.signshop;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.commands.SellCommands;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import me.jascotty2.bettershop.shop.Shop;
import me.jascotty2.bettershop.utils.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.util.Str;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopSign {

	public final static String SIGN_TEXT = "[BetterShop]";
	private final Sign sign;
	public String itemName; // final
	JItem item = null, catItems[] = null;
	boolean isInv, inHand, isBuy, isCategory;
	double customPrice = -1;
	int amount;

	public ShopSign(Sign s) {
			
		if (s == null) {
			throw new IllegalArgumentException("Sign cannot be null!");
		} else if (!ChatColor.stripColor(s.getLine(0)).equalsIgnoreCase(SIGN_TEXT)) {
			throw new IllegalArgumentException("Invalid Sign!");
		}
		this.sign = s;
		//try {
		String action = ChatColor.stripColor(sign.getLine(1).trim()).replace("  ", " ");

		// quick check for valid sign action
		if (Str.count(action, " ") > 1
				|| (action.contains(" ") && !Str.startIsIn(action, new String[]{
					"buy ", "buyall ", "buystack ",
					"sell ", "sellall ", "sellstack "}))
				|| (!action.contains(" ") && !Str.startIsIn(action, new String[]{
					"buy", "buyall", "buystack",
					"sell", "sellall", "sellstack"}))) {
			throw new IllegalArgumentException("Invalid Sign! (invalid action: " + action + ")");
		}
		isBuy = action.toLowerCase().startsWith("buy");

		// first find the item(s)
		String searchItem = ChatColor.stripColor(sign.getLine(2).toLowerCase().replace(" ", ""));
		JItem toAdd[];
		// check if is a category
		if(!searchItem.isEmpty() && (itemName = JItemDB.findCategory(searchItem)) != null) {
			toAdd = JItemDB.getItemsByCategory(searchItem);
			catItems = toAdd;
			isCategory = true;
		} else {
			toAdd = searchItem.isEmpty() ? new JItem[]{null} : new JItem[]{JItemDB.findItem(searchItem)};
			if(toAdd[0] == null) toAdd = JItemDB.findItems(searchItem);
			if (toAdd.length == 1 && toAdd[0] != null) {
				item = toAdd[0];
				itemName = item.Name();
			} else if (toAdd.length > 1) {
//				System.out.println("matches for " + searchItem);
//				for(JItem  i : toAdd){
//					System.out.println(i == null ? "null " : i.Name());
//				}
				throw new IllegalArgumentException("Invalid Sign! (multiple items match query)");
			} else {
				itemName = null;
			}
		}
		
		isInv = searchItem.equals("inv") || action.contains("inv");
		inHand = searchItem.equals("hand") || searchItem.equals("inhand") || action.contains("hand");

		if (action.contains("all")) {
			amount = -1;
		} else {
			String amt = action.contains(" ")
					? action.substring(action.lastIndexOf(' ')).trim().toLowerCase() : "1";
			amount = CheckInput.GetInt(amt, -1);
			if (action.contains("stack")) {
				if (item == null && catItems == null) {
					// can't buy/sell stacks of nothing
					throw new IllegalArgumentException("Invalid Sign! (invalid item: " + searchItem + ")");
				}
				amount = item != null ? (amount <= 0
						? (BetterShop.getSettings().usemaxstack ? item.MaxStackSize() : 64)
						: amount * (BetterShop.getSettings().usemaxstack ? item.MaxStackSize() : 64))
						: 64;
			}

			if (amount <= 0) {
				throw new IllegalArgumentException("Invalid Sign! (bad amount: " + amt + ")");
			}
		}
		
		// now check if there's a custom price for the transaction
		String customPriceStr = ChatColor.stripColor(sign.getLine(3).trim());
		if (!customPriceStr.isEmpty()) {
			customPrice = CheckInput.ExtractDouble(customPriceStr, -1);
			if(amount > 0){
				customPrice /= amount;
			}
		}

		if (isBuy) {
			if (item == null && catItems == null) {
				// can't buy nothing
				throw new IllegalArgumentException("Invalid Sign! (invalid item: " + searchItem + ")");
			}
		} else {
			// is selling
			if ((amount > 0 && item == null && catItems == null && inHand == false)
					|| (item != null && item.ID() <= 0)) {
				// can't sell nothing
				throw new IllegalArgumentException("Invalid Sign! (invalid item: " + searchItem + ")");
			} else if (item != null && item.isKit()) {
				throw new IllegalArgumentException("Invalid Sign! (Kits cannot be sold)");
			} else if (item != null && item.isEntity()) {
				throw new IllegalArgumentException("Invalid Sign! (Entities cannot be sold)");
			}
		}
		
//		} catch (Exception e) {
//			Logger.getAnonymousLogger().warning(e.toString() + " " + e.getMessage() + "\n" + Str.getStackStr(e));
//		}
	}

	public void updateColor() {

		//sign.setLine(0, BetterShop.getConfig().activeSignColor + SIGN_TEXT);

		boolean up = false;
		if (!sign.getLine(0).startsWith(BetterShop.getSettings().activeSignColor)) {
			sign.setLine(0, BetterShop.getSettings().activeSignColor + SIGN_TEXT);
			up = true;
		}
		if (BetterShop.getSettings().signItemColor && item != null
				&& item.color != null && !sign.getLine(2).startsWith(item.color)) {
			if (BetterShop.getSettings().signItemColorBWswap) {
				if (ChatColor.BLACK.toString().equals(item.color)) {
					if (!sign.getLine(2).startsWith(ChatColor.WHITE.toString())) {
						sign.setLine(2, ChatColor.WHITE + ChatColor.stripColor(sign.getLine(2)));
						up = true;
					}
				} else if (ChatColor.WHITE.toString().equals(item.color)) {
					if (!sign.getLine(2).startsWith(ChatColor.BLACK.toString())) {
						sign.setLine(2, ChatColor.BLACK + ChatColor.stripColor(sign.getLine(2)));
						up = true;
					}
				} else if (!sign.getLine(2).startsWith(item.color)) {
					sign.setLine(2, item.color + ChatColor.stripColor(sign.getLine(2)));
					up = true;
				}
			}
		}
		if (up) {
			sign.update();
		}
	}

	public JItem getItem() {
		return item;
	}

	public JItem getItem(Player p) {
		return inHand && p.getItemInHand() != null
				&& p.getItemInHand().getAmount() > 0
				? JItemDB.GetItem(p.getItemInHand()) : null;
	}

	double priceCheck(Player player) throws SQLException, Exception {
		if (customPrice >= 0) {
			return customPrice;
		}
		// now find the price of the transaction
		double price = -1;
		Shop shop = BetterShop.getShop(sign.getBlock().getLocation());
		if (isBuy) {
			int buyAmt = amount;
			if (item != null && !item.IsLegal()
					&& (BetterShop.getSettings().allowbuyillegal
					|| BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL, false))) {
				BSutils.sendMessage(player, BetterShop.getSettings().getString("illegalbuy").
						replace("<item>", item.coloredName()));
				return -1;
			} else if (item != null && !shop.pricelist.isForSale(item)) {
				BSutils.sendMessage(player,
						BetterShop.getSettings().getString("notforsale").
						replace("<item>", item.coloredName()));
				return -1;
			}
			if (amount < 0) { // buyall
				// get max. can buy
				buyAmt = BSutils.amtCanHold(player, item);
				long avail = -1;

				if (shop.config.useStock()) {
					try {
						avail = shop.stock.getItemAmount(item);
					} catch (Exception ex) {
						BetterShopLogger.Log(Level.SEVERE, ex, false);
					}
					if (avail == 0) {
						BSutils.sendMessage(player, BetterShop.getSettings().getString("outofstock").
								replace("<item>", item.coloredName()));
						return -1;
					} else if (avail >= 0 && amount > avail) {
						BSutils.sendMessage(player, BetterShop.getSettings().getString("lowstock").
								replace("<item>", item.coloredName()).
								replace("<amt>", String.valueOf(avail)));
						buyAmt = (int) avail;
					}
				}
			}
			// should now have amount to buy
			price = shop.pricelist.itemBuyPrice(player, item, buyAmt);
		} else {
			// is selling
			if (amount < 0) {
				//playerAmount = 0;
				List<ItemStack> sellable = SellCommands.getCanSell(player,
						isInv, item == null ? null : item == null ? catItems : new JItem[]{item}, customPrice);
				for (ItemStack it : sellable) {
					//playerAmount += it.getAmount();
					price += shop.pricelist.itemSellPrice(player, it, it.getAmount());
				}
			} else {
				//playerAmount = BSutils.amtHas(player, item);
				price = shop.pricelist.itemSellPrice(player, item, BSutils.amtHas(player, item));
			}
		}
		return price;
	}

	public Sign getSign() {
		return sign;
	}

	public double getCustomPrice() {
		return customPrice;
	}
	
} // end class SignItem

