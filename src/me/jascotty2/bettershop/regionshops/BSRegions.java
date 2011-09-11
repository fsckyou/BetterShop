/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: methods for managing & tracking regions
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

import com.sk89q.wg_regions_52.ApplicableRegionSet;
import com.sk89q.wg_regions_52.CuboidRegion;
import com.sk89q.wg_regions_52.PolygonalRegion;
import com.sk89q.wg_regions_52.Region;
import com.sk89q.wg_regions_52.managers.RegionManager;
import com.sk89q.wg_regions_52.managers.GlobalRegionManager;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import me.jascotty2.bettershop.BSutils;
import org.bukkit.Server;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author jacob
 */
public class BSRegions {

	protected static WorldEditPlugin worldEdit = null;
	File loadedFolder = null;
	protected final GlobalRegionManager globalRegionManager;

	public BSRegions(Server server, File dataFolder) {
		loadedFolder = dataFolder;
		globalRegionManager = new GlobalRegionManager(server, dataFolder);
	} // end default constructor

	public void load() {
		globalRegionManager.preload();
	}

	public boolean hasRegion(Location loc) {
		return globalRegionManager.hasRegion(loc);
	}

	public String getRegionName(Location loc) {
		if (loc != null) {
			final World world = loc.getWorld();
			final RegionManager mgr = globalRegionManager.get(world);
			ApplicableRegionSet regions = mgr.getApplicableRegions(loc);
			if (regions.size() > 0) {
				Region r = regions.iterator().next();
				return r.getId();
			}
		}
		return null;
	}

	public boolean define(Player pl, String name) {
		return worldEdit == null ? false : define(pl, name, worldEdit.getSelection(pl));
	}

	public boolean define(Player pl, String id, Selection sel) {
		if (sel == null) {
			BSutils.sendMessage(pl, ChatColor.YELLOW + "Select a region first");
			return false;
		} else if (!Region.isValidId(id)) {
			BSutils.sendMessage(pl, ChatColor.RED + "Invalid region ID specified!");
			return false;
		} else if (id.equalsIgnoreCase("__global__")) {
			BSutils.sendMessage(pl, ChatColor.RED + "A region cannot be named __global__");
			return false;
		}

		Region region;

		// Detect the type of region from WorldEdit
		if (sel instanceof Polygonal2DSelection) {
			final Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
			final int minY = polySel.getNativeMinimumPoint().getBlockY();
			final int maxY = polySel.getNativeMaximumPoint().getBlockY();
			region = new PolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
		} else if (sel instanceof CuboidSelection) {
			final BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
			final BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
			region = new CuboidRegion(id, min, max);
		} else {
			BSutils.sendMessage(pl, ChatColor.RED + "The type of region selected in WorldEdit is unsupported!");
			return false;
		}

		final RegionManager mgr = globalRegionManager.get(sel.getWorld());
		mgr.addRegion(region);

		try {
			mgr.save();
			BSutils.sendMessage(pl, ChatColor.GREEN + "Region saved as " + id + ".");
		} catch (IOException e) {
			BSutils.sendMessage(pl, ChatColor.RED + "Failed to write regions file: " + e.getMessage());
		}
		return true;
	}

	public String[] list(final World world, int pageSize, int pageNum) {
		String[] regionIDList = list(world);
		int pages = (int) Math.ceil(regionIDList.length / (float) pageSize);
		if (pageNum < pages) {
			int numLeft = regionIDList.length - pageNum * pageSize;
			String[] page = new String[numLeft > pageSize ? pageSize : numLeft];
			System.arraycopy(regionIDList, pageNum * pageSize, page, 0, page.length);
			return page;
		}
		return new String[0];
	}

	public String[] list(final World world) {
		if (world != null) {

			final RegionManager mgr = globalRegionManager.get(world);
			final Map<String, Region> regions = mgr.getRegions();

			final String[] regionIDList = regions.keySet().toArray(new String[0]);
			Arrays.sort(regionIDList);
			return regionIDList;
		} else {

			final Map<String, Map<String, Region>> regions = new TreeMap<String, Map<String, Region>>();

			for (Entry<String, RegionManager> mgr : globalRegionManager.getAllEntries()) {
				regions.put(mgr.getKey(), mgr.getValue().getRegions());
			}

			int size = 0;//regions.size();
			for (final String w : regions.keySet()) {
				size += regions.get(w).size();
			}

			int i = 0;
			String[] regionIDList = new String[size];
			for (final String w : regions.keySet()) {
				for (final String r : regions.get(w).keySet()) {
					regionIDList[i++] = w + ":" + r;
				}
			}
			Arrays.sort(regionIDList);

			return regionIDList;
		}
	}

	public void remove(World w, String id) {
		remove(null, w, id);
	}

	public boolean remove(CommandSender pl, World world, String id) {
		if (world == null) {
			if (pl == null) {
				return false;
			} else if (id.contains(":") || !(pl instanceof Player)) {
				String worldname;
				if (id.contains(":") && id.indexOf(':') == id.lastIndexOf(':')) {
					worldname = id.substring(0, id.indexOf(':'));
					id = id.substring(id.indexOf(':') + 1);
				} else {
					BSutils.sendMessage(pl, ChatColor.RED + "must specify world (world:region)");
					return false;
				}

				for (final World w : pl.getServer().getWorlds()) {
					if (w.getName().equalsIgnoreCase(worldname)) {
						world = w;
						break;
					}
				}

				if (world == null) {
					BSutils.sendMessage(pl, ChatColor.RED + "world not found");
					return false;
				}
			} else if (pl instanceof Player) {
				world = ((Player) pl).getWorld();
			}
		}
		if (world != null) {
			final RegionManager mgr = globalRegionManager.get(world);
			final Region region = mgr.getRegion(id);

			if (region == null) {
				BSutils.sendMessage(pl, ChatColor.RED + "Could not find a region by that ID.");
				return false;
			}

			mgr.removeRegion(id);

			BSutils.sendMessage(pl, ChatColor.GREEN + "Region '" + id + "' removed.");

			try {
				mgr.save();
			} catch (IOException e) {
				BSutils.sendMessage(pl, ChatColor.RED + "Failed to write regions file: " + e.getMessage());
			}
		}
		return true;
	}

	public Collection<Region> getAll() {
		ArrayList<Region> all = new ArrayList<Region>();
		for (RegionManager r : globalRegionManager.getAll()) {
			all.addAll(r.getRegions().values());
		}
		return all;
	}

	public Set<Entry<String, RegionManager>> getAllRegionManagers() {
		return globalRegionManager.getAllEntries();
	}
} // end class BSRegions

