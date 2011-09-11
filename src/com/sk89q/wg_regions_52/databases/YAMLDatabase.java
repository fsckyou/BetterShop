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

package com.sk89q.wg_regions_52.databases;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;

import com.sk89q.wg_regions_52.*;
import com.sk89q.wg_regions_52.Region.CircularInheritanceException;
import com.sk89q.wg_regions_52.util.yaml.Configuration;
import com.sk89q.wg_regions_52.util.yaml.ConfigurationNode;


public class YAMLDatabase extends AbstractProtectionDatabase {
    
    private Configuration config;
    private Map<String, Region> regions;
    
    public YAMLDatabase(File file) {
        config = new Configuration(file);
    }

    public void load() throws IOException {
        config.load();
        
        final Map<String, ConfigurationNode> regionData = config.getNodes("regions");
        
        // No regions are even configured
        if (regionData == null) {
            regions = new HashMap<String, Region>();
            return;
        }

        final Map<String,Region> regs = new HashMap<String,Region>();
        final Map<Region,String> parentSets = new LinkedHashMap<Region, String>();
        
        for (final Map.Entry<String, ConfigurationNode> entry : regionData.entrySet()) {
            final String id = entry.getKey().toLowerCase().replace(".", "");
            final ConfigurationNode node = entry.getValue();
            
            final String type = node.getString("type");
            Region region;
            
            try {
                if (type == null) {
                    //logger.warning("Undefined region type for region '" + id + '"');
                    continue;
                } else if (type.equals("cuboid")) {
                    final Vector pt1 = checkNonNull(node.getVector("min"));
                    final Vector pt2 = checkNonNull(node.getVector("max"));
                    final BlockVector min = Vector.getMinimum(pt1, pt2).toBlockVector();
                    final BlockVector max = Vector.getMaximum(pt1, pt2).toBlockVector();
                    region = new CuboidRegion(id, min, max);
                } else if (type.equals("poly2d")) {
                    final Integer minY = checkNonNull(node.getInt("min-y"));
                    final Integer maxY = checkNonNull(node.getInt("max-y"));
                    final List<BlockVector2D> points = node.getBlockVector2dList("points", null);
                    region = new PolygonalRegion(id, points, minY, maxY);
                } else if (type.equals("global")) {
                    region = new GlobalRegion(id);
                } else {
                    //logger.warning("Unknown region type for region '" + id + '"');
                    continue;
                }

                regs.put(id, region);

                final String parentId = node.getString("parent");
                if (parentId != null) {
                    parentSets.put(region, parentId);
                }

                final String info = node.getString("info");
                if (info != null) {
                    region.setInfo(info);
                }
            } catch (NullPointerException e) {
                //logger.warning("Missing data for region '" + id + '"');
            }
        }
        
        // Relink parents
        for (final Map.Entry<Region, String> entry : parentSets.entrySet()) {
            final Region parent = regs.get(entry.getValue());
            if (parent != null) {
                try {
                    entry.getKey().setParent(parent);
                } catch (CircularInheritanceException e) {
                    //logger.warning("Circular inheritance detect with '" + entry.getValue() + "' detected as a parent");
                }
            } else {
                //logger.warning("Unknown region parent: " + entry.getValue());
            }
        }
        
        regions = regs;
    }
    
    private <V> V checkNonNull(V val) throws NullPointerException {
        if (val == null) {
            throw new NullPointerException();
        }
        
        return val;
    }

    public void save() throws IOException {
        config.clear();
        
        for (final Map.Entry<String, Region> entry : regions.entrySet()) {
            final Region region = entry.getValue();
            final ConfigurationNode node = config.addNode("regions." + entry.getKey());
            
            if (region instanceof CuboidRegion) {
                final CuboidRegion cuboid = (CuboidRegion) region;
                node.setProperty("type", "cuboid");
                node.setProperty("min", cuboid.getMinimumPoint());
                node.setProperty("max", cuboid.getMaximumPoint());
            } else if (region instanceof PolygonalRegion) {
                final PolygonalRegion poly = (PolygonalRegion) region;
                node.setProperty("type", "poly2d");
                node.setProperty("min-y", poly.getMinimumPoint().getBlockY());
                node.setProperty("max-y", poly.getMaximumPoint().getBlockY());
                
                final List<Map<String, Object>> points = new ArrayList<Map<String,Object>>();
                for (final BlockVector2D point : poly.getPoints()) {
                    final Map<String, Object> data = new HashMap<String, Object>();
                    data.put("x", point.getBlockX());
                    data.put("z", point.getBlockZ());
                    points.add(data);
                }
                
                node.setProperty("points", points);
            } else if (region instanceof GlobalRegion) {
                node.setProperty("type", "global");
            } else {
                node.setProperty("type", region.getClass().getCanonicalName());
            }

            final Region parent = region.getParent();
            if (parent != null) {
                node.setProperty("parent", parent.getId());
            }
			if(region.getInfo() != null){
				node.setProperty("info", region.getInfo());
			}
        }
        
        config.setHeader("#\r\n" +
                "# WorldGuard regions file\r\n" +
                "#\r\n" +
                "# WARNING: THIS FILE IS AUTOMATICALLY GENERATED. If you modify this file by\r\n" +
                "# hand, be aware that A SINGLE MISTYPED CHARACTER CAN CORRUPT THE FILE. If\r\n" +
                "# WorldGuard is unable to parse the file, your regions will FAIL TO LOAD and\r\n" +
                "# the contents of this file will reset. Please use a YAML validator such as\r\n" +
                "# http://yaml-online-parser.appspot.com (for smaller files).\r\n" +
                "#\r\n" +
                "# REMEMBER TO KEEP PERIODICAL BACKUPS.\r\n" +
                "#");
        config.save();
    }
    
    public Map<String, Region> getRegions() {
        return regions;
    }

    public void setRegions(Map<String, Region> regions) {
        this.regions = regions;
    }
    
}
