/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: shop manager with support for different region-based shops
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
package me.jascotty2.bettershop.regionshops;

import com.sk89q.wg_regions_52.Region;
import com.sk89q.wg_regions_52.managers.RegionManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.shop.Shop;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * @author jacob
 */
public class RegionShopManager {

	BSRegions regions = null;
	Map<String, Shop> shops = new HashMap<String, Shop>();

	public int load() {
		if (regions == null) {
			Plugin p = BetterShop.getPlugin().getServer().getPluginManager().getPlugin("WorldEdit");
			try {
				regions = new BSRegions(BetterShop.getPlugin().getServer(),
						BetterShop.getPlugin().getDataFolder());
				regions.load();
				if (p != null && p instanceof WorldEditPlugin) {
					BSRegions.worldEdit = (WorldEditPlugin) p;
				}
			} catch (NoClassDefFoundError e) {
				if (p == null) {
					BetterShopLogger.Log("to enable existing regions, put a copy of WorldEdit in the lib folder, "
							+ "or install WorldEdit to the server");
				} else {
					BetterShopLogger.Warning("Unexpected error while loading region manager");
				}
				regions = null;
			}
		}

		int numErrors = 0;
		// TODO: allow multiple shop definitions..
		{
			Shop s = new Shop();
			if (!s.load(null)) {
				BetterShopLogger.Warning("Error Loading " + (true ? "Main" : null) + " Shop");
				++numErrors;
			}
			shops.put(null, s);
		}
		if (regions != null) {
			for (Entry<String, RegionManager> m : regions.getAllRegionManagers()) {
				for (Region r : m.getValue().getRegions().values()) {//regions.getAll()) {//
					if (!shops.containsKey(r.getInfo())) {
						BetterShopLogger.Warning("invalid shop defined in region " + r.getId() + " (removed)");
						regions.remove(null, null, m.getKey() + ":" + r.getId());
					} else {
						shops.put(r.getId(), shops.get(r.getInfo()));
					}
				}
			}
		}
		return numErrors;
	}

	public boolean isCommandShopEnabled(Location loc) {
		if (loc == null) {
			return BetterShop.getConfig().useCommandShop();
		} else if (!BetterShop.getConfig().useCommandShop()) {
			return false;
		} else if (BetterShop.getConfig().useCommandShopGlobal()) {
			return true;
		}
		boolean isRegion = inShopRegion(loc);
		return BetterShop.getConfig().useRegionCommandShop() && isRegion
				|| BetterShop.getConfig().useGlobalCommandShop() && !isRegion;
	}

	/**
	 * Primarily for admin commands, if a shop exists in this area
	 * @param loc
	 * @return
	 */
	public boolean locationHasShop(Location loc) {
		if (BetterShop.getConfig().useCommandShopGlobal()) {
			return true;
		} else if (BetterShop.getConfig().useRegionCommandShop()) {
			return inShopRegion(loc);
		} else {
			return !inShopRegion(loc);
		}
	}

	public boolean inShopRegion(Location loc) {
		return regions == null ? false : regions.hasRegion(loc);
	}

	public Shop getShop(Location loc) {
		if (regions == null) {
			return shops.get(null);
		} else {
			return shops.get(regions.getRegionName(loc));
		}
	}

	public Shop getShop(String s) {
		return shops.get(s);
	}

	public Collection<Shop> getShops() {
		return shops.values();
	}

	public boolean canUseRegions() {
		return regions != null;
	}

	public boolean addRegion(String regionName, Player player) {
		return addRegion(regionName, null, player);
	}

	public boolean addRegion(String regionName, String shopName, Player player) {
		if (regions != null && shops.containsKey(shopName)) {
			if (regions.define(player, regionName)) {
				shops.put(regionName, shops.get(shopName));
				return true;
			}
		}
		return false;
	}

	public boolean removeRegion(CommandSender sender, String regionName) {
		if (regions == null) {
			return false;
		}
		return regions.remove(sender,
				sender instanceof Player ? ((Player) sender).getWorld() : null, regionName);
	}

	public String[] getRegionList(World w, int page, int pazesize) {
		return regions == null ? new String[0] : regions.list(w, pazesize, page);
	}

	public int numRegions() {
		return regions == null ? 0 : regions.getAll().size();
	}

	public void checkRestock() {
		for (Shop s : shops.values()) {
			if (s.stock != null && s.config.useStock()) {
				s.stock.checkStockRestock();
			}
		}
	}

	public void restock() {
		for (Shop s : shops.values()) {
			if (s.stock != null && s.config.useStock()) {
				s.stock.restock(true);
			}
		}
	}

	public void closeAll() {
		for (Shop s : shops.values()) {
			try {
				s.stock.close();
			} catch (Exception ex) {
				//BetterShopLogger.Severe(ex, false);
			}
			try {
				s.pricelist.close();
			} catch (Exception ex) {
				//BetterShopLogger.Severe(ex, false);
			}
		}
		shops.clear();
		if (regions != null) {
			regions.globalRegionManager.unloadAll();
		}
	}
} // end class RegionShopManager

