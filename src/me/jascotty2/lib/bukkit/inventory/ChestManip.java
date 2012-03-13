/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: for manipulating the items in a chest
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
package me.jascotty2.lib.bukkit.inventory;

import java.util.ArrayList;
import java.util.List;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

/**
 * @author jacob
 */
public class ChestManip {

	public synchronized static boolean containsItem(Chest chest, Material check) {
		Chest otherChest = otherChest(chest.getBlock());

		return ItemStackManip.contains(chest.getInventory().getContents(), check)
				|| (otherChest != null && ItemStackManip.contains(otherChest.getInventory().getContents(), check));

	}

	public synchronized static boolean containsItem(Chest chest, Material check, short damage) {
		Chest otherChest = otherChest(chest.getBlock());

		return ItemStackManip.contains(chest.getInventory().getContents(), check, damage)
				|| (otherChest != null && ItemStackManip.contains(otherChest.getInventory().getContents(), check));

	}

	public synchronized static void removeItem(Chest chest, Material check) {
		Chest otherChest = otherChest(chest.getBlock());
		if (otherChest == null) {
			chest.getInventory().setContents(ItemStackManip.remove(chest.getInventory().getContents(), check));
		} else {
			if (ItemStackManip.contains(chest.getInventory().getContents(), check)) {
				chest.getInventory().setContents(ItemStackManip.remove(chest.getInventory().getContents(), check));
			} else {
				otherChest.getInventory().setContents(ItemStackManip.remove(otherChest.getInventory().getContents(), check));
			}
		}
	}

	public synchronized static void removeItem(Chest chest, Material check, short damage) {
		Chest otherChest = otherChest(chest.getBlock());
		if (otherChest == null) {
			chest.getInventory().setContents(ItemStackManip.remove(chest.getInventory().getContents(), check));
		} else {
			if (ItemStackManip.contains(chest.getInventory().getContents(), check, damage)) {
				chest.getInventory().setContents(ItemStackManip.remove(chest.getInventory().getContents(), check, damage));
			} else {
				otherChest.getInventory().setContents(ItemStackManip.remove(otherChest.getInventory().getContents(), check, damage));
			}
		}
	}

	public synchronized static ItemStack[] getContents(Chest chest) {
		Chest otherChest = otherChest(chest.getBlock());
		if (otherChest == null) {
			return chest.getInventory().getContents();
		} else {
			ItemStack iss[] = new ItemStack[27 * 2];
			// return with the top portion first
			if (topChest(chest) == chest) {
				System.arraycopy(chest.getInventory().getContents(), 0, iss, 0, 27);
				System.arraycopy(otherChest.getInventory().getContents(), 0, iss, 27, 27);
			} else {
				System.arraycopy(otherChest.getInventory().getContents(), 0, iss, 0, 27);
				System.arraycopy(chest.getInventory().getContents(), 0, iss, 27, 27);
			}
			return iss;
		}
	}

	public synchronized static List<JItem> getItems(Chest c) {
		ItemStack[] chest = getContents(c);
		ArrayList<JItem> chestItems = new ArrayList<JItem>();
		for (ItemStack i : chest) {
			JItem it = JItemDB.GetItem(i);
			if (!chestItems.contains(it)) {
				chestItems.add(it);
			}
		}
		return chestItems;
	}

	public static void setContents(Chest chest, ItemStack iss[]) {
		setContents(chest, iss, true);
	}

	public synchronized static void setContents(Chest chest, ItemStack iss[], boolean useOrder) {
		if(chest == null) return;
		if(iss.length == 27) {
			chest.getInventory().setContents(iss);
		} else if (iss.length == 27 * 2) {
			// new bukkit changed this method completely.. :/
			//ItemStack iss1[] = new ItemStack[27];
			//ItemStack iss2[] = new ItemStack[27];
			//System.arraycopy(iss, 0, iss1, 0, iss1.length);
			//System.arraycopy(iss, 27, iss2, 0, iss2.length);
			
			Chest otherChest = otherChest(chest.getBlock());
			if (otherChest == null) {
				//chest.getInventory().setContents(iss1);
				chest.getInventory().setContents(iss);
			} else {
				if (!useOrder || topChest(chest) == chest) {
					//chest.getInventory().setContents(iss1);
					//otherChest.getInventory().setContents(iss2);
					chest.getInventory().setContents(iss);
				} else {
					//otherChest.getInventory().setContents(iss1);
					//chest.getInventory().setContents(iss2);
					otherChest.getInventory().setContents(iss);
				}
			}
		}
	}
	
	
	public synchronized static void addContents(Chest chest, ItemStack is) {
		Chest otherChest = otherChest(chest.getBlock());
		if (otherChest == null) {
			chest.getInventory().addItem(is);
		} else {
			if (topChest(chest) == chest) {
				if (!ItemStackManip.is_full(chest.getInventory().getContents(), is)) {
					chest.getInventory().addItem(is);
				} else {
					otherChest.getInventory().addItem(is);
				}
			} else { // if (!is_full(chest.getInventory().getContents(), is)) {
				if (!ItemStackManip.is_full(otherChest.getInventory().getContents(), is)) {
					otherChest.getInventory().addItem(is);
				} else {
					chest.getInventory().addItem(is);
				}
			}
		}
	}

	/**
	 * add contents of the stack to the chest, allowing more stacking
	 * @param chest
	 * @param is
	 */
	public synchronized static void addContentsStack(Chest chest, ItemStack is) {
		Chest otherChest = otherChest(chest.getBlock());
		if (otherChest == null) {
			chest.getInventory().addItem(is);
		} else {
			if (otherChest.getX() < chest.getX()
					|| otherChest.getZ() < chest.getZ()) {
				if (!ItemStackManip.is_full(otherChest.getInventory().getContents(), is, true)) {
					otherChest.getInventory().setContents(ItemStackManip.add(otherChest.getInventory().getContents(), is));
				} else {
					chest.getInventory().setContents(ItemStackManip.add(chest.getInventory().getContents(), is));
				}
			} else { // if (!is_full(chest.getInventory().getContents(), is)) {
				if (!ItemStackManip.is_full(chest.getInventory().getContents(), is, true)) {
					chest.getInventory().setContents(ItemStackManip.add(chest.getInventory().getContents(), is));
				} else {
					otherChest.getInventory().setContents(ItemStackManip.add(otherChest.getInventory().getContents(), is));
				}
			}
		}
	}

	public synchronized static Chest otherChest(Block bl) {
		if (bl == null) {
			return null;
		} else if (bl.getRelative(BlockFace.NORTH).getType() == Material.CHEST) {
			return (Chest) bl.getRelative(BlockFace.NORTH).getState();
		} else if (bl.getRelative(BlockFace.WEST).getType() == Material.CHEST) {
			return (Chest) bl.getRelative(BlockFace.WEST).getState();
		} else if (bl.getRelative(BlockFace.SOUTH).getType() == Material.CHEST) {
			return (Chest) bl.getRelative(BlockFace.SOUTH).getState();
		} else if (bl.getRelative(BlockFace.EAST).getType() == Material.CHEST) {
			return (Chest) bl.getRelative(BlockFace.EAST).getState();
		}
		return null;
	}

	public static Chest topChest(Chest c) {
		if(c == null) return null;
		Chest otherChest = otherChest(c.getBlock());
		if (otherChest != null && (otherChest.getX() < c.getX()
				|| otherChest.getZ() < c.getZ())) {
			return otherChest;
		} else {
			return c;
		}
	}
} // end class ChestManip

