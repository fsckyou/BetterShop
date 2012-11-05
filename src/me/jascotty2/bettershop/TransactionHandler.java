/**
 * Copyright (C) 2012 Jacob Scott <jascottytechie@gmail.com>
 * Description: static methods used when buying/selling
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

import me.jascotty2.bettershop.enums.BetterShopPermission;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import me.jascotty2.bettershop.regionshops.RegionShopManager;
import me.jascotty2.bettershop.shop.Shop;
import me.jascotty2.bettershop.enums.ShopMethod;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.inventory.ItemStackManip;
import me.jascotty2.lib.bukkit.item.Kit;

public class TransactionHandler {

	private final BSEcon economy;
	private final RegionShopManager shopManager;

	public TransactionHandler(BSEcon economy, RegionShopManager shopManager) {
		if (economy == null || shopManager == null) {
			throw new IllegalArgumentException("TransactionHandler Constructor cannot be null");
		}
		this.economy = economy;
		this.shopManager = shopManager;
	}

	/**
	 * execute a shop transaction <br>
	 * note: this method assumes that the player has permission to access the shop at their location
	 * @param player
	 * @param action
	 * @return 
	 */
	public TransactionResult execute(Player player, PlayerTransation action, ShopMethod method) {
		if (player == null || action == null) {
			throw new IllegalArgumentException("TransactionHandler execute arguments cannot be null");
		}

		// check if has permission to use the shop
		if (method != ShopMethod.PLUGIN) {
			if (!BSPermissions.hasPermission(player, action.is_selling ? BetterShopPermission.USER_SELL : BetterShopPermission.USER_BUY)) {
				return new TransactionResult(BetterShop.getSettings().getString("permdeny").replace("<perm>",
						(action.is_selling ? BetterShopPermission.USER_SELL : BetterShopPermission.USER_BUY).toString()));
			}
			// check if the shop is accessable from here
			if ((method == ShopMethod.COMMAND || method == ShopMethod.SPOUT)
					&& !shopManager.locationHasShop(player.getLocation())
					&& !shopManager.isCommandShopEnabled(player.getLocation())) {
				return new TransactionResult(BetterShop.getSettings().getString("regionShopDisabled"));
			}
		}

		// lookup item(s)
		if (action.items == null || action.items.length == 0) {
			if (action.itemSearch == null || action.itemSearch.trim().length() == 0) {
				return new TransactionResult("no item to lookup provided");
			}
			if (JItemDB.isCategory(action.itemSearch)) {
				action.items = JItemDB.getItemsByCategory(action.itemSearch);
			} else {
				action.items = new JItem[1];
				action.items[0] = JItemDB.findItem(action.itemSearch);
			}
			if (action.items == null || action.items.length == 0 || action.items[0] == null) {
				return new TransactionResult(BetterShop.getSettings().getString("unkitem").replace("<item>", action.itemSearch));
			}
		}
		TransactionResult result = new TransactionResult();

		// check if has permission to purchase item
		if (!action.is_selling && !BSPermissions.hasPermission(player, BetterShopPermission.ADMIN_ILLEGAL)) {
			String cantBuy = "";
			int good = 0;
			for (int i = 0; i < action.items.length; ++i) {
				if (action.items[i] != null && !action.items[i].IsLegal()) {
					if (action.items[i] != null) {
						cantBuy += (cantBuy.length() > 0 ? ", " : "") + action.items[i].coloredName();
						action.items[i] = null;
					}
				} else {
					++good;
				}
			}
			if (!cantBuy.isEmpty()) {
				result.addError(BetterShop.getSettings().getString("illegalbuy").replace("<item>", cantBuy));
			}
			if (good == 0) {
				return result;
			}
		}

		Shop shop = shopManager.getShop(method == ShopMethod.PLUGIN ? null : player.getLocation());

		// check if the item(s) are sold/accepted by this shop
		try {
			String reject = "";
			int good = 0;
			for (int i = 0; i < action.items.length; ++i) {
				if (action.items[i] != null && !(action.is_selling
						? shop.pricelist.isForSale(action.items[i])
						: shop.pricelist.canBuy(action.items[i]))) {
					if (action.items[i] != null) {
						reject += (reject.length() > 0 ? ", " : "") + action.items[i].coloredName();
						action.items[i] = null;
					}
				} else {
					++good;
				}
			}
			if (!reject.isEmpty()) {
				result.addError(BetterShop.getSettings().getString(
						action.is_selling ? "donotwant" : "notforsale").replace("<item>", reject));
			}
			if (good == 0) {
				return result;
			}
		} catch (Exception e) {
			result.addError("Unexpected Error while accessing Pricelist: " + e.getMessage());
			return result;
		}

		PlayerInventory inv = player.getInventory();
		// now check how many can buy/sell
		// invCount becomes the max of any one item that the player can buy
		int invCount = 0;
		if (action.is_selling) {
			for (JItem it : action.items) {
				if (it != null && !it.isEntity()) {
					// don't count armor slots
					int amt = ItemStackManip.count(inv.getContents(), it, 0, 35);
					if (amt > invCount) {
						invCount = amt;
					}
				}
			}
		} else {
			// buying
			invCount = ItemStackManip.amountCanHold(inv.getContents(),
					new Kit(action.items), !BetterShop.getSettings().usemaxstack);

		}
		int stockAmt = 0;
		// check amount in stock
		if (BetterShop.getSettings().useItemStock) {
			try {
				for (JItem it : action.items) {
					if (it != null) {
						long st = action.is_selling ? shop.stock.freeStockRemaining(it) : shop.stock.getItemAmount(it);
						if (st > stockAmt) {
							if (st >= Integer.MAX_VALUE) {
								stockAmt = Integer.MAX_VALUE;
								break;
							} else {
								stockAmt = (int) st;
							}
						} else if (st == -1) {
							stockAmt = Integer.MAX_VALUE;
							break;
						}
					}
				}
			} catch (Exception ex) {
				result.addError("Unexpected Error while accessing Shop Stock: " + ex.getMessage());
				return result;
			}
		} else {
			stockAmt = Integer.MAX_VALUE;
		}
		
		int amtCan = 0;
		if(action.is_selling) {
			// if add player shops, test if the player can afford to buy this many
			amtCan = Integer.MAX_VALUE;
		} else {
			// check if the player can afford to buy this many items
			 
		}
		result.all_ok = true;
		return result;
	}

	public static class TransactionResult {

		public boolean all_ok = false;
		public int end_amount;
		public float end_price;
		public String error_message;

		public TransactionResult() {
		}

		public TransactionResult(String errorMessage) {
			this.error_message = errorMessage;
		}

		public void addError(String error) {
			if (error_message == null || error_message.isEmpty()) {
				error_message = error;
			} else {
				error_message += "\n" + error;
			}
		}
	}
}
