/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: ( static methods for manipulating itemstacks )
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
import java.util.Arrays;
import java.util.List;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.bukkit.item.JItems;
import me.jascotty2.lib.bukkit.item.Kit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackManip {

	/**
	 * items that should not stack
	 */
	// mushroom soup, buckets
	public final static List<Integer> noStack = Arrays.asList(282, 325, 326, 327, 335);

	/**
	 * check if this stack cannot hold one more of this item
	 * @param items
	 * @param check
	 * @return
	 */
	public static boolean is_full(ItemStack[] items, ItemStack check) {
		return is_full(items, check, false);
	}

	/**
	 * check if this stack cannot hold one more of this item
	 * @param items
	 * @param check
	 * @param extraStack if to assume is allowed to stack some unstackables
	 * @return
	 */
	public static boolean is_full(ItemStack[] items, ItemStack check, boolean extraStack) {
		int amt = check.getAmount();
		for (ItemStack item : items) {
			int mx = !extraStack || noStack.contains(item == null ? 0 : item.getTypeId())
					? JItems.getMaxStack(item) : 64;
			if (item == null || item.getAmount() == 0
					|| (item.getTypeId() == check.getTypeId()
					&& (item.getAmount() + amt <= mx))) {
				return false;
			} else if (item.getTypeId() == check.getTypeId()) {
				amt -= mx - item.getAmount();
			}
		}
		return true;
	}

	/**
	 * count how many of this item is in the ItemStack
	 * @param items
	 * @param check
	 * @return
	 */
	public static int count(ItemStack[] items, JItem check) {
		if (check == null) {
			return emptySlots(items);
		}
		int amt = 0;
		for (ItemStack item : items) {
			if (item != null && check.equals(item)) {
				amt += item.getAmount();
			}
		}
		return amt;
	}

	/**
	 * count how many times this material occurs in the ItemStack
	 * @param items
	 * @param check
	 * @return
	 */
	public static int count(ItemStack[] items, Material check) {
		if (check == null) {
			return emptySlots(items);
		}
		int amt = 0;
		for (ItemStack item : items) {
			if (item != null && item.getType() == check) {
				amt += item.getAmount();
			}
		}
		return amt;
	}

	/**
	 * count how many slots in this ItemStack array are empty
	 * @param items
	 * @return
	 */
	public static int emptySlots(ItemStack[] items) {
		int amt = 0;
		if (items != null) {
			for (ItemStack item : items) {
				if (item == null || item.getAmount() <= 0) {
					++amt;
				}
			}
		}
		return amt;
	}

	public static boolean contains(ItemStack[] items, Material check) {
		if (items != null) {
			if (check == null) {
				for (ItemStack item : items) {
					if (item == null) {
						return true;
					}
				}
			} else {
				for (ItemStack item : items) {
					if (item != null && item.getType() == check) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean contains(ItemStack[] items, Material check, short damage) {
		if (items != null) {
			if (check == null) {
				for (ItemStack item : items) {
					if (item == null) {
						return true;
					}
				}
			} else {
				for (ItemStack item : items) {
					if (item != null && item.getType() == check
							&& item.getDurability() == damage) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean canHold(ItemStack[] items, JItem check, int amt, boolean extraStack){
		return amountCanHold(items, check, extraStack) >= amt;
	}
	
	public static int amountCanHold(ItemStack[] items, JItem check, boolean extraStack) {
		int amt = 0;
		if (items == null) {
			return 0;
		} else if (check == null) {
			return emptySlots(items);
		} else if (check.isEntity()) {
			return -1;
		} else if (check.isKit()) {
			Kit kit = check instanceof Kit ? (Kit) check : JItemDB.getKit(check);
			Kit.KitItem kititems[] = kit.getKitItems();
			ItemStack invCopy[] = copy(items);
			// loop through & "add" one at a time
			while (true) {
				int numtoadd = 0;
				for (int itn = 0; itn < kititems.length; ++itn) {
					numtoadd = kititems[itn].itemAmount;
					int maxStack = !extraStack || noStack.contains(kititems[itn].ID()) ? kititems[itn].MaxStackSize() : 64;
					for (int i = 0; i < invCopy.length && numtoadd > 0; ++i) {
						if (invCopy[i] == null || invCopy[i].getAmount() == 0) {
							invCopy[i] = kititems[itn].toItemStack();
							invCopy[i].setAmount(numtoadd);
							numtoadd -= numtoadd;
						} else if (invCopy[i].getAmount() < maxStack && kititems[itn].iequals(invCopy[i])) {
							int d = maxStack < numtoadd ? maxStack : numtoadd;
							invCopy[i].setAmount(invCopy[i].getAmount() + d);
							numtoadd -= d;
						}
					}
					if (numtoadd > 0) {
						// has scanned through full stack & cannot add more
						return amt;
					}
				}
				// 1 was added to the copy
				++amt;
			}
		} else {
			for (ItemStack item : items) {
				int mx = !extraStack || (item != null && noStack.contains(item.getTypeId())) ? check.MaxStackSize() : 64;
				if (item == null || item.getAmount() == 0
						|| (check.equals(item) && item.getAmount() <= mx)) {
					amt += mx - (item == null ? 0 : item.getAmount());
				}
			}
		}
		return amt;
	}

	public static ItemStack[] remove(ItemStack[] items, ItemStack check) {
		return remove(items, check, 0);
	}

	public static ItemStack[] remove(ItemStack[] items, ItemStack check, int start) {
		if (items != null) {
			int total = check.getAmount();
			for (int i = start; i < items.length && total > 0; ++i) {
				if (items[i] != null) {
					if (items[i].getType() == check.getType()
							&& (items[i].getData() == null
							|| items[i].getData().getData() == check.getData().getData())) {
						int a = items[i].getAmount();
						if (total < a) {
							items[i].setAmount(a - total);
							total = 0;
						} else {
							items[i] = null;
							total -= a;
						}
						return items;
					}
				}
			}
		}
		return items;
	}

	public static ItemStack[] remove(ItemStack[] items, Material check) {
		for (int i = 0; i < items.length; ++i) {
			if (items[i] != null) {
				if (items[i].getType() == check) {
					if (items[i].getAmount() > 1) {
						items[i].setAmount(items[i].getAmount() - 1);
					} else {
						items[i] = null;
					}
					return items;
				}
			}
		}
		return items;
	}

	public static ItemStack[] remove(ItemStack[] items, Material check, short damage) {
		for (int i = 0; i < items.length; ++i) {
			if (items[i] != null) {
				if (items[i].getType() == check && items[i].getDurability() == damage) {
					if (items[i].getAmount() > 1) {
						items[i].setAmount(items[i].getAmount() - 1);
					} else {
						items[i] = null;
					}
					return items;
				}
			}
		}
		return items;
	}

	public static ItemStack[] remove(ItemStack[] items, JItem search, int amt) {
		return remove(items, search, amt, 0);
	}

	public static ItemStack[] remove(ItemStack[] items, JItem search, int total, int start) {
		if (items == null) {
			return null;
		} else if (search == null) {
			return items;
		} else if (!search.isKit() && !search.isEntity()) {
			for (int i = start; i < items.length && total > 0; ++i) {
				if (items[i] != null && search.equals(items[i])) {
					int a = items[i].getAmount();
					if (total < a) {
						items[i].setAmount(a - total);
						total = 0;
					} else {
						items[i] = null;
						total -= a;
					}
				}
			}
		}
		return items;
	}

	public static ItemStack[] remove(ItemStack[] items, ItemStack[] search) {
		return remove(items, search, 0);
	}

	public static ItemStack[] remove(ItemStack[] items, ItemStack[] search, int start) {
		for (ItemStack i : search) {
			remove(items, i, start);
		}
		return items;
	}

	public static ItemStack[] remove(ItemStack[] items, List<ItemStack> search) {
		return remove(items, search, 0);
	}

	public static ItemStack[] remove(ItemStack[] items, List<ItemStack> search, int start) {
		for (ItemStack i : search) {
			remove(items, i, start);
		}
		return items;
	}

	public static ItemStack[] add(ItemStack[] items, JItem toAdd, int amt) {
		return add(items, toAdd, amt, false);
	}

	public static ItemStack[] add(ItemStack[] items, JItem toAdd, int amt, boolean extraStack) {
		if (items == null) {
			return null;
		} else if (toAdd == null) {
			return items;
		} else if (toAdd.IsValidItem()) {
			int mx = !extraStack || noStack.contains(toAdd.ID()) ? toAdd.MaxStackSize() : 64;
			while(amt > 0){
				add(items, toAdd.toItemStack(amt > mx ? mx : amt), extraStack);
				amt -= mx;
			}
			return items;
		} else if (toAdd.isKit()) {
			Kit kit = toAdd instanceof Kit ? (Kit) toAdd : JItemDB.getKit(toAdd);
			Kit.KitItem kititems[] = kit.getKitItems();

			for (int num = 0; num < amt; ++num) {
				int numtoadd = 0;
				for (int itn = 0; itn < kit.numItems(); ++itn) {
					numtoadd = kititems[itn].itemAmount;
					int maxStack = !extraStack || noStack.contains(kititems[itn].ID()) ? kititems[itn].MaxStackSize() : 64;
					for (int i = 0; i < items.length && numtoadd > 0; ++i) {
						if (items[i] == null || items[i].getAmount() == 0) {
							items[i] = kititems[itn].toItemStack();
							items[i].setAmount(numtoadd);
							numtoadd -= numtoadd;
						} else if (items[i].getAmount() < maxStack && kititems[itn].iequals(items[i])) {
							int d = maxStack < numtoadd ? maxStack : numtoadd;
							items[i].setAmount(items[i].getAmount() + d);
							numtoadd -= d;
						}
					}
					if (numtoadd > 0) {
						break;
					}
				}
				if (numtoadd > 0) {
					//early exit while adding
					break;
				}
			}
		}
		return items;
	}

	/**
	 * add an ItemStack to another
	 * @param items
	 * @param toAdd
	 * @return
	 */
	public static ItemStack[] add(ItemStack[] items, ItemStack toAdd) {
		return add(items, toAdd, false);
	}

	/**
	 * add an ItemStack to an array
	 * @param items
	 * @param toAdd
	 * @param extraStack whether to allow some nonstackable items to stack
	 * @return
	 */
	public static ItemStack[] add(ItemStack[] items, ItemStack toAdd, boolean extraStack) {
		int amt = toAdd.getAmount();
		int mx = !extraStack || noStack.contains(toAdd.getTypeId())
						? JItems.getMaxStack(toAdd) : 64;
		boolean firstRun = true;
		for (int i = 0; i < items.length; ++i) {
			if (!firstRun && (items[i] == null || items[i].getAmount() == 0)) {
				items[i] = toAdd;
				items[i].setAmount(amt);
				return items;
			} else if (items[i] != null 
					&& items[i].getTypeId() == toAdd.getTypeId() 
					&& (!JItems.hasData(toAdd.getTypeId()) || items[i].getData().getData() == toAdd.getData().getData())
					&& items[i].getAmount() < mx) {
				// on first run, look for other stacks in array that could be incremented instead
				if (items[i].getAmount() + amt <= mx) {
					items[i].setAmount(items[i].getAmount() + amt);
					return items;
				} else {
					amt -= mx - items[i].getAmount();
					items[i].setAmount(mx);
				}
			} else if (firstRun && i + 1 >= items.length) {
				firstRun = false;
				i = -1; // negative, because gets incremented again 
			}
		}
		return items;
	}

	/**
	 * creates a summary of the itemstack array, <br/>
	 * so that each item is only listed once
	 * @param items
	 * @return
	 */
	public static List<ItemStack> itemStackSummary(ItemStack[] items) {
		return itemStackSummary(items, 0);
	}

	public static List<ItemStack> itemStackSummary(ItemStack[] items, int start) {
		ArrayList<ItemStack> summ = new ArrayList<ItemStack>();
		if (items != null) {
			for (int i = start; i < items.length; ++i) {
				if (items[i] != null) {
					int iti = indexOf(summ, items[i]);
					if (iti < 0) {
						summ.add(items[i].clone());
					} else {
						summ.get(iti).setAmount(summ.get(iti).getAmount() + items[i].getAmount());
					}
				}
			}
		}
		return summ;
	}

	public static List<ItemStack> itemStackSummary(ItemStack[] items, JItem[] search, int start) {
		ArrayList<ItemStack> summ = new ArrayList<ItemStack>();
		if (items != null) {
			for (int i = start; i < items.length; ++i) {
				if (items[i] != null && (search == null || JItem.contains(search, items[i]))) {
					int iti = indexOf(summ, items[i]);
					if (iti < 0) {
						summ.add(items[i].clone());
					} else {
						summ.get(iti).setAmount(summ.get(iti).getAmount() + items[i].getAmount());
					}
				}
			}
		}
		return summ;
	}

	/**
	 * checks for differences between two itemstack arrays <br/>
	 * result amounts are second - first <br/>
	 * if there are less of an item in the second, the result will have a negative amount
	 * @param stack1 first to compare against
	 * @param stack2 second to check for differences
	 * @return list of results
	 */
	public static List<ItemStack> itemStackDifferences(ItemStack[] stack1, ItemStack[] stack2) {
		ArrayList<ItemStack> changedItems = new ArrayList<ItemStack>();
		if (stack1 == null) {
			changedItems.addAll(Arrays.asList(stack1));
			return changedItems;
		} else if (stack2 == null) {
			changedItems.addAll(Arrays.asList(stack2));
			return changedItems;
		}
		// first, compile list of items before shopping
		List<ItemStack> oldInventory = itemStackSummary(stack1);
		// and after
		List<ItemStack> newInventory = itemStackSummary(stack2);
		// list of differences

		// find those items that have changed / removed to the second
		for (ItemStack i : oldInventory) {
			int iti = indexOf(newInventory, i);
			if (iti >= 0) {
				// in second: check for changes
				if (i.getAmount() != newInventory.get(iti).getAmount()) {
					i.setAmount(newInventory.get(iti).getAmount() - i.getAmount());
					changedItems.add(i);
				}
			} else {
				// not in the second
				i.setAmount(-i.getAmount());
				changedItems.add(i);
			}
		}
		// check for items added to the second
		for (ItemStack i : newInventory) {
			if (indexOf(oldInventory, i) == -1) {
				// not in the first
				changedItems.add(i);
			}
		}
		return changedItems;
	}

	/**
	 * ignoring amount, find the index of an itemstack in a list
	 * @param source list to search
	 * @param search search criteria
	 * @return index, or -1 if not found
	 */
	public static int indexOf(List<ItemStack> source, ItemStack search) {
		int ind = 0;
		if (search == null) {
			for (ItemStack i : source) {
				if (i == null) {
					return ind;
				}
				++ind;
			}
		} else {
			for (ItemStack i : source) {
				if (i != null && i.getType() == search.getType()
						&& i.getDurability() == search.getDurability()){//&& (i.getData() == null || (i.getData().getData() == search.getData().getData()))) {
					return ind;
				}
				++ind;
			}
		}
		return -1;
	}

	/**
	 * ignoring amount, find the index of an itemstack in a list
	 * @param source list to search
	 * @param search search criteria
	 * @return index, or -1 if not found
	 */
	public static int indexOf(List<ItemStack> source, JItem search) {
		int ind = 0;
		if (search == null) {
			for (ItemStack i : source) {
				if (i == null) {
					return ind;
				}
				++ind;
			}
		} else {
			for (ItemStack i : source) {
				if (search.equals(i)) {
					return ind;
				}
				++ind;
			}
		}
		return -1;
	}
	/**
	 * makes a copy of a minecraft ItemStack as a bukkit ItemStack
	 * @param minecraftItemStack
	 * @return
	 */
	public static ItemStack[] copy(net.minecraft.server.ItemStack[] minecraftItemStack) {
		ItemStack[] invCpy = new ItemStack[minecraftItemStack.length];
		for (int i = 0; i < minecraftItemStack.length; ++i) {
			invCpy[i] = minecraftItemStack[i] == null ? null
					: new ItemStack(minecraftItemStack[i].getItem().id,
					minecraftItemStack[i].count,
					(short) minecraftItemStack[i].getData(),
					(byte) minecraftItemStack[i].getData());
		}
		return invCpy;
	}

	/**
	 * makes a copy of a bukkit ItemStack
	 * @param items
	 * @return
	 */
	public static ItemStack[] copy(ItemStack[] items) {
		ItemStack[] invCpy = new ItemStack[items.length];
		for (int i = 0; i < items.length; ++i) {
			invCpy[i] = items[i] == null ? null : items[i].clone();
		}
		return invCpy;
	}
} // end class ItemStackManip

