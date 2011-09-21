/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: chest shop database
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
package me.jascotty2.bettershop.chestshop;

import me.jascotty2.bettershop.BSConfig;
import me.jascotty2.bettershop.utils.BetterShopLogger;

import me.jascotty2.lib.io.FileIO;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.bukkit.inventory.ChestManip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

/**
 * @author jacob
 */
public class ChestDB {

	public final static long signDBsaveWait = 30000; // don't save immediately, wait (30s)
	List<Location> chests = new ArrayList<Location>();
	Map<Location, ItemStack[]> savedChests = new HashMap<Location, ItemStack[]>();
	boolean changed = false;
	private DelayedSaver delaySaver = null;
	final Server server;

	public ChestDB(Server sv) {
		if (sv == null) {
			throw new IllegalArgumentException("Plugin Cannot be Null");
		}
		server = sv;
	}

	public synchronized boolean load() {
		if (BSConfig.chestDBFile.exists()) {
			try {
				List<String[]> signdb = FileIO.loadCSVFile(BSConfig.chestDBFile);
				for (String[] s : signdb) {
					if (s.length >= 4 && server.getWorld(s[0]) != null) {
						chests.add(new Location(server.getWorld(s[0]),
								CheckInput.GetDouble(s[1], 0),
								CheckInput.GetDouble(s[2], 0),
								CheckInput.GetDouble(s[3], 0)));
					}
				}
				// now scan & double-check these are all chests
				for (Location l : chests) {
					if (!(l.getBlock().getState() instanceof Chest)) {
						chests.remove(l);
					} else {
						//savedChests.put(l, ChestManip.getContents((Chest) l.getBlock().getState()));
						savedChests.put(l, ((Chest) l.getBlock().getState()).getInventory().getContents());
					}
				}
				return true;
			} catch (FileNotFoundException ex) {
				BetterShopLogger.Log(Level.SEVERE, ex, false);
			} catch (IOException ex) {
				BetterShopLogger.Log(Level.SEVERE, ex, false);
			} catch (Exception ex) {
				BetterShopLogger.Log(Level.SEVERE, ex);
			}
			return false;
		}
		return true;
	}

	public synchronized boolean save() {
		try {
			if (delaySaver != null) {
				delaySaver.cancel();
				delaySaver = null;
			}
		} catch (Exception e) {
			BetterShopLogger.Log(Level.SEVERE, e);
		}
		try {
			ArrayList<String> file = new ArrayList<String>();
			for (Location l : chests) {
				file.add(l.getWorld().getName() + ","
						+ l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
			}
			return FileIO.saveFile(BSConfig.chestDBFile, file) && !(changed = false);
		} catch (Exception e) {
			BetterShopLogger.Log(Level.SEVERE, e);
		}
		return false;
	}
	
	public boolean has(Location l){
		return chests.contains(l);
	}

	public void setChest(Location l) {
		if (l != null && !chests.contains(l)) {
			chests.add(l.clone());
			//savedChests.put(l.clone(), ChestManip.getContents((Chest) l.getBlock().getState()));
			savedChests.put(l, ((Chest) l.getBlock().getState()).getInventory().getContents());
			Chest other = ChestManip.otherChest(l.getBlock());
			if(other != null){
				chests.add(other.getBlock().getLocation());
				savedChests.put(other.getBlock().getLocation(), other.getInventory().getContents());
			}
			changed = true;
			delaySave();
		}
	}

	public void remove(Location l) {
		chests.remove(l);
		savedChests.remove(l);
//		if(l.getBlock().getState() instanceof Chest){
//			((Chest)l.getBlock().getState()).getInventory().clear();
//		}
		changed = true;
		delaySave();
	}

	public void remove(Location l, boolean removeDouble) {
		remove(l);
		if(removeDouble){
			Chest other = ChestManip.otherChest(l.getBlock());
			if(other != null){
				remove(other.getBlock().getLocation());
			}
		}
	}
	
	public boolean savedChestExists(Location l) {
		return chests.contains(l);
	}

	public Set<Map.Entry<Location, ItemStack[]>> getSavedChests() {
		return savedChests.entrySet();
	}

	public void delaySave() {
		if (delaySaver != null) {
			delaySaver.cancel();
		}
		delaySaver = new DelayedSaver();
		delaySaver.start(signDBsaveWait);
	}

	public boolean saveDelayActive(){
		return delaySaver != null;
	}
	
	protected class DelayedSaver extends TimerTask {
		public void start(long wait) {
			(new Timer()).schedule(this, wait);
		}

		@Override
		public void run() {
			save();
		}
	}

	public boolean isChanged() {
		return changed;
	}

} // end class ChestDB

