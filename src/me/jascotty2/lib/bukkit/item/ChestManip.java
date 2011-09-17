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
package me.jascotty2.lib.bukkit.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

/**
 * @author jacob
 */
public class ChestManip {

	// allowed to stack: apples, bows, bread, pork, cookedpork, fish, cookedfish,
	// signs, doors, carts, saddles, snowballs, boats, eggs, cake, beds, records
	public final static List<Integer> noStack = Arrays.asList(256, 257, 258, 259,
			267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 283,
			284, 285, 286, 290, 291, 292, 293, 294, 298, 299, 300, 301, 302, 303,
			304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317,
			325, 326, 327, 335);
	public final static List<Integer> allNoStack = Arrays.asList(256, 257, 258, 259,
			260, 261, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278,
			279, 283, 284, 285, 286, 290, 291, 292, 293, 294, 297, 298, 299, 300,
			301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314,
			315, 316, 317, 319, 320, 322, 323, 324, 325, 326, 327, 328, 329, 330,
			332, 333, 335, 342, 343, 344, 349, 350, 354, 355, 357, 2256, 2257);

	/**
	 * check if this stack cannot hold one more of this item
	 * @param items
	 * @param check
	 * @return
	 */
	public static boolean is_full(ItemStack[] items, ItemStack check) {
		int amt = check.getAmount();
		for (ItemStack item : items) {
			if (item == null || item.getAmount() == 0
					|| (item.getTypeId() == check.getTypeId()
					&& ((item.getAmount() + amt <= 64 && !allNoStack.contains(item.getTypeId()))
					|| (item.getAmount() + amt <= 16 && (item.getTypeId() == 344 || item.getTypeId() == 332))))) {
				return false;
			} else if (item.getTypeId() == check.getTypeId()) {
				if (item.getAmount() < 64 && !allNoStack.contains(item.getTypeId())) {
					amt -= 64 - item.getAmount();
				} else if (item.getAmount() < 16 && (item.getTypeId() == 344 || item.getTypeId() == 332)) {
					amt -= 16 - item.getAmount();
				} else if (item.getAmount() < 8 && item.getTypeId() == 357) {
					amt -= 8 - item.getAmount();
				}
			}
		}
		return true;
	}

	/**
	 * check if this stack cannot hold one more of this item, assuming allowed to stack some unstackables
	 * @param items
	 * @param check
	 * @return
	 */
	public synchronized static boolean is_fullStack(ItemStack[] items, ItemStack check) {
		int amt = check.getAmount();
		for (ItemStack item : items) {
			if (item == null || item.getAmount() == 0
					|| (item.getTypeId() == check.getTypeId()
					&& ((item.getAmount() + amt <= 64 && !noStack.contains(item.getTypeId()))))) {
				return false;
			} else if (item.getTypeId() == check.getTypeId()
					&& item.getAmount() < 64 && !noStack.contains(item.getTypeId())) {
				amt -= 64 - item.getAmount();
			}
		}
		return true;
	}

	public synchronized static int canHold(ItemStack[] items, JItem check, boolean useMaxStack) {
		int amt = 0;
		if (check.isEntity()) {
			return -1;
		} else if (check.isKit()) {
		} else {
			for (ItemStack item : items) {
				if (item == null || item.getAmount() == 0
						|| (check.equals(item) && item.getAmount() <= (useMaxStack ? check.MaxStackSize() : 64))) {
					amt += (useMaxStack ? check.MaxStackSize() : 64) - (item == null ? 0 : item.getAmount());
				}
			}
		}
		return amt;
	}

	public synchronized static int itemAmount(ItemStack[] items, JItem check) {
		int amt = 0;
		for (ItemStack item : items) {
			if (check != null && check.equals(item)) {
				amt += item.getAmount();
			}
		}
		return amt;
	}

	public synchronized static boolean containsItem(ItemStack[] items, Material check) {
		for (ItemStack item : items) {
			if (item != null && item.getType() == check) {
				return true;
			}
		}
		return false;
	}

	public synchronized static boolean containsItem(ItemStack[] items, Material check, short damage) {
		for (ItemStack item : items) {
			if (item != null && item.getType() == check && item.getDurability() == damage) {
				return true;
			}
		}
		return false;
	}

	public synchronized static boolean containsItem(Chest chest, Material check) {
		Chest otherChest = otherChest(chest.getBlock());

		return containsItem(chest.getInventory().getContents(), check)
				|| (otherChest != null && containsItem(otherChest.getInventory().getContents(), check));

	}

	public synchronized static boolean containsItem(Chest chest, Material check, short damage) {
		Chest otherChest = otherChest(chest.getBlock());

		return containsItem(chest.getInventory().getContents(), check, damage)
				|| (otherChest != null && containsItem(otherChest.getInventory().getContents(), check));

	}

	public synchronized static ItemStack[] removeItem(ItemStack[] items, Material check) {
		for (int i = 0; i < items.length; ++i) {
			if (items[i] != null) {
				if (items[i].getType() == check) {
					if (items[i].getAmount() > 1) {
						items[i].setAmount(items[i].getAmount() - 1);
					} else {
						items[i].setAmount(0);
						items[i].setTypeId(0);
					}
					return items;
				}
			}
		}
		return items;
	}

	public synchronized static ItemStack[] removeItem(ItemStack[] items, Material check, short damage) {
		for (int i = 0; i < items.length; ++i) {
			if (items[i] != null) {
				if (items[i].getType() == check && items[i].getDurability() == damage) {
					if (items[i].getAmount() > 1) {
						items[i].setAmount(items[i].getAmount() - 1);
					} else {
						items[i].setAmount(0);
						items[i].setTypeId(0);
					}
					return items;
				}
			}
		}
		return items;
	}

	public synchronized static void removeItem(Chest chest, Material check) {
		Chest otherChest = otherChest(chest.getBlock());
		if (otherChest == null) {
			chest.getInventory().setContents(removeItem(chest.getInventory().getContents(), check));
		} else {
			if (containsItem(chest.getInventory().getContents(), check)) {
				chest.getInventory().setContents(removeItem(chest.getInventory().getContents(), check));
			} else {
				otherChest.getInventory().setContents(removeItem(otherChest.getInventory().getContents(), check));
			}
		}
	}

	public synchronized static void removeItem(Chest chest, Material check, short damage) {
		Chest otherChest = otherChest(chest.getBlock());
		if (otherChest == null) {
			chest.getInventory().setContents(removeItem(chest.getInventory().getContents(), check));
		} else {
			if (containsItem(chest.getInventory().getContents(), check, damage)) {
				chest.getInventory().setContents(removeItem(chest.getInventory().getContents(), check, damage));
			} else {
				otherChest.getInventory().setContents(removeItem(otherChest.getInventory().getContents(), check, damage));
			}
		}
	}

	public synchronized static ItemStack[] putStack(ItemStack[] items, ItemStack toAdd) {
		int amt = toAdd.getAmount();
		for (int i = 0; i < items.length; ++i) {
			if (items[i] == null || items[i].getAmount() == 0) {
				items[i] = toAdd;
				return items;
			} else if ((items[i].getTypeId() == toAdd.getTypeId()
					&& ((items[i].getAmount() < 64 && !noStack.contains(toAdd.getTypeId()))))) {
				if (64 - items[i].getAmount() >= amt) {
					items[i].setAmount(items[i].getAmount() + amt);
					return items;
				} else {
					amt -= 64 - items[i].getAmount();
					items[i].setAmount(64);
				}
			}
		}
		return items;
	}

	public synchronized static ItemStack[] getContents(Chest chest) {
		Chest otherChest = otherChest(chest.getBlock());
		if (otherChest == null) {
			return chest.getInventory().getContents();
		} else {
			// return with the top portion first
			if (otherChest.getX() < chest.getX()
					|| otherChest.getZ() < chest.getZ()) {
				ArrayList<ItemStack> t = new ArrayList<ItemStack>();
				t.addAll(Arrays.asList(otherChest.getInventory().getContents()));
				t.addAll(Arrays.asList(chest.getInventory().getContents()));
				return t.toArray(new ItemStack[0]);
			} else {
				ArrayList<ItemStack> t = new ArrayList<ItemStack>();
				t.addAll(Arrays.asList(chest.getInventory().getContents()));
				t.addAll(Arrays.asList(otherChest.getInventory().getContents()));
				return t.toArray(new ItemStack[0]);
			}
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

	public synchronized static ItemStack[] getContentsSummary(ItemStack[] items) {
		ArrayList<ItemStack> summ = new ArrayList<ItemStack>();
		for (ItemStack i : items) {
			if (i != null) {
				boolean found = false;
				for (ItemStack s : summ) {
					if (i.getTypeId() == s.getTypeId() && i.getDurability() == s.getDurability()) {
						s.setAmount(s.getAmount() + i.getAmount());
						found = true;
						break;
					}
				}
				if (!found) {
					summ.add(new ItemStack(i.getTypeId(), i.getAmount(), i.getDurability()));
				}
			}
		}
		return summ.toArray(new ItemStack[0]);
	}

	public synchronized static void setContents(Chest chest, ItemStack iss[]) {
		if (iss.length == 27) {
			chest.getInventory().setContents(iss);
		} else if (iss.length == 27 * 2) {
			ItemStack iss1[] = new ItemStack[27];
			ItemStack iss2[] = new ItemStack[27];
			System.arraycopy(iss, 0, iss1, 0, iss1.length);
			System.arraycopy(iss, 27, iss2, 0, iss2.length);
			Chest otherChest = otherChest(chest.getBlock());
			if (otherChest == null) {
				chest.getInventory().setContents(iss);
			} else {
				if (otherChest.getX() < chest.getX()
						|| otherChest.getZ() < chest.getZ()) {
//					for (int i = 0; i < 27; ++i) {
//						chest.getInventory().setItem(i, iss[i + 27]);
//						otherChest.getInventory().setItem(i, iss[i]);
//					}
					chest.getInventory().setContents(iss2);
					otherChest.getInventory().setContents(iss1);
				} else {
//					for (int i = 0; i < 27; ++i) {
//						chest.getInventory().setItem(i, iss[i]);
//						otherChest.getInventory().setItem(i, iss[i + 27]);
//					}
					chest.getInventory().setContents(iss1);
					otherChest.getInventory().setContents(iss2);
				}
			}
		}
	}

	public synchronized static void addContents(Chest chest, ItemStack is) {
		Chest otherChest = otherChest(chest.getBlock());
		if (otherChest == null) {
			chest.getInventory().addItem(is);
		} else {
			if (otherChest.getX() < chest.getX()
					|| otherChest.getZ() < chest.getZ()) {
				if (!is_full(otherChest.getInventory().getContents(), is)) {
					otherChest.getInventory().addItem(is);
				} else {
					chest.getInventory().addItem(is);
				}
			} else { // if (!is_full(chest.getInventory().getContents(), is)) {
				if (!is_full(chest.getInventory().getContents(), is)) {
					chest.getInventory().addItem(is);
				} else {
					otherChest.getInventory().addItem(is);
				}
			}
		}
	}

	public synchronized static void addContentsStack(Chest chest, ItemStack is) {
		Chest otherChest = otherChest(chest.getBlock());
		if (otherChest == null) {
			chest.getInventory().addItem(is);
		} else {
			if (otherChest.getX() < chest.getX()
					|| otherChest.getZ() < chest.getZ()) {
				if (!is_full(otherChest.getInventory().getContents(), is)) {
					otherChest.getInventory().setContents(putStack(otherChest.getInventory().getContents(), is));
				} else {
					chest.getInventory().setContents(putStack(chest.getInventory().getContents(), is));
				}
			} else { // if (!is_full(chest.getInventory().getContents(), is)) {
				if (!is_full(chest.getInventory().getContents(), is)) {
					chest.getInventory().setContents(putStack(chest.getInventory().getContents(), is));
				} else {
					otherChest.getInventory().setContents(putStack(otherChest.getInventory().getContents(), is));
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
		Chest otherChest = otherChest(c.getBlock());
		if (otherChest != null && otherChest.getX() < c.getX()
				|| otherChest.getZ() < c.getZ()) {
			return otherChest;
		} else {
			return c;
		}
	}
} // end class ChestManip

