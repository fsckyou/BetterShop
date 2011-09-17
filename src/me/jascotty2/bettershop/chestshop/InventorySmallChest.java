/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: ( TODO )
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

import net.minecraft.server.EntityHuman;
import net.minecraft.server.IInventory;
import net.minecraft.server.ItemStack;

public class InventorySmallChest implements IInventory {
	ItemStack items[];String name;
	public InventorySmallChest(String name, org.bukkit.inventory.ItemStack itms[]) {
		this.name=name;
		items = new ItemStack[itms.length];
		for(int i=0; i<itms.length; ++i){
			items[i] = itms[i] == null ? null : new ItemStack(
					itms[i].getTypeId(), itms[i].getAmount(), itms[i].getDurability());
					/*,(byte)(itms[i].getData() == null ? 0 : itms[i].getData().getData())*/
		}
	}

	public int getSize() {
		return items.length;
	}

	public ItemStack getItem(int i) {
		return i >= items.length ? null : items[i];
	}

	public ItemStack splitStack(int i, int j) {
        if (i >= this.items.length) {
			return null;
        }
		ItemStack[] aitemstack = this.items;

        if (aitemstack[i] != null) {
            ItemStack itemstack;

            if (aitemstack[i].count <= j) {
                itemstack = aitemstack[i];
                aitemstack[i] = null;
                return itemstack;
            } else {
                itemstack = aitemstack[i].a(j);
                if (aitemstack[i].count == 0) {
                    aitemstack[i] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
	}

	public void setItem(int i, ItemStack is) {
		items[i] = is;
	}

	public String getName() {
		return name;
	}

	public int getMaxStackSize() {
		return 64;
	}

	public void update() {
	}


	public void e() {
	}

	public void t_() {
	}

	public ItemStack[] getContents() {
		return items;
	}

	public boolean a(EntityHuman eh) {
		return true;
	}
	
	public boolean a_(EntityHuman eh) {
		return true;
	}
} // end class InventorySmallChest

