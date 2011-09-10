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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.Location;

/**
 * Represents a region of any shape and size that can be protected.
 * 
 * @author sk89q
 */
public abstract class Region implements Comparable<Region> {
    
    private static final Pattern idPattern = Pattern.compile("^[A-Za-z0-9_,'\\-\\+/]{1,}$");
    
    /**
     * Holds the region's ID.
     */
    private String id;
    
    /**
     * Priority.
     */
    private int priority = 0;
    
    /**
     * Holds the curParent.
     */
    private Region parent;
    
    /**
     * Construct a new instance of this region.
     *
     * @param id
     */
    public Region(String id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the lower point of the cuboid.
     *
     * @return min point
     */
    public abstract BlockVector getMinimumPoint();

    /**
     * Get the upper point of the cuboid.
     *
     * @return max point
     */
    public abstract BlockVector getMaximumPoint();

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to setFlag
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    /**
     * @return the curParent
     */
    public Region getParent() {
        return parent;
    }

    /**
     * Set the curParent. This checks to make sure that it will not result
     * in circular inheritance.
     * 
     * @param parent the curParent to setFlag
     * @throws CircularInheritanceException 
     */
    public void setParent(Region parent) throws CircularInheritanceException {
        if (parent == null) {
            this.parent = null;
            return;
        }
        
        if (parent == this) {
            throw new CircularInheritanceException();
        }
        
        Region p = parent.getParent();
        while (p != null) {
            if (p == this) {
                throw new CircularInheritanceException();
            }
            p = p.getParent();
        }
        
        this.parent = parent;
    }

    /**
     * Get the number of blocks in this region
     * 
     * @return
     */
    public abstract int volume();
    
    /**
     * Check to see if a point is inside this region.
     * 
     * @param pt
     * @return
     */
    public abstract boolean contains(Vector pt);
    
    public abstract boolean contains(Location loc);
    /**
     * Compares to another region.
     * 
     * @param other
     * @return
     */
    public int compareTo(Region other) {
        if (id.equals(other.id)) {
            return 0;
        } else if (priority == other.priority) {
            return 1;
        } else if (priority > other.priority) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * Return the type of region as a user-friendly, lowercase name.
     * 
     * @return type of region
     */
    public abstract String getTypeName();

    /**
     * Get a list of intersecting regions.
     * 
     * @param regions
     * @return
     */
    public abstract List<Region> getIntersectingRegions(List<Region> regions);
    
    /**
     * Checks to see if the given ID is accurate.
     * 
     * @param id
     * @return
     */
    public static boolean isValidId(String id) {
        return idPattern.matcher(id).matches();
    }
    
    /**
     * Returns the hash code.
     */
    @Override
    public int hashCode(){
        return id.hashCode();
    }
    
    /**
     * Returns whether this region has the same ID as another region.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Region)) {
            return false;
        }
        
        final Region other = (Region) obj;
        return other.getId().equals(getId());
    }
    
    /**
     * Thrown when setting a curParent would create a circular inheritance
     * situation.
     * 
     */
    public static class CircularInheritanceException extends Exception {
        private static final long serialVersionUID = 7479613488496776022L;
    }
}
