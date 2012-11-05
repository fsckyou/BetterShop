/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: for adding a sign interface to bettershop
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

import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.item.ItemStockEntry;
import me.jascotty2.bettershop.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import me.jascotty2.bettershop.BSConfig;
import me.jascotty2.bettershop.BSEcon;
import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.commands.BuyCommands;
import me.jascotty2.bettershop.commands.SellCommands;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import me.jascotty2.bettershop.shop.Shop;
import me.jascotty2.lib.bukkit.inventory.ItemStackManip;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * @author jacob
 */
public class BSSignShop implements Listener {

	final static long signResWait = 5000;
	final BetterShop plugin;
	final SignDB signsd;
	final HashMap<Player, Long> playerInteractTime = new HashMap<Player, Long>();
	public final SignRestore checkSigns;

	public BSSignShop(BetterShop shop) {
		plugin = shop;
		signsd = new SignDB(plugin.getServer());
		checkSigns = new SignRestore(plugin, signsd);

	} // end default constructor

	public void registerEvents() {
		Plugin bs = BetterShop.getPlugin();
		PluginManager pm = bs.getServer().getPluginManager();

		pm.registerEvents(this, bs);
		pm.registerEvents(checkSigns, bs);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled() || !BetterShop.getSettings().signShopEnabled) {
			return;
		}

		if (event.getClickedBlock() != null
				&& (event.getClickedBlock().getType() == Material.WALL_SIGN
				|| event.getClickedBlock().getType() == Material.SIGN_POST)) {
			Sign clickedSign = (Sign) event.getClickedBlock().getState();
			Player player = event.getPlayer();
			if (ChatColor.stripColor(clickedSign.getLine(0)).equalsIgnoreCase(ShopSign.SIGN_TEXT)) {
				BetterShop.setLastCommand("Sign: [" + clickedSign.getLine(1) + "][" + clickedSign.getLine(2) + "][" + clickedSign.getLine(3) + "]");
				try {
					event.setCancelled(event.getAction() == Action.RIGHT_CLICK_BLOCK);
					// sign click flood protect
					Long lt = playerInteractTime.get(player);
					if (lt != null && System.currentTimeMillis() - lt < BSConfig.signInteractWait) {
						return;
					}
					playerInteractTime.put(player, System.currentTimeMillis());
					// general sign info
					Shop shop = BetterShop.getShop(event.getClickedBlock().getLocation());
					ShopSign signInfo = signsd.getSignShop(event.getClickedBlock().getLocation());
					boolean run = event.getAction() == Action.RIGHT_CLICK_BLOCK;
					if (signInfo != null) {
						// pricecheck
						String itemN = signInfo.getItem() != null ? signInfo.getItem().coloredName() : "";
						try {
							int numCheck = 0;
							double total = 0;
							if (signInfo.isBuy) {
								if (signInfo.item != null) {
									if (run) {
//										if (signInfo.amount > 0) {// not all
										BuyCommands.buyItem(player, signInfo.item, signInfo.amount, signInfo.getCustomPrice());
//										} else {
//											BuyCommands.buyAllItem(player, signInfo.item, signInfo.getCustomPrice());
//										}
										player.updateInventory(); // may be depricated, but only thing i can get to work :(
										return;
									}
									if (signInfo.getCustomPrice() >= 0) {
										int canHold = BSutils.amtCanHold(player, signInfo.item);
										int canAfford = 0;
										double bal = BSEcon.getBalance(player);
										long stock = -1;
										try {
											stock = shop.stock.getItemAmount(signInfo.item);
										} catch (Exception ex) {
											BetterShopLogger.Severe("Error in Stock Database", ex, false);
										}

										long amt = signInfo.getCustomPrice() == 0 ? Long.MAX_VALUE
												: (long) (bal / signInfo.getCustomPrice());
										if (amt > stock) {
											amt = stock;
										}
										if (amt > Integer.MAX_VALUE) {
											canAfford = Integer.MAX_VALUE;
										} else {
											canAfford = (int) amt;
										}
										numCheck = canAfford > canHold ? canHold : canAfford;
									} else {
										numCheck = shop.pricelist.getAmountCanBuy(player, signInfo.item);
									}
									if (signInfo.amount > 0) { // not all
										if (numCheck > signInfo.amount) {
											numCheck = signInfo.amount;
										}
									}
									if (numCheck == 0 && shop.config.useStock() && shop.stock.getItemAmount(signInfo.item) == 0) {
										BSutils.sendMessage(player, BetterShop.getSettings().getString("outofstock").
												replace("<item>", signInfo.item.coloredName()));
										return;
									}
									total = signInfo.getCustomPrice() >= 0 ? signInfo.getCustomPrice() * numCheck
											: shop.pricelist.itemBuyPrice(player, signInfo.item, numCheck);
								} else {
									// category
									List<ItemStockEntry> buy = BuyCommands.getCanBuy(player, signInfo.catItems, signInfo.getCustomPrice());
									if (buy.isEmpty() && !run) {
										// can't afford, so display full price
										for (JItem it : signInfo.catItems) {
											if (it != null && shop.pricelist.canBuy(it)) {
												long avail = shop.config.useStock() ? shop.stock.freeStockRemaining(it) : -1;
												if (avail != 0) {
													buy.add(new ItemStockEntry(it, signInfo.amount > avail && avail > 0 ? avail : signInfo.amount));
												}
											}
										}
									}
									JItem[] toBuy = new JItem[buy.size()];
									int n = 0;
									for (ItemStockEntry it : buy) {
										if (signInfo.amount > 0) { // not all
											if (it.amount > signInfo.amount) {
												it.amount = signInfo.amount;
											}
										}
										JItem j = JItemDB.GetItem(it.itemNum, (byte) it.itemSub);
										toBuy[n++] = j;
										itemN += j.coloredName() + ", ";
										if (run) {
											//BuyCommands.buyItem(player, JItemDB.GetItem(it.itemNum, (byte) it.itemSub), (int) it.amount, signInfo.getCustomPrice());
										} else {
											numCheck += it.amount;
											total += signInfo.getCustomPrice() >= 0 ? signInfo.getCustomPrice() * it.amount
													: shop.pricelist.itemBuyPrice(player, it.itemNum, (byte) it.itemSub, (int) it.amount);
										}
									}
									if (run) {
										//BuyCommands.buyItem(player, toBuy, signInfo.amount, signInfo.getCustomPrice());
										BuyCommands.buyItem(player, buy, signInfo.getCustomPrice());
										player.updateInventory(); // may be depricated, but only thing i can get to work :(
										return;
									}
									if (itemN.length() > 0) {
										itemN = itemN.substring(0, itemN.length() - 2);
										if (itemN.contains(",")) {
											itemN = "(" + itemN + ")";
										}
									}
								}
							} else {
								// selling
								if (signInfo.item != null) {
									numCheck = ItemStackManip.count(event.getPlayer().getInventory().getContents(), signInfo.item);
									if (signInfo.amount > 0 && numCheck > signInfo.amount) {
										numCheck = signInfo.amount;
									}
									if (run) {
										SellCommands.sellItems(player, signInfo.isInv, signInfo.item, numCheck, signInfo.getCustomPrice());
										player.updateInventory(); // may be depricated, but only thing i can get to work :(
										return;
									}
									total = signInfo.getCustomPrice() >= 0 ? signInfo.getCustomPrice() * numCheck
											: shop.pricelist.itemSellPrice(player, signInfo.item, numCheck);
								} else {
									if (run) {
										if (signInfo.catItems != null) {
											SellCommands.sellItems(player, signInfo.isInv,
													signInfo.catItems, signInfo.amount, signInfo.getCustomPrice());
										} else if (signInfo.inHand) {
											ItemStack hand = player.getItemInHand();
											if (hand == null || hand.getAmount() == 0) {
												BSutils.sendMessage(event.getPlayer(), "you don't have anything in your hand");
												return;
											}
											SellCommands.sellItems(player, signInfo.isInv,
													JItemDB.GetItem(hand), -1, signInfo.getCustomPrice());
										} else {
											SellCommands.sellItems(player, signInfo.isInv,
													null, signInfo.getCustomPrice());
										}
										player.updateInventory(); // may be depricated, but only thing i can get to work :(
										return;
									} else {
										List<ItemStack> sellable;
										if (signInfo.catItems != null) {
											sellable = SellCommands.getCanSell(player, signInfo.isInv, signInfo.catItems, signInfo.customPrice);
										} else if (signInfo.inHand) {
											ItemStack hand = player.getItemInHand();
											if (hand == null || hand.getAmount() == 0) {
												BSutils.sendMessage(event.getPlayer(), "you don't have anything in your hand");
												return;
											}
											sellable = SellCommands.getCanSell(player, signInfo.isInv, new JItem[]{JItemDB.GetItem(hand)}, signInfo.customPrice);
										} else {
											sellable = SellCommands.getCanSell(player, signInfo.isInv, null, signInfo.customPrice);
										}
										for (ItemStack ite : sellable) {
											if (signInfo.amount > 0) { // not all
												if (ite.getAmount() > signInfo.amount) {
													ite.setAmount(signInfo.amount);
												}
											}
											numCheck += ite.getAmount();
											JItem it = JItemDB.GetItem(ite);
											itemN += it.coloredName() + ", ";
											total += signInfo.getCustomPrice() >= 0 ? signInfo.getCustomPrice() * ite.getAmount()
													: shop.pricelist.itemSellPrice(player, it.ID(), (byte) it.Data(), ite.getAmount());
										}
									}
								}
							}
							if (itemN.endsWith(", ")) {
								itemN = itemN.substring(0, itemN.length() - 2);
								if (itemN.contains(",")) {
									itemN = "(" + itemN + ")";
								}
							}
							BSutils.sendMessage(event.getPlayer(),
									BetterShop.getSettings().getString(signInfo.isBuy ? "multipricecheckbuy" : "multipricechecksell"). //numCheck == 1 ? "pricecheck" : "multipricecheck"
									replace(signInfo.isBuy ? "<buyprice>" : "<sellprice>", String.format("%.2f", total /*numCheck > 0 ? total / numCheck : 0*/)).
									replace("<item>", itemN).
									replace("<curr>", BetterShop.getSettings().currency()).
									replace(signInfo.isBuy ? "<buycur>" : "<sellcur>", BSEcon.format(total /*numCheck > 0 ? total / numCheck : 0*/)).
									replace("<amt>", String.valueOf(numCheck)));

						} catch (Exception ex) {
							BetterShopLogger.Log(Level.SEVERE, ex);
							BSutils.sendMessage(event.getPlayer(), "Failed to lookup the price");
						}

					} else if (BSPermissions.hasPermission(event.getPlayer(),
							BetterShopPermission.ADMIN_MAKESIGN, true)) {
						// sign is not a shopsign: (if has permission) add sign
						try {
							ShopSign newSign = new ShopSign(clickedSign);

							// all checks passed: create sign
							signsd.setSign(event.getClickedBlock().getLocation(), newSign);
							newSign.updateColor();

							BSutils.sendMessage(event.getPlayer(), "new sign created"
									+ (newSign.getCustomPrice() >= 0 ? " with a custom price of "
									+ BSEcon.format(newSign.getCustomPrice()) + " each" : ""));

						} catch (Exception e) {
							BSutils.sendMessage(player, ChatColor.RED + "Error: " + e.getMessage());
						}
					}

				} catch (Exception e) {
					BetterShopLogger.Log(Level.SEVERE, e);
					event.getPlayer().sendMessage("An Error Occured");
				}
			}
		}
	}

	public boolean load() {
		return signsd.load();
	}

	public boolean save() {
		return !signsd.isChanged() || signsd.save();
	}

	public boolean saveDelayActive() {
		return signsd.saveDelayActive();
	}

	public int numSigns() {
		return signsd.getSavedSigns().size();
	}

	public void startProtecting() {
		checkSigns.start(signResWait);
	}

	public void stopProtecting() {
		checkSigns.cancel();
	}
	/*
	public class StopBreak extends BlockListener {
	]
	Location toStop = null;
	
	public void stopPlace(Location loc) {
	toStop = loc.clone();
	}
	
	@Override
	public void onBlockCanBuild(BlockCanBuildEvent event) {
	if (event.getBlock().getLocation().equals(toStop)) {
	event.setBuildable(false);
	}
	}
	}//*/
	/*
	public class UpdateInv extends TimerTask {
	
	Player toupdate = null;
	
	public UpdateInv(Player p) {
	toupdate = p;
	}
	
	public void start(long wait) {
	(new Timer()).schedule(this, wait);
	}
	
	@Override
	public void run() {
	toupdate.getInventory().setContents(toupdate.getInventory().getContents());
	}
	}//*/
} // end class BSSignShop

