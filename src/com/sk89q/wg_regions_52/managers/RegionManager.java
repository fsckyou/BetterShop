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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.sk89q.worldedit.Vector;
import com.sk89q.wg_regions_52.databases.ProtectionDatabase;
import com.sk89q.wg_regions_52.Region;
import com.sk89q.wg_regions_52.ApplicableRegionSet;

/**
 * An abstract class for getting, setting, and looking up regions. The most
 * simple implementation uses a flat list and iterates through the entire list
 * to look for applicable regions, but a more complicated (and more efficient)
 * implementation may use space partitioning techniques.
 * 
 * @author sk89q
 */
public abstract class RegionManager {
    
     ProtectionDatabase loader;

    /**
     * Construct the object.
     * 
     * @param loader
     */
    public RegionManager(ProtectionDatabase loader) {
        this.loader = loader;
    }

    /**
     * Load the list of regions. If the regions do not load properly, then
     * the existing list should be used (as stored previously).
     *
     * @throws IOException thrown on load error
     */
    public void load() throws IOException {
        loader.load(this);
    }

    /**
     * Save the list of regions.
     *
     * @throws IOException thrown on save eIf checking multiple flags for a single locationror
     */
    public void save() throws IOException {
        loader.save(this);
    }

    /**
     * Get a map of  regions. Use one of the region manager methods
     * if possible if working with regions.
     * 
     * @return map of regions, with keys being region IDs (lowercase)
     */
    public abstract Map<String, Region> getRegions();

    /**
     * Set a list of  regions. Keys should be lowercase in the given
     * map fo regions.
     * 
     * @param regions map of regions
     */
    public abstract void setRegions(Map<String, Region> regions);

    /**
     * Adds a region. If a region by the given name already exists, then
     * the existing region will be replaced.
     * 
     * @param region region to add
     */
    public abstract void addRegion(Region region);

    /**
     * Return whether a region exists by an ID.
     * 
     * @param id id of the region, can be mixed-case
     * @return whether the region exists
     */
    public abstract boolean hasRegion(String id);

    /**
     * Get a region by its ID.
     * 
     * @param id id of the region, can be mixed-case
     * @return region or null if it doesn't exist
     */
    public abstract Region getRegion(String id);

    /**
     * Removes a region, including inheriting children.
     * 
     * @param id id of the region, can be mixed-case
     */
    public abstract void removeRegion(String id);

    /**
     * Get an object for a point for rules to be applied with. Use this in order
     * to query for flag data or membership data for a given point.
     * 
     * @param loc Bukkit location
     * @return applicable region set
     */
    public ApplicableRegionSet getApplicableRegions(org.bukkit.Location loc) {
        return getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitUtil.toVector(loc));
    }

    /**
     * Get an object for a point for rules to be applied with. Use this in order
     * to query for flag data or membership data for a given point.
     * 
     * @param pt point
     * @return applicable region set
     */
    public abstract ApplicableRegionSet getApplicableRegions(Vector pt);

    /**
     * Get a list of region IDs that contain a point.
     * 
     * @param pt point
     * @return list of region Ids
     */
    public abstract List<String> getApplicableRegionsIDs(Vector pt);

    /**
     * Get the number of regions.
     *
     * @return number of regions
     */
    public abstract int size();

}
