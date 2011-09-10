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

import java.util.ArrayList;
import java.util.List;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import org.bukkit.Location;

public class GlobalRegion extends Region {

    public GlobalRegion(String id) {
        super(id);
    }

    @Override
    public BlockVector getMinimumPoint() {
        return new BlockVector(0, 0, 0);
    }

    @Override
    public BlockVector getMaximumPoint() {
        return new BlockVector(0, 0, 0);
    }

    @Override
    public int volume() {
        return 0;
    }

    @Override
    public boolean contains(Vector pt) {
        return false;
    }

    @Override
    public boolean contains(Location loc) {
        return false;
    }

    @Override
    public String getTypeName() {
        return "global";
    }

    @Override
    public List<Region> getIntersectingRegions(List<Region> regions){
            //throws UnsupportedIntersectionException {
        return new ArrayList<Region>();
    }

}
