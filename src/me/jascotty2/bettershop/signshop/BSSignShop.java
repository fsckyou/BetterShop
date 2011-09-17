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

import me.jascotty2.lib.util.Str;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.bukkit.item.ChestManip;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.item.ItemStockEntry;
import me.jascotty2.lib.bukkit.item.PriceListItem;
import me.jascotty2.lib.bukkit.MinecraftChatStr;
import me.jascotty2.bettershop.utils.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import me.jascotty2.bettershop.BSConfig;
import me.jascotty2.bettershop.BSEcon;
import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.commands.BuyCommands;
import me.jascotty2.bettershop.commands.SellCommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import me.jascotty2.bettershop.shop.Shop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * @author jacob
 */
public class BSSignShop extends PlayerListener {

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

	public void registerEvents(){
		Plugin bs = BetterShop.getPlugin();
		PluginManager pm = bs.getServer().getPluginManager();
		
		// for sign events
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Normal, bs);
		pm.registerEvent(Event.Type.BLOCK_BREAK, checkSigns, Event.Priority.Normal, bs);
		pm.registerEvent(Event.Type.BLOCK_PLACE, checkSigns, Event.Priority.Normal, bs);
		
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, checkSigns.tntBlock, Event.Priority.Low, bs);
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled() || !BetterShop.getConfig().signShopEnabled) {
			return;
		}
		//if(event.getAction() == Action.RIGHT_CLICK_BLOCK) event.getPlayer().getWorld().strikeLightning(event.getClickedBlock().getLocation());
		if (event.getClickedBlock() != null
				&& (event.getClickedBlock().getType() == Material.WALL_SIGN
				|| event.getClickedBlock().getType() == Material.SIGN_POST)) {
			Sign clickedSign = (Sign) event.getClickedBlock().getState();
			if (MinecraftChatStr.uncoloredStr(clickedSign.getLine(0)).equalsIgnoreCase("[BetterShop]")) {
				BetterShop.setLastCommand("Sign: [" + clickedSign.getLine(1) + "][" + clickedSign.getLine(2) + "][" + clickedSign.getLine(3) + "]");
				try {
					Long lt = playerInteractTime.get(event.getPlayer());
					if (lt != null && System.currentTimeMillis() - lt < BSConfig.signInteractWait) {
						event.setCancelled(event.getAction() == Action.RIGHT_CLICK_BLOCK);
						return;
					}
					playerInteractTime.put(event.getPlayer(), System.currentTimeMillis());
					boolean canEdit = BSPermissions.hasPermission(event.getPlayer(),
							BetterShopPermission.ADMIN_MAKESIGN);
					Shop shop = BetterShop.getShop(event.getClickedBlock().getLocation());
					String action = MinecraftChatStr.uncoloredStr(clickedSign.getLine(1)).trim().replace("  ", " ");
					if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
						//event.setCancelled(!canEdit);
						// if sign is registered, update prices
						if (signsd.signExists(event.getClickedBlock().getLocation())) {
							try {
								JItem i = signsd.getSignItem(event.getClickedBlock().getLocation());
								boolean isInv = false;
								if (i == null) {
									String in = ((Sign) event.getClickedBlock().getState()).getLine(2).toLowerCase().replaceAll(" ", "");
									isInv = in.equals("inv");
									if (!isInv && in.length() > 0) {
										if (in.equals("hand")
												|| in.equals("inhand")) {
											if (event.getPlayer().getItemInHand() == null
													|| event.getPlayer().getItemInHand().getAmount() == 0) {
												BSutils.sendMessage(event.getPlayer(), "you don't have anything in your hand");
												return;
											}
											i = JItemDB.findItem(event.getPlayer().getItemInHand());
										}
									}
								}
								String amt = action.contains(" ") ? action.substring(action.lastIndexOf(' ')).trim() : "1";
								if (action.endsWith("all")) {
									amt = "all";
								}
								if (!(amt.equalsIgnoreCase("all") || CheckInput.IsInt(amt))) {
									BSutils.sendMessage(event.getPlayer(), "invalid amount");
									return;
								}
								boolean canBuyIllegal = BetterShop.getConfig().allowbuyillegal || BSPermissions.hasPermission(event.getPlayer(), BetterShopPermission.ADMIN_ILLEGAL, false);
								boolean isBuy = action.toLowerCase().startsWith("buy");

								PriceListItem price = null;
								String pname = "";
								if (i != null) {
									price = shop.pricelist.getItemPrice(i);
									if (price == null) {
										BSutils.sendMessage(event.getPlayer(), i.Name() + " cannot be bought or sold");
										return;
									}
									pname = i.coloredName();
								} else {
									price = new PriceListItem();
									price.buy = price.sell = 0;
									pname = "(";
								}

								int numCheck = 1;
								if (amt.equalsIgnoreCase("all")) {
									if (isBuy) {
										// numCheck = ChestManip.canHold(event.getPlayer().getInventory().getContents(),
										//         i, BetterShop.getConfig().usemaxstack);
										//ArrayList<ItemStockEntry> sold = new ArrayList<ItemStockEntry>();
										ArrayList<ItemStockEntry> inv = BSutils.getTotalInventory(event.getPlayer(), false);
										int ipos = inv.indexOf(new ItemStockEntry(i));
										if (ipos >= 0) {
											numCheck += (int) inv.get(ipos).amount;
										} else {
											numCheck = 0;
										}
										price.buy *= numCheck;
										price.sell *= numCheck;
									} else {
										if (i != null) {
											numCheck = ChestManip.itemAmount(event.getPlayer().getInventory().getContents(), i);
										} else {
											List<ItemStockEntry> sellable = SellCommands.getCanSell(event.getPlayer(), false, null);
											int tt = 0;
											for (ItemStockEntry ite : sellable) {
												PriceListItem tprice = shop.pricelist.getItemPrice(JItemDB.findItem(ite.name));
												if (tprice != null) { // double-check..
													tt += ite.amount;
													price.buy += tprice.buy > 0 ? tprice.buy * ite.amount : 0;
													price.sell += tprice.sell > 0 ? tprice.sell * ite.amount : 0;
													if (pname.length() > 1) {
														pname += ", " + ite.name;
													} else {
														pname += ite.name;
													}
												}
											}
											pname += ")";
											numCheck = tt;
										}
									}
								} else {
									numCheck = CheckInput.GetInt(amt, 1);
								}

								BSutils.sendMessage(event.getPlayer(), String.format(
										BetterShop.getConfig().getString(isBuy ? "multipricecheckbuy" : "multipricechecksell"). //numCheck == 1 ? "pricecheck" : "multipricecheck"
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
										pname,
										BetterShop.getConfig().currency(),
										(price.IsLegal() || canBuyIllegal) && price.buy >= 0
										? BSEcon.format(price.buy) : "No",
										price.sell >= 0 ? BSEcon.format(price.sell) : "No",
										!shop.config.useStock() || shop.stock.getItemAmount(i) < 0 ? "INF" : shop.stock.getItemAmount(i),
										numCheck));

							} catch (Exception ex) {
								BetterShopLogger.Log(Level.SEVERE, ex);
								BSutils.sendMessage(event.getPlayer(), "Failed to lookup the price");
							}
						} else if (canEdit) {// else, (if has permission) add sign
							if (Str.count(action, " ") > 1
									|| (action.contains(" ") && !Str.startIsIn(action, new String[]{
										"buy ", "buyall ", "buystack ",
										"sell ", "sellall ", "sellstack "}))
									|| (!action.contains(" ") && !Str.startIsIn(action, new String[]{
										"buy", "buyall", "buystack",
										"sell", "sellall", "sellstack"}))) {
								BSutils.sendMessage(event.getPlayer(), "invalid action");
								return;
							}
							String amt = action.contains(" ") ? action.substring(action.lastIndexOf(' ')).trim() : "1";
							if (action.endsWith("all")) {
								amt = "all";
							}
							if (!(amt.equalsIgnoreCase("all") || CheckInput.GetInt(amt, -1) > 0)) {
								BSutils.sendMessage(event.getPlayer(), "invalid amount");
								return;
							}
							String in = MinecraftChatStr.uncoloredStr(clickedSign.getLine(2)).replace(" ", "").toLowerCase();
							JItem toAdd[] = new JItem[]{JItemDB.findItem(in)};
							if (toAdd != null && toAdd[0] == null && clickedSign.getLine(2).length() > 0) {
								if (!(in.equals("inv")
										|| in.equals("hand")
										|| in.equals("inhand"))) {
									toAdd = JItemDB.findItems(clickedSign.getLine(2));
								} else {
									toAdd = new JItem[1];
								}
							}
							if (toAdd == null) {
								BSutils.sendMessage(event.getPlayer(), "error");
								return;
							} else if (toAdd.length == 0 || (toAdd[0] == null && !action.startsWith("sellall"))) {
								BSutils.sendMessage(event.getPlayer(), "no matching items");
								return;
							} else if (toAdd.length > 1) {
								BSutils.sendMessage(event.getPlayer(), "more than one matching items");
								return;
							} else if (toAdd[0] != null && toAdd[0].ID() <= 0) {
								BSutils.sendMessage(event.getPlayer(), toAdd[0].Name() + " cannot be bought or sold");
								return;
							} else if (toAdd[0] != null && action.startsWith("sell")) {
								if (toAdd[0].isEntity()) {
									BSutils.sendMessage(event.getPlayer(), "entities cannot be sold");
									return;
								} else if (toAdd[0].isKit()) {
									BSutils.sendMessage(event.getPlayer(), "kits cannot be sold");
									return;
								} else if (!shop.pricelist.isForSale(toAdd[0])) {
									BSutils.sendMessage(event.getPlayer(), "item cannot be sold");
									return;
								}
							} else if (action.startsWith("buy")
									&& !(in.equals("hand") || in.equals("inhand"))) {
								if (toAdd[0] == null) { //  && !amt.equalsIgnoreCase("all")
									BSutils.sendMessage(event.getPlayer(), "must provide an item to buy");
									return;
								} else if (shop.pricelist.getBuyPrice(toAdd[0]) < 0) {
									BSutils.sendMessage(event.getPlayer(), "item cannot be bought");
									return;
								}
							}
							// all checks passed: create sign
							signsd.setSign(event.getClickedBlock().getLocation(), toAdd[0]);
							clickedSign.setLine(0, BetterShop.getConfig().activeSignColor + MinecraftChatStr.uncoloredStr(clickedSign.getLine(0)));

							if (toAdd[0] != null && toAdd[0].color != null) {
								if (BetterShop.getConfig().signItemColorBWswap && ChatColor.BLACK.toString().equals(toAdd[0].color)) {
									clickedSign.setLine(2, ChatColor.WHITE.toString() + clickedSign.getLine(2));
								} else if (BetterShop.getConfig().signItemColorBWswap && ChatColor.WHITE.toString().equals(toAdd[0].color)) {
									clickedSign.setLine(2, ChatColor.BLACK.toString() + clickedSign.getLine(2));
								} else {
									clickedSign.setLine(2, toAdd[0].color + clickedSign.getLine(2));
								}
							}

							clickedSign.update();
							BSutils.sendMessage(event.getPlayer(), "new sign created");


						} else {
							// let them know they can't make a sign
							BSPermissions.hasPermission(event.getPlayer(), BetterShopPermission.ADMIN_MAKESIGN, true);
						}
					} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						if (signsd.signExists(event.getClickedBlock().getLocation())) {
							event.setCancelled(true);
							//buildStopper.stopPlace(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation());
							try {
								JItem i = signsd.getSignItem(event.getClickedBlock().getLocation());

								boolean isInv = false;
								if (i == null) {
									String in = ((Sign) event.getClickedBlock().getState()).getLine(2).toLowerCase().replaceAll(" ", "");
									isInv = in.equals("inv");
									if (!isInv && in.length() > 0) {
										if (in.equals("hand")
												|| in.equals("inhand")) {
											if (event.getPlayer().getItemInHand() == null
													|| event.getPlayer().getItemInHand().getAmount() == 0) {
												BSutils.sendMessage(event.getPlayer(), "you don't have anything in your hand");
												return;
											}
											i = JItemDB.findItem(event.getPlayer().getItemInHand());
										}
									}
								}
								if (i != null && !BetterShop.getConfig().allowbuyillegal && !i.IsLegal()
										&& !BSPermissions.hasPermission(event.getPlayer(), BetterShopPermission.ADMIN_ILLEGAL, true)) {
									return;
								}
								String amt = action.contains(" ") ? action.substring(action.lastIndexOf(' ')).trim() : "1";
								if (action.endsWith("all")) {
									amt = "all";
								} else if (action.contains("stack")) {
									if (i == null) {
										BSutils.sendMessage(event.getPlayer(), "Invalid Sign..");
										return;
									}
									amt = String.valueOf(i.getMaxStackSize());
								}
								if (!(amt.equalsIgnoreCase("all") || CheckInput.GetInt(amt, -1) > 0)) {
									BSutils.sendMessage(event.getPlayer(), "invalid amount");
									return;
								}
								boolean isBuy = action.toLowerCase().startsWith("buy");
								if ((!isBuy && !BSPermissions.hasPermission(event.getPlayer(), BetterShopPermission.USER_SELL, true))
										|| isBuy && !BSPermissions.hasPermission(event.getPlayer(), BetterShopPermission.USER_BUY, true)) {
									return;
								}

								if (i != null) {
									if (isBuy) {
										if (shop.pricelist.getBuyPrice(i) < 0) {
											BSutils.sendMessage(event.getPlayer(), "item cannot be bought");
											return;
										}
									} else {
										if (shop.pricelist.getSellPrice(i) < 0) {
											BSutils.sendMessage(event.getPlayer(), "item cannot be sold");
											return;
										}
									}
								}

								if (amt.equalsIgnoreCase("all")) {
									if (isBuy) {
										BuyCommands.buyAllItem(event.getPlayer(), i);
									} else {
										SellCommands.sellItems(event.getPlayer(), isInv, i, -1);
									}
								} else {
									if (isBuy) {
										BuyCommands.buyItem(event.getPlayer(), i, CheckInput.GetInt(amt, 0));
									} else {
										SellCommands.sellItems(event.getPlayer(), isInv, i, CheckInput.GetInt(amt, 0));
									}
								}

							} catch (Exception ex) {
								BetterShopLogger.Log(Level.SEVERE, ex);
							}
							// may be depricated, but only thing i can get to work :(
							event.getPlayer().updateInventory();
						} else {
							BSutils.sendMessage(event.getPlayer(), "this is not a legal sign");
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
		return signsd.save();
	}

	public boolean saveDelayActive() {
		return signsd.saveDelayActive();
	}

	public int numSigns(){
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

