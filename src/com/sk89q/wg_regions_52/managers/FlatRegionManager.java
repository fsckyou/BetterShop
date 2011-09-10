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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import com.sk89q.worldedit.Vector;
import com.sk89q.wg_regions_52.ApplicableRegionSet;
import com.sk89q.wg_regions_52.Region;
import com.sk89q.wg_regions_52.databases.ProtectionDatabase;
import java.util.Iterator;
import org.bukkit.Location;

/**
 * A very simple implementation of the region manager that uses a flat list
 * and iterates through the list to identify applicable regions. This method
 * is not very efficient.
 * 
 * @author sk89q
 */
public class FlatRegionManager extends RegionManager {

    /**
     * List of  regions.
     */
    private Map<String, Region> regions;

    /**
     * Construct the manager.
     * 
     * @param regionloader 
     */
    public FlatRegionManager(ProtectionDatabase regionloader) {
        super(regionloader);
        regions = new TreeMap<String, Region>();
    }

    /**
     * Get a list of  regions.
     *
     * @return
     */
    @Override
    public Map<String, Region> getRegions() {
        return regions;
    }

    /**
     * Set a list of  regions.
     */
    @Override
    public void setRegions(Map<String, Region> regions) {
        this.regions = new TreeMap<String, Region>(regions);
    }

    /**
     * Adds a region.
     * 
     * @param region
     */
    @Override
    public void addRegion(Region region) {
        regions.put(region.getId().toLowerCase(), region);
    }

    /**
     * Removes a region and its children.
     * 
     * @param id
     */
    @Override
    public void removeRegion(String id) {
        final Region region = regions.get(id.toLowerCase());
        regions.remove(id.toLowerCase());

        if (region != null) {
            final List<String> removeRegions = new ArrayList<String>();
            final Iterator<Region> iter = regions.values().iterator();
            while (iter.hasNext()) {
                final Region curRegion = iter.next();
                if (curRegion.getParent() == region) {
                    removeRegions.add(curRegion.getId().toLowerCase());
                }
            }

            for (final String remId : removeRegions) {
                removeRegion(remId);
            }
        }
    }

    /**
     * Return whether a region exists by an ID.
     * 
     * @param id
     * @return
     */
    @Override
    public boolean hasRegion(String id) {
        return regions.containsKey(id.toLowerCase());
    }

    public boolean hasRegion(Location loc) {
        for (final Region region : regions.values()) {
            if (region.contains(loc)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a region by its ID.
     * 
     * @param id
     */
    @Override
    public Region getRegion(String id) {
        return regions.get(id.toLowerCase());
    }

    /**
     * Get an object for a point for rules to be applied with.
     * 
     * @param pt
     * @return
     */
    @Override
    public ApplicableRegionSet getApplicableRegions(Vector pt) {
        final TreeSet<Region> appRegions =
                new TreeSet<Region>();

        for (final Region region : regions.values()) {
            if (region.contains(pt)) {
                appRegions.add(region);

                Region parent = region.getParent();

                while (parent != null) {
                    if (!appRegions.contains(parent)) {
                        appRegions.add(region);
                    }

                    parent = parent.getParent();
                }
            }
        }

        return new ApplicableRegionSet(appRegions, regions.get("__global__"));
    }

    /**
     * Get a list of region IDs that contain a point.
     * 
     * @param pt
     * @return
     */
    @Override
    public List<String> getApplicableRegionsIDs(Vector pt) {
        final List<String> applicable = new ArrayList<String>();

        for (final Map.Entry<String, Region> entry : regions.entrySet()) {
            if (entry.getValue().contains(pt)) {
                applicable.add(entry.getKey());
            }
        }

        return applicable;
    }

    /**
     * Get the number of regions.
     * 
     * @return
     */
    @Override
    public int size() {
        return regions.size();
    }
}
