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
package com.sk89q.wg_regions_52;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Represents a set of regions for a particular point or area and the rules
 * that are represented by that set. An instance of this can be used to
 * query the value of a flag or check if a player can build in the respective
 * region or point. This object contains the list of applicable regions and so
 * the expensive search of regions that are in the desired area has already
 * been completed.
 * 
 * @author sk89q
 */
public class ApplicableRegionSet implements Iterable<Region> {

    private Collection<Region> applicable;
    private Region globalRegion;

    /**
     * Construct the object.
     * 
     * @param applicable
     * @param globalRegion 
     */
    public ApplicableRegionSet(Collection<Region> applicable, Region globalRegion) {
        this.applicable = applicable;
        this.globalRegion = globalRegion;
    }
    
    public Collection<Region> regions() {
        return applicable;
    }
    
    /**
     * Clear a region's parents for isFlagAllowed().
     * 
     * @param needsClear
     * @param hasCleared
     * @param region
     */
    private void clearParents(Set<Region> needsClear, Set<Region> hasCleared, Region region) {
        Region parent = region.getParent();

        while (parent != null) {
            if (!needsClear.remove(parent)) {
                hasCleared.add(parent);
            }

            parent = parent.getParent();
        }
    }

    /**
     * Clear a region's parents for getFlag().
     * 
     * @param needsClear
     * @param hasCleared
     * @param region
     */
    private void clearParents(Map<Region, ?> needsClear, Set<Region> hasCleared, Region region) {
        Region parent = region.getParent();

        while (parent != null) {
            if (needsClear.remove(parent) == null) {
                hasCleared.add(parent);
            }

            parent = parent.getParent();
        }
    }
    
    /**
     * Get the number of regions that are included.
     * 
     * @return
     */
    public int size() {
        return applicable.size();
    }
    
    /**
     * Get an iterator of affected regions.
     * 
     * @return
     */
    public Iterator<Region> iterator() {
        return applicable.iterator();
    }
}
