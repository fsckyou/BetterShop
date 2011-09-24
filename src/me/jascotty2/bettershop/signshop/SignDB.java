/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: sign shop database
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
package me.jascotty2.bettershop.signshop;

import me.jascotty2.bettershop.BSConfig;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.utils.BetterShopLogger;

import me.jascotty2.lib.io.FileIO;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.bukkit.item.JItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

/**
 * @author jacob
 */
public class SignDB {

	public final static long signDBsaveWait = 30000; // don't save immediately, wait (30s)
	HashMap<Location, ShopSign> signs = new HashMap<Location, ShopSign>();
	HashMap<Location, Sign> savedSigns = new HashMap<Location, Sign>();
	HashMap<Location, BlockState> signBlocks = new HashMap<Location, BlockState>();
	boolean changed = false;
	private SignSaver delaySaver = null;
	final Server server;

	public SignDB(Server sv) {
		if (sv == null) {
			throw new IllegalArgumentException("Plugin Cannot be Null");
		}
		server = sv;
	}

	public boolean load() {
		if (BSConfig.signDBFile.exists()) {
			try {
				List<String[]> signdb = FileIO.loadCSVFile(BSConfig.signDBFile);
				try {
					for (String[] s : signdb) {
						if (s.length >= 4 && server.getWorld(s[0]) != null) {
							try {
								Location l = new Location(server.getWorld(s[0]),
										CheckInput.GetDouble(s[1], 0),
										CheckInput.GetDouble(s[2], 0),
										CheckInput.GetDouble(s[3], 0));
								if (l.getBlock().getState() instanceof Sign
										&& ChatColor.stripColor(((Sign) l.getBlock().getState()).getLine(0)).equalsIgnoreCase("[BetterShop]")) {
									Sign checkSign = (Sign) l.getBlock().getState();
									ShopSign signInfo = new ShopSign(checkSign);
									signs.put(l, signInfo);

									// save sign
									savedSigns.put(l.clone(), checkSign);

									// save block that anchors it
									if (l.getBlock().getType() == Material.SIGN_POST) {
										signBlocks.put(l.getBlock().getRelative(BlockFace.DOWN).getLocation(),
												l.getBlock().getRelative(BlockFace.DOWN).getState());
									} else {
										Block a = getSignAnchor(l.getBlock());
										if (a != null) {
											signBlocks.put(a.getLocation(), a.getState());
										}
									}

									boolean up = false;
									if (!checkSign.getLine(0).startsWith(BetterShop.getConfig().activeSignColor)) {
										checkSign.setLine(0, BetterShop.getConfig().activeSignColor + ChatColor.stripColor(checkSign.getLine(0)));
										up = true;
									}
									if (BetterShop.getConfig().signItemColor) {
										JItem i = signInfo.getItem();
										if (i != null && i.color != null && !checkSign.getLine(2).startsWith(i.color)) {
											if (BetterShop.getConfig().signItemColorBWswap && ChatColor.BLACK.toString().equals(i.color)) {
												checkSign.setLine(2, ChatColor.WHITE + ChatColor.stripColor(checkSign.getLine(2)));
											} else if (BetterShop.getConfig().signItemColorBWswap && ChatColor.WHITE.toString().equals(i.color)) {
												checkSign.setLine(2, ChatColor.BLACK + ChatColor.stripColor(checkSign.getLine(2)));
											} else {
												checkSign.setLine(2, i.color + ChatColor.stripColor(checkSign.getLine(2)));
											}
											up = true;
										}
									}

									if (up) {
										checkSign.update();
									}
								}
							} catch (Exception e) {
								BetterShopLogger.Severe("Invalid Sign while Loading: " + e.getMessage(), false);
							}
						}
					}

				} catch (Exception e) {
					BetterShopLogger.Log(Level.SEVERE, "Unexpected Error while Loading Signs", e, false);
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

	public boolean save() {
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
			for (Location l : signs.keySet().toArray(new Location[0])) {
				file.add(l.getWorld().getName() + ","
						+ l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
			}
			return FileIO.saveFile(BSConfig.signDBFile, file) && !(changed = false);
		} catch (Exception e) {
			BetterShopLogger.Log(Level.SEVERE, e);
		}
		return false;
	}

	public void setSign(Location l) {
		if (l != null && l.getBlock().getState() instanceof Sign) {
			setSign(l, new ShopSign((Sign) l.getBlock().getState()));
		}
	}

	public void setSign(Location l, ShopSign s) {
		if (l != null && s != null && l.getBlock().getState() instanceof Sign) {
			signs.put(l.clone(), s);
			savedSigns.put(l.clone(), (Sign) l.getBlock().getState());
			Block a = getSignAnchor(l.getBlock());
			if (a != null) {
				signBlocks.put(a.getLocation(), a.getState());
			}
			changed = true;
			delaySave();
		}
	}

	public void remove(Location l) {
		signs.remove(l);
		savedSigns.remove(l);
		Block b = getSignAnchor(l.getBlock());
		if (b != null) {
			signBlocks.remove(b.getLocation());
		}
		changed = true;
		delaySave();
	}

	public boolean signExists(Location l) {
		return signs.containsKey(l);
	}

	public ShopSign getSignShop(Location l) {
		return signs.get(l);
	}

	public List<Block> getShopSigns(Block b) {
		ArrayList<Block> list = getSigns(b);
		for (int i = 0; i < list.size(); ++i) {
			if (!signs.containsKey(list.get(i).getLocation())) {
				list.remove(i);
				--i;
			}
		}
		return list;
	}

	public Collection<BlockState> getSignAnchors() {
		return signBlocks.values();
	}

	public Collection<Sign> getSavedSigns() {
		return savedSigns.values();
	}

	public void delaySave() {
		if (delaySaver != null) {
			delaySaver.cancel();
		}
		delaySaver = new SignSaver();
		delaySaver.start(signDBsaveWait);
	}

	public boolean saveDelayActive() {
		return delaySaver != null;
	}

	protected class SignSaver extends TimerTask {

		public void start(long wait) {
			(new Timer()).schedule(this, wait);
		}

		@Override
		public void run() {
			save();
		}
	}

//	final static BlockFace checkFaces[] = new BlockFace[]{BlockFace.SELF, BlockFace.UP,
//		BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};
	public static ArrayList<Block> getSigns(Block b) {
		ArrayList<Block> list = new ArrayList<Block>();
		if (b.getState() instanceof Sign) {
			list.add(b);
		} else {
			if (b.getRelative(BlockFace.UP).getType() == Material.SIGN_POST) {
				list.add(b.getRelative(BlockFace.UP));
			}
			if (b.getRelative(BlockFace.NORTH).getType() == Material.WALL_SIGN
					&& b.getRelative(BlockFace.NORTH).getData() == 4) {
				list.add(b.getRelative(BlockFace.NORTH));
			}
			if (b.getRelative(BlockFace.SOUTH).getType() == Material.WALL_SIGN
					&& b.getRelative(BlockFace.SOUTH).getData() == 5) {
				list.add(b.getRelative(BlockFace.SOUTH));
			}

			if (b.getRelative(BlockFace.WEST).getType() == Material.WALL_SIGN
					&& b.getRelative(BlockFace.WEST).getData() == 3) {
				list.add(b.getRelative(BlockFace.WEST));
			}

			if (b.getRelative(BlockFace.EAST).getType() == Material.WALL_SIGN
					&& b.getRelative(BlockFace.EAST).getData() == 2) {
				list.add(b.getRelative(BlockFace.EAST));
			}

			//BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST
		}
		return list;
	}

	public static Block getSignAnchor(Block b) {
		if (b.getState() instanceof Sign) {
			switch (b.getData()) {
				case 2: // w
					return b.getRelative(BlockFace.WEST);
				case 3: // e
					return b.getRelative(BlockFace.EAST);
				case 4: // s
					return b.getRelative(BlockFace.SOUTH);
				case 5: // n
					return b.getRelative(BlockFace.NORTH);
			}
		}
		return null;
	}

	public boolean isChanged() {
		return changed;
	}
} // end class SignDB

