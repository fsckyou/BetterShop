// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.wg_regions_52.managers;

import com.sk89q.wg_regions_52.databases.YAMLDatabase;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

import me.jascotty2.bettershop.utils.BetterShopLogger;

/**
 * This class keeps track of region information for every world. It loads
 * world region information as needed.
 * 
 * @author sk89q
 * @author Redecouverte
 */
public class GlobalRegionManager {

	/**
	 * Reference to the plugin.
	 */
	private Server bukkitServer;
	protected File pluginFolder = null;
	/**
	 * Map of managers per-world.
	 */
	private HashMap<String, RegionManager> managers;
	/**
	 * Stores the list of modification dates for the world files. This allows
	 * WorldGuard to reload files as needed.
	 */
	private HashMap<String, Long> lastModified;

	/**
	 * Construct the object.
	 *
	 * @param server reference to the bukkit server
	 * @param pluginFolder where the worlds folder will be saved
	 */
	public GlobalRegionManager(Server server, File pluginFolder) {
		this.bukkitServer = server;
		this.pluginFolder = pluginFolder;
		managers = new HashMap<String, RegionManager>();
		lastModified = new HashMap<String, Long>();

		(new File(pluginFolder, "regions")).mkdirs();
	}

	/**
	 * Unload region information.
	 */
	public void unload() {
		managers.clear();
		lastModified.clear();
	}

	/**
	 * Get the path for a world's regions file.
	 *
	 * @param name
	 * @return
	 */
	protected File getPath(String name) {
		return new File(pluginFolder, "regions" + File.separator + name + ".yml");
	}

	/**
	 * Unload region information for a world.
	 *
	 * @param name
	 */
	public void unload(String name) {
		final RegionManager manager = managers.get(name);

		if (manager != null) {
			managers.remove(name);
			lastModified.remove(name);
		}
	}

	/**
	 * Unload all region information.
	 */
	public void unloadAll() {
		managers.clear();
		lastModified.clear();
	}

	/**
	 * Load region information for a world.
	 *
	 * @param world
	 * @return
	 */
	public RegionManager load(World world) {
		final String name = world.getName();
		final File file = getPath(name);
		RegionManager manager = null;

		try {
			// Create a manager
			manager = new FlatRegionManager(new YAMLDatabase(file));
			managers.put(name, manager);
			if (file.exists()) {
				manager.load();
				
				BetterShopLogger.Log(manager.getRegions().size()
						+ " regions loaded for '" + name + "'");
				// Store the last modification date so we can track changes
				lastModified.put(name, file.lastModified());
			}

			return manager;
			//} catch (FileNotFoundException e) {
		} catch (IOException e) {
			BetterShopLogger.Log(Level.WARNING, "Failed to load regions from file "
					+ file.getAbsolutePath() + " : " + e.getMessage());
		}

		return manager;
	}

	/**
	 * Preloads region managers for all worlds.
	 */
	public void preload() {
		// Load regions
		for (final World world : bukkitServer.getWorlds()) {
			load(world);
		}
	}

	/**
	 * Reloads the region information from file when region databases
	 * have changed.
	 */
	public void reloadChanged() {
		for (final String name : managers.keySet()) {
			final File file = getPath(name);

			Long oldDate = lastModified.get(name);

			if (oldDate == null) {
				oldDate = 0L;
			}

			try {
				if (file.lastModified() > oldDate) {
					final World world = bukkitServer.getWorld(name);

					if (world != null) {
						load(world);
					}
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Get the region manager for a particular world.
	 *
	 * @param world
	 * @return
	 */
	public RegionManager get(World world) {
		RegionManager manager = managers.get(world.getName());
		if (manager == null) {
			manager = load(world);
		}
		return manager;
	}

	public Collection<RegionManager> getAll() {
		return managers.values();
	}

	public Set<Entry<String, RegionManager>> getAllEntries() {
		return managers.entrySet();
	}

	public boolean hasRegion(Location loc) {
		final World world = loc.getWorld();
		final RegionManager mgr = get(world);
		return mgr != null && mgr.getApplicableRegions(loc).size() > 0;
	}
}
