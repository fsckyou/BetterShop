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

import com.sk89q.worldedit.*;
//import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;

/**
 * Represents a cuboid region that can be .
 *
 * @author sk89q
 */
public class CuboidRegion extends Region {

    /**
     * Store the first point.
     */
    private BlockVector min;
    /**
     * Store the second point.
     */
    private BlockVector max;

    /**
     * Construct a new instance of this cuboid region.
     *
     * @param id
     * @param min 
     * @param max 
     */
    public CuboidRegion(String id, BlockVector min, BlockVector max) {
        super(id);
        this.min = min;
        this.max = max;
    }

    /**
     * Get the lower point of the cuboid.
     *
     * @return min point
     */
    @Override
    public BlockVector getMinimumPoint() {
        return min;
    }

    /**
     * Set the lower point of the cuboid.
     *
     * @param pt
     */
    public void setMinimumPoint(BlockVector pt) {
        min = pt;
    }

    /**
     * Get the upper point of the cuboid.
     *
     * @return max point
     */
    @Override
    public BlockVector getMaximumPoint() {
        return max;
    }

    /**
     * Set the upper point of the cuboid.
     *
     * @param pt
     */
    public void setMaximumPoint(BlockVector pt) {
        max = pt;
    }

    /**
     * Checks to see if a point is inside this region.
     */
    @Override
    public boolean contains(Vector pt) {
        final int x = pt.getBlockX();
        final int y = pt.getBlockY();
        final int z = pt.getBlockZ();
        return x >= min.getBlockX() && x <= max.getBlockX()
                && y >= min.getBlockY() && y <= max.getBlockY()
                && z >= min.getBlockZ() && z <= max.getBlockZ();
    }


    @Override
    public boolean contains(Location loc) {
        final int x = loc.getBlockX();
        final int y = loc.getBlockY();
        final int z = loc.getBlockZ();
        return x >= min.getBlockX() && x <= max.getBlockX()
                && y >= min.getBlockY() && y <= max.getBlockY()
                && z >= min.getBlockZ() && z <= max.getBlockZ();
    }
    
    /*
    public boolean intersectsWith(Region region) throws UnsupportedIntersectionException {
        
        if (region instanceof CuboidRegion) {
            CuboidRegion r1 = (CuboidRegion) this;
            CuboidRegion r2 = (CuboidRegion) region;
            BlockVector min1 = r1.getMinimumPoint();
            BlockVector max1 = r1.getMaximumPoint();
            BlockVector min2 = r2.getMinimumPoint();
            BlockVector max2 = r2.getMaximumPoint();

            return !(min1.getBlockX() > max2.getBlockX()
                    || min1.getBlockY() > max2.getBlockY()
                    || min1.getBlockZ() > max2.getBlockZ()
                    || max1.getBlockX() < min2.getBlockX()
                    || max1.getBlockY() < min2.getBlockY()
                    || max1.getBlockZ() < min2.getBlockZ());
        } else if (region instanceof PolygonalRegion) {
            throw new UnsupportedIntersectionException();
        } else {
            throw new UnsupportedIntersectionException();
        }
    }
    */

    @Override
    public List<Region> getIntersectingRegions(List<Region> regions){// throws UnsupportedIntersectionException {
        final int numRegions = regions.size();
        final List<Region> intersectingRegions = new ArrayList<Region>();
        int i, i2, i3;

        for (i = 0; i < numRegions; i++) {
            final Region region = regions.get(i);
            final BlockVector rMinPoint = region.getMinimumPoint();
            final BlockVector rMaxPoint = region.getMaximumPoint();

            // Check whether the region is outside the min and max vector
            if ((rMinPoint.getBlockX() < min.getBlockX() && rMaxPoint.getBlockX() < min.getBlockX()) 
                            || (rMinPoint.getBlockX() > max.getBlockX() && rMaxPoint.getBlockX() > max.getBlockX())
                    && ((rMinPoint.getBlockY() < min.getBlockY() && rMaxPoint.getBlockY() < min.getBlockY())
                            || (rMinPoint.getBlockY() > max.getBlockY() && rMaxPoint.getBlockY() > max.getBlockY()))
                    && ((rMinPoint.getBlockZ() < min.getBlockZ() && rMaxPoint.getBlockZ() < min.getBlockZ())
                            || (rMinPoint.getBlockZ() > max.getBlockZ() && rMaxPoint.getBlockZ() > max.getBlockZ())) ) {
                //intersectingRegions.add(regions.get(i));
                continue;
            }

            // Check whether the regions points are inside the other region
            if (region.contains(new Vector(min.getBlockX(), min.getBlockY(), min.getBlockZ()))
                    || region.contains(new Vector(min.getBlockX(), min.getBlockY(), max.getBlockZ()))
                    || region.contains(new Vector(min.getBlockX(), max.getBlockY(), max.getBlockZ()))
                    || region.contains(new Vector(min.getBlockX(), max.getBlockY(), min.getBlockZ()))
                    || region.contains(new Vector(max.getBlockX(), max.getBlockY(), max.getBlockZ()))
                    || region.contains(new Vector(max.getBlockX(), max.getBlockY(), min.getBlockZ()))
                    || region.contains(new Vector(max.getBlockX(), min.getBlockY(), min.getBlockZ()))
                    || region.contains(new Vector(max.getBlockX(), min.getBlockY(), max.getBlockZ())) ) {
                intersectingRegions.add(regions.get(i));
                continue;
            }

            // Check whether the other regions points are inside the current region
            if (region instanceof PolygonalRegion) {
                for (i2 = 0; i2 < ((PolygonalRegion)region).getPoints().size(); i2++) {
                    final BlockVector2D pt2Dr = ((PolygonalRegion)region).getPoints().get(i2);
                    final int minYr = ((PolygonalRegion)region).minY;
                    final int maxYr = ((PolygonalRegion)region).maxY;
                    final Vector ptr = new Vector(pt2Dr.getBlockX(), minYr, pt2Dr.getBlockZ());
                    final Vector ptr2 = new Vector(pt2Dr.getBlockX(), maxYr, pt2Dr.getBlockZ());

                    if (this.contains(ptr) || this.contains(ptr2)) {
                        intersectingRegions.add(regions.get(i));
                        continue;
                    }
                }
            } else if (region instanceof CuboidRegion) {
                final BlockVector ptcMin = region.getMinimumPoint(); 
                final BlockVector ptcMax = region.getMaximumPoint();

                if (this.contains(new Vector(ptcMin.getBlockX(), ptcMin.getBlockY(), ptcMin.getBlockZ()))
                        || this.contains(new Vector(ptcMin.getBlockX(), ptcMin.getBlockY(), ptcMax.getBlockZ()))
                        || this.contains(new Vector(ptcMin.getBlockX(), ptcMax.getBlockY(), ptcMax.getBlockZ()))
                        || this.contains(new Vector(ptcMin.getBlockX(), ptcMax.getBlockY(), ptcMin.getBlockZ()))
                        || this.contains(new Vector(ptcMax.getBlockX(), ptcMax.getBlockY(), ptcMax.getBlockZ()))
                        || this.contains(new Vector(ptcMax.getBlockX(), ptcMax.getBlockY(), ptcMin.getBlockZ()))
                        || this.contains(new Vector(ptcMax.getBlockX(), ptcMin.getBlockY(), ptcMin.getBlockZ()))
                        || this.contains(new Vector(ptcMax.getBlockX(), ptcMin.getBlockY(), ptcMax.getBlockZ())) ) {
                    intersectingRegions.add(regions.get(i));
                    continue;
                }
            } else {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            // Check whether the current regions edges collide with the regions edges
            boolean regionIsIntersecting = false;
            final List<BlockVector2D> points = new ArrayList<BlockVector2D>();
            points.add(new BlockVector2D(min.getBlockX(), min.getBlockZ()));
            points.add(new BlockVector2D(min.getBlockX(), max.getBlockZ()));
            points.add(new BlockVector2D(max.getBlockX(), max.getBlockZ()));
            points.add(new BlockVector2D(max.getBlockX(), min.getBlockZ()));

            for (i2 = 0; i2 < points.size(); i2++) {
                boolean checkNextPoint = false;
                final BlockVector2D currPoint = points.get(i2);
                final BlockVector2D nextPoint = (i2 == points.size() - 1 ? points.get(0) : points.get(i2 + 1));
                int currX = currPoint.getBlockX();
                int currZ = currPoint.getBlockZ();
                while (!checkNextPoint) {
                    for(i3 = min.getBlockY(); i3 <= max.getBlockY(); i3++) {
                        if (region.contains(new Vector(currX, i3, currZ))) {
                            intersectingRegions.add(regions.get(i));
                            regionIsIntersecting = true;
                            break;
                        }
                    }

                    if (currX == nextPoint.getBlockX() || currZ == nextPoint.getBlockZ() || regionIsIntersecting) {
                        checkNextPoint = true;
                    }

                    if (nextPoint.getBlockX() > currPoint.getBlockX()) {
                        currX++;
                    } else {
                        currX--;
                    }
                    if (nextPoint.getBlockZ() > currPoint.getBlockZ()) {
                        currZ++;
                    } else {
                        currZ--;
                    }
                }

                if (regionIsIntersecting) {
                    break;
                }
            }
        }

        return intersectingRegions;
    }


    /**
     * Return the type of region as a user-friendly name.
     * 
     * @return type of region
     */
    @Override
    public String getTypeName() {
        return "cuboid";
    }

    /**
     * Get the number of Blocks in this region
     * 
     * @return
     */
    @Override
    public int volume() {
        final int xLength = max.getBlockX() - min.getBlockX() + 1;
        final int yLength = max.getBlockY() - min.getBlockY() + 1;
        final int zLength = max.getBlockZ() - min.getBlockZ() + 1;
        final int volume = xLength * yLength * zLength;
        return volume;
    }


}
