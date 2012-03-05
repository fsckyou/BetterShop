/**
 * Copyright (C) 2012 Jacob Scott <jascottytechie@gmail.com>
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

import me.jascotty2.lib.bukkit.item.JItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class JInventory {

	private final ItemStack[] inv;
	/**
	 * if when adding or condensing can stack normally unstackable items
	 */
	protected boolean allowExtraStack = false;

	public JInventory() {
		inv = new ItemStack[36];
	}

	public JInventory(PlayerInventory toCopy) {
		if (toCopy == null) {
			throw new NullPointerException("PlayerInventory cannot be null");
		}
		inv = new ItemStack[toCopy.getSize()];
		setContents(toCopy.getContents());
	}

	public JInventory(PlayerInventory toCopy, boolean canStack) {
		if (toCopy == null) {
			throw new NullPointerException("PlayerInventory cannot be null");
		}
		inv = new ItemStack[toCopy.getSize()];
		setContents(toCopy.getContents());
		allowExtraStack = canStack;
	}

	public JInventory(ItemStack[] toCopy) {
		inv = new ItemStack[toCopy.length];
		setContents(toCopy);
	}

	public JInventory(ItemStack[] toCopy, boolean canStack) {
		inv = new ItemStack[toCopy.length];
		setContents(toCopy);
		allowExtraStack = canStack;
	}

	public void setExtraStacking(boolean canStack) {
		allowExtraStack = canStack;
	}

	public final void setContents(ItemStack[] toCopy) {
		if (toCopy == null || toCopy.length != inv.length) {
			throw new IllegalArgumentException("cannot copy array of differing size");
		}
		for (int i = 0; i < inv.length; ++i) {
			inv[i] = toCopy[i] == null ? null : toCopy[i].clone();
		}
	}

	public int size() {
		return inv.length;
	}

	public ItemStack[] getContents() {
		return inv;
	}

	public ItemStack get(int i) {
		if (i < 0 || i >= inv.length) {
			throw new ArrayIndexOutOfBoundsException("Index out of range: " + i);
		}
		return inv[i];
	}

	public void set(int i, ItemStack it) {
		if (i < 0 || i >= inv.length) {
			throw new ArrayIndexOutOfBoundsException("Index out of range: " + i);
		}
		inv[i] = it;
	}

	public int firstEmpty() {
		for (int i = 0; i < inv.length; ++i) {
			if (inv[i] == null) {
				return i;
			}
		}
		return -1;
	}

	public int find(ItemStack it) {
		return it == null ? firstEmpty() : find(it.getTypeId(), it.getDurability());
	}

	public int find(ItemStack it, boolean exact) {
		if (!exact) {
			return it == null ? firstEmpty() : find(it.getTypeId(), it.getDurability());
		} else {
			for (int i = 0; i < inv.length; ++i) {
				if (inv[i] != null
						&& inv[i].getTypeId() == it.getTypeId()
						&& inv[i].getDurability() == it.getDurability()
						&& inv[i].getEnchantments().equals(it.getEnchantments())) {
					return i;
				}
			}
			return -1;
		}
	}

	public int find(Material m) {
		return m == null ? firstEmpty() : find(m.getId(), (short) 0);
	}

	public int find(Material m, short d) {
		return m == null ? firstEmpty() : find(m.getId(), d);
	}

	public int find(int id) {
		return find(id, (short) 0);
	}

	public int find(int id, short d) {
		for (int i = 0; i < inv.length; ++i) {
			if (inv[i] != null
					&& inv[i].getTypeId() == id
					&& (!JItems.hasData(id) || inv[i].getDurability() == d)) {
				return i;
			}
		}
		return -1;
	}

	public ItemStack getFirstItem(ItemStack it) {
		if(it == null) {
			throw new NullPointerException("ItemStack cannot be null");
		}
		int i = find(it.getTypeId(), it.getDurability());
		return i > -1 ? inv[i] : null;
	}

	public ItemStack getFirstItem(ItemStack it, boolean exact) {
		if(it == null) {
			throw new NullPointerException("ItemStack cannot be null");
		}
		int i = exact ? find(it, true) : find(it.getTypeId(), it.getDurability());
		return i > -1 ? inv[i] : null;
	}
	
	public ItemStack getFirstItem(Material m) {
		return getFirstItem(m.getId(), (short) 0);
	}

	public ItemStack getFirstItem(Material m, short d) {
		return getFirstItem(m.getId(), d);
	}

	public ItemStack getFirstItem(int id) {
		return getFirstItem(id, (short) 0);
	}

	public ItemStack getFirstItem(int id, short d) {
		int i = find(id, d);
		return i > -1 ? inv[i] : null;
	}

	public boolean contains(ItemStack it) {
		if(it == null) {
			throw new NullPointerException("ItemStack cannot be null");
		}
		return find(it.getTypeId(), it.getDurability()) > -1;
	}

	public boolean contains(ItemStack it, boolean exact) {
		if(it == null) {
			throw new NullPointerException("ItemStack cannot be null");
		}
		return (exact ? find(it, true) : find(it.getTypeId(), it.getDurability())) > -1;
	}
	
	public boolean contains(Material m) {
		return contains(m.getId(), (short) 0);
	}

	public boolean contains(Material m, short d) {
		return contains(m.getId(), d);
	}

	public boolean contains(int id) {
		return contains(id, (short) 0);
	}

	public boolean contains(int id, short d) {
		return find(id, d) > -1;
	}
	
	public int count() {
		int c = 0;
		for (int i = 0; i < inv.length; ++i) {
			if (inv[i] != null) {
				c += inv[i].getAmount();
			}
		}
		return c;
	}

	public int count(Material m) {
		return m == null ? countFree() : count(m.getId(), (short) 0);
	}

	public int count(Material m, short d) {
		return m == null ? countFree() : count(m.getId(), d);
	}

	public int count(int id) {
		return count(id, (short) 0);
	}

	public int count(int id, short d) {
		int c = 0;
		for (int i = 0; i < inv.length; ++i) {
			if (inv[i] != null && inv[i].getTypeId() == id
					&& (!JItems.hasData(id) || inv[i].getDurability() == d)) {
				c += inv[i].getAmount();
			}
		}
		return c;
	}

	public int countFree() {
		int c = 0;
		for (int i = 0; i < inv.length; ++i) {
			if (inv[i] == null || inv[i].getAmount() == 0) {
				++c;
			}
		}
		return c;
	}

	public int countFree(Material m) {
		return m == null ? countFree() : countFree(m.getId(), (short) 0);
	}

	public int countFree(Material m, short d) {
		return m == null ? countFree() : countFree(m.getId(), d);
	}

	public int countFree(int id) {
		return countFree(id, (short) 0);
	}

	public int countFree(int id, short d) {
		int c = 0;
		int max = allowExtraStack && JItems.isStackable(id) ? 64 : JItems.getMaxStack(id, d);
		for (int i = 0; i < inv.length; ++i) {
			if (inv[i] == null || inv[i].getAmount() == 0) {
				c += max;
			} else if (inv[i].getAmount() < max
					&& inv[i].getTypeId() == id
					&& (!JItems.hasData(id) || inv[i].getDurability() == d)) {
				c += max - inv[i].getAmount();
			}
		}
		return c;
	}

	public void condense() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	// condense with enchantment checks & not stacking items that shouldn't stack
	public void safeCondense() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
